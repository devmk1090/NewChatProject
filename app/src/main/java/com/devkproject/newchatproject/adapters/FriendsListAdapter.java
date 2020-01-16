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

    public static final int UNSELECTION_MODE = 1;
    public static final int SELECTION_MODE = 2;
    private int selectionMode = UNSELECTION_MODE;

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
    public void setSelectionMode(int selectionMode) {
        this.selectionMode = selectionMode;
        notifyDataSetChanged();
    }
    public int getSelectionMode() {
        return this.selectionMode;
    }
    public int getSelectionUserCount() {
        int selectedCount = 0;
        for (User user : friendList) {
            if(user.isSelection()) { // true 인 사람만
                selectedCount ++;
            }
        }
        return selectedCount;
    }

    // 선택된 유저들의 정보를 리턴
    public String [] getSelectedUids() {
        String [] seletedUids = new String [getSelectionUserCount()];
        int i = 0;
        for (User user : friendList) {
            if(user.isSelection()) { // true 인 사람만
                seletedUids[i++] = user.getUid();
            }
        }
        return seletedUids;
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
        holder.user_email.setText(friend.getUserEmail());
        if(getSelectionMode() == UNSELECTION_MODE) {
            holder.user_check.setVisibility(View.GONE);
        } else {
            holder.user_check.setVisibility(View.VISIBLE);
        }
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
        private TextView user_name, user_email, user_status;
        private CheckBox user_check;

        public FriendHolder(@NonNull final View itemView) {
            super(itemView);
            user_image = (CircleImageView) itemView.findViewById(R.id.users_item_image);
            user_name = (TextView) itemView.findViewById(R.id.users_item_name);
            user_email = (TextView) itemView.findViewById(R.id.users_item_email);
            user_status = (TextView) itemView.findViewById(R.id.users_item_status);
            user_check = (CheckBox) itemView.findViewById(R.id.users_item_checkBox);
        }
    }
}
