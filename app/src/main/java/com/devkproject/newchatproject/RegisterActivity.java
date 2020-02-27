package com.devkproject.newchatproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.devkproject.newchatproject.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText nickName;
    private Button signUp_FinishButton, duplicationButton, signUp_changeAuth;
    private boolean duplication = false;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference userRef;
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        nickName = (EditText) findViewById(R.id.signUp_nick_editText);
        SetEditText();

        signUp_FinishButton = (Button) findViewById(R.id.signUp_FinishButton);
        duplicationButton = (Button) findViewById(R.id.signUp_duplication);
        signUp_changeAuth = (Button) findViewById(R.id.signUp_changeAuth);

        duplicationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Duplication();
            }
        });

        signUp_FinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (duplication == false) {
                    Toast.makeText(RegisterActivity.this, "닉네임 중복체크를 해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    SetDisplayName();
                }
            }
        });

        signUp_changeAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        SendUserToLoginActivity();
                    }
                });
            }
        });
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }

    private void SetDisplayName() {
        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(nickName.getText().toString()).build();
        mAuth.getCurrentUser().updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    userRef.child(mCurrentUser.getUid()).child("userNickname").setValue(nickName.getText().toString());
                    SendUserToMainActivity();
                }
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
                            Toast.makeText(RegisterActivity.this, "존재하는 닉네임입니다", Toast.LENGTH_SHORT).show();
                            duplication = false;
                        } else {
                            if (loopCount++ >= userCount) {
                                Toast.makeText(RegisterActivity.this, "사용할 수 있는 닉네임입니다", Toast.LENGTH_SHORT).show();
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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        return;
    }

    private void SetEditText() {
        nickName.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                Pattern ps = Pattern.compile("^[a-zA-Z0-9ㄱ-ㅎ가-힣]*$");
                if (!ps.matcher(source).matches()) {
                    return "";
                }
                return null;
            }
        }});
        nickName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
    }
}
