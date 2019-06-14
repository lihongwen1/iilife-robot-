package com.ilife.iliferobot.ui;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACDeviceMsg;
import com.accloud.service.ACException;
import com.ilife.iliferobot.able.Constants;
import com.ilife.iliferobot.able.MsgCodeUtils;
import com.ilife.iliferobot.utils.MyLogger;
import com.ilife.iliferobot.utils.ToastUtils;
import com.ilife.iliferobot.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chenjiaping on 2017/7/31.
 */

public class ControlPopupWindow extends PopupWindow implements View.OnClickListener {
    private final String TAG = ControlPopupWindow.class.getSimpleName();
    protected Context context;
    private ImageView image_right;
    private ImageView image_left;
    private ImageView image_back;
    private ImageView image_forward;

    private String physicalDeviceId;
    private String subdomain;
    private Timer timer;
    private Task timeTask;
    private ACDeviceMsg acDeviceMsg;
    private long lastDownTime;


    public ControlPopupWindow(Context context, String physicalDeviceId, String subdomain) {
        super(context);
        this.context = context;
        this.physicalDeviceId = physicalDeviceId;
        this.subdomain = subdomain;
        setContentView(initContentView());
        setHeight(initHeight());
        setWidth(initWidth());
        setTouchable(true);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable());
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        acDeviceMsg = new ACDeviceMsg();
        acDeviceMsg.setCode(MsgCodeUtils.Proceed);
        timer = new Timer();
        timeTask = new Task();
        timer.schedule(timeTask, 0, 4000);
    }

    public View initContentView() {
        View view = LayoutInflater.from(context).inflate(R.layout.window_direction, null);
        image_left = (ImageView) view.findViewById(R.id.image_left);
        image_right = (ImageView) view.findViewById(R.id.image_right);
        image_forward = (ImageView) view.findViewById(R.id.image_forward);
        image_back = (ImageView) view.findViewById(R.id.image_control_back);
        setListener();
        return view;
    }

    public void setListener() {
        image_left.setOnTouchListener(new MyTouchListener());
        image_right.setOnTouchListener(new MyTouchListener());
        image_forward.setOnTouchListener(new MyTouchListener());
        if (!subdomain.equals(Constants.subdomain_x800)) {
            image_back.setOnTouchListener(new MyTouchListener());
        }
    }

    public int initHeight() {
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        return height;
    }

    public int initWidth() {
        int width = WindowManager.LayoutParams.WRAP_CONTENT;
        return width;
    }

    @Override
    public void onClick(View v) {
        ACDeviceMsg acDeviceMsg = null;
        switch (v.getId()) {
            case R.id.image_left:
                acDeviceMsg = new ACDeviceMsg(MsgCodeUtils.Proceed, new byte[]{0x03});
                break;
            case R.id.image_right:
                acDeviceMsg = new ACDeviceMsg(MsgCodeUtils.Proceed, new byte[]{0x04});
                break;
            case R.id.image_forward:
                acDeviceMsg = new ACDeviceMsg(MsgCodeUtils.Proceed, new byte[]{0x01});
                break;
            case R.id.image_back:
                acDeviceMsg = new ACDeviceMsg(MsgCodeUtils.Proceed, new byte[]{0x02});
                break;
        }
        setAllUnEnable();
        sendToDeviceWithOption_process(acDeviceMsg, physicalDeviceId);
    }

    public void setAllUnEnable() {
        image_left.setEnabled(false);
        image_right.setEnabled(false);
        image_forward.setEnabled(false);
        image_back.setEnabled(false);
    }

    public void setAllEnable() {
        image_left.setEnabled(true);
        image_right.setEnabled(true);
        image_forward.setEnabled(true);
        image_back.setEnabled(true);
    }

    public void sendToDeviceWithOption(ACDeviceMsg deviceMsg, final String physicalDeviceId) {
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalDeviceId, deviceMsg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void success(ACDeviceMsg deviceMsg) {
                switch (deviceMsg.getCode()) {
                    case MsgCodeUtils.Proceed:
                        byte[] resp = deviceMsg.getContent();
                        break;
                }
            }

            @Override
            public void error(ACException e) {
                ToastUtils.showErrorToast(context, e.getErrorCode());
            }
        });
    }

    class MyTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                setAllUnEnable();
                v.setEnabled(true);
                v.setSelected(true);
                MyLogger.e(TAG, "onTouch  MotionEvent.ACTION_DOWN System.currentTimeMillis()-lastDownTime<400  " + (System.currentTimeMillis() - lastDownTime < 400));
                if (System.currentTimeMillis() - lastDownTime < 400) {
                    v.setSelected(false);
                    setAllEnable();
                    return false;
                } else {
                    lastDownTime = System.currentTimeMillis();
                }
                timeTask.setId(v.getId());
                timeTask.run();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                setAllEnable();
                v.setSelected(false);
                timeTask.setId(0);
                timeTask.run();
                acDeviceMsg.setContent(new byte[]{0x05});
                sendToDeviceWithOption(acDeviceMsg, physicalDeviceId);
                MyLogger.e(TAG, "MotionEvent.ACTION_UP   sendToDeviceWithOption 0x05");
            }
            return true;
        }
    }


    class Task extends TimerTask {
        int resId;

        public Task() {
            resId = 0;
        }

        public void setId(int id) {
            this.resId = id;
        }

        @Override
        public void run() {
            if (resId == 0) {
                return;
            }
            switch (resId) {
                case R.id.image_left:
                    acDeviceMsg.setContent(new byte[]{0x03});
                    break;
                case R.id.image_right:
                    acDeviceMsg.setContent(new byte[]{0x04});
                    break;
                case R.id.image_forward:
                    acDeviceMsg.setContent(new byte[]{0x01});
                    break;
                case R.id.image_back:
                    acDeviceMsg.setContent(new byte[]{0x02});
                    break;
            }
            sendToDeviceWithOption(acDeviceMsg, physicalDeviceId);
            MyLogger.e(TAG, "ACTION_DOWN Task run  sendToDeviceWithOption " + acDeviceMsg.getContent()[0]);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        ACDeviceMsg acDeviceMsg = new ACDeviceMsg(MsgCodeUtils.WorkMode, new byte[]{0x02});
        sendToDeviceWithOption(acDeviceMsg, physicalDeviceId);
        if (timer != null) {
            timer.cancel();
        }
    }

    public void sendToDeviceWithOption_process(ACDeviceMsg deviceMsg, final String physicalDeviceId) {
        AC.bindMgr().sendToDeviceWithOption(subdomain, physicalDeviceId, deviceMsg, Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
            @Override
            public void success(ACDeviceMsg deviceMsg) {
                setAllEnable();
            }

            @Override
            public void error(ACException e) {
                ToastUtils.showErrorToast(context, e.getErrorCode());
                setAllEnable();
            }
        });
    }
}
