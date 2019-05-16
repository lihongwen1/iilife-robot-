package com.ilife.iliferobot_cn.utils;

import com.accloud.service.ACDeviceMsg;

public class ACSkills {
    private ACDeviceMsg mAcDevMsg;
    private static ACSkills acSkills;

    private ACSkills() {
        mAcDevMsg = new ACDeviceMsg();
    }

    public static synchronized ACSkills get() {
        if (acSkills == null) {
            synchronized (ACSkills.class) {
                if (acSkills == null) {
                    acSkills = new ACSkills();
                }
            }
        }
        return acSkills;
    }

    public ACDeviceMsg queryVirtual() {
        mAcDevMsg.setCode(MsgCodeUtils.QueryVirtualWall);
        mAcDevMsg.setContent(new byte[]{0x00});
        return mAcDevMsg;
    }

    public ACDeviceMsg enterVirtualMode() {
        mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
        mAcDevMsg.setContent(new byte[]{MsgCodeUtils.STATUE_VIRTUAL_EDIT});
        return mAcDevMsg;
    }

    public ACDeviceMsg setVirtualWall(byte[] virtualContentBytes) {
        mAcDevMsg.setCode(MsgCodeUtils.SetVirtualWall);
        mAcDevMsg.setContent(virtualContentBytes);
        return mAcDevMsg;
    }

    public ACDeviceMsg enterPointMode() {
        mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
        mAcDevMsg.setContent(new byte[]{MsgCodeUtils.STATUE_POINT});
        return mAcDevMsg;
    }

    public ACDeviceMsg enterAlongMode() {
        mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
        mAcDevMsg.setContent(new byte[]{MsgCodeUtils.STATUE_ALONG});
        return mAcDevMsg;
    }

    public ACDeviceMsg enterWaitMode() {
        mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
        mAcDevMsg.setContent(new byte[]{MsgCodeUtils.STATUE_WAIT});
        return mAcDevMsg;
    }

    public ACDeviceMsg enterPlanningMode() {
        mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
        mAcDevMsg.setContent(new byte[]{MsgCodeUtils.STATUE_PLANNING});
        return mAcDevMsg;
    }

    public ACDeviceMsg enterRechargeMode() {
        mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
        mAcDevMsg.setContent(new byte[]{MsgCodeUtils.STATUE_RECHARGE});
        return mAcDevMsg;
    }

    public ACDeviceMsg turnLeft() {
        mAcDevMsg.setCode(MsgCodeUtils.Proceed);
        mAcDevMsg.setContent(new byte[]{MsgCodeUtils.PROCEED_LEFT});
        return mAcDevMsg;
    }

    public ACDeviceMsg turnRight() {
        mAcDevMsg.setCode(MsgCodeUtils.Proceed);
        mAcDevMsg.setContent(new byte[]{MsgCodeUtils.PROCEED_RIGHT});
        return mAcDevMsg;
    }

    public ACDeviceMsg turnNoResponse() {
        mAcDevMsg.setCode(MsgCodeUtils.Proceed);
        mAcDevMsg.setContent(new byte[]{MsgCodeUtils.PROCEED_PAUSE});
        return mAcDevMsg;
    }

    public ACDeviceMsg turnForward() {
        mAcDevMsg.setCode(MsgCodeUtils.Proceed);
        mAcDevMsg.setContent(new byte[]{MsgCodeUtils.PROCEED_FORWARD});
        return mAcDevMsg;
    }

    public ACDeviceMsg normalSuction() {
        mAcDevMsg.setCode(MsgCodeUtils.Proceed);
        mAcDevMsg.setContent(new byte[]{MsgCodeUtils.PROCEED_FORWARD});
        return mAcDevMsg;
    }

    public ACDeviceMsg cleaningNormal(int mopForce) {
        mAcDevMsg.setCode(MsgCodeUtils.CleanForce);
        mAcDevMsg.setContent(new byte[]{MsgCodeUtils.CLEANNING_CLEANING_NORMAL, (byte) mopForce});
        return mAcDevMsg;
    }

    public ACDeviceMsg cleaningMax(int mopForce) {
        mAcDevMsg.setCode(MsgCodeUtils.CleanForce);
        mAcDevMsg.setContent(new byte[]{MsgCodeUtils.CLEANNING_CLEANING_MAX, (byte) mopForce});
        return mAcDevMsg;
    }

}
