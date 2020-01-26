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

import de.hdodenhof.circleimageview.CircleImageView;

public class AddFriendActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText searchText;
    private Button searchButton;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        toolbar = (Toolbar) findViewById(R.id.addFriend_toolbar);
        searchText = (EditText) findViewById(R.id.addFriend_search_editText);
        searchButton = (Button) findViewById(R.id.addFriend_search_button);
        recyclerView = (RecyclerView) findViewById(R.id.addFriend_recyclerView);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("아이디로 친구 찾기");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AddFriendActivityRecyclerViewAdapter());

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

        }

        @Override
        public int getItemCount() {
            return 0;
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
