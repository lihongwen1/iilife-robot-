package com.ilife.iliferobot.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.TextView;

import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.utils.Constants;
import com.ilife.iliferobot.view.MapView;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.entity.HistoryRecord_x9;
import com.ilife.iliferobot.utils.DataUtils;
import com.ilife.iliferobot.utils.MyLog;
import com.ilife.iliferobot.utils.SpUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;

/**
 * Created by chenjiaping on 2017/8/18.
 */

public class HistoryDetailActivity_x9 extends BackBaseActivity {
    private final String TAG = HistoryDetailActivity_x9.class.getSimpleName();
    private byte[] slamBytes;
    private byte[] roadBytes;
    private ArrayList<String> mapList;
    private ArrayList<Integer> historyPointsList;
    private int xMin;
    private int xMax;
    private int yMin;
    private int yMax;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.mv_history_detail)
    MapView mapView;
    @BindView(R.id.tv_end_reason)
    TextView tv_end_reason;
    @BindView(R.id.tv_clean_time)
    TextView tv_clean_time;
    @BindView(R.id.tv_lean_area)
    TextView tv_lean_area;
    private String subdomain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getData();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_history_detail_x9;
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
        mapView.drawRoadMap(historyPointsList, null);
    }

    private void drawSlamMap(byte[] slamBytes) {
        mapView.updateSlam(xMin, xMax, yMin, yMax, 6);
        mapView.drawSlamMap(slamBytes);
        mapView.drawObstacle();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (subdomain.equals(Constants.subdomain_x900)) {
            drawHistoryMap();
        }else {
            drawHistoryMapX8();
        }
    }

    private void drawHistoryMapX8() {
        //decode data
        int length = 0;
        List<Byte> byteList = new ArrayList<>();
        ArrayList<Integer> pointList = new ArrayList<>();
        if (mapList != null) {
            if (mapList.size() > 0) {
                for (int i = 0; i < mapList.size(); i++) {
                    String data = mapList.get(i);
                    byte[] bytes = Base64.decode(data, Base64.DEFAULT);
                    length = bytes[0];
                    for (int j = 1; j < bytes.length; j++) {
                        byteList.add(bytes[j]);
                    }
                }
            }
        }
        byte tempdata = 0;
        byte mapdata = 0;
        //decode x, y
        for (int i = 0; i < byteList.size() / length; i++) {
            for (int j = 0; j < length; j++) {
                mapdata = byteList.get(i * length + j);
                for (int k = 0; k < 8; k++) {
                    tempdata = (byte) (0x80 >> k);
                    float x;

                    x = (i - byteList.size() / length / 2);
                    float y = (j * 8 + k - length * 4);
                    if ((mapdata & tempdata) == tempdata) {
                        if(subdomain.equals(Constants.subdomain_x800)){
                            pointList.add((int) y);
                            pointList.add((int) x);
                        }else {
                            pointList.add((int) x);
                            pointList.add((int) y);
                        }

                    }
                }
            }
        }
        mapView.drawBoxMapX8(pointList);

    }

    private void getData() {//取出传递过来的集合
        subdomain = SpUtils.getSpString(this, MainActivity.KEY_SUBDOMAIN);
        Intent intent = getIntent();
        if (intent != null) {
            HistoryRecord_x9 record = (HistoryRecord_x9) intent.getSerializableExtra("Record");
            mapList = record.getHistoryData();
            xMin = record.getSlam_xMin();
            xMax = record.getSlam_xMax();
            yMax = 1500 - record.getSlam_yMin();
            yMin = 1500 - record.getSlam_yMax();
            MyLog.e(TAG, "getDate===:" + xMin + "<--->" + xMax + "<--->" + yMin + "<--->" + yMax + "<--->");
            long time_ = record.getStart_time();
            String date = generateTime(time_, getString(R.string.history_adapter_month_day));
            tv_title.setText(date);
            tv_end_reason.setText(getResources().getString(R.string.setting_aty_end_reason, gerRealErrortTip(record.getStop_reason())));
            tv_clean_time.setText(record.getWork_time() / 60 + "min");
            tv_lean_area.setText(record.getClean_area() + "㎡");
        }
    }

    public String generateTime(long time, String strFormat) {
        SimpleDateFormat format = new SimpleDateFormat(strFormat);
        String str = format.format(new Date((time + 10) * 1000));
        return str;
    }

    private String gerRealErrortTip(int number) {
        String text = "";
        switch (number) {
            case 1:
                text = getResources().getString(R.string.stop_work_reason1);
                break;
            case 2:
                text = getResources().getString(R.string.stop_work_reason2);
                break;
            case 3:
                text = getResources().getString(R.string.stop_work_reason3);
                break;
            case 4:
                text = getResources().getString(R.string.stop_work_reason4);
                break;
            case 5:
                text = getResources().getString(R.string.stop_work_reason5);
                break;
            case 6:
                text = getResources().getString(R.string.stop_work_reason6);
                break;
            default:
                text = getResources().getString(R.string.stop_work_reason1);
                break;
        }
        return text;
    }

    public void initView() {
        historyPointsList = new ArrayList<>();
    }

}