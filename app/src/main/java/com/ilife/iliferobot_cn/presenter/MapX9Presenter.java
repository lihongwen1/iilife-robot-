package com.ilife.iliferobot_cn.presenter;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACClassDataMgr;
import com.accloud.service.ACDeviceDataMgr;
import com.accloud.service.ACDeviceMsg;
import com.accloud.service.ACException;
import com.accloud.service.ACMsg;
import com.accloud.service.ACObject;
import com.google.gson.Gson;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.activity.MainActivity;
import com.ilife.iliferobot_cn.activity.MapActivity_X9_;
import com.ilife.iliferobot_cn.app.MyApplication;
import com.ilife.iliferobot_cn.base.BasePresenter;
import com.ilife.iliferobot_cn.contract.MapX9Contract;
import com.ilife.iliferobot_cn.entity.PropertyInfo;
import com.ilife.iliferobot_cn.entity.RealTimeMapInfo;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.DataUtils;
import com.ilife.iliferobot_cn.utils.DeviceUtils;
import com.ilife.iliferobot_cn.utils.MsgCodeUtils;
import com.ilife.iliferobot_cn.utils.MyLog;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.TimeUtil;
import com.ilife.iliferobot_cn.utils.ToastUtils;
import com.ilife.iliferobot_cn.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapX9Presenter extends BasePresenter<MapX9Contract.View> implements MapX9Contract.Presenter {
    private final String TAG = "MapX9Presenter";
    private ACDeviceDataMgr.PropertyReceiver propertyReceiver;
    public static final String KEY_IS_MAX = "isMaxMode";
    public static final String KEY_MOP_FORCE = "mopForce";
    public static final String KEY_VOICE_OPEN = "voiceOpen";
    private long deviceId;
    private String physicalId, subdomain, robotType;
    private Gson gson;
    boolean isVirtualEdit, isDelete, isVirtualWall, isOnCreate, isAdd;
    private byte[] slamBytes;
    private int curStatus, errorCode, batteryNo, workTime, cleanArea;
    private Timer timer;
    private ArrayList<Integer> realTimePoints, historyRoadList;
    private ArrayList<int[]> wallPointList = new ArrayList<>();
    private ArrayList<int[]> existPointList = new ArrayList<>();
    private ACDeviceMsg mAcDevMsg;
    boolean isWork, hasAppoint, isMaxMode, hasStart, hasStart_, voiceOpen, isX800;//hasSart标记point动画启动状态
    /**
     * 实时地图相关
     */
    private byte[] bytes_subscribe;
    private int packageId = 0;
    private int package_index = 1;
    private long lastStartTime;

    /**
     * 查询设备状态相关
     */
    private int mopForce;

    private final int TAG_CONTROL = 0x01;
    private final int TAG_NORMAL = 0x02;
    private final int TAG_RECHAGRGE = 0x03;
    private final int TAG_KEYPOINT = 0x04;
    private final int TAG_ALONG = 0x05;
    private byte sendByte;
    private byte[] virtualContentBytes;
    private static final int SEND_VIR = 1;
    private static final int EXIT_VIR = 2;

    @Override
    public void attachView(MapX9Contract.View view) {
        super.attachView(view);
        gson = new Gson();
        mAcDevMsg = new ACDeviceMsg();
        realTimePoints = new ArrayList<>();
        historyRoadList = new ArrayList<>();
        deviceId = SpUtils.getLong(MyApplication.getInstance(), MainActivity.KEY_DEVICEID);
        subdomain = SpUtils.getSpString(MyApplication.getInstance(), MainActivity.KEY_SUBDOMAIN);
        physicalId = SpUtils.getSpString(MyApplication.getInstance(), MainActivity.KEY_PHYCIALID);
        if (subdomain.equals(Constants.subdomain_x900)) {
            robotType = "X900";
        } else if (subdomain.equals(Constants.subdomain_x787)) {
            robotType = "X787";
        } else {
            robotType = "X785";
        }
    }

    public void initTimer() {
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (curStatus == 0x06 || curStatus == 0x08 || curStatus == 0x07) {
                    getRealTimeMap();
                }
            }
        };
        timer.schedule(task, 0, 3 * 1000);
    }

    @Override
    public void getRealTimeMap() {
        final ACMsg req = new ACMsg();
        req.setName("searchCleanRealTime");
        req.put("device_id", deviceId);
        String serviceName = "ILife-X900-CN-Test";
        AC.sendToService("", serviceName, Constants.SERVICE_VERSION, req, new PayloadCallback<ACMsg>() {
            @Override
            public void success(ACMsg resp) {
                Log.d(TAG,"getRealTimeMap----");
                String strMap = resp.getString("slam_map");
                int xMax = resp.getInt("slam_x_max");
                int xMin = resp.getInt("slam_x_min");
                int yMax = resp.getInt("slam_y_max");
                int yMin = resp.getInt("slam_y_min");
                if (!TextUtils.isEmpty(strMap)) {
                    slamBytes = Base64.decode(strMap, Base64.DEFAULT);
                    if (isOnCreate) {
                        mView.updateSlam(xMin, xMax, yMin, yMax, slamBytes);
                        mView.drawObstacle();
                        isOnCreate = false;
                    }
                }
            }

            @Override
            public void error(ACException e) {
                MyLog.e(TAG, "getRealTimeMap e = " + e.toString());
            }
        });
    }

    @Override
    public void getHistoryRoad() {
        final ACMsg req = new ACMsg();
        req.setName("searchCleanRoadData");
        req.put("device_id", deviceId);
        String serviceNames = "ILife-X900-CN-Test";
        AC.sendToService("", serviceNames, Constants.SERVICE_VERSION, req, new PayloadCallback<ACMsg>() {
            @Override
            public void success(ACMsg acMsg) {
                Log.d(TAG,"getHistoryRoad()-----------");
                ArrayList<ACObject> objects = acMsg.get("data");
                if (objects != null && objects.size() > 0) {
                    for (int i = 0; i < objects.size(); i++) {
                        ACObject acObject = objects.get(i);
                        String cleanData = acObject.getString("clean_data");
                        byte[] history_bytes = Base64.decode(cleanData, Base64.DEFAULT);
                        if (history_bytes.length > 0) {
                            if (history_bytes.length == 6) {
                                //清除路径
                            } else {
                                if ((history_bytes.length - 2) % 4 == 0) {
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
                    //绘制历史路径坐标点，下一条路径的起始坐标为上 一条路径的终点坐标
                    mView.drawSlamMap(slamBytes);
                    mView.drawRoadMap(realTimePoints, historyRoadList);
                    mView.drawObstacle();
                }
            }

            @Override
            public void error(ACException e) {

            }
        });
    }

    /**
     * 查询虚拟墙
     */
    public void queryVirtualWall() {
        mAcDevMsg.setCode(MsgCodeUtils.QueryVirtualWall);
        mAcDevMsg.setContent(new byte[]{0x00});
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, mAcDevMsg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void success(ACDeviceMsg acDeviceMsg) {
                existPointList.clear();
                byte[] resp = acDeviceMsg.getContent();
                if (resp != null && resp.length > 0) {
                    byte count = resp[1];//虚拟墙总数
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
                        int[] dataPoint = {sx, sy, ex, ey};
                        existPointList.add(dataPoint);
                        //TODO saveQueryRect
//                        saveQueryRect(dataPoint);
                    }
                    wallPointList.clear();
                    wallPointList.addAll(existPointList);
                    //TODO 绘制虚拟墙
                    mView.drawVirtualWall(existPointList);
                }
            }

            @Override
            public void error(ACException e) {

            }
        });
    }


    public void subscribeRealTimeMap() {
        Map<String, Object> primaryKey = new HashMap<>();
        primaryKey.put("device_id", deviceId);
        AC.classDataMgr().subscribe("clean_realtime", primaryKey, ACClassDataMgr.OPTYPE_ALL,
                new VoidCallback() {
                    @Override
                    public void success() {
                        AC.classDataMgr().registerDataReceiver(new ACClassDataMgr.ClassDataReceiver() {
                            @Override
                            public void onReceive(String s, int i, String s1) {
                                Log.d(TAG,"subscribeRealTimeMap-------");
                                synchronized (this) {
                                    Gson gson = new Gson();
                                    RealTimeMapInfo mapInfo = gson.fromJson(s1, RealTimeMapInfo.class);
                                    if (mapInfo.getPackage_num() == 1) {
                                        bytes_subscribe = Base64.decode(mapInfo.getClean_data(), Base64.DEFAULT);
                                    } else {
                                        if (mapInfo.getPackage_id() != package_index) {
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
                                                if (lastStartTime == mapInfo.getStart_time()) {
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
                                        if (packageId != bytes_subscribe[1]) {
                                            packageId = bytes_subscribe[1];
                                        } else {
                                            if (realTimePoints.size() >= 2) {
                                                int end_y = realTimePoints.get(realTimePoints.size() - 2);
                                                int end_x = realTimePoints.get(realTimePoints.size() - 1);
                                                realTimePoints.add(end_x);
                                                realTimePoints.add(end_y);
                                            }
                                        }
                                        packageId++;
                                        if (packageId > 255) {
                                            packageId = 0;
                                        }
                                        for (int j = 2; j <= bytes_subscribe.length - 4; j += 4) {
                                            int x = DataUtils.bytesToInt(new byte[]{bytes_subscribe[j], bytes_subscribe[j + 1]}, 0);
                                            int y = DataUtils.bytesToInt(new byte[]{bytes_subscribe[j + 2], bytes_subscribe[j + 3]}, 0);
                                            if ((x == 0x7fff) & (y == 0x7fff)) {
                                                mView.updateCleanArea(Utils.getString(R.string.map_aty_gang));
                                                mView.updateCleanTime(Utils.getString(R.string.map_aty_gang));
                                                mView.cleanMapView();
                                                realTimePoints.clear();
                                                historyRoadList.clear();
                                                wallPointList.clear();
                                            } else {
                                                realTimePoints.add(x * 224 / 100 + 750);
                                                realTimePoints.add(y * 224 / 100 + 750);
                                            }
                                        }

                                        /**
                                         * 绘制地图
                                         */
                                        if (realTimePoints.size() > 0) {

                                            //slam地图
                                            mView.drawSlamMap(slamBytes);
                                            //历史路径
                                            mView.drawRoadMap(realTimePoints, historyRoadList);
                                            //重绘障碍物
                                            mView.drawObstacle();
                                        }
                                    }
                                    int workTime = mapInfo.getReal_clean_time();
                                    int cleanArea = mapInfo.getReal_clean_area();
                                    if (curStatus == 0x08) {
                                        mView.updateCleanArea(Utils.getString(R.string.map_aty_gang));
                                        mView.updateCleanTime(Utils.getString(R.string.map_aty_gang));
                                    } else {
                                        mView.updateCleanTime(workTime / 60 + "min");
                                        mView.updateCleanArea(cleanArea / 100.0 + "㎡");
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void error(ACException e) {
                    }
                }
        );
    }

    /**
     * 查询预约信息
     */
    @Override
    public void getAppointmentMsg() {
        ACDeviceMsg msg_clockInfo = new ACDeviceMsg(MsgCodeUtils.ClockInfos, new byte[]{0x00});
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, msg_clockInfo,
                Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
                    @Override
                    public void success(ACDeviceMsg deviceMsg) {
                        adjustTime();
                        if (isViewAttached()) {
                            byte[] resp = deviceMsg.getContent();
                            if (resp != null && resp.length == 50) {
                                hasAppoint = hasAppoint(resp);
                            }
                        }
                    }

                    @Override
                    public void error(ACException e) {
                        adjustTime();
                        hasAppoint = false;
                    }
                });
    }

    @Override
    public void getDevStatus() {
        ACDeviceMsg msg_devStatus = new ACDeviceMsg(MsgCodeUtils.DevStatus, new byte[]{0x00});
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, msg_devStatus,
                Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
                    @Override
                    public void success(ACDeviceMsg deviceMsg) {
                        byte[] bytes = deviceMsg.getContent();
                        if (bytes != null) {
                            if (bytes.length == 10) {
                                isX800 = true;
                            }
                            errorCode = bytes[8];
                            curStatus = bytes[0];
                            batteryNo = bytes[5];
                            mopForce = bytes[4];
                            isMaxMode = bytes[3] == 0x01;
                            voiceOpen = bytes[6] == 0x01;
                            setStatus(curStatus, batteryNo, mopForce, isMaxMode, voiceOpen);
                            if (curStatus != 0x06) {//0x06规划模式
                                mView.updateCleanTime(Utils.getString(R.string.map_aty_gang));
                                mView.updateCleanArea(Utils.getString(R.string.map_aty_gang));
                            } else {
                                mView.updateCleanArea(cleanArea / 100.0 + "㎡");
                                mView.updateCleanTime(workTime / 60 + "min");
                            }
                            mView.showErrorPopup(errorCode);
                            if (errorCode != 0) {
                                mView.setMapViewVisible(false);
                            }
                        }
                    }

                    @Override
                    public void error(ACException e) {

                    }
                });
    }

    public void setStatus(int curStatus, int batteryNo, int mopForce, boolean isMaxMode, boolean voiceOpen) {
        isWork = isWork(curStatus);
        mView.updateStatue(DeviceUtils.getStatusStr(MyApplication.getInstance(), curStatus, errorCode));
        mView.updateStartStatue(isWork, isWork ? Utils.getString(R.string.map_aty_stop) : Utils.getString(R.string.map_aty_start));
        mView.updateTvVirtualStatue(canEdit(curStatus));
        if (batteryNo != -1) {
            mView.setBatteryImage(curStatus, batteryNo);
            SpUtils.saveBoolean(MyApplication.getInstance(), physicalId + KEY_IS_MAX, isMaxMode);
            SpUtils.saveInt(MyApplication.getInstance(), physicalId + KEY_MOP_FORCE, mopForce);
            SpUtils.saveBoolean(MyApplication.getInstance(), physicalId + KEY_VOICE_OPEN, voiceOpen);
        }
        mView.clearAll(curStatus);//清空所有布局，以便根据status更新显示布局
        if (curStatus == 0x07) {//虚拟墙编辑模式
            isVirtualEdit = true;
            mView.showVirtualEdit();
            mView.setMapViewVisible(true);
        } else if (curStatus == 0x05) { //重点
            mView.setPointViewVisible(true);
            if (!hasStart) {
                mView.updateQuanAnimation(true);
                hasStart = true;
            }
            mView.setTvUseStatus(TAG_KEYPOINT);
        } else if (curStatus == 0x0A) { //遥控
            mView.setTvUseStatus(TAG_CONTROL);
        } else if (curStatus == 0x04) {//沿墙模式
            mView.setAlongViewVisible(true);
            if (!hasStart_) {
                mView.updateAlongAnimation(true);
                hasStart_ = true;
            }
            mView.setTvUseStatus(TAG_ALONG);
        } else if (canEdit(curStatus)) {
            mView.showBottomView();
        }
        mView.updateOperationViewStatue(curStatus);
        mView.setVirtualWallStatus(canEdit(curStatus));
    }

    @Override
    public boolean canEdit(int curStatus) {
        // 0X02待机 0x06规划 0x08回冲 0x09充电 0x0D 临时中点 0x0C暂停
        return curStatus == 0x02 || curStatus == 0x06 || curStatus == 0x08 ||
                curStatus == 0x09 || curStatus == 0x0D || curStatus == 0x0C;
    }


    @Override
    public boolean isWork(int curStatus) {
        if (curStatus == 0x03 || curStatus == 0x04 ||
                curStatus == 0x05 || curStatus == 0x06 ||
                curStatus == 0x08) {
            return true;
        }
        return false;
    }


    private boolean hasAppoint(byte[] resp) {
        for (int j = 1; j <= 31; j += 5) {
            if (resp[j] != 0) {
                return true;
            }
        }
        return false;
    }

    private void adjustTime() {
        ACDeviceMsg msg_adjustTime = new ACDeviceMsg(MsgCodeUtils.AdjustTime, TimeUtil.getTimeBytes());
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, msg_adjustTime, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void success(ACDeviceMsg acDeviceMsg) {

            }

            @Override
            public void error(ACException e) {

            }
        });
    }

    @Override
    public void initPropReceiver() {
        propertyReceiver = (s, l, s1) -> {
            MyLog.e(TAG, "onPropertyReceive ==== " + s1);
            if (isViewAttached()) {
                PropertyInfo info = gson.fromJson(s1, PropertyInfo.class);
                MyLog.e(TAG, "initPropReceiver onPropertyReceive errorCode = " + info.getError_info());
                errorCode = info.getError_info();
                batteryNo = info.getBattery_level();
                curStatus = info.getWork_pattern();
                isMaxMode = info.getVacuum_cleaning() == 0x01;
                mopForce = info.getCleaning_cleaning();
                voiceOpen = info.getVoice_mode() == 0x01;
                setStatus(curStatus, batteryNo, mopForce, isMaxMode, voiceOpen);
                if (curStatus != 0x06) {//0x06规划模式
                    mView.updateCleanTime(Utils.getString(R.string.map_aty_gang));
                    mView.updateCleanArea(Utils.getString(R.string.map_aty_gang));
                } else {
                    mView.updateCleanArea(cleanArea / 100.0 + "㎡");
                    mView.updateCleanTime(workTime / 60 + "min");
                }
                mView.showErrorPopup(errorCode);
                if (errorCode != 0) {
                    mView.setMapViewVisible(false);
                }
            }
        };
    }

    @Override
    public void registerPropReceiver() {
        AC.deviceDataMgr().subscribeProperty(subdomain, deviceId,
                new VoidCallback() {
                    @Override
                    public void success() {
                        AC.deviceDataMgr().registerPropertyReceiver(propertyReceiver);
                    }

                    @Override
                    public void error(ACException e) {

                    }
                }
        );
    }

    @Override
    public void sendToDeviceWithOption(ACDeviceMsg msg) {
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, msg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void error(ACException e) {
                ToastUtils.showErrorToast(MyApplication.getInstance(), e.getErrorCode());
            }

            @Override
            public void success(ACDeviceMsg deviceMsg) {
                byte[] bytes = deviceMsg.getContent();
                curStatus = bytes[0];
                setStatus(curStatus, -1, mopForce, isMaxMode, voiceOpen);
            }
        });
    }


    @Override
    public void sendToDeviceWithOption_start(ACDeviceMsg msg) {
        sendByte = mAcDevMsg.getContent()[0];
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, msg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void error(ACException e) {
                MyLog.e(TAG, "sendToDeviceWithOption_start error " + e.toString());
                ToastUtils.showErrorToast(MyApplication.getInstance(), e.getErrorCode());
            }

            @Override
            public void success(ACDeviceMsg deviceMsg) {
                byte[] bytes = deviceMsg.getContent();
                MyLog.e(TAG, "sendToDeviceWithOption_start  success " + bytes[0]);
                curStatus = bytes[0];
                if (curStatus == sendByte) {
                    setStatus(curStatus, -1, mopForce, isMaxMode, voiceOpen);
                } else {
                    if (curStatus == 0x0B) {//寻找模式
                        ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_charge));
                    }
                }
            }
        });
    }

    @Override
    public void enterVirtualMode() {
        if (curStatus == 0x06) {
            mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
            mAcDevMsg.setContent(new byte[]{0x07});
            sendByte = mAcDevMsg.getContent()[0];
            sendToDeviceWithOption_start(mAcDevMsg);
        }
    }


    /**
     *
     * @param list SEND_VIR添加虚拟墙时为新增虚拟墙集合，EXIT_VIR 时，为null
     * @param tag
     */
    public void sendVirtualWallData(final ArrayList<int[]> list, final int tag) {
        if (tag==SEND_VIR){
            wallPointList.addAll(list);
        }
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
                    MyLog.e(TAG, "byte arry==:" + startxBytes[0] + "," + startxBytes[1] + "," + startyBytes[0] + "," + startyBytes[1] + "," + endxBytes[0] + "," + endxBytes[1] + "," + endyBytes[0] + "," + endyBytes[1]);
                    MyLog.e(TAG, "byte arry==:" + Integer.toHexString(startxBytes[0]) + "," + Integer.toHexString(startxBytes[1]) + "," + Integer.toHexString(startyBytes[0]) + "," + Integer.toHexString(startyBytes[1]) + "," + Integer.toHexString(endxBytes[0]) + "," + Integer.toHexString(endxBytes[1]) + "," + Integer.toHexString(endyBytes[0]) + "," + Integer.toHexString(endyBytes[1]));
                    MyLog.e(TAG, "xia fa qian wei zhuanhua zuo biao :" + "(" + floats[0] + "," + floats[1] + ")" + ":" + "(" + floats[2] + "," + floats[3] + ")");
                    MyLog.e(TAG, "xia fa qian zhuanhua hou zuo biao :" + "(" + x1 + "," + y1 + ")" + ":" + "(" + x2 + "," + y2 + ")");
                }
            } else {
                MyLog.e(TAG, "sendLists is null");
            }
            mAcDevMsg.setCode(MsgCodeUtils.SetVirtualWall);
            mAcDevMsg.setContent(virtualContentBytes);
            sendToDeviceWithOptionVirtualWall(mAcDevMsg, physicalId, tag);
        }).start();
    }


    /**
     * 申请添加/删除虚拟墙
     *
     * @param acDeviceMsg
     * @param physicalDeviceId
     * @param tag
     */
    public void sendToDeviceWithOptionVirtualWall(ACDeviceMsg acDeviceMsg, String physicalDeviceId, final int tag) {
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalDeviceId, acDeviceMsg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void success(ACDeviceMsg acDeviceMsg) {
                if (tag == SEND_VIR) {//添加虚拟墙成功
                    existPointList.clear();
                    existPointList.addAll(wallPointList);
                    mView.sendHandler(MapActivity_X9_.SEND_VIRTUALDATA_SUCCESS);
                }
                if (tag == EXIT_VIR) {//删除虚拟墙成功
                    wallPointList.clear();
                    wallPointList.addAll(existPointList);
                }
                //TODO 绘制虚拟墙
                mView.drawVirtualWall(existPointList);
            }

            @Override
            public void error(ACException e) {
                mView.sendHandler(MapActivity_X9_.SEND_VIRTUALDATA_FAILED);
            }
        });
    }

    @Override
    public void enterAlongMode() {
        mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
        if (curStatus == 0x02 || curStatus == 0x04 || curStatus == 0x0A) {
            byte b = (byte) (curStatus == 0x04 ? 0x02 : 0x04);
            mAcDevMsg.setContent(new byte[]{b});
            sendToDeviceWithOption(mAcDevMsg);
        } else if (curStatus == 0x09 || curStatus == 0x0B) {
            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_charge));
        } else {
            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_can_not_execute));
        }
    }

    @Override
    public void enterPointMode() {
        if (curStatus == 0x09 || curStatus == 0x0B) {
            ToastUtils.showToast(MyApplication.getInstance(), Utils. getString(R.string.map_aty_charge));
        } else if (curStatus == 0x08 || curStatus == 0x04) {
            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_can_not_execute));
        } else {
            mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
            if (curStatus == 0x05) {
                mAcDevMsg.setContent(new byte[]{0x02});
            } else {
                mAcDevMsg.setContent(new byte[]{0x05});
            }
            sendToDeviceWithOption(mAcDevMsg);
        }
    }

    @Override
    public void enterRechargeMode() {
        if (curStatus == 0x09 || curStatus == 0x0B) {
            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_charge));
        } else if (curStatus == 0x05 || curStatus == 0x04) {
            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_can_not_execute));
        } else {
            mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
            if (curStatus == 0x08) {
                mAcDevMsg.setContent(new byte[]{0x02});
            } else {
                mAcDevMsg.setContent(new byte[]{0x08});
            }
            sendToDeviceWithOption(mAcDevMsg);
        }
    }

    @Override
    public int getCurStatus() {
        return curStatus;
    }
}
