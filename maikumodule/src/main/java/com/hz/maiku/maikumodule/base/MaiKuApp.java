package com.hz.maiku.maikumodule.base;

import android.content.Context;
import android.content.Intent;
import android.media.ResourceBusyException;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.baidu.crabsdk.CrabSDK;
import com.hz.maiku.maikumodule.bean.AppProcessInfornBean;
import com.hz.maiku.maikumodule.exception.CustomCrashHandler;
import com.hz.maiku.maikumodule.manager.AddressListManager;
import com.hz.maiku.maikumodule.manager.CallLogManager;
import com.hz.maiku.maikumodule.manager.CleanManager;
import com.hz.maiku.maikumodule.manager.JunkCleanerManager;
import com.hz.maiku.maikumodule.manager.MemoryManager;
import com.hz.maiku.maikumodule.manager.NotificationsManager;
import com.hz.maiku.maikumodule.manager.ProcessManager;
import com.hz.maiku.maikumodule.manager.SMSManager;
import com.hz.maiku.maikumodule.service.HideAppService;
import com.hz.maiku.maikumodule.service.LoadAppListService;
import com.hz.maiku.maikumodule.service.LockService;
import com.hz.maiku.maikumodule.util.SpHelper;
import com.hz.maiku.maikumodule.util.XmlFileUtil;

import org.litepal.LitePalApplication;
import java.util.List;
import java.util.Map;


/**
 * Created by Shurrik on 2017/11/29.
 */

public class MaiKuApp extends LitePalApplication {

    private static  final  String  TAG =MaiKuApp.class.getName();
    public static Context mContext;
    public static List<AppProcessInfornBean> cpuLists;

    @Override
    public void onCreate() {
        super.onCreate();
        this.mContext = this;

        initConfig();
        initManagers();
        initServices();
        initAppsFlyer();
        initBaiduCrab();
    }



    private void initConfig() {
        try {
            String ADMOB_ID =Config.getPropertiesURL(getApplicationContext(),"ADMOB_ID");
            String FACEBOOK_ID =Config.getPropertiesURL(getApplicationContext(),"FACEBOOK_ID");
            String BAIDU_KEY =Config.getPropertiesURL(getApplicationContext(),"BAIDU_KEY");
            String AF_DEV_KEY =Config.getPropertiesURL(getApplicationContext(),"AF_DEV_KEY");
            String COLORPRIMARY =Config.getPropertiesURL(getApplicationContext(),"COLORPRIMARY");
            String COLORPRIMARYDARK =Config.getPropertiesURL(getApplicationContext(),"COLORPRIMARYDARK");
            String PACKAGENAMEURL =Config.getPropertiesURL(getApplicationContext(),"PACKAGENAMEURL");
            if(!TextUtils.isEmpty(ADMOB_ID)){
                Constant.ADMOB_ID =ADMOB_ID;
            }
            if(!TextUtils.isEmpty(FACEBOOK_ID)){
                Constant.FACEBOOK_ID =FACEBOOK_ID;
            }
            if(!TextUtils.isEmpty(BAIDU_KEY)){
                Constant.BAIDU_KEY =BAIDU_KEY;
            }
            if(!TextUtils.isEmpty(AF_DEV_KEY)){
                Constant.AF_DEV_KEY =AF_DEV_KEY;
            }

            if(!TextUtils.isEmpty(COLORPRIMARY)){
                Constant.COLORPRIMARY =COLORPRIMARY;
            }

            if(!TextUtils.isEmpty(COLORPRIMARYDARK)){
                Constant.COLORPRIMARYDARK =COLORPRIMARYDARK;
            }

            if(!TextUtils.isEmpty(COLORPRIMARYDARK)){
                Constant.PACKAGENAMEURL =PACKAGENAMEURL;
            }

        }catch (Exception e){

        }
    }


    private void initManagers() {
        MemoryManager.init(this);
        ProcessManager.init(getApplicationContext());
        CleanManager.init(this);
        JunkCleanerManager.init(this);
        SpHelper.getInstance().init(this);
        AddressListManager.init(getApplicationContext());
        CallLogManager.init(getApplicationContext());
        SMSManager.init(getApplicationContext());
        NotificationsManager.init(getApplicationContext());
        boolean isInit = (boolean) SpHelper.getInstance().get(Constant.NOTIFICATION_INIT_APP,false);
        if(!isInit){
            NotificationsManager.getmInstance().initAppSelectState();
            SpHelper.getInstance().put(Constant.NOTIFICATION_INIT_APP,true);
        }
    }

    /**
     * 初始化服务
     */
    private void initServices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, LoadAppListService.class));
        } else {
            startService(new Intent(this, LoadAppListService.class));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, HideAppService.class));
        } else {
            startService(new Intent(this, HideAppService.class));
        }


        boolean lockState = (boolean) SpHelper.getInstance().get(Constant.LOCK_STATE, false);
        if (lockState) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, LockService.class));
            } else {
                startService(new Intent(this, LockService.class));
            }
        }

    }

    /**
     * 初始化AppsFlyer
     */
    private void initAppsFlyer() {
        AppsFlyerConversionListener conversionDataListener =
                new AppsFlyerConversionListener() {
                    @Override
                    public void onInstallConversionDataLoaded(Map<String, String> conversionData) {
                        for (String attrName : conversionData.keySet()) {
                            Log.d(AppsFlyerLib.LOG_TAG, "attribute: " + attrName + " = " + conversionData.get(attrName));
                        }
                    }

                    @Override
                    public void onInstallConversionFailure(String errorMessage) {
                        Log.d(AppsFlyerLib.LOG_TAG, "error getting conversion data: " + errorMessage);
                    }

                    @Override
                    public void onAppOpenAttribution(Map<String, String> conversionData) {

                    }

                    @Override
                    public void onAttributionFailure(String errorMessage) {
                        Log.d(AppsFlyerLib.LOG_TAG, "error onAttributionFailure : " + errorMessage);
                    }
                };
        //Your dev key is accessible from the AppsFlyer Dashboard under the Configuration section inside App Settings
        AppsFlyerLib.getInstance().init(Constant.AF_DEV_KEY, conversionDataListener, getApplicationContext());
        AppsFlyerLib.getInstance().startTracking(this);
    }

    /**
     * 初始化百度Crab
     */
    private void initBaiduCrab() {
        CrabSDK.init(this, Constant.BAIDU_KEY);
        // 开启卡顿捕获功能, 传入每天上传卡顿信息个数，-1代表不限制, 已自动打开
        CrabSDK.enableBlockCatch(-1);
        CrabSDK.setUploadLimitOfSameCrashInOneday(-1);
        CrabSDK.setUploadLimitOfCrashInOneday(-1);
        CrabSDK.setBlockThreshold(2000);
        /* 初始化全局异常捕获信息 */
        CustomCrashHandler customCrashHandler = CustomCrashHandler.getInstance();
        customCrashHandler.init(this);
    }


    public static Context getmContext() {
        return mContext;
    }
}