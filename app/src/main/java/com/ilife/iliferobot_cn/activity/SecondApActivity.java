package com.ilife.iliferobot_cn.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.ilife.iliferobot_cn.R;
import com.ilife.iliferobot_cn.base.BaseActivity;
import com.ilife.iliferobot_cn.utils.MyLog;
import com.ilife.iliferobot_cn.utils.ToastUtils;
import com.ilife.iliferobot_cn.utils.UserUtils;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * Created by chengjiaping on 2018/8/28.
 */

public class SecondApActivity extends BaseActivity implements View.OnClickListener {
    final String TAG = SecondApActivity.class.getSimpleName();
    public static final String EXTRA_PHYSICALID = "EXTRA_PHYSICALID";
    String ssid;
    String pass;
    Context context;
    Button bt_next;
    EditText et_physicalId;
    ImageView image_scan, image_back;
    IntentIntegrator integrator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ap_second);
        initView();
        initData();
    }

    private void initView() {
        context = this;
        bt_next = (Button) findViewById(R.id.bt_next);
        image_scan = (ImageView) findViewById(R.id.image_scan);
        image_back = (ImageView) findViewById(R.id.image_back);
        image_back.setOnClickListener(this);
        et_physicalId = (EditText) findViewById(R.id.et_physicalId);

        bt_next.setOnClickListener(this);
        image_scan.setOnClickListener(this);
    }

    private void initData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            ssid = bundle.getString(FirstApActivity.EXTRA_SSID);
            pass = bundle.getString(FirstApActivity.EXTRA_PASS);
        }
        integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(MyCaptureActivity.class);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_next:
                String physicalId = et_physicalId.getText().toString();
                if (TextUtils.isEmpty(physicalId)) {
                    ToastUtils.showToast(context, getString(R.string.ap_aty_input));
                } else {
                    if (!UserUtils.checkPhysicalId(physicalId)) {
                        ToastUtils.showToast(context, getString(R.string.ap_aty_physical_false));
                    } else {
                        Intent i = new Intent(context, ThirdApActivity.class);
                        i.putExtra(EXTRA_PHYSICALID, physicalId);
                        i.putExtra(FirstApActivity.EXTRA_SSID, ssid);
                        i.putExtra(FirstApActivity.EXTRA_PASS, pass);
                        startActivity(i);
                    }
                }
                break;
            case R.id.image_scan:
                new RxPermissions(this).requestEach(Manifest.permission.CAMERA).subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(@NonNull Permission permission) throws Exception {
                        if (permission.granted) {
                            // 用户已经同意该权限
                            Intent i = new Intent(context, CaptureActivity.class);
                            startActivityForResult(i, CaptureActivity.RESULT_CODE_QR_SCAN);
                        } else {
                            // 用户拒绝了该权限，并且选中『不再询问』
//                                ToastUtils.showToast(context,"请授予访问摄像头的权限");
                            ToastUtils.showToast(context, getString(R.string.access_camera));
                        }
                    }
                });
                break;
            case R.id.image_back:
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureActivity.RESULT_CODE_QR_SCAN && resultCode == CaptureActivity.RESULT_CODE_QR_SCAN) {
            if (data != null) {
                String content = data.getStringExtra(CaptureActivity.INTENT_EXTRA_KEY_QR_SCAN);
                MyLog.e(TAG, "onActivityResult " + content);
                if (!TextUtils.isEmpty(content)) {
                    et_physicalId.setText(content);
                    et_physicalId.setSelection(content.length());
                }
            }
        }
    }
}
