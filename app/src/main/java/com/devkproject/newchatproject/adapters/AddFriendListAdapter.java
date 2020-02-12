package com.devkproject.newchatproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.devkproject.newchatproject.AddFriendActivity;
import com.devkproject.newchatproject.R;
import com.devkproject.newchatproject.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddFriendListAdapter extends RecyclerView.Adapter<AddFriendListAdapter.AddFriendHolder> {

    private ArrayList<User> friendList;
    private DatabaseReference friendRef;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference userRef;


    public AddFriendListAdapter() {
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

    public void clear() {
        friendList.clear();
    }

    @NonNull
    @Override
    public AddFriendHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_friend_item, parent, false);
        AddFriendHolder addFriendHolder = new AddFriendHolder(v);
        return addFriendHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final AddFriendHolder holder, final int position) {
        final User friend = getItem(position);

        holder.userRequest.setVisibility(View.VISIBLE);
        holder.userRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Iterator<DataSnapshot> usersIterator = dataSnapshot.getChildren().iterator();
                        while(usersIterator.hasNext()) {

                            final User currentUser = usersIterator.next().getValue(User.class);

                            if(currentUser.getUserNickname().equals(AddFriendActivity.getName)) {

                                friendRef.child(currentUser.getUid()).setValue(currentUser, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                        friendRef.child(currentUser.getUid()).child("requestType").setValue("sendRequest");
                                        userRef.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                User user = dataSnapshot.getValue(User.class);
                                                // 내 정보를 친구에게 등록
                                                userRef.child(currentUser.getUid()).child("friends").child(mCurrentUser.getUid()).setValue(user);

                                                userRef.child(currentUser.getUid()).child("friends").child(mCurrentUser.getUid()).child("requestType").setValue("receivedRequest");
                                                Toast.makeText(v.getContext(), "친구신청 완료", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                holder.rootView.setVisibility(View.GONE);
                notifyDataSetChanged();
            }
        });

        holder.userName.setText(friend.getUserNickname());
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public static class AddFriendHolder extends RecyclerView.ViewHolder {

        public CircleImageView imageView;
        public TextView userName;
        public Button userAccept, userRefuse, userRequest;
        private RelativeLayout rootView;

        public AddFriendHolder(@NonNull View v) {
            super(v);
            imageView = (CircleImageView) v.findViewById(R.id.users_item_image);
            userName = (TextView) v.findViewById(R.id.users_item_name);
            userAccept = (Button) v.findViewById(R.id.users_accept_button);
            userRefuse = (Button) v.findViewById(R.id.users_refuse_button);
            userRequest = (Button) v.findViewById(R.id.users_request_button);
            rootView = (RelativeLayout) itemView.findViewById(R.id.friend_rootView);
        }
    }
}
