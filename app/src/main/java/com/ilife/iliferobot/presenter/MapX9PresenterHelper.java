package com.ilife.iliferobot.presenter;

import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACException;
import com.accloud.service.ACMsg;
import com.accloud.service.ACObject;
import com.ilife.iliferobot.able.Constants;
import com.ilife.iliferobot.able.DeviceUtils;
import com.ilife.iliferobot.entity.Coordinate;
import com.ilife.iliferobot.model.bean.CleaningDataX8;
import com.ilife.iliferobot.utils.DataUtils;
import com.ilife.iliferobot.utils.MyLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 帮助类，获取服务器数据，解析处理负载数据
 * //TODO 数据排序，界面销毁时，终止任务，二次调用时的数据清空
 */
public class MapX9PresenterHelper {
    private String TAG = "MapX9PresenterHelper";
    private ExecutorService fixedThread;//用户解析实时地图的历史数据
    private int pageNo;
    private volatile boolean isStop;//同步
    private CleaningDataX8[] dataX8s;
    private OnHistoryDataResponse mOnResponse;
    private CompositeDisposable mComDisposable;
    private final int SUB_LENGTH = 200;//每200包数据开一个线程去解析
    /**
     * 线程计数器，用于监听到所有线程任务执行完毕后，继续执行当前线程;
     * 改变量的初始值需为已知的线程数量，不然会导致当前线程无限等待；
     */
    private CountDownLatch countDownLatch;

    public MapX9PresenterHelper(OnHistoryDataResponse mOnResponse) {
        this.mOnResponse = mOnResponse;
    }

    public void getHistoryData(long deviceId, String subdomain) {
        isStop = false;//初次开始或者重入
        pageNo = 1;
        List<String> historyData = new ArrayList<>();
        Disposable d = Single.create((SingleOnSubscribe<ACMsg>) emitter -> {
            if (isStop) {//page has been destroyed
                emitter.onError(new Exception("you need to retry after a while as the view is not attach"));
            } else {
                MyLogger.d(TAG, "getHistoryDataX8-------------------------------pageNo:" + pageNo);
                MyLogger.d(TAG, "111111111     " + (Looper.getMainLooper() == Looper.myLooper()));
                ACMsg req = new ACMsg();
                req.setName("searchCleanRealTimeMore");
                req.put("device_id", deviceId);
                req.put("pageNo", pageNo);
                AC.sendToServiceWithoutSign(DeviceUtils.getServiceName(subdomain), Constants.SERVICE_VERSION, req, new PayloadCallback<ACMsg>() {
                    @Override
                    public void success(ACMsg resp) {
                        MyLogger.d(TAG, "getHistoryDataX8,and success to get the data of page " + pageNo);
                        MyLogger.d(TAG, "22222222     " + (Looper.getMainLooper() == Looper.myLooper()));
                        emitter.onSuccess(resp);
                    }

                    @Override
                    public void error(ACException e) {
                        // TODO 需处理当历史地图请求失败时，需重新请求
                        emitter.onError(e);
                    }
                });
            }


        }).observeOn(Schedulers.single()).map(acMsg -> {
            MyLogger.d(TAG, "333333     " + (Looper.getMainLooper() == Looper.myLooper()));
            ArrayList<ACObject> data = acMsg.get("data");
            if (data != null && data.size() > 0) {
                for (int i = 0; i < data.size(); i++) {
                    historyData.add(data.get(i).getString("clean_data"));
                }
            }
            if (data == null || data.size() != 1000) {//1000条是单页数据上限
                pageNo = -1;
            } else {
                pageNo++;
            }
            return true;
        }).retry(2).repeatUntil(() -> {
            MyLogger.e(TAG, "Ready to get the data of page " + pageNo + ", it won't work if the pageNo is -1");
            return pageNo == -1;
        }).subscribe(aBoolean -> {
            if (pageNo == -1) {//-1代表历史清扫数据已经查询完毕
                MyLogger.d(TAG, "4444------------数据量：" + historyData.size());
                if (historyData.size() > 0) {//有数据，开启多线程去解析数据
                    int threadNumbs = historyData.size() / SUB_LENGTH;
                    if (historyData.size() % SUB_LENGTH != 0) {
                        threadNumbs += 1;
                    }
                    fixedThread = Executors.newFixedThreadPool(threadNumbs);
                    dataX8s = new CleaningDataX8[threadNumbs];
                    countDownLatch = new CountDownLatch(threadNumbs);
                    for (int i = 0; i < threadNumbs; i++) {//多线程分段处理数据
                        fixedThread.execute(new ParseRunnable(historyData.subList(SUB_LENGTH * i, i == threadNumbs - 1 ? historyData.size() : SUB_LENGTH * (i + 1)), i));
                    }
                    try {
                        MyLogger.e(TAG, "确保方法不是在主线程调用，避免阻塞Ui,阻塞当前线程,等待数据处理完毕----------------");
                        countDownLatch.await();
                        MyLogger.e(TAG, "数据已经处理完成，线程继续执行-------------------");
                        mOnResponse.onHistoryData(dataX8s);
                        cancelOrFinish();
                    } catch (InterruptedException e) {
                        MyLogger.e(TAG, "线程被中断-----------------");
                    }
                } else {
                    cancelOrFinish();
                    mOnResponse.onHistoryData(null);
                }

            }
        }, throwable -> {
            mOnResponse.onFail();
            MyLogger.e(TAG, "Failed to get history map data,and you need to retry sometime" + " and the reason is :" + throwable.getMessage());
        });
        mComDisposable = new CompositeDisposable();
        mComDisposable.add(d);
    }

