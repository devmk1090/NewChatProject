package com.devkproject.newchatproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.devkproject.newchatproject.fragment.ChatFragment;
import com.devkproject.newchatproject.fragment.FriendsFragment;
import com.devkproject.newchatproject.fragment.RequestFragment;
import com.devkproject.newchatproject.model.User;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

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
    private GoogleSignInClient mGoogleSignInClient;

    private AdView adView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });
        adView = findViewById(R.id.main_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("users");
        mChatMemberRef = FirebaseDatabase.getInstance().getReference("chat_members");
        mChat = FirebaseDatabase.getInstance().getReference("chat_messages");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        pushToken();

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
                SignOut();
                return true;
            case R.id.toolbar_withdraw:
                MemberWithdraw();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void SendAddFriendActivity() {
        Intent intent = new Intent(MainActivity.this, AddFriendActivity.class);
        startActivity(intent);
    }
    private void toolbarHelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setTitle("도움말")
                .setMessage("# 친구 신청 : 상단 오른쪽의 사람 아이콘을 클릭하고 상대방의 닉네임을 검색 후 친구신청 아이콘 클릭" +
                        "\n\n" +
                        "# 친구 추가 : 상대방이 친구 신청을 했다면 '요청' 탭에서 수락 클릭" +
                        "\n\n" +
                        "# 친구 삭제 : 친구를 길게 터치하면 대화상자가 나타납니다." +
                        "\n\n" +
                        "# 대화방 나가기 : 대화방을 길게 터치하면 대화상자가 나타납니다.")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }
    private void SignOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setTitle("로그아웃")
                .setMessage("로그아웃 하시겠습니까 ?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();
                        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                SendUserToLoginActivity();
                            }
                        });
                    }
                })
                .setNegativeButton("아니오", null)
                .show();
    }
    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }
    // [회원 탈퇴 로직]
    // 1.나의 계정 삭제
    // 2.users 의 나의 정보 삭제
    // 3.chat_members 에서 chatID 를 얻어온다. 값이 없으면 finishAffinity 를 호출하여 종료.
    // 3-1.chat_members 가 한명뿐이라면 chat_message 삭제
    // 4.chat_members 에서 나를 삭제
    // 5.상대방 친구 목록에서 나를 삭제
    private void MemberWithdraw() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setTitle("회원 탈퇴 설명")
                .setMessage("1.어플에 등록된 구글 계정 삭제" +
                        "\n" +
                        "2.데이터베이스에 있는 나의 정보 삭제" +
                        "\n" +
                        "3.모든 대화방에서 나가기" +
                        "\n" +
                        "4.나의 친구목록 삭제" +
                        "\n" +
                        "5.상대방 친구목록에서 나를 삭제")
                .setPositiveButton("탈퇴", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
                        builder1.setMessage("정말 탈퇴하시겠습니까 ?")
                                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mCurrentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()) {

                                                    userRef.child(mCurrentUser.getUid()).removeValue(new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(@Nullable final DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                                            mChatMemberRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    if(dataSnapshot.exists()) {
                                                                        for (DataSnapshot item : dataSnapshot.getChildren()) {
                                                                            final String key = item.getKey();

                                                                            mChatMemberRef.child(key).child(mCurrentUser.getUid()).removeValue(new DatabaseReference.CompletionListener() {
                                                                                @Override
                                                                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                            for (DataSnapshot uid : dataSnapshot.getChildren()) {
                                                                                                User user = uid.getValue(User.class);
                                                                                                if (!mCurrentUser.getUid().equals(user.getUid())) {
                                                                                                    userRef.child(user.getUid()).child("friends").child(mCurrentUser.getUid()).removeValue(new DatabaseReference.CompletionListener() {
                                                                                                        @Override
                                                                                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                                                            finishAffinity();
                                                                                                        }
                                                                                                    });
                                                                                                }
                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                        }
                                                                                    });
                                                                                }
                                                                            });
                                                                        }
                                                                    } else {
                                                                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                for (DataSnapshot uid : dataSnapshot.getChildren()) {
                                                                                    User user = uid.getValue(User.class);
                                                                                    if (!mCurrentUser.getUid().equals(user.getUid())) {
                                                                                        userRef.child(user.getUid()).child("friends").child(mCurrentUser.getUid()).removeValue(new DatabaseReference.CompletionListener() {
                                                                                            @Override
                                                                                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                                                finishAffinity();
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                }
                                                            });
                                                        }
                                                    });
                                                } else {
                                                    Log.d(TAG, "회원 탈퇴 실패");
                                                    Toast.makeText(MainActivity.this, "잠시 후 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                })
                                .setNegativeButton("아니오", null)
                                .show();
                    }
                }).setNegativeButton("취소", null)
                .show();

    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setTitle("알림")
                .setMessage("죵료하시겠습니까 ?")
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
    private void pushToken() {

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if(!task.isSuccessful()) {
                    return;
                }
                String token = task.getResult().getToken();
                userRef.child(mCurrentUser.getUid()).child("deviceToken").setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "토큰 생성 완료");
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        userRef.child(mCurrentUser.getUid()).child("status").setValue(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userRef.child(mCurrentUser.getUid()).child("status").setValue(false);
    }
}
