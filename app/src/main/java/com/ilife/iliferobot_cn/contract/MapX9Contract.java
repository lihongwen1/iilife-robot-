package com.ilife.iliferobot_cn.contract;

import com.accloud.service.ACDeviceMsg;
import com.ilife.iliferobot_cn.base.BaseView;

import java.util.ArrayList;
import java.util.List;

public interface MapX9Contract {
    interface Model {

    }

    interface View extends BaseView {

        void updateSlam(int xMin, int xMax, int yMin, int yMax, byte[] slamBytes);

        void drawRoadMap(ArrayList<Integer> roadList, ArrayList<Integer> historyRoadList);

        void updateCleanTime(String value);

        void updateCleanArea(String value);

        void updateStatue(String value);

        void updateStartStatue(boolean isSelect, String value);

        void updateTvVirtualStatue(boolean isSelect);

        void cleanMapView();

        void drawSlamMap(byte[] slamBytes);

        void drawObstacle();

        void setBatteryImage(int curStatus, int batteryNo);

        void updateQuanAnimation(boolean isStart);


        void updateAlongAnimation(boolean isStary);

        void hideVirtualEdit();
        void clearAll(int curStatus);
        void showVirtualEdit();
        void setMapViewVisible(boolean isViesible);
        void setTvUseStatus(int tag);
        void setAlongViewVisible(boolean isVisible);

        /**
         * 设置重点清扫布局的显示状态
         */
        void setPointViewVisible(boolean isVisible);
        void showBottomView();
        void  showMap();
        void updateOperationViewStatue(int surStatu);
        void setVirtualWallStatus(boolean isEnable);
        void showErrorPopup(int errorCode);
        void sendHandler(int msgCode);
        void drawVirtualWall(List<int []> existPointList);
    }

    interface Presenter {
        void initTimer();

        void getRealTimeMap();

        void getHistoryRoad();

        void queryVirtualWall();

        void subscribeRealTimeMap();

        void getAppointmentMsg();

        void getDevStatus();

        void setStatus(int curStatus, int batteryNo, int mopForce, boolean isMaxMode, boolean voiceOpen);
        boolean canEdit(int curStatus);
        boolean isWork(int curStatus);
        void initPropReceiver();
        void registerPropReceiver();
        void sendToDeviceWithOption(ACDeviceMsg msg);
        void sendToDeviceWithOption_start(ACDeviceMsg msg);
        void enterVirtualMode();
        void sendVirtualWallData(final List<int[]> list, final int tag);
        void sendToDeviceWithOptionVirtualWall(ACDeviceMsg acDeviceMsg, String physicalDeviceId, final int tag);
        int getCurStatus();
        /**
         * 进入沿边模式
         */
        void enterAlongMode();

        /**
         * 进入重点模式
         */
        void enterPointMode();
        void enterRechargeMode();

    }

}
