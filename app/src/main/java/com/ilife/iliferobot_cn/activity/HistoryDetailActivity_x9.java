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
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.entity.HistoryRecord_x9;
import com.ilife.iliferobot_cn.utils.BitmapUtils;
import com.ilife.iliferobot_cn.utils.DataUtils;
import com.ilife.iliferobot_cn.utils.MyLog;
import com.ilife.iliferobot_cn.utils.SpUtils;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by chenjiaping on 2017/8/18.
 */

public class HistoryDetailActivity_x9 extends BaseActivity implements View.OnClickListener {
    private final String TAG = HistoryDetailActivity_x9.class.getSimpleName();
    //    private final int SPACE = 8;
    private final int SPACE = 4;
    static final int NONE = 1;
    static final int DRAG = 2;
    static final int ZOOM = 3;
    static final int DX = -390;
    static final int DY = -239;
    int mode = 1;
    private byte[] slamBytes;
    private byte[] roadBytes;
    private ImageView imageView;
    private ImageView image_back;
    private ArrayList<String> mapList;
    private ArrayList<Paint> paints;
    private Paint paint_history;
    private byte[] bytes;
    private int length;
    private byte attr;
    private Paint paint_slam;
    int count = 0;
    int x = 0;
    int y = 0;
    private ArrayList<Integer> historyPointsList;
    private Context context;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paint_1;
    private Paint paint_2;
    private Paint paint_3;
    private String subdomain;
    private long deviceId;
    private int width;
    private int height;
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    PointF startPoints = new PointF();
    PointF midPoint = new PointF();
    PointF rectMid = new PointF();
    float oriDis = 1f;
    //    float space;
//    float curSpace = 2.0f;
    private int startX;
    private int startY;
    private int lineCount;
    private RelativeLayout rl_image;
    private int xMin;
    private int xMax;
    private int yMin;
    private int yMax;
    private int startReason;
    private int stopReason;
    private TextView tv_start_reason;
    private TextView tv_stop_reason;
    private int xdistence;
    private int ydistence;
    float curSpace = 6.0f;
    float SPACES = 6.0f;
    float max;
    float min = 4.0f;
    float[] matrixvalues = new float[9];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        getData();
//        decodeData();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_history_detail;
    }

    private void drawHistoryMap() {
        if (mapList != null && mapList.size() > 0) {
            //salm地图
            String slamData = mapList.get(0);
            if (!TextUtils.isEmpty(slamData)) {
                slamBytes = Base64.decode(slamData, Base64.DEFAULT);
                MyLog.e(TAG, "slamBytes:" + slamBytes.length);
                drawSlamMap(slamBytes);
            }
            //历史路径
            String roadData = mapList.get(1);
            if (!TextUtils.isEmpty(roadData)) {
                roadBytes = Base64.decode(roadData, Base64.DEFAULT);
                drawRoad(roadBytes);
                MyLog.e(TAG, "roadBytes:" + roadBytes.length + "<--->");
            }
        }

        imageView.setImageBitmap(bitmap);

    }

    private void drawRoad(byte[] roadBytes) {
        if (roadBytes == null || roadBytes.length == 0) {
            MyLog.e(TAG, "bytes is null");
            return;
        } else {
            MyLog.e(TAG, "bytes is not null：" + roadBytes.length);
            if (roadBytes.length >= 4 && roadBytes.length % 4 == 0) {
                for (int j = 0; j < roadBytes.length; j += 4) {
                    int pointx = DataUtils.bytesToInt(new byte[]{roadBytes[j], roadBytes[j + 1]}, 0);//2,3
                    int pointy = DataUtils.bytesToInt(new byte[]{roadBytes[j + 2], roadBytes[j + 3]}, 0);//4,5
                    historyPointsList.add((pointx * 224) / 100 + 750);
                    historyPointsList.add((pointy * 224) / 100 + 750);
                }
            } else {
                MyLog.e(TAG, "bytes is not null222：" + roadBytes.length);
            }
        }
        drawHistoryRoad();
    }

    private void drawHistoryRoad() {
        if (historyPointsList != null && historyPointsList.size() > 0) {
            for (int k = 0; k < historyPointsList.size() - 2; k += 2) {
                canvas.drawLine((float) historyPointsList.get(k), (float) 1500 - historyPointsList.get(k + 1), (float) historyPointsList.get(k + 2), (float) 1500 - historyPointsList.get(k + 3), paint_history);
            }
        }
    }

    private void drawSlamMap(byte[] slamBytes) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.save();
        for (int i = 0; i < slamBytes.length; i += 3) {
            attr = slamBytes[i];
            length = DataUtils.bytesToUInt(new byte[]{slamBytes[i + 1], slamBytes[i + 2]}, 0);
            if (attr == 0x01) {
                paint_slam = paint_1;
            } else if (attr == 0x02) {
                paint_slam = paint_2;
            } else {
                paint_slam = paint_3;
            }
            for (int j = 0; j < length; j++) {
                if (count >= 1500) {
                    x = 0;
                    count = 0;
                    y++;
                }
                if (attr != 0x03) {
                    canvas.drawPoint(x, 1500 - y, paint_slam);
                    canvas.save();
                }
                x++;
                count++;
            }
        }
        resetVirable();
    }

    private void resetVirable() {//重置变量
        x = 0;
        y = 0;
        count = 0;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if ((!hasFocus) || bitmap != null) {
            return;
        }
        if (width == 0) {
            width = imageView.getWidth();
            height = imageView.getHeight();
        }

        bitmap = BitmapUtils.getBitmap();
        canvas = new Canvas(bitmap);
        imageView.setImageBitmap(bitmap);
        setMapToMid();
        drawHistoryMap();
    }

    private void setMapToMid() {
        MyLog.e(TAG, "xdistence = " + xdistence + " ydistence = " + ydistence);
        if (xdistence == 0 || ydistence == 0) {
            matrix.postTranslate((width - xdistence) / 2, (height - ydistence) / 2);
            matrix.postScale(3, 3, width / 2, height / 2);
            imageView.setImageMatrix(matrix);
        } else {
            if (xdistence > ydistence) {
                setMatrixWithXorY(xdistence, width);
            } else {
                setMatrixWithXorY(ydistence, width);
            }
        }
    }

    private void setMatrixWithXorY(int distence, int WidorHei) {
        if (WidorHei <= 720) {
            setScaleNum(WidorHei, distence, 2.5f);
        } else {
            setScaleNum(WidorHei, distence, 3.5f);
        }
    }

    private void setScaleNum(int width, int distence, float scaleNum) {
        if (width / distence >= scaleNum) {
            resetMatrix(scaleNum, scaleNum);
            MyLog.e(TAG, "ScaleWith=====:2.5f" + "<--->" + distence);
        } else {
            double result = width * 1.0 / distence;
            BigDecimal bigDecimal = new BigDecimal(result).setScale(1, BigDecimal.ROUND_HALF_UP);
            resetMatrix(bigDecimal.floatValue(), bigDecimal.floatValue());
            MyLog.e(TAG, "ScaleWith=====:" + "<--->" + bigDecimal.floatValue() + "<--->" + width + "<--->" + distence);
        }
    }

    private void resetMatrix(float sx, float sy) {
        rectMid.set((xMax + xMin) / 2, (yMax + yMin) / 2);
        int trX = bitmap.getWidth() / 2 - (int) rectMid.x;
        int trY = bitmap.getHeight() / 2 - (int) rectMid.y;
        matrix.postTranslate(DX + trX, DY - trY);
        matrix.postScale(sx, sy, width / 2, height / 2);
        imageView.setImageMatrix(matrix);
    }

    private void decodeData() {
        if (mapList != null) {
            if (mapList.size() == 1) {
                bytes = Base64.decode(mapList.get(0), Base64.DEFAULT);
            } else {
                for (int i = 0; i < mapList.size(); i++) {
                    if (i == 0) {
                        bytes = Base64.decode(mapList.get(0), Base64.DEFAULT);
                    } else {
                        bytes = concat(bytes, Base64.decode(mapList.get(i), Base64.DEFAULT), 2);
                    }
                }
            }
        }
    }

    private void getData() {//取出传递过来的集合
        Intent intent = getIntent();
        if (intent != null) {
            HistoryRecord_x9 record = (HistoryRecord_x9) intent.getSerializableExtra("Record");
            mapList = record.getHistoryData();
            xMin = record.getSlam_xMin();
            xMax = record.getSlam_xMax();
            yMin = record.getSlam_yMin();
            yMax = record.getSlam_yMax();
            startReason = record.getStart_reason();
            stopReason = record.getStop_reason();
            xdistence = xMax - xMin;
            ydistence = yMax - yMin;
            MyLog.e(TAG, "getDate===:" + xMin + "<--->" + xMax + "<--->" + yMin + "<--->" + yMax + "<--->" + startReason + "<--->" + stopReason);
//            DeviceUtils.setCleanReason(context,startReason,stopReason,tv_start_reason,tv_stop_reason);
        }
    }

    public void initView() {
        context = this;
        subdomain = SpUtils.getSpString(context, "subdomain");
        deviceId = SpUtils.getLong(context, "deviceId");
        historyPointsList = new ArrayList<>();
        paints = new ArrayList<>();
        paint_slam = new Paint();
        paint_1 = new Paint();
        paint_1.setColor(Color.parseColor("#39ABA7"));//障碍物
        paint_2 = new Paint();
        paint_2.setColor(Color.parseColor("#57D6D2"));//未清扫
        paint_3 = new Paint();
        paint_3.setColor(Color.parseColor("#00ffffff"));
//        paint_3.setStrokeWidth(SPACE);
        paint_history = new Paint();
        paint_history.setColor(Color.parseColor("#ffffff"));

        paints.add(paint_1);
        paints.add(paint_2);
        paints.add(paint_3);

        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        imageView.setOnTouchListener(new MyTouchListener());
        image_back = (ImageView) findViewById(R.id.image_back);
        image_back.setOnClickListener(this);
//        tv_start_reason = (TextView) findViewById(R.id.tv_start_reason_);
//        tv_stop_reason = (TextView) findViewById(R.id.tv_stop_reason_);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_back:
                finish();
                break;
        }
    }

    public class MyTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView view = (ImageView) v;
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    matrix.set(view.getImageMatrix());
                    savedMatrix.set(matrix);
                    startPoints.set(event.getX(), event.getY());
                    mode = DRAG;
                    MyLog.e(TAG, "ACTION_DOWN:");
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    oriDis = DataUtils.distance(event);
                    if (oriDis > 10f) {
                        savedMatrix.set(matrix);
                        midPoint = DataUtils.midPoint(event);
                        mode = ZOOM;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                        doDraging(event, startPoints, view);
                    } else if (mode == ZOOM) {
                        doZooming(event, midPoint, view);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    MyLog.e(TAG, "ACTION_UP:");
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    MyLog.e(TAG, "ACTION_POINTER_UP:");
                    // 手指放开事件
                    mode = NONE;
                    break;
            }
            return true;
        }

    }

    private void doZooming(MotionEvent event, PointF midPoint, ImageView view) {
        if (width <= 720) {
            max = 30.0f;
        } else {
            max = 42.0f;
        }
        float newDist = DataUtils.distance(event);
        if (newDist > 10f) {
            matrix.set(savedMatrix);
            float scale = newDist / oriDis;
            MyLog.e("MainActivity", "scale beishu111===:" + "<--->" + curSpace + "<--->");
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
                view.setImageMatrix(matrix);
                matrix.getValues(matrixvalues);
                curSpace = SPACES * matrixvalues[Matrix.MSCALE_X];
            } else if (curSpace < min) {
                curSpace = min;
            } else {
                curSpace = max;
            }
        }
    }

    private void doDraging(MotionEvent event, PointF startPoint, ImageView view) {//手势拖拽
        matrix.set(savedMatrix);
        matrix.postTranslate(event.getX() - startPoint.x, event.getY()
                - startPoint.y);
        MyLog.e(TAG, "Draging===:" + (event.getX() - startPoint.x) + "<--->" + (event.getY() - startPoint.y));
        view.setImageMatrix(matrix);
    }

    public byte[] concat(byte[] a, byte[] b, int offset) {
        byte[] c = new byte[a.length + b.length - offset];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, offset, c, a.length, b.length - offset);
        return c;
    }

}
