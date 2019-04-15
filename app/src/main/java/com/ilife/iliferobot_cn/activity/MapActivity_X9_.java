package com.ilife.iliferobot_cn.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.AlertDialog;

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
import android.widget.RelativeLayout;
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
import com.ilife.iliferobot_cn.utils.AlertDialogUtils;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.DataUtils;
import com.ilife.iliferobot_cn.utils.DeviceUtils;
import com.ilife.iliferobot_cn.utils.DialogUtils;
import com.ilife.iliferobot_cn.utils.DisplayUtil;
import com.ilife.iliferobot_cn.utils.MsgCodeUtils;
import com.ilife.iliferobot_cn.utils.MyLog;
import com.ilife.iliferobot_cn.utils.SpUtils;
import com.ilife.iliferobot_cn.utils.TimeUtil;
import com.ilife.iliferobot_cn.utils.ToastUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chengjiaping on 2018/8/15.
 */

public class MapActivity_X9_ extends BaseActivity implements View.OnClickListener {
    final String TAG = MapActivity_X9_.class.getSimpleName();
    public static final String INTENT_ACTION = "com.example.MapActivity";
    public static final String KEY_IS_MAX = "isMaxMode";
    public static final String KEY_MOP_FORCE = "mopForce";
    public static final String KEY_VOICE_OPEN = "voiceOpen";
    public static final int UPDATE_SLAM = 0x11;
    public static final int VIRTUALWALL_MAXCOUNT = 0x12;
    public static final int SET_CANVAS_ROAD_BITMAP = 0x14;
    public static final int SEND_VIRTUALDATA_SUCCESS = 0x15;
    public static final int SEND_VIRTUALDATA_FAILED = 0x16;
    public static final int QUERYVIRTUAL_SUCCESS_SHOWLINE = 0x17;
    public static final int START_AUTO_SCALE = 0x18;
    final int TAG_CONTROL = 0x01;
    final int TAG_NORMAL = 0x02;
    final int TAG_RECHAGRGE = 0x03;
    final int TAG_KEYPOINT = 0x04;
    final int TAG_ALONG = 0x05;
    static final int SEND_VIR = 1;
    static final int EXIT_VIR = 2;
    Gson gson;
    Context context;
    View anchorView;
    RelativeLayout relativeLayout;
    long deviceId;
    int mopForce;
    byte sendByte;
    boolean isWork, hasAppoint, isMaxMode, hasStart, hasStart_, voiceOpen, isX800;
    String physicalId, subdomain, robotType;
    int curStatus, errorCode, batteryNo, workTime, cleanArea;
    TextView tv_time, tv_area, tv_title, tv_start, tv_status, tv_use_control, tv_point, tv_recharge, tv_along, tv_clock;
    ImageView image_ele, image_back, image_start,
            image_control, image_setting, image_function, image_animation,
            image_clock, image_point, image_recharge, image_key_point, image_along, image_edge;
    Animation animation, animation_alpha;
    ImageView image_quan;
    View layout_recharge, layout_key_point, layout_along, layout_map;
    ACDeviceMsg mAcDevMsg;
    PopupWindow errorPopup, menuPopup, controlPopup;
    AnimationDrawable drawable;
    ArrayList<Integer> pointList;
    ArrayList<String> pointStrList;
    ArrayList<Integer> obstaclesList;
    ACDeviceDataMgr.PropertyReceiver propertyReceiver;
    AlertDialog alertDialog;
    ImageView image_slam, image_road, image_wall, image_wall_icon;
    Bitmap bitmap_slam, bitmap_road, bitmap_wall;
    Canvas canvas_slam, canvas_road, canvas_wall;
    Timer timer;
    TimerTask task;
    boolean isVirtualEdit, isDelete, isVirtualWall, isOnCreate, isAdd;
    boolean isAutoScale = true;
    byte[] slamBytes, contentBytes;
    int[] colors;
    Paint paint;
    float mapScale;
    int x, y, totalCount, translateDx, translateDy;
    float[] values = new float[9];
    Matrix matrix = new Matrix();
    Matrix matrixs = new Matrix();
    Matrix savedMatrix = new Matrix();
    PointF rectMid = new PointF();
    PointF startPoints = new PointF();
    PointF keyDownPoint = new PointF();
    private float start_x, start_y, downX, downY, moveDis, space;
    static final int NONE = 1;
    static final int DRAG = 2;
    static final int ZOOM = 3;
    int mode = 1;
    int packageId = 0;
    int package_index = 1;
    float oriDis = 1f;
    PointF midPoint, virMidPoint, wallStartPt;
    private int numWidth, numHeight;
    long lastStartTime;
    int index, vNum, length;
    Paint paint_radius, paint_virtual;
    int[] realPoint, numIcons;
    //    float curSpace = 2.0f;
    ArrayList<Rect> rectLists = new ArrayList<>();
    Bitmap bitmapNum, bitmap_scale;
    private Rect dstRect, srcRect;
    byte[] bytes_subscribe;
    ArrayList<Integer> realTimePoints, historyRoadList;
    ArrayList<int[]> wallPointList = new ArrayList<>();
    ArrayList<int[]> existPointList = new ArrayList<>();
    Paint paint_real, paint_history, paint_position;
    ImageView image_clear, image_add, image_delete, image_save;
    StringBuilder sb;
    float curSpace = 6.0f;
    float SPACE = 6.0f;
    float max;
    float min = 4.0f;
    float[] matrixvalues = new float[9];
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_SLAM:
                    canvas_slam.save();
                    image_slam.setImageBitmap(bitmap_slam);
                    break;
                case SET_CANVAS_ROAD_BITMAP:
                    canvas_road.save();
                    image_road.setImageBitmap(bitmap_road);
                    break;
                case SEND_VIRTUALDATA_SUCCESS:
                    ToastUtils.showToast(context, getString(R.string.map_aty_set_suc));
                    isVirtualEdit = false;
                    isAdd = false;
                    break;
                case SEND_VIRTUALDATA_FAILED:
                    ToastUtils.showToast(context, getString(R.string.map_aty_set_fail));
                    break;
                case QUERYVIRTUAL_SUCCESS_SHOWLINE:
                    drawVirtualLines(wallPointList);
                    break;
                case VIRTUALWALL_MAXCOUNT:
                    ToastUtils.showToast(context, context.getString(R.string.map_aty_max_count));
                    break;
                case START_AUTO_SCALE:
                    isAutoScale = true;
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        initData();
        super.onCreate(savedInstanceState);
        initMap();
        initTimer();
        getHistoryRoad();
        getVirtualWallInfo();
        subscribeRealTimeMap();
    }
    @Override
    public int getLayoutId() {
        return R.layout.activity_map_new;
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
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    public void initView() {
        errorPopup = new PopupWindow();
        anchorView = findViewById(R.id.rl_status);
        tv_time = (TextView) findViewById(R.id.tv_time);
        tv_area = (TextView) findViewById(R.id.tv_area);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_start = (TextView) findViewById(R.id.tv_start);
        tv_status = (TextView) findViewById(R.id.tv_status);
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
        layout_map = findViewById(R.id.layout_map);
        layout_along = findViewById(R.id.layout_along);
        layout_recharge = findViewById(R.id.layout_recharge);
        layout_key_point = findViewById(R.id.layout_key_point);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        drawable = (AnimationDrawable) image_animation.getBackground();
        image_back.setOnClickListener(this);
        image_start.setOnClickListener(this);
        image_control.setOnClickListener(this);
        image_setting.setOnClickListener(this);
        image_function.setOnClickListener(this);

        image_slam = findViewById(R.id.image_slam);
        image_road = findViewById(R.id.image_road);
        image_wall = findViewById(R.id.image_wall);

        layout_map.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });
        image_wall.setOnTouchListener(new MyTouchListener());
        tv_title.setText(getString(R.string.map_aty_title, robotType));

        initWall();
    }

    public void initWall() {
        image_wall_icon = new ImageView(context);
        image_wall_icon.setId(R.id.image_wall_icon);
        image_wall_icon.setImageResource(R.drawable.n_bg_wall);
        relativeLayout.addView(image_wall_icon);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(DisplayUtil.dip2px(context, 30), DisplayUtil.dip2px(context, 30));
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.ABOVE, R.id.rl_bottom);
        lp.bottomMargin = DisplayUtil.dip2px(context, 20);
        lp.rightMargin = DisplayUtil.dip2px(context, 25);
        image_wall_icon.setLayoutParams(lp);
        image_wall_icon.setOnClickListener(this);

        image_clear = initBottomZoom(R.id.image_clear, R.drawable.n_image_clear);
        image_add = initBottomZoom(R.id.image_add, R.drawable.n_image_add);
        image_delete = initBottomZoom(R.id.image_delete, R.drawable.n_image_delete);
        image_save = initBottomZoom(R.id.image_save, R.drawable.save_virtual);

        image_clear.setOnClickListener(this);
        image_add.setOnClickListener(this);
        image_delete.setOnClickListener(this);
        image_save.setOnClickListener(this);
        hideVirtualEdit();
        numIcons = new int[]{R.drawable.virtua1, R.drawable.virtua2, R.drawable.virtua3, R.drawable.virtua4, R.drawable.virtua5,
                R.drawable.virtua6, R.drawable.virtua7, R.drawable.virtua8, R.drawable.virtua9, R.drawable.virtua10};
        matrixs.postScale(1 / 3f, 1 / 3f);
        Bitmap numBit = BitmapFactory.decodeResource(getResources(), numIcons[0]);
        Bitmap numBitmap = Bitmap.createBitmap(numBit, 0, 0, numBit.getWidth(), numBit.getHeight(), matrixs, true);
        numWidth = numBitmap.getWidth();//1.0f-->47
        numHeight = numBitmap.getHeight();//1.0f-->47
    }

    public void initMap() {
        bitmap_slam = Bitmap.createBitmap(1500, 1500, Bitmap.Config.ARGB_8888);
        if (!bitmap_slam.isMutable()) {
            bitmap_slam = bitmap_slam.copy(Bitmap.Config.ARGB_8888, true);
        }
        canvas_slam = new Canvas(bitmap_slam);
        image_slam.setImageBitmap(bitmap_slam);

        bitmap_road = Bitmap.createBitmap(1500, 1500, Bitmap.Config.ARGB_8888);
        if (!bitmap_road.isMutable()) {
            bitmap_road = bitmap_road.copy(Bitmap.Config.ARGB_8888, true);
        }
        canvas_road = new Canvas(bitmap_road);
        image_road.setImageBitmap(bitmap_road);

        bitmap_wall = Bitmap.createBitmap(1500, 1500, Bitmap.Config.ARGB_8888);
        if (bitmap_wall.isMutable()) {
            bitmap_wall = bitmap_wall.copy(Bitmap.Config.ARGB_8888, true);
        }
        canvas_wall = new Canvas(bitmap_wall);
        image_wall.setImageBitmap(bitmap_wall);

        colors = new int[]{Color.parseColor("#9f5948"),
                Color.parseColor("#DCDCDC"),
                Color.parseColor("#00ffffff")};
        paint = new Paint();
        paint.setAntiAlias(true);

        paint_radius = new Paint();
        paint_radius.setColor(Color.RED);
        paint_radius.setAntiAlias(true);
        paint_radius.setStrokeWidth(3);

        paint_virtual = new Paint();
        paint_virtual.setStyle(Paint.Style.STROKE);
        paint_virtual.setColor(Color.RED);
        paint_virtual.setStrokeWidth(3);
        paint_virtual.setAntiAlias(true);

        paint_real = new Paint();
        paint_real.setColor(Color.parseColor("#ffffff"));//路径
        paint_real.setStrokeWidth(1);

        paint_history = new Paint();
        paint_history.setColor(Color.parseColor("#ffffff"));
        paint_history.setStrokeWidth(1);

        paint_position = new Paint();
        paint_position.setColor(Color.GREEN);
        paint_position.setStrokeWidth(10);
    }

    public void initTimer() {
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                if (curStatus == 0x06 || curStatus == 0x08 || curStatus == 0x07) {
                    getRealTimeMap();
                }
            }
        };
        timer.schedule(task, 0, 3 * 1000);
    }

    private void getRealTimeMap() {
        final ACMsg req = new ACMsg();
        req.setName("searchCleanRealTime");
        req.put("device_id", deviceId);
        String serviceName = "ILife-X900-CN-Test";
//        String serviceName = "ILife-X900-CN";
        AC.sendToService("", serviceName, Constants.SERVICE_VERSION, req, new PayloadCallback<ACMsg>() {
            @Override
            public void success(ACMsg resp) {
                String strMap = resp.getString("slam_map");
                int xMax = resp.getInt("slam_x_max");
                int xMin = resp.getInt("slam_x_min");
                int yMax = resp.getInt("slam_y_max");
                int yMin = resp.getInt("slam_y_min");
                rectMid.set((xMax + xMin) / 2, (yMax + yMin) / 2);
                if (!TextUtils.isEmpty(strMap)) {
                    slamBytes = Base64.decode(strMap, Base64.DEFAULT);
                    if (isOnCreate) {
                        drawSlamBytes();
                        isOnCreate = false;
                    }
                    translateDx = image_slam.getWidth() / 2 - bitmap_slam.getWidth() / 2;
                    translateDy = image_slam.getHeight() / 2 - bitmap_slam.getHeight() / 2;
                    if (isAutoScale) {
                        if (xMax > 0 && xMin > 0 && yMax > 0 && yMin > 0) {
                            if (image_slam.getWidth() > 0 && image_slam.getHeight() > 0 && canvas_slam.getWidth() > 0 && canvas_slam.getHeight() > 0) {
                                int xdistence = xMax - xMin;
                                int ydistence = yMax - yMin;
                                if (xdistence > ydistence) {
                                    setMatrixWithX(xdistence);
                                } else {
                                    setMatrixWithY(ydistence);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void error(ACException e) {
                MyLog.e(TAG, "getRealTimeMap e = " + e.toString());
            }
        });
    }

    private void getHistoryRoad() {
        final ACMsg req = new ACMsg();
        req.setName("searchCleanRoadData");
        req.put("device_id", deviceId);
        String serviceNames = "ILife-X900-CN-Test";
//        String serviceNames = "ILife-X900-CN";
        AC.sendToService("", serviceNames, Constants.SERVICE_VERSION, req, new PayloadCallback<ACMsg>() {
            @Override
            public void success(ACMsg acMsg) {
                ArrayList<ACObject> objects = acMsg.get("data");
                if (objects != null && objects.size() > 0) {
                    for (int i = 0; i < objects.size(); i++) {
                        ACObject acObject = objects.get(i);
                        String cleanData = acObject.getString("clean_data");
                        byte[] history_bytes = Base64.decode(cleanData, Base64.DEFAULT);
                        if (history_bytes.length > 0) {
                            if (history_bytes.length == 6) {
                                //清除路径
//                                canvasRoad.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//                                canvasRoad.save();
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
                    if (historyRoadList != null && historyRoadList.size() > 0) {
                        for (int k = 0; k < historyRoadList.size() - 2; k += 2) {
                            canvas_road.drawLine((float) historyRoadList.get(k), (float) 1500 - historyRoadList.get(k + 1),
                                    (float) historyRoadList.get(k + 2), (float) 1500 - historyRoadList.get(k + 3), paint_history);
                            canvas_road.drawPoint((float) historyRoadList.get(k), (float) 1500 - historyRoadList.get(k + 1), paint_history);
                        }
                    }
                    handler.sendEmptyMessage(SET_CANVAS_ROAD_BITMAP);
                }
            }

            @Override
            public void error(ACException e) {

            }
        });

    }

    private void getVirtualWallInfo() {
        mAcDevMsg.setCode(MsgCodeUtils.QueryVirtualWall);
        mAcDevMsg.setContent(new byte[]{0x00});
        queryVirtualWall(mAcDevMsg, physicalId);
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
                                                    bytes_subscribe = concat_(bytes_subscribe, Base64.decode(mapInfo.getClean_data(), Base64.DEFAULT), bytes_subscribe[0]);
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
                                                realTimePoints.add(end_y);
                                                realTimePoints.add(end_x);
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
                                                tv_area.setText(getString(R.string.map_aty_gang));
                                                tv_time.setText(getString(R.string.map_aty_gang));

                                                canvas_road.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                                                canvas_road.save();
                                                image_road.setImageBitmap(bitmap_road);

                                                canvas_slam.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                                                canvas_slam.save();
                                                image_slam.setImageBitmap(bitmap_slam);

                                                canvas_wall.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                                                canvas_wall.save();
                                                image_wall.setImageBitmap(bitmap_wall);

                                                realTimePoints.clear();
                                                historyRoadList.clear();
                                                obstaclesList.clear();
//                                                sendLists.clear();
                                                wallPointList.clear();
                                            } else {
                                                realTimePoints.add(y * 224 / 100 + 750);
                                                realTimePoints.add(x * 224 / 100 + 750);
                                            }
                                        }

                                        if (realTimePoints.size() > 0) {
                                            canvas_road.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                                            canvas_road.save();
                                            //slam地图
                                            drawSlamBytes();
                                            //实时路径
                                            for (int j = 0; j < realTimePoints.size() - 2; j += 2) {
                                                canvas_road.drawLine(realTimePoints.get(j + 1),
                                                        1500 - realTimePoints.get(j),
                                                        realTimePoints.get(j + 3),
                                                        1500 - realTimePoints.get(j + 2), paint_real);
                                                canvas_road.drawPoint(realTimePoints.get(j + 1), 1500 - realTimePoints.get(j), paint_real);
                                            }
                                            //历史路径
                                            if (historyRoadList != null && historyRoadList.size() > 0) {
                                                for (int m = 0; m < historyRoadList.size() - 2; m += 2) {
                                                    canvas_road.drawLine((float) historyRoadList.get(m), (float) 1500 - historyRoadList.get(m + 1), (float) historyRoadList.get(m + 2), (float) 1500 - historyRoadList.get(m + 3), paint_history);
                                                    canvas_road.drawPoint((float) historyRoadList.get(m), (float) 1500 - historyRoadList.get(m + 1), paint_history);
                                                }
                                            }
                                            //重绘障碍物
                                            if (obstaclesList.size() > 0) {
                                                paint.setColor(colors[0]);
                                                for (int f = 0; f < obstaclesList.size(); f += 2) {
                                                    canvas_road.drawPoint(obstaclesList.get(f), obstaclesList.get(f + 1), paint);
                                                }
                                            }
                                            obstaclesList.clear();
                                            int endX = realTimePoints.get(realTimePoints.size() - 1);
                                            int endY = realTimePoints.get(realTimePoints.size() - 2);
                                            canvas_road.drawCircle(endX, 1500 - endY, 5, paint_position);
                                        }
                                    }
                                    canvas_road.save();
                                    image_road.setImageBitmap(bitmap_road);
                                    int workTime = mapInfo.getReal_clean_time();
                                    int cleanArea = mapInfo.getReal_clean_area();
                                    if (curStatus == 0x08) {
                                        tv_area.setText("——");
                                        tv_time.setText("——");
                                    } else {
                                        tv_area.setText(cleanArea / 100.0 + "㎡");
                                        tv_time.setText(workTime / 60 + "min");
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

    private void resetVirable() {//重置变量
        x = 0;
        y = 0;
        totalCount = 0;
    }

    private void setMatrixWithX(int distence) {
        MyLog.e(TAG, "ScaleWithX=====:" + distence);
        if (image_slam.getWidth() / distence >= 2.5) {
            mapScale = 2.5f;
            resetMatrix(2.5f, 2.5f);
        } else {
            double result = image_slam.getWidth() * 1.0 / distence;
            BigDecimal bigDecimal = new BigDecimal(result).setScale(1, BigDecimal.ROUND_HALF_UP);
            resetMatrix(bigDecimal.floatValue(), bigDecimal.floatValue());
        }
    }

    private void setMatrixWithY(int distence) {
        if (image_slam.getHeight() / distence >= 2.5) {
            mapScale = 2.5f;
            resetMatrix(2.5f, 2.5f);
        } else {
            double result = image_slam.getHeight() * 1.0 / distence;
            BigDecimal bigDecimal = new BigDecimal(result).setScale(1, BigDecimal.ROUND_HALF_UP);
            resetMatrix(bigDecimal.floatValue(), bigDecimal.floatValue());
        }
    }

    private void resetMatrix(float sx, float sy) {
        int trX = bitmap_slam.getWidth() / 2 - (int) rectMid.x;
        int trY = bitmap_slam.getHeight() / 2 - (int) rectMid.y;
        MyLog.e(TAG, "Scale==resetMatrix:" + trX + ";" + trY);
        matrix.reset();
        matrix.postTranslate(translateDx + trX, translateDy - trY);
        matrix.postScale(sx, sy, image_slam.getWidth() / 2, image_slam.getHeight() / 2);
        setImageViewMatrix();
    }

    private void setImageViewMatrix() {
        image_slam.setImageMatrix(matrix);
        image_road.setImageMatrix(matrix);
        image_wall.setImageMatrix(matrix);
    }

    public void initData() {
        sb = new StringBuilder();
        isOnCreate = true;
        context = this;
        gson = new Gson();
        mAcDevMsg = new ACDeviceMsg();
        pointList = new ArrayList<>();
        pointStrList = new ArrayList<>();
        obstaclesList = new ArrayList<>();
        realTimePoints = new ArrayList<>();
        historyRoadList = new ArrayList<>();
        animation = AnimationUtils.loadAnimation(context, R.anim.anims_ni);
        animation.setInterpolator(new LinearInterpolator());
        animation_alpha = AnimationUtils.loadAnimation(context, R.anim.anim_alpha);
        deviceId = SpUtils.getLong(context, MainActivity.KEY_DEVICEID);
        subdomain = SpUtils.getSpString(context, MainActivity.KEY_SUBDOMAIN);
        physicalId = SpUtils.getSpString(context, MainActivity.KEY_PHYCIALID);
        if (subdomain.equals(Constants.subdomain_x900)) {
            robotType = "X900";
        } else if (subdomain.equals(Constants.subdomain_x787)) {
            robotType = "X787";
        } else {
            robotType = "X785";
        }
    }

    private void initPropReceiver() {
        propertyReceiver = new ACDeviceDataMgr.PropertyReceiver() {
            @Override
            public void onPropertyReceive(String s, long l, String s1) {
                MyLog.e(TAG, "onPropertyReceive ==== " + s1);
                if (!isDestroyed()) {
                    PropertyInfo info = gson.fromJson(s1, PropertyInfo.class);
                    MyLog.e(TAG, "initPropReceiver onPropertyReceive errorCode = " + info.getError_info());
                    errorCode = info.getError_info();
                    batteryNo = info.getBattery_level();
                    curStatus = info.getWork_pattern();
                    isMaxMode = info.getVacuum_cleaning() == 0x01;
                    mopForce = info.getCleaning_cleaning();
                    voiceOpen = info.getVoice_mode() == 0x01;
                    setStatus(curStatus, batteryNo, mopForce, isMaxMode, voiceOpen);
                    setTimeAndArea(curStatus);
                    if (errorCode != 0) {
                        if (errorPopup != null && !errorPopup.isShowing()) {
                            showErrorPopup(errorCode);
                        }
                        if (layout_map.getVisibility() == View.VISIBLE) {
                            layout_map.setVisibility(View.INVISIBLE);
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

    public void setTimeAndArea(int curStatus) {
        if (curStatus != 0x06) {
            tv_time.setText(getString(R.string.map_aty_gang));
            tv_area.setText(getString(R.string.map_aty_gang));
        } else {
            tv_area.setText(cleanArea / 100.0 + "㎡");
            tv_time.setText(workTime / 60 + "min");
        }
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

    public void showSelectDialog() {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_select_mode_dialog, null);
        v.findViewById(R.id.tv_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogUtils.hidden(alertDialog);
                mAcDevMsg.setContent(new byte[]{0x0c});
                sendByte = mAcDevMsg.getContent()[0];
                sendToDeviceWithOption_start(mAcDevMsg);
            }
        });
        v.findViewById(R.id.tv_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogUtils.hidden(alertDialog);
                mAcDevMsg.setContent(new byte[]{0x02});
                sendByte = mAcDevMsg.getContent()[0];
                sendToDeviceWithOption_start(mAcDevMsg);

            }
        });
        int width = (int) getResources().getDimension(R.dimen.dp_300);
        int height = (int) getResources().getDimension(R.dimen.dp_140);
        alertDialog = AlertDialogUtils.showDialog(context, v, width, height);
    }

    private void showSetWallDialog() {//进入虚拟墙编辑模式
        View v = LayoutInflater.from(context).inflate(R.layout.layout_set_wall_dialog, null);
        int width = (int) getResources().getDimension(R.dimen.dp_300);
        int height = (int) getResources().getDimension(R.dimen.dp_140);
        alertDialog = AlertDialogUtils.showDialog(context, v, width, height);
        v.findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtils.hideDialog(alertDialog);
                if (curStatus == 0x06) {
//                    setVirtualWallStatus(false);
                    mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
                    mAcDevMsg.setContent(new byte[]{0x07});
                    sendByte = mAcDevMsg.getContent()[0];
                    sendToDeviceWithOption_start(mAcDevMsg);
                }
            }
        });
        v.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtils.hideDialog(alertDialog);
            }
        });
    }

    private void showClearWallDialog() {//清除虚拟墙
        View v = LayoutInflater.from(context).inflate(R.layout.layout_clear_wall_dialog, null);
        int width = (int) getResources().getDimension(R.dimen.dp_300);
        int height = (int) getResources().getDimension(R.dimen.dp_140);
        alertDialog = AlertDialogUtils.showDialog(context, v, width, height);
        v.findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtils.hideDialog(alertDialog);
//                index = 0;
//                canvas_wall.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//                canvas_wall.save();
//                wallPointList.clear();
//                rectLists.clear();
//                vNum = 0;
//                image_wall.setImageBitmap(bitmap_wall);
                sendVirtualWallData(existPointList, EXIT_VIR);
            }
        });
        v.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtils.hideDialog(alertDialog);
            }
        });
    }


    private void showAddWallDialog() {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_add_wall_dialog, null);
        int width = (int) getResources().getDimension(R.dimen.dp_300);
        int height = (int) getResources().getDimension(R.dimen.dp_140);
        alertDialog = AlertDialogUtils.showDialog(context, v, width, height);
        v.findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtils.hideDialog(alertDialog);
                image_add.setSelected(true);
                image_delete.setSelected(false);
                isAdd = true;
                isDelete = false;
                isVirtualWall = true;
            }
        });
        v.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtils.hideDialog(alertDialog);
            }
        });
    }

    private void showDeleteWallDialog() {//删除虚拟墙
        View v = LayoutInflater.from(context).inflate(R.layout.layout_delete_wall_dialog, null);
        int width = (int) getResources().getDimension(R.dimen.dp_300);
        int height = (int) getResources().getDimension(R.dimen.dp_140);
        alertDialog = AlertDialogUtils.showDialog(context, v, width, height);
        v.findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtils.hideDialog(alertDialog);
                image_delete.setSelected(true);
                image_add.setSelected(false);
                isAdd = false;
                isDelete = true;
                isVirtualWall = false;
            }
        });
        v.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtils.hideDialog(alertDialog);
            }
        });
    }

    private void showSaveWallDialog() {//下发虚拟墙
        View v = LayoutInflater.from(context).inflate(R.layout.layout_save_wall_dialog, null);
        int width = (int) getResources().getDimension(R.dimen.dp_300);
        int height = (int) getResources().getDimension(R.dimen.dp_140);
        alertDialog = AlertDialogUtils.showDialog(context, v, width, height);
        v.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtils.hideDialog(alertDialog);
            }
        });
        v.findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtils.hideDialog(alertDialog);
                sendVirtualWallData(wallPointList, SEND_VIR);
            }
        });
    }

