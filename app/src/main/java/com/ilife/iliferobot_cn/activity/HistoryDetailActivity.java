package com.ilife.iliferobot_cn.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.SpUtils;

import java.util.ArrayList;

/**
 * Created by chenjiaping on 2017/8/18.
 */

public class HistoryDetailActivity extends BaseActivity implements View.OnClickListener, View.OnTouchListener {
    private final String TAG = HistoryDetailActivity.class.getSimpleName();
    private ImageView imageView;
    private ImageView image_back;
    private ArrayList<String> mapList;
    private ArrayList<Byte> byteList;
    private int length;

    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paint;

    private byte tempdata = 0;
    private byte mapdata = 0;

    private int width;
    private int height;
    private int space = 10;
    private int nCnt;
    private double nLenStart;
    private String subdomain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);
        initView();
        getData();
        decodeData();
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

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        canvas.translate(width / 2, height / 2);

        if (length * 8 * space > width) {
            space = 5;
        }

        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.color_f08300));
        paint.setStrokeWidth(space - 2);

        for (int i = 0; i < byteList.size() / length; i++) {
            for (int j = 0; j < length; j++) {
                mapdata = byteList.get(i * length + j);
                for (int k = 0; k < 8; k++) {
                    tempdata = (byte) (0x80 >> k);
                    float x;
                    if (subdomain.equals(Constants.subdomain_x800)) {
                        x = (i - byteList.size() / length / 2) * space + 1;
                    } else {
                        x = -(i - byteList.size() / length / 2) * space + 1;
                    }
                    float y = (j * 8 + k - length * 4) * space + 1;
                    if ((mapdata & tempdata) == tempdata) {                           //1,着色
                        canvas.drawPoint(x, y, paint);
                    }
                }
            }
        }
        canvas.save();
        imageView.setImageBitmap(bitmap);
    }

    private void decodeData() {
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
    }

    private void getData() {
        subdomain = SpUtils.getSpString(this, MainActivity.KEY_SUBDOMAIN);
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                mapList = bundle.getStringArrayList("mapList");
            }
        }
    }

    private void initView() {
        byteList = new ArrayList<>();

        imageView = (ImageView) findViewById(R.id.image_map);
        imageView.setOnTouchListener(this);
        image_back = (ImageView) findViewById(R.id.image_back);
        image_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_back:
                finish();
                break;
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
            if (nLenEnd > nLenStart)                                                                //通过两个手指开始距离和结束距离，来判断放大缩小,亲测这里没有问题
            {
                float scale = Float.valueOf(nLenEnd / nLenStart + "");                                  //大于1,亲测应该是可以的
                space *= scale;
                if (space >= 20) {
                    space = 20;
                }
//                imageView.invalidate();
                myInvalidate();
            } else {
                float scale = Float.valueOf(nLenEnd / nLenStart + "");                                  //小于1，亲测应该是可以的
                space *= scale;
                if (space <= 4) {
                    space = 4;
                }
//                imageView.invalidate();
                myInvalidate();
            }
        }
//        if (nCnt==2){
//            return true;
//        }
        return true;
    }

    public void myInvalidate() {
        width = imageView.getWidth();
        height = imageView.getHeight();

//        setSpace(length);

//        bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_4444);
//        canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
//        canvas.translate(width/2,height/2);

//        Paint paint_1 = new Paint();
//        paint_1.setColor(Color.parseColor("#00bdb5"));
        paint.setStrokeWidth(space - 2);

        for (int i = 0; i < byteList.size() / length; i++) {
            for (int j = 0; j < length; j++) {
                mapdata = byteList.get(i * length + j);
                for (int k = 0; k < 8; k++) {
                    tempdata = (byte) (0x80 >> k);
                    float x;
                    if (subdomain.equals(Constants.subdomain_x800)) {
                        x = (i - byteList.size() / length / 2) * space + 1;
                    } else {
                        x = -(i - byteList.size() / length / 2) * space + 1;
                    }

                    float y = (j * 8 + k - length * 4) * space + 1;
                    if ((mapdata & tempdata) == tempdata) {                           //1,着色
                        canvas.drawPoint(x, y, paint);
                    }
                }
            }
        }
        canvas.save();
        imageView.setImageBitmap(bitmap);
    }
}
