package com.ilife.iliferobot.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

public class CustomPopupWindow extends PopupWindow {

    private CustomPopupWindow(Builder builder) {
        super(builder.context);

        builder.view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        setContentView(builder.view);
        setHeight(builder.height == 0 ? ViewGroup.LayoutParams.WRAP_CONTENT : builder.height);
        setWidth(builder.width == 0 ? ViewGroup.LayoutParams.WRAP_CONTENT : builder.width);
        setBackgroundDrawable(new ColorDrawable(0x00000000));//设置透明背景
        setOutsideTouchable(builder.cancelTouchout);//设置outside可点击
        setFocusable(builder.isFocusable);
        setTouchable(builder.isTouchable);

        if (builder.animStyle != 0) {
            setAnimationStyle(builder.animStyle);
        }
    }


    @Override
    public void dismiss() {
    }

    public void disMissPop(Activity activity) {
        super.dismiss();
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = 1.0f; //0.0-1.0
        activity.getWindow().setAttributes(lp);
    }

    public void showAtLocation(Activity activity, View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = 0.6f; //0.0-1.0
        activity.getWindow().setAttributes(lp);
    }

    public static final class Builder {

        private Context context;
        private int height, width;
        private boolean cancelTouchout;
        private boolean isFocusable = true;
        private boolean isTouchable = true;
        private View view;
        private int animStyle;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder view(int resView) {
            view = LayoutInflater.from(context).inflate(resView, null);
            return this;
        }

        public Builder view(View resVew) {
            view = resVew;
            return this;
        }

        public Builder heightpx(int val) {
            height = val;
            return this;
        }

        public Builder widthpx(int val) {
            width = val;
            return this;
        }

        public Builder heightdp(int val) {
            height = dip2px(context, val);
            return this;
        }

        public Builder widthdp(int val) {
            width = dip2px(context, val);
            return this;
        }

        public Builder heightDimenRes(int dimenRes) {
            height = context.getResources().getDimensionPixelOffset(dimenRes);
            return this;
        }

        public Builder widthDimenRes(int dimenRes) {
            width = context.getResources().getDimensionPixelOffset(dimenRes);
            return this;
        }

        public Builder cancelTouchout(boolean val) {
            cancelTouchout = val;
            return this;
        }

        public Builder isFocusable(boolean val) {
            isFocusable = val;
            return this;
        }

        public Builder isTouchable(boolean val) {
            isTouchable = val;
            return this;
        }

        public Builder animStyle(int val) {
            animStyle = val;
            return this;
        }

        public Builder addViewOnclick(int viewRes, View.OnClickListener listener) {
            view.findViewById(viewRes).setOnClickListener(listener);
            return this;
        }


        public CustomPopupWindow build() {

            return new CustomPopupWindow(this);
        }
    }

    @Override
    public int getWidth() {
        return getContentView().getMeasuredWidth();
    }

    /**
     * PopuWindow在安卓7布局大小兼容问题的处理
     * 在android7.0上，如果不主动约束PopuWindow的大小，比如，设置布局大小为 MATCH_PARENT,那么PopuWindow会变得尽可能大，
     * 以至于 view下方无空间完全显示PopuWindow，而且view又无法向上滚动，此时PopuWindow会主动上移位置，直到可以显示完全。
     * 　解决办法：主动约束PopuWindow的内容大小，重写showAsDropDown方法：
     *
     * @param anchor
     */
    @Override
    public void showAsDropDown(View anchor) {

        if (Build.VERSION.SDK_INT >= 24) {
            Rect visibleFrame = new Rect();
            anchor.getGlobalVisibleRect(visibleFrame);
            int height = anchor.getResources().getDisplayMetrics().heightPixels - visibleFrame.bottom;
            setHeight(height);
        }
        super.showAsDropDown(anchor);
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        if (Build.VERSION.SDK_INT >= 24) {
            Rect visibleFrame = new Rect();
            anchor.getGlobalVisibleRect(visibleFrame);
            int height = anchor.getResources().getDisplayMetrics().heightPixels - visibleFrame.bottom;
            setHeight(height);
        }
        super.showAsDropDown(anchor, xoff, yoff);
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
