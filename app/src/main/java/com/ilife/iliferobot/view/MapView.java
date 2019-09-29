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
import com.ilife.iliferobot.model.bean.SlamLineBean;
import com.ilife.iliferobot.model.bean.VirtualWallBean;
import com.ilife.iliferobot.utils.BitmapUtils;
import com.ilife.iliferobot.utils.DataUtils;
import com.ilife.iliferobot.utils.MyLogger;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.utils.Utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 900系列采用直接繪製s
 */
public class MapView extends View {
    private int width, height;
    private static String TAG = "MapView";
    private Paint slamPaint, roadPaint, virtualPaint, positionCirclePaint;
    private Path roadPath, existVirtualPath, slamPath, obstaclePath, boxPath;
    private float beforeDistance;
    public static final int MODE_NONE = 1;
    public static final int MODE_DRAG = 2;
    public static final int MODE_ZOOM = 3;
    public static final int MODE_ADD_VIRTUAL = 4;
    public static final int MODE_DELETE_VIRTUAL = 5;
    private int MODE;
    private float originalScale = 1, userScale = 1, systemScale = 1;//user-scale is the scale value generate by user scale the screen
    private float baseScare = 1;//基准坐标缩放比例，以slam边缘占据view3/4，与view同坐标中心为标准

    private PointF sCenter, downPoint;//sCenter map缩放中心
    private Matrix matrix;
    private float dragX, dragY;
    private int[] colors;
    private List<VirtualWallBean> virtualWallBeans;
    private static final int MIN_WALL_LENGTH = 20;
    private Bitmap deleteBitmap;//删除电子墙的bitmap
    private static final int deleteIconW = 36;
    private List<RectF> deleteIconRectFs = new ArrayList<>(10);
    private List<SlamLineBean> lastLineBeans = new ArrayList<>();
    private Paint boxPaint;
    private RectF curVirtualWall = new RectF();
    private ArrayList<Integer> pointList = new ArrayList<>();
    private boolean isSetExtraDrag;
    private float startX = 0, startY = 0, endX = 0, endY = 0;
    private Canvas boxCanvas, slamCanvas;
    private Bitmap boxBitmap, slamBitmap;
    private RectF slamRect = new RectF();
    private boolean robotSeriesX9;
    private boolean unconditionalRecreate;
    private int extraWH = 60;//额外的宽高
    private Runnable restoreRunnable;//控制延迟15s后恢复地图
    private boolean isNeedRestore = true;
    private  int paddingBottom;//改变地图居中中心点
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

    public void setNeedRestore(boolean needRestore) {
        isNeedRestore = needRestore;
    }

    public void setRobotSeriesX9(boolean isX9) {
        this.robotSeriesX9 = isX9;
        if (isX9) {
            extraWH = 100;
        } else {
            extraWH = 60;
        }
    }

    private void init() {
        colors = new int[]{getResources().getColor(R.color.obstacle_color), getResources().getColor(R.color.slam_color),
                getResources().getColor(R.color.color_00ffffff)};
        MODE = MODE_NONE;
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
        slamPaint.setColor(colors[1]);
        slamPaint.setStrokeWidth(4f);
        positionCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        positionCirclePaint.setStyle(Paint.Style.FILL);
        positionCirclePaint.setFilterBitmap(true);
        positionCirclePaint.setStrokeJoin(Paint.Join.ROUND);
        positionCirclePaint.setColor(getResources().getColor(R.color.color_ef8200));
        positionCirclePaint.setStrokeWidth(1f);


        roadPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        roadPaint.setStyle(Paint.Style.STROKE);
        roadPaint.setFilterBitmap(true);
        roadPaint.setStrokeJoin(Paint.Join.ROUND);
        roadPaint.setColor(getResources().getColor(R.color.white));
        roadPaint.setStrokeWidth(4f);

        virtualPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        virtualPaint.setStyle(Paint.Style.STROKE);
        virtualPaint.setFilterBitmap(true);
        virtualPaint.setStrokeJoin(Paint.Join.ROUND);
        virtualPaint.setColor(getResources().getColor(R.color.color_f08300));
        virtualPaint.setStrokeWidth(3f);

        boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        boxPaint.setStyle(Paint.Style.FILL);
        boxPaint.setFilterBitmap(true);
        /**
         * 电子墙路径集合
         */
        virtualWallBeans = new ArrayList<>();
        boxPath = new Path();
    }

