package com.devkproject.newchatproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.devkproject.newchatproject.adapters.MessageListAdapter;
import com.devkproject.newchatproject.model.Chat;
import com.devkproject.newchatproject.model.Message;
import com.devkproject.newchatproject.model.PhotoMessage;
import com.devkproject.newchatproject.model.TextMessage;
import com.devkproject.newchatproject.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private String mChatID;
    private EditText chat_message;
    private ImageButton chat_camera, chat_send;
    private Toolbar mToolbar;

    private FirebaseDatabase mFirebaseDB;
    private DatabaseReference mChatRef;
    private DatabaseReference mChatMemberRef;
    private DatabaseReference mChatMessageRef;
    private DatabaseReference mUserRef;
    private FirebaseUser mCurrentUser;

    private RecyclerView recyclerView;
    private MessageListAdapter messageListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chat_message = (EditText) findViewById(R.id.chat_message_editText);
        chat_camera = (ImageButton) findViewById(R.id.chat_camera_button);
        chat_send = (ImageButton) findViewById(R.id.chat_send_button);
        mToolbar = (Toolbar) findViewById(R.id.chat_room_toolbar);
        recyclerView= (RecyclerView) findViewById(R.id.chat_recyclerView);

        mFirebaseDB = FirebaseDatabase.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserRef = mFirebaseDB.getReference("users");
        mChatID = getIntent().getStringExtra("chat_id");

        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextColor(Color.WHITE);

        if(mChatID != null) {
            mChatRef = mFirebaseDB.getReference("users").child(mCurrentUser.getUid()).child("chats").child(mChatID);
            mChatMessageRef = mFirebaseDB.getReference("chat_messages").child(mChatID);
            mChatMemberRef = mFirebaseDB.getReference("chat_members").child(mChatID);
            mChatRef.child("title").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String title = dataSnapshot.getValue(String.class);
                    mToolbar.setTitle(title);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            initTotalUnreadCount();
        }
        else {
            mChatRef = mFirebaseDB.getReference("users").child(mCurrentUser.getUid()).child("chats");
        }
        // 0. 방 정보 설정 <-- 기존 방이어야 가능함.
        // 1. 대화 상대에 내가 선택한 사람 추가
        // 2. 각 상대별 chats 에 방추가
        // 3. 메세지 정보 중 읽은 사람에 내 정보를 추가
        // 4. 첫 메세지 전송

        messageListAdapter = new MessageListAdapter();
        recyclerView.setAdapter(messageListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        chat_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendEvent(v);
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        if(mChatID != null) {
            removeMessageListener();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if(mChatID != null) {
            addMessageListener();
        }
    }
    private void removeMessageListener() {
        mChatMessageRef.removeEventListener(mMessageEventListener);
    }

    private void addMessageListener() {
        mChatMessageRef.addChildEventListener(mMessageEventListener);
    }

    ChildEventListener mMessageEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            // 신규메세지
            Message item = dataSnapshot.getValue(Message.class);
            /* 메세지 단위에서의 ReadUserList & UnreadCount 처리 */
            // 읽음 처리 : 내가 존재하는지를 확인.
            // chat_messages > {chat_id} > {message_id} > readUserList
            List<String> readUserUIDList = item.getReadUserList();
            if(readUserUIDList != null) {
                if(!readUserUIDList.contains(mCurrentUser.getUid())) {
                    // 존재하지 않는다면 chat_messages > {chat_id} > {message_id} > unreadCount -= 1
                    dataSnapshot.getRef().runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) { // mutableData - > 변하는 값들
                            Message mutableMessage = mutableData.getValue(Message.class); // readUserList 에 내 uid 추가 , unreadCount -= 1
                            List<String> mutableReadUserList = mutableMessage.getReadUserList();
                            mutableReadUserList.add(mCurrentUser.getUid());
                            int mutableUnreadCount = mutableMessage.getUnreadCount() - 1;

                            if(mutableMessage.getMessageType() == Message.MessageType.PHOTO) {
                                PhotoMessage mutablePhotoMessage = mutableData.getValue(PhotoMessage.class);
                                mutablePhotoMessage.setReadUserList(mutableReadUserList);
                                mutablePhotoMessage.setUnreadCount(mutableUnreadCount);
                                mutableData.setValue(mutablePhotoMessage);
                            }
                            else {
                                TextMessage mutableTextMessage = mutableData.getValue(TextMessage.class);
                                mutableTextMessage.setReadUserList(mutableReadUserList);
                                mutableTextMessage.setUnreadCount(mutableUnreadCount);
                                mutableData.setValue(mutableTextMessage);
                            }
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                        }
                    });
                }
            }
            if(item.getMessageType() == Message.MessageType.TEXT) {
                TextMessage textMessage = dataSnapshot.getValue(TextMessage.class);
                messageListAdapter.addItem(textMessage);
            }
            else if (item.getMessageType() == Message.MessageType.PHOTO) {
                PhotoMessage photoMessage = dataSnapshot.getValue(PhotoMessage.class);
                messageListAdapter.addItem(photoMessage);
            }
            // 읽음 처리 후 ui 처리
        }
        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            // 변경된 메세지 ex)unreadCount
            // 어댑터쪽에 변경된 메세지 데이터를 전달하고 메세지 아이디 번호로 해당 메세지의 위치를 알아내서 메세지 리스트의 값을 변경한다
            Message item = dataSnapshot.getValue(Message.class);
            if(item.getMessageType() == Message.MessageType.TEXT) {
                TextMessage textMessage = dataSnapshot.getValue(TextMessage.class);
                messageListAdapter.addItem(textMessage);
            }
            else if (item.getMessageType() == Message.MessageType.PHOTO) {
                PhotoMessage photoMessage = dataSnapshot.getValue(PhotoMessage.class);
                messageListAdapter.addItem(photoMessage);
            }
        }
        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {}
    };

    private void initTotalUnreadCount() { // 메세지를 읽지않은 사용자가 방에 들어갈때 읽음으로 표시하기 위해 0 으로 초기화
        mChatRef.child("totalUnreadCount").setValue(0);
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
        textMessage.setMessageUser(new User(mCurrentUser.getUid(), mCurrentUser.getEmail(), mCurrentUser.getDisplayName(), mCurrentUser.getPhotoUrl().toString()));
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
