package com.ilife.iliferobot_cn.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.app.MyApplication;
import com.ilife.iliferobot_cn.utils.DataUtils;
import com.ilife.iliferobot_cn.utils.Utils;

import java.math.BigDecimal;
import java.util.ArrayList;

public class MapView extends View {
    private static String TAG = "MapView";
    private int width, height, centerX, centerY;
    private Paint slamPaint, roadPaint, virtualPaint, positionCirclePaint;
    private Path roadPath, slamPath;
    private float lastX, lastY;
    private float beforeDistance, afterDistance;
    public static final int MODE_NONE = 1;
    public static final int MODE_DRAG = 2;
    public static final int MODE_ZOOM = 3;
    private int MODE;
    private int originalMode;
    private float scare = 1;
    private float originalScare = 1;//view缩放比例
    private float baseScare = 1;//基准坐标缩放比例，以slam边缘占据view3/4，与view同坐标中心为标准

    private PointF sCenter, downPoint;
    private Bitmap roadBitmap, slagBitmap;
    private Canvas roadCanvas, slamCanvas;
    private boolean isInitBuffer = false;
    private Matrix matrix;
    private float dragX, originalDragX, dragY, originalDragY;
    private final int DEFAULT_SIZE = 1500;
    private float deviationX, deviationY;
    private int[] colors;
    private boolean isEditVirtualWall = false;

    public MapView(Context context) {
        super(context);
        init();
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        colors = new int[]{getResources().getColor(R.color.obstacle_color), getResources().getColor(R.color.slam_color),
                getResources().getColor(R.color.color_00ffffff)};
        MODE = MODE_NONE;
        originalMode = MODE;
        matrix = new Matrix();
        sCenter = new PointF(0, 0);
        downPoint = new PointF(0, 0);
        roadPath = new Path();
        slamPath = new Path();
        slamPaint = new Paint();
        slamPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        slamPaint.setStyle(Paint.Style.STROKE);
        slamPaint.setFilterBitmap(true);
        slamPaint.setStrokeJoin(Paint.Join.ROUND);
        slamPaint.setStrokeCap(Paint.Cap.ROUND);
        slamPaint.setColor(getResources().getColor(R.color.colorPrimary));
        slamPaint.setStrokeWidth(4f);

        positionCirclePaint = new Paint();
        positionCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        positionCirclePaint.setStyle(Paint.Style.FILL);
        positionCirclePaint.setFilterBitmap(true);
        positionCirclePaint.setStrokeJoin(Paint.Join.ROUND);
        positionCirclePaint.setStrokeCap(Paint.Cap.ROUND);
        positionCirclePaint.setColor(getResources().getColor(R.color.color_ef8200));
        positionCirclePaint.setStrokeWidth(3f);


        roadPaint = new Paint();
        roadPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        roadPaint.setStyle(Paint.Style.STROKE);
        roadPaint.setFilterBitmap(true);
        roadPaint.setStrokeJoin(Paint.Join.ROUND);
        roadPaint.setStrokeCap(Paint.Cap.ROUND);
        roadPaint.setColor(getResources().getColor(R.color.white));
        roadPaint.setStrokeWidth(4f);

        virtualPaint = new Paint();
        virtualPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        virtualPaint.setStyle(Paint.Style.STROKE);
        virtualPaint.setFilterBitmap(true);
        virtualPaint.setStrokeJoin(Paint.Join.ROUND);
        virtualPaint.setStrokeCap(Paint.Cap.ROUND);
        virtualPaint.setColor(getResources().getColor(R.color.color_ff2035));
        virtualPaint.setStrokeWidth(3f);
    }

    public void setMODE(int MODE) {
        this.MODE = MODE;
        this.originalMode = MODE;
    }

    public void setEditVirtualWall(boolean isEditVirtualWall) {
        this.isEditVirtualWall = isEditVirtualWall;
    }

    private void initBuffer() {
        if (isInitBuffer) {
            return;
        }
        isInitBuffer = true;
        roadBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        slagBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        roadCanvas = new Canvas(roadBitmap);
        slamCanvas = new Canvas(slagBitmap);
        updateSlam(644, 807, 736, 827, null);
    }

