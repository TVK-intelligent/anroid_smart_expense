package com.example.smartexpense.api;

import android.app.Application;
import android.content.pm.ApplicationInfo;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;

    public static synchronized ApiService getApiService() {
        if (retrofit == null) {
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        okhttp3.Request request = chain.request();
                        String lang = java.util.Locale.getDefault().getLanguage();
                        okhttp3.Request newRequest = request.newBuilder()
                                .header("Accept-Language", lang)
                                .build();
                        return chain.proceed(newRequest);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(resolveBaseUrl())
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

    private static String resolveBaseUrl() {
        return "http://192.168.1.200:8080/";
    }

    private static boolean isDebugBuild() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Object app = activityThreadClass.getMethod("currentApplication").invoke(null);
            if (app instanceof Application) {
                ApplicationInfo info = ((Application) app).getApplicationInfo();
                return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            }
        } catch (Throwable ignored) {
            // Fall back to the LAN URL if the app instance is not available yet.
        }
        return false;
    }
}
