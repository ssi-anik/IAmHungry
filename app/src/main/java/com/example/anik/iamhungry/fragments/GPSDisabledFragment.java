package com.example.anik.iamhungry.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.anik.iamhungry.R;

/**
 * Created by Anik on 04-Sep-15, 004.
 */
public class GPSDisabledFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_location_disabled, container, false);
        return view;
    }
}