    /**
     * 绘制路线地图，包含历史地图/即时地图
     * y坐标没有按照实际坐标绘制
     *
     * @param roadList
     */
    public void drawRoadMap(ArrayList<Integer> roadList, ArrayList<Integer> historyRoadList) {
        roadCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        roadCanvas.save();
//        绘制历史路径坐标点，下一条路径的起始坐标为上 一条路径的终点坐标
        if (roadList != null && roadList.size() > 0) {
            for (int k = 0; k < roadList.size() - 1; k += 2) {
                if (k == 0) {
                    roadPath.moveTo(matrixCoordinateX(roadList.get(0)), matrixCoordinateY(1500 - roadList.get(1)));//设置起点
                } else {
                    roadPath.lineTo(matrixCoordinateX(roadList.get(k)), matrixCoordinateY(1500 - roadList.get(k + 1)));
                }
            }
        }

        if (historyRoadList != null && historyRoadList.size() > 0) {
            for (int k = 0; k < historyRoadList.size() - 1; k += 2) {
                if (k == 0) {
                    roadPath.moveTo(matrixCoordinateX(historyRoadList.get(0)), matrixCoordinateY(1500 - historyRoadList.get(1)));//设置起点
                } else {
                    roadPath.lineTo(matrixCoordinateX(historyRoadList.get(k)), matrixCoordinateY(1500 - historyRoadList.get(k + 1)));
                }
                Log.d("roadx", "x:" + historyRoadList.get(k) + "---y:" + (1500 - historyRoadList.get(k + 1)));
            }
        }
        roadCanvas.drawPath(roadPath, roadPaint);
        if (roadList != null && roadList.size() > 2) {
            float endY = matrixCoordinateY(1500 - roadList.get(roadList.size() - 1));
            float endX = matrixCoordinateX(roadList.get(roadList.size() - 2));
            roadCanvas.drawCircle(endX, endY, Utils.dip2px(MyApplication.getInstance(), 5), positionCirclePaint);
        }
        invalidate();
    }

    /**
     * 绘制障碍物点
     *
     * @param obstacleList 障碍物点集合
     */
    public void drawObstacle(ArrayList<Integer> obstacleList) {
        float[] obstacles = new float[obstacleList.size()];
        for (int i = 0; i < obstacleList.size(); i++) {
            if (i % 2 == 0) {
                obstacles[i] = matrixCoordinateX(obstacleList.get(i));
            } else {
                obstacles[i] = matrixCoordinateY(obstacleList.get(i));
            }

        }
        slamPaint.setColor(colors[0]);
        roadCanvas.drawPoints(obstacles, slamPaint);
        invalidate();
    }


    /**
     * @param xMin
     * @param xMax
     * @param yMin
     * @param yMax
     * @param slamBytes
     */
    public void updateSlam(int xMin, int xMax, int yMin, int yMax, byte[] slamBytes) {
        int xLength = xMax - xMin;
        int yLength = yMax - yMin;
        double result = width * 0.85f / xLength;
        BigDecimal bigDecimal = new BigDecimal(result).setScale(1, BigDecimal.ROUND_HALF_UP);
        if (baseScare == 1) {
            baseScare = bigDecimal.floatValue();
        }
        float left = (width - xLength * baseScare) / 2;
        float top = (height - yLength * baseScare) / 2;
        deviationX = xMin - left / baseScare;
        deviationY = yMin - top / baseScare;


        sCenter.set(width / 2f, height / 2f);
        slamCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        slamCanvas.save();
        drawSlamMap(slamBytes);
        invalidate();
    }


    /**
     * s
     *
     * @param originalCoordinate
     * @return
     */
    private float matrixCoordinateX(float originalCoordinate) {
        float value = (originalCoordinate - deviationX) * baseScare;
        return value;
    }

    /**
     * s
     *
     * @param originalCoordinate
     * @return
     */
    private float matrixCoordinateY(float originalCoordinate) {
        float value = (originalCoordinate - deviationY) * baseScare;
        return value;
    }

