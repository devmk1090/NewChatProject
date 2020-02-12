package com.devkproject.newchatproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.devkproject.newchatproject.R;
import com.devkproject.newchatproject.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestListAdapter extends RecyclerView.Adapter<RequestListAdapter.RequestHolder> {

    private ArrayList<User> friendList;

    private DatabaseReference friendRef;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    public RequestListAdapter() {
        friendList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        friendRef = FirebaseDatabase.getInstance().getReference("users").child(mCurrentUser.getUid()).child("friends");
        userRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public void addItem(User friend) {
        friendList.add(friend);
        notifyDataSetChanged();
    }

    public User getItem(int position) {
        return this.friendList.get(position);
    }

    @NonNull
    @Override
    public RequestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_friend_item, parent, false);
        return new RequestHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestHolder holder, int position) {
        final User friend = getItem(position);
        final RequestHolder requestHolder = (RequestHolder) holder;

        requestHolder.user_Accept.setVisibility(View.VISIBLE);
        requestHolder.user_Refuse.setVisibility(View.VISIBLE);

        requestHolder.user_Accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendRef.child(friend.getUid()).child("requestType").setValue("accept");
                userRef.child(friend.getUid()).child("friends").child(mCurrentUser.getUid()).child("requestType").setValue("accept");
                requestHolder.rootView.setVisibility(View.GONE);
                notifyDataSetChanged();
            }
        });
        requestHolder.user_Refuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendRef.child(friend.getUid()).child("requestType").setValue("refuse");
                requestHolder.rootView.setVisibility(View.GONE);
                notifyDataSetChanged();
                friendRef.child(friend.getUid()).removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        userRef.child(friend.getUid()).child("friends").child(mCurrentUser.getUid()).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            }
                        });
                    }
                });

            }
        });
        requestHolder.user_name.setText(friend.getUserNickname());
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public static class RequestHolder extends RecyclerView.ViewHolder {

        private CircleImageView user_image;
        private TextView user_name;
        private Button user_Accept, user_Refuse;
        private RelativeLayout rootView;

        public RequestHolder(@NonNull View itemView) {
            super(itemView);
            user_image = (CircleImageView) itemView.findViewById(R.id.users_item_image);
            user_name = (TextView) itemView.findViewById(R.id.users_item_name);
            user_Accept = (Button) itemView.findViewById(R.id.users_accept_button);
            user_Refuse = (Button) itemView.findViewById(R.id.users_refuse_button);
            rootView = (RelativeLayout) itemView.findViewById(R.id.friend_rootView);
        }
    }
}
