package com.capton.chunmengassistant;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BrowserActivity extends AppCompatActivity implements View.OnClickListener,View.OnTouchListener{

    private String url;
    private TextView infoTitleTv;
    private EditText editUrlEt;
    private ImageView closeBtn,refreshBtn,goBtn;
    private LinearLayout infoBarLayout,editBarLayout;
    private RelativeLayout touchLayout;

    private WebView webView;
    private WebViewClient webviewClient;
    private ProgressBar loadProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        url=getIntent().getStringExtra("url");

        setContentView(R.layout.activity_browser);
        initView();
        setListener();

    }

    private void setListener() {
        infoTitleTv.setOnClickListener(this);
        editUrlEt.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
        goBtn.setOnClickListener(this);
        refreshBtn.setOnClickListener(this);
        touchLayout.setOnTouchListener(this);

    }

    private String mTitle="";
    private void initView() {
        infoTitleTv= (TextView) findViewById(R.id.infoTitle);
        editUrlEt= (EditText) findViewById(R.id.editUrl);
        closeBtn= (ImageView) findViewById(R.id.closeBtn);
        goBtn= (ImageView) findViewById(R.id.goBtn);
        refreshBtn= (ImageView) findViewById(R.id.refresh);
        infoBarLayout= (LinearLayout) findViewById(R.id.infoBar);
        editBarLayout= (LinearLayout) findViewById(R.id.editBar);
        touchLayout= (RelativeLayout) findViewById(R.id.touchLayout);
        webView= (WebView) findViewById(R.id.webView);
        loadProgressBar= (ProgressBar) findViewById(R.id.loadProgressBar);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                 loadProgressBar.setProgress(newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                mTitle=title;
                infoTitleTv.setText(mTitle);
            }
        });
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                view.loadUrl(url);
                return true;
            }

        });

        editBarLayout.setVisibility(View.INVISIBLE);
        infoTitleTv.setText(url);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.infoTitle:
                editBarLayout.setVisibility(View.VISIBLE);
                infoBarLayout.setVisibility(View.INVISIBLE);
                isEditBarShown=true;
                editUrlEt.setText(url);
                editUrlEt.setFocusable(true);
                editUrlEt.setFocusableInTouchMode(true);
                editUrlEt.requestFocus();
                BrowserActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                break;
            case R.id.closeBtn:
               finish();
                break;
            case R.id.goBtn:
               url=editUrlEt.getText().toString();
                webView.loadUrl(url);
                webView.reload();
                break;
            case R.id.refresh:
                webView.loadUrl(url);
                break;
        }
    }




    private boolean isEditBarShown;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getKeyCode()==KeyEvent.KEYCODE_BACK){
            if(isEditBarShown) {
                editBarLayout.setVisibility(View.INVISIBLE);
                infoBarLayout.setVisibility(View.VISIBLE);
                isEditBarShown=false;
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()){
            case R.id.touchLayout:
                if(event.getAction()==MotionEvent.ACTION_DOWN) {
                    if (isEditBarShown) {
                        editBarLayout.setVisibility(View.INVISIBLE);
                        infoBarLayout.setVisibility(View.VISIBLE);
                        isEditBarShown = false;
                        return true;
                    }
                }
                break;
        }
        return false;
    }


}
