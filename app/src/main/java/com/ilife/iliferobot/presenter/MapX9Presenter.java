package com.ilife.iliferobot.presenter;

import android.text.TextUtils;
import android.util.Base64;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACClassDataMgr;
import com.accloud.service.ACDeviceDataMgr;
import com.accloud.service.ACDeviceMsg;
import com.accloud.service.ACException;
import com.accloud.service.ACMsg;
import com.accloud.service.ACObject;
import com.accloud.utils.LogUtil;
import com.google.gson.Gson;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.able.ACSkills;
import com.ilife.iliferobot.able.Constants;
import com.ilife.iliferobot.able.DeviceUtils;
import com.ilife.iliferobot.able.MsgCodeUtils;
import com.ilife.iliferobot.activity.BaseMapActivity;
import com.ilife.iliferobot.activity.MainActivity;
import com.ilife.iliferobot.activity.SettingActivity;
import com.ilife.iliferobot.app.MyApplication;
import com.ilife.iliferobot.base.BasePresenter;
import com.ilife.iliferobot.contract.MapX9Contract;
import com.ilife.iliferobot.entity.PropertyInfo;
import com.ilife.iliferobot.entity.RealTimeMapInfo;
import com.ilife.iliferobot.utils.DataUtils;
import com.ilife.iliferobot.utils.MyLogger;
import com.ilife.iliferobot.utils.SpUtils;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.utils.Utils;

import org.reactivestreams.Publisher;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

// TODO APP后台CPU消耗问题
//TODO 处理x800系列绘制地图不全部绘制，只绘制新增的点
//TODO 重点，延边，遥控模式下机器会清扫完成，此时可以清空地图数据
public class MapX9Presenter extends BasePresenter<MapX9Contract.View> implements MapX9Contract.Presenter {
    private final String TAG = "MapX9Presenter";
    private ACDeviceDataMgr.PropertyReceiver propertyReceiver;
    public static final String KEY_IS_MAX = "isMaxMode";
    public static final String KEY_MOP_FORCE = "mopForce";
    public static final String KEY_VOICE_OPEN = "voiceOpen";
    private long deviceId;
    private String physicalId, subdomain, robotType;
    private Gson gson;
    private byte[] slamBytes;
    private int curStatus, errorCode, batteryNo = -1, workTime, cleanArea, virtualStatus;
    private ArrayList<Integer> realTimePoints, historyRoadList;
    private List<int[]> wallPointList = new ArrayList<>();
    private List<int[]> existPointList = new ArrayList<>();
    private boolean isMaxMode, voiceOpen;
    private static final int STATUS_FLAG_COMPLETION = 3;
    private int statusFlag = STATUS_FLAG_COMPLETION;
    /**
     * 实时地图相关
     */
    private byte[] bytes_subscribe;
    private int package_index = 1;
    private long lastStartTime;

    /**
     * 查询设备状态相关
     */
    private int mopForce;
    private int device_type;
    private byte[] virtualContentBytes;
    /**
     * x800实时地图数据
     */
    private ArrayList<Integer> pointList;// map集合
    private boolean isSubscribeRealMap, isInitSlamTimer, isGainDevStatus, isGetHistory;
    private boolean haveMap = true;//标记机型是否有地图 V85机器没有地图，但是有地图清扫数据
    private boolean havMapData = true;//A7 无地图，也无地图清扫数据
    private int minX, maxX, minY, maxY;//数据的边界，X800系列机器会用到
    private CompositeDisposable mComDisposable;
    private int retryTimes = 1;//the retry times of gaining the device status

    @Override
    public void attachView(MapX9Contract.View view) {
        super.attachView(view);
        mComDisposable = new CompositeDisposable();
        gson = new Gson();
        realTimePoints = new ArrayList<>();
        historyRoadList = new ArrayList<>();
        pointList = new ArrayList<>();
        deviceId = SpUtils.getLong(MyApplication.getInstance(), MainActivity.KEY_DEVICEID);
        subdomain = SpUtils.getSpString(MyApplication.getInstance(), MainActivity.KEY_SUBDOMAIN);
        physicalId = SpUtils.getSpString(MyApplication.getInstance(), MainActivity.KEY_PHYCIALID);
        robotType = DeviceUtils.getRobotType(subdomain);
        if (robotType.equals(Constants.XV3PRO) || robotType.equals(Constants.V5x) || robotType.equals(Constants.V85) || robotType.equals(Constants.A7)) {
            haveMap = false;
        }
        if (robotType.equals(Constants.A7) || robotType.equals(Constants.V5x) || robotType.equals(Constants.XV3PRO)) {
            havMapData = false;
        }
        if (robotType.equals(Constants.V5x) || robotType.equals(Constants.XV3PRO)) {//V5x只有随机模式
            SpUtils.saveInt(MyApplication.getInstance(), physicalId + SettingActivity.KEY_MODE, MsgCodeUtils.STATUE_RANDOM);
        }
    }

    @Override
    public String getSubDomain() {
        return subdomain;
    }

    @Override
    public String getPhysicalId() {
        return physicalId;
    }

    @Override
    public String getRobotType() {
        return robotType;
    }


    @Override
    public boolean isX900Series() {
        return robotType.equals(Constants.X900) || robotType.equals(Constants.X910);
    }

    @Override
    public boolean isLongPressControl() {
        return getRobotType().equals(Constants.V85) || getRobotType().equals(Constants.X785) || getRobotType().equals(Constants.X787) || getRobotType().equals(Constants.A7);
    }