//    private void showIfSaveDialog() {
//        View v = LayoutInflater.from(context).inflate(R.layout.layout_if_save_dialog, null);
//        int width = (int) getResources().getDimension(R.dimen.dp_300);
//        int height = (int) getResources().getDimension(R.dimen.dp_140);
//        alertDialog = AlertDialogUtils.showDialog(context,v,width,height);
//        v.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                DialogUtils.hideDialog(alertDialog);
//            }
//        });
//        v.findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                DialogUtils.hideDialog(alertDialog);
//                saveRect(realPoint);
//            }
//        });
//        alertDialog.setCanceledOnTouchOutside(false);
//    }

//    private void saveRect(int[] pointlist) {//保存画好的虚拟墙的矩形
//        if (!isDelete) {
//            Rect svRect = new Rect(pointlist[2] - numWidth / 2 - 10, pointlist[3] - numHeight - 10, pointlist[2] + numWidth / 2 + 10, pointlist[3] + 10);
//            rectLists.add(svRect);
//        }
//    }

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
                    (int) getResources().getDimension(R.dimen.dp_32),
                    (int) getResources().getDimension(R.dimen.dp_120));
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

    public void getClockInfo() {
        ACDeviceMsg msg_clockInfo = new ACDeviceMsg(MsgCodeUtils.ClockInfos, new byte[]{0x00});
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, msg_clockInfo,
                Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
                    @Override
                    public void success(ACDeviceMsg deviceMsg) {
                        adjustTime();
                        if (!isDestroyed()) {
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

    public void adjustTime() {
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

    public void setStatus(int curStatus, int batteryNo, int mopForce, boolean isMaxMode, boolean voiceOpen) {
        isWork = isWork(curStatus);
        tv_status.setText(DeviceUtils.getStatusStr(context, curStatus, errorCode));
        image_start.setSelected(isWork);
        tv_start.setText(isWork ? getString(R.string.map_aty_stop) : getString(R.string.map_aty_start));
        image_wall_icon.setSelected(canEdit(curStatus));
        if (batteryNo != -1) {
            setBatteryImage(curStatus, batteryNo);
            SpUtils.saveBoolean(context, physicalId + KEY_IS_MAX, isMaxMode);
            SpUtils.saveInt(context, physicalId + KEY_MOP_FORCE, mopForce);
            SpUtils.saveBoolean(context, physicalId + KEY_VOICE_OPEN, voiceOpen);
        }
        clearAll(curStatus);
//        if (curStatus==0x8){ //回充
//            layout_recharge.setVisibility(View.VISIBLE);
//            drawable.start();
//            setTextStatus(TAG_RECHAGRGE);
////            image_quan.clearAnimation();
////            hasStart = false;
//        }else
        if (curStatus == 0x07) {
            isVirtualEdit = true;
            showVirtualEdit();
            layout_map.setVisibility(View.VISIBLE);
        } else if (curStatus == 0x05) { //重点
            layout_key_point.setVisibility(View.VISIBLE);
            if (!hasStart) {
                image_quan.startAnimation(animation);
                hasStart = true;
            }
            setTextStatus(TAG_KEYPOINT);
        } else if (curStatus == 0x0A) { //遥控
            setTextStatus(TAG_CONTROL);
        }
//        else if (curStatus==0x02|| //待机暂停
//                curStatus==0x0C){
//
//        }
        else if (curStatus == 0x04) {
            layout_along.setVisibility(View.VISIBLE);
            if (!hasStart_) {
                image_edge.startAnimation(animation_alpha);
                hasStart_ = true;
            }
            setTextStatus(TAG_ALONG);
        } else if (canEdit(curStatus)) {
            showMap();
//            layout_map.setVisibility(View.VISIBLE);
//            image_ele.bringToFront();
//            image_wall_icon.bringToFront();
//            setTextStatus(TAG_NORMAL);
        }

        if (menuPopup != null && menuPopup.isShowing()) {
            image_point.setSelected(curStatus == 0x05);
            tv_point.setSelected(curStatus == 0x05);
            image_recharge.setSelected(curStatus == 0x08);
            tv_recharge.setSelected(curStatus == 0x08);
            image_along.setSelected(curStatus == 0x04);
            tv_along.setSelected(curStatus == 0x04);
        }
        setVirtualWallStatus(canEdit(curStatus));
    }

    public void clearAll(int curStatus) {
        if (curStatus != 0x5) {
            image_quan.clearAnimation();
            hasStart = false;
        }
        if (curStatus != 0x04) {
            image_edge.clearAnimation();
            hasStart_ = false;
        }
        hideVirtualEdit();
        layout_recharge.setVisibility(View.GONE);
        layout_key_point.setVisibility(View.GONE);
        layout_along.setVisibility(View.GONE);
        layout_map.setVisibility(View.INVISIBLE);
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
                    if (isX800) {
                        showSelectDialog();
                        return;
                    } else {
                        mAcDevMsg.setContent(new byte[]{0x02});
                    }
                } else {
                    mAcDevMsg.setContent(new byte[]{0x06});
                }
                sendByte = mAcDevMsg.getContent()[0];
                sendToDeviceWithOption_start(mAcDevMsg);
                break;
            case R.id.image_control://done
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
                timer.cancel();
                break;
            case R.id.image_setting:
                Intent i = new Intent(context, SettingActivity.class);
                startActivity(i);
                break;
            case R.id.image_clock: //done
                if (menuPopup != null && menuPopup.isShowing()) {
                    menuPopup.dismiss();
                }
                Intent i_clock = new Intent(context, ClockingActivity.class);
                startActivity(i_clock);
                break;
            case R.id.image_along:  //done
                mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
                if (curStatus == 0x02 || curStatus == 0x04 || curStatus == 0x0A) {
                    byte b = (byte) (curStatus == 0x04 ? 0x02 : 0x04);
                    mAcDevMsg.setContent(new byte[]{b});
                    sendToDeviceWithOption(mAcDevMsg);
                } else if (curStatus == 0x09 || curStatus == 0x0B) {
                    ToastUtils.showToast(context, getString(R.string.map_aty_charge));
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
                } else if (curStatus == 0x05 || curStatus == 0x04) {
                    ToastUtils.showToast(context, getString(R.string.map_aty_can_not_execute));
                } else {
                    mAcDevMsg.setCode(MsgCodeUtils.WorkMode);
                    if (curStatus == 0x08) {
                        mAcDevMsg.setContent(new byte[]{0x02});
                    } else {
                        mAcDevMsg.setContent(new byte[]{0x08});
                    }
                    sendToDeviceWithOption(mAcDevMsg);
                }
                break;
            case R.id.image_wall_icon:
                if (canEdit(curStatus)) {
                    showSetWallDialog();
                }
                break;
            case R.id.image_clear:
                showClearWallDialog();
                break;
            case R.id.image_add:
                if (isAdd) {
                    image_add.setSelected(false);
                    isAdd = false;
//                    isVirtualWall = false;
                } else {
                    showAddWallDialog();
                }
                break;
            case R.id.image_delete:
                if (isDelete) {
                    image_delete.setSelected(false);
                    isDelete = false;
                } else {
                    showDeleteWallDialog();
                }
                break;
            case R.id.image_save:
                showSaveWallDialog();
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
                setStatus(curStatus, -1, mopForce, isMaxMode, voiceOpen);
            }
        });
    }

    public void sendToDeviceWithOption_start(ACDeviceMsg msg) {
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, msg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void error(ACException e) {
                MyLog.e(TAG, "sendToDeviceWithOption_start error " + e.toString());
                ToastUtils.showErrorToast(context, e.getErrorCode());
            }

            @Override
            public void success(ACDeviceMsg deviceMsg) {
                byte[] bytes = deviceMsg.getContent();
                MyLog.e(TAG, "sendToDeviceWithOption_start  success " + bytes[0]);
                curStatus = bytes[0];
                if (curStatus == sendByte) {
                    setStatus(curStatus, -1, mopForce, isMaxMode, voiceOpen);
                } else {
                    if (curStatus == 0x0B) {
                        ToastUtils.showToast(context, getString(R.string.map_aty_charge));
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (errorPopup != null && errorPopup.isShowing()) {
            errorPopup.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            workTime = intent.getIntExtra("workTime", 0);
            cleanArea = intent.getIntExtra("cleanArea", 0);
            MyLog.e(TAG, "onReceive workTime = " + workTime + " cleanArea = " + cleanArea);
            if (curStatus != 0x06) {
                tv_time.setText(getString(R.string.map_aty_gang));
                tv_area.setText(getString(R.string.map_aty_gang));
            } else {
                tv_area.setText(cleanArea / 100.0 + "㎡");
                tv_time.setText(workTime / 60 + "min");
            }
        }
    }

    public class MyTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView view = (ImageView) v;
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if (isVirtualEdit) {
                        if (isAdd) {
                            if (!isDelete && wallPointList.size() >= 10) {
                                handler.sendEmptyMessage(VIRTUALWALL_MAXCOUNT);
                            } else {
                                matrix.getValues(values);
                                wallStartPt = getMatrixValues(event);
                                start_x = wallStartPt.x;
                                start_y = wallStartPt.y;
                            }
                        } else {
                            if (isDelete) {
                                PointF keyDeletePt = getMatrixValues(event);
                                matrix.getValues(values);
                                downX = keyDeletePt.x;
                                downY = keyDeletePt.y;
                                doDeleteVirtual(downX, downY);
                                canvas_wall.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                                canvas_wall.save();
                                drawVirtualLines(wallPointList);
                                image_wall.setImageBitmap(bitmap_wall);
                            } else {//编辑模式下非添加和删除时移动
                                initDraging(view, event);
                            }
                        }
                    } else {
                        initDraging(view, event);
                        isAutoScale = false;
                        handler.removeMessages(START_AUTO_SCALE);
                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    initZooming(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    MyLog.e(TAG, "isVirtualEdit = " + isVirtualEdit + " isAdd= " + isAdd + " isDelete = " + isDelete);
                    if (isVirtualEdit) {
                        if (isAdd) {
                            if (!isDelete && wallPointList.size() >= 10) {
                                break;
                            } else {
                                canvas_wall.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                                canvas_wall.save();
                                matrix.getValues(values);
                                PointF keyMovePt = getMatrixValues(event);
                                float move_x = keyMovePt.x;
                                float move_y = keyMovePt.y;
                                if (distance(wallStartPt, keyMovePt) > 20) {
                                    canvas_wall.drawLine(start_x, start_y, move_x, move_y, paint_virtual);
                                }
                                drawVirtualLines(wallPointList);
                                canvas_wall.save();
                                image_wall.setImageBitmap(bitmap_wall);
                            }
                        } else if (isDelete) {
                            break;
                        } else {
                            doDragOrZoom(event);
                        }
                    } else {
                        doDragOrZoom(event);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (isVirtualEdit) {
                        if (isAdd) {
                            if (wallPointList.size() >= 10) {
                                break;
                            }
                            matrix.getValues(values);
                            PointF wallEndPt = getMatrixValues(event);
                            float end_x = wallEndPt.x;
                            float end_y = wallEndPt.y;
                            if (distance(wallStartPt, wallEndPt) >= 20) {
                                canvas_wall.drawCircle(start_x, start_y, 1.5f, paint_radius);
                                canvas_wall.drawCircle(end_x, end_y, 1.5f, paint_radius);
                                realPoint = new int[]{(int) start_x, (int) start_y, (int) end_x, (int) end_y};
                                wallPointList.add(realPoint);
                                saveQueryRect(realPoint);
                                bitmapNum = BitmapFactory.decodeResource(getResources(), numIcons[wallPointList.size() - 1]);
                                drawVirNumUni(realPoint, bitmapNum);
                                canvas_wall.save();
                                image_wall.setImageBitmap(bitmap_wall);
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
            }
            return true;
        }
    }

    private PointF getMatrixValues(MotionEvent event) {
        float x = (event.getX() - values[Matrix.MTRANS_X]) / values[Matrix.MSCALE_X];
        float y = (event.getY() - values[Matrix.MTRANS_Y]) / values[Matrix.MSCALE_X];
        return new PointF(x, y);
    }

    private void doDeleteVirtual(float downX, float downY) {//删除虚拟墙
        if (rectLists != null && rectLists.size() > 0) {
            for (int i = 0; i < rectLists.size(); i++) {
                Rect rect = rectLists.get(i);
                if (rect.contains((int) (downX), (int) (downY))) {
                    rectLists.remove(i);
                    wallPointList.remove(i);
                }
            }
        }
    }

    private float distance(PointF pointF1, PointF pointF2) {
        float x = pointF1.x - pointF2.x;
        float y = pointF1.y - pointF2.y;
        return (float) Math.sqrt(x * x + y * y);
    }

    private void setDraging(MotionEvent event, PointF startPoint) {//手势拖拽
        matrix.set(savedMatrix);
        matrix.postTranslate(event.getX() - startPoint.x, event.getY()
                - startPoint.y);
        setImageViewMatrix();
    }

    private void setZooming(MotionEvent event, PointF midPoint, int width) {//手势缩放
        if (width <= 720) {
            max = 30.0f;
        } else {
            max = 42.0f;
        }
        float newDist = DataUtils.distance(event);
        if (newDist > 10f) {
            matrix.set(savedMatrix);
            float scale = newDist / oriDis;
            if (curSpace >= min && curSpace <= max) {
                if (curSpace == min) {
                    if (scale <= 1) {
                        return;
                    }
                }
                if (curSpace == max) {
                    if (scale >= 1) {
                        return;
                    }
                }
                matrix.postScale(scale, scale, midPoint.x, midPoint.y);
                setImageViewMatrix();
                matrix.getValues(matrixvalues);
                curSpace = SPACE * matrixvalues[Matrix.MSCALE_X];
            } else if (curSpace < min) {
                curSpace = min;
            } else {
                curSpace = max;
            }
        }
    }

    private void drawVirtualLines(ArrayList<int[]> pointList) {
        if (pointList != null && pointList.size() > 0) {
            for (int i = 0; i < pointList.size(); i++) {
                int[] point = pointList.get(i);
                canvas_wall.drawLine((float) point[0], (float) point[1], (float) point[2], (float) point[3], paint_virtual);
                canvas_wall.drawCircle((float) point[0], (float) point[1], 1.5f, paint_radius);
                canvas_wall.drawCircle((float) point[2], (float) point[3], 1.5f, paint_radius);
                bitmapNum = BitmapFactory.decodeResource(getResources(), numIcons[i]);
                drawVirNumUni(point, bitmapNum);
            }
        }
//        canvas_wall.save();
//        image_wall.setImageBitmap(bitmap_wall);
    }

//    private void drawListLines() {
//        if (wallPointList != null && wallPointList.size() > 0) {
//            for (int i = 0; i < wallPointList.size(); i++) {
//                int[] data = wallPointList.get(i);
//                bitmapNum = BitmapFactory.decodeResource(getResources(), numIcons[i + vNum]);
//                bitmap_scale = Bitmap.createBitmap(bitmapNum, 0, 0, bitmapNum.getWidth(), bitmapNum.getHeight(), matrixs, true);
//                srcRect = new Rect(0, 0, bitmap_scale.getWidth(), bitmap_scale.getHeight());
//                dstRect = new Rect((int) data[2] - bitmap_scale.getWidth() / 2, (int) data[3] - bitmap_scale.getHeight() - 5, (int) data[2] + bitmap_scale.getWidth() / 2, (int) data[3] - 5);
//                canvas_wall.drawBitmap(bitmap_scale, srcRect, dstRect, null);
//            }
//        }
//    }

    private void drawNumOnly(int[] data) {
        bitmapNum = BitmapFactory.decodeResource(getResources(), numIcons[wallPointList.size()]);
        bitmap_scale = Bitmap.createBitmap(bitmapNum, 0, 0, bitmapNum.getWidth(), bitmapNum.getHeight(), matrixs, true);
        srcRect = new Rect(0, 0, bitmap_scale.getWidth(), bitmap_scale.getHeight());
        dstRect = new Rect((int) data[2] - bitmap_scale.getWidth() / 2, (int) data[3] - bitmap_scale.getHeight() - 5, (int) data[2] + bitmap_scale.getWidth() / 2, (int) data[3] - 5);
        canvas_wall.drawBitmap(bitmap_scale, srcRect, dstRect, null);
        canvas_wall.save();
        image_wall.setImageBitmap(bitmap_wall);
    }


    public byte[] concat_(byte[] a, byte[] b, byte type) {
        int offset = 0;
        switch (type) {
            case 1:
                offset = 7;
                break;
            case 2:
                offset = 2;
                break;
        }
        byte[] c = new byte[a.length + b.length - offset];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, offset, c, a.length, b.length - offset);
        return c;
    }

    private void drawSlamBytes() {
        if (slamBytes != null && slamBytes.length > 0) {
            canvas_slam.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            canvas_slam.save();
            for (int i = 0; i < slamBytes.length; i += 3) {
                byte attr = slamBytes[i];
                MyLog.e(TAG, "drawSlamBytes attr = " + attr);
                length = DataUtils.bytesToInt2(new byte[]{slamBytes[i + 1], slamBytes[i + 2]}, 0);
                paint.setColor(colors[attr - 1]);
                for (int j = 0; j < length; j++) {
                    if (totalCount >= 1500) {
                        x = 0;
                        totalCount = 0;
                        y++;
                    }
                    if (attr != 0x03) {
                        canvas_slam.drawPoint(x, 1500 - y, paint);
                        if (attr == 0x01) {
                            obstaclesList.add(x);
                            obstaclesList.add(1500 - y);
                        }
                    }
                    x++;
                    totalCount++;
                }
            }
            resetVirable();
            handler.sendEmptyMessage(UPDATE_SLAM);
        }
    }

    public void setVirtualWallStatus(boolean enable) {
        image_wall_icon.setSelected(enable);
        image_wall_icon.setClickable(enable);
    }

    private ImageView initBottomZoom(int id, int resId) {
        ImageView imageView = new ImageView(context);
        imageView.setId(id);
        imageView.setImageResource(resId);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(DisplayUtil.dip2px(context, 30), DisplayUtil.dip2px(context, 30));
        relativeLayout.addView(imageView);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.bottomMargin = DisplayUtil.dip2px(context, 20);
        if (id == R.id.image_clear) {
            lp.leftMargin = DisplayUtil.dip2px(context, 80);
        } else if (id == R.id.image_add) {
            lp.addRule(RelativeLayout.RIGHT_OF, R.id.image_clear);
            lp.leftMargin = DisplayUtil.dip2px(context, 25);
        } else if (id == R.id.image_delete) {
            lp.addRule(RelativeLayout.RIGHT_OF, R.id.image_add);
            lp.leftMargin = DisplayUtil.dip2px(context, 25);
        } else if (id == R.id.image_save) {
            lp.addRule(RelativeLayout.RIGHT_OF, R.id.image_delete);
            lp.leftMargin = DisplayUtil.dip2px(context, 25);
        }
        imageView.setLayoutParams(lp);
        return imageView;
    }

    public void showVirtualEdit() {
        image_add.setVisibility(View.VISIBLE);
        image_delete.setVisibility(View.VISIBLE);
        image_clear.setVisibility(View.VISIBLE);
        image_save.setVisibility(View.VISIBLE);
        isAdd = false;
        isDelete = false;
        image_add.setSelected(false);
        image_delete.setSelected(false);
    }

    public void hideVirtualEdit() {
        image_add.setVisibility(View.GONE);
        image_delete.setVisibility(View.GONE);
        image_clear.setVisibility(View.GONE);
        image_save.setVisibility(View.GONE);
        isAdd = false;
        isDelete = false;
        image_add.setSelected(false);
        image_delete.setSelected(false);
    }

    private void sendVirtualWallData(final ArrayList<int[]> list, final int tag) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                contentBytes = new byte[82];
//                if (sendLists != null && sendLists.size() > 0) {
                if (list != null && list.size() > 0) {
//                    int size = sendLists.size();
                    int size = list.size();
                    byte open = (byte) 0x01;
                    byte counts = (byte) size;
                    contentBytes[0] = open;
                    contentBytes[1] = counts;
                    for (int t = 1; t < size + 1; t++) {
//                        int[] floats = sendLists.get(t - 1);
                        int[] floats = list.get(t - 1);
                        int x1 = (int) floats[0] - 750;
                        int y1 = (int) 1500 - floats[1] - 750;
                        int x2 = (int) floats[2] - 750;
                        int y2 = (int) 1500 - floats[3] - 750;
                        byte[] startxBytes = DataUtils.intToBytes(x1);
                        byte[] startyBytes = DataUtils.intToBytes(y1);
                        byte[] endxBytes = DataUtils.intToBytes(x2);
                        byte[] endyBytes = DataUtils.intToBytes(y2);
                        contentBytes[(t - 1) * 8 + 2] = startxBytes[0];
                        contentBytes[(t - 1) * 8 + 3] = startxBytes[1];
                        contentBytes[(t - 1) * 8 + 4] = startyBytes[0];
                        contentBytes[(t - 1) * 8 + 5] = startyBytes[1];
                        contentBytes[(t - 1) * 8 + 6] = endxBytes[0];
                        contentBytes[(t - 1) * 8 + 7] = endxBytes[1];
                        contentBytes[(t - 1) * 8 + 8] = endyBytes[0];
                        contentBytes[(t - 1) * 8 + 9] = endyBytes[1];
                        MyLog.e(TAG, "byte arry==:" + startxBytes[0] + "," + startxBytes[1] + "," + startyBytes[0] + "," + startyBytes[1] + "," + endxBytes[0] + "," + endxBytes[1] + "," + endyBytes[0] + "," + endyBytes[1]);
                        MyLog.e(TAG, "byte arry==:" + Integer.toHexString(startxBytes[0]) + "," + Integer.toHexString(startxBytes[1]) + "," + Integer.toHexString(startyBytes[0]) + "," + Integer.toHexString(startyBytes[1]) + "," + Integer.toHexString(endxBytes[0]) + "," + Integer.toHexString(endxBytes[1]) + "," + Integer.toHexString(endyBytes[0]) + "," + Integer.toHexString(endyBytes[1]));
                        MyLog.e(TAG, "xia fa qian wei zhuanhua zuo biao :" + "(" + floats[0] + "," + floats[1] + ")" + ":" + "(" + floats[2] + "," + floats[3] + ")");
                        MyLog.e(TAG, "xia fa qian zhuanhua hou zuo biao :" + "(" + x1 + "," + y1 + ")" + ":" + "(" + x2 + "," + y2 + ")");
                    }
                } else {
                    MyLog.e(TAG, "sendLists is null");
                }
                mAcDevMsg.setCode(MsgCodeUtils.SetVirtualWall);
                mAcDevMsg.setContent(contentBytes);
                sendToDeviceWithOptionVirtualWall(mAcDevMsg, physicalId, tag);
            }
        }).start();

    }

    private void sendToDeviceWithOptionVirtualWall(ACDeviceMsg acDeviceMsg, String physicalDeviceId, final int tag) {
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalDeviceId, acDeviceMsg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void success(ACDeviceMsg acDeviceMsg) {
                if (tag == SEND_VIR) {
                    existPointList.clear();
                    existPointList.addAll(wallPointList);
                    handler.sendEmptyMessage(SEND_VIRTUALDATA_SUCCESS);
                }
                if (tag == EXIT_VIR) {
                    wallPointList.clear();
                    wallPointList.addAll(existPointList);
                }
                drawSuccessVirtual();
//                drawVirtualLines(existPointList);
            }

            @Override
            public void error(ACException e) {
                handler.sendEmptyMessage(SEND_VIRTUALDATA_FAILED);
            }
        });
    }

    private void queryVirtualWall(final ACDeviceMsg deviceMsg, final String physicalId) {
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, deviceMsg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
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
                        saveQueryRect(dataPoint);
                    }
                    wallPointList.clear();
                    wallPointList.addAll(existPointList);
                    handler.sendEmptyMessage(QUERYVIRTUAL_SUCCESS_SHOWLINE);
                }
            }

            @Override
            public void error(ACException e) {

            }
        });
    }

    private void saveQueryRect(int[] data) {
        Rect rect = new Rect(data[2] - numWidth / 2 - 10, data[3] - numHeight - 10, data[2] + numWidth / 2 + 10, data[3] + 10);
        rectLists.add(rect);
    }

    public boolean canEdit(int curStatus) {
        if (curStatus == 0x02 || curStatus == 0x06 || curStatus == 0x08 ||
                curStatus == 0x09 || curStatus == 0x0D || curStatus == 0x0C) {
            return true;
        }
        return false;
    }

    public void showMap() {
        layout_map.setVisibility(View.VISIBLE);
        image_ele.bringToFront();
        image_wall_icon.bringToFront();
        setTextStatus(TAG_NORMAL);
    }

    private void initDraging(ImageView view, MotionEvent event) {
        matrix.set(view.getImageMatrix());
        savedMatrix.set(matrix);
        startPoints.set(event.getX(), event.getY());
        mode = DRAG;
    }

    private void initZooming(MotionEvent event) {
        oriDis = DataUtils.distance(event);
        if (oriDis > 10f) {
            savedMatrix.set(matrix);
            midPoint = DataUtils.midPoint(event);
            mode = ZOOM;
        }
    }

    private void doDragOrZoom(MotionEvent event) {
        if (mode == DRAG) {
            setDraging(event, startPoints);
        } else if (mode == ZOOM) {
            setZooming(event, midPoint, image_slam.getWidth());
        }
    }

    private void drawSuccessVirtual() {
//        isAdd = false;
//        isDelete = false;
//        image_add.setSelected(false);
//        image_delete.setSelected(false);
        hideVirtualEdit();
        canvas_wall.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        rectLists.clear();
        if (existPointList != null && existPointList.size() > 0) {
            for (int i = 0; i < existPointList.size(); i++) {
                int[] point = existPointList.get(i);
                //虚拟墙
                canvas_wall.drawLine((float) point[0], (float) point[1], (float) point[2], (float) point[3], paint_virtual);
                canvas_wall.drawCircle((float) point[0], (float) point[1], 1f, paint_radius);
                canvas_wall.drawCircle((float) point[2], (float) point[3], 1f, paint_radius);
                //num图标
                bitmapNum = BitmapFactory.decodeResource(getResources(), numIcons[i]);
                drawVirNumUni(point, bitmapNum);
                saveQueryRect(point);
            }
        }
        canvas_wall.save();
        image_wall.setImageBitmap(bitmap_wall);
    }

    private void drawVirNumUni(int[] data, Bitmap bitmap) {//画虚拟墙编号
        bitmap_scale = Bitmap.createBitmap(bitmap, 0, 0, bitmapNum.getWidth(), bitmapNum.getHeight(), matrixs, true);
        srcRect = new Rect(0, 0, bitmap_scale.getWidth(), bitmap_scale.getHeight());
        dstRect = new Rect((int) data[2] - bitmap_scale.getWidth() / 2, (int) data[3] - bitmap_scale.getHeight() - 5, (int) data[2] + bitmap_scale.getWidth() / 2, (int) data[3] - 5);
        canvas_wall.drawBitmap(bitmap_scale, srcRect, dstRect, null);
    }
}
