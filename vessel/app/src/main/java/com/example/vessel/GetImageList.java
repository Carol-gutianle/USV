package com.example.vessel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONArray;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class GetImageList extends Activity implements View.OnClickListener{

    private List<Map<String, String>> listItem;
    public String url = "http://s7bp57.natappfree.cc/get_all_filename";

    private List<Map<String, String>> jsonJX(String data) {
        List<Map<String,String>> list = new ArrayList<>();
        if(data!=null) {
            try {
                JSONArray jsonArray = JSONArray.parseArray(data);
                //遍历数组
                for (int i = 0; i < jsonArray.size(); i++) {
                    //将字段的值遍历并转型
                    String filename = jsonArray.getJSONObject(i).getString("filename");
                    String filedate = jsonArray.getJSONObject(i).getString("filedate");
                    Map<String, String> map = new HashMap<>();
                    map.put("filename", filename);
                    map.put("filedate", filedate);
                    list.add(map);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return list;
    }
    /**
     * 从服务器获得适配器数据
     */

    private List<Map<String, String>> getData() throws IOException {
        /*获取图片名称*/
        OkHttpClient okhttpclient = new OkHttpClient();

        Request request = new Request.Builder().url("http://s7bp57.natappfree.cc/get_all_filename").build();
        Response response = okhttpclient.newCall(request).execute();
        ResponseBody resBody = response.body();
        String data = resBody.string();
        return jsonJX(data);
    }
    public static volatile String res = "hhh";

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_all);
//        getSync();
        CustomRunnable cRunnable = new CustomRunnable();
        Thread thread = new Thread(cRunnable,"子线程");
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        listItem = jsonJX(cRunnable.getData());
        System.out.println(listItem.size());
        SimpleAdapter adapter = new SimpleAdapter(this,listItem,R.layout.history_all_item,
                new String[]{"filename","filedate"},new int[]{R.id.filename,R.id.filedate});
        ListView listView = findViewById(R.id.lv_expense);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // 第二步：通过Intent跳转至新的页面
                Intent intent = new Intent(GetImageList.this, SingleImage.class);
                intent.putExtra("path",listItem.get(position).get("filename"));
                startActivity(intent);
            }
        });
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
        intent.setClass(GetImageList.this,ShowVideo.class);
        startActivity(intent);
    }

    private void goToHistory() {
        Intent intent = new Intent();
        intent.setClass(GetImageList.this,GetImageList.class);
        startActivity(intent);
    }

    static final class CustomRunnable implements Runnable {
        private String res = "";
        public void run() {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().url("http://z5be2m.natappfree.cc/get_all_filename").build();
            //准备好请求的Code对象
            Call call = okHttpClient.newCall(request);
            try {
                //同步请求要创建子线程,是因为execute()方法，会阻塞后面代码的执行
                //只有执行了execute方法之后,得到了服务器的响应response之后，才会执行后面的代码
                //所以同步请求要在子线程中完成
                Response response = call.execute();
//                    Log.i("test",response.body().string());
                res = response.body().string();
                //把服务器给我们响应的字符串数据打印出来
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        private String getData() {
            return res;
        }
    }




}