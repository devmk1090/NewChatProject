package com.devkproject.newchatproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

public class ChatActivity extends AppCompatActivity {

    private EditText chat_message;
    private ImageButton chat_camera, chat_send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chat_message = (EditText) findViewById(R.id.chat_message_editText);
        chat_camera = (ImageButton) findViewById(R.id.chat_camera_button);
        chat_send = (ImageButton) findViewById(R.id.chat_send_button);

        String uid = getIntent().getStringExtra("uid");
        String [] uids = getIntent().getStringArrayExtra("uids");
    }
}