    /**
     * 无条件刷新创建bitmap
     *
     * @param unconditionalRecreate
     */
    public void setUnconditionalRecreate(boolean unconditionalRecreate) {
        this.unconditionalRecreate = unconditionalRecreate;
        userScale = 1;
        originalScale = userScale;
    }


    /**
     * 设置地图模式
     *
     * @param MODE MODE_VIRTUAL 电子墙编辑模式
     */
    public void setMODE(int MODE) {
        this.MODE = MODE;
        if (MODE == MODE_ADD_VIRTUAL || MODE == MODE_DELETE_VIRTUAL) {
            drawVirtualWall();
        }
    }

    public boolean isInMode(int mode) {
        return mode == MODE;
    }


    /**
     * 绘制路线地图，包含历史地图/即时地图
     * y坐标没有按照实际坐标绘制，1500-y
     *
     * @param roadList        事实地图数据
     * @param historyRoadList 历史地图数据
     */
    private void drawRoadMap(ArrayList<Integer> roadList, ArrayList<Integer> historyRoadList) {
        roadPath.reset();
        MyLogger.d(TAG, "drawRoadMap-----");
        List<Integer> allPoint = new ArrayList<>();
        if (historyRoadList != null) {
            allPoint.addAll(historyRoadList);
        }
        if (roadList != null) {
            allPoint.addAll(roadList);
        }
//        绘制历史路径坐标点，下一条路径的起始坐标为上 一条路径的终点坐标
        boolean flag = false;
        if (allPoint.size() > 0) {
            for (int k = 0; k < allPoint.size() - 1; k += 2) {
                if (k == allPoint.size() - 2) {
                    endX = matrixCoordinateX(allPoint.get(k));
                    endY = matrixCoordinateY(1500 - allPoint.get(k + 1));
                }
                if (k == 0) {
                    startX = matrixCoordinateX(allPoint.get(0));
                    startY = matrixCoordinateY(1500 - allPoint.get(1));
                    roadPath.moveTo(startX, startY);//设置起点
                } else {
                    if (allPoint.get(k) == 400 && allPoint.get(k + 1) == 400) {
                        flag = true;
                        MyLogger.d(TAG, "new package data is arrived");
                        continue;
                    }
                    if (flag) {
                        flag = false;
                        roadPath.moveTo(matrixCoordinateX(allPoint.get(k)), matrixCoordinateY(1500 - allPoint.get(k + 1)));
                    } else {
                        roadPath.lineTo(matrixCoordinateX(allPoint.get(k)), matrixCoordinateY(1500 - allPoint.get(k + 1)));
                    }
                }
            }
        }
    }


