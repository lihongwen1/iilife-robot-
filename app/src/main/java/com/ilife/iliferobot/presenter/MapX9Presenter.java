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
import com.ilife.iliferobot.activity.MapActivity_X9_;
import com.ilife.iliferobot.activity.SettingActivity;
import com.ilife.iliferobot.app.MyApplication;
import com.ilife.iliferobot.base.BasePresenter;
import com.ilife.iliferobot.able.Constants;
import com.ilife.iliferobot.able.DeviceUtils;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.activity.MainActivity;
import com.ilife.iliferobot.contract.MapX9Contract;
import com.ilife.iliferobot.entity.PropertyInfo;
import com.ilife.iliferobot.entity.RealTimeMapInfo;
import com.ilife.iliferobot.able.ACSkills;
import com.ilife.iliferobot.utils.DataUtils;
import com.ilife.iliferobot.able.MsgCodeUtils;
import com.ilife.iliferobot.utils.MyLogger;
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

//TODO 处理X900的暂停事件的模拟事件更新UI问题
// TODO APP后台CPU消耗问题
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
    boolean isWork, isMaxMode, voiceOpen;

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
    private int device_type;
    private byte sendByte;
    private byte[] virtualContentBytes;
    /**
     * x800实时地图数据
     */
    private ArrayList<Integer> pointList;// map集合
    private ArrayList<String> pointStrList;
    private boolean isSubscribeRealMap, isInitSlamTimer;

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
            case Constants.subdomain_a9s:
                robotType = "a9s";
                break;
            case Constants.subdomain_a8s:
                robotType = "a8s";
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
     * 包含3s查询实时地图(slam map)
     */
    public void initTimer() {
        if (!subdomain.equals(Constants.subdomain_x900)) {//只有x900需要每3s获取实时地图
            return;
        }
        isInitSlamTimer = true;
        getRealTimeMap();//need get real time map in the first time enter
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                LogUtil.d(TAG, "getRealTimeMap---" + curStatus);
                if (!isViewAttached()) {
                    return;
                }
                if (curStatus == MsgCodeUtils.STATUE_PLANNING) {
                    getRealTimeMap();
                }
            }
        };
        timer.schedule(task, 0, 3 * 1000);
    }

    /**
     * x900 slam map
     * x800 history road
     */
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
                    MyLogger.d(TAG, "xMax:" + xMax + "  xmin:" + xMin + "   ymax:" + yMax + "    ymin" + yMin);
                    if (!TextUtils.isEmpty(strMap)) {
                        slamBytes = Base64.decode(strMap, Base64.DEFAULT);
                        if (isViewAttached()) {
                            //判断isViewAttached避免页面销毁后最后一次的定时器导致程序崩溃 0x07电子墙编辑模式,0x08回冲模式下不更新地图
                            mView.updateSlam(xMin, xMax, yMin, yMax);
                            if (curStatus != MsgCodeUtils.STATUE_VIRTUAL_EDIT
                                    && curStatus != MsgCodeUtils.STATUE_RECHARGE && curStatus != MsgCodeUtils.STATUE_OFF_LINE && curStatus != MsgCodeUtils.STATUE_CHARGING_
                                    && curStatus != MsgCodeUtils.STATUE_CHARGING) {
                                mView.drawSlamMap(slamBytes);
                                mView.drawRoadMap(realTimePoints, historyRoadList);
                                mView.drawObstacle();
                                mView.drawVirtualWall(null);//只是刷新电子墙
                            }
                        }
                    }
                } else {//x800系列
                    ArrayList<ACObject> data = resp.get("data");
                    if (data == null || data.size() == 0) {
                        return;
                    }
                    for (int i = 0; i < data.size(); i++) {
                        parseRealTimeMapX8(data.get(i).getString("clean_data"), false);
                    }
                }
            }

            @Override
            public void error(ACException e) {
                MyLogger.e(TAG, "getRealTimeMap e = " + e.toString());
            }
        });
    }

    @Override
    public void getHistoryRoad() {
        final ACMsg req = new ACMsg();
        req.setName("searchCleanRoadDataMore");
        req.put("device_id", deviceId);
        AC.sendToServiceWithoutSign(DeviceUtils.getServiceName(subdomain), Constants.SERVICE_VERSION, req, new PayloadCallback<ACMsg>() {
            @Override
            public void success(ACMsg acMsg) {
                if (!isViewAttached()) {
                    return;
                }
                MyLogger.d(TAG, "getHistoryRoad()-----------");
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
     * //TODO 电子墙实时更新
     * 查询电子墙
     */
    public void queryVirtualWall() {
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, ACSkills.get().queryVirtual(), Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void success(ACDeviceMsg acDeviceMsg) {
                if (!isViewAttached() || curStatus == MsgCodeUtils.STATUE_RECHARGE ||
                        curStatus == MsgCodeUtils.STATUE_CHARGING || curStatus == MsgCodeUtils.STATUE_CHARGING_) {
                    return;
                }
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
                        MyLogger.d("queryVirtualWall", "original:" + startX + "---" + startY + "---" + endX + "----" + endY);
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
                        MyLogger.d("queryVirtualWall", sx + "---" + sy + "---" + ex + "----" + ey);
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


    /**
     * 订阅实时地图
     */
    public void subscribeRealTimeMap() {
        Map<String, Object> primaryKey = new HashMap<>();
        primaryKey.put("device_id", deviceId);
        AC.classDataMgr().subscribe("clean_realtime", primaryKey, ACClassDataMgr.OPTYPE_ALL,
                new VoidCallback() {
                    @Override
                    public void success() {
                        isSubscribeRealMap = true;
                        AC.classDataMgr().registerDataReceiver((s, i, s1) -> {
                            MyLogger.d(TAG, "received map data------" + s1);
                            if (!isViewAttached()) {//
                                return;
                            }
                            if (subdomain.equals(Constants.subdomain_x900)) {
                                parseRealTimeMapX9(s1);
                            } else {//x800 x785 x787 series
                                Gson gson = new Gson();
                                RealTimeMapInfo mapInfo = gson.fromJson(s1, RealTimeMapInfo.class);
                                String clean_data = mapInfo.getClean_data();
                                parseRealTimeMapX8(clean_data, true);
                            }
                        });
                    }

                    @Override
                    public void error(ACException e) {
                        MyLogger.d(TAG, "订阅实时地图异常" + e.getMessage());
                    }
                }
        );
    }

    /**
     * x800绘制黄方格地图
     *
     * @param clean_data
     * @param needDraw   for x8 series robot ,will be no need to draw map using these data,but save these data,when data is from the method -'getRealTimeMap'
     */
    private void parseRealTimeMapX8(String clean_data, boolean needDraw) {
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
                    MyLogger.e(TAG, "subscribeRealTimeMap===== (x==0x7fff)&(y==0x7fff) 地图被清掉了");
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
        if (needDraw && pointList != null) {
            mView.drawBoxMapX8(pointList);
            mView.updateCleanTime(getTimeValue());
            mView.updateCleanArea(getAreaValue());
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
            if (curStatus == MsgCodeUtils.STATUE_RECHARGE || curStatus == MsgCodeUtils.STATUE_CHARGING_ || curStatus == MsgCodeUtils.STATUE_CHARGING) {//回冲不绘制路径
                return;
            }
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
     * 获取设备状态
     */
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
                            batteryNo = bytes[5];
                            mopForce = bytes[4];
                            isMaxMode = bytes[3] == 0x01;
                            voiceOpen = bytes[6] == 0x01;
                            curStatus = bytes[0];
                            MyLogger.d(TAG, "gain the device statue and the status is :" + curStatus);
                            setStatus(curStatus, batteryNo, mopForce, isMaxMode, voiceOpen);
                            mView.updateCleanArea(getAreaValue());
                            mView.updateCleanTime(getTimeValue());
                            mView.showErrorPopup(errorCode);
                            if (errorCode != 0) {
                                mView.cleanMapView();
                            }
                            /**
                             * 所有设备需调用
                             */
                            adjustTime();
                            registerPropReceiver();
                            //请求设备相关数据
                            if (robotType.equals("X900")) {
                                if (!isSubscribeRealMap) {
                                    subscribeRealTimeMap();
                                }
                                if (!isInitSlamTimer) {
                                    initTimer();
                                }
                                getHistoryRoad();
                                queryVirtualWall();
                            } else {//x800系列
                                getRealTimeMap();
                                if (!isSubscribeRealMap) {
                                    subscribeRealTimeMap();
                                }
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
        MyLogger.d(TAG, "setStatus----------curStatus" + curStatus);
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
        MyLogger.d(TAG, "set statue,and statue code is:" + curStatus);
        if (curStatus == MsgCodeUtils.STATUE_CHARGING || curStatus == MsgCodeUtils.STATUE_CHARGING_ || curStatus == MsgCodeUtils.STATUE_SLEEPING || ((robotType.equals("X900") && curStatus == MsgCodeUtils.STATUE_WAIT))) {//休眠，或者x900的待机不显示地图
            mView.cleanMapView();
        } else if (curStatus == MsgCodeUtils.STATUE_RECHARGE) { //回充
            mView.updateRecharge(true);
            mView.cleanMapView();
            mView.setTvUseStatus(MapActivity_X9_.TAG_RECHAGRGE);
        } else if (curStatus == MsgCodeUtils.STATUE_VIRTUAL_EDIT) {//电子墙编辑模式
            isVirtualEdit = true;
            mView.showVirtualEdit();
        } else if (curStatus == MsgCodeUtils.STATUE_POINT) { //重点
            mView.updatePoint(true);
            mView.cleanMapView();
            mView.setTvUseStatus(MapActivity_X9_.TAG_KEYPOINT);
        } else if (curStatus == MsgCodeUtils.STATUE_REMOTE_CONTROL) { //遥控
            mView.setTvUseStatusVisible(true);
        } else if (curStatus == MsgCodeUtils.STATUE_ALONG) {//沿墙模式
            mView.updateAlong(true);
            mView.cleanMapView();
            mView.setTvUseStatus(MapActivity_X9_.TAG_ALONG);
        }
    }

    @Override
    public boolean isLowPowerWorker() {
        return batteryNo < 6 && curStatus != MsgCodeUtils.STATUE_OFF_LINE;
    }

    @Override
    public boolean canEdit(int curStatus) {
        // 0X02待机 0x06规划 0x08回冲 0x09充电 0x0D 临时中点 0x0C暂停
        return curStatus == MsgCodeUtils.STATUE_WAIT || curStatus == MsgCodeUtils.STATUE_PLANNING || curStatus == MsgCodeUtils.STATUE_RECHARGE ||
                curStatus == MsgCodeUtils.STATUE_CHARGING || curStatus == MsgCodeUtils.STATUE_TEMPORARY_POINT || curStatus == MsgCodeUtils.STATUE_PAUSE;
    }


    @Override
    public boolean isWork(int curStatus) {
        return curStatus == MsgCodeUtils.STATUE_RANDOM || curStatus == MsgCodeUtils.STATUE_ALONG ||
                curStatus == MsgCodeUtils.STATUE_POINT || curStatus == MsgCodeUtils.STATUE_TEMPORARY_POINT || curStatus == MsgCodeUtils.STATUE_PLANNING ||
                curStatus == MsgCodeUtils.STATUE_RECHARGE;
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
                MyLogger.d(TAG, "adjust Time Success");
            }

            @Override
            public void error(ACException e) {
                MyLogger.d(TAG, "adjust Time Fail");
            }
        });
    }

    /**
     * 注册状态变化监听
     */
    @Override
    public void initPropReceiver() {
        propertyReceiver = (s, l, s1) -> {
            MyLogger.e(TAG, "onPropertyReceive ==== " + s1);
            if (isViewAttached()) {
                PropertyInfo info = gson.fromJson(s1, PropertyInfo.class);
                MyLogger.e(TAG, "initPropReceiver onPropertyReceive errorCode = " + info.getError_info());
                errorCode = info.getError_info();
                batteryNo = info.getBattery_level();
                isMaxMode = info.getVacuum_cleaning() == MsgCodeUtils.CLEANNING_CLEANING_MAX;
                mopForce = info.getCleaning_cleaning();
                voiceOpen = info.getVoice_mode() == 0x01;
                device_type = info.getDevice_type();
                int lastStatus = curStatus;
                curStatus = info.getWork_pattern();
                if (curStatus==MsgCodeUtils.STATUE_PLANNING||(lastStatus == MsgCodeUtils.STATUE_VIRTUAL_EDIT && lastStatus != curStatus)) {//退出电子墙编辑模式时查询电子墙
                    queryVirtualWall();
                }
                MyLogger.d(TAG, "set statue,and statue code is 571:" + curStatus);
                setStatus(curStatus, batteryNo, mopForce, isMaxMode, voiceOpen);
                mView.updateCleanArea(getAreaValue());
                mView.updateCleanTime(getTimeValue());
                mView.showErrorPopup(errorCode);
                if (errorCode != 0) {
                    mView.cleanMapView();
                }
            }
        };
    }

    private String getAreaValue() {
        BigDecimal bg = new BigDecimal(cleanArea / 100.0f);
        double area = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        if (curStatus == MsgCodeUtils.STATUE_OFF_LINE || curStatus == MsgCodeUtils.STATUE_POINT || curStatus == MsgCodeUtils.STATUE_ALONG || curStatus == MsgCodeUtils.STATUE_SLEEPING || curStatus == MsgCodeUtils.STATUE_WAIT || curStatus == MsgCodeUtils.STATUE_RECHARGE
                || curStatus == MsgCodeUtils.STATUE_CHARGING || curStatus == MsgCodeUtils.STATUE_CHARGING_ || curStatus == MsgCodeUtils.STATUE_REMOTE_CONTROL) {
            return Utils.getString(R.string.map_aty_gang);
        } else {
            return area + "㎡";
        }
    }

    private String getTimeValue() {
        MyLogger.d(TAG, "curStatus=======" + curStatus);
        int min = Math.round(workTime / 60f);
        if (curStatus == MsgCodeUtils.STATUE_OFF_LINE || curStatus == MsgCodeUtils.STATUE_POINT || curStatus == MsgCodeUtils.STATUE_ALONG || curStatus == MsgCodeUtils.STATUE_SLEEPING || curStatus == MsgCodeUtils.STATUE_WAIT || curStatus == MsgCodeUtils.STATUE_RECHARGE
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
                        if (propertyReceiver == null) {
                            initPropReceiver();
                        }
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
                MyLogger.d(TAG, "command failed reason" + e.getMessage());
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
                        int subCode = deviceMsg.getContent()[0];
                        int tag_code = -1;
                        if (subCode == 0x01) {
                            tag_code = MapActivity_X9_.TAG_FORWARD;
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
                        if (curStatus == MsgCodeUtils.STATUE_PLANNING && sendByte == MsgCodeUtils.STATUE_WAIT) {
                            curStatus = MsgCodeUtils.STATUE_PAUSE;
                            sendByte = MsgCodeUtils.STATUE_PAUSE;
                        } else {
                            curStatus = bytes[0];
                        }
                        if (curStatus != MsgCodeUtils.STATUE_PLANNING && curStatus == sendByte && robotType.equals("X900")) {//900的暂停模式发的是待机命令，不准确
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
        if (curStatus == MsgCodeUtils.STATUE_PLANNING) {//规划模式下进入电子墙
            ACDeviceMsg acDeviceMsg = ACSkills.get().enterVirtualMode();
            sendByte = acDeviceMsg.getContent()[0];
            sendToDeviceWithOption(acDeviceMsg);
        }
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
                    MyLogger.e(TAG, "byte arry==:" + startxBytes[0] + "," + startxBytes[1] + "," + startyBytes[0] + "," + startyBytes[1] + "," + endxBytes[0] + "," + endxBytes[1] + "," + endyBytes[0] + "," + endyBytes[1]);
                    MyLogger.e(TAG, "byte arry==:" + Integer.toHexString(startxBytes[0]) + "," + Integer.toHexString(startxBytes[1]) + "," + Integer.toHexString(startyBytes[0]) + "," + Integer.toHexString(startyBytes[1]) + "," + Integer.toHexString(endxBytes[0]) + "," + Integer.toHexString(endxBytes[1]) + "," + Integer.toHexString(endyBytes[0]) + "," + Integer.toHexString(endyBytes[1]));
                    MyLogger.e(TAG, "xia fa qian wei zhuanhua zuo biao :" + "(" + floats[0] + "," + floats[1] + ")" + ":" + "(" + floats[2] + "," + floats[3] + ")");
                    MyLogger.e(TAG, "xia fa qian zhuanhua hou zuo biao :" + "(" + x1 + "," + y1 + ")" + ":" + "(" + x2 + "," + y2 + ")");
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
        if (curStatus == MsgCodeUtils.STATUE_WAIT || curStatus == MsgCodeUtils.STATUE_ALONG || (curStatus == MsgCodeUtils.STATUE_POINT && !subdomain.equals(Constants.subdomain_x900) && !subdomain.equals(Constants.subdomain_x800)) || curStatus == MsgCodeUtils.STATUE_REMOTE_CONTROL ||
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
        if (curStatus == MsgCodeUtils.STATUE_WAIT || curStatus == MsgCodeUtils.STATUE_POINT || (curStatus == MsgCodeUtils.STATUE_ALONG && !subdomain.equals(Constants.subdomain_x900) && !subdomain.equals(Constants.subdomain_x800)) || curStatus == MsgCodeUtils.STATUE_REMOTE_CONTROL ||
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
        if (timer != null) {
            timer.cancel();
        }
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
        super.detachView();
    }

}
