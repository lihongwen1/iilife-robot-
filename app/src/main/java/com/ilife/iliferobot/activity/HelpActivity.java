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

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACException;
import com.accloud.service.ACFeedback;
import com.bumptech.glide.Glide;
import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.utils.AlertDialogUtils;
import com.ilife.iliferobot.utils.BitmapUtils;
import com.ilife.iliferobot.utils.DialogUtils;
import com.ilife.iliferobot.utils.KeyboardUtils;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.utils.UserUtils;
import com.ilife.iliferobot.utils.Utils;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;


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
    ScrollView scrollView;
    TextView tv_numbs, tv_telNum1;
    EditText et_email, et_type, et_content;
    RelativeLayout rl_type;
    ImageView image_add, image_1;
    TextView bt_confirm;
    File captureFile, albumFile;
    PopupWindow typePop;
    AlertDialog alertDialog;
    String[] types;
    int curResId;
    private Bitmap bitmap_1;
    private Bitmap bitmap_2;
    private byte[] byte1;
    private byte[] byte2;
    Activity activity;
    View view;
    ArrayList<String> permissions;
    int index;
    Uri takePicUri;
    Dialog dialog;
    @BindView(R.id.tv_top_title)
    TextView tv_title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        initView();
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
        types = getResources().getStringArray(R.array.device_name);
        tv_numbs = (TextView) findViewById(R.id.tv_numbs);
        et_email = (EditText) findViewById(R.id.et_email);
        et_type = (EditText) findViewById(R.id.et_type);
        et_content = (EditText) findViewById(R.id.et_content);
        image_1 = (ImageView) findViewById(R.id.image_1);
        image_add = (ImageView) findViewById(R.id.image_add);
        tv_telNum1 = findViewById(R.id.tv_telNum1);
        bt_confirm = (TextView) findViewById(R.id.bt_confirm);
        rl_type = (RelativeLayout) findViewById(R.id.rl_type);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        view = findViewById(R.id.view);
        tv_title.setText(R.string.personal_aty_help);
        image_1.setOnClickListener(this);
        et_type.setOnClickListener(this);
        image_add.setOnClickListener(this);
        tv_telNum1.setOnClickListener(new MyListener());
        bt_confirm.setOnClickListener(this);
        et_content.addTextChangedListener(new MyTextWatcher());

//        scrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
//        scrollView.setFocusable(true);
//        scrollView.setFocusableInTouchMode(true);
//        scrollView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                v.requestFocusFromTouch();
//                return false;
//            }
//        });

