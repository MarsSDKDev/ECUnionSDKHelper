package com.ec.union.ecadhelper;

import android.app.Activity;

import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.ec.union.ad.sdk.Ut;
import com.ec.union.ad.sdk.platform.BaseEntry;
import com.ec.union.ad.sdk.platform.ECAdError;


public class Entry extends BaseEntry {

    public Entry() {
        sdkNm = Config.PLATFORM_NAME;
        sdkVer = Config.PLATFORM_VER;
        sdkPermission = Config.PLATFORM_PERMISSION;
    }

    @Override
    public void onApplicationCreate() {

        if (null == Ut.getCls(mContext.getClassLoader(), Config.CLS_NM)) {
            if (null != mInitListener) {
                mInitListener.onFail(new ECAdError(ECAdError.JAR_MAIN_CLS_NOT_EXIST, Config.AD_MAIN_CLS_NOT_EXIST));
            }
            return;
        }

        String appid = mInitParams.optString(Config.APP_ID);
        Ut.logI("tt appId: " + appid);
        if (Ut.isStringEmpty(appid)) {
            if (null != mInitListener) {
                mInitListener.onFail(new ECAdError(ECAdError.INIT_PARAM_EMPTY, "appid is null!!"));
            }
            return;
        }

        boolean isDebug = mInitParams.optBoolean(Config.IS_DEBUG,false);
        Ut.logI("tt isDebug: " + isDebug);

        TTAdSdk.init(mContext.getApplicationContext(),
                new TTAdConfig.Builder()
                        .appId(appid)
//                        .useTextureView(false) //使用TextureView控件播放视频,默认为SurfaceView,当有SurfaceView冲突的场景，可以使用TextureView
                        .appName(Ut.getAppName(mContext))
                        .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK)
                        .allowShowNotify(true) //是否允许sdk展示通知栏提示
                        .allowShowPageWhenScreenLock(true) //是否在锁屏场景支持展示广告落地页
                        .debug(isDebug) //测试阶段打开，可以通过日志排查问题，上线时去除该调用
                        .directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI, TTAdConstant.NETWORK_STATE_3G, TTAdConstant.NETWORK_STATE_4G) //允许直接下载的网络状态集合
//                        .supportMultiProcess(false) //是否支持多进程，true支持
                        .build());

        if (null != mInitListener) {
            mInitListener.onSuccess();
        }

    }

    @Override
    public void onActivityCreate(Activity activity) {

    }

    @Override
    public void onDestroy(Activity activity) {

    }

    @Override
    public void onStart(Activity activity) {

    }

    @Override
    public void onRestart(Activity activity) {

    }

    @Override
    public void onResume(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {

    }

    @Override
    public void onStop(Activity activity) {

    }
}
