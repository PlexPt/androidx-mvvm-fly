package com.github.plexpt.base.http;

import android.text.TextUtils;
import com.socks.library.KLog;
import java.io.IOException;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * Created by zhy on 16/3/1
 */
public class LoggerInterceptor implements Interceptor {
    public static final String TAG = "okhttp";
    private boolean showResponse;

    public LoggerInterceptor(String tag, boolean showResponse) {
        if (TextUtils.isEmpty(tag)) {
            tag = TAG;
        }
        this.showResponse = showResponse;
    }

    public LoggerInterceptor(String tag) {
        this(tag, false);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        logForRequest(request);
        Response response = chain.proceed(request);
        return logForResponse(response);
    }

    private Response logForResponse(Response response) {
        try {
            //===>response log
            KLog.i( "========response'log=======");
            Response.Builder builder = response.newBuilder();
            Response clone = builder.build();
            KLog.i( "url : " + clone.request().url());
            KLog.i( "code : " + clone.code());
            KLog.i( "protocol : " + clone.protocol());
            if (!TextUtils.isEmpty(clone.message()))
                KLog.i( "message : " + clone.message());

            if (showResponse) {
                ResponseBody body = clone.body();
                if (body != null) {
                    MediaType mediaType = body.contentType();
                    if (mediaType != null) {
                        KLog.i( "responseBody's contentType : " + mediaType.toString());
                        if (isText(mediaType)) {
                            String resp = body.string();
                            KLog.i( "responseBody's content : " + resp);

                            body = ResponseBody.create(mediaType, resp);
                            return response.newBuilder().body(body).build();
                        } else {
                            KLog.i( "responseBody's content : " + " maybe [file part] , too large too print , ignored!");
                        }
                    }
                }
            }

            KLog.i( "========response'log=======end");
        } catch (Exception e) {
//            e.printStackTrace();
        }

        return response;
    }

    private void logForRequest(Request request) {
        try {
            String url = request.url().toString();
            Headers headers = request.headers();

            KLog.i( "========request'log=======");
            KLog.i( "method : " + request.method());
            KLog.i( "url : " + url);
            if (headers != null && headers.size() > 0) {
                KLog.i( "headers : " + headers.toString());
            }
            RequestBody requestBody = request.body();
            if (requestBody != null) {
                MediaType mediaType = requestBody.contentType();
                if (mediaType != null) {
//                    KLog.i(mediaType.type());
//                    KLog.i(mediaType.subtype());
                    KLog.i( "requestBody's contentType : " + mediaType.toString());
//                    if (mediaType.toString().equals("application/json")){
//                        KLog.i(bodyToString(request));
//                    }
                    if (isText(mediaType)) {
                        KLog.i( "requestBody's content : " + bodyToString(request));
                    } else {
                        KLog.i( "requestBody's content : " + " maybe [file part] , too large too print , ignored!");
                    }
                }
            }
            KLog.i( "========request'log=======end");
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    private boolean isText(MediaType mediaType) {
        if (mediaType.type() != null && mediaType.type().equals("text")) {
            return true;
        }
        if (mediaType.subtype() != null) {
            if (mediaType.subtype().equals("json") ||
                    mediaType.subtype().equals("xml") ||
                    mediaType.subtype().equals("html") ||
                    mediaType.subtype().equals("webviewhtml")
                    )
                return true;
        }
        return false;
    }

    private String bodyToString(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "something error when show requestBody.";
        }
    }
}
