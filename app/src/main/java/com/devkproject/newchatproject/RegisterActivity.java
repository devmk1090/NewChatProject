package com.devkproject.newchatproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.devkproject.newchatproject.model.User;
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

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText nickName;
    private Button signUp_FinishButton, duplicationButton;
    private boolean duplication = false;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference userRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        nickName = (EditText) findViewById(R.id.signUp_nick_editText);
        signUp_FinishButton = (Button) findViewById(R.id.signUp_FinishButton);
        duplicationButton = (Button) findViewById(R.id.signUp_duplication);

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
                    Toast.makeText(RegisterActivity.this, "아이디 중복체크를 해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    SetDisplayName();
                }
            }
        });
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
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
