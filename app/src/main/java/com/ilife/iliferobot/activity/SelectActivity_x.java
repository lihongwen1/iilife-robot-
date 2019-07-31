package com.ilife.iliferobot.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.ilife.iliferobot.BuildConfig;
import com.ilife.iliferobot.adapter.XAdapter;
import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.able.Constants;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.base.BaseQuickAdapter;
import com.ilife.iliferobot.model.CleanningRobot;
import com.ilife.iliferobot.utils.SpUtils;
import com.ilife.iliferobot.utils.Utils;
import com.ilife.iliferobot.view.SpaceItemDecoration;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by chengjiaping on 2018/8/9.
 */
//DONE
public class SelectActivity_x extends BackBaseActivity {
    final String TAG = SelectActivity_x.class.getSimpleName();
    public static final String KEY_SUBDOMAIN = "key_subdomain";
    public static final String KEEY_SUBDOMAIN_ID = "key_subdomain_id";
    Context context;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    BaseQuickAdapter adapter;
    @BindView(R.id.tv_top_title)
    TextView tvTitle;
    private List<CleanningRobot> robots = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_select_x;
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void initView() {
        context = this;
        tvTitle.setText(R.string.select_x_aty_add);
        recyclerView.addItemDecoration(new SpaceItemDecoration(Utils.dip2px(this, 6)));
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        initAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void initAdapter() {
        if (Utils.isIlife()) {
            switch (BuildConfig.Area) {
                case 0:
                    robots.add(new CleanningRobot(R.drawable.n_x900, "ILIFE X900", Constants.subdomain_x900, Constants.subdomainId_x900));
                    robots.add(new CleanningRobot(R.drawable.n_x800, "ILIFE X800", Constants.subdomain_x800, Constants.subdomainId_x800));
                    robots.add(new CleanningRobot(R.drawable.n_x787, "ILIFE X787", Constants.subdomain_x787, Constants.subdomainId_x787));
                    robots.add(new CleanningRobot(R.drawable.n_x785, "ILIFE X785", Constants.subdomain_x785, Constants.subdomainId_x785));
                    break;
                case 3:
                    robots.add(new CleanningRobot(R.drawable.n_x800, "ILIFE A9", Constants.subdomain_x800, Constants.subdomainId_x800));
                    break;
            }
        } else {
            robots.add(new CleanningRobot(R.drawable.n_a9s, "ZACO A9s", Constants.subdomain_a9s, Constants.subdomaiId_a9s));
            robots.add(new CleanningRobot(R.drawable.n_a8s, "ZACO A8s", Constants.subdomain_a8s, Constants.subdomaiId_a8s));
            robots.add(new CleanningRobot(R.drawable.n_v85, "ZACO V85", Constants.subdomain_v85, Constants.subdomaiId_v85));
        }
        adapter = new XAdapter(R.layout.x_series_item, robots);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            SpUtils.saveString(context, KEY_SUBDOMAIN, robots.get(position).getSubdomain());
            SpUtils.saveLong(context, KEEY_SUBDOMAIN_ID, robots.get(position).getSubdomainId());
            Intent i = new Intent(context, FirstApActivity.class);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.IS_FIRST_AP = true;
    }


}
