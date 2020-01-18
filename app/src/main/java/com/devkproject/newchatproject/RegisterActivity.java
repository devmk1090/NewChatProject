package com.devkproject.newchatproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.devkproject.newchatproject.model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final int PICK_FROM_ALBUM = 100;
    private RadioGroup gender;
    private RadioButton man;
    private String strGender;
    private EditText email, password, nickName;
    private ImageView profileImage;
    private Button signUp_FinishButton;
    private Uri imageUri;

    private StorageReference userProfileImagesRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        userProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        gender = (RadioGroup) findViewById(R.id.signUp_radioGroup);
        profileImage = (ImageView) findViewById(R.id.signUp_imageView_profile);
        email = (EditText) findViewById(R.id.signUp_email_editText);
        password = (EditText) findViewById(R.id.signUp_password_editText);
        nickName = (EditText) findViewById(R.id.signUp_nick_editText);
        signUp_FinishButton = (Button) findViewById(R.id.signUp_FinishButton);

        gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                man = gender.findViewById(checkedId);
                switch (checkedId) {
                    case R.id.signUp_man:
                    case R.id.signUp_woman:
                        strGender = man.getText().toString();
                        break;
                    default:
                }
            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_FROM_ALBUM);
            }
        });
        signUp_FinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUpUser();
            }
        });
    }
    public void SignUpUser() {
        final String userEmail = email.getText().toString();
        final String userNickName = nickName.getText().toString();
        final String userPassword = password.getText().toString();
        if(imageUri == null) {
            Toast.makeText(RegisterActivity.this, "이미지를 선택하세요", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(userEmail)) {
            Toast.makeText(RegisterActivity.this, "이메일을 입력하세요", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(userNickName)) {
            Toast.makeText(RegisterActivity.this, "닉네임을 입력하세요", Toast.LENGTH_SHORT).show();

        }
        else if(TextUtils.isEmpty(userPassword)){
            Toast.makeText(RegisterActivity.this, "비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();

        }
        else if (strGender == null) {
            Toast.makeText(RegisterActivity.this, "성별을 선택해주세요", Toast.LENGTH_SHORT).show();
        }
        else {
            mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                final String currentUserID = mAuth.getCurrentUser().getUid();
                                // 회원가입할때 정보담기
                                // DisplayName 을 userNickName 에서 받은 정보로 바꿔준다
                                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(userNickName)
                                        .setPhotoUri(imageUri).build();
                                task.getResult().getUser().updateProfile(userProfileChangeRequest);

                                final StorageReference filePath = userProfileImagesRef.child(currentUserID + ".jpg");
                                UploadTask uploadTask = filePath.putFile(imageUri);
                                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                    @Override
                                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                        if(!task.isSuccessful()) {
                                            throw task.getException();
                                        }
                                        return filePath.getDownloadUrl();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if(task.isSuccessful()) {
                                            Uri downloadUri = task.getResult();
                                            String imageUrl = downloadUri.toString();
                                            User userModel = new User();
                                            userModel.setUserNickname(userNickName);
                                            userModel.setUserEmail(userEmail);
                                            userModel.setProfileImageUrl(imageUrl);
                                            userModel.setUid(currentUserID);
                                            userModel.setGender(strGender);
                                            FirebaseDatabase.getInstance().getReference().child("users").child(currentUserID).setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    RegisterActivity.this.finish();
                                                }
                                            });
                                        }
                                        else {
                                            Toast.makeText(RegisterActivity.this, "업로드에 실패하였습니다", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
//                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
//                                startActivity(intent);
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK && data != null) {
            profileImage.setImageURI(data.getData()); // 가운데 뷰를 바꾼다
            imageUri = data.getData(); // 이미지 경로 원본
        }
    }
}
