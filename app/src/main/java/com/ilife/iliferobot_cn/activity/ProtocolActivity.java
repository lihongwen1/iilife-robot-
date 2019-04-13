package com.ilife.iliferobot_cn.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.github.barteksc.pdfviewer.PDFView;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;

import java.util.Locale;

/**
 * Created by chengjiaping on 2017/11/30.
 */

public class ProtocolActivity extends BaseActivity {
    private ImageView image_back;
    //    private TextView tv_protocol;
    PDFView pdfView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protocol);
        initView();
    }

    private void initView() {
        String lan = Locale.getDefault().getLanguage();
        String fileName;
        if (lan.equals("zh")) {
            fileName = "ilife_zh.pdf";
        } else if (lan.equals("de")) {
            fileName = "ilife_de.pdf";
        } else {
            fileName = "ilife_en.pdf";
        }
//        String fileName = lan.equals("zh")?"ilife_zh.pdf":"ilife_en.pdf";
        pdfView = (PDFView) findViewById(R.id.pdfView);
        pdfView.fromAsset(fileName).defaultPage(0)
                .enableAnnotationRendering(true)
                .swipeHorizontal(false)
                .spacing(10)
                .load();
        image_back = (ImageView) findViewById(R.id.image_back);
        image_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

//        tv_protocol = (TextView) findViewById(R.id.tv_protocol);
//        tv_protocol.setText(ProtocOlUtils.PROTOCOL);
    }
}