//        rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//            @Override
//            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                if(oldBottom != 0 && bottom != 0 &&(oldBottom - bottom > 0)){
//                    ToastUtils.showToast(context,"show");
//                }else if(oldBottom != 0 && bottom != 0 &&(bottom - oldBottom > 0)){
//                    ToastUtils.showToast(context,"hidden");
//                }
//            }
//        });
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
            switch (curResId) {
                case R.id.image_1:
                    bitmap_1 = BitmapUtils.compressBitmap(activity, uri, 180, 180);
                    byte1 = BitmapUtils.bitmapToByte(bitmap_1);
                    image_1.setVisibility(View.VISIBLE);
                    Glide.with(context).load(byte1).into(image_1);
                    break;
                case R.id.image_add:
                    if (image_1.getVisibility() == View.GONE) {
                        bitmap_1 = BitmapUtils.compressBitmap(activity, uri, 180, 180);
                        byte1 = BitmapUtils.bitmapToByte(bitmap_1);
                        image_1.setVisibility(View.VISIBLE);
                        Glide.with(context).load(byte1).into(image_1);
                    } else {
                        bitmap_2 = BitmapUtils.compressBitmap(activity, uri, 180, 180);
                        byte2 = BitmapUtils.bitmapToByte(bitmap_2);
                        Glide.with(context).load(byte2).into(image_add);
                    }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_telNum1:
//                Intent intent = new Intent(Intent.ACTION_DIAL);
                new RxPermissions(this).requestEach(Manifest.permission.CALL_PHONE).subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(@NonNull Permission permission) throws Exception {
                        if (permission.granted) {
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            Uri data = Uri.parse("tel:" + "4009368886");
                            intent.setData(data);
                            startActivity(intent);
                        } else {
                            // 用户拒绝了该权限，并且选中『不再询问』
                            ToastUtils.showToast(context, getString(R.string.access_photo));
                        }
                    }
                });
                break;
            case R.id.et_type:
                showPopup();
                break;
            case R.id.image_1:
                showPhotoDialog();
                curResId = v.getId();
                break;
            case R.id.image_add:
                index = 0;
                permissions.clear();
                new RxPermissions(this).requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA).subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(@NonNull Permission permission) throws Exception {
                        index++;
                        if (permission.granted) {
                            permissions.add(permission.name);
                        }
                        if (index == 2) {
                            if (permissions.size() == 2) {
                                showPhotoDialog();
                                curResId = R.id.image_add;
                            } else if (permissions.size() == 1) {
                                if (permissions.contains(Manifest.permission.CAMERA)) {
                                    ToastUtils.showToast(context, getString(R.string.access_storage));
                                } else {
                                    ToastUtils.showToast(context, getString(R.string.access_camera));
                                }
                            } else {
                                ToastUtils.showToast(context, getString(R.string.access_camera_storage));
                            }
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
//                if (AppUtils.isAppInstalled(context, "com.android.gallery3d")){
//                    Intent intent = new Intent(Intent.ACTION_PICK,
//                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                    intent.setPackage("com.android.gallery3d");
//                    startActivityForResult(intent, ALBUM);
//                }
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                intent.setPackage("com.android.gallery3d");
                startActivityForResult(intent, ALBUM);
                break;
            case R.id.bt_confirm:
                String email = et_email.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    ToastUtils.showToast(context, getString(R.string.login_aty_input_email));
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
                if (!UserUtils.isEmail(email)) {
                    ToastUtils.showToast(context, getString(R.string.login_aty_wrong_email));
                    return;
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
        permissions = new ArrayList<>();
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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                et_type.setText(types[position]);
                typePop.dismiss();
            }
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
        if (isByteUsable(byte1)) {
            feedback.addFeedbackPicture("pictures", byte1);
        }
        if (isByteUsable(byte2)) {
            feedback.addFeedbackPicture("pictures", byte2);
        }
        AC.feedbackMgr().submitFeedback(feedback, new VoidCallback() {
            @Override
            public void success() {
                dialog.dismiss();
                ToastUtils.showToast(context, getString(R.string.help_aty_commit_suc));
                finish();
            }

            @Override
            public void error(ACException e) {
                dialog.dismiss();
                ToastUtils.showToast(context, getString(R.string.help_aty_commit));
            }
        });
    }

    public boolean isByteUsable(byte[] bytes) {
        if (bytes != null && bytes.length > 0) {
            return true;
        }
        return false;
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

    class MyListener implements View.OnClickListener {
        @Override
        public void onClick(final View view) {
            new RxPermissions(activity).requestEach(Manifest.permission.CALL_PHONE).subscribe(new Consumer<Permission>() {
                @Override
                public void accept(@NonNull Permission permission) throws Exception {
                    if (permission.granted) {
                        String tel;
                        if (view.getId() == R.id.tv_telNum1) {
                            tel = "89299401228";
                        } else {
                            tel = "0034918607768";
                        }
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        Uri data = Uri.parse("tel:" + tel);
                        intent.setData(data);
                        startActivity(intent);
                    } else {
                        // 用户拒绝了该权限，并且选中『不再询问』
                        ToastUtils.showToast(context, getString(R.string.access_photo));
                    }
                }
            }).dispose();
        }
    }
}
