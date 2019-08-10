package com.salman.weatherforecaster.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.salman.weatherforecaster.R;

import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlacesFragment extends Fragment {
    private static final String TAG = "PlacesFragment";
    Context mContex;
    private static final String  API_KEY = "AIzaSyAfjybPkW3yYWg3EtS6jUS0yTWEwzeBX5I";
    PlacesClient placesClient;
    public PlacesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_places, container, false);


        if (!Places.isInitialized()) {
            Places.initialize(getActivity(), API_KEY);
        }

        placesClient = Places.createClient(mContex);
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment =
                (AutocompleteSupportFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng latLng = place.getLatLng();
                Log.d(TAG, "onPlaceSelected: "+ "lat: " +latLng.latitude + "lng: " +latLng.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContex = context;
    }
}
