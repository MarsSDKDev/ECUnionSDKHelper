package com.ec.union.adhelper;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.ec.union.ad.sdk.Ut;
import com.ec.union.ad.sdk.api.ECAdType;
import com.ec.union.ad.sdk.platform.ECAdError;
import com.ec.union.ad.sdk.platform.IECAd;
import com.ec.union.ad.sdk.platform.IECAdListener;

import org.json.JSONObject;

import java.util.List;


public class Interstitial implements IECAd {

    private TTNativeExpressAd mTTAd;
    private boolean isRunLoad;
    private boolean isReady;

    @Override
    public void show(Activity activity, ViewGroup containner, String posId, JSONObject showParam, IECAdListener adListener) {
        if (isRunLoad) {
            showAd(activity, containner, posId, showParam, adListener);
        } else {
            loadAd(activity, containner, posId, showParam, adListener);
        }
    }


    private void showAd(final Activity activity, ViewGroup containner, final String posId, final JSONObject showParam, final IECAdListener adListener) {
        if (null != mTTAd) {
            mTTAd.setExpressInteractionListener(new TTNativeExpressAd.AdInteractionListener() {
                @Override
                public void onAdDismiss() {

                        Ut.stopVisual(posId);


                    if (adListener != null) {
                        adListener.onAdDismissed();
                    }
                }

                @Override
                public void onAdClicked(View view, int type) {
                    if (adListener != null) {
                        adListener.onAdClick();
                    }
                }

                @Override
                public void onAdShow(View view, int type) {
                    isReady = false;
                    if (adListener != null) {
                        adListener.onAdShow();
                    }


                        Ut.initVisual(activity,posId,showParam, ECAdType.INTERSTITIAL.getAdType(),null);
                        Ut.startVisual(posId);


                }

                @Override
                public void onRenderFail(View view, String msg, int code) {
                    isReady = false;
                    if (adListener != null) {
                        adListener.onAdFailed(new ECAdError(code, msg));
                    }
                }

                @Override
                public void onRenderSuccess(View view, float width, float height) {

                    mTTAd.showInteractionExpressAd(activity);
                }
            });
            mTTAd.render();
        } else {
            isReady = false;
            if (adListener != null) {
                adListener.onAdFailed(new ECAdError("ttAd is null..."));
            }
        }
    }

    private void loadAd(final Activity activity, final ViewGroup containner, final String posId, final JSONObject showParam, final IECAdListener adListener) {

        TTAdNative mTTAdNative = TTAdSdk.getAdManager().createAdNative(activity);

        float expressViewWidth = 350;
        float expressViewHeight = 0; // 350
//        try{
//            String eWstr = showParam.optString("expressViewWidth");
//            String eHstr = showParam.optString("expressViewHeight");
//            if (!Ut.isStringEmpty(eWstr) && !Ut.isStringEmpty(eHstr)) {
//                expressViewWidth = Float.parseFloat(eWstr);
//                expressViewHeight = Float.parseFloat(eHstr);
//            }
//
//        }catch (Exception e){
//            e.printStackTrace();
//            expressViewHeight = 0; //高度设置为0,则高度会自适应
//        }

        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(posId) //广告位id
                .setSupportDeepLink(true)
                .setAdCount(1) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(expressViewWidth,expressViewHeight) //期望模板广告view的size,单位dp
                .setImageAcceptedSize(640,320 )//这个参数设置即可，不影响模板广告的size
                .build();
        //加载广告
        mTTAdNative.loadInteractionExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {
                isReady = false;
                if (adListener != null) {
                    adListener.onAdFailed(new ECAdError(code, message));
                }

            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                if (ads == null || ads.size() == 0){
                    isReady = false;
                    if (adListener != null) {
                        adListener.onAdFailed(new ECAdError("ads is empty.."));
                    }
                    return;
                }
                if (mTTAd != null) {
                    mTTAd.destroy();
                }
                mTTAd = ads.get(0);
                isReady = true;

                if (adListener != null) {
                    adListener.onAdReady();
                }

                if (!isRunLoad) {
                    showAd(activity, containner, posId, showParam, adListener);
                }

            }
        });
    }


    @Override
    public void load(Activity activity, ViewGroup containner, String posId, JSONObject showParam, IECAdListener adListener) {
        isRunLoad = true;
        loadAd(activity, containner, posId, showParam, adListener);
    }

    @Override
    public boolean isReady() {
        return isReady;
    }

    @Override
    public void setVisibility(boolean visibility) {

    }

    @Override
    public void onPause(Activity activity) {

    }

    @Override
    public void onResume(Activity activity) {

    }

    @Override
    public void onStop(Activity activity) {

    }

    @Override
    public void onDestroy(Activity activity) {
        if (mTTAd != null) {
            mTTAd.destroy();
        }
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
