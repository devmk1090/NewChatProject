package com.devkproject.newchatproject.fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devkproject.newchatproject.R;

public class ChatFragment extends Fragment {


    public ChatFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View chatView = inflater.inflate(R.layout.fragment_chat, container,false);
        return chatView;
    }

}
