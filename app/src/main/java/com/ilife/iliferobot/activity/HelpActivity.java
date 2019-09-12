package com.ilife.iliferobot.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACException;
import com.accloud.service.ACFeedback;
import com.ilife.iliferobot.BuildConfig;
import com.ilife.iliferobot.able.DeviceUtils;
import com.ilife.iliferobot.adapter.HelpFeedImgAdapter;
import com.ilife.iliferobot.app.MyApplication;
import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.utils.AlertDialogUtils;
import com.ilife.iliferobot.utils.BitmapUtils;
import com.ilife.iliferobot.utils.DialogUtils;
import com.ilife.iliferobot.utils.KeyboardUtils;
import com.ilife.iliferobot.utils.MyLogger;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.utils.UserUtils;
import com.ilife.iliferobot.utils.Utils;
import com.ilife.iliferobot.view.SpaceItemDecoration;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * Created by chengjiaping on 2018/8/13.
 */

public class HelpActivity extends BackBaseActivity implements View.OnClickListener {
    final String TAG = HelpActivity.class.getSimpleName();
    final int CAPTURE = 0x01;
    final int ALBUM = 0x02;
    final int CROP_PIC = 0x03;
    Context context;
    LayoutInflater inflater;
    EditText et_email, et_type, et_content;
    RelativeLayout rl_type;
    File captureFile, albumFile;
    PopupWindow typePop;
    AlertDialog alertDialog;
    String[] types;
    Activity activity;
    View view;
    Uri takePicUri;
    Dialog dialog;
    @BindView(R.id.tv_numbs)
    TextView tv_numbs;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.rv_feed_image)
    RecyclerView rv_feed_image;
    @BindView(R.id.image_add)
    ImageView image_add;
    private List<Bitmap> images = new ArrayList<>();
    private HelpFeedImgAdapter rvAdapter;
    private int replacePosition = -1;//标记需要替换的feed image的位置
    private int permissionFlag = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFile();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_help;
    }

    public void initView() {
        context = this;
        activity = this;
        dialog = DialogUtils.createLoadingDialog_(context);
        inflater = LayoutInflater.from(context);

        types = DeviceUtils.getSupportDevices();
        et_email = (EditText) findViewById(R.id.et_email);
        if (Utils.isSupportPhone()) {
            et_email.setHint(R.string.login_aty_input_email_phone);
        } else {
            et_email.setHint(R.string.personal_input_email);
        }
        et_type = (EditText) findViewById(R.id.et_type);
        et_content = (EditText) findViewById(R.id.et_content);
        rl_type = (RelativeLayout) findViewById(R.id.rl_type);
        view = findViewById(R.id.view);
        tv_title.setText(R.string.personal_aty_help);
        et_content.addTextChangedListener(new MyTextWatcher());
        if (Utils.isIlife()) {
            findViewById(R.id.ll_tel_zaco).setVisibility(View.GONE);
            findViewById(R.id.ll_tel_ilife).setVisibility(View.VISIBLE);
            switch (BuildConfig.Area) {
                case AC.REGIONAL_NORTH_AMERICA://US
                    ((TextView) findViewById(R.id.tv_telNum1)).setText("1-800-631-9676");
                    ((TextView) findViewById(R.id.tv_phone_time)).setText("(Mon-Fri 09:00-17:00,CST)");
                    break;
                case  AC.REGIONAL_SOUTHEAST_ASIA:
                    ((TextView) findViewById(R.id.tv_telNum1)).setText("072-730-2277");
                    ((TextView) findViewById(R.id.tv_phone_time)).setText(Utils.getString(R.string.service_time_ja));
                    ((TextView) findViewById(R.id.tv_phone_time1)).setText(Utils.getString(R.string.service_time1_ja));
                    break;
            }
        } else {//ZACO
            findViewById(R.id.ll_tel_zaco).setVisibility(View.VISIBLE);
            findViewById(R.id.ll_tel_ilife).setVisibility(View.GONE);
        }
        rv_feed_image.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rv_feed_image.setAdapter(rvAdapter = new HelpFeedImgAdapter(context, R.layout.item_feed_image, images));
        rv_feed_image.addItemDecoration(new SpaceItemDecoration(Utils.dip2px(this, 6), true));
        rvAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            switch (view.getId()) {
                case R.id.iv_feed_img:
                    replacePosition = position;
                    showPhotoDialog();
                    break;
                case R.id.iv_delete_img:
                    if (images.size() > position) {
                        images.remove(position);
                        image_add.setVisibility(images.size() == 2 ? View.GONE : View.VISIBLE);
                        rvAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri uri;
            if (requestCode == CAPTURE) {
                uri = Uri.fromFile(captureFile);
            } else {
                uri = data.getData();
            }
            if (uri == null) {
                return;
            }
            if (uri.toString().contains("video")) {
                //please select image files ,but video.
                return;
            }
            Bitmap bitmap = BitmapUtils.compressBitmap(activity, uri, 180, 180);
            if (replacePosition != -1) {
                images.remove(replacePosition);
                images.add(replacePosition, bitmap);
                replacePosition = -1;
            } else {
                images.add(bitmap);
                image_add.setVisibility(images.size() == 2 ? View.GONE : View.VISIBLE);
            }
            rvAdapter.notifyDataSetChanged();
        }
    }


    @OnClick({R.id.tv_telNum_de, R.id.tv_telNum_eu, R.id.tv_telNum1, R.id.et_type, R.id.image_add
            , R.id.bt_confirm})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_telNum_de:
            case R.id.tv_telNum_eu:
            case R.id.tv_telNum1:
                new RxPermissions(this).requestEach(Manifest.permission.CALL_PHONE).subscribe(permission -> {
                    if (permission.granted) {
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        Uri data = Uri.parse("tel:" + ((TextView) v).getText().toString().trim());
                        intent.setData(data);
                        startActivity(intent);
                    } else {
                        // 用户拒绝了该权限，并且选中『不再询问』
                        ToastUtils.showToast(context, getString(R.string.access_photo));
                    }
                }).dispose();
                break;
            case R.id.et_type:
                showPopup();
                break;
            case R.id.image_add:
                permissionFlag = 0;
                new RxPermissions(this).requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA).subscribe(permission -> {
                    if (permission.granted) {
//                        11 - 3 10 - 2 01 - 1 00 - 0
                        if (permission.name.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            permissionFlag += 2;
                        } else {
                            permissionFlag++;
                        }
                    }
                    if (permission.name.equals(Manifest.permission.CAMERA)) {
                        switch (permissionFlag) {
                            case 0:
                                ToastUtils.showToast(context, getString(R.string.access_camera_storage));
                                break;
                            case 1:
                                ToastUtils.showToast(context, getString(R.string.access_storage));
                                break;
                            case 2:
                                ToastUtils.showToast(context, getString(R.string.access_camera));
                                break;
                            case 3:
                                replacePosition = -1;
                                showPhotoDialog();
                                break;
                        }
                    }

                });
                break;
            case R.id.rl_photo:
                AlertDialogUtils.hidden(alertDialog);
                Intent intent_capture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    takePicUri = FileProvider.getUriForFile(context, getApplication().getPackageName() + ".provider", captureFile);
                    intent_capture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent_capture.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } else {
                    takePicUri = Uri.fromFile(captureFile);
                }
                intent_capture.putExtra(MediaStore.EXTRA_OUTPUT, takePicUri);
                startActivityForResult(intent_capture, CAPTURE);
                break;
            case R.id.rl_album:
                AlertDialogUtils.hidden(alertDialog);
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, ALBUM);
                break;
            case R.id.bt_confirm:
                String email = et_email.getText().toString().trim();
                if (!Utils.checkAccountUseful(email)) {
                    return;
                }
                String type = et_type.getText().toString().trim();
                if (TextUtils.isEmpty(type)) {
                    ToastUtils.showToast(context, getString(R.string.feedback_aty_type_null));
                    return;
                }
                String contents = et_content.getText().toString().trim();
                if (TextUtils.isEmpty(contents)) {
                    ToastUtils.showToast(context, getString(R.string.help_aty_content_isnull));
                    return;
                }
                if (Utils.isChinaEnvironment()) {
                    if (!UserUtils.isEmail(email) && !UserUtils.isPhone(email)) {
                        if (Utils.isSupportPhone()) {
                            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.regist_wrong_account));
                        } else {
                            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.regist_wrong_email));
                        }
                        return;
                    }
                } else {
                    if (!UserUtils.isEmail(email)) {
                        ToastUtils.showToast(context, getString(R.string.regist_wrong_email));
                        return;
                    }
                }
                dialog.show();
                commit(email, contents, type);
                break;
        }
    }

    private void initFile() {
        File imageFile = new File(Environment.getExternalStorageDirectory().getPath() + "/ilife");
        if (!imageFile.exists()) {
            imageFile.mkdirs();
        }
        captureFile = new File(imageFile, "capture.png");
        albumFile = new File(imageFile, "album.jpg");
    }

    public void showPopup() {
        KeyboardUtils.hideSoftInput(this);
        if (typePop == null) {
            View contentView = LayoutInflater.from(this).inflate(R.layout.typelist, null);
            initPopView(contentView);
            ListView listView = (ListView) contentView.findViewById(R.id.listView);
            listView.setAdapter(new ArrayAdapter<>(this, R.layout.simple_list_item, R.id.simple_list_item_textView, types));
            typePop = new PopupWindow(this);
            typePop.setContentView(contentView);
            typePop.setWidth(et_type.getWidth() + 4);
            typePop.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
            typePop.setBackgroundDrawable(new ColorDrawable());
            typePop.setOutsideTouchable(true);
            typePop.setFocusable(true);
        }
        if (!typePop.isShowing()) {
            int xOff = (int) (rl_type.getLeft() + getResources().getDimension(R.dimen.dp_30) - 2);
            typePop.showAsDropDown(rl_type, xOff, Utils.dip2px(this, 4));
        }
    }

    public void initPopView(View view) {
        ListView listView = (ListView) view.findViewById(R.id.listView);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            et_type.setText(types[position]);
            typePop.dismiss();
        });
    }

    class MyTextWatcher implements TextWatcher {
        private CharSequence temp;//监听前的文本
        private int editStart;//光标开始位置
        private int editEnd;//光标结束位置
        private final int charMaxNum = 140;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            temp = s;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            tv_numbs.setText(getString(R.string.help_aty_text_count, (charMaxNum - s.length()) + ""));
        }

        @Override
        public void afterTextChanged(Editable s) {
            editStart = et_content.getSelectionStart();
            editEnd = et_content.getSelectionEnd();
            if (temp.length() > charMaxNum) {
                ToastUtils.showToast(context, getString(R.string.feedback_aty_count_limit));
                s.delete(editStart - 1, editEnd);
                int tempSelection = editStart;
                et_content.setText(s);
                et_content.setSelection(tempSelection);
            }
        }
    }

    private void commit(String email, String contents, String type) {
        ACFeedback feedback = new ACFeedback();
        feedback.addFeedback("description", contents);
        feedback.addFeedback("telephoneNumber", email);
        feedback.addFeedback("deviceType", type);
        for (Bitmap bm : images) {
            byte[] img = BitmapUtils.bitmapToByte(bm);
            feedback.addFeedbackPicture("pictures", img);
        }

        AC.feedbackMgr().submitFeedback(feedback, new VoidCallback() {
            @Override
            public void success() {
                dialog.dismiss();
                ToastUtils.showToast(context, getString(R.string.help_aty_commit_suc));
                removeActivity();
            }

            @Override
            public void error(ACException e) {
                dialog.dismiss();
                MyLogger.d(TAG, getString(R.string.help_aty_commit) + e.getMessage() + "---" + e.getDescription());
                ToastUtils.showToast(context, getString(R.string.help_aty_commit));
            }
        });
    }


    public void showPhotoDialog() {
        if (alertDialog == null) {
            View contentView = inflater.inflate(R.layout.dialog_helt_photo, null);
            contentView.findViewById(R.id.rl_photo).setOnClickListener(this);
            contentView.findViewById(R.id.rl_album).setOnClickListener(this);
            int width = (int) getResources().getDimension(R.dimen.dp_300);
            int height = (int) getResources().getDimension(R.dimen.dp_80);
            int yOffset = (int) getResources().getDimension(R.dimen.dp_30);
            alertDialog = AlertDialogUtils.showDialogBottom(context, contentView, width, height, yOffset);
        } else {
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }
}
