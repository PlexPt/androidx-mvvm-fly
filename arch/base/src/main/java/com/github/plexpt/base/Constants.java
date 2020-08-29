package com.github.plexpt.base;

import okhttp3.MediaType;

/**
 * 常量
 */
public interface Constants {

    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    MediaType TEXT = MediaType.parse("text/plain; charset=utf-8");
    MediaType PART_FORMDATA = MediaType.parse("multipart/form-data; charset=utf-8");
    int DEVICE_ANDROID = 1;
    String SERVER_PHONE = "11111";

    String TOKEN_KEY = "user_token";//token
    String LAT = "latitude";
    String LON = "longitude";
    String CURRENT_LOCATION_NAME = "current_location_name";
    String USER_JSON = "user_json";
    String MMKV_SERVICE_PASSENGER_ID = "passenger_cross_process_id";
    String MMKV_SERVICE_DRIVER_ID = "driver_cross_process_id";
    String MMKV_APP_ID = "mmkv_default";

    String ACTION_RESET_LOCATION = "action_reset_location";

}
