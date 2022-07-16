package com.example.vessel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Callback;


public class SingleImage extends AppCompatActivity implements View.OnClickListener{

    private ImageButton history_button;
    private ImageButton video_button;
    private final String image_url = " http://z5be2m.natappfree.cc/image?path=";

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_single);
        final Intent intent = getIntent();
        String suf = intent.getStringExtra("path");
        System.out.println("参数" + suf);
        //在布局里面放一个ImageView,放网络请求后的图片
        final ImageView image = (ImageView) findViewById(R.id.image_1);
        final ImageView mainImage = (ImageView) findViewById(R.id.mainImage);
        //初始化OkHttpClient
        final OkHttpClient client = new OkHttpClient();

        //创建OkHttpClient针对某个url的数据请求
        Request request = new Request.Builder().url(image_url+suf).build();

        Call call = client.newCall(request);
        /*设置按钮跳转*/
        history_button = findViewById(R.id.history_button);
        history_button.setOnClickListener(this);
        video_button = findViewById(R.id.video_button);
        video_button.setOnClickListener(this);
        //请求加入队列
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //此处处理请求失败的业务逻辑
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //我写的这个例子是请求一个图片
                //response的body是图片的byte字节
                byte[] bytes = response.body().bytes();
                //response.body().close();

                //把byte字节组装成图片
                final Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                //回调是运行在非ui主线程，
                //数据请求成功后，在主线程中更新
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //网络图片请求成功，更新到主线程的ImageView
                        image.setImageBitmap(bmp);
                        mainImage.setImageBitmap(bmp);
                    }
                });
            }
        });
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
        intent.setClass(SingleImage.this,ShowVideo.class);
        startActivity(intent);
    }

    private void goToHistory() {
        Intent intent = new Intent();
        intent.setClass(SingleImage.this,GetImageList.class);
        startActivity(intent);
    }
}