package com.example.vessel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class ShowVideo extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //去掉标题栏
        supportRequestWibaawndowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        // 实例化组件
        WebView shipView = (WebView) findViewById(R.id.shipVideo);
        // 开启javascript 渲染
        shipView.getSettings().setJavaScriptEnabled(true);
        // 处理各种浏览器事件
        shipView.setWebViewClient(new WebViewClient());
        // 载入内容,这个根据你自己的链接来
        shipView.loadUrl("http://xngqu9.natappfree.cc");
        /*设置按钮跳转*/
        ImageButton history_button = findViewById(R.id.history_button);
        history_button.setOnClickListener(this);
        ImageButton video_button = findViewById(R.id.video_button);
        video_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.history_button) {
            goToHistory();
        } else if(v.getId() == R.id.video_button) {
            goToVideo();
        }
    }

    private void goToVideo() {
        Intent intent = new Intent();
        intent.setClass(ShowVideo.this,ShowVideo.class);
        startActivity(intent);
    }

    private void goToHistory() {
        Intent intent = new Intent();
        intent.setClass(ShowVideo.this,GetImageList.class);
        startActivity(intent);
    }
}