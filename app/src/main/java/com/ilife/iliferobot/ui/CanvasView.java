package com.ilife.iliferobot.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.util.Base64;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACClassDataMgr;
import com.accloud.service.ACDeviceMsg;
import com.accloud.service.ACException;
import com.accloud.service.ACMsg;
import com.accloud.service.ACObject;
import com.google.gson.Gson;

import com.ilife.iliferobot.entity.RealTimeMapInfo;
import com.ilife.iliferobot.utils.Constants;
import com.ilife.iliferobot.utils.DataUtils;
import com.ilife.iliferobot.utils.DeviceUtils;
import com.ilife.iliferobot.utils.MsgCodeUtils;
import com.ilife.iliferobot.utils.MyLog;
import com.ilife.iliferobot.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenjiaping on 2017/7/7.
 */

public class CanvasView extends View implements View.OnTouchListener {
    private final String TAG = CanvasView.class.getSimpleName();
    public static final String INTENT_ACTION = "com.example.CanvasView";
    public final String UNDER_LINE = "_";
    public static int space = 10;
    private Context context;
    private long deviceId;

    private GestureDetector gd;
    private ArrayList<Integer> pointList;
    private ArrayList<String> pointStrList;
    private Paint paint;
    private double nLenStart;
    private boolean isIgnore;
    private int scrollingOffsetX;
    private int scrollingOffsetY;
    private int width;
    private int height;
    private int nCnt;

    private int workTime;
    private int cleanArea;
    private String physicalDeviceId;
    private String subdomain;

    private Intent intent;

    private int clickCount = 0;
    private long firstClickTime;
    private String serviceName;

    public CanvasView(Context context, long deviceId, String physicalDeviceId, String subdomain) {
        super(context);
        this.context = context;
        this.deviceId = deviceId;
        this.physicalDeviceId = physicalDeviceId;
        this.subdomain = subdomain;
        init();
        setOnTouchListener(this);
    }

    private void init() {
        pointList = new ArrayList<>();
        pointStrList = new ArrayList<>();
        paint = new Paint();
        intent = new Intent();
        intent.setAction(INTENT_ACTION);
        gd = new GestureDetector(context, new InnerGestureListener());

        subscribeRealTimeMap();
        getRealTimeMap();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (width == 0 || height == 0) {
            width = getMeasuredWidth();
            height = getMeasuredHeight();
        }
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        paint.setColor(Color.parseColor("#ffffff"));
        canvas.drawPaint(paint);
        canvas.translate(width / 2 + scrollingOffsetX, height / 2 + scrollingOffsetY);

        int vertz = 0;
        int vertz1 = 0;

        //绘制网格背景
        paint.setColor(Color.parseColor("#eeeeef"));
        for (int i = 0; i < 100; i++) {
            if (i % 10 == 0) {
                paint.setStrokeWidth(2);
            } else {
                paint.setStrokeWidth(1);
            }
            canvas.drawLine(-width / 2 - scrollingOffsetX, vertz, width / 2 - scrollingOffsetX, vertz, paint);
            canvas.drawLine(vertz, -height / 2 - scrollingOffsetY, vertz, height / 2 - scrollingOffsetY, paint);
            vertz += space;

            canvas.drawLine(-width / 2 - scrollingOffsetX, vertz1, width / 2 - scrollingOffsetX, vertz1, paint);
            canvas.drawLine(vertz1, -height / 2 - scrollingOffsetY, vertz1, height / 2 - scrollingOffsetY, paint);
            vertz1 += -space;
        }

        //绘制清扫区域的黄方格
        paint.setColor(context.getResources().getColor(R.color.color_f08300));
        paint.setStrokeWidth((float) (space - 2));

        if (pointList.size() > 0) {
            for (int i = 1; i < pointList.size(); i += 2) {
                long x = -pointList.get(i - 1);
                long y = -pointList.get(i);
                canvas.drawPoint((float) (x * space), -(float) (y * space), paint);
            }

            //绘制机器位置点
            int size = pointList.size();
            long finalX = -Long.valueOf(pointList.get(size - 2));
            long finalY = Long.valueOf(pointList.get(size - 1));
            paint.setColor(context.getResources().getColor(R.color.color_f08300));
            canvas.drawCircle((finalX * space), (finalY * space), space, paint);
        }
    }


