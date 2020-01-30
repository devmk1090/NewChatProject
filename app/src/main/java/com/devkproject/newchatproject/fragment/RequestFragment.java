package com.devkproject.newchatproject.fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devkproject.newchatproject.R;
import com.devkproject.newchatproject.adapters.RequestListAdapter;
import com.devkproject.newchatproject.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private static final String TAG = "RequestFragment";
    private RecyclerView recyclerView;
    private RequestListAdapter requestListAdapter;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference friendRef;

    public RequestFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View requestView = inflater.inflate(R.layout.fragment_request, container,false);

        recyclerView = (RecyclerView) requestView.findViewById(R.id.request_recyclerView);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        friendRef = FirebaseDatabase.getInstance().getReference("users").child(mCurrentUser.getUid()).child("friends");

        requestListAdapter = new RequestListAdapter();
        recyclerView.setAdapter(requestListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        requestFriendListener();

        return requestView;
    }

    private void requestFriendListener() {
        friendRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                User friend = dataSnapshot.getValue(User.class);
                if(friend.getRequestType().equals("receivedRequest")) {
                    requestListAdapter.addItem(friend);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