    /**
     * 包含3s查询实时地图(slam map)
     */
    public void intervalToObtainSlam() {
        if (!isX900Series()) {//只有x900需要每3s获取实时地图
            return;
        }
        isInitSlamTimer = true;
        MyLogger.d(TAG, "intervalToObtainSlam-------------------------------");
        ACMsg req = new ACMsg();
        req.setName("searchCleanRealTime");
        req.put("device_id", deviceId);
        Disposable disposable = Observable.interval(0, 3, TimeUnit.SECONDS).subscribeOn(Schedulers.single()).
                observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
            LogUtil.d(TAG, "getRealTimeMap---" + curStatus);
            if (!isViewAttached()) {
                return;
            }
            if (isDrawMap()) {
                AC.sendToServiceWithoutSign(DeviceUtils.getServiceName(subdomain), Constants.SERVICE_VERSION, req, new PayloadCallback<ACMsg>() {
                    @Override
                    public void success(ACMsg resp) {
                        MyLogger.d(TAG, "intervalToObtainSlam-------------success------------------");
                        if (!isViewAttached()) {//回冲或者视图销毁后不绘制路径
                            return;
                        }
                        String strMap = resp.getString("slam_map");
                        int xMax = resp.getInt("slam_x_max");
                        int xMin = resp.getInt("slam_x_min");
                        int yMin = 1500 - resp.getInt("slam_y_max");
                        int yMax = 1500 - resp.getInt("slam_y_min");
                        if (!TextUtils.isEmpty(strMap)) {
                            slamBytes = Base64.decode(strMap, Base64.DEFAULT);
                            //判断isViewAttached避免页面销毁后最后一次的定时器导致程序崩溃
                            if (isViewAttached() && isDrawMap()) {
                                mView.updateSlam(xMin, xMax, yMin, yMax, 6, 4);
                                mView.drawMapX9(realTimePoints, historyRoadList, slamBytes);
                            }
                        }
                    }

                    @Override
                    public void error(ACException e) {
                        MyLogger.e(TAG, "Failed to obtain the slam data,and the error information is  " + e.getMessage());
                    }
                });
            }
        });
        mComDisposable.add(disposable);
    }


    @Override
    public void updateSlamX8(ArrayList<Integer> src, int offset) {
        if (src == null || src.size() < 2) {
            return;
        }
        if (minX == 0 && minY == 0 && maxX == 0 && maxY == 0) {
            minX = -src.get(0);
            minY = -src.get(1);
            maxX = -src.get(0);
            maxY = -src.get(1);
            offset = 0;
            MyLogger.d(TAG, "data is  clear, and  need to reset all params");
        }
        int x, y;
        for (int i = offset + 1; i < src.size(); i += 2) {
            x = -src.get(i - 1);
            y = -src.get(i);
            if (minX > x) {
                minX = x;
            }
            if (maxX < x) {
                maxX = x;
            }
            if (minY > y) {
                minY = y;
            }
            if (maxY < y) {
                maxY = y;
            }
        }
        mView.updateSlam(minX, maxX, minY, maxY, 15, 9);
    }

    private int pageNo = 1;// 900 800等机器分页请求历史地图

    /**
     * Repetition only happens after each success.
     */
    private void getHistoryDataX8() {
        pageNo = 1;
        Disposable d = Single.create((SingleOnSubscribe<ACMsg>) emitter -> {
            if (!isViewAttached()) {//page has been destroyed
                emitter.onError(new Exception("you need to retry after a while as the view is not attach"));
            } else {
                MyLogger.d(TAG, "getHistoryDataX8-------------------------------");
                ACMsg req = new ACMsg();
                req.setName("searchCleanRealTimeMore");
                req.put("device_id", deviceId);
                req.put("pageNo", pageNo);
                AC.sendToServiceWithoutSign(DeviceUtils.getServiceName(subdomain), Constants.SERVICE_VERSION, req, new PayloadCallback<ACMsg>() {
                    @Override
                    public void success(ACMsg resp) {
                        MyLogger.d(TAG, "getHistoryDataX8,and success to get the data of page " + pageNo);
                        emitter.onSuccess(resp);
                    }

                    @Override
                    public void error(ACException e) {
                        // TODO 需处理当历史地图请求失败时，需重新请求
                        emitter.onError(e);
                    }
                });
            }


        }).map(acMsg -> {
            ArrayList<ACObject> data = acMsg.get("data");
            if (data != null && data.size() > 0) {
                for (int i = 0; i < data.size(); i++) {
                    parseRealTimeMapX8(data.get(i).getString("clean_data"));
                }
                updateSlamX8(pointList, 0);
            }
            if (data == null || data.size() != 1000) {
                pageNo = -1;
            } else {
                pageNo++;
            }
            return true;
        }).retry(2).repeatUntil(() -> {
            MyLogger.e(TAG, "Ready to get the data of page " + pageNo + ", it won't work if the pageNo is -1");
            return pageNo == -1;
        }).observeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread()).subscribe(aBoolean -> {
            isGetHistory = true;
            mView.updateCleanTime(getTimeValue());
            mView.updateCleanArea(getAreaValue());
            if (haveMap && isViewAttached() && isDrawMap()) {
                mView.drawMapX8(pointList);
            }
        }, throwable -> {
            isGetHistory = false;
            MyLogger.e(TAG, "Failed to get history map data,and you need to retry sometime");
        });
        mComDisposable.add(d);
    }

    /**
     * x900获取历史地图
     * 考虑多线程同步问题
     */
    @Override
    public void getHistoryRoadX9() {
        pageNo = 1;
        Disposable d = Single.create(new SingleOnSubscribe<ACMsg>() {
            @Override
            public void subscribe(SingleEmitter<ACMsg> emitter) throws Exception {
                if (!isViewAttached()) {//page has been destroyed
                    emitter.onError(new Exception("you need to retry after a while as the view is not attach"));
                } else {
                    ACMsg req = new ACMsg();
                    req.setName("searchCleanRoadDataMore");
                    req.put("device_id", deviceId);
                    req.put("pageNo", pageNo);
                    AC.sendToServiceWithoutSign(DeviceUtils.getServiceName(subdomain), Constants.SERVICE_VERSION, req, new PayloadCallback<ACMsg>() {
                        @Override
                        public void success(ACMsg acMsg) {
                            emitter.onSuccess(acMsg);
                        }

                        @Override
                        public void error(ACException e) {
                            emitter.onError(e);
                        }
                    });
                }
            }
        }).map(acMsg -> {
            ArrayList<ACObject> objects = acMsg.get("data");
            parseHistoryX9(objects);
            if (objects == null || objects.size() != 1000) {
                pageNo = -1;
            } else {
                pageNo++;
            }
            return true;
        }).retry(2).repeatUntil(() -> {
            MyLogger.e(TAG, "Ready to get the data of page " + pageNo + ", it won't work if the pageNo is -1");
            return pageNo == -1;
        }).observeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread()).subscribe(aBoolean -> {
            isGetHistory = true;
            if (isViewAttached()) {
                mView.updateCleanArea(getAreaValue());
                mView.updateCleanTime(getTimeValue());
                //绘制历史路径坐标点，下一条路径的起始坐标为上 一条路径的终点坐标
                if (isDrawMap()) {
                    mView.drawMapX9(realTimePoints, historyRoadList, slamBytes);
                }
            }
        }, throwable -> {
            isGetHistory = false;
            MyLogger.e(TAG, "Failed to get history map data,and you need to retry sometime");
        });
        mComDisposable.add(d);
    }


    private void parseHistoryX9(ArrayList<ACObject> objects) {
        if (objects != null && objects.size() > 0) {
            for (int i = 0; i < objects.size(); i++) {
                ACObject acObject = objects.get(i);
                String cleanData = acObject.getString("clean_data");
                workTime = Integer.parseInt(acObject.getString("real_clean_time"));
                cleanArea = Integer.parseInt(acObject.getString("real_clean_area"));
                byte[] history_bytes = Base64.decode(cleanData, Base64.DEFAULT);
                if (history_bytes.length > 0) {
                    if (history_bytes.length == 6) {
                        //清除路径
                    } else {
                        if ((history_bytes.length - 2) % 4 == 0) {
                            if (historyRoadList != null && historyRoadList.size() > 0) {
                                historyRoadList.add(400);
                                historyRoadList.add(400);
                            }
                            for (int j = 2; j < history_bytes.length; j += 4) {
                                int pointX = DataUtils.bytesToInt(new byte[]{history_bytes[j], history_bytes[j + 1]}, 0);
                                int pointY = DataUtils.bytesToInt(new byte[]{history_bytes[j + 2], history_bytes[j + 3]}, 0);
                                historyRoadList.add((pointX * 224) / 100 + 750);
                                historyRoadList.add((pointY * 224) / 100 + 750);
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * //TODO 电子墙实时更新
     * 查询电子墙
     */
    public void queryVirtualWall() {
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, ACSkills.get().queryVirtual(), Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void success(ACDeviceMsg acDeviceMsg) {
                existPointList.clear();
                byte[] resp = acDeviceMsg.getContent();
                if (resp != null && resp.length > 0) {
                    byte count = resp[1];//电子墙总数
                    MyLogger.d(TAG, "virtual wall count :" + count);
                    for (int i = 9; i < 8 * count + 2; i += 8) {
                        //地图坐标
                        int startX = DataUtils.bytesToInt(new byte[]{resp[i - 7], resp[i - 6]}, 0);//2,3
                        int startY = DataUtils.bytesToInt(new byte[]{resp[i - 5], resp[i - 4]}, 0);//4,5
                        int endX = DataUtils.bytesToInt(new byte[]{resp[i - 3], resp[i - 2]}, 0);//6,7
                        int endY = DataUtils.bytesToInt(new byte[]{resp[i - 1], resp[i]}, 0);//8,9
                        //显示坐标(加移量后)
                        int sx = startX + 750;
                        int sy = 1500 - (startY + 750);
                        int ex = endX + 750;
                        int ey = 1500 - (endY + 750);
                        if (sx == ex && sy == ey) {
                            MyLogger.d("queryVirtualWall", "无效数据");
                            continue;
                        }
                        int[] dataPoint = {sx, sy, ex, ey};
                        existPointList.add(dataPoint);
                    }
                    wallPointList.clear();
                    wallPointList.addAll(existPointList);
                    if (haveMap && isViewAttached() && isDrawMap()) {
                        mView.drawVirtualWall(existPointList);
                    }
                }
            }

            @Override
            public void error(ACException e) {
                LogUtil.d(TAG, e.getErrorCode() + e.toString());
            }
        });
    }


    /**
     * 订阅实时地图
     */
    public void subscribeRealTimeMap() {
        Map<String, Object> primaryKey = new HashMap<>();
        primaryKey.put("device_id", deviceId);
        Disposable d = Single.create((SingleOnSubscribe<Boolean>) emitter -> AC.classDataMgr().
                subscribe("clean_realtime", primaryKey, ACClassDataMgr.OPTYPE_ALL, new VoidCallback() {
                    @Override
                    public void success() {
                        emitter.onSuccess(true);
                    }

                    @Override
                    public void error(ACException e) {
                        MyLogger.e(TAG, "Register real time map failed,and the error information is " + e.getMessage() + "and it will trigger to retry");
                        emitter.onError(e);
                    }
                })).retry(3).subscribe(aBoolean -> {
            isSubscribeRealMap = true;
            AC.classDataMgr().registerDataReceiver((s, i, s1) -> {
                MyLogger.d(TAG, "received map data------" + s1 + "----" + "---" + i + "-----------" + s);
                if (!isViewAttached()) {//
                    return;
                }
                if (isX900Series()) {
                    parseRealTimeMapX9(s1);
                    if (realTimePoints != null && realTimePoints.size() > 0 && isDrawMap()) {
                        mView.drawMapX9(realTimePoints, historyRoadList, slamBytes);
                    }
                } else {//x800 x785 x787  a9s a8s  v85 series
                    Gson gson = new Gson();
                    RealTimeMapInfo mapInfo = gson.fromJson(s1, RealTimeMapInfo.class);
                    String clean_data = mapInfo.getClean_data();
                    int offset = pointList.size();
                    parseRealTimeMapX8(clean_data);
                    updateSlamX8(pointList, offset);
                    mView.updateCleanTime(getTimeValue());
                    mView.updateCleanArea(getAreaValue());
                    if (haveMap && pointList != null && isDrawMap()) {
                        mView.drawMapX8(pointList);
                    }
                }
            });
        }, e -> {
            isSubscribeRealMap = false;
            MyLogger.d(TAG, "Register real time map Exception:" + e.getMessage());
        });
        mComDisposable.add(d);
    }

    /**
     * x800绘制黄方格地图
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
            workTime = DataUtils.bytesToInt2(byte_time, 0);
            cleanArea = DataUtils.bytesToInt2(byte_area, 0);
            for (int j = 7; j < bytes.length; j += 4) {
                int x = DataUtils.bytesToInt(new byte[]{bytes[j - 3], bytes[j - 2]}, 0);
                int y = DataUtils.bytesToInt(new byte[]{bytes[j - 1], bytes[j]}, 0);
                if ((x == 0x7fff) & (y == 0x7fff)) {
                    MyLogger.e(TAG, "the map data has been cleaned and reset");
                    pointList.clear();
                    workTime = 0;
                    cleanArea = 0;
                    minX = 0;
                    minY = 0;
                    maxX = 0;
                    maxY = 0;
                } else {
                    pointList.add(x);
                    pointList.add(1500 - y);
                }
            }
        }
    }

    /**
     * x900绘制slam地图
     *
     * @param mapSrc
     */
    private void parseRealTimeMapX9(String mapSrc) {
        synchronized (this) {
            Gson gson = new Gson();
            RealTimeMapInfo mapInfo = gson.fromJson(mapSrc, RealTimeMapInfo.class);
            if (mapInfo.getDevice_id() != deviceId) {//服务器错乱，下发了不属于该设备的数据
                return;
            }
            workTime = mapInfo.getReal_clean_time();
            cleanArea = mapInfo.getReal_clean_area();
            mView.updateCleanTime(getTimeValue());
            mView.updateCleanArea(getAreaValue());
            if (mapInfo.getPackage_num() == 1) {//包数量 1包
                bytes_subscribe = Base64.decode(mapInfo.getClean_data(), Base64.DEFAULT);
            } else { //包数量 多包
                if (mapInfo.getPackage_id() != package_index) {//包id
                    if (mapInfo.getPackage_id() == 1) {
                        lastStartTime = mapInfo.getStart_time();
                        bytes_subscribe = Base64.decode(mapInfo.getClean_data(), Base64.DEFAULT);
                        package_index++;
                    } else {
                        package_index = 1;
                    }
                    return;
                } else {
                    if (package_index == 1) {
                        lastStartTime = mapInfo.getStart_time();
                        bytes_subscribe = Base64.decode(mapInfo.getClean_data(), Base64.DEFAULT);
                    } else {
                        if (lastStartTime == mapInfo.getStart_time()) {//同一包数据，合并
                            bytes_subscribe = Utils.concat_(bytes_subscribe, Base64.decode(mapInfo.getClean_data(), Base64.DEFAULT), bytes_subscribe[0]);
                        } else {
                            package_index = 1;
                            return;
                        }
                    }
                    if (package_index < mapInfo.getPackage_num()) {
                        package_index++;
                        return;
                    } else {
                        package_index = 1;
                    }
                }
            }
            if (bytes_subscribe[0] == 2) {
                if (realTimePoints != null && realTimePoints.size() > 0) {
                    realTimePoints.add(400);
                    realTimePoints.add(400);
                }
                for (int j = 2; j <= bytes_subscribe.length - 4; j += 4) {
                    int x = DataUtils.bytesToInt(new byte[]{bytes_subscribe[j], bytes_subscribe[j + 1]}, 0);
                    int y = DataUtils.bytesToInt(new byte[]{bytes_subscribe[j + 2], bytes_subscribe[j + 3]}, 0);
                    if ((x == 0x7fff) & (y == 0x7fff)) {//出现错误的坐标信息，放弃所有数据
                        mView.cleanMapView();
                        realTimePoints.clear();
                        historyRoadList.clear();
                        wallPointList.clear();
                        return;
                    } else {
                        if (realTimePoints != null) {
                            realTimePoints.add(x * 224 / 100 + 750);
                            realTimePoints.add(y * 224 / 100 + 750);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void prepareToReloadData() {
        isGetHistory = false;
        historyRoadList.clear();
        realTimePoints.clear();//X900 series
        pointList.clear();//X800 series
    }


    /**
     * 休眠或者充电随便发一条命令，以激活设备
     */
    @Override
    public void getDeviceProperty() {
        Completable.timer(2, TimeUnit.SECONDS).observeOn(Schedulers.single()).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                if (statusFlag != STATUS_FLAG_COMPLETION) {
                    AC.deviceDataMgr().fetchCurrentProperty(subdomain, deviceId, new PayloadCallback<String>() {
                        @Override
                        public void success(String s) {
                            handlePropertyData(s, true);
                        }

                        @Override
                        public void error(ACException e) {
                            MyLogger.d(TAG, "getDeviceProperty:       " + e.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable e) {

            }
        });

    }


    /**
     * 获取设备状态
     */
    @Override
    public void getDevStatus() {
        retryTimes = 1;//reset the number of retries for getting devices status
        Single.create((SingleOnSubscribe<ACDeviceMsg>) emitter -> {
            MyLogger.d(TAG, "gain the device status");
            ACDeviceMsg msg_devStatus = new ACDeviceMsg(MsgCodeUtils.DevStatus, new byte[]{0x00});
            AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, msg_devStatus,
                    Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
                        @Override
                        public void success(ACDeviceMsg deviceMsg) {
                            emitter.onSuccess(deviceMsg);
                        }

                        @Override
                        public void error(ACException e) {
                            emitter.onError(e);
                        }
                    });
        }).retryWhen(tf -> tf.flatMap((Function<Throwable, Publisher<?>>) throwable -> (Publisher<Boolean>) s -> {
            MyLogger.d(TAG, "GAIN DEVICE STATUS ERROR-----:" + throwable.getMessage());
            if (retryTimes > 5) {
                s.onError(throwable);
            } else {
                Disposable disposable = Observable.timer(1, TimeUnit.SECONDS).subscribe(aLong -> {
                    s.onNext(true);
                    retryTimes++;
                });
                mComDisposable.add(disposable);
            }
        })).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<ACDeviceMsg>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(ACDeviceMsg deviceMsg) {
                if (!isViewAttached()) {
                    return;
                }
                isGainDevStatus = true;
                if (haveMap) {
                    sendToDeviceWithOption(ACSkills.get().upLoadRealMsg(0x01));
                }
                /**
                 * To avoid the failure of the first registration property listener, re-register it after the status is checked
                 */
                if (propertyReceiver == null) {
                    registerPropReceiver();
                }
                byte[] bytes = deviceMsg.getContent();
                if (bytes != null) {
                    errorCode = bytes[8] &0xff;
                    batteryNo = bytes[5];
                    mopForce = bytes[4];
                    isMaxMode = bytes[3] == 0x01;
                    voiceOpen = bytes[6] == 0x01;
                    curStatus = bytes[0];
                    virtualStatus = bytes[7];
                    if (robotType.equals(Constants.X800) && device_type == 0) {
                        device_type = bytes[9]&0xff;
                    }
                    MyLogger.d(TAG, "gain the device status success and the status is :" + curStatus + "--------");
                    setStatus(curStatus, batteryNo, mopForce, isMaxMode, voiceOpen);
                    mView.updateCleanArea(getAreaValue());
                    mView.updateCleanTime(getTimeValue());
                    mView.showErrorPopup(errorCode);
                    if (errorCode != 0) {
                        mView.cleanMapView();
                    }

                    /**
                     * 请求设备相关数据
                     */
                    if (isX900Series()) {
                        if (!isSubscribeRealMap) {
                            subscribeRealTimeMap();
                        }
                        if (!isInitSlamTimer) {
                            intervalToObtainSlam();
                        }
                        if (!isGetHistory) {
                            getHistoryRoadX9();
                        }
                        queryVirtualWall();
                    } else {//x800系列
                        if (!isGetHistory && havMapData) {
                            getHistoryDataX8();
                        }
                        if (!isSubscribeRealMap && havMapData) {
                            subscribeRealTimeMap();
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                if (!isViewAttached()) {
                    return;
                }
                if (haveMap) {
                    sendToDeviceWithOption(ACSkills.get().upLoadRealMsg(0x01));
                }
                /**
                 * 避免第一次注册属性监听失败，查询到状态后重新注册
                 */
                if (propertyReceiver == null) {
                    registerPropReceiver();
                }
                isGainDevStatus = false;
                MyLogger.d(TAG, "To gain the device status fail ,and the reason is: " + e.getMessage());
            }
        });
    }


    @Override
    public boolean isMaxMode() {
        isMaxMode = SpUtils.getBoolean(MyApplication.getInstance(), physicalId + KEY_IS_MAX);
        return isMaxMode;
    }

    @Override
    public void reverseMaxMode() {
        if (isMaxMode) {
            sendToDeviceWithOption(ACSkills.get().cleaningNormal(mopForce));
        } else {
            sendToDeviceWithOption(ACSkills.get().cleaningMax(mopForce));
        }
    }

    private void refreshMap() {
        if (slamBytes != null && realTimePoints != null && historyRoadList != null && existPointList != null) {
            if (isX900Series()) {
                mView.drawMapX9(realTimePoints, historyRoadList, slamBytes);
            } else {
                mView.drawMapX8(pointList);
            }
        }
    }

    @Override
    public void refreshStatus() {
        setStatus(curStatus, batteryNo, mopForce, isMaxMode, voiceOpen);
    }

    public void setStatus(int curStatus, int batteryNo, int mopForce, boolean isMaxMode, boolean voiceOpen) {
        if (curStatus == MsgCodeUtils.STATUE_PLANNING || curStatus == MsgCodeUtils.STATUE_RANDOM) {//保存清掃模式
            SpUtils.saveInt(MyApplication.getInstance(), physicalId + SettingActivity.KEY_MODE, curStatus);
        }
        MyLogger.d(TAG, "setStatus----------curStatus" + curStatus);
        if (!isViewAttached()) {
            return;
        }
        if (batteryNo != -1) {//set battery icon
            mView.setBatteryImage(curStatus, batteryNo);
            SpUtils.saveBoolean(MyApplication.getInstance(), physicalId + KEY_IS_MAX, isMaxMode);
            SpUtils.saveInt(MyApplication.getInstance(), physicalId + KEY_MOP_FORCE, mopForce);
            SpUtils.saveBoolean(MyApplication.getInstance(), physicalId + KEY_VOICE_OPEN, voiceOpen);
        }
        mView.clearAll(curStatus);//清空所有不常显示布局，以便根据status更新显示布局
        boolean isWork = isWork(curStatus);
        mView.updateStatue(DeviceUtils.getStatusStr(MyApplication.getInstance(), curStatus, errorCode));//待机，规划
        mView.updateStartStatue(isWork, isWork ? Utils.getString(R.string.map_aty_stop) : Utils.getString(R.string.map_aty_start));
        mView.updateOperationViewStatue(curStatus);
        if (curStatus == MsgCodeUtils.STATUE_RANDOM || curStatus == MsgCodeUtils.STATUE_PLANNING || curStatus == MsgCodeUtils.STATUE_CHARGING_ || curStatus == MsgCodeUtils.STATUE_CHARGING || (curStatus == MsgCodeUtils.STATUE_RECHARGE && !isX900Series())) {
            mView.setCurrentBottom(BaseMapActivity.USE_MODE_NORMAL);
        }
        if (curStatus == MsgCodeUtils.STATUE_REMOTE_CONTROL || curStatus == MsgCodeUtils.STATUE_POINT
                || curStatus == MsgCodeUtils.STATUE_ALONG) {
            mView.setCurrentBottom(BaseMapActivity.USE_MODE_REMOTE_CONTROL);
        }

        mView.showBottomView();
        if (haveMap && isViewAttached() && isDrawMap()) {
//            mView.setMapViewVisible(true);
            refreshMap();
        } else {
            mView.cleanMapView();
//            mView.setMapViewVisible(false);
        }
        switch (curStatus) {
            case MsgCodeUtils.STATUE_RECHARGE://回充
                mView.updateRecharge(true);
                break;
            case MsgCodeUtils.STATUE_VIRTUAL_EDIT://电子墙编辑模式
                mView.showVirtualEdit();
                break;
            case MsgCodeUtils.STATUE_POINT://重点
                mView.updatePoint(true);
                mView.setTvUseStatus(BaseMapActivity.TAG_KEYPOINT);
                break;
            case MsgCodeUtils.STATUE_ALONG://沿墙模式
                mView.updateAlong(true);
                mView.setTvUseStatus(BaseMapActivity.TAG_ALONG);
                break;
            case MsgCodeUtils.STATUE_RANDOM:
                mView.setTvUseStatus(BaseMapActivity.TAG_RANDOM);
                break;
            case MsgCodeUtils.STATUE_WAIT:
                mView.setUnconditionalRecreate(true);
                break;
        }

    }


    @Override
    public boolean isVirtualWallOpen() {
        return virtualStatus == MsgCodeUtils.VIRTUAL_WALL_OPEN;
    }

    @Override
    public boolean isLowPowerWorker() {
        return batteryNo != -1 && batteryNo <= 6 && (curStatus == MsgCodeUtils.STATUE_SLEEPING || curStatus == MsgCodeUtils.STATUE_WAIT);
    }

    /**
     * 判断设备状态是否需要绘制地图
     *
     * @return
     */
    @Override
    public boolean isDrawMap() {
        return (curStatus == MsgCodeUtils.STATUE_TEMPORARY_POINT || curStatus == MsgCodeUtils.STATUE_PLANNING
                || curStatus == MsgCodeUtils.STATUE_PAUSE || curStatus == MsgCodeUtils.STATUE_VIRTUAL_EDIT
                || (curStatus == MsgCodeUtils.STATUE_RECHARGE && isX900Series())) && mView.isActivityInteraction();
    }


    @Override
    public boolean isWork(int curStatus) {
        return curStatus == MsgCodeUtils.STATUE_RANDOM || curStatus == MsgCodeUtils.STATUE_ALONG ||
                curStatus == MsgCodeUtils.STATUE_POINT || curStatus == MsgCodeUtils.STATUE_TEMPORARY_POINT || curStatus == MsgCodeUtils.STATUE_PLANNING ||
                curStatus == MsgCodeUtils.STATUE_RECHARGE;
    }


    /**
     * @param s1          property json
     * @param isFromFetch Where is property json from,fetch or automatically push.
     */
    @Override
    public void handlePropertyData(String s1, boolean isFromFetch) {
        MyLogger.d(TAG, "handlePropertyData ==== " + s1 + "---------------isFromFetch--------" + isFromFetch);
        if (!isFromFetch) {
            statusFlag = STATUS_FLAG_COMPLETION;
        } else if (statusFlag == STATUS_FLAG_COMPLETION) {
            return;
        }

        if (isViewAttached()) {
            PropertyInfo info = gson.fromJson(s1, PropertyInfo.class);
            errorCode = info.getError_info();
            batteryNo = info.getBattery_level();
            isMaxMode = info.getVacuum_cleaning() == MsgCodeUtils.CLEANNING_CLEANING_MAX;
            mopForce = info.getCleaning_cleaning();
            voiceOpen = info.getVoice_mode() == 0x01;
            if (device_type == 0) {
                device_type = info.getDevice_type();
            }
            virtualStatus = info.getLight_mode();
            int lastStatus = curStatus;
            curStatus = info.getWork_pattern();
            if (isX900Series() && lastStatus != curStatus && isNeedQueryVirtual(curStatus, lastStatus)) {
                queryVirtualWall();
            }
            if (lastStatus != MsgCodeUtils.STATUE_VIRTUAL_EDIT || curStatus != lastStatus) {
                setStatus(curStatus, batteryNo, mopForce, isMaxMode, voiceOpen);
            }
            mView.updateCleanArea(getAreaValue());
            mView.updateCleanTime(getTimeValue());
            mView.showErrorPopup(errorCode);
//            if (errorCode != 0) {
//                mView.cleanMapView();
//            }
        }
    }

    /**
     * 注册状态变化监听
     */
    @Override
    public void initPropReceiver() {
        propertyReceiver = (s, l, s1) -> handlePropertyData(s1, false);
    }

    private boolean isNeedQueryVirtual(int curStatus, int lastStatus) {
        //退出电子墙编辑模式时查询电子墙
        return curStatus == MsgCodeUtils.STATUE_RECHARGE || curStatus == MsgCodeUtils.STATUE_REMOTE_CONTROL || curStatus == MsgCodeUtils.STATUE_ALONG || curStatus == MsgCodeUtils.STATUE_POINT || curStatus == MsgCodeUtils.STATUE_PLANNING || lastStatus == MsgCodeUtils.STATUE_VIRTUAL_EDIT;
    }

    private String getAreaValue() {
        BigDecimal bg = new BigDecimal(cleanArea / 100.0f);
        double area = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        if (curStatus == MsgCodeUtils.STATUE_RECHARGE || !havMapData || (!isDrawMap() && curStatus != MsgCodeUtils.STATUE_RANDOM && curStatus != MsgCodeUtils.STATUE_TEMPORARY_POINT)) {
            return Utils.getString(R.string.map_aty_gang);
        } else {
            return area + "㎡";
        }
    }

    private String getTimeValue() {
        int min = (int) (workTime / 60f);
        if (curStatus == MsgCodeUtils.STATUE_RECHARGE || !havMapData || (!isDrawMap() && curStatus != MsgCodeUtils.STATUE_RANDOM && curStatus != MsgCodeUtils.STATUE_TEMPORARY_POINT)) {
            return Utils.getString(R.string.map_aty_gang);
        } else {
            return min + "min";
        }
    }

    @Override
    public void registerPropReceiver() {
        Single.create((SingleOnSubscribe<Boolean>) emitter -> AC.deviceDataMgr().subscribeProperty(subdomain, deviceId,
                new VoidCallback() {
                    @Override
                    public void success() {
                        if (propertyReceiver == null) {
                            initPropReceiver();
                        }
                        AC.deviceDataMgr().registerPropertyReceiver(propertyReceiver);
                        emitter.onSuccess(true);
                    }

                    @Override
                    public void error(ACException e) {
                        MyLogger.d(TAG, "注册状态监听失败--------------------重试中");
                        emitter.onError(e);
                    }
                }
        )).retryWhen(throwable -> throwable.flatMap((Function<Throwable, Publisher<?>>) throwable1 -> (Publisher<Boolean>) subscriber -> {
            Disposable disposable = Observable.timer(1, TimeUnit.SECONDS).subscribe(aLong -> {
                if (propertyReceiver == null) {
                    subscriber.onNext(true);
                } else {
                    subscriber.onError(new Exception("this task has been done by other object"));
                }
            });
            mComDisposable.add(disposable);
        })).subscribe(new SingleObserver<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(Boolean aBoolean) {
                MyLogger.d(TAG, "注册状态监听成功---------------------");
            }

            @Override
            public void onError(Throwable e) {
                MyLogger.d(TAG, "注册状态监听失败-------------------" + e.getMessage());
            }
        });
    }

    @Override
    public void sendToDeviceWithOption(ACDeviceMsg msg) {
        if (propertyReceiver == null || statusFlag != STATUS_FLAG_COMPLETION) {
            MyLogger.d(TAG, "注册属性监听失效或者未注册,需要重新注册！");
            registerPropReceiver();
            statusFlag = STATUS_FLAG_COMPLETION;
        }
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, msg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void error(ACException e) {
                if (msg.getCode() == MsgCodeUtils.UPLOADMSG) {//上传地图数据的请求不提示超时
                    return;
                }
                MyLogger.d(TAG, msg.getContent()[0] + " command failed reason" + e.getMessage());
                ToastUtils.showErrorToast(MyApplication.getInstance(), e.getErrorCode());
            }

            @Override
            public void success(ACDeviceMsg deviceMsg) {
                switch (deviceMsg.getCode()) {
                    case MsgCodeUtils.UPLOADMSG:
                        MyLogger.d(TAG, "上传实时信息" + deviceMsg.getContent()[0]);
                        break;
                    case MsgCodeUtils.CleanForce://更新max mode
                        byte[] resp = deviceMsg.getContent();
                        isMaxMode = resp[0] == 0x01;
                        mView.updateMaxButton(isMaxMode);
                        SpUtils.saveBoolean(MyApplication.getInstance(), physicalId + KEY_IS_MAX, isMaxMode);
                        break;
                    case MsgCodeUtils.Proceed://遥控器模式
                        statusFlag = 1;
                        getDeviceProperty();
                        int subCode = deviceMsg.getContent()[0];
                        int tag_code = -1;
                        if (subCode == 0x01) {
                            tag_code = BaseMapActivity.TAG_FORWARD;
                        } else if (subCode == 0x03) {
                            tag_code = BaseMapActivity.TAG_LEFT;
                        } else if (subCode == 0x04) {
                            tag_code = BaseMapActivity.TAG_RIGHT;
                        }
                        if (tag_code != -1) {
                            mView.setTvUseStatus(tag_code);
                        }
                        break;
                    case MsgCodeUtils.WorkMode://下发工作模式
                        statusFlag = 1;
                        getDeviceProperty();
                        if (!isGainDevStatus) {
                            getDevStatus();
                            MyLogger.d(TAG, "gain the device status again");
                        }
                        byte[] bytes = deviceMsg.getContent();
//                        curStatus = bytes[0];
//                        int lastStatus = curStatus;
//
//                        if (isX900Series()) {
//                            if ((lastStatus == MsgCodeUtils.STATUE_PLANNING || lastStatus == MsgCodeUtils.STATUE_VIRTUAL_EDIT || lastStatus == MsgCodeUtils.STATUE_PAUSE) && sendByte == MsgCodeUtils.STATUE_WAIT) {
//                                curStatus = MsgCodeUtils.STATUE_PAUSE;
//                                sendByte = MsgCodeUtils.STATUE_PAUSE;
//                            } else {
//                                curStatus = bytes[0];
//                            }
//                        } else {
//                            curStatus = bytes[0];
//                        }
//                        if (lastStatus != MsgCodeUtils.STATUE_RECHARGE && curStatus != MsgCodeUtils.STATUE_PLANNING && curStatus == sendByte) {//900的暂停模式发的是待机命令，不准确
//                            setStatus(curStatus, -1, mopForce, isMaxMode, voiceOpen);
//                        }
                        if (bytes[0] == MsgCodeUtils.STATUE_CHARGING_) {//寻找模式
                            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_charge));
                        }
                        break;
                }
            }
        });
    }


    @Override
    public void enterVirtualMode() {
        ACDeviceMsg acDeviceMsg = ACSkills.get().enterVirtualMode();
        sendToDeviceWithOption(acDeviceMsg);
    }


    /**
     * @param list SEND_VIR添加电子墙时为新增电子墙集合，EXIT_VIR 时，为null
     */
    public void sendVirtualWallData(final List<int[]> list) {
        wallPointList.clear();
        wallPointList.addAll(list);
        new Thread(() -> {
            virtualContentBytes = new byte[82];
//                if (sendLists != null && sendLists.size() > 0) {
            if (wallPointList != null && wallPointList.size() > 0) {
//                    int size = sendLists.size();
                int size = wallPointList.size();
                byte open = (byte) 0x01;
                byte counts = (byte) size;
                virtualContentBytes[0] = open;
                virtualContentBytes[1] = counts;
                for (int t = 1; t < size + 1; t++) {
//                        int[] floats = sendLists.get(t - 1);
                    int[] floats = wallPointList.get(t - 1);
                    int x1 = (int) floats[0] - 750;
                    int y1 = (int) 1500 - floats[1] - 750;
                    int x2 = (int) floats[2] - 750;
                    int y2 = (int) 1500 - floats[3] - 750;
                    byte[] startxBytes = DataUtils.intToBytes(x1);
                    byte[] startyBytes = DataUtils.intToBytes(y1);
                    byte[] endxBytes = DataUtils.intToBytes(x2);
                    byte[] endyBytes = DataUtils.intToBytes(y2);
                    virtualContentBytes[(t - 1) * 8 + 2] = startxBytes[0];
                    virtualContentBytes[(t - 1) * 8 + 3] = startxBytes[1];
                    virtualContentBytes[(t - 1) * 8 + 4] = startyBytes[0];
                    virtualContentBytes[(t - 1) * 8 + 5] = startyBytes[1];
                    virtualContentBytes[(t - 1) * 8 + 6] = endxBytes[0];
                    virtualContentBytes[(t - 1) * 8 + 7] = endxBytes[1];
                    virtualContentBytes[(t - 1) * 8 + 8] = endyBytes[0];
                    virtualContentBytes[(t - 1) * 8 + 9] = endyBytes[1];
                }
            } else {
                MyLogger.e(TAG, "sendLists is null");
            }
            sendToDeviceWithOptionVirtualWall(ACSkills.get().setVirtualWall(virtualContentBytes), physicalId);
        }).start();
    }


    /**
     * 申请添加/删除电子墙
     *
     * @param acDeviceMsg
     * @param physicalDeviceId
     */
    public void sendToDeviceWithOptionVirtualWall(ACDeviceMsg acDeviceMsg, String physicalDeviceId) {
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalDeviceId, acDeviceMsg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void success(ACDeviceMsg acDeviceMsg) {
                existPointList.clear();
                existPointList.addAll(wallPointList);
                ToastUtils.showToast(Utils.getString(R.string.map_aty_set_suc));
                mView.drawVirtualWall(existPointList);
            }

            @Override
            public void error(ACException e) {
                ToastUtils.showToast(Utils.getString(R.string.map_aty_set_fail));
            }
        });
    }

    @Override
    public boolean pointToAlong() {
        return robotType.equals(Constants.XV3PRO) || robotType.equals(Constants.V5x) || robotType.equals(Constants.V85) || robotType.equals(Constants.X785) || robotType.equals(Constants.X787) || robotType.equals(Constants.A7);
    }

    @Override
    public void enterAlongMode() {
        if ((curStatus == MsgCodeUtils.STATUE_POINT && pointToAlong()) || curStatus == MsgCodeUtils.STATUE_WAIT || curStatus == MsgCodeUtils.STATUE_ALONG || curStatus == MsgCodeUtils.STATUE_REMOTE_CONTROL ||
                curStatus == MsgCodeUtils.STATUE_PAUSE) {
            if (curStatus == MsgCodeUtils.STATUE_ALONG) {
                sendToDeviceWithOption(ACSkills.get().enterWaitMode());
            } else {
                sendToDeviceWithOption(ACSkills.get().enterAlongMode());
            }
        } else if (curStatus == MsgCodeUtils.STATUE_CHARGING || curStatus == MsgCodeUtils.STATUE_CHARGING_) {
            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_charge));
        } else {
            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_can_not_execute));
        }
    }

    @Override
    public void enterPointMode() {
        if ((curStatus == MsgCodeUtils.STATUE_ALONG && pointToAlong()) || curStatus == MsgCodeUtils.STATUE_WAIT || curStatus == MsgCodeUtils.STATUE_POINT || curStatus == MsgCodeUtils.STATUE_REMOTE_CONTROL ||
                curStatus == MsgCodeUtils.STATUE_PAUSE) {
            if (curStatus == MsgCodeUtils.STATUE_POINT) {
                sendToDeviceWithOption(ACSkills.get().enterWaitMode());
            } else {
                sendToDeviceWithOption(ACSkills.get().enterPointMode());
            }
        } else if (curStatus == MsgCodeUtils.STATUE_CHARGING || curStatus == MsgCodeUtils.STATUE_CHARGING_) {
            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_charge));
        } else {
            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_can_not_execute));
        }
    }

    @Override
    public int getDevice_type() {
        return device_type;
    }

    @Override
    public void enterRechargeMode() {
        if (curStatus == MsgCodeUtils.STATUE_CHARGING || curStatus == MsgCodeUtils.STATUE_CHARGING_) {
            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_charge));
        } else if (curStatus == MsgCodeUtils.STATUE_POINT || curStatus == MsgCodeUtils.STATUE_ALONG) {
            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_can_not_execute));
        } else {
            if (curStatus == MsgCodeUtils.STATUE_RECHARGE) {
                sendToDeviceWithOption(ACSkills.get().enterWaitMode());
            } else {
                sendToDeviceWithOption(ACSkills.get().enterRechargeMode());
            }
        }
    }

    @Override
    public int getCurStatus() {
        return curStatus;
    }

    @Override
    public boolean isRandomMode() {
        return SpUtils.getInt(MyApplication.getInstance(), physicalId + SettingActivity.KEY_MODE) == MsgCodeUtils.STATUE_RANDOM;
    }


    @Override
    public void detachView() {
        if (mComDisposable != null) {
            mComDisposable.dispose();
        }
        try {
            if (isSubscribeRealMap) {
                Map<String, Object> primaryKey = new HashMap<>();
                primaryKey.put("device_id", deviceId);
                AC.classDataMgr().unSubscribe("clean_realtime", primaryKey, ACClassDataMgr.OPTYPE_ALL, new VoidCallback() {
                    @Override
                    public void success() {
                        MyLogger.d(TAG, "unsubscribe real time map success");
                    }

                    @Override
                    public void error(ACException e) {
                        MyLogger.d(TAG, "unsubscribe real time map fail");
                    }
                });
            }
            if (propertyReceiver != null) {
                AC.deviceDataMgr().unregisterPropertyReceiver(propertyReceiver);
                AC.deviceDataMgr().unSubscribeAllProperty();
            }
        } catch (Exception e) {
            MyLogger.e(TAG, "unsubscribe real time map error");
        }
        super.detachView();
    }

}