    class InnerGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { //这里是控制单点移动的
            if (isIgnore || nCnt == 2) {
                isIgnore = false;
            } else {
                scrollingOffsetY += -distanceY;
                scrollingOffsetX += -distanceX;
                invalidate();
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float velocityX1 = 0;
            float velocityY1 = 0;
            return super.onFling(e1, e2, velocityX1, velocityY1);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        nCnt = event.getPointerCount();
        int n = event.getAction();
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN && 2 == nCnt) {
            int xLen = Math.abs((int) event.getX(0) - (int) event.getX(1));
            int yLen = Math.abs((int) event.getY(0) - (int) event.getY(1));
            nLenStart = Math.sqrt((double) xLen * xLen + (double) yLen * yLen);
        } else if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP && 2 == nCnt) {
            int xLen = Math.abs((int) event.getX(0) - (int) event.getX(1));
            int yLen = Math.abs((int) event.getY(0) - (int) event.getY(1));
            double nLenEnd = Math.sqrt((double) xLen * xLen + (double) yLen * yLen);
            if (nLenEnd > nLenStart) {
                float scale = Float.valueOf(nLenEnd / nLenStart + "");
                space *= scale;
                if (space >= 20) {
                    space = 20;
                }
                invalidate();
            } else {
                float scale = Float.valueOf(nLenEnd / nLenStart + "");
                space *= scale;
                if (space <= 5) {
                    space = 7;
                }
                invalidate();
            }
            isIgnore = true;
        }
        if (nCnt == 2) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //==========================================
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (clickCount == 0) {
                firstClickTime = System.currentTimeMillis();
                clickCount = 1;
            } else {
                long secondClickTime = System.currentTimeMillis();
                //视为连续单击
                if (secondClickTime - firstClickTime <= 500) {
                    scrollingOffsetX = 0;
                    scrollingOffsetY = 0;
                    invalidate();
                    clickCount = 0;
                } else {
                    firstClickTime = secondClickTime;
                    clickCount = 1;
                }
            }
        }
        //==========================================
        gd.setIsLongpressEnabled(true);
        return gd.onTouchEvent(event);
    }

    //onWindowFocusChanged先于onDraw执行
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
//            width = this.getWidth();
//            height = this.getHeight();
            invalidate();
//            subscribeRealTimeMap();
//            getRealTimeMap();
            upLoadMsg((byte) 0x01);
        } else {
            upLoadMsg((byte) 0x00);
        }

    }

    /**
     *  下发上传请求实时信息指令
     * @param b
     */
    public void upLoadMsg(byte b) {
        ACDeviceMsg deviceMsg = new ACDeviceMsg(MsgCodeUtils.UPLOADMSG, new byte[]{b});
        sendToDeviceWithOption(deviceMsg, physicalDeviceId);
    }

    public void sendToDeviceWithOption(final ACDeviceMsg deviceMsg, String physicalDeviceId) {
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalDeviceId, deviceMsg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void success(ACDeviceMsg acDeviceMsg) {

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
                                Gson gson = new Gson();
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
                                            MyLog.e(TAG, "subscribeRealTimeMap===== (x==0x7fff)&(y==0x7fff) 地图被清掉了");
                                            pointList.clear();
                                            pointStrList.clear();
                                            invalidate();
                                            workTime = 0;
                                            cleanArea = 0;
                                        } else {
                                            if (!pointStrList.contains(x + UNDER_LINE + y)) {
                                                pointList.add(x);
                                                pointList.add(y);
                                                pointStrList.add(x + UNDER_LINE + y);
                                            }
                                        }
                                    }
                                    invalidate();
                                    intent.putExtra("workTime", workTime);
                                    intent.putExtra("cleanArea", cleanArea);
                                    context.sendBroadcast(intent);
                                }
                            }
                        });
                    }

                    @Override
                    public void error(ACException e) {
                        MyLog.e(TAG, "subscribeRealTimeMap e " + e.toString());
                    }
                }
        );
    }

    private void getRealTimeMap() {
        final ACMsg req = new ACMsg();
        req.setName("searchCleanRealTime");
        req.put("device_id", deviceId);
        serviceName = DeviceUtils.getServiceName(subdomain);
        AC.sendToService("", serviceName, Constants.SERVICE_VERSION, req, new PayloadCallback<ACMsg>() {
            @Override
            public void success(ACMsg resp) {
                ArrayList<ACObject> data = resp.get("data");
                if (data == null || data.size() == 0) {
                    return;
                }
                for (int i = 0; i < data.size(); i++) {
                    byte[] bytes = Base64.decode(data.get(i).getString("clean_data"), Base64.DEFAULT);
                    if (bytes == null || bytes.length < 4 || (bytes.length % 4) != 0) {
                        return;
                    }
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
                            MyLog.e(TAG, "getRealTimeMap (x==0x7fff)&(y==0x7fff) 地图被清掉了");
                            pointList.clear();
                            pointStrList.clear();
                            invalidate();
                            workTime = 0;
                            cleanArea = 0;
                        } else {
                            if (!pointStrList.contains(x + UNDER_LINE + y)) {
                                pointList.add(x);
                                pointList.add(y);
                                pointStrList.add(x + UNDER_LINE + y);
                            }
                        }
                    }
                }
                invalidate();
                intent.putExtra("workTime", workTime);
                intent.putExtra("cleanArea", cleanArea);
                context.sendBroadcast(intent);
            }

            @Override
            public void error(ACException e) {
                MyLog.e(TAG, "getRealTimeMap e " + e.toString());
            }
        });
    }
}

