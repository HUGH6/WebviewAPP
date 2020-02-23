package com.webview.app.components;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/*
 * 自定义样式Toast
 */
public class CustomToast {
    private Toast mToast;
    public CustomToast(Context context, CharSequence text, int duration) {
        mToast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        mToast.setGravity(Gravity.CENTER, 0, 0);
//        toast.show();
//        mToast.setView(view);
//        toast.show();
    }

    public static CustomToast makeText(Context context, CharSequence text, int duration) {
        return new CustomToast(context, text, duration);
    }

    public void show() {
        if (mToast != null) {
            mToast.show();
        }
    }

//    public void setGravity(int gravity, int xOffset, int yOffset) {
//        if (mToast != null) {
//            mToast.setGravity(gravity, xOffset, yOffset);
//        }
//    }
}
