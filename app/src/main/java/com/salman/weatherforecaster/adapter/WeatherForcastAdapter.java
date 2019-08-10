package com.salman.weatherforecaster.adapter;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.salman.weatherforecaster.R;
import com.salman.weatherforecaster.common.Common;
import com.salman.weatherforecaster.model.WeatherForecastResult;
import com.squareup.picasso.Picasso;

public class WeatherForcastAdapter extends RecyclerView.Adapter<WeatherForcastAdapter.MyViewHolder> {
    Context context;
    WeatherForecastResult weatherForecastResult;


    public WeatherForcastAdapter(Context context, WeatherForecastResult weatherForecastResult) {
        this.context = context;
        this.weatherForecastResult = weatherForecastResult;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int position) {

        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/w/")
                .append(weatherForecastResult.list.get(position).weather.get(0).getIcon())
                .append(".png").toString()).into(myViewHolder.image);

        myViewHolder.date.setText(new StringBuilder(Common.convertUnixToDate(weatherForecastResult.list.get(position).dt)));
        myViewHolder.day.setText(new StringBuilder(Common.convertUnixToDay(weatherForecastResult.list.get(position).dt)));
        myViewHolder.minTemp.setText(("Min: " + weatherForecastResult.list.get(position).temp.getMin()) + " °С");
        myViewHolder.maxTemp.setText(("Max: " + weatherForecastResult.list.get(position).temp.getMax()) + " °С");


    }

    @Override
    public int getItemCount() {
        return weatherForecastResult.list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView day, minTemp, date, maxTemp;
        ImageView image;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            day = itemView.findViewById(R.id.day);
            minTemp = itemView.findViewById(R.id.minTemp);
            image = itemView.findViewById(R.id.image);
            date = itemView.findViewById(R.id.date);
            maxTemp = itemView.findViewById(R.id.maxTemp);

        }
    }
}
