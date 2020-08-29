package com.github.plexpt.base;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import com.blankj.utilcode.util.CrashUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.Utils;
import com.socks.library.KLog;
import com.tencent.mmkv.MMKV;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import me.goldze.mvvmhabit.base.BaseApplication;


public abstract class BaseApp extends BaseApplication {

    private static final String TAG = BaseApp.class.getName();

    private static BaseApp mContext;

    private static String NIGHT = "night";

    private Handler handler;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();

        if (inMainProcess(this)) {
            Utils.init(this);
            MMKV.initialize(this);
//            Stetho.initializeWithDefaults(this);
            KLog.init(isDebug());
            mContext = this;
//            AppCompatDelegate.setDefaultNightMode(isNightMode() ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            CrashUtils.init();
            System.setProperty("rx2.purge-period-seconds", "30");
            RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
                @Override
                public void accept(@NonNull Throwable throwable) throws Exception {
                    if (isDebug()) {
                        throwable.printStackTrace();
                    }
                }
            });
        }
    }


    @Override
    public void onTerminate() {
        mContext = null;
        super.onTerminate();
    }

    private boolean isLowMemoryDevice() {
        if (Build.VERSION.SDK_INT >= 19) {
            return ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).isLowRamDevice();
        } else {
            return false;
        }
    }

    public static Context getContext() {
        return mContext;
    }

    public abstract boolean isDebug();

    public abstract String getUmengKey();

    public static boolean isDebugMode() {
        return ((BaseApp) getContext()).isDebug();
    }

    public static boolean inMainProcess(Context context) {
        String packageName = context.getPackageName();
        String processName = getProcessName(context);
        return packageName.equals(processName);
    }

    /**
     * @return 是否登录
     */
    public static boolean isLogin() {
        return !TextUtils.isEmpty(getToken());
    }

    public static String getToken() {
        String token = getMMKV(Constants.MMKV_APP_ID).getString(Constants.TOKEN_KEY, "");
        KLog.d("TOKEN", token);
        return TextUtils.isEmpty(token) ? "" : token;
    }

    public static void clearToken(String serviceId) {
        getMMKV(serviceId).putString(Constants.TOKEN_KEY, "");
        SPUtils.getInstance().put(Constants.TOKEN_KEY, "");
    }

    public static MMKV getMMKV(String serviceId) {
        return MMKV.mmkvWithID(serviceId, MMKV.MULTI_PROCESS_MODE);
    }

    /**
     * 获取当前进程名
     *
     * @return 进程名
     */
    public static final String getProcessName(Context context) {
        String processName = null;

        // ActivityManager
        ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));

        while (true) {
            for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
                if (info.pid == android.os.Process.myPid()) {
                    processName = info.processName;
                    break;
                }
            }

            // go home
            if (!TextUtils.isEmpty(processName)) {
                return processName;
            }

            // take a rest and again
            try {
                Thread.sleep(100L);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    protected void openStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectCustomSlowCalls()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                //.penaltyDeath()
                .build());
    }

    public abstract void tokenTimeOut();


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.fontScale != 1)//非默认值
        {
            getResources();
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        if (res.getConfiguration().fontScale != 1) {//非默认值
            Configuration newConfig = new Configuration();
            newConfig.setToDefaults();//设置默认
            res.updateConfiguration(newConfig, res.getDisplayMetrics());
        }
        return res;
    }
}
