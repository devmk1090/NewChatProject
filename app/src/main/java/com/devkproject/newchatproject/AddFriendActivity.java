package com.devkproject.newchatproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.devkproject.newchatproject.adapters.AddFriendListAdapter;
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

public class AddFriendActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText searchText;
    private ImageButton searchButton;
    private RecyclerView recyclerView;
    private ArrayList<User> friendList;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference userRef;
    private DatabaseReference friendRef;
    private DatabaseReference requesrRef;

    private AddFriendListAdapter addFriendListAdapter;
    public static String getName;

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
        searchButton = (ImageButton) findViewById(R.id.addFriend_search_button);
        recyclerView = (RecyclerView) findViewById(R.id.addFriend_recyclerView);
        friendList = new ArrayList<>();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("닉네임으로 친구 찾기");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitleTextColor(Color.WHITE);


        addFriendListAdapter = new AddFriendListAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(addFriendListAdapter);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFriend();

            }
        });
    }

    private void searchFriend() {
        getName = searchText.getText().toString();
        if(getName.isEmpty()) {
            Toast.makeText(AddFriendActivity.this, "닉네임을 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if(getName.equals(mCurrentUser.getDisplayName())) {
            Toast.makeText(AddFriendActivity.this, "자기 자신은 입력할 수 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot friendItem : dataSnapshot.getChildren()) {
                    User user = friendItem.getValue(User.class);
                    if (user.getUserNickname().equals(getName) && user.getRequestType().equals("")) {
                        Toast.makeText(AddFriendActivity.this, "이미 등록된 친구입니다", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(user.getUserNickname().equals(getName) && user.getRequestType().equals("sendRequest")) {
                        Toast.makeText(AddFriendActivity.this, "친구 신청을 이미 보냈습니다", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Iterator<DataSnapshot> usersIterator = dataSnapshot.getChildren().iterator();
                        int userCount = (int) dataSnapshot.getChildrenCount();
                        int loopCount = 1;

                        while (usersIterator.hasNext()) {

                            final User currentUser = usersIterator.next().getValue(User.class);
                            if (getName.equals(currentUser.getUserNickname())) {
                                addFriendListAdapter.clear();
                                addFriendListAdapter.addItem(currentUser);
                            } else {
                                if(loopCount++ >= userCount) {
                                    Toast.makeText(AddFriendActivity.this, "가입하지 않은 닉네임입니다", Toast.LENGTH_SHORT).show();
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
