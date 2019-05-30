package com.ilife.iliferobot.listener;

import com.accloud.service.ACException;

/**
 * Created by chengjiaping on 2018/9/3.
 */

public interface ReNameListener {
    void onSuccess();

    void onError(ACException e);
}
