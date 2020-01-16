package com.devkproject.newchatproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.devkproject.newchatproject.model.Chat;
import com.devkproject.newchatproject.model.Message;
import com.devkproject.newchatproject.model.TextMessage;
import com.devkproject.newchatproject.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private String mChatID;
    private EditText chat_message;
    private ImageButton chat_camera, chat_send;

    private FirebaseDatabase mFirebaseDB;
    private DatabaseReference mChatRef;
    private DatabaseReference mChatMemberRef;
    private DatabaseReference mChatMessageRef;
    private DatabaseReference mUserRef;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chat_message = (EditText) findViewById(R.id.chat_message_editText);
        chat_camera = (ImageButton) findViewById(R.id.chat_camera_button);
        chat_send = (ImageButton) findViewById(R.id.chat_send_button);
        mFirebaseDB = FirebaseDatabase.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserRef = mFirebaseDB.getReference("users");

        mChatID = getIntent().getStringExtra("chat_id");

        // 0. 방 정보 설정 <-- 기존 방이어야 가능함.
        // 1. 대화 상대에 내가 선택한 사람 추가
        // 2. 각 상대별 chats 에 방추가
        // 3. 메세지 정보 중 읽은 사람에 내 정보를 추가
        // 4. 첫 메세지 전송

        chat_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendEvent(v);
            }
        });

    }
    public void onSendEvent(View v) {
        if(mChatID != null) {
            sendMessage();
        } else {
            createChat();
        }
    }
    private void sendMessage() {
        // 메세지 키 생성
        mChatMessageRef = mFirebaseDB.getReference("chat_messages").child(mChatID);
        // chat_message > {chat_id} > {message_id} > messageinfo
        String messageID = mChatMessageRef.push().getKey();
        String messageText = chat_message.getText().toString();

        if(messageText.isEmpty()) {
            return;
        }
        final TextMessage textMessage = new TextMessage();
        textMessage.setMessageText(messageText);
        textMessage.setMessageDate(new Date());
        textMessage.setChatID(mChatID);
        textMessage.setMessageID(messageID);
        textMessage.setMessageType(Message.MessageType.TEXT);
        textMessage.setReadUserList(Arrays.asList(new String[]{mCurrentUser.getUid()})); // 자기 자신은 읽었으므로
        String [] uids = getIntent().getStringArrayExtra("uids");
        if(uids != null) {
            textMessage.setUnreadCount(uids.length - 1);
        }
        chat_message.setText("");

        // 상대방 정보 가져오기
        mChatMemberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                // unreadCount 세팅하기 위한 상대방의 수를 가져옴.
                long memberCount = dataSnapshot.getChildrenCount();
                textMessage.setUnreadCount((int) (memberCount - 1));

                mChatMessageRef.child(textMessage.getMessageID()).setValue(textMessage, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        Iterator<DataSnapshot> memberIterator = dataSnapshot.getChildren().iterator();
                        while(memberIterator.hasNext()) {
                            // 대화 상대 한명한명 돌면서 정보를 읽는다
                            User chatMember = memberIterator.next().getValue(User.class);
                            mUserRef.child(chatMember.getUid()).child("chats").child(mChatID).child("lastMessage").setValue(textMessage);
                            if(!chatMember.getUid().equals(mCurrentUser.getUid())) {
                                mUserRef.child(chatMember.getUid()).child("chats").child(mChatID).child("totalUnreadCount")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                long totalUnreadCount = dataSnapshot.getValue(long.class);
                                                dataSnapshot.getRef().setValue(totalUnreadCount + 1);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                            }
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private boolean isSentMessage = false;
    private void createChat() {
        final Chat chat = new Chat();
        // users > {uid} > chats > {chat_uid} 의 고유 값을 가져온다
        mChatRef = mFirebaseDB.getReference("users").child(mCurrentUser.getUid()).child("chats");
        mChatID = mChatRef.push().getKey();
        mChatMemberRef = mFirebaseDB.getReference("chat_members").child(mChatID);
        mChatMessageRef = mFirebaseDB.getReference("chat_messages").child(mChatID);

        chat.setChatID(mChatID);
        chat.setCreateDate(new Date());

        String uid = getIntent().getStringExtra("uid");
        String [] uids = getIntent().getStringArrayExtra("uids");
        if(uid != null) {
            // 1:1 방
            uids = new String[]{uid};
        }
        // String 형이었던 uids 가 arrayList 형으로 바뀐다. -> Arrays.asList
        List<String> uidList = new ArrayList<>(Arrays.asList(uids));
        uidList.add(mCurrentUser.getUid());

        // 타이틀에 표시되는 사용자1, 사용자2 등등을 위한 코드

        for(String userId : uidList) {
            // uid > userinfo 를 가져와야함
            mUserRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                    User member = dataSnapshot.getValue(User.class);

                    mChatMemberRef.child(member.getUid()).setValue(member, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            // USERS > uid > chats > {chat_id} > chatinfo
                            dataSnapshot.getRef().child("chats").child(mChatID).setValue(chat);
                            if(!isSentMessage) {
                                sendMessage();
                                // 한번 메세지 보내면 못보내도록 true 로 변경
                                isSentMessage = true;
                            }
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
