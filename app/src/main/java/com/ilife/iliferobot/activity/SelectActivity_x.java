package com.ilife.iliferobot.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.ilife.iliferobot.BuildConfig;
import com.ilife.iliferobot.able.DeviceUtils;
import com.ilife.iliferobot.adapter.XAdapter;
import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.able.Constants;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.base.BaseQuickAdapter;
import com.ilife.iliferobot.model.bean.CleanningRobot;
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
    public static final String KEY_BIND_WHITE = "key_bind_white";
    Context context;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    BaseQuickAdapter adapter;
    @BindView(R.id.tv_top_title)
    TextView tvTitle;
    private List<CleanningRobot> robots = new ArrayList<>();
    private String[] supportRobots;

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
        supportRobots = DeviceUtils.getSupportDevices();
        String robotName;

        for (String deviceType : supportRobots) {
            if (BuildConfig.Area == AC.REGIONAL_CHINA || BuildConfig.BRAND.equals("ZACO")) {
                robotName = BuildConfig.BRAND + " " + deviceType;
            } else {
                robotName = deviceType;
            }
            switch (deviceType) {
                case Constants.X900:
                    robots.add(new CleanningRobot(R.drawable.n_x900, robotName, Constants.subdomain_x900, Constants.subdomainId_x900));
                    break;
                case Constants.X800://国内X800包含 黑色款和白色款
                    robots.add(new CleanningRobot(R.drawable.n_x800, robotName + " • 黑", Constants.subdomain_x800, Constants.subdomainId_x800));
                    robots.add(new CleanningRobot(R.drawable.n_x800_white, robotName + " • 白", Constants.subdomain_x800, Constants.subdomainId_x800));
                    break;
                case Constants.X787:
                    robots.add(new CleanningRobot(R.drawable.n_x787, robotName, Constants.subdomain_x787, Constants.subdomainId_x787));
                    break;
                case Constants.X785:
                    robots.add(new CleanningRobot(R.drawable.n_x785, robotName, Constants.subdomain_x785, Constants.subdomainId_x785));
                    break;
                case Constants.A8s:
                    robots.add(new CleanningRobot(R.drawable.n_a8s, robotName, Constants.subdomain_a8s, Constants.subdomaiId_a8s));
                    break;
                case Constants.A9s:
                    if (Utils.isIlife()) {
                        robots.add(new CleanningRobot(R.drawable.n_x800, robotName, Constants.subdomain_x800, Constants.subdomainId_x800));
                    } else {
                        robots.add(new CleanningRobot(R.drawable.n_a9s, robotName, Constants.subdomain_a9s, Constants.subdomaiId_a9s));
                    }
                    break;

                case Constants.V3x:
                    robots.add(new CleanningRobot(R.drawable.n_v5x, robotName, Constants.subdomain_V3x, Constants.subdomainId_V3x));
                    break;
                case Constants.V85:
                    robots.add(new CleanningRobot(R.drawable.n_v85, robotName, Constants.subdomain_v85, Constants.subdomaiId_v85));
                    break;
                case Constants.X910:
                    robots.add(new CleanningRobot(R.drawable.n_x910, robotName, Constants.subdomain_x910, Constants.subdomaiId_x910));
                    break;
                case Constants.V5x:
                    robots.add(new CleanningRobot(R.drawable.n_v5x, robotName, Constants.subdomain_v5x, Constants.subdomaiId_v5x));
                    break;
                case Constants.A9:
                    if (BuildConfig.Area == AC.REGIONAL_SOUTHEAST_ASIA) {//日规A9为白色图
                        robots.add(new CleanningRobot(R.drawable.n_x800_white, Constants.A9, Constants.subdomain_x800, Constants.subdomainId_x800));
                    } else {
                        robots.add(new CleanningRobot(R.drawable.n_x800, robotName, Constants.subdomain_x800, Constants.subdomainId_x800));
                    }
                    break;
                case Constants.A7:
                    robots.add(new CleanningRobot(R.drawable.n_x787, robotName, Constants.subdomain_a7, Constants.subdomainId_A7));
                    break;

            }
        }
        adapter = new XAdapter(R.layout.x_series_item, robots);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            SpUtils.saveString(context, KEY_SUBDOMAIN, robots.get(position).getSubdomain());
            SpUtils.saveLong(context, KEEY_SUBDOMAIN_ID, robots.get(position).getSubdomainId());
            if (robots.get(position).getName().contains("白") || (BuildConfig.Area == AC.REGIONAL_SOUTHEAST_ASIA && robots.get(position).getName().equals(Constants.A9))) {
                SpUtils.saveBoolean(context, KEY_BIND_WHITE, true);
            } else {
                SpUtils.saveBoolean(context, KEY_BIND_WHITE, false);
            }

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
