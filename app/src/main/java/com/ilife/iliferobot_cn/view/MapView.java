package com.ilife.iliferobot_cn.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.accloud.utils.LogUtil;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.app.MyApplication;
import com.ilife.iliferobot_cn.model.VirtualWallBean;
import com.ilife.iliferobot_cn.utils.BitmapUtils;
import com.ilife.iliferobot_cn.utils.DataUtils;
import com.ilife.iliferobot_cn.utils.ToastUtils;
import com.ilife.iliferobot_cn.utils.Utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MapView extends View {
    private static String TAG = "MapView";
    private int width, height;
    private Paint slamPaint, roadPaint, virtualPaint, positionCirclePaint;
    private Path roadPath, existvirtualPath, slamPath, obstaclePath;
    private float downX, downY;
    private float beforeDistance, afterDistance;
    public static final int MODE_NONE = 1;
    public static final int MODE_DRAG = 2;
    public static final int MODE_ZOOM = 3;
    public static final int MODE_ADD_VIRTUAL = 4;
    public static final int MODE_DELETE_VIRTUAL = 5;
    private int MODE;
    private int originalMode;
    private float scare = 1;
    private float originalScare = 1;//view缩放比例
    private float baseScare = 1;//基准坐标缩放比例，以slam边缘占据view3/4，与view同坐标中心为标准

    private PointF sCenter, downPoint;
    private Bitmap roadBitmap, slagBitmap, virtualBitmap;
    private Canvas roadCanvas, slamCanvas, virtualCanvas;
    private boolean isInitBuffer = false;
    private Matrix matrix;
    private float dragX, originalDragX, dragY, originalDragY;
    private final int DEFAULT_SIZE = 1500;
    private float deviationX, deviationY;
    private int[] colors;
    private List<VirtualWallBean> virtualWallBeans;
    private static final int MIN_WALL_LENGTH = 20;
    private Bitmap deleteBitmap;//删除虚拟墙的bitmap
    private static final int deleteIconW = 36;

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
        existvirtualPath = new Path();
        slamPath = new Path();
        obstaclePath = new Path();
        deleteBitmap = BitmapUtils.decodeSampledBitmapFromResource(getResources(), R.drawable.n_icon_delete_virtual, deleteIconW, deleteIconW);


        slamPaint = new Paint();
        slamPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        slamPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        slamPaint.setFilterBitmap(true);
        slamPaint.setStrokeJoin(Paint.Join.ROUND);
        slamPaint.setStrokeCap(Paint.Cap.ROUND);
        slamPaint.setColor(getResources().getColor(R.color.colorPrimary));
        slamPaint.setStrokeWidth(4f);

        positionCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        positionCirclePaint.setStyle(Paint.Style.FILL);
        positionCirclePaint.setFilterBitmap(true);
        positionCirclePaint.setStrokeJoin(Paint.Join.ROUND);
        positionCirclePaint.setStrokeCap(Paint.Cap.ROUND);
        positionCirclePaint.setColor(getResources().getColor(R.color.color_ef8200));
        positionCirclePaint.setStrokeWidth(3f);


        roadPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        roadPaint.setStyle(Paint.Style.STROKE);
        roadPaint.setFilterBitmap(true);
        roadPaint.setStrokeJoin(Paint.Join.ROUND);
        roadPaint.setStrokeCap(Paint.Cap.ROUND);
        roadPaint.setColor(getResources().getColor(R.color.white));
        roadPaint.setStrokeWidth(2f);

        virtualPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        virtualPaint.setStyle(Paint.Style.STROKE);
        virtualPaint.setFilterBitmap(true);
        virtualPaint.setStrokeJoin(Paint.Join.ROUND);
        virtualPaint.setStrokeCap(Paint.Cap.ROUND);
        virtualPaint.setColor(getResources().getColor(R.color.color_f08300));
        virtualPaint.setStrokeWidth(5f);
        /**
         * 虚拟墙路径集合
         */
        virtualWallBeans = new ArrayList<>();
    }

    /**
     * 设置地图模式
     *
     * @param MODE MODE_VIRTUAL 虚拟墙编辑模式
     */
    public void setMODE(int MODE) {
        this.MODE = MODE;
        this.originalMode = MODE;
        if (MODE == MODE_ADD_VIRTUAL) {
            drawVirtualWall();
        }
        if (MODE == MODE_DELETE_VIRTUAL) {
            drawVirtualWall();
            //draw delete sign
        }
    }

    public boolean isInMode(int mode) {
        return mode == MODE;
    }

    private void initBuffer() {
        if (isInitBuffer) {
            return;
        }
        isInitBuffer = true;
        roadBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        slagBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        virtualBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        roadCanvas = new Canvas(roadBitmap);
        slamCanvas = new Canvas(slagBitmap);
        virtualCanvas = new Canvas(virtualBitmap);
    }

    /**
     * 绘制路线地图，包含历史地图/即时地图
     * y坐标没有按照实际坐标绘制
     *
     * @param roadList
     */
    public void drawRoadMap(ArrayList<Integer> roadList, ArrayList<Integer> historyRoadList) {
        Log.d(TAG, "drawRoadMap-----");
        roadPath.reset();
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
     */
    public void drawObstacle() {
        slamPaint.setColor(colors[0]);
        slamCanvas.drawPath(obstaclePath, slamPaint);
        slamPaint.setColor(colors[1]);
        slamCanvas.drawPath(slamPath, slamPaint);
        invalidate();
    }

    /**
     * 清楚所有绘图痕迹
     */
    public void clean() {
        virtualCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        slamCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        roadCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    /**
     * @param xMin
     * @param xMax
     * @param yMin
     * @param yMax
     * @param slamBytes
     */
    // TODO 地图x方向居中
    public void updateSlam(int xMin, int xMax, int yMin, int yMax, byte[] slamBytes) {
        Log.d(TAG, "updateSlam---" + xMin + "---" + xMax + "---" + yMin + "---" + yMax);
        int xLength = xMax - xMin;
        int yLength = yMax - yMin;
        double resultX = width * 0.80f / xLength;
        double resultY = height * 0.70f / yLength;
        BigDecimal bigDecimal = new BigDecimal(Math.min(resultX, resultY)).setScale(1, BigDecimal.ROUND_HALF_UP);
        if (baseScare == 1) {
            baseScare = bigDecimal.floatValue();
        }
        deviationX = (xMin + xMax) / 2f - width / 2f / baseScare;
        deviationY = 750 - height / 2f / baseScare;
        Log.d(TAG, "deviationX" + deviationX + "---" + "deviationY" + deviationY);


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

    private float reMatrixCoordinateX(float originalCoordinate) {
        float value = originalCoordinate / baseScare + deviationX;
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

    private float reMatrixCoordinateY(float originalCoordinate) {
        float value = originalCoordinate / baseScare + deviationY;
        return value;
    }

    public void setScare(int scare) {
        this.scare = scare;
    }

    /**
     * roadPath layer matrix
     *
     * @param canvas
     */
    // TODO 手势缩放移动的时候延迟刷新实时地图数据/或者生成刷新队列
    @Override
    protected void onDraw(Canvas canvas) {
        matrix.reset();
        matrix.postTranslate(dragX, dragY);
        Log.d("SelfPaint", "Scare" + scare + "---tansX:" + dragX + "---" + dragY + "--sx" + sCenter.x + "---sy" + sCenter.y);
        matrix.postScale(scare, scare, sCenter.x, sCenter.y);
        canvas.drawBitmap(slagBitmap, matrix, slamPaint);
        canvas.drawBitmap(roadBitmap, matrix, roadPaint);
        canvas.drawBitmap(virtualBitmap, matrix, virtualPaint);
        super.onDraw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
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


    // TODO 重绘事件不要太频繁，真正有需求的时候才能调用
    // TODO 屏幕坐标转设备坐标
    ////删除虚拟墙的时候支持拖动,添加虚拟墙的时候支持缩放，NONE时支持缩放和拖动
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int me = event.getAction() & MotionEvent.ACTION_MASK;
        float x = event.getX() / scare + getOffsetX();
        float y = event.getY() / scare + getOffsetY();
        Log.d("SelfPaint", "x--:" + x + "y---:" + y);
        switch (me) {
            case MotionEvent.ACTION_DOWN:
                downX = x;
                downY = y;
                downPoint.set(event.getX(), event.getY());
                if (MODE == MODE_ADD_VIRTUAL) {
                    if (virtualWallBeans.size() >= 10) {
                        //TODO 提示虚拟墙数量超最大数
                    } else {
                    }
                } else if (MODE == MODE_DELETE_VIRTUAL) {
                    // TODO 删除虚拟墙
                } else {
                    originalMode = MODE;
                    MODE = MODE_DRAG;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (MODE == MODE_ZOOM) {
                    if (event.getPointerCount() == 2) {
                        calculateScare(event);
                    }
                } else if (MODE == MODE_DRAG || MODE == MODE_DELETE_VIRTUAL) {
                    dragX = (event.getX() - downPoint.x) / scare + originalDragX;
                    dragY = (event.getY() - downPoint.y) / scare + originalDragY;
                } else if (MODE == MODE_ADD_VIRTUAL) {
                    if (virtualWallBeans.size() >= 10) {
                        //TODO 提示虚拟墙数量超最大数
                    } else {
                        virtualCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        virtualCanvas.save();
                        virtualCanvas.drawPath(existvirtualPath, virtualPaint);
                        virtualCanvas.drawLine(downX, downY, x, y, virtualPaint);
                    }
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (MODE == MODE_NONE) {
//                    roadPath.addCircle(x, y, 5, Path.Direction.CCW);
//                    roadBitmap.eraseColor(Color.TRANSPARENT);
//                    roadCanvas.drawPath(roadPath, slamPaint);
//                    invalidate();
                } else if (MODE == MODE_ADD_VIRTUAL) {
                    if (virtualWallBeans.size() >= 10) {
                        // TODO 提示虚拟墙数量达到最大值
                    } else {
                        float distance = distance(downX, downY, x, y);
                        if (distance < MIN_WALL_LENGTH) {
                            // TODO 提示虚拟墙太短
                        } else {
                            existvirtualPath.moveTo(downX, downY);
                            existvirtualPath.lineTo(x, y);//加入到已存在的虚拟墙集合中去
                            VirtualWallBean virtualWallBean = new VirtualWallBean(virtualWallBeans.size() + 1, new int[]{(int) reMatrixCoordinateX(downX), (int) reMatrixCoordinateY(downY), (int) reMatrixCoordinateX(x), (int) reMatrixCoordinateY(y)}
                                    , 2);
                            virtualWallBeans.add(virtualWallBean);
                            drawVirtualWall();
                        }
                    }
                } else if (MODE == MODE_DELETE_VIRTUAL) {
                    //TODO delete  virtual wall
                    for (VirtualWallBean vr : virtualWallBeans) {
                        if (vr.getDeleteIcon() == null) {
                            continue;
                        }
                        if (vr.getDeleteIcon().contains(x, y)) {
                            ToastUtils.showToast("删除第" + vr.getNumber() + "条虚拟墙");
                            if (vr.getState() == 2) {//新增的虚拟墙，还未保存到服务器，可以直接移除
                                virtualWallBeans.remove(vr);
                            }
                            if (vr.getState() == 1) {//服务器上的虚拟墙，可能操作会被取消掉，只需要改变状态
                                vr.setState(3);
                            }
                            drawVirtualWall();
                            break;
                        }
                    }
                    //删除虚拟墙的时候支持拖动
                    originalDragX = dragX;
                    originalDragY = dragY;
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

    private float distance(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return (float) Math.sqrt(x * x + y * y);
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

    /**
     * 撤销所有虚拟墙操作，恢复到与服务器数据一致的状态
     */
    public void undoAllOperation() {
        if (virtualWallBeans != null && virtualWallBeans.size() > 0) {
            Iterator<VirtualWallBean> iterator = virtualWallBeans.iterator();
            while (iterator.hasNext()) {
                VirtualWallBean virtualWallBean = iterator.next();
                if (virtualWallBean.getState() == 2) {
                    iterator.remove();
                } else if (virtualWallBean.getState() == 3) {//被置为待删除的服务器虚拟墙恢复状态
                    virtualWallBean.setState(1);
                }
            }
        }
        drawVirtualWall();
    }

    /**
     * 获取虚拟墙列表,包含新增，和删除
     *
     * @return
     */
    public List<int[]> getVirtualWallPointfs() {
        List<int[]> virtualWallPointfs = new ArrayList<>();
        for (VirtualWallBean vr : virtualWallBeans) {
            if (vr.getState() != 3) {
                virtualWallPointfs.add(vr.getPointfs());
            }
        }
        return virtualWallPointfs;
    }


    /**
     * 查询到服务其虚拟墙数据后调用绘制虚拟墙
     *
     * @param existPointList 服务器虚拟墙数据集合
     */
    public void drawVirtualWall(List<int[]> existPointList) {
        if (existPointList != null && existPointList.size() > 0) {
            VirtualWallBean bean;
            virtualWallBeans.clear();
            for (int i = 0; i < existPointList.size(); i++) {
                bean = new VirtualWallBean(i + 1, existPointList.get(i), 1);
                virtualWallBeans.add(bean);
            }
        }
        drawVirtualWall();
    }

    /**
     * 绘制虚拟墙
     */
    public void drawVirtualWall() {
        if (virtualWallBeans == null) {
            return;
        }
        existvirtualPath.reset();
        virtualCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        virtualCanvas.save();
        for (VirtualWallBean vir : virtualWallBeans) {
            if (vir.getState() != 3) {
                existvirtualPath.moveTo(matrixCoordinateX(vir.getPointfs()[0]), matrixCoordinateY(vir.getPointfs()[1]));
                existvirtualPath.lineTo(matrixCoordinateX(vir.getPointfs()[2]), matrixCoordinateY(vir.getPointfs()[3]));
            }
        }
        virtualCanvas.drawPath(existvirtualPath, virtualPaint);
        if (MODE == MODE_DELETE_VIRTUAL) {//删除虚拟墙模式，需要画出减号删除键
            RectF rectF;
            for (VirtualWallBean vir : virtualWallBeans) {
                if (vir.getState() != 3) {
                    float l = matrixCoordinateX((vir.getPointfs()[0] + vir.getPointfs()[2]) / 2f);
                    float t = matrixCoordinateY((vir.getPointfs()[1] + vir.getPointfs()[3]) / 2f);
                    float r = l + deleteBitmap.getWidth() * scare;
                    float b = t + deleteBitmap.getHeight() * scare;
                    virtualCanvas.drawBitmap(deleteBitmap, l, t, virtualPaint);
                    rectF = new RectF(l, t, r, b);
                    vir.setDeleteIcon(rectF);
                }
            }

        }
        invalidate();
    }


    private int x, y, length, totalCount;

    /**
     * 从(0,1500)开始向上一行行绘制slam map
     */
    public void drawSlamMap(byte[] slamBytes) {
        if (slamBytes != null) {
            Log.d(TAG, "drawSlamMap-----" + slamBytes.length);
        }
        slamPath.reset();
        obstaclePath.reset();
        if (slamBytes != null && slamBytes.length > 0) {
            slamCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            slamCanvas.save();
            for (int i = 0; i < slamBytes.length; i += 3) {
                Path path;
                byte attr = slamBytes[i];
                if (attr == 0x01) {
                    path = obstaclePath;
                } else {
                    path = slamPath;
                }
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
//                            path.moveTo(matrixCoordinateX(x), matrixCoordinateY(1500 - y - 1));
//                            path.addRect(matrixCoordinateX(x - 1), matrixCoordinateY(1500 - y - 1),
//                                    matrixCoordinateX(x), matrixCoordinateY(1500 - y), Path.Direction.CCW);
                            int distance = (int) (matrixCoordinateY(1500 - y) - matrixCoordinateY(1500 - y - 1));
                            for (int k = 0; k < distance; k++) {
                                path.moveTo(matrixCoordinateX(x - 1), matrixCoordinateY(1500 - y) + k);
                                path.lineTo(matrixCoordinateX(x), matrixCoordinateY(1500 - y) + k);
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
        slamCanvas.drawPath(slamPath, slamPaint);
    }
}
