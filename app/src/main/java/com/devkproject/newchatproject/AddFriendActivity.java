package com.devkproject.newchatproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.devkproject.newchatproject.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddFriendActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText searchText;
    private Button searchButton;
    private RecyclerView recyclerView;
    private ArrayList<User> friendList;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference userRef;
    private DatabaseReference friendRef;
    private DatabaseReference requesrRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("users");
        friendRef = FirebaseDatabase.getInstance().getReference("users").child(mCurrentUser.getUid()).child("friends");
        requesrRef = FirebaseDatabase.getInstance().getReference("friend_request");

        toolbar = (Toolbar) findViewById(R.id.addFriend_toolbar);
        searchText = (EditText) findViewById(R.id.addFriend_search_editText);
        searchButton = (Button) findViewById(R.id.addFriend_search_button);
        recyclerView = (RecyclerView) findViewById(R.id.addFriend_recyclerView);
        friendList = new ArrayList<>();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("아이디로 친구 찾기");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AddFriendActivityRecyclerViewAdapter());

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFriend();
            }
        });
        addFriendListener();
    }

    private void searchFriend() {
        final String getName = searchText.getText().toString();
        if(getName.isEmpty()) {
            Toast.makeText(AddFriendActivity.this, "아이디를 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if(getName.equals(mCurrentUser.getDisplayName())) {
            Toast.makeText(AddFriendActivity.this, "자기 자신은 친구로 입력할 수 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot friendItem : dataSnapshot.getChildren()) {
                    User user = friendItem.getValue(User.class);
                    if(user.getUserNickname().equals(getName)) {
                        Toast.makeText(AddFriendActivity.this, "이미 등록된 친구입니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Iterator<DataSnapshot> usersIterator = dataSnapshot.getChildren().iterator();
                        int userCount = (int) dataSnapshot.getChildrenCount();
                        int loopCount = 1;

                        while(usersIterator.hasNext()) {

                            final User currentUser = usersIterator.next().getValue(User.class);
                            if(getName.equals(currentUser.getUserNickname())) {
                                friendRef.push().setValue(currentUser, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                        userRef.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                User user = dataSnapshot.getValue(User.class);
                                                // 내 정보를 친구에게 등록
                                                userRef.child(currentUser.getUid()).child("friends").push().setValue(user);
                                                Toast.makeText(AddFriendActivity.this, "친구등록 완료", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                });
                                requesrRef.child(mCurrentUser.getUid()).child(currentUser.getUid()).push().setValue("sent");
                                requesrRef.child(currentUser.getUid()).child(mCurrentUser.getUid()).push().setValue("received");
                            } else {
                                if(loopCount++ >= userCount) {
                                    Toast.makeText(AddFriendActivity.this, "가입을 하지 않은 아이디입니다", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addFriendListener() {
        friendRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                User friend = dataSnapshot.getValue(User.class);
                friendList.add(friend);
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

    class AddFriendActivityRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public AddFriendActivityRecyclerViewAdapter() {

        }

        public User getItem(int position) {
            return friendList.get(position);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_friend_item, parent, false);
            return new CustomViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            User friend = getItem(position);
            CustomViewHolder customViewHolder = (CustomViewHolder) holder;

            customViewHolder.userAccept.setVisibility(View.VISIBLE);
            customViewHolder.userRefuse.setVisibility(View.VISIBLE);
            customViewHolder.userName.setText(friend.getUserNickname());
            if(friend.getProfileImageUrl() != null) {
                Glide.with(holder.itemView)
                        .load(friend.getProfileImageUrl())
                        .into(customViewHolder.imageView);
            }


        }

        @Override
        public int getItemCount() {
            return friendList.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {

            public CircleImageView imageView;
            public TextView userName;
            public Button userAccept, userRefuse;

            public CustomViewHolder(@NonNull View v) {
                super(v);
                imageView = (CircleImageView) v.findViewById(R.id.users_item_image);
                userName = (TextView) v.findViewById(R.id.users_item_name);
                userAccept = (Button) v.findViewById(R.id.users_accept_button);
                userRefuse = (Button) v.findViewById(R.id.users_refuse_button);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
