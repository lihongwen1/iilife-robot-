package com.ilife.iliferobot;

import android.view.View;

import androidx.test.espresso.FailureHandler;

import org.hamcrest.Matcher;

public class CustomFailureHandler implements FailureHandler {
    @Override
    public void handle(Throwable error, Matcher<View> viewMatcher) {
        if (viewMatcher.matches("登录")){
            System.out.println("the value is login");
        }else{
            System.out.println("the value is out of expected");
        }

    }
}
