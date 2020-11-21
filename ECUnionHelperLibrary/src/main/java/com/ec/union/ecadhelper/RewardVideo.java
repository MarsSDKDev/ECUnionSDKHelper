package com.ec.union.ecadhelper;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ViewGroup;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.ec.union.ad.sdk.Ut;
import com.ec.union.ad.sdk.api.ECAdType;
import com.ec.union.ad.sdk.common.game.GameParam;
import com.ec.union.ad.sdk.platform.ECAdError;
import com.ec.union.ad.sdk.platform.IECAd;
import com.ec.union.ad.sdk.platform.IECAdListener;
import com.ec.union.ad.sdk.platform.IECAdSync;

import org.json.JSONObject;

import java.util.Map;


public class RewardVideo implements IECAd, IECAdSync {

    private TTRewardVideoAd mTTRewardVideoAd;
    private boolean isRewardVideoReady;

    private boolean isLoadAndShow;
    private Map<String, String> mGameParams;

    private boolean isPreload;
    private boolean isReward;


    @Override
    public void show(Activity activity, ViewGroup containner, String posId, JSONObject showParam, IECAdListener adListener) {
        UIUtils.debugToast(activity,"调用 '展示' " + RewardVideo.class.getSimpleName() + "广告.");
        isPreload = showParam.optBoolean(Config.IS_PRELOAD, false);

        if (isPreload) {

            if (isLoadAndShow) {
                isLoadAndShow = false;
            }
            if (null != mTTRewardVideoAd) {
                if (isRewardVideoReady) {
                    mTTRewardVideoAd.showRewardVideoAd(activity);
                } else {
                    if (null != adListener) {
                        adListener.onAdFailed(new ECAdError("The reward video is not ready."));
                    }
                }

            } else {
                if (null != adListener) {
                    adListener.onAdFailed(new ECAdError(ECAdError.AD_LOAD_FAIL, "The reward video has not been loaded."));
                }
            }

        } else {
            loadAd(activity, containner, posId, showParam, adListener);
        }

    }


    public void loadAd(final Activity activity, final ViewGroup containner, final String posId, final JSONObject showParam, final IECAdListener adListener) {

        UIUtils.debugToast(activity,"调用 '加载' " + RewardVideo.class.getSimpleName() + "广告.");
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
        Ut.logD("isLoadAndShow:" + isLoadAndShow + " - gameParams:" + mGameParams);

        //这个属性，但不为0时，就是模板渲染视频广告，否则为普通视频广告
        boolean isExpress = showParam.optBoolean(Config.IS_EXPRESS, false);
        if (isExpress) {
            builder.setExpressViewAcceptedSize(500, 500);
        }


        if (isLoadAndShow && null != mGameParams) {
            builder.setRewardName(mGameParams.get(GameParam.REWARD_NAME.getValue()));
            try {
                builder.setRewardAmount(Integer.valueOf(mGameParams.get(GameParam.REWARD_AMOUNT.getValue())));
            } catch (Exception e) {
                e.printStackTrace();
            }
            builder.setUserID(mGameParams.get(GameParam.USER_ID.getValue()));
            builder.setMediaExtra(mGameParams.get(GameParam.EXTRA.getValue()));
        } else {
            builder.setUserID("");//用户id,必传参数
        }


        AdSlot adSlot = builder.build();

        mTTAdNative.loadRewardVideoAd(adSlot, new TTAdNative.RewardVideoAdListener() {
            @Override
            public void onError(int i, String s) {
                String rewardErr = "reward video error. fail code: " + i + "msg: " + s;
                Ut.logI(rewardErr);
                isRewardVideoReady = false;
                if (null != adListener) {
                    adListener.onAdFailed(new ECAdError(i, rewardErr));
                }
            }

            @Override
            public void onRewardVideoAdLoad(TTRewardVideoAd ttRewardVideoAd) {

                if (null == ttRewardVideoAd) {
                    Ut.logI("reward video ad obj is null");
                    isRewardVideoReady = false;
                    if (null != adListener) {
                        adListener.onAdFailed(new ECAdError(ECAdError.AD_LOAD_FAIL, "ttRewardVideoAd is null"));
                    }
                    return;
                }
                mTTRewardVideoAd = ttRewardVideoAd;

                ttRewardVideoAd.setRewardAdInteractionListener(new TTRewardVideoAd.RewardAdInteractionListener() {

                    @Override
                    public void onAdShow() {
                        if (null != adListener) {
                            adListener.onAdShow();
                        }


                        Ut.initVisual(activity, posId, showParam, ECAdType.REWARDVIDEO.getAdType(), null);
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


                        Ut.stopVisual(posId);

                        isRewardVideoReady = false;

                        if (null != adListener) {
                            adListener.onAdDismissed();
                        }
                        if (isReward) {
                            isReward = false;
                            if (null != adListener) {
                                adListener.onAdReward();
                                UIUtils.debugToast(activity,RewardVideo.class.getSimpleName() + "广告 回调奖励接口");
                            }
                        }
                    }

                    @Override
                    public void onVideoComplete() {
                        Ut.logI("reward video ad onVideoComplete");
                        isReward = true;
//                        if (null != adListener) {
//                            adListener.onAdReward();
//                        }
                    }

                    @Override
                    public void onVideoError() {
                        isRewardVideoReady = false;
                        if (null != adListener) {
                            adListener.onAdFailed(new ECAdError(ECAdError.AD_LOAD_FAIL, "onVideoError"));
                        }
                    }

                    @Override
                    public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName) {
                        isReward = true;
                        Ut.logI("onRewardVerify: " + rewardVerify + "rewardAmount: " + rewardAmount + "rewardName: " + rewardName);

//                        if (rewardVerify) {
//                            if (null != adListener) {
//                                adListener.onAdReward();
//                            }
//                        }
                    }

                    @Override
                    public void onSkippedVideo() {

                    }
                });

                if (isPreload) {

                    isRewardVideoReady = true;
                    if (null != adListener) {
                        adListener.onAdReady();
                    }

                } else {
                    mTTRewardVideoAd.showRewardVideoAd(activity);
                }

                if (isLoadAndShow) {
                    show(activity, containner, posId, showParam, adListener);
                }

            }

            @Override
            public void onRewardVideoCached() {
                Ut.logI("reward video ad onRewardVideoCached");
            }
        });

    }


    @Override
    public void load(Activity activity, ViewGroup containner, String posId, JSONObject showParam, IECAdListener adListener) {

        isPreload = showParam.optBoolean(Config.IS_PRELOAD, false);

        if (isPreload) {

            loadAd(activity, containner, posId, showParam, adListener);

        } else {
            isRewardVideoReady = true;
            if (null != adListener) {
                adListener.onAdReady();
            }

        }


    }

    @Override
    public void loadAndShow(Activity activity, ViewGroup containner, String posId, JSONObject showParam, IECAdListener adListener, Map<String, String> gameParams) {

        isPreload = showParam.optBoolean(Config.IS_PRELOAD, false);

        if (isPreload) {
            Ut.logD("loadAndShow....");
            isLoadAndShow = true;
            mGameParams = gameParams;
            load(activity, containner, posId, showParam, adListener);
        } else {
            Ut.logD("loadAndShow 不是预加载，展示失败");
        }
    }

    @Override
    public boolean isReady() {
        return isRewardVideoReady;
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
