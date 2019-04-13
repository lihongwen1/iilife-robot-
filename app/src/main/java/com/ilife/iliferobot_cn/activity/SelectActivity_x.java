package com.ilife.iliferobot_cn.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ImageView;

import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.adapter.X_seriesAdapter;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.utils.Constants;
import com.ilife.iliferobot_cn.utils.SpUtils;

/**
 * Created by chengjiaping on 2018/8/9.
 */
//DONE
public class SelectActivity_x extends BaseActivity implements View.OnClickListener {
    final String TAG = SelectActivity_x.class.getSimpleName();
    public static final String KEY_SUBDOMAIN = "key_subdomain";
    public static final String KEEY_SUBDOMAIN_ID = "key_subdomain_id";
    Context context;
    ImageView image_back;
    RecyclerView recyclerView;
    X_seriesAdapter adapter;
    String[] subdomains;
    long[] subdomainIds;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_x);
        initView();
    }

    public void initView() {
        context = this;
        image_back = (ImageView) findViewById(R.id.image_back);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));

        subdomains = new String[]{Constants.subdomain_x800, Constants.subdomain_x787, Constants.subdomain_x785};
//        subdomains = new String[]{Constants.subdomain_x900,Constants.subdomain_x787,Constants.subdomain_x785};
        subdomainIds = new long[]{Constants.subdomainId_x800, Constants.subdomainId_x787, Constants.subdomainId_x785};
//        subdomainIds = new long[]{Constants.subdomainId_x900,Constants.subdomainId_x787,Constants.subdomainId_x785};

        adapter = new X_seriesAdapter(context);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new X_seriesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                SpUtils.saveString(context, KEY_SUBDOMAIN, subdomains[position]);
                SpUtils.saveLong(context, KEEY_SUBDOMAIN_ID, subdomainIds[position]);
//                Intent i = new Intent(context,GuideActivity.class);
                Intent i = new Intent(context, ApGuideActivity.class);
//                i.putExtra(EXTRA_SUBDOMAIN,subdomains[position]);
                startActivity(i);
            }
        });

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
}
