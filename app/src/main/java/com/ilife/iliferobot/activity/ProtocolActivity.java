package com.ilife.iliferobot.activity;

import android.os.Bundle;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.R;

import java.util.Locale;

import butterknife.BindView;

/**
 * Created by chengjiaping on 2017/11/30.
 */

public class ProtocolActivity extends BackBaseActivity {
    PDFView pdfView;
    @BindView(R.id.tv_top_title)
    TextView tv_title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_protocol;
    }

    public void initView() {
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
        tv_title.setText(R.string.personal_aty_protocol);
        pdfView.fromAsset(fileName)
                .pages(0, 2, 1, 3, 3, 3) // all pages are displayed by default
                .enableSwipe(true) // allows to block changing pages using swipe
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                .password(null)
                .scrollHandle(null)
                .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                // spacing between pages in dp. To define spacing color, set view background
                .spacing(0)
                .load();
    }
}
