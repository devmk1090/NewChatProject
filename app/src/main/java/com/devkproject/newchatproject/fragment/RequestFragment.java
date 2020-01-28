package com.devkproject.newchatproject.fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devkproject.newchatproject.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View setView = inflater.inflate(R.layout.fragment_request, container,false);
        return setView;
    }

}
