package com.ilife.iliferobot.adapter;

import androidx.annotation.NonNull;

import com.ilife.iliferobot.R;
import com.ilife.iliferobot.base.BaseQuickAdapter;
import com.ilife.iliferobot.base.BaseViewHolder;
import com.ilife.iliferobot.model.bean.VoiceLanguageBean;

import java.util.List;

public class VoiceLanguageAdapter extends BaseQuickAdapter<VoiceLanguageBean, BaseViewHolder> {
    private int defaultLanguage;

    public VoiceLanguageAdapter(int layoutId, @NonNull List<VoiceLanguageBean> data) {
        super(layoutId, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, int position) {
        VoiceLanguageBean voiceLanguage = data.get(position);
        holder.setText(R.id.tv_voice_language, voiceLanguage.getLanguage());
        holder.setVisible(R.id.iv_voice_language, voiceLanguage.getLanguageCode() == defaultLanguage);
    }

    public void setDefaultLanguage(int defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }
}
