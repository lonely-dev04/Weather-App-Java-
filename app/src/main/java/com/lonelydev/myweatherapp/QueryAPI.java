package com.lonelydev.myweatherapp;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface QueryAPI {
    @GET("current.json")
    Call<DataModel> getData(
            @Query("key") String key,
            @Query("q") String q,
            @Query("aqi") String aqi
    );
}

interface PexelsService {

    @Headers({
            "Authorization: i5cSsVe1VC1ziDaZlGiJ7Rk1Kd9UdbAHyjqKjpjqlViOWFtczzgRjuvC"
    })
    @GET("v1/search")
    Call<PexelsResponse> getRandomPhotos(
            @Query("query") String query,
            @Query("per_page") int perPage
    );
}
