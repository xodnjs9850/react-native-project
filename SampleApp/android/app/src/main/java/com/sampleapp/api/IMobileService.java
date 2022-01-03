package com.sampleapp.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IMobileService {
    @Headers("Content-Type: application/json")
    @POST("/api/v1/mobile")
    Call<JsonObject> mobileService(@Header("Authorization") String authorization, @Body JsonObject body);
}
