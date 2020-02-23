package com.webview.app.components;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.webkit.WebView;

public class CustomWebView extends WebView {
    public interface PlayFinish{
        void After();
    }

    PlayFinish df;

    public void setDf(PlayFinish playFinish) {
        this.df = playFinish;
    }
    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomWebView(Context context) {
        super(context);
    }

    //onDraw表示显示完毕
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        df.After();
    }
}
