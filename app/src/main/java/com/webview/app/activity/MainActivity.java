package com.webview.app.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.webview.app.R;
import com.webview.app.components.CustomToast;
import com.webview.app.utils.NetworkUtil;

public class MainActivity extends Activity {

    WebView webView;            // 自定义WebView，继承自WebView
    ImageView imageLauncher;    // webview加载时显示的加载图片

    boolean firstLaunch = true; // webview是否初次加载页面标志
    private Handler handler = new Handler();    // 定时任务

    String webUrl = "https://www.baidu.com";
    String errorUrl = "file:///android_asset/error.html";

    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData(); // 初始化webview等数据，加载网站
    }

    /**
     * 1.初始化webview
     * 2.加载网站
     */
    public void initData() {
        initWebView();

        // 判断网络是否可用，若不可用，则加载错误提示页面
        if(NetworkUtil.isNetworkAvailable(MainActivity.this)) {
            loadWeb(webUrl);
        } else {
            loadWeb(errorUrl);
        }
    }

    @SuppressWarnings("deprecation")
    private void initWebView(){
        // webview组件
        webView = (WebView)findViewById(R.id.webview);
        // 加载webview的加载页图片
        imageLauncher = (ImageView)findViewById(R.id.image_launcher);
        // webview设置
        final WebSettings webSettings = webView.getSettings();

        // 支持html中javascript解析
        webSettings.setJavaScriptEnabled(true);

        Runnable runnable = new Runnable() {
            @Override
            public void run() { // 5秒后执行该方法
                // handler自带方法实现定时器
                try {
                    imageLauncher.setVisibility(View.GONE); // 隐藏
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        // 判断是否是首次加载页面，若是首次，则显示加载页
        if (firstLaunch) {
            handler.postDelayed(runnable, 3000); // 5秒后执行runnable 的run方法
        }


        //此方法可以在webview中打开链接而不会跳转到外部浏览器
        webView.setWebViewClient(new WebViewClient() {
             @Override
             public boolean shouldOverrideUrlLoading(WebView view, String url) {
                 view.loadUrl(url);
                 return true;
             }

             @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                if (firstLaunch) {
                    webView.setVisibility(View.VISIBLE);
                    firstLaunch = false;
                }

                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);

                // 这里进行无网络或错误处理，具体可以根据errorCode的值进行判断，做跟详细的处理。
                // 判断网络连接
                if (!NetworkUtil.isNetworkAvailable(MainActivity.this)) {
                    CustomToast.makeText(MainActivity.this, "network not connected!", Toast.LENGTH_LONG).show();
                }
                // 跳转到错误页面
                webView.loadUrl("file:///android_asset/error.html");
            }
        });

        //设置支持html5本地存储，有些h5页面服务器做了缓存，webview控件也要设置，否则显示不出来页面
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheMaxSize(1024*1024*8);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        webSettings.setAppCachePath(appCachePath);
        // 可以读取文件缓存
        webSettings.setAllowFileAccess(true);
        //开启H5(APPCache)缓存功能
        webSettings.setAppCacheEnabled(true);

        //支持多窗口
        webSettings.setSupportMultipleWindows(true);

        webSettings.setBlockNetworkImage(false);

        webSettings.setLoadsImagesAutomatically(true);

        // android webview 从Lollipop(5.0)开始webview默认不允许混合模式，
        // https当中不能加载http资源，所以需要设置开启
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }


        /**
         * 设置Android的webview支持<input type="file"/>文件上传
         * 实现各个android版本的兼容
         */
        webView.setWebChromeClient(new WebChromeClient(){
            // For 3.0+ Devices (Start)
            protected void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            // For Lollipop 5.0+ Devices
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }
                uploadMessage = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e) {
                    uploadMessage = null;
                    CustomToast.makeText(getBaseContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }

            //For Android 4.1 only
            protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
            }


            protected void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }

            /**
             * 多窗口的问题
             */
//            private void newWin(WebSettings mWebSettings) {
//                //html中的_bank标签就是新建窗口打开，有时会打不开，需要加以下
//                //然后复写 WebChromeClient的onCreateWindow方法
//                mWebSettings.setSupportMultipleWindows(false);
//                mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);
//            }
        });

        //        webView.setScrollBarStyle(View.GONE);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);

        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.requestFocus();

    }

    @SuppressLint("JavascriptInterface")
    public void loadWeb(String url){
        webView.loadUrl(url);
    }


    /**
     * 重载onKeyDown的函数
     * 实现返回操作在页面内回退,而不是直接退出程序
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()){
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 选择图片，返回图片数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                uploadMessage = null;
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) {
                return;
            }

            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else {
            CustomToast.makeText(MainActivity.this, "choose picture failed", Toast.LENGTH_LONG).show();
        }
    }
}