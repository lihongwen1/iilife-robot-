package com.ilife.iliferobot.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.ilife.iliferobot.R;
import com.ilife.iliferobot.app.MyApplication;
import com.ilife.iliferobot.model.SlamLineBean;
import com.ilife.iliferobot.model.VirtualWallBean;
import com.ilife.iliferobot.utils.BitmapUtils;
import com.ilife.iliferobot.utils.DataUtils;
import com.ilife.iliferobot.utils.MyLogger;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.utils.Utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//TODO 2.电子墙集合需要增加锁机制，避免数据错乱；
//TODO 2.移动导致绘图卡顿的bug;3.800updateslam异常
public class MapView extends View {
    private static final int TYPE_DRAW_CONDITION = 1;
    private static final int TYPE_DRAW_NOW = 2;
    private int width, height;
    private static String TAG = "MapView";
    private Paint slamPaint, roadPaint, virtualPaint, positionCirclePaint;
    private Path roadPath, existVirtualPath, slamPath, obstaclePath, boxPath;
    private float downX, downY;
    private float beforeDistance;
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
    private Bitmap roadBitmap, slagBitmap;
    private Canvas roadCanvas, slamCanvas;
    private boolean isInitBuffer = false;
    private Matrix matrix;
    private float dragX, originalDragX, dragY, originalDragY;
    private final int DEFAULT_SIZE = 1500;
    private float deviationX, deviationY;
    private int[] colors;
    private List<VirtualWallBean> virtualWallBeans;
    private static final int MIN_WALL_LENGTH = 20;
    private Bitmap deleteBitmap;//删除电子墙的bitmap
    private static final int deleteIconW = 36;
    private List<RectF> deleteIconRectFs = new ArrayList<>(10);

    private Canvas boxCanvas;
    private Bitmap boxBitmap;
    private Paint boxPaint;
    private RectF curVirtualWall = new RectF();
    private ArrayList<Integer> pointList = new ArrayList<>();
    private int endPositionRadius = 6;
    private int paddingBottom;

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
        existVirtualPath = new Path();
        slamPath = new Path();
        obstaclePath = new Path();
        deleteBitmap = BitmapUtils.decodeSampledBitmapFromResource(getResources(), R.drawable.n_icon_delete_virtual, deleteIconW, deleteIconW);


        slamPaint = new Paint();
        slamPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        slamPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        slamPaint.setFilterBitmap(true);
        slamPaint.setStrokeJoin(Paint.Join.ROUND);
        slamPaint.setStrokeCap(Paint.Cap.ROUND);
        slamPaint.setColor(colors[1]);
        slamPaint.setStrokeWidth(1f);
        positionCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        positionCirclePaint.setStyle(Paint.Style.FILL);
        positionCirclePaint.setFilterBitmap(true);
        positionCirclePaint.setStrokeJoin(Paint.Join.ROUND);
        positionCirclePaint.setStrokeCap(Paint.Cap.ROUND);
        positionCirclePaint.setColor(getResources().getColor(R.color.color_ef8200));
        positionCirclePaint.setStrokeWidth(1f);


        roadPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        roadPaint.setStyle(Paint.Style.STROKE);
        roadPaint.setFilterBitmap(true);
        roadPaint.setStrokeJoin(Paint.Join.ROUND);
        roadPaint.setStrokeCap(Paint.Cap.ROUND);
        roadPaint.setColor(getResources().getColor(R.color.white));
        roadPaint.setStrokeWidth(4f);

        virtualPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        virtualPaint.setStyle(Paint.Style.STROKE);
        virtualPaint.setFilterBitmap(true);
        virtualPaint.setStrokeJoin(Paint.Join.ROUND);
        virtualPaint.setStrokeCap(Paint.Cap.ROUND);
        virtualPaint.setColor(getResources().getColor(R.color.color_f08300));
        virtualPaint.setStrokeWidth(3f);

        boxPaint = new Paint();
        boxPath = new Path();

