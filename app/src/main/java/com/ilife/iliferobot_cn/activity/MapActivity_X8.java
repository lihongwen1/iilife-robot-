package com.ilife.iliferobot_cn.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

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
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.entity.PropertyInfo;
import com.ilife.iliferobot_cn.entity.RealTimeMapInfo;
import com.ilife.iliferobot_cn.ui.ControlPopupWindow;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.DataUtils;
import com.ilife.iliferobot_cn.utils.DeviceUtils;
import com.ilife.iliferobot_cn.utils.MsgCodeUtils;
import com.ilife.iliferobot_cn.utils.MyLog;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chengjiaping on 2018/8/15.
 */

public class MapActivity_X8 extends BaseActivity implements View.OnClickListener {
    final String TAG = MapActivity_X8.class.getSimpleName();
    public static final String INTENT_ACTION = "com.example.MapActivity";
    public static final String KEY_IS_MAX = "isMaxMode";
    public static final String KEY_MOP_FORCE = "mopForce";
    final int NONE = 1;
    final int DRAG = 2;
    final int ZOOM = 3;
    int mode = 1;
    float SPACE = 8;
    float curSpace = 8;
    final int TAG_CONTROL = 0x01;
    final int TAG_NORMAL = 0x02;
    final int TAG_RECHAGRGE = 0x03;
    final int TAG_KEYPOINT = 0x04;
    final int TAG_ALONG = 0x05;
    final int EDGE_TAG = 0x06;
    final String UNDER_LINE = "_";
    Gson gson;
    Paint paint;
    Bitmap bitmap;
    Canvas canvas;
    Context context;
    View anchorView;
    float oriDis = 1f;
    long deviceId;
    int mopForce;
    byte sendByte;
    boolean isWork, hasAppoint, isMaxMode, hasStart, isSend;
    String physicalId, subdomain, robotType;
    int curStatus, errorCode, batteryNo, workTime, cleanArea, imageWidth, imageHeight;
    TextView tv_time, tv_area, tv_title, tv_start, tv_status, tv_use_control, tv_point, tv_recharge, tv_along, tv_clock;
    ImageView image_map, image_ele, image_back, image_start,
            image_control, image_setting, image_function, image_animation,
            image_clock, image_point, image_recharge, image_key_point, image_along, image_edge;
    Animation animation;
    ImageView image_quan;
    View layout_recharge, layout_key_point, layout_along;
    ACDeviceMsg mAcDevMsg;
    PopupWindow errorPopup, menuPopup, controlPopup;
    AnimationDrawable drawable;
    ArrayList<Integer> pointList;
    ArrayList<String> pointStrList;
    ACDeviceDataMgr.PropertyReceiver propertyReceiver;
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    PointF startPoint = new PointF();
    PointF midPoint = new PointF();
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case EDGE_TAG:
                    if (curStatus == 0x04) {
                        if (image_edge.getVisibility() == View.VISIBLE) {
                            image_edge.setVisibility(View.GONE);
                        } else {
                            image_edge.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_new);
        initView();
        initData();
        subscribeRealTimeMap();
        getRealTimeMap();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getClockInfo();
        getDevStatus();
        initPropReceiver();
        registerPropReceiver();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && imageWidth == 0) {
            imageWidth = image_map.getWidth();
            imageHeight = image_map.getHeight();
            matrix.postTranslate(imageWidth / 2 - 1000, imageHeight / 2 - 1000);
//            matrix.postScale(6,6,imageWidth/2,imageHeight/2);
            image_map.setImageMatrix(matrix);
            setAndSaveBitmap();
        }
    }

    private void initView() {
        context = this;
        errorPopup = new PopupWindow();
        anchorView = findViewById(R.id.rl_status);
        tv_time = (TextView) findViewById(R.id.tv_time);
        tv_area = (TextView) findViewById(R.id.tv_area);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_start = (TextView) findViewById(R.id.tv_start);
        tv_status = (TextView) findViewById(R.id.tv_status);
        image_map = (ImageView) findViewById(R.id.image_map);
        image_ele = (ImageView) findViewById(R.id.image_ele);
        image_ele.setImageResource(R.drawable.map_aty_battery4);
        image_back = (ImageView) findViewById(R.id.image_back);
        image_start = (ImageView) findViewById(R.id.image_start);
        image_control = (ImageView) findViewById(R.id.image_control);
        image_setting = (ImageView) findViewById(R.id.image_setting);
        tv_use_control = (TextView) findViewById(R.id.tv_use_control);
        image_edge = (ImageView) findViewById(R.id.image_edge);
        image_function = (ImageView) findViewById(R.id.image_function);
        image_animation = (ImageView) findViewById(R.id.image_animation);
        image_key_point = (ImageView) findViewById(R.id.image_key_point);
        image_quan = (ImageView) findViewById(R.id.image_quan);
        layout_along = findViewById(R.id.layout_along);
        layout_recharge = findViewById(R.id.layout_recharge);
        layout_key_point = findViewById(R.id.layout_key_point);
        drawable = (AnimationDrawable) image_animation.getBackground();
        image_back.setOnClickListener(this);
        image_start.setOnClickListener(this);
        image_control.setOnClickListener(this);
        image_setting.setOnClickListener(this);
        image_function.setOnClickListener(this);
        image_map.setOnTouchListener(new MyTouchListener());
    }

    public void initData() {
        gson = new Gson();
        mAcDevMsg = new ACDeviceMsg();
        pointList = new ArrayList<>();
        pointStrList = new ArrayList<>();
        animation = AnimationUtils.loadAnimation(context, R.anim.anims_ni);
        animation.setInterpolator(new LinearInterpolator());
        deviceId = SpUtils.getLong(context, MainActivity.KEY_DEVICEID);
        subdomain = SpUtils.getSpString(context, MainActivity.KEY_SUBDOMAIN);
        physicalId = SpUtils.getSpString(context, MainActivity.KEY_PHYCIALID);
        if (subdomain.equals(Constants.subdomain_x800)) {
            robotType = "X800";
        } else if (subdomain.equals(Constants.subdomain_x787)) {
            robotType = "X787";
        } else {
            robotType = "X785";
        }
        tv_title.setText(getString(R.string.map_aty_title, robotType));
        bitmap = Bitmap.createBitmap(2000, 2000, Bitmap.Config.ARGB_8888).copy(Bitmap.Config.ARGB_8888, true);
        canvas = new Canvas(bitmap);
        paint = new Paint();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        handler.sendEmptyMessage(EDGE_TAG);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void initPropReceiver() {
        propertyReceiver = new ACDeviceDataMgr.PropertyReceiver() {
            @Override
            public void onPropertyReceive(String s, long l, String s1) {
                MyLog.e(TAG, "onPropertyReceive ==== " + s1);
                if (!isDestroyed()) {
                    PropertyInfo info = gson.fromJson(s1, PropertyInfo.class);
                    errorCode = info.getError_info();
                    batteryNo = info.getBattery_level();
                    curStatus = info.getWork_pattern();
                    isMaxMode = info.getVacuum_cleaning() == 0x01;
                    mopForce = info.getCleaning_cleaning();
                    setStatus(curStatus, batteryNo, mopForce, isMaxMode);
                    setTimeAndArea(curStatus);
                    if (errorCode != 0) {
                        if (errorPopup != null && !errorPopup.isShowing()) {
                            showErrorPopup(errorCode);
                        }
                    } else {
                        if (errorPopup != null && errorPopup.isShowing()) {
                            errorPopup.dismiss();
                        }
                    }
                }
            }
        };
    }

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

    public void getRealTimeMap() {
        final ACMsg req = new ACMsg();
        req.setName("searchCleanRealTime");
        req.put("device_id", deviceId);
        String serviceName = DeviceUtils.getServiceName(subdomain);
        AC.sendToService("", serviceName, Constants.SERVICE_VERSION, req, new PayloadCallback<ACMsg>() {
            @Override
            public void success(ACMsg resp) {
                ArrayList<ACObject> data = resp.get("data");
                if (data != null && data.size() > 0) {
                    for (int i = 0; i < data.size(); i++) {
                        byte[] bytes = Base64.decode(data.get(i).getString("clean_data"), Base64.DEFAULT);
                        if (bytes != null && bytes.length >= 4 && (bytes.length % 4) == 0) {
                            if (i == data.size() - 1) {
                                byte[] byte_area = new byte[]{bytes[0], bytes[1]};
                                byte[] byte_time = new byte[]{bytes[2], bytes[3]};
                                workTime = DataUtils.bytesToInt2(byte_time, 0);
                                cleanArea = DataUtils.bytesToInt2(byte_area, 0);
                            }
                            for (int j = 7; j < bytes.length; j += 4) {
                                int x = DataUtils.bytesToInt(new byte[]{bytes[j - 3], bytes[j - 2]}, 0);
                                int y = DataUtils.bytesToInt(new byte[]{bytes[j - 1], bytes[j]}, 0);
                                if ((x == 0x7fff) & (y == 0x7fff)) {
                                    pointList.clear();
                                    pointStrList.clear();
                                    workTime = 0;
                                    cleanArea = 0;
                                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                                    setAndSaveBitmap();
                                } else {
                                    if (!pointStrList.contains(x + UNDER_LINE + y)) {
                                        pointList.add(x);
                                        pointList.add(y);
                                        pointStrList.add(x + UNDER_LINE + y);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void error(ACException e) {

            }
        });
    }

    private void subscribeRealTimeMap() {
        Map<String, Object> primaryKey = new HashMap<>();
        primaryKey.put("device_id", deviceId);
        AC.classDataMgr().subscribe("clean_realtime", primaryKey, ACClassDataMgr.OPTYPE_ALL,
                new VoidCallback() {
                    @Override
                    public void success() {
                        AC.classDataMgr().registerDataReceiver(new ACClassDataMgr.ClassDataReceiver() {
                            @Override
                            public void onReceive(String s, int i, String s1) {
                                RealTimeMapInfo mapInfo = gson.fromJson(s1, RealTimeMapInfo.class);
                                String clean_data = mapInfo.getClean_data();
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
                                            pointList.clear();
                                            pointStrList.clear();
                                            workTime = 0;
                                            cleanArea = 0;
                                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                                            setAndSaveBitmap();
                                        } else {
                                            if (!pointStrList.contains(x + UNDER_LINE + y)) {
                                                pointList.add(x);
                                                pointList.add(y);
                                                pointStrList.add(x + UNDER_LINE + y);
                                            }
                                        }
                                    }
                                    setTimeAndArea(curStatus);
                                }
                            }
                        });
                    }

                    @Override
                    public void error(ACException e) {
                        MyLog.e(TAG, "subscribeRealTimeMap error " + e.toString());
                    }
                }
        );
    }

    public void setTimeAndArea(int curStatus) {
        if (curStatus != 0x06) {
            tv_time.setText(getString(R.string.map_aty_gang));
            tv_area.setText(getString(R.string.map_aty_gang));
        } else {
            tv_area.setText(cleanArea / 100.0 + "㎡");
            tv_time.setText(workTime / 60 + "min");
            drawMap();
        }
    }

    public void setAndSaveBitmap() {
        canvas.save();
        image_map.setImageBitmap(bitmap);
    }

    public void drawMap() {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        paint.setColor(getResources().getColor(R.color.color_f08300));
        paint.setStrokeWidth(SPACE - 2);
        if (pointList.size() > 0) {
            for (int i = 1; i < pointList.size(); i += 2) {
                long x = -pointList.get(i - 1);
                long y = -pointList.get(i);
//                canvas.drawPoint((float) (x*space),-(float) (y*space),paint);
                MyLog.e(TAG, "drawMap  x = " + x + " y = " + y);
                canvas.drawPoint(x * SPACE + 1000, (-y) * SPACE + 1000, paint);
            }
            int size = pointList.size();
            long finalX = -Long.valueOf(pointList.get(size - 2));
            long finalY = Long.valueOf(pointList.get(size - 1));
//            paint.setColor(Color.parseColor("#63B8FF"));
//            canvas.drawCircle((finalX*space),(finalY*space),space,paint);
            paint.setColor(getResources().getColor(R.color.color_f39800));
            paint.setStrokeWidth(1);
            canvas.drawCircle(finalX * SPACE + 1000, finalY * SPACE + 1000, 6, paint);
        }
        setAndSaveBitmap();
    }

    public void showErrorPopup(int errorCode) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.layout_popup_error, null);
        errorPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        errorPopup.setContentView(contentView);
        initErrorPopup(errorCode, contentView);
        errorPopup.setOutsideTouchable(false);
        errorPopup.setFocusable(false);
        errorPopup.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        errorPopup.setHeight((int) getResources().getDimension(R.dimen.dp_60));
        errorPopup.showAsDropDown(anchorView);
    }

    private void initErrorPopup(int code, View contentView) {
        ImageView image_delete = (ImageView) contentView.findViewById(R.id.image_delete);
        TextView tv_error = (TextView) contentView.findViewById(R.id.tv_error);
        tv_error.setText(DeviceUtils.getErrorText(context, code));
        image_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyLog.e(TAG, "onClick ===");
                if (errorPopup != null) {
                    errorPopup.dismiss();
                }
            }
        });
    }

    public void showMenuPopup() {
        if (menuPopup == null) {
            View contentView = LayoutInflater.from(context).inflate(R.layout.layout_popup_menu, null);
            menuPopup = new PopupWindow(context);
            menuPopup.setBackgroundDrawable(new ColorDrawable());
            menuPopup.setContentView(contentView);
            initMenuView(contentView);
            menuPopup.setOutsideTouchable(true);
            menuPopup.setFocusable(true);
            menuPopup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
            menuPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        }
        if (!menuPopup.isShowing()) {
            menuPopup.showAtLocation(anchorView, Gravity.BOTTOM | Gravity.END,
                    (int) getResources().getDimension(R.dimen.dp_27),
                    (int) getResources().getDimension(R.dimen.dp_100));
        }
        image_point.setSelected(curStatus == 0x05);
        tv_point.setSelected(curStatus == 0x05);
        image_recharge.setSelected(curStatus == 0x08);
        tv_recharge.setSelected(curStatus == 0x08);
        image_along.setSelected(curStatus == 0x04);
        tv_along.setSelected(curStatus == 0x04);
        image_clock.setSelected(hasAppoint);
        tv_clock.setSelected(hasAppoint);
    }

    public void initMenuView(View contentView) {
        tv_clock = (TextView) contentView.findViewById(R.id.tv_clock);
        tv_along = (TextView) contentView.findViewById(R.id.tv_along);
        tv_point = (TextView) contentView.findViewById(R.id.tv_point);
        tv_recharge = (TextView) contentView.findViewById(R.id.tv_recharge);
        image_clock = (ImageView) contentView.findViewById(R.id.image_clock);
        image_along = (ImageView) contentView.findViewById(R.id.image_along);
        image_point = (ImageView) contentView.findViewById(R.id.image_point);
        image_recharge = (ImageView) contentView.findViewById(R.id.image_recharge);
        image_clock.setOnClickListener(this);
        image_along.setOnClickListener(this);
        image_point.setOnClickListener(this);
        image_recharge.setOnClickListener(this);
    }

    public void showControlPopup() {
        if (controlPopup == null) {
            controlPopup = new ControlPopupWindow(context, physicalId, subdomain);
            controlPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    setTextStatus(TAG_NORMAL);
                }
            });
        }
        if (!controlPopup.isShowing()) {
            controlPopup.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
            setTextStatus(TAG_CONTROL);
        }
    }

    public void setTextStatus(int tag) {
        switch (tag) {
            case TAG_NORMAL:
                tv_use_control.setVisibility(View.GONE);
                break;
            case TAG_CONTROL:
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(getString(R.string.map_aty_use_control));
                break;
            case TAG_RECHAGRGE:
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(getString(R.string.map_aty_use_recharging, robotType));
                break;
            case TAG_KEYPOINT:
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(getString(R.string.map_aty_key_pointing, robotType));
                break;
            case TAG_ALONG:
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(getString(R.string.map_aty_use_along));
                break;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if ((errorPopup != null && errorPopup.isShowing()) ||
                (menuPopup != null && menuPopup.isShowing()) ||
                (controlPopup != null && controlPopup.isShowing())) {
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }

    public void getClockInfo() {
        ACDeviceMsg msg_clockInfo = new ACDeviceMsg(MsgCodeUtils.ClockInfos, new byte[]{0x00});
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, msg_clockInfo,
                Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
                    @Override
                    public void success(ACDeviceMsg deviceMsg) {
                        if (!isDestroyed()) {
                            byte[] resp = deviceMsg.getContent();
                            if (resp != null && resp.length == 50) {
                                hasAppoint = hasAppoint(resp);
                            }
                        }
                    }

                    @Override
                    public void error(ACException e) {
                        hasAppoint = false;
                    }
                });
    }

    public void getDevStatus() {
        ACDeviceMsg msg_devStatus = new ACDeviceMsg(MsgCodeUtils.DevStatus, new byte[]{0x00});
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, msg_devStatus,
                Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
                    @Override
                    public void success(ACDeviceMsg deviceMsg) {
                        byte[] bytes = deviceMsg.getContent();
                        if (bytes != null) {
                            errorCode = bytes[8];
                            curStatus = bytes[0];
                            batteryNo = bytes[5];
                            mopForce = bytes[4];
                            isMaxMode = bytes[3] == 0x01;
                            setStatus(curStatus, batteryNo, mopForce, isMaxMode);
                            setTimeAndArea(curStatus);
                            if (errorCode != 0) {
                                if (errorPopup != null && !errorPopup.isShowing()) {
                                    showErrorPopup(errorCode);
                                }
                            } else {
                                if (errorPopup != null && errorPopup.isShowing()) {
                                    errorPopup.dismiss();
                                }
                            }
                        }
                    }

                    @Override
                    public void error(ACException e) {

                    }
                });
    }

    public void setStatus(int curStatus, int batteryNo, int mopForce, boolean isMaxMode) {
        isWork = isWork(curStatus);
        tv_status.setText(DeviceUtils.getStatusStr(context, curStatus, errorCode));
        image_start.setSelected(isWork);
        tv_start.setText(isWork ? getString(R.string.map_aty_stop) : getString(R.string.map_aty_start));
        if (batteryNo != -1) {
            setBatteryImage(curStatus, batteryNo);
            SpUtils.saveBoolean(context, physicalId + KEY_IS_MAX, isMaxMode);
            SpUtils.saveInt(context, physicalId + KEY_MOP_FORCE, mopForce);
        }
        clearAll(curStatus);
        if (curStatus == 0x8) { //回充
            layout_recharge.setVisibility(View.VISIBLE);
            drawable.start();
            setTextStatus(TAG_RECHAGRGE);
            image_quan.clearAnimation();
            hasStart = false;
        } else if (curStatus == 0x05) { //重点
            layout_key_point.setVisibility(View.VISIBLE);
            if (!hasStart) {
                image_quan.startAnimation(animation);
                hasStart = true;
            }
            setTextStatus(TAG_KEYPOINT);
        } else if (curStatus == 0x0A) { //遥控
            setTextStatus(TAG_CONTROL);
        } else if (curStatus == 0x02 || //待机暂停
                curStatus == 0x0C) {

        } else if (curStatus == 0x04) {
            layout_along.setVisibility(View.VISIBLE);
            setTextStatus(TAG_ALONG);
        } else if (curStatus == 0x06) {
            image_map.setVisibility(View.VISIBLE);
            setTextStatus(TAG_NORMAL);
            image_quan.clearAnimation();
            hasStart = false;
        }

        if (menuPopup != null && menuPopup.isShowing()) {
            image_point.setSelected(curStatus == 0x05);
            tv_point.setSelected(curStatus == 0x05);
            image_recharge.setSelected(curStatus == 0x08);
            tv_recharge.setSelected(curStatus == 0x08);
            image_along.setSelected(curStatus == 0x04);
            tv_along.setSelected(curStatus == 0x04);
        }
    }

    public void clearAll(int curStatus) {
        layout_recharge.setVisibility(View.GONE);
        layout_key_point.setVisibility(View.GONE);
        layout_along.setVisibility(View.GONE);
        image_map.setVisibility(View.GONE);
        tv_use_control.setVisibility(View.GONE);
    }

    public void setBatteryImage(int curStatus, int batteryNo) {
        if (curStatus == 0x09 || curStatus == 0x0b) {
            if (batteryNo <= 6) {
                image_ele.setImageResource(R.drawable.map_aty_battery1_ing);   //红色
            } else if (batteryNo < 35) {
                image_ele.setImageResource(R.drawable.map_aty_battery2_ing);   //一格
            } else if (batteryNo < 75) {
                image_ele.setImageResource(R.drawable.map_aty_battery3_ing);   //两格
            } else if (batteryNo >= 75) {
                image_ele.setImageResource(R.drawable.map_aty_battery4_ing);   //满格
            }
        } else {
            if (batteryNo <= 6) {
                image_ele.setImageResource(R.drawable.map_aty_battery1);   //红色
            } else if (batteryNo < 35) {
                image_ele.setImageResource(R.drawable.map_aty_battery2);   //一格
            } else if (batteryNo < 75) {
                image_ele.setImageResource(R.drawable.map_aty_battery3);   //两格
            } else if (batteryNo >= 75) {
                image_ele.setImageResource(R.drawable.map_aty_battery4);   //满格
            }
        }

    }

    public boolean hasAppoint(byte[] resp) {
        for (int j = 1; j <= 31; j += 5) {
            if (resp[j] != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_start: //done
                mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
                if (isWork(curStatus)) {
                    mAcDevMsg.setContent(new byte[]{0x02});
                } else {
                    mAcDevMsg.setContent(new byte[]{0x06});
                }
                sendByte = mAcDevMsg.getContent()[0];
                sendToDeviceWithOption_start(mAcDevMsg);
                break;
            case R.id.image_control:
                if (isWork(curStatus) || curStatus == 0x01) {
                    ToastUtils.showToast(context, getString(R.string.map_aty_can_not_execute));
                } else if (curStatus == 0x0B || curStatus == 0x09) {
                    ToastUtils.showToast(context, getString(R.string.map_aty_charge));
                } else {
                    showControlPopup();
                }
                break;
            case R.id.image_function:
                showMenuPopup();
                break;
            case R.id.image_back:
                finish();
                break;
            case R.id.image_setting:
                Intent i = new Intent(context, SettingActivity.class);
                startActivity(i);
                break;
            case R.id.image_clock:
                if (menuPopup != null && menuPopup.isShowing()) {
                    menuPopup.dismiss();
                }
                Intent i_clock = new Intent(context, ClockingActivity.class);
                startActivity(i_clock);
                break;
            case R.id.image_along:  //done
                mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
                if (curStatus == 0x02 || curStatus == 0x04) {
                    byte b = (byte) (curStatus == 0x04 ? 0x02 : 0x04);
                    mAcDevMsg.setContent(new byte[]{b});
                    sendToDeviceWithOption(mAcDevMsg);
                } else {
                    ToastUtils.showToast(context, getString(R.string.map_aty_can_not_execute));
                }
                break;
            case R.id.image_point:  //done
                if (curStatus == 0x09 || curStatus == 0x0B) {
                    ToastUtils.showToast(context, getString(R.string.map_aty_charge));
                } else if (curStatus == 0x08 || curStatus == 0x04) {
                    ToastUtils.showToast(context, getString(R.string.map_aty_can_not_execute));
                } else {
                    mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
                    if (curStatus == 0x05) {
                        mAcDevMsg.setContent(new byte[]{0x02});
                    } else {
                        mAcDevMsg.setContent(new byte[]{0x05});
                    }
                    sendToDeviceWithOption(mAcDevMsg);
                }
                break;
            case R.id.image_recharge:  //done
                if (curStatus == 0x09 || curStatus == 0x0B) {
                    ToastUtils.showToast(context, getString(R.string.map_aty_charge));
                    return;
                }
                if (curStatus == 0x05 || curStatus == 0x04) {
                    ToastUtils.showToast(context, getString(R.string.map_aty_can_not_execute));
                    return;
                }
                mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
                if (curStatus == 0x08) {
                    mAcDevMsg.setContent(new byte[]{0x02});
                } else {
                    mAcDevMsg.setContent(new byte[]{0x08});
                }
                sendToDeviceWithOption(mAcDevMsg);
                break;
        }
    }

    public boolean isWork(int curStatus) {
        if (curStatus == 0x03 || curStatus == 0x04 ||
                curStatus == 0x05 || curStatus == 0x06 ||
                curStatus == 0x08) {
            return true;
        }
        return false;
    }

    public void sendToDeviceWithOption(ACDeviceMsg msg) {
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, msg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void error(ACException e) {
                ToastUtils.showErrorToast(context, e.getErrorCode());
            }

            @Override
            public void success(ACDeviceMsg deviceMsg) {
                byte[] bytes = deviceMsg.getContent();
                curStatus = bytes[0];
                setStatus(curStatus, -1, mopForce, isMaxMode);
            }
        });
    }

    public void sendToDeviceWithOption_start(ACDeviceMsg msg) {
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, msg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void error(ACException e) {
                ToastUtils.showErrorToast(context, e.getErrorCode());
            }

            @Override
            public void success(ACDeviceMsg deviceMsg) {
                byte[] bytes = deviceMsg.getContent();
                curStatus = bytes[0];
                if (curStatus == sendByte) {
                    setStatus(curStatus, -1, mopForce, isMaxMode);
                } else {
                    if (curStatus == 0x0B) {
                        ToastUtils.showToast(context, getString(R.string.map_aty_charge));
                    }
                }
            }
        });
    }

    public class MyTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView view = (ImageView) v;
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    //单点触控
                    matrix.set(view.getImageMatrix());
                    savedMatrix.set(matrix);
                    startPoint.set(event.getX(), event.getY());
                    mode = DRAG;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    //多点触控
                    oriDis = distance(event);
                    if (oriDis > 10f) {
                        savedMatrix.set(matrix);
                        midPoint = midPoint(event);
                        mode = ZOOM;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    // 手指滑动事件
                    if (mode == DRAG) {
                        // 是一个手指拖动
                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - startPoint.x, event.getY()
                                - startPoint.y);
                        view.setImageMatrix(matrix);
                    } else if (mode == ZOOM) {
                        // 两个手指滑动
                        float newDist = distance(event);
                        if (newDist > 10f) {
                            matrix.set(savedMatrix);
                            float scale = newDist / oriDis;
                            MyLog.e(TAG, "scale = " + scale);
//                            lastSpace = curSpace;
                            curSpace = curSpace * scale;
                            MyLog.e(TAG, "curSpace = " + curSpace);
                            if (curSpace >= 0.5f && curSpace <= 16.0f) {
                                MyLog.e(TAG, "curSpace>=2.0f&&curSpace<=8.0f");
                                matrix.postScale(scale, scale, midPoint.x, midPoint.y);
                                view.setImageMatrix(matrix);
                            } else if (curSpace < 0.5f) {
                                MyLog.e(TAG, "curSpace>=2.0f&&curSpace<=8.0f no");
//                                curSpace = lastSpace;
                                curSpace = 0.5f;
                            } else {
                                curSpace = 16.0f;
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    // 手指放开事件
                    mode = NONE;
//                    curSpace = SPACE;
//                    if (SPACE>=8.0f){
//                        SPACE = 8.0f;
//                    } else if (SPACE<=2.0f){
//                        SPACE = 2.0f;
//                    }
                    break;
            }
//            view.setImageMatrix(matrix);
            return true;
        }
    }

    private PointF midPoint(MotionEvent event) {
        float x = (event.getX(0) + event.getX(1)) / 2;
        float y = (event.getY(0) + event.getY(1)) / 2;
        return new PointF(x, y);
    }

    /**
     * 计算两个手指间的距离
     *
     * @param event 触摸事件
     * @return 放回两个手指之间的距离
     */
    private float distance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);//两点间距离公式
    }

    @Override
    public void onBackPressed() {
        if (errorPopup != null && errorPopup.isShowing()) {
            errorPopup.dismiss();
        } else {
            super.onBackPressed();
        }
    }
}
