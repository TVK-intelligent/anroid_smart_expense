package com.example.smartexpense.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // Backend Spring Boot đang chạy trên máy tính cùng mạng LAN với điện thoại.
    // Đổi IP này nếu máy tính của bạn đổi mạng.
    private static final String BASE_URL = "http://172.16.1.80:8080/";
    private static Retrofit retrofit = null;

    public static synchronized ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
