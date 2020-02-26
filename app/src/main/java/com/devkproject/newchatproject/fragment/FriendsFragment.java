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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devkproject.newchatproject.ChatActivity;
import com.devkproject.newchatproject.R;
import com.devkproject.newchatproject.adapters.FriendsListAdapter;
import com.devkproject.newchatproject.customviews.RecyclerViewItemClickListener;
import com.devkproject.newchatproject.model.User;
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
    private FirebaseUser mCurrentUser;
    private DatabaseReference userRef;
    private DatabaseReference friendsRef;
    private DatabaseReference mChatMemberRef;

    private RecyclerView recyclerView;
    private FriendsListAdapter friendsListAdapter;

    public FriendsFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View friendsView = inflater.inflate(R.layout.fragment_friends, container,false);

        recyclerView = (RecyclerView) friendsView.findViewById(R.id.friends_recyclerView);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        friendsRef = FirebaseDatabase.getInstance().getReference("users").child(mCurrentUser.getUid()).child("friends");
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

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
                builder.setMessage(friend.getUserNickname() + "님과 대화하시겠습니까 ?")
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                                chatIntent.putExtra("uid", friend.getUid());
                                startActivityForResult(chatIntent, ChatFragment.JOIN_ROOM_REQUEST_CODE);
                            }
                        })
                        .setNegativeButton("아니오", null)
                        .show();
            }
        }));

        return friendsView;
    }

    private void addFriendListener() {

        friendsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
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
