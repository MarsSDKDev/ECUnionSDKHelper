package com.ec.union.ecadhelper;

import android.Manifest;


public class Config {
    public static final String[] PLATFORM_PERMISSION = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static final String PLATFORM_NAME = "ecadhelper";

    public static final String PLATFORM_VER = "3.2.0.1";

    public static final String CLS_NM = "com.bytedance.sdk.openadsdk.TTAdSdk";

    public static final String APP_ID = "appId";
    public static final String IS_DEBUG = "isDebug";

    public static final String Y_OFFSET_PERCENT = "YOffsetPercent";
    public static final String WIDTH_SIZE_PERCENT = "widthSizePercent";
    public static final String ASPECT_RATIO = "aspectRatio";

    public static final String IS_EXPRESS = "isExpress";
    public static final String IS_PRELOAD = "isPreload";

    public static final String SPLASH_TIMEOUT = "splashTimeout";

    public static final String AD_MAIN_CLS_NOT_EXIST = "找不到jar主类...";

}
