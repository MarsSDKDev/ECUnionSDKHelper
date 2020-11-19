package com.ec.union.adhelper;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.ec.union.ad.sdk.Ut;
import com.ec.union.ad.sdk.api.ECAdType;
import com.ec.union.ad.sdk.platform.ECAdError;
import com.ec.union.ad.sdk.platform.IECAdListener;
import com.ec.union.ad.sdk.platform.banner.BaseBanner;

import org.json.JSONObject;

import java.util.List;


public class Banner extends BaseBanner {

    private ViewGroup mContainner;
    private boolean mVisibility = true;
    private FrameLayout mFrameLayout;
    private TTNativeExpressAd mTTAd;


    @Override
    public void show(final Activity activity, ViewGroup containner, final String posId, final JSONObject showParam, final IECAdListener adListener) {

        if (null != mFrameLayout) {
            mFrameLayout.removeAllViews();
        }
        if (mContainner != null) {
            mContainner.removeAllViews();
        }
        onDestroy(activity);

        mContainner = containner;

        ViewGroup.LayoutParams layoutParams = containner.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        containner.setLayoutParams(layoutParams);

        FrameLayout frameLayout = new FrameLayout(activity);

        RelativeLayout.LayoutParams reLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        reLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        String bannerPosStr = showParam.optString(BANNER_POS_KEY);
        if (BANNER_POS_TOP.equals(bannerPosStr)) {
            reLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        } else {
            reLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
        mFrameLayout = frameLayout;
        containner.addView(frameLayout, reLayoutParams);


        TTAdNative mTTAdNative = TTAdSdk.getAdManager().createAdNative(activity);

        Resources resources = activity.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();

        float expressViewWidth = 360;
        float expressViewHeight = 50;
        if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            expressViewWidth = Ut.px2dip(activity, dm.widthPixels);
            expressViewHeight = expressViewWidth / 7.5f; //375:50
        } else {
            expressViewWidth = Ut.px2dip(activity, dm.widthPixels) / 2.f;
            expressViewHeight = expressViewWidth / 7.5f; //375:50
        }

        try {
            String eWstr = showParam.optString("expressViewWidth");
            if (!Ut.isStringEmpty(eWstr)) {
                expressViewWidth = Float.parseFloat(eWstr);
            }
        } catch (Exception e) {
            e.printStackTrace();
//            expressViewHeight = 0; //高度设置为0,则高度会自适应
        }
        try {
            String eHstr = showParam.optString("expressViewHeight");
            if (!Ut.isStringEmpty(eHstr)) {
                expressViewHeight = Float.parseFloat(eHstr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        String slideIntervalTimeStr = showParam.optString("slideIntervalTime");
        int slideIntervalTime = 30;
        if (!Ut.isStringEmpty(slideIntervalTimeStr)) {
            try {
                slideIntervalTime = Integer.parseInt(slideIntervalTimeStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(posId) //广告位id
                .setSupportDeepLink(true)
                .setAdCount(1) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(expressViewWidth, expressViewHeight) //期望模板广告view的size,单位dp
                .setImageAcceptedSize(640, 320)//这个参数设置即可，不影响模板广告的size
                .build();

//        final int finalExpressViewWidth = (int) expressViewWidth;
//        final int finalExpressViewHeight = (int) expressViewHeight;
        final int finalSlideIntervalTime = slideIntervalTime;
        mTTAdNative.loadBannerExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int i, String s) {
                if (null != adListener) {
                    adListener.onAdFailed(new ECAdError(i, s));
                }
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> list) {
                if (list == null || list.size() == 0) {
                    if (null != adListener) {
                        adListener.onAdFailed(new ECAdError("list is empty"));
                    }
                    return;
                }

                mTTAd = list.get(0);
                mTTAd.setSlideIntervalTime(finalSlideIntervalTime * 1000);
                mTTAd.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
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


                        Ut.initVisual(activity, posId, showParam, ECAdType.BANNER.getAdType(), null);
                        Ut.startVisual(posId);


                    }

                    @Override
                    public void onRenderFail(View view, String msg, int code) {
                        if (null != adListener) {
                            adListener.onAdFailed(new ECAdError(code, msg));
                        }
                    }

                    @Override
                    public void onRenderSuccess(View view, float width, float height) {//返回view的宽高 单位 dp

                        //在渲染成功回调时展示广告，提升体验
                        if (null != adListener) {
                            adListener.onAdReady();
                        }

                        if (null != mFrameLayout) {
                            mFrameLayout.addView(view, Ut.dip2px(activity, (int) width), Ut.dip2px(activity, (int) height));
                        }

                        setVisibility(mVisibility);
                    }
                });
                //使用默认模板中默认dislike弹出样式
                mTTAd.setDislikeCallback(activity, new TTAdDislike.DislikeInteractionCallback() {
                    @Override
                    public void onSelected(int position, String value) {
                        //用户选择不喜欢原因后，移除广告展示
                        Ut.logI("position: " + position + " ,value: " + value);

                        Ut.stopVisual(posId);

                        if (mContainner != null) {
                            mContainner.removeAllViews();
                        }
                        if (null != adListener) {
                            adListener.onAdDismissed();
                        }

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onRefuse() {

                    }
                });
                mTTAd.render();

            }
        });

    }

    @Override
    public void setVisibility(boolean visibility) {

        mVisibility = visibility;

        if (null != mContainner) {
            if (visibility) {
                Ut.logI("tt banner setVisibility VISIBLE" );
                mContainner.setVisibility(View.VISIBLE);
            } else {
                Ut.logI("tt banner setVisibility GONE" );
                mContainner.setVisibility(View.GONE);
            }
        }
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
            //调用destroy()方法释放
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
