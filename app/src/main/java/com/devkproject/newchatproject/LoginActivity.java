package com.devkproject.newchatproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.devkproject.newchatproject.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private LinearLayout login_main_layout;
    private ImageButton login_image_button;

    private SignInButton signInButton;
    private GoogleSignInClient mGoogleSignInClient;
    private static int GOOGLE_LOGIN_OPEN = 501;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    // 로컬 캐시 기능 조심히 쓰자..
//    static {
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInButton = (SignInButton) findViewById(R.id.login_signUp_button);
        login_main_layout = (LinearLayout) findViewById(R.id.login_main_layout);
        login_image_button = (ImageButton) findViewById(R.id.login_image_button);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("users");

        if(mCurrentUser != null){
            userRef.child(mCurrentUser.getUid()).child("userNickname").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        if (dataSnapshot.getValue().equals("")) {
                            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                            finish();
                        } else {
                            SendUserToMainActivity();
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }

        login_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickImageButton();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setColorScheme(SignInButton.COLOR_DARK);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignIn();
            }
        });
    }
    private void GoogleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_LOGIN_OPEN);
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            final User user = new User();
                            user.setUserEmail(firebaseUser.getEmail());
                            user.setUid(firebaseUser.getUid());
                            user.setRequestType("");
                            user.setAfterCount(true);
                            user.setUserNickname("");

                            userRef.child(user.getUid()).child("userNickname").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()) {
                                        if(dataSnapshot.getValue().equals("")) {
                                            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                                            finish();
                                        } else {
                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                            finish();
                                        }
                                    } else {
                                        userRef.child(user.getUid()).setValue(user, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                if (databaseError == null) {
                                                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                                                    finish();
                                                }
                                            }
                                        });
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {}
                            });
                        } else {
                            Snackbar.make(login_main_layout, "로그인에 실패했습니다. 다시 시도해주세요.", Snackbar.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(login_main_layout, "로그인에 실패했습니다. 다시 시도해주세요.", Snackbar.LENGTH_LONG).show();
                    }
                });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GOOGLE_LOGIN_OPEN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "로그인 실패", e);
            }
        }
    }
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    private void ClickImageButton() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setMessage("# 닉네임을 따로 설정하여 사용하기 때문에 " +
                "타인의 구글 계정 정보를 알 수 없습니다.")
                .setPositiveButton("확인", null)
                .setTitle("알림")
                .show();
    }
}
