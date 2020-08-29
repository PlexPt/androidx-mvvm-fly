package com.github.plexpt.base.http;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import com.blankj.utilcode.util.Utils;
import com.github.plexpt.base.BaseApp;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.socks.library.KLog;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import me.goldze.mvvmhabit.http.cookie.CookieJarImpl;
import me.goldze.mvvmhabit.http.cookie.store.PersistentCookieStore;
import me.goldze.mvvmhabit.http.interceptor.BaseInterceptor;
import me.goldze.mvvmhabit.http.interceptor.CacheInterceptor;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
/**
 * Created by goldze on 2017/5/10.
 * RetrofitClient封装单例类, 实现网络请求
 */
public class RetrofitClient {

    //超时时间
    private static final int DEFAULT_TIMEOUT = 30;

    //缓存时间
    private static final int CACHE_TIMEOUT = 10 * 1024 * 1024;

    //服务端根路径  https://api.xiantian-tech.com    http://192.168.50.143:8080/
    private String BASE_URL = "http://192.168.50.143:8080/";

    private Context mContext = Utils.getApp();

    private OkHttpClient okHttpClient;

    private Retrofit retrofit;

    private Cache cache = null;

    private File httpCacheDirectory;

    private BaseInterceptor baseInterceptor;

    private static class SingletonHolder {

        @SuppressLint("StaticFieldLeak")
        private static RetrofitClient INSTANCE = new RetrofitClient();
    }

    public static RetrofitClient getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private RetrofitClient() {
        initApi(BASE_URL);
    }


    private void initApi(String url) {
        if (TextUtils.isEmpty(url)) {
            url = BASE_URL;
        }
        Map<String, String> params = new HashMap<>();
        params.put("token", BaseApp.getToken());

        if (httpCacheDirectory == null) {
            httpCacheDirectory = new File(mContext.getCacheDir(), "goldze_cache");
        }

        try {
            if (cache == null) {
                cache = new Cache(httpCacheDirectory, CACHE_TIMEOUT);
            }
        } catch (Exception e) {
            KLog.e("Could not create http cache", e);
        }
        baseInterceptor = new BaseInterceptor(params);
        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory();
        okHttpClient = new OkHttpClient.Builder()
                .cookieJar(new CookieJarImpl(new PersistentCookieStore(mContext)))
//                .cache(cache)
                .addInterceptor(baseInterceptor)
                .addInterceptor(new CacheInterceptor(mContext))
//                .addInterceptor(new CaptureInfoInterceptor())
                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                .addInterceptor(new LoggerInterceptor("UserApp", true))
//                .addInterceptor(new LoggingInterceptor
//                        .Builder()//构建者模式
//                        .loggable(BuildConfig.DEBUG) //是否开启日志打印
//                        .setLevel(Level.BASIC) //打印的等级
//                        .log(Platform.INFO) // 打印类型
//                        .request("Request") // request的Tag
//                        .response("Response")// Response的Tag
//                        .addHeader("log-header", "I am the log request header.") // 添加打印头, 注意 key 和 value 都不能是中文
//                        .build()
//                )
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(10, 30, TimeUnit.SECONDS))
                // 这里你可以根据自己的机型设置同时连接的个数和时间，我这里8个，和每个保持时间为10s
                .build();

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .serializeNulls()
                .enableComplexMapKeySerialization()
                .create();

        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
//                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
//                .addConverterFactory(FastJsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(url)
                .build();

    }

    public void initBaseUrl(String baseUrl) {
        this.BASE_URL = baseUrl;
        initApi(baseUrl);
    }

    public void saveToken() {
        Map<String, String> params = new HashMap<>();
        params.put("token", BaseApp.getToken());
        baseInterceptor.setHeaders(params);
    }

    /**
     * create you ApiService
     * Create an implementation of the API endpoints defined by the {@code service} interface.
     */
    public <T> T create(final Class<T> service) {
        if (service == null) {
            throw new RuntimeException("Api service is null!");
        }
        return retrofit.create(service);
    }

    /**
     * /**
     * execute your customer API
     * For example:
     * MyApiService service =
     * RetrofitClient.getInstance(MainActivity.this).create(MyApiService.class);
     * <p>
     * RetrofitClient.getInstance(MainActivity.this)
     * .execute(service.lgon("name", "password"), subscriber)
     * * @param subscriber
     */

    public static <T> T execute(Observable<T> observable, Observer<T> subscriber) {
        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);

        return null;
    }
}