    /**
     * X900系列绘图
     *
     * @param roadList
     * @param historyRoadList
     * @param slamBytes
     */
    public void drawMapX9(ArrayList<Integer> roadList, ArrayList<Integer> historyRoadList, byte[] slamBytes) {
        if (slamBitmap == null && slamCanvas == null) {
            return;
        }
        //drawing white road line
        drawRoadMap(roadList, historyRoadList);
        //drawing slam map
        if (slamBytes != null) {//900系列绘制路径时不绘制slam
            drawSlamMap(slamBytes);
        }
        slamCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        slamCanvas.save();
        slamCanvas.drawColor(getResources().getColor(R.color.colorPrimary));
        slamPaint.setColor(colors[1]);
        slamCanvas.drawPath(slamPath, slamPaint);
        slamPaint.setColor(colors[0]);
        slamCanvas.drawPath(obstaclePath, slamPaint);
        slamCanvas.drawPath(roadPath, roadPaint);
        //draw start point with green color
        if (startX != 0 || startY != 0) {
            positionCirclePaint.setColor(getResources().getColor(R.color.start_point));
            slamCanvas.drawCircle(startX, startY, Utils.dip2px(MyApplication.getInstance(), 4), positionCirclePaint);
        }
        //draw end point with yellow color
        if (endX != 0 || endY != 0) {
            positionCirclePaint.setColor(getResources().getColor(R.color.color_f08300));
            slamCanvas.drawCircle(endX, endY, Utils.dip2px(MyApplication.getInstance(), 6), positionCirclePaint);
        }
        invalidateUI();
    }

    public void drawMapX8(ArrayList<Integer> dataList) {
        MyLogger.d(TAG, "----------drawMapX8---------");
        drawBoxMapX8(dataList);
        //draw end point with yellow color
//        boxCanvas.drawColor(getResources().getColor(R.color.colorPrimary));
        boxCanvas.drawPath(boxPath, boxPaint);
        if (endX != 0 || endY != 0) {
            positionCirclePaint.setColor(getResources().getColor(R.color.color_f08300));
            boxCanvas.drawCircle(endX, endY, Utils.dip2px(MyApplication.getInstance(), 12), positionCirclePaint);
        }
        invalidateUI();
    }


