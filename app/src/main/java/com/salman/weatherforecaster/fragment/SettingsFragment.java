package com.salman.weatherforecaster.fragment;



import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.salman.weatherforecaster.R;


public class SettingsFragment extends PreferenceFragment {


    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.my_preferences);

    }



}
