package com.ilife.iliferobot.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACException;
import com.accloud.service.ACFeedback;
import com.accloud.service.ACObject;
import com.accloud.service.ACUserDevice;
import com.bumptech.glide.Glide;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.ilife.iliferobot.BuildConfig;
import com.ilife.iliferobot.able.Constants;
import com.ilife.iliferobot.activity.fragment.UniversalDialog;
import com.ilife.iliferobot.app.MyApplication;
import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.utils.MyLogger;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.utils.AlertDialogUtils;
import com.ilife.iliferobot.utils.BitmapUtils;
import com.ilife.iliferobot.utils.DisplayUtil;
import com.ilife.iliferobot.utils.GlideCircleTransform;
import com.ilife.iliferobot.utils.SpUtils;
import com.ilife.iliferobot.utils.Utils;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * Created by chengjiaping on 2018/8/13.
 */

public class PersonalActivity extends BackBaseActivity implements View.OnClickListener {
    final String TAG = PersonalActivity.class.getSimpleName();
    final String GALLERY_PACKAGE_NAME = "com.android.gallery3d";
    final int REQUEST_SCAN = 0X01;
    final int TAKE_PIC = 0x02;
    final int LOCAL_PIC = 0x03;
    int color_e2;
    int color_ac;
    int color_f6;
    int dialog_width, dialog_height, dialog_height_;
    Context context;
    long userId, exitTime;
    boolean isShow;
    File tempFile;
    String userName, content, email, type;
    TextView tv_userName, del_tv_title, tv_version, tv_content;
    ImageView image_forward, image_avatar;
    LayoutInflater inflater;
    RelativeLayout rl_help;
    RelativeLayout rl_scan;
    RelativeLayout rl_share;
    RelativeLayout rl_logout;
    RelativeLayout rl_delete_account;
    RelativeLayout rl_protocol;
    RelativeLayout rootView;
    LinearLayout ll_device;
    IntentIntegrator integrator;
    AlertDialog alertDialog;
    ArrayList<ACUserDevice> mDeviceList;
    ArrayList<String> formats;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_personal;
    }

    @Override
    public void initView() {
        context = this;
        color_e2 = getResources().getColor(R.color.color_e2);
        color_ac = getResources().getColor(R.color.color_ac);
        color_f6 = getResources().getColor(R.color.color_f6);
        inflater = LayoutInflater.from(context);
        tv_version = (TextView) findViewById(R.id.tv_version);
        tv_userName = (TextView) findViewById(R.id.tv_userName);
        image_forward = (ImageView) findViewById(R.id.image_forward);
        image_avatar = (ImageView) findViewById(R.id.image_avatar);
        rl_help = (RelativeLayout) findViewById(R.id.rl_help);
        rl_scan = (RelativeLayout) findViewById(R.id.rl_scan);
        rl_share = (RelativeLayout) findViewById(R.id.rl_share);
        rootView = (RelativeLayout) findViewById(R.id.rootView);
        ll_device = (LinearLayout) findViewById(R.id.ll_device);
        rl_logout = (RelativeLayout) findViewById(R.id.rl_logout);
        rl_delete_account = (RelativeLayout) findViewById(R.id.rl_delete_account);
        rl_protocol = (RelativeLayout) findViewById(R.id.rl_protocol);

        rl_help.setOnClickListener(this);
        rl_scan.setOnClickListener(this);
        rl_share.setOnClickListener(this);
        tv_userName.setOnClickListener(this);
        rl_logout.setOnClickListener(this);
        rl_delete_account.setOnClickListener(this);
        image_avatar.setOnClickListener(this);
        rl_protocol.setOnClickListener(this);
        ((TextView) findViewById(R.id.tv_top_title)).setText(R.string.personal_aty_personal_center);
    }

    public void initData() {
        File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/pic");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        tempFile = new File(dir, "temp.png");
        formats = new ArrayList<>();
        if (AC.accountMgr().isLogin()) {
            getNameAndAvatar();
            userId = AC.accountMgr().getUserId();
            email = SpUtils.getSpString(context, LoginActivity.KEY_EMAIL);
        }
        mDeviceList = new ArrayList<>();
        integrator = new IntentIntegrator(this);
        formats.add("CODE_128");
        formats.add("QR_CODE");
        integrator.setDesiredBarcodeFormats(formats);
        integrator.setCaptureActivity(CaptureActivity.class);
        dialog_width = (int) getResources().getDimension(R.dimen.dp_300);
        dialog_height = (int) getResources().getDimension(R.dimen.dp_140);
        dialog_height_ = (int) getResources().getDimension(R.dimen.dp_146);

        String version = getVersion();
        if (!TextUtils.isEmpty(version)) {
            tv_version.setText(getString(R.string.personal_aty_version, version, BuildConfig.FLAVOR_NAME));
        }
    }

    public void getNameAndAvatar() {
        AC.accountMgr().getUserProfile(new PayloadCallback<ACObject>() {
            @Override
            public void success(ACObject acObject) {
                if (!isDestroyed()) {
                    userName = acObject.get("nick_name");
//                    String url = acObject.get("_avatar");
                    if (!TextUtils.isEmpty(userName)) {
                        tv_userName.setText(userName);
                    }
//                    if (!TextUtils.isEmpty(url)){
//                        Glide.with(context).load(url).transform(new GlideCircleTransform(context)).into(image_avatar);
//                    }
                }
            }

            @Override
            public void error(ACException e) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.image_avatar:
//                showPhotoDialog();
                break;
            case R.id.rl_help:
                i = new Intent(context, HelpActivity.class);
                startActivity(i);
                break;
            case R.id.rl_scan:
                new RxPermissions(this).requestEach(Manifest.permission.CAMERA).subscribe(permission -> {
                    if (permission.granted) {
                        Intent i12 = new Intent(context, CaptureActivity.class);
                        startActivityForResult(i12, CaptureActivity.RESULT_CODE_QR_SCAN);
                    } else {
                        ToastUtils.showToast(context, getString(R.string.access_camera));
                    }
                });
                break;
            case R.id.rl_delete_account:
                showDeleteAccountDialog();
                break;
            case R.id.rl_logout:
                showLogoutDialog();
                break;
            case R.id.rl_protocol:
                if (Utils.isIlife()) {
                    i = new Intent(context, ProtocolActivity.class);
                    startActivity(i);
                } else {
                    i = new Intent(context, ZacoProtocolActivity.class);
                    startActivity(i);
                }
                break;
            case R.id.tv_userName:
                showRenameDialog();
                break;
            case R.id.rl_share:
                if (AC.accountMgr().isLogin()) {
                    getOwnerList();
                    if (mDeviceList.size() > 0) {
                        if (!isShow) {
                            showDeviceList();
                        }
                        ll_device.setVisibility(!isShow ? View.VISIBLE : View.GONE);
                        image_forward.setRotation(!isShow ? -90 : 0);
                        isShow = !isShow;
                    } else {
                        ToastUtils.showToast(context, getString(R.string.personal_aty_no_shareable));
                    }
                } else {
                    ToastUtils.showToast(context, getString(R.string.personal_aty_login_first));
                }
                break;
            case R.id.rl_photo:
                AlertDialogUtils.hidden(alertDialog);
                Intent i1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                i1.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
                startActivityForResult(i1, TAKE_PIC);
                break;
            case R.id.rl_album:
                AlertDialogUtils.hidden(alertDialog);
                Intent i2 = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i2.setPackage(GALLERY_PACKAGE_NAME);
                startActivityForResult(i2, LOCAL_PIC);
                break;
        }
    }

    /**
     * 拿到账号
     */
    private void showDeleteAccountDialog() {
        UniversalDialog deleteAccount = new UniversalDialog();
        String hint = Utils.getString(R.string.personal_aty_del_content);
        if (!Utils.isIlife()&&hint.contains("ILIFE")) {
            hint = hint.replace("ILIFE", Constants.BRAND_ZACO);
        }
        deleteAccount.setDialogType(UniversalDialog.TYPE_NORMAL).setTitleColor(getResources().getColor(R.color.color_f08300)).
                setTitle(email.isEmpty()?Utils.getString(R.string.personal_acy_del):email).setHintTip(hint).
                setOnRightButtonClck(() -> {
                    if (AC.accountMgr().isLogin()) {
                        ACFeedback feedback = new ACFeedback();
                        feedback.addFeedback("description", "请求删除账号");
                        feedback.addFeedback("telephoneNumber", email);
                        feedback.addFeedback("deviceType", "android");
                        AC.feedbackMgr().submitFeedback(feedback, new VoidCallback() {
                            @Override
                            public void success() {
                                Intent i = new Intent(context, QuickLoginActivity.class);
                                startActivity(i);
                                removeALLActivity();
                            }

                            @Override
                            public void error(ACException e) {
                                ToastUtils.showToast(context, getString(R.string.help_aty_commit));
                            }
                        });

                    }
                }).show(getSupportFragmentManager(), "logout");
    }

    private void showLogoutDialog() {
        UniversalDialog logoutDialog = new UniversalDialog();
        logoutDialog.setDialogType(UniversalDialog.TYPE_NORMAL).setTitleColor(getResources().getColor(R.color.color_f08300)).
                setTitle(Utils.getString(R.string.personal_acy_exit)).setHintTip(Utils.getString(R.string.personal_aty_exit_content)).
                setOnRightButtonClck(() -> {
                    if (AC.accountMgr().isLogin()) {
                        removeAbleAlia();
                        AC.accountMgr().logout();
                        Intent i = new Intent(context, QuickLoginActivity.class);
                        startActivity(i);
                        removeALLActivity();
                    }
                }).show(getSupportFragmentManager(), "logout");
    }

    private void removeAbleAlia() {
        //userId为用户ID，通过AbleCloud登录接口返回的ACUserInfo可以获取到userId；第二个参数写死ablecloud即可。
        PushAgent.getInstance(this).deleteAlias(String.valueOf(AC.accountMgr().getUserId()), "ablecloud", (b, s) -> {
            MyLogger.d(TAG, "移除able推送别名，message :" + s);
        });
    }

    private void showRenameDialog() {
        UniversalDialog logoutDialog = new UniversalDialog();
        logoutDialog.setDialogType(UniversalDialog.TYPE_NORMAL).setCanEdit(true).setTitle(Utils.getString(R.string.personal_aty_set_name))
                .setHintTip(Utils.getString(R.string.user_nickname)).setOnRightButtonWithValueClck((name) -> {
            if (TextUtils.isEmpty(name)) {
                ToastUtils.showToast(context, getString(R.string.setting_aty_devName_null));
                return;
            }

            if (name.length() > Utils.getInputMaxLength()) {
                ToastUtils.showToast(getResources().getString(R.string.name_max_length, Utils.getInputMaxLength() + ""));
                return;
            }
            if (!name.equals(userName)) {
                changeNickName(name);
            }
            logoutDialog.dismiss();
        }).show(getSupportFragmentManager(), "rename");
    }


    public void changeNickName(final String name) {
        AC.accountMgr().changeNickName(name, new VoidCallback() {
            @Override
            public void success() {
                ToastUtils.showToast(context, getString(R.string.personal_aty_reset_suc));
                userName = name;
                tv_userName.setText(name);
            }

            @Override
            public void error(ACException e) {
                ToastUtils.showToast(context, getString(R.string.personal_aty_reset_fail));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureActivity.RESULT_CODE_QR_SCAN && resultCode == CaptureActivity.RESULT_CODE_QR_SCAN) {
            if (data != null) {
                String shareCode = data.getStringExtra(CaptureActivity.INTENT_EXTRA_KEY_QR_SCAN);
                if (!TextUtils.isEmpty(shareCode)) {
                    MyLogger.e(TAG, "onActivityResult shareCode = " + shareCode);
                    bindDevice(shareCode);
                }
            }
        }

        if (resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            switch (requestCode) {
                case TAKE_PIC:
                    bitmap = BitmapUtils.compressBitmap(this, Uri.fromFile(tempFile), image_avatar.getWidth(), image_avatar.getHeight());

                    break;
                case LOCAL_PIC:
                    bitmap = BitmapUtils.compressBitmap(this, data.getData(), image_avatar.getWidth(), image_avatar.getHeight());
                    break;
            }
            if (bitmap != null) {
                byte[] bytes = BitmapUtils.bitmapToByte(bitmap);
                setAvatar(bytes);
            }
        }
    }

    public void setAvatar(final byte[] bytes) {
        AC.accountMgr().setAvatar(bytes, new PayloadCallback<String>() {
            @Override
            public void success(String url) {
                if (!isDestroyed()) {
                    Glide.with(context).load(bytes).skipMemoryCache(true).transform(new GlideCircleTransform()).into(image_avatar);
                    ToastUtils.showToast(context, getString(R.string.personal_aty_avatar_suc));
                }
            }

            @Override
            public void error(ACException e) {
                ToastUtils.showToast(context, getString(R.string.personal_aty_avatar_fail));
            }
        });
    }

    public void bindDevice(String shareCode) {
        AC.bindMgr().bindDeviceWithShareCode(shareCode, new PayloadCallback<ACUserDevice>() {
            @Override
            public void success(ACUserDevice userDevice) {
                ToastUtils.showToast(getString(R.string.personal_aty_bind_done));
                removeActivity();
            }

            @Override
            public void error(ACException e) {
                ToastUtils.showToast(getString(R.string.personal_aty_bind_fail));
            }
        });
    }


    public void getOwnerList() {
        mDeviceList.clear();
        List<ACUserDevice> mAcUserDevices = MyApplication.getInstance().getmAcUserDevices();
        if (mAcUserDevices != null && mAcUserDevices.size() > 0) {
            for (int i = 0; i < mAcUserDevices.size(); i++) {
                long ownerId = mAcUserDevices.get(i).getOwner();
                if (ownerId == userId) {
                    mDeviceList.add(mAcUserDevices.get(i));
                }
            }
        }
    }

    public void showDeviceList() {
        ll_device.removeAllViews();
        for (int i = 0; i < mDeviceList.size(); i++) {
//            if (i==0){
//                if (mDeviceList.size()>1){
//                    addShortLine();
//                } else {
//                    addLongLine();
//                }
//            }
            if (i != 0) {
                addShortLine();
            }
            TextView textView = new TextView(context);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setTextSize(14);
            textView.setTextColor(color_ac);
            textView.setBackgroundColor(color_f6);
            int height = (int) getResources().getDimension(R.dimen.dp_60);
            int paddingStart = (int) getResources().getDimension(R.dimen.dp_80);
            ViewGroup.LayoutParams lp_text = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
            textView.setPadding(paddingStart, 0, 0, 0);
            textView.setLayoutParams(lp_text);
            String devName = mDeviceList.get(i).getName();
            String physicalDeviceId = mDeviceList.get(i).getPhysicalDeviceId();
            long devId = mDeviceList.get(i).getDeviceId();
            if (!TextUtils.isEmpty(devName)) {
                if (devName.contains(Constants.ROBOT_WHITE_TAG)) {
                    devName = devName.replace(Constants.ROBOT_WHITE_TAG, "");
                }
                textView.setText(devName);
            } else {
                textView.setText(physicalDeviceId);
            }
            textView.setOnClickListener(new MyTextListener(devId));
            ll_device.addView(textView);
//            if (i!= mDeviceList.size()-1){
//                addShortLine();
//            }
        }
    }

    public void addLongLine() {
        View line = new View(context);
        line.setBackgroundColor(color_e2);
        ViewGroup.LayoutParams lp_line = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                DisplayUtil.dip2px(context, 1));
        line.setLayoutParams(lp_line);
        ll_device.addView(line);
    }

    public void addShortLine() {
        View line = new View(context);
        int margin = (int) getResources().getDimension(R.dimen.dp_30);
        line.setBackgroundColor(color_e2);
        LinearLayout.LayoutParams lp_line = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                DisplayUtil.dip2px(context, 1));
        lp_line.setMargins(margin, 0, margin, 0);
        line.setLayoutParams(lp_line);
        ll_device.addView(line);
    }

    public class MyTextListener implements View.OnClickListener {
        private long devId;

        public MyTextListener(long devId) {
            this.devId = devId;
        }

        @Override
        public void onClick(View v) {
            AC.bindMgr().refreshShareCode(devId, 3 * 60 * 1000, new PayloadCallback<String>() {
                @Override
                public void success(String shareCode) {
                    showQrDialog(shareCode);
                }

                @Override
                public void error(ACException e) {
                    ToastUtils.showToast(context, getString(R.string.per_fgm_gain_fail));
                }
            });
        }
    }

    public void showQrDialog(String shareCode) {
        View contentView = inflater.inflate(R.layout.layout_qr_code, null);
        ImageView imageView = (ImageView) contentView.findViewById(R.id.image_map);
        createCode(imageView, shareCode);
        int width = (int) getResources().getDimension(R.dimen.dp_260);
        int height = (int) getResources().getDimension(R.dimen.dp_260);
        alertDialog = AlertDialogUtils.showDialog(context, contentView, width, height);
    }

    public void createCode(ImageView imageView, String info) {
        Bitmap bitmap;
        BitMatrix matrix;
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 0);
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            int width = (int) getResources().getDimension(R.dimen.dp_188);
            int height = (int) getResources().getDimension(R.dimen.dp_188);
            matrix = writer.encode(info, BarcodeFormat.QR_CODE, width, height, hints);
            BarcodeEncoder encoder = new BarcodeEncoder();
            bitmap = encoder.createBitmap(matrix);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
//                image_map.setBackgroundColor(getResources().getColor(R.color.color_ef8200));
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }


    public String getVersion() {
        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