        /**
         * 电子墙路径集合
         */
        virtualWallBeans = new ArrayList<>();
    }

    /**
     * 设置地图模式
     *
     * @param MODE MODE_VIRTUAL 电子墙编辑模式
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
        boxBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        roadCanvas = new Canvas(roadBitmap);
        slamCanvas = new Canvas(slagBitmap);
        boxCanvas = new Canvas(boxBitmap);
        sCenter.set(width / 2f, height / 2f);
    }

    /**
     * 绘制路线地图，包含历史地图/即时地图
     * y坐标没有按照实际坐标绘制
     *
     * @param roadList
     */
    public void drawRoadMap(ArrayList<Integer> roadList, ArrayList<Integer> historyRoadList) {
        MyLogger.d(TAG, "drawRoadMap-----");
        roadPath.reset();
        roadCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        roadCanvas.save();
        float startX = 0, startY = 0, endX = 0, endY = 0;//标记起点坐标
//        绘制最新路径坐标点，下一条路径的起始坐标为上 一条路径的终点坐标
        boolean flag = false;//新包数据标记,用 x,y=400标记
        if (roadList != null && roadList.size() > 0) {
            for (int k = 0; k < roadList.size() - 1; k += 2) {
                if (k == 0) {
                    startX = matrixCoordinateX(roadList.get(0));
                    startY = matrixCoordinateY(1500 - roadList.get(1));
                    roadPath.moveTo(startX, startY);//设置起点
                } else {
                    if (roadList.get(k) == 400 && roadList.get(k + 1) == 400) {
                        flag = true;
                        MyLogger.d(TAG, "new package data is access");
                        continue;
                    }
                    if (flag) {
                        flag = false;
                        roadPath.moveTo(matrixCoordinateX(roadList.get(k)), matrixCoordinateY(1500 - roadList.get(k + 1)));
                    } else {
                        roadPath.lineTo(matrixCoordinateX(roadList.get(k)), matrixCoordinateY(1500 - roadList.get(k + 1)));
                    }
                }
            }
        }
//        绘制历史路径坐标点，下一条路径的起始坐标为上 一条路径的终点坐标
        if (historyRoadList != null && historyRoadList.size() > 0) {
            for (int k = 0; k < historyRoadList.size() - 1; k += 2) {
                if (k == historyRoadList.size() - 2) {
                    endX = matrixCoordinateX(historyRoadList.get(k));
                    endY = matrixCoordinateY(1500 - historyRoadList.get(k + 1));
                }
                if (k == 0) {
                    startX = matrixCoordinateX(historyRoadList.get(0));
                    startY = matrixCoordinateY(1500 - historyRoadList.get(1));
                    roadPath.moveTo(startX, startY);//设置起点
                } else {
                    if (historyRoadList.get(k) == 400 && historyRoadList.get(k + 1) == 400) {
                        flag = true;
                        MyLogger.d(TAG, "new package data is arrived");
                        continue;
                    }
                    if (flag) {
                        flag = false;
                        roadPath.moveTo(matrixCoordinateX(historyRoadList.get(k)), matrixCoordinateY(1500 - historyRoadList.get(k + 1)));
                    } else {
                        roadPath.lineTo(matrixCoordinateX(historyRoadList.get(k)), matrixCoordinateY(1500 - historyRoadList.get(k + 1)));
                    }
                }
            }
        }
        roadCanvas.drawPath(roadPath, roadPaint);
        if (startX != 0 && startY != 0) {
            positionCirclePaint.setColor(getResources().getColor(R.color.white));
            roadCanvas.drawCircle(startX, startY, Utils.dip2px(MyApplication.getInstance(), 4), positionCirclePaint);
        }
        if (roadList != null && roadList.size() > 2) {
            endY = matrixCoordinateY(1500 - roadList.get(roadList.size() - 1));
            endX = matrixCoordinateX(roadList.get(roadList.size() - 2));
            positionCirclePaint.setColor(getResources().getColor(R.color.color_f08300));
            roadCanvas.drawCircle(endX, endY, Utils.dip2px(MyApplication.getInstance(), endPositionRadius), positionCirclePaint);
        } else if (endX != 0 && endY != 0) {
            positionCirclePaint.setColor(getResources().getColor(R.color.color_f08300));
            roadCanvas.drawCircle(endX, endY, Utils.dip2px(MyApplication.getInstance(), endPositionRadius), positionCirclePaint);
        }

        invalidateUI(TYPE_DRAW_CONDITION);
    }

    /**
     * 绘制障碍物点
     */
    public void drawObstacle() {
        slamPaint.setColor(colors[1]);
        slamCanvas.drawPath(slamPath, slamPaint);
        slamPaint.setColor(colors[0]);
        slamCanvas.drawPath(obstaclePath, slamPaint);
        invalidateUI(TYPE_DRAW_CONDITION);
    }

    /**
     * 清除所有绘图痕迹
     */
    public void clean() {
        deleteIconRectFs.clear();
        existVirtualPath.reset();
        slamCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        roadCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        boxCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        invalidateUI(TYPE_DRAW_CONDITION);
    }

    public void setPaddingBottom(int paddingBottom) {
        if (this.paddingBottom == 0) {
            this.paddingBottom = paddingBottom;
            sCenter.set(width / 2f, (height - paddingBottom) / 2f);
        }
    }

    /**
     * @param xMin
     * @param xMax
     * @param yMin
     * @param yMax
     */
    // TODO make sure the map is in the center of the screen  and it will be drawn all details ,there is an error that the genuine coordinate is less than the value 'xmin'
    public void updateSlam(int xMin, int xMax, int yMin, int yMax, int maxScare) {
        int xLength = xMax - xMin;
        int yLength = yMax - yMin;
        if (xLength <= 0 || yLength <= 0) {
            return;
        }
        double resultX = width * 0.80f / xLength;
        double resultY = (height - paddingBottom) * 0.80f / yLength;
        BigDecimal bigDecimal = new BigDecimal(Math.min(resultX, resultY)).setScale(1, BigDecimal.ROUND_HALF_UP);
        baseScare = Math.round(bigDecimal.floatValue());
        if (baseScare >= maxScare) {
            baseScare = maxScare;
        }
        if (baseScare < 3.0f) {
            roadPaint.setStrokeWidth(3f);
        } else {
            roadPaint.setStrokeWidth(4f);
        }
        MyLogger.d(TAG, "updateSlam---" + xMin + "---" + xMax + "---" + yMin + "---" + yMax + "---width:---" + width + "---height:---" + height + "baseScare:---" + baseScare);
        deviationX = (xMin + xMax) / 2f * baseScare - width / 2f;
        deviationY = (yMax + yMin) / 2f * baseScare - (height - paddingBottom) / 2f;
        MyLogger.d(TAG, "deviationX" + deviationX + "---" + "deviationY" + deviationY);
    }


    /**
     * s
     *
     * @param originalCoordinate
     * @return
     */
    private float matrixCoordinateX(float originalCoordinate) {
        float value = originalCoordinate * baseScare - deviationX;
        return value;
    }

    private float reMatrixCoordinateX(float originalCoordinate) {
        float value = (originalCoordinate + deviationX) / baseScare;
        return value;
    }

    /**
     * s
     *
     * @param originalCoordinate
     * @return
     */
    private float matrixCoordinateY(float originalCoordinate) {
        float value = originalCoordinate * baseScare - deviationY;
        return value;
    }

    private float reMatrixCoordinateY(float originalCoordinate) {
        float value = (originalCoordinate + deviationY) / baseScare;
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
        MyLogger.d("SelfPaint", "Scare" + scare + "---tansX:" + dragX + "---" + dragY + "--sx" + sCenter.x + "---sy" + sCenter.y);
        matrix.postScale(scare, scare, sCenter.x, sCenter.y);
        canvas.drawBitmap(slagBitmap, matrix, slamPaint);
        canvas.drawBitmap(roadBitmap, matrix, roadPaint);
        canvas.drawBitmap(boxBitmap, matrix, boxPaint);
        //绘制电子墙
        canvas.setMatrix(matrix);
        canvas.drawPath(existVirtualPath, virtualPaint);
        if (MODE == MODE_DELETE_VIRTUAL) {
            for (RectF rf : deleteIconRectFs) {
                canvas.drawBitmap(deleteBitmap, rf.left, rf.top, virtualPaint);
            }
        }
        if (curVirtualWall.left != 0) {
            canvas.drawLine(curVirtualWall.left, curVirtualWall.top, curVirtualWall.right, curVirtualWall.bottom, virtualPaint);
        }
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
    ////删除电子墙的时候支持拖动,添加电子墙的时候支持缩放，NONE时支持缩放和拖动
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int me = event.getAction() & MotionEvent.ACTION_MASK;
        float x = event.getX() / scare + getOffsetX();
        float y = event.getY() / scare + getOffsetY();
        switch (me) {
            case MotionEvent.ACTION_CANCEL:
                MODE = originalMode;
            case MotionEvent.ACTION_DOWN:
                downX = x;
                downY = y;
                downPoint.set(event.getX(), event.getY());
                if (MODE == MODE_ADD_VIRTUAL) {
                    // 添加电子墙
                } else if (MODE == MODE_DELETE_VIRTUAL) {
                    //  删除电子墙
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
                    float distance = distance(downX, downY, x, y);
                    if (distance > MIN_WALL_LENGTH) {
                        curVirtualWall.set(downX, downY, x, y);
                    }
                }
                invalidateUI(TYPE_DRAW_NOW);
                break;
            case MotionEvent.ACTION_UP:
                curVirtualWall.setEmpty();
                if (MODE == MODE_NONE) {
//                    roadPath.addCircle(x, y, 5, Path.Direction.CCW);
//                    roadBitmap.eraseColor(Color.TRANSPARENT);
//                    roadCanvas.drawPath(roadPath, slamPaint);
//                    invalidate();
                } else if (MODE == MODE_ADD_VIRTUAL) {
                    if (getUsefulWallNum() >= 10) {
                        // TODO 提示电子墙数量达到最大值
                        ToastUtils.showToast(Utils.getString(R.string.map_aty_max_count));
//                        virtualCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//                        virtualCanvas.save();
//                        virtualCanvas.drawPath(existVirtualPath, virtualPaint);
                    } else {
                        float distance = distance(downX, downY, x, y);
                        if (distance < MIN_WALL_LENGTH) {
                        } else {
                            existVirtualPath.moveTo(downX, downY);
                            existVirtualPath.lineTo(x, y);//加入到已存在的电子墙集合中去
                            VirtualWallBean virtualWallBean = new VirtualWallBean(virtualWallBeans.size() + 1, new int[]{(int) reMatrixCoordinateX(downX), (int) reMatrixCoordinateY(downY), (int) reMatrixCoordinateX(x), (int) reMatrixCoordinateY(y)}
                                    , 2);
                            virtualWallBeans.add(virtualWallBean);
                            drawVirtualWall();
                        }
                    }
                } else if (MODE == MODE_DELETE_VIRTUAL) {
                    //TODO delete  virtual wall
                    Iterator<VirtualWallBean> iterator = virtualWallBeans.iterator();
                    VirtualWallBean vr;
                    while (iterator.hasNext()) {
                        vr = iterator.next();
                        if (vr.getDeleteIcon().contains(x, y)) {
//                            ToastUtils.showToast("删除第" + vr.getNumber() + "条电子墙");
                            if (vr.getState() == 2) {//新增的电子墙，还未保存到服务器，可以直接移除
                                if (deleteIconRectFs.size() > vr.getNumber() - 1) {
                                    deleteIconRectFs.remove(vr.getNumber() - 1);
                                }
                                virtualWallBeans.remove(vr);
                            }
                            if (vr.getState() == 1) {//服务器上的电子墙，可能操作会被取消掉，只需要改变状态
                                vr.setState(3);
                            }
                            drawVirtualWall();
                            break;
                        }
                    }
                    //删除电子墙的时候支持拖动
                    originalDragX = dragX;
                    originalDragY = dragY;
                } else if (MODE == MODE_DRAG) {
                    originalDragX = dragX;
                    originalDragY = dragY;
                }
                MODE = originalMode;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                MODE = originalMode;
                originalScare = scare;
                break;
            case MotionEvent.ACTION_POINTER_DOWN://多指DOWN
                if (event.getPointerCount() == 2) {
                    MODE = MODE_ZOOM;
                    beforeDistance = distance(event);
                }
                break;

        }
        return true;
    }

    private void calculateScare(MotionEvent event) {
        float afterDistance = distance(event);
        if (Math.abs(afterDistance - beforeDistance) > 10) {
//            sCenter = midPoint(event);
            scare = (afterDistance / beforeDistance) * originalScare;
            MyLogger.d("scare", "---" + scare);
            if (scare < 0.6f) {
                scare = 0.6f;
            }
            if (scare > 4.5f) {
                scare = 4.5f;
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

    private int getUsefulWallNum() {
        int num = 0;
        for (VirtualWallBean vb : virtualWallBeans) {
            if (vb.getState() == 1 || vb.getState() == 2) {
                num++;
            }
        }
        MyLogger.d(TAG, "useful wall number:" + num);
        return num;
    }

    /**
     * 撤销所有电子墙操作，恢复到与服务器数据一致的状态
     */
    public void undoAllOperation() {
        if (virtualWallBeans != null && virtualWallBeans.size() > 0) {
            Iterator<VirtualWallBean> iterator = virtualWallBeans.iterator();
            while (iterator.hasNext()) {
                VirtualWallBean virtualWallBean = iterator.next();
                if (virtualWallBean.getState() == 2) {
                    iterator.remove();
                } else if (virtualWallBean.getState() == 3) {//被置为待删除的服务器电子墙恢复状态
                    virtualWallBean.setState(1);
                }
            }
        }
        drawVirtualWall();
    }

    /**
     * 获取电子墙列表,包含新增，和删除
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
     * 查询到服务其电子墙数据后调用绘制电子墙
     *
     * @param existPointList 服务器电子墙数据集合
     */
    public void drawVirtualWall(List<int[]> existPointList) {
        if (existPointList == null) {
            drawVirtualWall();//Represents when the map is updating
        } else if (existPointList.size() == 0) {
            virtualWallBeans.clear();
            drawVirtualWall();
        } else {
            virtualWallBeans.clear();
            VirtualWallBean bean;
            for (int i = 0; i < existPointList.size(); i++) {
                bean = new VirtualWallBean(i + 1, existPointList.get(i), 1);
                virtualWallBeans.add(bean);
            }
            drawVirtualWall();
        }
    }

    /**
     * 绘制电子墙
     */
    public void drawVirtualWall() {
        if (virtualWallBeans == null) {
            return;
        }
        deleteIconRectFs.clear();
        existVirtualPath.reset();
        for (VirtualWallBean vir : virtualWallBeans) {
            if (vir.getState() != 3) {
                existVirtualPath.moveTo(matrixCoordinateX(vir.getPointfs()[0]), matrixCoordinateY(vir.getPointfs()[1]));
                existVirtualPath.lineTo(matrixCoordinateX(vir.getPointfs()[2]), matrixCoordinateY(vir.getPointfs()[3]));
            }
        }
        if (MODE == MODE_DELETE_VIRTUAL) {//删除电子墙模式，需要画出减号删除键
            RectF rectF;
            for (VirtualWallBean vir : virtualWallBeans) {
                if (vir.getState() != 3) {
                    float cx = matrixCoordinateX((vir.getPointfs()[0] + vir.getPointfs()[2]) / 2f);
                    float cy = matrixCoordinateY((vir.getPointfs()[1] + vir.getPointfs()[3]) / 2f);
                    float distance = 60;//偏移坐标中心点的距离
                    if ((matrixCoordinateX(vir.getPointfs()[2]) == matrixCoordinateX(vir.getPointfs()[0]))) {
                        cx -= distance;
                    } else {
                        float k = (matrixCoordinateY(vir.getPointfs()[3]) - matrixCoordinateY(vir.getPointfs()[1])) / (matrixCoordinateX(vir.getPointfs()[2])
                                - matrixCoordinateX(vir.getPointfs()[0]));
                        //
                        MyLogger.d(TAG, "tanx:" + k);

                        float translationY = (float) (distance * (Math.sqrt(1 + k * k) / (1 + k * k)));
                        float translationX = Math.abs(k) * translationY;

                        if (k > 0) {
                            cx += translationX;
                            cy -= translationY;
                        } else {
                            cx -= translationX;
                            cy -= translationY;
                        }
                    }
                    float l = cx - deleteBitmap.getWidth() * scare / 2;
                    float t = cy - deleteBitmap.getWidth() * scare / 2;
                    float r = l + deleteBitmap.getWidth() * scare;
                    float b = t + deleteBitmap.getHeight() * scare;
                    rectF = new RectF(l, t, r, b);
                    deleteIconRectFs.add(rectF);
                    vir.setDeleteIcon(rectF);
                }
            }

        }
        invalidateUI(TYPE_DRAW_NOW);
    }

    List<SlamLineBean> lastLineBeans = new ArrayList<>();

    /**
     * 从(0,1500)开始向上一行行绘制slam map
     */
    public void drawSlamMap(byte[] slamBytes) {
        if (slamCanvas == null) {
            isInitBuffer = false;
            initBuffer();
        }
        int x = 0, y = 0, length, totalCount = 0;
        if (slamBytes != null && slamBytes.length > 0) {
            slamPath.reset();
            obstaclePath.reset();
            slamCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            slamCanvas.save();
            for (int i = 0; i < slamBytes.length; i += 3) {
                byte attr = slamBytes[i];
                length = DataUtils.bytesToInt2(new byte[]{slamBytes[i + 1], slamBytes[i + 2]}, 0);
                slamPaint.setColor(colors[attr - 1]);
                int distanceTo1500 = 1500 - totalCount;
                switch (attr) {
                    case 0x03://未探索区域
                        if (length > 1500) {
                            duplicateLine(y);//避免线段的结尾和新线段的开始部分是未探索区域，导致slam绘制不完整
                            y += length / 1500;
                            length = length % 1500;
                        }
                        if (length < distanceTo1500) {
                            x += length;
                            totalCount += length;
                        } else {
                            x = length - distanceTo1500;
                            duplicateLine(y);
                            y++;
                            totalCount = x;
                        }
                        break;
                    case 0x01://障碍物
                    case 0x02://已探索区域
                        if (length > 1500) {
                            duplicateLine(y);
                            for (int j = 0; j < length / 1500; j++) {
                                y++;
                                lastLineBeans.add(new SlamLineBean(attr, 0, x));
                            }
                            length = length % 1500;
                        }

                        if (length < distanceTo1500) {
                            x += length;
                            totalCount += length;
                            lastLineBeans.add(new SlamLineBean(attr, x - length, x));
                        } else {
                            lastLineBeans.add(new SlamLineBean(attr, x, 1500));
                            duplicateLine(y);
                            y++;
                            x = length - distanceTo1500;
                            lastLineBeans.add(new SlamLineBean(attr, 0, x));
                            totalCount = x;
                        }
                        break;
                }
            }
        }
    }


    /**
     * 复制startY行的所有线段路径
     *
     * @param startY
     */
    private void duplicateLine(int startY) {
        if (lastLineBeans == null || lastLineBeans.size() == 0) {
            return;
        }
        //切换到下一行绘制
        for (int j = 0; j < baseScare; j++) {
            for (SlamLineBean slb : lastLineBeans) {
                Path realPath = slb.getType() == 0x01 ? obstaclePath : slamPath;
                realPath.moveTo(matrixCoordinateX(slb.getStartX()), matrixCoordinateY(1500 - startY) + j);
                realPath.lineTo(matrixCoordinateX(slb.getEndX()), matrixCoordinateY(1500 - startY) + j);
            }
        }
        lastLineBeans.clear();//清空所有线的数据
    }


    /**
     * 绘制x800的黄方格地图
     *
     * @param dataList
     */
    public void drawBoxMapX8(ArrayList<Integer> dataList) {
        if (dataList == null) {
            return;
        }
        if (dataList.size() == 0) {
            boxPath.reset();
            boxCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            invalidateUI(TYPE_DRAW_CONDITION);
            return;
        }
        pointList.clear();
        pointList.addAll(dataList);
        float endY, endX;
        int x, y;
        boxCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        boxPath.reset();
        //绘制清扫区域的白方格
        boxPaint.setColor(getResources().getColor(R.color.white));
        boxPaint.setStrokeWidth(1);
        if (pointList.size() > 0) {
            for (int i = 1; i < pointList.size(); i += 2) {
                x = -pointList.get(i - 1);
                y = -pointList.get(i);
                boxPath.addRect(matrixCoordinateX(x), height - matrixCoordinateY(y), matrixCoordinateX(x) + baseScare - 2, height - matrixCoordinateY(y) + baseScare - 2, Path.Direction.CCW);
            }
        }
        boxCanvas.drawPath(boxPath, boxPaint);
        endY = height - matrixCoordinateY(-pointList.get(pointList.size() - 1));
        endX = matrixCoordinateX(-pointList.get(pointList.size() - 2));
        positionCirclePaint.setColor(getResources().getColor(R.color.color_f08300));
        boxCanvas.drawCircle(endX, endY, Utils.dip2px(MyApplication.getInstance(), endPositionRadius), positionCirclePaint);
        invalidateUI(TYPE_DRAW_CONDITION);
    }

    private void invalidateUI(int type) {
        invalidate();
//        switch (type) {
//            case TYPE_DRAW_CONDITION:
//                if (MODE == MODE_NONE) {
//                    invalidate();
//                }else {
//                    MyLogger.d(TAG,"the redraw request is ignored ");
//                }
//                break;
//            case TYPE_DRAW_NOW:
//                invalidate();
//                break;
//        }
    }
}
