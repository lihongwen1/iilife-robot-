package com.ilife.iliferobot.utils;

/**
 * Created by chenjiaping on 2017/8/3.
 */

public class MsgCodeUtils {
    //下发
    public static final int WorkMode = 0x46;
    public static final int RoomMode = 0x47;
    public static final int CleanForce = 0x48;
    public static final int Proceed = 0x49;
    public static final int RestLifeTime = 0x4B;
    public static final int NoDisturbing = 0x4E;
    public static final int FactoryReset = 0x4F;
    public static final int SetVirtualWall = 0x53;//下发虚拟墙
    public static final int CheckMachineInfo = 0x54;//获取主机固件信息
    public static final int DeviceUpdate = 0x55;//下发升级
    //下发预约指令
    public static final int Appointment = 0x4A;
    //下发上传实时信息指令
    public static final int UPLOADMSG = 0x4D;
    public static final int AdjustTime = 0X4C;

    //查询
    public static final int DevStatus = 0x41;
    public static final int MatConditions = 0x44;
    public static final int ClockInfos = 0x42;
    public static final int HistoryRecord = 0x43;
    public static final int QueryVirtualWall = 0x45;//查询虚拟墙

    //模式
    public static final int STATUE_POINT=0X05;
    public static final int STATUE_ALONG=0X04;
    public static final int STATUE_SLEEPING=0X01;
    public static final int STATUE_WAIT=0X02;
    public static final int STATUE_PLANNING=0X06;
    public static final int STATUE_VIRTUAL_EDIT=0X07;
    public static final int STATUE_RECHARGE=0X08;
    public static final int STATUE_CHARGING =0X09;
    public static final int STATUE_REMOTE_CONTROL =0X0A;
    public static final int STATUE_CHARGING_ =0X0B;
    public static final int STATUE_PAUSE =0X0C;
    //遥控器
    public static final int PROCEED_NO_RESPONSE =0X00;
    public static final int PROCEED_FORWARD =0X01;
    public static final int PROCEED_BACK =0X02;
    public static final int PROCEED_LEFT =0X03;
    public static final int PROCEED_RIGHT =0X04;
    public static final int PROCEED_PAUSE =0X05;
    //清洁
    public static final int CLEANNING_CLEANING_NORMAL =0X00;
    public static final int CLEANNING_CLEANING_MAX =0X01;




}
