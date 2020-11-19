package com.ec.union.adhelper;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ViewGroup;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.ec.union.ad.sdk.Ut;
import com.ec.union.ad.sdk.api.ECAdType;
import com.ec.union.ad.sdk.platform.ECAdError;
import com.ec.union.ad.sdk.platform.IECAd;
import com.ec.union.ad.sdk.platform.IECAdListener;

import org.json.JSONObject;


public class FullVideo implements IECAd {

    private TTFullScreenVideoAd mTTFullScreenVideoAd;

    private boolean isFullVideoReady;
    private boolean isPreload;

    @Override
    public void show(Activity activity, ViewGroup containner, String posId, JSONObject showParam, IECAdListener adListener) {

        isPreload = showParam.optBoolean(Config.IS_PRELOAD, false);

        if (isPreload) {
            if (null != mTTFullScreenVideoAd) {
                if (isFullVideoReady) {
                    mTTFullScreenVideoAd.showFullScreenVideoAd(activity);
                } else {
                    if (null != adListener) {
                        adListener.onAdFailed(new ECAdError("The full video is not ready."));
                    }
                }

            } else {
                if (null != adListener) {
                    adListener.onAdFailed(new ECAdError(ECAdError.AD_LOAD_FAIL, "The full video has not been loaded."));
                }
            }

        } else {
            loadAd(activity, containner, posId, showParam, adListener);
        }

    }


    public void loadAd(final Activity activity, final ViewGroup containner, final String posId, final JSONObject showParam, final IECAdListener adListener) {


        TTAdNative mTTAdNative = TTAdSdk.getAdManager().createAdNative(activity);

        int width = 1080;
        int height = 1920;

        int orientation = TTAdConstant.VERTICAL;
        if (Ut.isScreenOriatationLandscape(activity)) {
            orientation = TTAdConstant.HORIZONTAL;
        }

        AdSlot.Builder builder = new AdSlot.Builder()
                .setCodeId(posId)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(width, height)
                .setOrientation(orientation);//必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL

        //这个属性，但不为0时，就是模板渲染视频广告，否则为普通视频广告
        boolean isExpress = showParam.optBoolean(Config.IS_EXPRESS, false);
        if (isExpress) {
            builder.setExpressViewAcceptedSize(500, 500);
        }

        AdSlot adSlot = builder.build();

        mTTAdNative.loadFullScreenVideoAd(adSlot, new TTAdNative.FullScreenVideoAdListener() {
            @Override
            public void onError(int i, String s) {
                String fullErr = "full video error. fail code: " + i + "msg: " + s;
                Ut.logI(fullErr);
                isFullVideoReady = false;
                if (null != adListener) {
                    adListener.onAdFailed(new ECAdError(i, fullErr));
                }
            }

            @Override
            public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ttFullScreenVideoAd) {

                if (null == ttFullScreenVideoAd) {
                    Ut.logI("full video ad obj is null");
                    isFullVideoReady = false;
                    if (null != adListener) {
                        adListener.onAdFailed(new ECAdError(ECAdError.AD_LOAD_FAIL, "ttFullScreenVideoAd is null"));
                    }
                    return;
                }

                mTTFullScreenVideoAd = ttFullScreenVideoAd;

                ttFullScreenVideoAd.setFullScreenVideoAdInteractionListener(new TTFullScreenVideoAd.FullScreenVideoAdInteractionListener() {
                    @Override
                    public void onAdShow() {
                        if (null != adListener) {
                            adListener.onAdShow();

                        }


                        Ut.initVisual(activity, posId, showParam, ECAdType.FULLVIDEO.getAdType(), null);
                        Ut.startVisual(posId);


                    }

                    @Override
                    public void onAdVideoBarClick() {
                        if (null != adListener) {
                            adListener.onAdClick();
                        }
                    }

                    @Override
                    public void onAdClose() {
                        isFullVideoReady = false;

                        Ut.stopVisual(posId);


                        if (null != adListener) {
                            adListener.onAdDismissed();
                        }
                    }

                    @Override
                    public void onVideoComplete() {
                        Ut.logI("full video ad onVideoComplete");
//                        if (null != adListener) {
//                            adListener.onAdReward();
//                        }
                    }

                    @Override
                    public void onSkippedVideo() {
                        Ut.logI("full video ad onSkippedVideo");
                    }
                });


                if (isPreload) {
                    isFullVideoReady = true;
                    if (null != adListener) {
                        adListener.onAdReady();
                    }
                } else {
                    mTTFullScreenVideoAd.showFullScreenVideoAd(activity);
                }


            }

            @Override
            public void onFullScreenVideoCached() {
                Ut.logI("full video ad onFullScreenVideoCached");

            }
        });


    }


    @Override
    public void load(Activity activity, ViewGroup containner, String posId, JSONObject showParam, IECAdListener adListener) {

        isPreload = showParam.optBoolean(Config.IS_PRELOAD, false);

        if (isPreload) {
            loadAd(activity, containner, posId, showParam, adListener);
        } else {
            isFullVideoReady = true;
            if (null != adListener) {
                adListener.onAdReady();
            }
        }

    }

    @Override
    public boolean isReady() {
        return isFullVideoReady;
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
