package com.doro4028.iggcleaner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.hz.maiku.maikumodule.util.ToastUtil;
import com.doro4028.iggcleaner.main.MainActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.lang.ref.WeakReference;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author heguogui
 * @version v 1.0.0
 * @describe
 * @date 2019/2/21
 * @email 252774645@qq.com
 */
public class WelcomeActivity extends AppCompatActivity {
    private static final int SHOW_TIME = 2500;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what==0){
                showPermissions();
            }
            return false;
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        new Handler().postDelayed(new MyRunnable(WelcomeActivity.this), SHOW_TIME);
    }

    private class MyRunnable implements Runnable {
        WeakReference<WelcomeActivity> mActivity;

        public MyRunnable(WelcomeActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            if (mActivity.get() == null) {
                return;
            }
            Message message = new Message();
            message.what=0;
            mHandler.sendMessage(message);
        }
    }


    private void showPermissions() {

        try {
            if (WelcomeActivity.this != null) {
                RxPermissions rxPermission = new RxPermissions(WelcomeActivity.this);
                if(rxPermission!=null){
                    rxPermission.request(Manifest.permission.CLEAR_APP_CACHE,
                            Manifest.permission.DELETE_CACHE_FILES
                    ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(aBoolean -> {

                    });

                    RxPermissions rxPermission1 = new RxPermissions(WelcomeActivity.this);
                    rxPermission1.request(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_PHONE_STATE
                    ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(aBoolean -> {
                        if (!aBoolean) {
                            ToastUtil.showToast(WelcomeActivity.this, "Sorry! no permission, some functions are not available");
                            showPermissions();
                        } else {
                            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                            finish();
                        }

                    });
                }else {
                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                    finish();
                }
            }else {
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                finish();
            }
        }catch (Exception e){

        }
    }
}
