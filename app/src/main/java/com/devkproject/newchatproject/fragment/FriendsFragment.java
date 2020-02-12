package com.devkproject.newchatproject.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.devkproject.newchatproject.ChatActivity;
import com.devkproject.newchatproject.R;
import com.devkproject.newchatproject.adapters.FriendsListAdapter;
import com.devkproject.newchatproject.customviews.RecyclerViewItemClickListener;
import com.devkproject.newchatproject.model.User;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class FriendsFragment extends Fragment {

    private static final String TAG = "FriendsFragment";

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference userRef;
    private DatabaseReference friendsRef;
    private FirebaseAnalytics mFirebaseAnalytics;
    private DatabaseReference mChatMemberRef;

    private RecyclerView recyclerView;
    private FriendsListAdapter friendsListAdapter;
    //private String test = "-M-oytdAyCntwEWmTaJH";

    public FriendsFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View friendsView = inflater.inflate(R.layout.fragment_friends, container,false);

        recyclerView = (RecyclerView) friendsView.findViewById(R.id.friends_recyclerView);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        friendsRef = FirebaseDatabase.getInstance().getReference("users").child(mUser.getUid()).child("friends");
        userRef = FirebaseDatabase.getInstance().getReference("users");
        mChatMemberRef = FirebaseDatabase.getInstance().getReference("chat_members");

        addFriendListener();

        friendsListAdapter = new FriendsListAdapter();
        recyclerView.setAdapter(friendsListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getContext(), new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final User friend = friendsListAdapter.getItem(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),3);
                builder.setTitle(friend.getUserNickname() + "님과 대화를 하시겠습니까 ?");
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                        chatIntent.putExtra("uid", friend.getUid());
                      //  chatIntent.putExtra("chat_id",test);
                        startActivityForResult(chatIntent, ChatFragment.JOIN_ROOM_REQUEST_CODE);
                    }
                }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        }));

        return friendsView;
    }
    private void chatMember() {
        mChatMemberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot item : dataSnapshot.getChildren()) {
                    String key = item.getKey();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addFriendListener() {

        friendsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // User 모델과 friend 는 구조가 동일하기 때문에 User 모델로 가져온다
                User friend = dataSnapshot.getValue(User.class);
                if(friend.getRequestType().equals("accept")) {
                    friendsListAdapter.addItem(friend);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                User friend = dataSnapshot.getValue(User.class);
                friendsListAdapter.removeItem(friend);
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
