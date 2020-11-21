package com.ec.union.ecadhelper;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTSplashAd;
import com.ec.union.ad.sdk.Ut;
import com.ec.union.ad.sdk.api.ECAdType;
import com.ec.union.ad.sdk.platform.ECAdError;
import com.ec.union.ad.sdk.platform.IECAd;
import com.ec.union.ad.sdk.platform.IECAdListener;

import org.json.JSONObject;


public class Splash implements IECAd {

    private boolean isFirstRun;
    //是否强制跳转到主页面
    private boolean mForceGoMain;
    private IECAdListener hbAdListener;
    private String mPosId;

    @Override
    public void show(final Activity activity, final ViewGroup containner, final String posId, final JSONObject showParam, final IECAdListener adListener) {
        UIUtils.debugToast(activity,"调用 '展示' " + Splash.class.getSimpleName() + "广告.");
        mPosId = posId;
        hbAdListener = adListener;

        try {
            Class cl = Class.forName("pl.droidsonroids.gif.GifTextureView");
        } catch (Exception e) {

            if (null != adListener) {
                adListener.onAdFailed(new ECAdError("gif arr does not exist."));
            }
            return;

        }
        if (isFirstRun) { //因为开屏只回调第一次的加载。重复调用开屏没有反应
            if (null != adListener) {
                adListener.onAdFailed(new ECAdError("开屏只能调用一次。"));
            }
            return;
        }
        isFirstRun = true;


        int width = 1080;
        int height = 1920;

        int timeout = 5000;
        String timeoutStr = showParam.optString(Config.SPLASH_TIMEOUT);
        if (!Ut.isStringEmpty(timeoutStr)) {
            try {
                timeout = Integer.parseInt(timeoutStr);
            } catch (Exception ignored) {
            }
        }

        AdSlot adSlot = null;

        boolean isExpress = showParam.optBoolean(Config.IS_EXPRESS, false);

        if (isExpress) {
            //个性化模板广告需要传入期望广告view的宽、高，单位dp，请传入实际需要的大小，
            //比如：广告下方拼接logo、适配刘海屏等，需要考虑实际广告大小
            float expressViewWidth = UIUtils.getScreenWidthDp(activity);
            float expressViewHeight = UIUtils.getHeight(activity);
            adSlot = new AdSlot.Builder()
                    .setCodeId(posId)
                    .setSupportDeepLink(true)
                    .setImageAcceptedSize(1080, 1920)
                    //模板广告需要设置期望个性化模板广告的大小,单位dp,代码位是否属于个性化模板广告，请在穿山甲平台查看
                    .setExpressViewAcceptedSize(expressViewWidth, expressViewHeight)
                    .build();
        } else {
            adSlot = new AdSlot.Builder()
                    .setCodeId(posId)
                    .setSupportDeepLink(true)
                    .setImageAcceptedSize(1080, 1920)
                    .build();
        }

        TTAdNative mTTAdNative = TTAdSdk.getAdManager().createAdNative(activity);
        //step4:请求广告，调用开屏广告异步请求接口，对请求回调的广告作渲染处理
        mTTAdNative.loadSplashAd(adSlot, new TTAdNative.SplashAdListener() {
            @Override
            public void onError(int i, String s) {

                if (null != adListener) {
                    adListener.onAdFailed(new ECAdError(i, s));
                }

            }

            @Override
            public void onTimeout() {
                if (null != adListener) {
                    adListener.onAdFailed(new ECAdError("splash timeout"));
                }
            }

            @Override
            public void onSplashAdLoad(TTSplashAd ttSplashAd) {
                if (null == ttSplashAd) {
                    if (null != adListener) {
                        adListener.onAdFailed(new ECAdError("null == ttSplashAd"));
                    }
                    return;
                }
                //获取SplashView
                View splashView = ttSplashAd.getSplashView();
                containner.addView(splashView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                //设置SplashView的交互监听器
                ttSplashAd.setSplashInteractionListener(new TTSplashAd.AdInteractionListener() {
                    @Override
                    public void onAdClicked(View view, int type) {

                        if (null != adListener) {
                            adListener.onAdClick();
                        }
                    }

                    @Override
                    public void onAdShow(View view, int type) {
                        if (null != adListener) {
                            adListener.onAdShow();
                        }

                        Ut.initVisual(activity, posId, showParam, ECAdType.SPLASH.getAdType(), null);
                        Ut.startVisual(posId);

                    }

                    @Override
                    public void onAdSkip() {

                        Ut.stopVisual(posId);

                        if (null != adListener) {
                            adListener.onAdDismissed();
                        }

                    }

                    @Override
                    public void onAdTimeOver() {

                        Ut.stopVisual(posId);

                        if (null != adListener) {
                            adListener.onAdDismissed();
                        }
                    }
                });
            }
        }, timeout);


    }

    @Override
    public void load(Activity activity, ViewGroup containner, String posId, JSONObject showParam, IECAdListener adListener) {

    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setVisibility(boolean visibility) {

    }

    @Override
    public void onPause(Activity activity) {

    }

    @Override
    public void onResume(Activity activity) {
        //用户点击后切换界面后再次进入开屏界面，判断是否该跳转到主页面
        if (mForceGoMain) {
            if (null != mPosId) {
                Ut.stopVisual(mPosId);
            }
            if (null != hbAdListener) {
                hbAdListener.onAdDismissed();
            }
        }
    }

    @Override
    public void onStop(Activity activity) {
        mForceGoMain = true;
    }

    @Override
    public void onDestroy(Activity activity) {

    }

    @Override
    public void onRestart(Activity activity) {

    }

    @Override
    public void onStart(Activity activity) {

    }

    @Override
    public void onNewIntent(Activity activity, Intent intent) {

    }

    @Override
    public void onSaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onConfigurationChanged(Activity activity, Configuration newConfig) {

    }
}