    /**
     * roadPath layer matrix
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        matrix.reset();
        matrix.postTranslate(dragX, dragY);
        Log.d("SelfPaint", "Scare" + scare + "---tansX:" + dragX + "---" + dragY + "--sx" + sCenter.x + "---sy" + sCenter.y);
        matrix.postScale(scare, scare, sCenter.x, sCenter.y);
        canvas.drawBitmap(slagBitmap, matrix, slamPaint);
        canvas.drawBitmap(roadBitmap, matrix, roadPaint);
        super.onDraw(canvas);
        roadPath.reset();
        slamPath.reset();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        centerX = (int) (width / 2f);
        height = h;
        centerY = (int) (height / 2f);
        initBuffer();

    }

    private PointF midPoint(MotionEvent event) {
        float x = (event.getX(0) + event.getX(1)) / 2;
        float y = (event.getY(0) + event.getY(1)) / 2;
        return new PointF(x, y);
    }

    private float getOffsetX() {
        return (sCenter.x * (scare - 1) / scare) - dragX;
    }

    private float getOffsetY() {
        return scare + (sCenter.y * (scare - 1) / scare) - dragY;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int me = event.getAction() & MotionEvent.ACTION_MASK;
        float x = event.getX() / scare + getOffsetX();
        float y = event.getY() / scare + getOffsetY();
        Log.d("SelfPaint", "x--:" + x + "y---:" + y);
        switch (me) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                lastY = y;
                downPoint.set(event.getX(), event.getY());
                roadPath.moveTo(x, y);
                if (!isEditVirtualWall) {
                    originalMode = MODE;
                    MODE = MODE_DRAG;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (MODE == MODE_ZOOM) {
                    if (event.getPointerCount() == 2) {
                        calculateScare(event);
                    }
                } else if (MODE == MODE_DRAG) {
                    dragX = (event.getX() - downPoint.x) / scare + originalDragX;
                    dragY = (event.getY() - downPoint.y) / scare + originalDragY;
                } else {
                    roadPath.quadTo(lastX, lastY, (x + lastX) / 2f, (y + lastY) / 2f);
                    lastX = x;
                    lastY = y;
                    roadBitmap.eraseColor(Color.TRANSPARENT);
                    roadCanvas.drawPath(roadPath, virtualPaint);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (MODE == MODE_NONE) {
//                    roadPath.addCircle(x, y, 5, Path.Direction.CCW);
//                    roadBitmap.eraseColor(Color.TRANSPARENT);
//                    roadCanvas.drawPath(roadPath, slamPaint);
//                    invalidate();
                } else if (MODE == MODE_DRAG) {
                    originalDragX = dragX;
                    originalDragY = dragY;
                }
                MODE = originalMode;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                originalScare = scare;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    originalMode = MODE;
                    MODE = MODE_ZOOM;
                    beforeDistance = distance(event);
                }
                break;

        }
        return true;
    }

    private void calculateScare(MotionEvent event) {
        afterDistance = distance(event);
        if (Math.abs(afterDistance - beforeDistance) > 10) {
//            sCenter = midPoint(event);
            scare = (afterDistance / beforeDistance) * originalScare;
            Log.d("scare", "---" + scare);
            if (scare < 0.6f) {
                scare = 0.6f;
            }
        }
    }

    /**
     * 计算两个手指间的距离
     *
     * @param event 触摸事件
     * @return 放回两个手指之间的距离
     */
    private float distance(MotionEvent event) {
        float x = 0;
        float y = 0;
        try {
            x = event.getX(0) - event.getX(1);
            y = event.getY(0) - event.getY(1);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return (float) Math.sqrt(x * x + y * y);//两点间距离公式
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = getMySize(widthMeasureSpec);
        final int height = getMySize(heightMeasureSpec);
        setMeasuredDimension(width, height);
//        final int min = Math.min(width, height);//保证控件为方形
//        setMeasuredDimension(min, min);
    }

    /**
     * 获取测量大小
     */
    private int getMySize(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;//确切大小,所以将得到的尺寸给view
        } else if (specMode == MeasureSpec.AT_MOST) {
            //默认值为450px,此处要结合父控件给子控件的最多大小(要不然会填充父控件),所以采用最小值
            result = Math.min(DEFAULT_SIZE, specSize);
        } else {
            result = DEFAULT_SIZE;
        }
        return result;
    }


    private int x, y, length, totalCount;


    /**
     * 从(0,1500)开始向上一行行绘制slam map
     */
    public void drawSlamMap(byte[] slamBytes) {
        if (slamBytes != null && slamBytes.length > 0) {
            slamCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            slamCanvas.save();
            for (int i = 0; i < slamBytes.length; i += 3) {
                byte attr = slamBytes[i];
                length = DataUtils.bytesToInt2(new byte[]{slamBytes[i + 1], slamBytes[i + 2]}, 0);
                Log.d(TAG, "地图类型:" + attr + "---length" + length);
                slamPaint.setColor(colors[attr - 1]);
                for (int j = 0; j < length; j++) {
                    if (totalCount >= 1500) {
                        x = 0;
                        totalCount = 0;
                        y++;
                    }
                    if (attr != 0x03) {//0x03未探索区域 0x02已探索区域
                        Log.d(TAG, "slam x:---" + x + "y:---" + (1500 - y));
                        if (i != 0) {
                            int distance = (int) (matrixCoordinateY(1500 - y) - matrixCoordinateY(1500 - y - 1));
                            for (int k = 0; k < distance; k++) {
                                slamCanvas.drawLine(matrixCoordinateX(x - 1), matrixCoordinateY(1500 - y) + k, matrixCoordinateX(x), matrixCoordinateY(1500 - y) + k, slamPaint);

                            }

                        }
                    }
                    x++;
                    totalCount++;
                }
            }
            x = 0;
            totalCount = 0;
            y = 0;
        }

    }


}