    public void cancelOrFinish() {
        isStop = true;
        if (mComDisposable != null && !mComDisposable.isDisposed()) {
            mComDisposable.dispose();
        }
        if (fixedThread != null) {
            fixedThread.shutdownNow();
        }
        dataX8s = null;
        MyLogger.e(TAG, "cancelOrFinish：页面销毁或者数据处理完成!");
        //TODO 中断所有解析数据的线程
    }

    class ParseRunnable implements Runnable {
        private List<String> subData;
        private CleaningDataX8 dataX8;
        private int index;

        ParseRunnable(List<String> subData, int index) {
            this.subData = subData;
            this.index = index;
            this.dataX8 = new CleaningDataX8();
            dataX8s[index] = dataX8;
        }

        @Override
        public void run() {
            MyLogger.d(TAG, "------------开始解析数据：" + index);
            if (!isStop) {
                for (int i = 0; i < subData.size(); i++) {
                    if (isStop) {
                        break;//退出线程
                    }
                    parseRealTimeMapX8(subData.get(i));
                }
            }
            countDownLatch.countDown();//当前线程执行完毕需要-1；
        }

        /**
         * 解析x800黄方格地图数据
         *
         * @param clean_data
         */
        private void parseRealTimeMapX8(String clean_data) {
            if (!TextUtils.isEmpty(clean_data)) {
                byte[] bytes = Base64.decode(clean_data, Base64.DEFAULT);
                if ((bytes.length % 4) != 0) {
                    return;
                }
                byte[] byte_area = new byte[]{bytes[0], bytes[1]};
                byte[] byte_time = new byte[]{bytes[2], bytes[3]};
                int tempWorkTime = DataUtils.bytesToInt2(byte_time, 0);
                int tempCleanArea = DataUtils.bytesToInt2(byte_area, 0);
                dataX8.setCleanArea(tempCleanArea);
                dataX8.setWorkTime(tempWorkTime);
                Coordinate coordinate;
                for (int j = 7; j < bytes.length; j += 4) {
                    int x = DataUtils.bytesToInt(new byte[]{bytes[j - 3], bytes[j - 2]}, 0);
                    int y = DataUtils.bytesToInt(new byte[]{bytes[j - 1], bytes[j]}, 0);
                    if ((x == 0x7fff) & (y == 0x7fff)) {//数据清空标记，遇到此标记，该数据包以前的数据都是无效的。
                        MyLogger.e(TAG, "the map data has been cleaned and reset");
                        dataX8.setHaveClearFlag(true);
                    } else {
                        coordinate = new Coordinate(x, 1500 - y);
                        if (isStop) {
                            MyLogger.e(TAG, "the page has been destroyed，the data processing will make no sense/the data processing is no longer meaningful!");
                            break;
                        } else {
                            dataX8.addCoordinate(coordinate);
                        }
                    }
                }
            }
        }
    }

    interface OnHistoryDataResponse {
        void onHistoryData(CleaningDataX8[] data);

        void onFail();
    }
}
