package com.devkproject.newchatproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.devkproject.newchatproject.fragment.ChatFragment;
import com.devkproject.newchatproject.fragment.FriendsFragment;
import com.devkproject.newchatproject.fragment.SetFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private FriendsFragment friendFragment = new FriendsFragment();
    private ChatFragment chatFragment = new ChatFragment();
    private SetFragment setFragment = new SetFragment();
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar); // 툴바를 액티비티의 앱바로 지정
        toolbar.setTitle(R.string.app_name);

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
                    case R.id.navi_setting: {
                        transaction.replace(R.id.main_frameLayout, setFragment).commitAllowingStateLoss();
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
                friendFragment.searchFriends();
                return true;
            case R.id.toolbar_multiChat:
                friendFragment.toggleSelectionMode();
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
}
