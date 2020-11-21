package com.ec.union.ecadhelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.ec.union.ad.sdk.Ut;
import com.ec.union.ad.sdk.api.ECAdType;
import com.ec.union.ad.sdk.platform.ECAdError;
import com.ec.union.ad.sdk.platform.IECAd;
import com.ec.union.ad.sdk.platform.IECAdListener;
import com.youth.mrs.banner.Banner;
import com.youth.mrs.banner.loader.ViewGruopImageLoader;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;


public class Feed implements IECAd {

    private ViewGroup mContainner;
    private boolean mVisibility = true;

    private List<TTNativeExpressAd> mFeedAds;
    private List<View> mFeedViews;
    //    private Activity mActivity;
    private IECAdListener mAdListener;
    private float mSizePercent = 100;
    private float mYOffsetPercent = 0;
    private float mAspectRatio = 1.78f;
    private int mMargin = 18;
    private int mCornerRadius = 18;

    private RelativeLayout mHostLayout;
    private RelativeLayout.LayoutParams mHostlayoutParams;
    private com.youth.mrs.banner.Banner mBanner;
    private GifImageView mGifImageView;
    private GifDrawable mGifDrawable;

    private boolean isRender;

    private String mPosId;
    private JSONObject mShowParam;
    private Activity mActivity;

