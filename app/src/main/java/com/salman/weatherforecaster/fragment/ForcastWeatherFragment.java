package com.salman.weatherforecaster.fragment;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.salman.weatherforecaster.R;
import com.salman.weatherforecaster.adapter.WeatherForcastAdapter;
import com.salman.weatherforecaster.common.Common;
import com.salman.weatherforecaster.common.SimpleDividerItemDecoration;
import com.salman.weatherforecaster.model.WeatherForecastResult;
import com.salman.weatherforecaster.retrofit.RetrofitClient;
import com.salman.weatherforecaster.retrofit.WeatherService;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class ForcastWeatherFragment extends Fragment {
    private static final String TAG = "ForcastWeatherFragment";
    CompositeDisposable compositeDisposable;
    WeatherService weatherService;
    RecyclerView recyclerView;
    ProgressDialog progressDialog;

    static ForcastWeatherFragment instance;
    public static ForcastWeatherFragment getInstance() {
        if (instance == null) {
            instance = new ForcastWeatherFragment();
        }
        return instance;
    }

    public ForcastWeatherFragment() {
        compositeDisposable = new CompositeDisposable();
        weatherService = RetrofitClient.getRetrofit().create(WeatherService.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forcast_weather, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new
                LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        Log.d(TAG, "onCreateView: " + Common.current_location.toString());
        getForecastInformation();
        return view;
    }

    public void getForecastInformation() {
        //progressDialog.show();
        compositeDisposable.add(weatherService.getForeCastWeather(
                String.valueOf(Common.current_location.getLatitude()),
                String.valueOf(Common.current_location.getLongitude()),
                "7",
                Common.APP_ID,
                "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherForecastResult>() {
                    @Override
                    public void accept(WeatherForecastResult weatherForecastResult) throws Exception {
                        Log.d(TAG, "accept: "+weatherForecastResult.toString());
                        displayForecastWeather(weatherForecastResult);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "Error: "+throwable.getMessage() );
                        //progressDialog.dismiss();
                    }
                })
        );
    }

    public void displayForecastWeather(WeatherForecastResult weatherForecastResult) {
        WeatherForcastAdapter adapter = new WeatherForcastAdapter(getContext(), weatherForecastResult);
        recyclerView.setAdapter(adapter);
        //progressDialog.dismiss();
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

}