    /**
     * 清除所有绘图痕迹
     */
    public void clean() {
        deleteIconRectFs.clear();
        existVirtualPath.reset();
        roadPath.reset();
        slamPath.reset();
        boxPath.reset();
        if (slamCanvas != null) {
            slamCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
        if (boxCanvas != null) {
            boxCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
        invalidateUI();
    }

    public void resetCenter(int paddingBottom) {
        if (!isSetExtraDrag) {
            isSetExtraDrag = true;
            this.paddingBottom=paddingBottom;
            sCenter.set(width / 2f, (height - paddingBottom) / 2f);
        }
    }


    /**
     * @param xMin
     * @param xMax
     * @param yMin
     * @param yMax
     */
    // TODO it should't be created ,when it's size is less than the old
    //TODO  judge if you need redraw the virtual walls
    //TODO 清扫完成后recreate the bitmap
    public void updateSlam(int xMin, int xMax, int yMin, int yMax) {
        int xLength = xMax - xMin;
        int yLength = yMax - yMin;
        if (xLength <= 0 || yLength <= 0) {
            return;
        }
        if (robotSeriesX9) {
            baseScare = 10.0f;
            if (xLength * baseScare > width || yLength * baseScare > sCenter.y * 2) {
                MyLogger.d(TAG, "SYSTEM SCALE MAP -------------");
                float systemW = (width * 0.9f) / ((xLength * baseScare));
                float systemH = (sCenter.y * 2 * 0.9f) / ((yLength * baseScare));
                systemScale = Math.min(systemH, systemW);
                systemScale = new BigDecimal(systemScale).setScale(2, BigDecimal.ROUND_HALF_DOWN).floatValue();
                if (systemScale < 0.3f) {
                    baseScare = new BigDecimal(baseScare * systemScale / 0.3f).setScale(0, BigDecimal.ROUND_HALF_DOWN).floatValue();
                    systemScale = 0.3f;
                }
            }
        } else {
            baseScare = 30;
            if (xLength * baseScare > width * 1.6f || yLength * baseScare > height * 1.6f) {
                systemScale = new BigDecimal(caculateSystemScale(xLength, yLength, 30)).setScale(1, BigDecimal.ROUND_DOWN).floatValue();
            } else {
                systemScale = 0.5f;
            }
        }
        MyLogger.d(TAG, "xLength----" + xLength + "----yLength-----" + yLength+"---------height"+height+"---------width--------"+width);
        MyLogger.d(TAG, "--systemScale--:" + systemScale + "------baseScare----:" + baseScare + "------userScale-------" + userScale);
        slamRect.set(xMin, yMin, xMax, yMax);
        if (robotSeriesX9) {
            int needWidth = (int) ((xMax - xMin) * baseScare) + extraWH;
            int needHeight = (int) ((yMax - yMin) * baseScare) + extraWH;
            if (slamCanvas == null && slamBitmap == null) {
                slamBitmap = Bitmap.createBitmap(needWidth, needHeight, Bitmap.Config.ARGB_8888);
                slamCanvas = new Canvas(slamBitmap);
            } else if (slamBitmap != null && ((needWidth != slamBitmap.getWidth() || needHeight != slamBitmap.getHeight()) || unconditionalRecreate)) {
                MyLogger.d(TAG, "reCreate the bitmap................");
                slamBitmap.recycle();
                slamBitmap = Bitmap.createBitmap(needWidth, needHeight, Bitmap.Config.ARGB_8888);
                slamCanvas.setBitmap(slamBitmap);
                unconditionalRecreate = false;
            }
            drawVirtualWall();//刷新虚拟墙
        } else {
            int needWidth = (int) ((xMax - xMin + 1) * baseScare) + extraWH;
            int needHeight = (int) ((yMax - yMin + 1) * baseScare) + extraWH;
            if (boxCanvas == null && boxBitmap == null) {
                boxBitmap = Bitmap.createBitmap(needWidth, needHeight, Bitmap.Config.ARGB_8888);
                boxCanvas = new Canvas(boxBitmap);
            } else if (boxBitmap != null && ((needWidth != boxBitmap.getWidth() || needHeight != boxBitmap.getHeight()) || unconditionalRecreate)) {
                MyLogger.d(TAG, "reCreate the bitmap................");
                boxBitmap.recycle();
                boxBitmap = Bitmap.createBitmap(needWidth, needHeight, Bitmap.Config.ARGB_8888);
                boxCanvas.setBitmap(boxBitmap);
                unconditionalRecreate = false;
            }
        }

    }

    private float caculateSystemScale(int xLength, int yLength, int scale) {
        float systemW = (width * 0.9f) / ((xLength * scale));
        float systemH = (height * 0.9f) / ((yLength * scale));
        float value = Math.min(systemH, systemW);
        if (value > 0.3f || scale - 10 == 0) {
            baseScare = scale;
            return value;
        } else {
            return caculateSystemScale(xLength, yLength, scale - 10);
        }
    }

    /**
     * s
     *
     * @param originalCoordinate
     * @return
     */
    private float matrixCoordinateX(float originalCoordinate) {
        return (originalCoordinate - slamRect.left) * baseScare + extraWH / 2f;
    }

    private float reMatrixCoordinateX(float originalCoordinate) {
        return (originalCoordinate - extraWH / 2f) / baseScare + slamRect.left;
    }

    /**
     * s
     *
     * @param originalCoordinate
     * @return
     */
    private float matrixCoordinateY(float originalCoordinate) {
        return (originalCoordinate - slamRect.top) * baseScare + extraWH / 2f;
    }

    private float reMatrixCoordinateY(float originalCoordinate) {
        return (originalCoordinate - extraWH / 2f) / baseScare + slamRect.top;
    }


    /**
     * roadPath layer matrix
     *
     * @param canvas
     */
    // TODO 手势缩放移动的时候延迟刷新实时地图数据/或者生成刷新队列
    @Override
    protected void onDraw(Canvas canvas) {
        if (!robotSeriesX9) {//X800 series
            if (boxCanvas != null && boxBitmap != null) {
                matrix.reset();
                matrix.postTranslate(dragX + sCenter.x - boxBitmap.getWidth() / 2f, dragY + sCenter.y - boxBitmap.getHeight() / 2f);
                matrix.postScale(getRealScare(), getRealScare(), sCenter.x, sCenter.y);
                canvas.drawBitmap(boxBitmap, matrix, boxPaint);
            }
        } else {//x900 series
            if (slamCanvas != null && slamBitmap != null) {
                matrix.reset();
                matrix.postTranslate(dragX + sCenter.x - slamBitmap.getWidth() / 2f, dragY + sCenter.y - slamBitmap.getHeight() / 2f);
                matrix.postScale(getRealScare(), getRealScare(), sCenter.x, sCenter.y);
                canvas.drawBitmap(slamBitmap, matrix, null);
                /**
                 * draw virtual wall
                 */
                canvas.concat(matrix);
                canvas.drawPath(existVirtualPath, virtualPaint);

                if (MODE == MODE_DELETE_VIRTUAL || MODE == MODE_DELETE_VIRTUAL * MODE_DRAG || MODE == MODE_DELETE_VIRTUAL * MODE_ZOOM) {
                    for (RectF rf : deleteIconRectFs) {
                        canvas.drawBitmap(deleteBitmap, rf.left, rf.top, virtualPaint);
                    }
                }
                if (curVirtualWall.left != 0) {
                    canvas.drawLine(curVirtualWall.left, curVirtualWall.top, curVirtualWall.right, curVirtualWall.bottom, virtualPaint);
                }

            }
        }

        super.onDraw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        sCenter.set(width / 2f, (height - paddingBottom) / 2f);
    }

    private float getRealScare() {
        return userScale * systemScale;
    }

    private float getOffsetX() {
        float actualDragX;
        if (robotSeriesX9) {
            actualDragX = dragX + sCenter.x - (slamBitmap == null ? 0 : slamBitmap.getWidth() / 2f);
        } else {
            actualDragX = dragX + sCenter.x - (boxBitmap == null ? 0 : boxBitmap.getWidth() / 2f);
        }
        return sCenter.x * (getRealScare() - 1) / getRealScare() - actualDragX;
    }

    private float getOffsetY() {
        float actualDragY;
        if (robotSeriesX9) {
            actualDragY = dragY + sCenter.y - (slamBitmap == null ? 0 : slamBitmap.getHeight() / 2f);
        } else {
            actualDragY = dragY + sCenter.y - (boxBitmap == null ? 0 : boxBitmap.getHeight() / 2f);
        }
        return sCenter.y * (getRealScare() - 1) / getRealScare() - actualDragY;
    }


    // TODO 重绘事件不要太频繁，真正有需求的时候才能调用
    //TODO 优化：删除电子墙的时候支持拖动,添加电子墙的时候支持缩放，NONE时支持缩放和拖动
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int me = event.getAction() & MotionEvent.ACTION_MASK;
        float x = event.getX() / getRealScare() + getOffsetX();
        float y = event.getY() / getRealScare() + getOffsetY();
        switch (me) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_DOWN:
                if (isNeedRestore && restoreRunnable != null) {
                    removeCallbacks(restoreRunnable);
                }
                downPoint.set(x, y);
                switch (MODE) {
                    case MODE_ADD_VIRTUAL:
                        // 添加电子墙
                        if (getUsefulWallNum() >= 10) {
                            ToastUtils.showToast(Utils.getString(R.string.map_aty_max_count));
                        }
                        break;
                    case MODE_DELETE_VIRTUAL:
                        MODE = MODE_DELETE_VIRTUAL * MODE_DRAG;
                        break;
                    case MODE_NONE:
                        MODE = MODE_DRAG;
                        break;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                switch (MODE) {
                    case MODE_ZOOM * MODE_ADD_VIRTUAL:
                    case MODE_ZOOM * MODE_DELETE_VIRTUAL:
                    case MODE_ZOOM:
                        if (event.getPointerCount() == 2) {
                            calculateScale(event);
                        }
                        break;
                    case MODE_DELETE_VIRTUAL:
                        break;
                    case MODE_ADD_VIRTUAL:
                        if (getUsefulWallNum() < 10) {
                            float distance = distance(downPoint.x, downPoint.y, x, y);
                            if (distance > MIN_WALL_LENGTH) {
                                curVirtualWall.set(downPoint.x, downPoint.y, x, y);
                            }
                        }
                        break;
                    case MODE_DELETE_VIRTUAL * MODE_DRAG:
                        float distance = distance(downPoint.x, downPoint.y, x, y);
                        if (distance > 5) {
                            dragX += x - downPoint.x;
                            dragY += y - downPoint.y;
                        }
                        MyLogger.d(TAG, "-----TOUCH---distance:" + distance);
                        break;
                    case MODE_DRAG:
                        dragX += x - downPoint.x;
                        dragY += y - downPoint.y;
                        break;
                }
                invalidateUI();
                break;
            case MotionEvent.ACTION_UP:
                boolean needResetChange = false;
                float distance = distance(downPoint.x, downPoint.y, x, y);
                if (distance < 5 && MODE == MODE_DRAG * MODE_DELETE_VIRTUAL) {//删除时，拖动距离小，认为是点击
                    MODE = MODE_DELETE_VIRTUAL;
                }
                switch (MODE) {
                    case MODE_ADD_VIRTUAL:
                        curVirtualWall.setEmpty();
                        if (getUsefulWallNum() < 10 && distance(downPoint.x, downPoint.y, x, y) > MIN_WALL_LENGTH) {
                            existVirtualPath.moveTo(downPoint.x, downPoint.y);
                            existVirtualPath.lineTo(x, y);//加入到已存在的电子墙集合中去
                            VirtualWallBean virtualWallBean = new VirtualWallBean(virtualWallBeans.size() + 1,
                                    new int[]{(int) reMatrixCoordinateX(downPoint.x), (int) reMatrixCoordinateY(downPoint.y), (int) reMatrixCoordinateX(x), (int) reMatrixCoordinateY(y)}
                                    , 2);
                            virtualWallBeans.add(virtualWallBean);
                            drawVirtualWall();
                        }
                        break;
                    case MODE_DELETE_VIRTUAL:
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
                        break;
                    case MODE_ADD_VIRTUAL * MODE_ZOOM:
                        originalScale = userScale;
                        MODE = MODE_ADD_VIRTUAL;
                        needResetChange = true;
                        break;
                    case MODE_DELETE_VIRTUAL * MODE_ZOOM:
                        originalScale = userScale;
                        MODE = MODE_DELETE_VIRTUAL;
                        needResetChange = true;
                        break;
                    case MODE_ZOOM:
                        originalScale = userScale;
                        MODE = MODE_NONE;
                        needResetChange = true;
                        break;
                    case MODE_DELETE_VIRTUAL * MODE_DRAG:
                        MODE = MODE_DELETE_VIRTUAL;
                        needResetChange = true;
                        break;
                    case MODE_DRAG:
                        needResetChange = true;
                        MODE = MODE_NONE;
                        break;
                }

                if (needResetChange) {
                    if (isNeedRestore && restoreRunnable == null) {
                        restoreRunnable = () -> {
                            userScale = 1;
                            originalScale = 1;
                            dragX = 0;
                            dragY = 0;
                            invalidateUI();
                            MyLogger.d(TAG, "-------restore map to the original state----------");
                        };
                    }
                    postDelayed(restoreRunnable, 15 * 1000);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                switch (MODE) {
                    case MODE_ADD_VIRTUAL:
                        break;
                    case MODE_DELETE_VIRTUAL:
                        break;
                    case MODE_DRAG:
                        break;
                    case MODE_ZOOM:
                        userScale = new BigDecimal(userScale).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
                        break;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN://多指DOWN
                if (event.getPointerCount() == 2) {//双指
                    switch (MODE) {
                        case MODE_DRAG:
                            MODE = MODE_ZOOM;
                            break;
                        case MODE_DRAG * MODE_DELETE_VIRTUAL:
                            MODE = MODE_DELETE_VIRTUAL * MODE_ZOOM;
                            break;
                        case MODE_ADD_VIRTUAL:
                            MODE = MODE_ADD_VIRTUAL * MODE_ZOOM;
                            break;
                    }
                    beforeDistance = distance(event);
                }
                break;

        }
        return true;
    }

    private void calculateScale(MotionEvent event) {
        float afterDistance = distance(event);
        if (Math.abs(afterDistance - beforeDistance) > 10) {
            userScale = (afterDistance / beforeDistance) * originalScale;
            float minScale = 0.6f;
            if (userScale < minScale) {
                userScale = minScale;
            }
            float maxScale = 2.0f / systemScale;
            if (userScale > maxScale) {
                userScale = maxScale;
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
        int DEFAULT_SIZE = 1500;
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
        if (MODE == MODE_DELETE_VIRTUAL || MODE == MODE_DELETE_VIRTUAL * MODE_DRAG || MODE == MODE_DELETE_VIRTUAL * MODE_ZOOM) {//删除电子墙模式，需要画出减号删除键
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
                    float l = cx - deleteBitmap.getWidth() * getRealScare() / 2;
                    float t = cy - deleteBitmap.getWidth() * getRealScare() / 2;
                    float r = l + deleteBitmap.getWidth() * getRealScare();
                    float b = t + deleteBitmap.getHeight() * getRealScare();
                    rectF = new RectF(l, t, r, b);
                    deleteIconRectFs.add(rectF);
                    vir.setDeleteIcon(rectF);
                }
            }

        }
        invalidateUI();
    }


    /**
     * 从(0,1500)开始向上一行行绘制slam map
     */
    private void drawSlamMap(byte[] slamBytes) {
        int x = 0, y = 0, length, totalCount = 0;
        if (slamBytes != null && slamBytes.length > 0) {
            slamPath.reset();
            obstaclePath.reset();
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
        for (int j = 0; j < baseScare; j += 4) {
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
    private void drawBoxMapX8(ArrayList<Integer> dataList) {
        if (boxCanvas == null && boxBitmap == null) {
            boxCanvas = new Canvas();
            boxBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            boxCanvas.setBitmap(boxBitmap);
        }
        if (dataList == null || dataList.size() == 0) {
            boxPath.reset();
            boxCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            invalidateUI();
            return;
        }
        pointList.clear();
        pointList.addAll(dataList);
        boxPath.reset();
        boxCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        int x, y;
        //绘制清扫区域的白方格
        boxPaint.setColor(getResources().getColor(R.color.white));
        boxPaint.setStrokeWidth(1);
        float space = new BigDecimal(baseScare * 0.1f).setScale(0, BigDecimal.ROUND_HALF_DOWN).floatValue();
        if (pointList.size() > 0) {
            for (int i = 1; i < pointList.size(); i += 2) {
                x = -pointList.get(i - 1);
                y = -pointList.get(i);
                boxPath.addRect(matrixCoordinateX(x), matrixCoordinateY(y), matrixCoordinateX(x) + baseScare - space, matrixCoordinateY(y) + baseScare - space, Path.Direction.CCW);
            }
        }
        endY = matrixCoordinateY(-pointList.get(pointList.size() - 1)) + (baseScare - space) / 2f;
        endX = matrixCoordinateX(-pointList.get(pointList.size() - 2)) + (baseScare - space) / 2f;
    }

    private void invalidateUI() {
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (slamBitmap != null) {
            slamBitmap.recycle();
            slamBitmap = null;
        }
        if (boxBitmap != null) {
            boxBitmap.recycle();
            boxBitmap = null;
        }

    }
}