    @Override
    public void show(final Activity activity, ViewGroup containner, String posId, JSONObject showParam, IECAdListener adListener) {
        UIUtils.debugToast(activity,"调用 '展示' " + Feed.class.getSimpleName() + "广告.");
        mActivity =activity;
        mPosId = posId;
        mShowParam = showParam;
        mAdListener = adListener;
        mContainner = containner;

        String yOffsetPercentStr = showParam.optString(Config.Y_OFFSET_PERCENT);
        if (!Ut.isStringEmpty(yOffsetPercentStr)) {
            try {
                mYOffsetPercent = Float.parseFloat(yOffsetPercentStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String sizePercentStr = showParam.optString(Config.WIDTH_SIZE_PERCENT);
        if (!Ut.isStringEmpty(sizePercentStr)) {
            try {
                mSizePercent = Float.parseFloat(sizePercentStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String aspectRatioStr = showParam.optString(Config.ASPECT_RATIO);
        if (!Ut.isStringEmpty(aspectRatioStr)) {
            try {
                mAspectRatio = Float.parseFloat(aspectRatioStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Resources resources = activity.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();

        float sizePercent = mSizePercent / 100;
        float width = (dm.widthPixels * sizePercent);
        float height = (width / mAspectRatio);
        width = width - mMargin * 2;
        height = height - mMargin * 2;


        //设置广告参数
        final AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(posId) //广告位id
                .setSupportDeepLink(true)
                .setAdCount(2) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(Ut.px2dip(activity, (int) width), Ut.px2dip(activity, (int) height)) //期望个性化模板广告view的size,单位dp
                .setImageAcceptedSize(640, 320) //这个参数设置即可，不影响个性化模板广告的size
                .build();
        //加载广告
        TTAdNative mTTAdNative = TTAdSdk.getAdManager().createAdNative(activity.getApplicationContext());

        mTTAdNative.loadNativeExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {
                mAdListener.onAdFailed(new ECAdError(code, message));
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> adList) {

                if (adList == null || adList.isEmpty()) {
                    mAdListener.onAdFailed(new ECAdError("onNativeExpressAdLoad: ad is empty."));
                    return;
                }

                Ut.logI("adList.size=" + adList.size());

                ArrayList list = new ArrayList();
                ArrayList videoList = new ArrayList();
                for (TTNativeExpressAd ad : adList) {
                    Ut.logI("ad.getImageMode=" + ad.getImageMode());

                    if (ad.getImageMode() == 5) {
                        if (videoList.size() == 0) {
                            videoList.add(ad);
                        }
                    } else {
                        list.add(ad);
                    }
                }

                if (videoList.size() > 0) {
                    setup(activity, videoList);
                } else if (list.size() > 0) {
                    setup(activity, list);
                } else {
                    mAdListener.onAdFailed(new ECAdError("没可用的广告资源类型"));
                }

            }
        });

    }

    private void setup(Activity activity, List list) {
        isRender = false;
        closeView();
        mFeedViews = new ArrayList<>();
        mFeedAds = list;
        setupAdData(activity, mFeedAds);
        setVisibility(mVisibility);
        mAdListener.onAdReady();
    }


    private void closeView() {

        if (null != mGifImageView) {
            mGifImageView.setVisibility(View.GONE);
            mGifImageView = null;
        }
        if (null != mGifDrawable) {
            mGifDrawable.stop();
            mGifDrawable.recycle();
            mGifDrawable = null;
        }

        if (null != mFeedViews) {
            mFeedViews.clear();
        }

        if (null != mBanner) {
            mBanner.update(new ArrayList());
            mBanner.releaseBanner();
            mBanner = null;
        }

        if (null != mContainner) {
            mContainner.removeAllViews();
        }

        if (null != mFeedAds) {
            for (int i = 0; i < mFeedAds.size(); i++) {
                TTNativeExpressAd feed = mFeedAds.get(i);
                feed.destroy();
            }
            mFeedAds = null;
        }

    }

    private void setupAdData(final Activity activity, List<TTNativeExpressAd> ads) {

        Resources resources = activity.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();

        float sizePercent = mSizePercent / 100;
        float yOffsetPercent = mYOffsetPercent / 100;

        int width = (int) (dm.widthPixels * sizePercent);
        int height = (int) (width / mAspectRatio);
        Ut.logI("feed view width:" + width + "height:" + height);
        int bottomMargin = (int) ((dm.heightPixels - height) * yOffsetPercent);//底部边距
        Ut.logI("feed bottomMargin:" + bottomMargin);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM); //底部对齐
        layoutParams.bottomMargin = bottomMargin;
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL); //居中
        mHostlayoutParams = layoutParams;

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
//        drawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        drawable.setCornerRadius(30);
        drawable.setColor(Color.WHITE);

        //feed view
        final RelativeLayout hostLayout = new RelativeLayout(activity);
        hostLayout.setBackgroundColor(Color.WHITE);
        hostLayout.setBackground(drawable);

        mHostLayout = hostLayout;
        mContainner.addView(hostLayout, layoutParams);

        try {
            mGifImageView = new GifImageView(activity);
            //生成个位的随机数
            int math = (int) ((Math.random() * 1000)) % 4;
            Ut.logI("banner math=" + math);
            mGifDrawable = new GifDrawable(activity.getAssets().open("bbmrs_banner_bg_res/bg" + math + ".gif"));
            mGifDrawable.setCornerRadius(mCornerRadius);
            mGifImageView.setBackground(mGifDrawable);
            hostLayout.addView(mGifImageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        } catch (Exception e) {
            e.printStackTrace();
            Ut.logI("GifDrawable Exception=" + e.getMessage());
        }

        RelativeLayout mainContentLayout = new RelativeLayout(activity);
        width = width - mMargin * 2;
        height = height - mMargin * 2;
        RelativeLayout.LayoutParams mainContentViewLayoutParams = new RelativeLayout.LayoutParams(width, height);
        mainContentViewLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        hostLayout.addView(mainContentLayout, mainContentViewLayoutParams);

        mBanner = new Banner(activity);
        mBanner.setOffscreenPageLimit(2);
        mBanner.setDelayTime(3500);
        RelativeLayout.LayoutParams bannerLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        mainContentLayout.addView(mBanner, bannerLayoutParams);

        for (int i = 0; i < ads.size(); i++) {
            TTNativeExpressAd feedAd = ads.get(i);
            if (feedAd.getImageMode() == 5) {
                feedAd.setVideoAdListener(new TTNativeExpressAd.ExpressVideoAdListener() {

                    @Override
                    public void onVideoLoad() {
                        Ut.logI("视频加载成功");
                    }

                    @Override
                    public void onVideoError(int errorCode, int extraCode) {
                        Ut.logI("视频播放错误：errorCode=" + errorCode + ",extraCode=" + extraCode);
                    }

                    @Override
                    public void onVideoAdStartPlay() {
                        Ut.logI("视频开始播放");
                    }

                    @Override
                    public void onVideoAdPaused() {
                        Ut.logI("视频暂停播放");
                    }

                    @Override
                    public void onVideoAdContinuePlay() {
                        Ut.logI("视频继续播放");
                    }

                    @Override
                    public void onProgressUpdate(long l, long l1) {

                    }

                    @Override
                    public void onVideoAdComplete() {
                        Ut.logI("视频播放完成");
                    }

                    @Override
                    public void onClickRetry() {
                        Ut.logI("onClickRetry");
                    }
                });

            }

            feedAd.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
                @Override
                public void onAdClicked(View view, int type) {
                    Ut.logI("onAdClicked");
                    if (null != mAdListener) {
                        mAdListener.onAdClick();
                    }
                }

                @Override
                public void onAdShow(View view, int type) {
                    Ut.logI("onAdShow");
                    if (null != mAdListener) {
                        mAdListener.onAdShow();
                    }

                    Ut.initVisual(activity, mPosId, mShowParam, ECAdType.FEED.getAdType(), null);
                    Ut.startVisual(mPosId);


                }

                @Override
                public void onRenderFail(View view, String msg, int code) {
                    Ut.logI("render fail:" + msg + ", code:" + code);
                    isRender = false;
                    if (null != mAdListener) {
                        mAdListener.onAdFailed(new ECAdError(code, msg));
                    }

                }

                @Override
                public void onRenderSuccess(View view, float width, float height) {
                    //返回view的宽高 单位 dp
                    isRender = true;
                    Ut.logI("tt feed view: " + view);
                    Ut.logI("渲染成功 width=" + width + ", height=" + height);
                    if (null != mFeedViews) {
                        mFeedViews.add(view);
                        if (null != mBanner) {
                            mBanner.update(mFeedViews);
                        }
                    }
                }
            });

            feedAd.setDislikeCallback(activity, new TTAdDislike.DislikeInteractionCallback() {
                @Override
                public void onSelected(int position, String value) {
                    Ut.logI("点击 position=" + position + ", value" + value);

                    Ut.stopVisual(mPosId);

                    closeView();
                    if (null != mAdListener) {
                        mAdListener.onAdDismissed();
                    }
                }

                @Override
                public void onCancel() {
                    Ut.logI("点击取消");
                }

                @Override
                public void onRefuse() {

                }
            });

            feedAd.render();//调用render开始渲染广告

        }

        mBanner.setImageLoader(new ViewGruopImageLoader() {
            @Override
            public void display(Context context, Object data, final ViewGroup contentView) {
//                TTNativeExpressAd feedAd = (TTNativeExpressAd) data;

                contentView.addView((View) data, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }

            @Override
            public ViewGroup createView(Context context, Object data) {
//                TTNativeExpressAd mTTAd = (TTNativeExpressAd) data;
                RelativeLayout re = new RelativeLayout(context);

//                re.setBackgroundColor(Color.BLACK);
                return re;
            }

            @Override
            public void destroyView(Object data, ViewGroup contentView) {
//                TTNativeExpressAd mTTAd = (TTNativeExpressAd) data;
//                if (null != mTTAd.getExpressAdView() && null != mTTAd.getExpressAdView().getParent()) {
//                    ((ViewGroup) mTTAd.getExpressAdView().getParent()).removeView(mTTAd.getExpressAdView());
//                }
//                mTTAd.destroy();

                contentView.removeAllViews();
            }
        });

    }


    @Override
    public void load(Activity activity, ViewGroup containner, String posId, JSONObject showParam, IECAdListener adListener) {

    }

    @Override
    public boolean isReady() {
        return null != mFeedAds && mFeedAds.size() > 0;
    }

    @Override
    public void setVisibility(boolean visibility) {
        Ut.logI("feed view visibility=" + visibility);
        UIUtils.debugToast(mActivity,"显示隐藏 " + Feed.class.getSimpleName() + "广告. 状态:"+visibility);
        mVisibility = visibility;
        if (null != mContainner) {
            if (visibility) {
                if (isReady()) {
                    Ut.logI("feed view visibility mContainner.getChildCount()=" + mContainner.getChildCount());
                    mContainner.setVisibility(View.VISIBLE);

//                    TTNativeExpressAd ad = mFeedAds.get(0);
//                    if (ad.getImageMode() == 5) {
                    if (null != mHostLayout && null != mHostlayoutParams) {
                        if (mHostLayout.getParent() == null) {
                            mContainner.addView(mHostLayout, mHostlayoutParams);
                        }
                    }
//                    }

                    if (null != mGifImageView) {
                        mGifImageView.setVisibility(View.VISIBLE);
                    }
                    if (null != mGifDrawable) {
                        mGifDrawable.start();
                    }
                    if (null != mBanner) {
                        mBanner.startAutoPlay();
                    }
                } else {
                    Ut.logI("feed view params fail or is not ready..");
                }
            } else {
                Ut.logI("feed view gone");

//                if (isReady()) {
//                    TTNativeExpressAd ad = mFeedAds.get(0);
//                    if (ad.getImageMode() == 5) {
                mContainner.removeAllViews();
//                    }
//                }

                if (null != mBanner) {
                    mBanner.stopAutoPlay();
                }
                if (null != mGifDrawable) {
                    mGifDrawable.stop();
                }
                if (null != mGifImageView) {
                    mGifImageView.setVisibility(View.GONE);
                }
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
        closeView();
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
