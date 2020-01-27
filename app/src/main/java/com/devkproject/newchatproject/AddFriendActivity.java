package com.devkproject.newchatproject;

import androidx.annotation.NonNull;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("users");

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
    }

    private void searchFriend() {
        String getName = searchText.getText().toString();
        if(getName.isEmpty()) {
            Toast.makeText(AddFriendActivity.this, "아이디를 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if(getName.equals(mCurrentUser.getDisplayName())) {

        }
    }

    class AddFriendActivityRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public AddFriendActivityRecyclerViewAdapter() {

        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_friend_item, parent, false);
            return new CustomViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            CustomViewHolder customViewHolder = (CustomViewHolder) holder;

            customViewHolder.userName.setText("테스트123");

        }

        @Override
        public int getItemCount() {
            return friendList.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {

            public CircleImageView imageView;
            public TextView userName, userEmail, userStatus;

            public CustomViewHolder(@NonNull View v) {
                super(v);
                imageView = (CircleImageView) v.findViewById(R.id.users_item_image);
                userName = (TextView) v.findViewById(R.id.users_item_name);
                userEmail = (TextView) v.findViewById(R.id.users_item_email);
                userStatus = (TextView) v.findViewById(R.id.users_item_status);
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
