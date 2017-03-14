package com.xiayu.progressivedemo;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MainActivity extends Activity {


    private static final String BITMAP = "bitmap";
    private ImageView iv1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bitmap bitmap = msg.getData().getParcelable(BITMAP);
            if (bitmap != null) {
                iv1.setImageBitmap(bitmap);
            }
        }
    };
    private String url;
    HttpUrlFetcher httpUrl = new HttpUrlFetcher();
    private boolean isCancel;

    public void show(View v) {
        isCancel = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = httpUrl.loadData(new URL(url));
                    byte[]      mByte       = new byte[httpUrl.getSize()];
                    byte        lastOne     = 0;
                    byte        lastTwo     = 0;
                    int         offest      = 0;
                    while (!isCancel) {
                        //本次读取的字节
                        byte[] get = getBytes(inputStream);
                        //放入本次读取的数据
                        System.arraycopy(get, 0, mByte, offest, get.length);
                        offest = offest + get.length;
                        //记录最后两位字符
                        lastOne = mByte[offest - 1];
                        lastTwo = mByte[offest - 2];
                        //替换掉最后两个字节为FFD9,否则无法转化成bitmap
                        mByte[offest - 2] = -1;
                        mByte[offest - 1] = -39;
                        //生成bitmap
                        Bitmap result = BitmapFactory.decodeByteArray(mByte, 0, offest);
                        //还原最后两个字节
                        mByte[offest - 2] = lastTwo;
                        mByte[offest - 1] = lastOne;
                        Message obtain = Message.obtain();
                        Bundle  bundle = new Bundle();
                        bundle.putParcelable(BITMAP, result);
                        obtain.setData(bundle);
                        handler.sendMessage(obtain);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    public static byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        //这里设置每次读取的数量,设置小一点是为了让效果更明显
        byte[] buffer = new byte[10]; // 用数据装
        int    len    = -1;
        //要实现比较理想的渐进式加载效果,其实不应该写死每次读取量,应该是根据FFDA来判断读到第几帧了
        if ((len = is.read(buffer)) != -1) {
            outstream.write(buffer, 0, len);
        } else {
            is.close();
        }

        outstream.close();
        // 关闭流一定要记得。
        return outstream.toByteArray();
    }

    public void hide(View v) {
        isCancel = true;
        handler.removeCallbacks(null);
        iv1.setImageResource(R.mipmap.ic_launcher);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        url = "http://www.reasoft.com/tutorials/web/img/progress.jpg";
        iv1 = (ImageView) findViewById(R.id.iv1);


    }

}
