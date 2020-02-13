package com.devkproject.newchatproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.devkproject.newchatproject.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText email, password, nickName, passwordConfirm;
    private Button signUp_FinishButton, duplicationButton;
    private ProgressDialog loadingBar;
    private boolean duplication = false;

    private GoogleSignInClient mGoogleSignInClient;
    private static int GOOGLE_LOGIN_OPEN = 501;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        mAuth = FirebaseAuth.getInstance();

        email = (EditText) findViewById(R.id.signUp_email_editText);
        password = (EditText) findViewById(R.id.signUp_password_editText);
        passwordConfirm = (EditText) findViewById(R.id.signUp_passwordConfirm_editText);

        nickName = (EditText) findViewById(R.id.signUp_nick_editText);
        signUp_FinishButton = (Button) findViewById(R.id.signUp_FinishButton);
        duplicationButton = (Button) findViewById(R.id.signUp_duplication);
        loadingBar = new ProgressDialog(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        duplicationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Duplication();
            }
        });

        signUp_FinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignUp();
            }
        });

    }

    private void Duplication () {
        if(nickName.getText().toString().isEmpty()) {
            Toast.makeText(RegisterActivity.this, "닉네임을 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Iterator<DataSnapshot> userIterator = dataSnapshot.getChildren().iterator();
                    int userCount = (int) dataSnapshot.getChildrenCount();
                    int loopCount = 1;
                    while (userIterator.hasNext()) {
                        User userItem = userIterator.next().getValue(User.class);
                        if (nickName.getText().toString().equals(userItem.getUserNickname())) {
                            Toast.makeText(RegisterActivity.this, "존재하는 아이디입니다", Toast.LENGTH_SHORT).show();
                            duplication = false;
                        } else {
                            if (loopCount++ >= userCount) {
                                Toast.makeText(RegisterActivity.this, "사용할 수 있는 아이디입니다", Toast.LENGTH_SHORT).show();
                                duplication = true;
                                nickName.setEnabled(false);
                                duplicationButton.setEnabled(false);
                                nickName.setBackgroundColor(getResources().getColor(R.color.gray));
                                return;
                            }
                        }
                    }
                } else {
                    duplication = true;
                    //Toast.makeText(RegisterActivity.this, "회원가입이 가능한 아이디입니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void GoogleSignUp() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_LOGIN_OPEN);
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        final String userNick = "EnvEng";
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "로그인 성공");
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            final User user = new User();
                            user.setUserEmail(firebaseUser.getEmail());
                            user.setUid(firebaseUser.getUid());
                            user.setRequestType("");
                            user.setAfterCount(true);
                            user.setUserNickname(userNick);
                            UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(userNick).build();

                            mAuth.getCurrentUser().updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        userRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(!dataSnapshot.exists()) {
                                                    userRef.child(user.getUid()).setValue(user, new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                            if(databaseError == null) {
                                                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                                                finish();
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                                    finish();
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                                        });
                                    }
                                }
                            });

                        } else {
                            Log.w(TAG, "로그인 실패", task.getException());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
    private void SignUpUser() {
        final String userEmail = email.getText().toString();
        final String userNickName = nickName.getText().toString();
        final String userPassword = password.getText().toString();
        if(TextUtils.isEmpty(userEmail)) {
            Toast.makeText(RegisterActivity.this, "이메일을 입력하세요", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(userNickName)) {
            Toast.makeText(RegisterActivity.this, "닉네임을 입력하세요", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(userPassword)){
            Toast.makeText(RegisterActivity.this, "비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
        } else if (!userPassword.equals(passwordConfirm.getText().toString()) ) {
            Toast.makeText(RegisterActivity.this, "비밀번호가 다릅니다", Toast.LENGTH_SHORT).show();
        } else if (duplication == false) {
            Toast.makeText(RegisterActivity.this, "아이디 중복체크를 해주세요", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("계정 생성");
            loadingBar.setMessage("계정 생성 중입니다");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                final String currentUserID = mAuth.getCurrentUser().getUid();
                                // 회원가입할때 정보담기
                                User userModel = new User();
                                userModel.setUserNickname(userNickName);
                                userModel.setUserEmail(userEmail);
                                userModel.setUid(currentUserID);
                                userModel.setRequestType("");
                                userModel.setAfterCount(true);

                                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(userNickName).build();
                                FirebaseAuth.getInstance().getCurrentUser().updateProfile(userProfileChangeRequest);

                                FirebaseDatabase.getInstance().getReference().child("users").child(currentUserID).setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        RegisterActivity.this.finish();
                                        loadingBar.dismiss();
                                    }
                                });
                            } else {
                                String message = task.getException().toString();
                                Log.d(TAG, message);
                                loadingBar.cancel();
                            }
                        }
                    });
        }
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
                Log.w(TAG, "구글 로그인 실패", e);
            }
        }
    }
}
