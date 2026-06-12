package com.example.smartexpense.api;

import android.app.Application;
import android.content.pm.ApplicationInfo;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;

    public static synchronized ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(resolveBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

    private static String resolveBaseUrl() {
        if (isDebugBuild()) {
            return "http://10.0.2.2:8080/";
        }
        return "http://172.16.1.80:8080/";
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
