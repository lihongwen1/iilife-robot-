package com.ilife.iliferobot.presenter;

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
import com.accloud.utils.LogUtil;
import com.google.gson.Gson;
import com.ilife.iliferobot.activity.MapActivity_X9_;
import com.ilife.iliferobot.activity.SettingActivity;
import com.ilife.iliferobot.app.MyApplication;
import com.ilife.iliferobot.base.BasePresenter;
import com.ilife.iliferobot.utils.Constants;
import com.ilife.iliferobot.utils.DeviceUtils;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.activity.MainActivity;
import com.ilife.iliferobot.contract.MapX9Contract;
import com.ilife.iliferobot.entity.PropertyInfo;
import com.ilife.iliferobot.entity.RealTimeMapInfo;
import com.ilife.iliferobot.utils.ACSkills;
import com.ilife.iliferobot.utils.DataUtils;
import com.ilife.iliferobot.utils.MsgCodeUtils;
import com.ilife.iliferobot.utils.MyLog;
import com.ilife.iliferobot.utils.SpUtils;
import com.ilife.iliferobot.utils.TimeUtil;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.utils.Utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    boolean isVirtualEdit;
    private byte[] slamBytes;
    private int curStatus, errorCode, batteryNo, workTime, cleanArea;
    private Timer timer;
    private ArrayList<Integer> realTimePoints, historyRoadList;
    private List<int[]> wallPointList = new ArrayList<>();
    private List<int[]> existPointList = new ArrayList<>();
    boolean isWork, hasAppoint, isMaxMode, voiceOpen;//hasSart标记point动画启动状态
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
    private byte sendByte;
    private byte[] virtualContentBytes;

    /**
     * x800实时地图数据
     */
    private ArrayList<Integer> pointList;// map集合
    private ArrayList<String> pointStrList;//

    @Override
    public void attachView(MapX9Contract.View view) {
        super.attachView(view);
        gson = new Gson();
        realTimePoints = new ArrayList<>();
        historyRoadList = new ArrayList<>();
        pointList = new ArrayList<>();
        pointStrList = new ArrayList<>();
        deviceId = SpUtils.getLong(MyApplication.getInstance(), MainActivity.KEY_DEVICEID);
        subdomain = SpUtils.getSpString(MyApplication.getInstance(), MainActivity.KEY_SUBDOMAIN);
        physicalId = SpUtils.getSpString(MyApplication.getInstance(), MainActivity.KEY_PHYCIALID);
        switch (subdomain) {
            case Constants.subdomain_x900:
                robotType = "X900";
                break;
            case Constants.subdomain_x800:
                robotType = "X800";
                break;
            case Constants.subdomain_x787:
                robotType = "X787";
                break;
            default:
                robotType = "X785";
                break;
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

    /**
     * 包含3s查询实时地图和查询虚拟墙
     */
    public void initTimer() {
        getRealTimeMap();//need get real time map in the first time enter
        if (!subdomain.equals(Constants.subdomain_x900)) {//只有x900需要每3s获取实时地图
            return;
        }
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                LogUtil.d(TAG, "getRealTimeMap---" + curStatus);
                if (!isViewAttached()) {
                    return;
                }
                if (curStatus == 0x06) {
                    getRealTimeMap();
                    queryVirtualWall();
                }
            }
        };
        timer.schedule(task, 0, 3 * 1000);
    }

    @Override
    public void getRealTimeMap() {
        if (!isViewAttached()) {//page destroyed
            return;
        }
        final ACMsg req = new ACMsg();
        req.setName("searchCleanRealTime");
        req.put("device_id", deviceId);
        AC.sendToServiceWithoutSign(DeviceUtils.getServiceName(subdomain), Constants.SERVICE_VERSION, req, new PayloadCallback<ACMsg>() {
            @Override
            public void success(ACMsg resp) {
                if (!isViewAttached()) {//回冲或者视图销毁后不绘制路径
                    return;
                }
                if (subdomain.equals(Constants.subdomain_x900)) {
                    String strMap = resp.getString("slam_map");
                    int xMax = resp.getInt("slam_x_max");
                    int xMin = resp.getInt("slam_x_min");
                    int yMin = 1500 - resp.getInt("slam_y_max");
                    int yMax = 1500 - resp.getInt("slam_y_min");
                    if (!TextUtils.isEmpty(strMap)) {
                        slamBytes = Base64.decode(strMap, Base64.DEFAULT);
                        if (isViewAttached() && curStatus != MsgCodeUtils.STATUE_VIRTUAL_EDIT
                                && curStatus != MsgCodeUtils.STATUE_RECHARGE) {
                            //判断isViewAttached避免页面销毁后最后一次的定时器导致程序崩溃 0x07虚拟墙编辑模式,0x08回冲模式下不更新地图
                            mView.updateSlam(xMin, xMax, yMin, yMax);
                            mView.drawSlamMap(slamBytes);
                            mView.drawRoadMap(realTimePoints, historyRoadList);
                            mView.drawObstacle();
                            mView.drawVirtualWall(null);//只是刷新虚拟墙
                        }
                    }
                } else {//x800系列
                    ArrayList<ACObject> data = resp.get("data");
                    if (data == null || data.size() == 0) {
                        return;
                    }
                    for (int i = 0; i < data.size(); i++) {
                        parseRealTimeMapX8(data.get(i).getString("clean_data"));
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
        AC.sendToServiceWithoutSign(DeviceUtils.getServiceName(subdomain), Constants.SERVICE_VERSION, req, new PayloadCallback<ACMsg>() {
            @Override
            public void success(ACMsg acMsg) {
                if (!isViewAttached()) {
                    return;
                }
                Log.d(TAG, "getHistoryRoad()-----------");
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
     * //TODO 虚拟墙实时更新
     * 查询虚拟墙
     */
    public void queryVirtualWall() {
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, ACSkills.get().queryVirtual(), Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void success(ACDeviceMsg acDeviceMsg) {
                if (!isViewAttached()) {
                    return;
                }
                existPointList.clear();
                byte[] resp = acDeviceMsg.getContent();
                StringBuilder stringBuilder = new StringBuilder();
                if (resp != null && resp.length > 0) {
                    byte count = resp[1];//虚拟墙总数
                    for (int i = 9; i < 8 * count + 2; i += 8) {
                        //地图坐标
                        int startX = DataUtils.bytesToInt(new byte[]{resp[i - 7], resp[i - 6]}, 0);//2,3
                        int startY = DataUtils.bytesToInt(new byte[]{resp[i - 5], resp[i - 4]}, 0);//4,5
                        int endX = DataUtils.bytesToInt(new byte[]{resp[i - 3], resp[i - 2]}, 0);//6,7
                        int endY = DataUtils.bytesToInt(new byte[]{resp[i - 1], resp[i]}, 0);//8,9
                        Log.d("queryVirtualWall", "original:" + startX + "---" + startY + "---" + endX + "----" + endY);
                        //显示坐标(加移量后)
                        int sx = startX + 750;
                        int sy = 1500 - (startY + 750);
                        int ex = endX + 750;
                        int ey = 1500 - (endY + 750);
                        int[] dataPoint = {sx, sy, ex, ey};
                        existPointList.add(dataPoint);
                        //TODO saveQueryRect
//                        saveQueryRect(dataPoint);
                        Log.d("queryVirtualWall", sx + "---" + sy + "---" + ex + "----" + ey);
                    }

                    wallPointList.clear();
                    wallPointList.addAll(existPointList);
                    mView.drawVirtualWall(existPointList);
                }
            }

            @Override
            public void error(ACException e) {
                LogUtil.d(TAG, e.getErrorCode() + e.toString());
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
                                Log.d(TAG, "received map data------" + s1);
                                if (!isViewAttached()) {//回冲或者视图销毁后不绘制路径
                                    return;
                                }
                                if (subdomain.equals(Constants.subdomain_x900)) {
                                    parseRealTimeMapX9(s1);
                                } else {
                                    Gson gson = new Gson();
                                    RealTimeMapInfo mapInfo = gson.fromJson(s1, RealTimeMapInfo.class);
                                    String clean_data = mapInfo.getClean_data();
                                    parseRealTimeMapX8(clean_data);
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
            mView.updateCleanTime(getTimeValue());
            mView.updateCleanArea(getAreaValue());
            for (int j = 7; j < bytes.length; j += 4) {
                int x = DataUtils.bytesToInt(new byte[]{bytes[j - 3], bytes[j - 2]}, 0);
                int y = DataUtils.bytesToInt(new byte[]{bytes[j - 1], bytes[j]}, 0);
                if ((x == 0x7fff) & (y == 0x7fff)) {
                    MyLog.e(TAG, "subscribeRealTimeMap===== (x==0x7fff)&(y==0x7fff) 地图被清掉了");
                    pointList.clear();
                    pointStrList.clear();
                    workTime = 0;
                    cleanArea = 0;
                } else {
                    if ((j == bytes.length - 1) || !pointStrList.contains(x + "_" + y)) {
                        pointList.add(x);
                        pointList.add(y);
                        pointStrList.add(x + "_" + y);
                    }
                }
            }
        }
        if (pointList != null) {
            mView.drawBoxMapX8(pointList);
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
            workTime = mapInfo.getReal_clean_time();
            cleanArea = mapInfo.getReal_clean_area();
            mView.updateCleanTime(getTimeValue());
            mView.updateCleanArea(getAreaValue());
            if (curStatus == MsgCodeUtils.STATUE_RECHARGE) {//回冲不绘制路径
                return;
            }
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
                    if ((x == 0x7fff) & (y == 0x7fff)) {//出现错误的坐标信息，放弃所有数据
//                        mView.updateCleanArea(Utils.getString(R.string.map_aty_gang));
//                        mView.updateCleanTime(Utils.getString(R.string.map_aty_gang));
                        mView.cleanMapView();
                        realTimePoints.clear();
                        historyRoadList.clear();
                        wallPointList.clear();
                        return;
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
        }
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
                        if (!isViewAttached()) {
                            return;
                        }
                        byte[] bytes = deviceMsg.getContent();
                        if (bytes != null) {
                            errorCode = bytes[8];
                            curStatus = bytes[0];
                            batteryNo = bytes[5];
                            mopForce = bytes[4];
                            isMaxMode = bytes[3] == 0x01;
                            voiceOpen = bytes[6] == 0x01;
                            Log.d(TAG, "set statue,and statue code is 430:" + curStatus);
                            setStatus(curStatus, batteryNo, mopForce, isMaxMode, voiceOpen);
                            mView.updateCleanArea(getAreaValue());
                            mView.updateCleanTime(getTimeValue());
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

    public void setStatus(int curStatus, int batteryNo, int mopForce, boolean isMaxMode, boolean voiceOpen) {

        if (!isViewAttached()) {
            return;
        }
        if (batteryNo != -1) {
            mView.setBatteryImage(curStatus, batteryNo);
            SpUtils.saveBoolean(MyApplication.getInstance(), physicalId + KEY_IS_MAX, isMaxMode);
            SpUtils.saveInt(MyApplication.getInstance(), physicalId + KEY_MOP_FORCE, mopForce);
            SpUtils.saveBoolean(MyApplication.getInstance(), physicalId + KEY_VOICE_OPEN, voiceOpen);
        }
        mView.clearAll(curStatus);//清空所有布局，以便根据status更新显示布局
        isWork = isWork(curStatus);
        mView.updateStatue(DeviceUtils.getStatusStr(MyApplication.getInstance(), curStatus, errorCode));//待机，规划
        mView.updateStartStatue(isWork, isWork ? Utils.getString(R.string.map_aty_stop) : Utils.getString(R.string.map_aty_start));
        mView.updateOperationViewStatue(curStatus);
        if (curStatus == MsgCodeUtils.STATUE_PLANNING || curStatus == MsgCodeUtils.STATUE_CHARGING_ || curStatus == MsgCodeUtils.STATUE_CHARGING) {
            mView.setCurrentBottom(MapActivity_X9_.USE_MODE_NORMAL);
        }
        if (/*curStatus == MsgCodeUtils.STATUE_RECHARGE ||*/ curStatus == MsgCodeUtils.STATUE_REMOTE_CONTROL || curStatus == MsgCodeUtils.STATUE_POINT
                || curStatus == MsgCodeUtils.STATUE_ALONG) {
            mView.setCurrentBottom(MapActivity_X9_.USE_MODE_REMOTE_CONTROL);
        }
        mView.showBottomView();
        Log.d(TAG, "set statue,and statue code is:" + curStatus);
        if (curStatus == 0x08) { //回充
            mView.updateRecharge(true);
            mView.setTvUseStatus(MapActivity_X9_.TAG_RECHAGRGE);
        } else if (curStatus == 0x07) {//虚拟墙编辑模式
            isVirtualEdit = true;
            mView.showVirtualEdit();
            mView.setMapViewVisible(true);
        } else if (curStatus == 0x05) { //重点
            mView.updatePoint(true);
            mView.setTvUseStatus(MapActivity_X9_.TAG_KEYPOINT);
        } else if (curStatus == 0x0A) { //遥控
//            mView.setTvUseStatusVisible(true);
        } else if (curStatus == 0x04) {//沿墙模式
            mView.updateAlong(true);
            mView.setTvUseStatus(MapActivity_X9_.TAG_ALONG);
        } else if (canEdit(curStatus)) {
            mView.showBottomView();
        }
    }

    @Override
    public boolean canEdit(int curStatus) {
        // 0X02待机 0x06规划 0x08回冲 0x09充电 0x0D 临时中点 0x0C暂停
        return curStatus == 0x02 || curStatus == 0x06 || curStatus == 0x08 ||
                curStatus == 0x09 || curStatus == 0x0D || curStatus == 0x0C;
    }


    @Override
    public boolean isWork(int curStatus) {
        return curStatus == 0x03 || curStatus == 0x04 ||
                curStatus == 0x05 || curStatus == 0x06 ||
                curStatus == 0x08;
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
                isMaxMode = info.getVacuum_cleaning() == MsgCodeUtils.CLEANNING_CLEANING_MAX;
                mopForce = info.getCleaning_cleaning();
                voiceOpen = info.getVoice_mode() == 0x01;
                Log.d(TAG, "set statue,and statue code is 571:" + curStatus);
                setStatus(curStatus, batteryNo, mopForce, isMaxMode, voiceOpen);
                mView.updateCleanArea(getAreaValue());
                mView.updateCleanTime(getTimeValue());
                mView.showErrorPopup(errorCode);
                if (errorCode != 0) {
                    mView.setMapViewVisible(false);
                }
            }
        };
    }

    private String getAreaValue() {
        BigDecimal bg = new BigDecimal(cleanArea / 100.0f);
        double area = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        if (curStatus == MsgCodeUtils.STATUE_POINT || curStatus == MsgCodeUtils.STATUE_ALONG || curStatus == MsgCodeUtils.STATUE_SLEEPING || curStatus == MsgCodeUtils.STATUE_WAIT || curStatus == MsgCodeUtils.STATUE_RECHARGE
                || curStatus == MsgCodeUtils.STATUE_CHARGING || curStatus == MsgCodeUtils.STATUE_CHARGING_ || curStatus == MsgCodeUtils.STATUE_REMOTE_CONTROL) {
            return Utils.getString(R.string.map_aty_gang);
        } else {
            return area + "㎡";
        }
    }

    private String getTimeValue() {
        int min = Math.round(workTime / 60f);
        if (curStatus == MsgCodeUtils.STATUE_POINT || curStatus == MsgCodeUtils.STATUE_ALONG || curStatus == MsgCodeUtils.STATUE_SLEEPING || curStatus == MsgCodeUtils.STATUE_WAIT || curStatus == MsgCodeUtils.STATUE_RECHARGE
                || curStatus == MsgCodeUtils.STATUE_CHARGING || curStatus == MsgCodeUtils.STATUE_CHARGING_ || curStatus == MsgCodeUtils.STATUE_REMOTE_CONTROL) {
            return Utils.getString(R.string.map_aty_gang);
        } else {
            return min + "min";
        }
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
        sendByte = msg.getContent()[0];
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, msg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void error(ACException e) {
                ToastUtils.showErrorToast(MyApplication.getInstance(), e.getErrorCode());
            }

            @Override
            public void success(ACDeviceMsg deviceMsg) {
                switch (deviceMsg.getCode()) {
                    case MsgCodeUtils.CleanForce://更新max mode
                        byte[] resp = deviceMsg.getContent();
                        isMaxMode = resp[0] == 0x01;
                        mView.updateMaxButton(isMaxMode);
                        SpUtils.saveBoolean(MyApplication.getInstance(), physicalId + KEY_IS_MAX, isMaxMode);
                        break;
                    case MsgCodeUtils.Proceed://遥控器模式
                        int subCode = deviceMsg.getContent()[0];
                        int tag_code = -1;
                        if (subCode == 0x01) {
                            tag_code = MapActivity_X9_.TAG_FORWAD;
                        } else if (subCode == 0x03) {
                            tag_code = MapActivity_X9_.TAG_LEFT;
                        } else if (subCode == 0x04) {
                            tag_code = MapActivity_X9_.TAG_RIGHT;
                        }
                        if (tag_code != -1) {
                            mView.setTvUseStatus(tag_code);
                        }
                        break;
                    case MsgCodeUtils.WorkMode://下发工作模式
                        byte[] bytes = deviceMsg.getContent();
                        curStatus = bytes[0];
                        if (curStatus == sendByte) {
                            setStatus(curStatus, -1, mopForce, isMaxMode, voiceOpen);
                        } else {
                            if (curStatus == 0x0B) {//寻找模式
                                ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_charge));
                            }
                        }
                        break;
                }
            }
        });
    }


    @Override
    public void enterVirtualMode() {
        if (curStatus == MsgCodeUtils.STATUE_PLANNING) {//规划模式下进入虚拟墙
            ACDeviceMsg acDeviceMsg = ACSkills.get().enterVirtualMode();
            sendByte = acDeviceMsg.getContent()[0];
            sendToDeviceWithOption(acDeviceMsg);
        }
    }


    /**
     * @param list SEND_VIR添加虚拟墙时为新增虚拟墙集合，EXIT_VIR 时，为null
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
                    MyLog.e(TAG, "byte arry==:" + startxBytes[0] + "," + startxBytes[1] + "," + startyBytes[0] + "," + startyBytes[1] + "," + endxBytes[0] + "," + endxBytes[1] + "," + endyBytes[0] + "," + endyBytes[1]);
                    MyLog.e(TAG, "byte arry==:" + Integer.toHexString(startxBytes[0]) + "," + Integer.toHexString(startxBytes[1]) + "," + Integer.toHexString(startyBytes[0]) + "," + Integer.toHexString(startyBytes[1]) + "," + Integer.toHexString(endxBytes[0]) + "," + Integer.toHexString(endxBytes[1]) + "," + Integer.toHexString(endyBytes[0]) + "," + Integer.toHexString(endyBytes[1]));
                    MyLog.e(TAG, "xia fa qian wei zhuanhua zuo biao :" + "(" + floats[0] + "," + floats[1] + ")" + ":" + "(" + floats[2] + "," + floats[3] + ")");
                    MyLog.e(TAG, "xia fa qian zhuanhua hou zuo biao :" + "(" + x1 + "," + y1 + ")" + ":" + "(" + x2 + "," + y2 + ")");
                }
            } else {
                MyLog.e(TAG, "sendLists is null");
            }
            sendToDeviceWithOptionVirtualWall(ACSkills.get().setVirtualWall(virtualContentBytes), physicalId);
        }).start();
    }


    /**
     * 申请添加/删除虚拟墙
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
                mView.sendHandler(MapActivity_X9_.SEND_VIRTUALDATA_SUCCESS);
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
        if (curStatus == MsgCodeUtils.STATUE_WAIT || curStatus == MsgCodeUtils.STATUE_ALONG || (curStatus == MsgCodeUtils.STATUE_POINT&&!subdomain.equals(Constants.subdomain_x900)) || curStatus == MsgCodeUtils.STATUE_REMOTE_CONTROL ||
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
        if (curStatus == MsgCodeUtils.STATUE_WAIT || curStatus == MsgCodeUtils.STATUE_POINT || (curStatus == MsgCodeUtils.STATUE_ALONG &&!subdomain.equals(Constants.subdomain_x900))|| curStatus == MsgCodeUtils.STATUE_REMOTE_CONTROL ||
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
        if (timer != null) {
            timer.cancel();
        }
        super.detachView();
    }

}
