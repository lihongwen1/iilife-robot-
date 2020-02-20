package com.ilife.iliferobot.activity;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACDeviceMsg;
import com.accloud.service.ACException;
import com.ilife.iliferobot.R;
import com.ilife.iliferobot.able.ACSkills;
import com.ilife.iliferobot.able.Constants;
import com.ilife.iliferobot.able.MsgCodeUtils;
import com.ilife.iliferobot.adapter.VoiceLanguageAdapter;
import com.ilife.iliferobot.app.MyApplication;
import com.ilife.iliferobot.base.BackBaseActivity;
import com.ilife.iliferobot.base.BaseQuickAdapter;
import com.ilife.iliferobot.model.bean.VoiceLanguageBean;
import com.ilife.iliferobot.utils.MyLogger;
import com.ilife.iliferobot.utils.SpUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class VoiceLanguageActivity extends BackBaseActivity {
    @BindView(R.id.rv_voice_language)
    RecyclerView rv_voice_language;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.fl_confirm_language)
    FrameLayout fl_confirm_language;
    private VoiceLanguageAdapter voiceLanguageAdapter;
    private List<VoiceLanguageBean> languageBeans = new ArrayList<>();
    private String subdomain, physicalId;
    private static final String TAG = "VoiceLanguageActivity";
    private int defaultLanguage;

    @Override
    public int getLayoutId() {
        return R.layout.activity_voice_language;
    }

    @Override
    public void initView() {
        tv_title.setText(R.string.setting_aty_robot_voice);
        rv_voice_language.setLayoutManager(new LinearLayoutManager(this));
        voiceLanguageAdapter = new VoiceLanguageAdapter(R.layout.item_voice_language, languageBeans);
        rv_voice_language.setAdapter(voiceLanguageAdapter);
        voiceLanguageAdapter.setOnItemClickListener((adapter, view, position) -> {
            int selectLanguage = languageBeans.get(position).getLanguageCode();
            if (selectLanguage == defaultLanguage) {
                return;
            }
            defaultLanguage = selectLanguage;

            voiceLanguageAdapter.setDefaultLanguage(defaultLanguage);
            voiceLanguageAdapter.notifyDataSetChanged();

            fl_confirm_language.setSelected(true);
            MyLogger.d(TAG, "code=:" + defaultLanguage);
        });
    }

    @Override
    public void initData() {
        super.initData();
        subdomain = SpUtils.getSpString(this, MainActivity.KEY_SUBDOMAIN);
        physicalId = SpUtils.getSpString(this, MainActivity.KEY_PHYCIALID);
        defaultLanguage = SpUtils.getInt(this, physicalId + SettingActivity.KEY_DEFAULT_LANGUAGE);
        MyLogger.e(TAG, "默认语言:" + defaultLanguage);
        String[] languages = getResources().getStringArray(R.array.array_voice_language);
        VoiceLanguageBean voiceLanguageBean;
        int languageCode = 0x06;//捷克
        for (String language : languages) {
            voiceLanguageBean = new VoiceLanguageBean(language, languageCode);
            if (languageCode == defaultLanguage) {//已设置的语言排在第一位
                languageBeans.add(0, voiceLanguageBean);
            } else {
                languageBeans.add(voiceLanguageBean);
            }
            languageCode++;
        }
        voiceLanguageAdapter.setDefaultLanguage(defaultLanguage);
        voiceLanguageAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.fl_confirm_language)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fl_confirm_language://确认语言设置
                showLoadingDialog();
                AC.bindMgr().sendToDeviceWithOption(subdomain, physicalId, ACSkills.get().setVoiceLanguage(defaultLanguage), Constants.CLOUD_ONLY, new PayloadCallback<ACDeviceMsg>() {
                    @Override
                    public void success(ACDeviceMsg acDeviceMsg) {
                        hideLoadingDialog();
                        SpUtils.saveInt(MyApplication.getInstance(), physicalId + SettingActivity.KEY_DEFAULT_LANGUAGE, defaultLanguage);
                        MyLogger.d(TAG, "切换语言成功:" + acDeviceMsg.getContent()[0]);
                        //finish
                        removeActivity();
                    }

                    @Override
                    public void error(ACException e) {
                        hideLoadingDialog();
                        MyLogger.e(TAG, "切换语言失败，reason : " + e.getMessage());
                    }
                });
                break;
        }
    }
}
