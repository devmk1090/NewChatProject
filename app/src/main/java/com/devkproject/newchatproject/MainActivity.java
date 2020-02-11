package com.devkproject.newchatproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.devkproject.newchatproject.fragment.ChatFragment;
import com.devkproject.newchatproject.fragment.FriendsFragment;
import com.devkproject.newchatproject.fragment.RequestFragment;
import com.devkproject.newchatproject.model.Chat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private FriendsFragment friendFragment = new FriendsFragment();
    private ChatFragment chatFragment = new ChatFragment();
    private RequestFragment requestFragment = new RequestFragment();

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference userRef;
    private DatabaseReference mChatMemberRef;
    private DatabaseReference mChat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("users");
        mChatMemberRef = FirebaseDatabase.getInstance().getReference("chat_members");
        mChat = FirebaseDatabase.getInstance().getReference("chat_messages");

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
        // 검색 툴바
//        SearchView searchView = (SearchView) menu.findItem(R.id.toolbar_search).getActionView();
//        searchView.setQueryHint("친구 추가 검색");
//        searchView.setMaxWidth(Integer.MAX_VALUE);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_addFriend:
                SendAddFriendActivity();
                return true;
            case R.id.toolbar_help:
                toolbarHelp();
                return true;
            case R.id.toolbar_logOut:
                mAuth.signOut();
                SendUserToLoginActivity();
                return true;
            case R.id.toolbar_withdraw:
                MemberWithdraw();
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
    private void toolbarHelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("도움말")
                .setIcon(R.drawable.ic_help_black_24dp)
                .setMessage("블라블라블라")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }
    // [회원 탈퇴 로직]
    // 1.chat_member 삭제 -> 상대의 채팅방에 대화상대가 없는 방입니다 출력
    // 2.상대방 친구목록에서 나를 삭제
    // 3.users 의 나의 정보 삭제
    // 4.나의 계정 삭제
    private void MemberWithdraw() {
        mChatMemberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Chat item = dataSnapshot.getValue(Chat.class);
                String id = item.getChatID();
                Log.d(TAG, id);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
//        mCurrentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if(task.isSuccessful()) {
//                    Log.d(TAG, "계정 삭제 완료");
//                    userRef.child(mCurrentUser.getUid()).removeValue(new DatabaseReference.CompletionListener() {
//                        @Override
//                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
//                            finishAffinity();
//                        }
//                    });
//                }
//            }
//        });
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("알림")
                .setMessage("죵료하시겠습니까 ?")
                .setIcon(R.drawable.ic_info_black_24dp)
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
