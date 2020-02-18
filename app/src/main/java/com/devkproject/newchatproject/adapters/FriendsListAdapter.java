package com.devkproject.newchatproject.adapters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.FriendHolder> {

    private ArrayList<User> friendList;

    private DatabaseReference friendRef;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;


    public FriendsListAdapter() {
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

    public void removeItem(User friend) {
        int position = getItemPosition(friend.getUid());
        if(position > -1) {
            friendList.remove(position);
            notifyDataSetChanged();
        }
    }

    private int getItemPosition(String friendUid){
        int position = 0;
        for(User currentItem : friendList) {
            if(currentItem.getUid().equals(friendUid)) {
                return position;
            }
            position ++;
        }
        return -1;
    }
    @NonNull
    @Override
    public FriendHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_friend_item, parent, false);
        FriendHolder friendHolder = new FriendHolder(view);

        return friendHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendHolder holder, int position) {
        final User friend = getItem(position);

        holder.user_name.setText(friend.getUserNickname());
        holder.rootView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext(), R.style.MyAlertDialogStyle);
                builder.setMessage("친구를 삭제하시겠습니까?")
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeItem(friend);
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
                        })
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public class FriendHolder extends RecyclerView.ViewHolder {

        private CircleImageView user_image;
        private TextView user_name;
        private RelativeLayout rootView;

        public FriendHolder(@NonNull final View itemView) {
            super(itemView);
            user_image = (CircleImageView) itemView.findViewById(R.id.users_item_image);
            user_name = (TextView) itemView.findViewById(R.id.users_item_name);
            rootView = (RelativeLayout) itemView.findViewById(R.id.friend_rootView);
        }
    }
}
