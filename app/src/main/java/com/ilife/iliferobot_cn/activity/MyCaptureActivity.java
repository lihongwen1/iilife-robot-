package com.ilife.iliferobot_cn.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

/**
 * Created by chengjiaping on 2017/7/5.
 */

public class MyCaptureActivity extends BaseActivity {
    private DecoratedBarcodeView mDBV;
    private CaptureManager captureManager;
    private ImageView image_back;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mycapture);
        mDBV = (DecoratedBarcodeView) findViewById(R.id.zxing_barcode_scanner);
        captureManager = new CaptureManager(this, mDBV);
        captureManager.initializeFromIntent(getIntent(), savedInstanceState);
        captureManager.decode();
        image_back = (ImageView) findViewById(R.id.image_back);
        image_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        captureManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        captureManager.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        captureManager.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        captureManager.onSaveInstanceState(outState);
    }
}
