package com.ilife.iliferobot.contract;

import com.accloud.service.ACDeviceMsg;
import com.ilife.iliferobot.base.BaseView;
import com.ilife.iliferobot.entity.Coordinate;

import java.util.ArrayList;
import java.util.List;

public interface MapX9Contract {
    interface Model {

    }

    interface View extends BaseView {
        void setTestText(String text);
        void setDevName();

        void showRemoteView();

        void updateSlam(int xMin, int xMax, int yMin, int yMax);


        void updateCleanTime(String value);

        void updateCleanArea(String value);

        void updateStatue(String value);

        void updateStartStatue(boolean isSelect, String value);

        void cleanMapView();



        void setBatteryImage(int curStatus, int batteryNo);

        void hideVirtualEdit();

        void clearAll(int curStatus);

        void showVirtualEdit();

        void setMapViewVisible(boolean isViesible);

        void setTvUseStatus(int tag);

        void showBottomView();

        void updateOperationViewStatue(int surStatu);

        void showErrorPopup(int errorCode);

        void drawVirtualWall(List<int[]> existPointList);

        void updateAlong(boolean isAlong);

        void updatePoint(boolean isPoint);

        void updateRecharge(boolean isRecharge);

        void updateMaxButton(boolean isMaXMode);

        void setCurrentBottom(int bottom);

        void showVirtualWallTip();
        void drawMapX9(ArrayList<Integer> roadList, ArrayList<Integer> historyRoadList, byte[] slamBytes);
        void drawMapX8(ArrayList<Coordinate> dataList);
        boolean isActivityInteraction();
        void setUnconditionalRecreate(boolean recreate);
    }

    interface Presenter {
        void adjustTime();
        int getDevice_type();

        String getSubDomain();

        String getPhysicalId();

        String getRobotType();

        void intervalToObtainSlam();

        void getHistoryRoadX9();

        void queryVirtualWall();

        void subscribeRealTimeMap();


        void getDevStatus();

        void setStatus(int curStatus, int batteryNo, int mopForce, boolean isMaxMode, int voiceVolume);

        boolean isWork(int curStatus);

        void initPropReceiver();

        void registerPropReceiver();

        void sendToDeviceWithOption(ACDeviceMsg msg);

        void enterVirtualMode();

        void sendVirtualWallData(final List<int[]> list);

        void sendToDeviceWithOptionVirtualWall(ACDeviceMsg acDeviceMsg, String physicalDeviceId);

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

        boolean isMaxMode();

        void reverseMaxMode();

        boolean isRandomMode();

        boolean isLowPowerWorker();

        /**
         * 是否绘制map
         *
         * @return
         */
        boolean isDrawMap();

        void updateSlamX8(ArrayList<Coordinate> src, int offset);

        boolean isX900Series();

        boolean pointToAlong();

        boolean isLongPressControl();

        void prepareToReloadData();

        boolean isVirtualWallOpen();

        void getDeviceProperty();

        void handlePropertyData(String sData,boolean isFromFetch);
        void refreshStatus();

    }

}
