package com.salman.weatherforecaster.retrofit;

import com.salman.weatherforecaster.model.WeatherResult;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface WeatherService {
/*    @GET("weather")
    Observable<WeatherResult> getWeatherByLatLon(@Query("lat") String lat,
                                                 @Query("lon") String lon,
                                                 @Query("appid") String appid,
                                                 @Query("units") String unit);*/

    @GET
    Call<WeatherResult> getCurrentWeatherResponse(@Url String url);
}
