package com.devkproject.newchatproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.devkproject.newchatproject.fragment.ChatFragment;
import com.devkproject.newchatproject.fragment.FriendsFragment;
import com.devkproject.newchatproject.fragment.RequestFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private FriendsFragment friendFragment = new FriendsFragment();
    private ChatFragment chatFragment = new ChatFragment();
    private RequestFragment requestFragment = new RequestFragment();
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        final Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar); // 툴바를 액티비티의 앱바로 지정
        getSupportActionBar().setTitle(mAuth.getCurrentUser().getDisplayName() + "님 환영합니다");
        toolbar.setTitleTextColor(Color.WHITE);

        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.main_frameLayout, friendFragment).commitAllowingStateLoss();

        BottomNavigationView bottomNavigationView = findViewById(R.id.main_bottom_navi);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                switch (item.getItemId()) {
                    case R.id.navi_friend: {
                        transaction.replace(R.id.main_frameLayout, friendFragment).commitAllowingStateLoss();
                        return true;
                    }
                    case R.id.navi_chat: {
                        transaction.replace(R.id.main_frameLayout, chatFragment).commitAllowingStateLoss();
                        return true;
                    }
                    case R.id.navi_request: {
                        transaction.replace(R.id.main_frameLayout, requestFragment).commitAllowingStateLoss();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_item, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.toolbar_search).getActionView();
        searchView.setQueryHint("친구 추가 검색");
        searchView.setMaxWidth(Integer.MAX_VALUE);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_search:
                return true;
            case R.id.toolbar_addFriend:
                SendAddFriendActivity();
                return true;
            case R.id.toolbar_logOut:
                mAuth.signOut();
                SendUserToLoginActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }
    private void SendAddFriendActivity() {
        Intent intent = new Intent(MainActivity.this, AddFriendActivity.class);
        startActivity(intent);
    }
}
