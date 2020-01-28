package com.devkproject.newchatproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.devkproject.newchatproject.R;
import com.devkproject.newchatproject.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.FriendHolder> {

    private ArrayList<User> friendList;
    private DatabaseReference friendRef;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;


    public FriendsListAdapter() {
        friendList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        friendRef = FirebaseDatabase.getInstance().getReference("users").child(mUser.getUid()).child("friends");
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
    public FriendHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_friend_item, parent, false);
        FriendHolder friendHolder = new FriendHolder(view);

        return friendHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendHolder holder, int position) {
        User friend = getItem(position);
        holder.user_name.setText(friend.getUserNickname());

        if(friend.getProfileImageUrl() != null) {
            Glide.with(holder.itemView)
                    .load(friend.getProfileImageUrl())
                    .into(holder.user_image);
        }
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public class FriendHolder extends RecyclerView.ViewHolder {

        private CircleImageView user_image;
        private TextView user_name;

        public FriendHolder(@NonNull final View itemView) {
            super(itemView);
            user_image = (CircleImageView) itemView.findViewById(R.id.users_item_image);
            user_name = (TextView) itemView.findViewById(R.id.users_item_name);
        }
    }
}
