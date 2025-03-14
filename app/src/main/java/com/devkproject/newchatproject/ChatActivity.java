package com.devkproject.newchatproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.devkproject.newchatproject.adapters.MessageListAdapter;
import com.devkproject.newchatproject.fragment.ChatFragment;
import com.devkproject.newchatproject.model.AfterMessage;
import com.devkproject.newchatproject.model.Chat;
import com.devkproject.newchatproject.model.ExitMessage;
import com.devkproject.newchatproject.model.Message;
import com.devkproject.newchatproject.model.NotificationModel;
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
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private String mChatID;
    private EditText chat_message;
    private ImageButton chat_side, chat_send;
    private Toolbar mToolbar;
    private RelativeLayout message_rootView;

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
        chat_side = (ImageButton) findViewById(R.id.chat_side_button);
        chat_send = (ImageButton) findViewById(R.id.chat_send_button);
        mToolbar = (Toolbar) findViewById(R.id.chat_room_toolbar);
        recyclerView= (RecyclerView) findViewById(R.id.chat_recyclerView);
        message_rootView = (RelativeLayout) findViewById(R.id.message_rootView);


        mFirebaseDB = FirebaseDatabase.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserRef = mFirebaseDB.getReference("users");
        mChatID = getIntent().getStringExtra("chat_id");

        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextColor(Color.WHITE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        if(mChatID != null) {
            mChatRef = mFirebaseDB.getReference("users").child(mCurrentUser.getUid()).child("chats").child(mChatID);
            mChatMessageRef = mFirebaseDB.getReference("chat_messages").child(mChatID);
            mChatMemberRef = mFirebaseDB.getReference("chat_members").child(mChatID);
            ChatFragment.JOINED_ROOM = mChatID;
            initTotalUnreadCount();
        } else {
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

        chat_side.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this, R.style.MyAlertDialogStyle);
                builder.setTitle("애프터 신청")
                        .setMessage("# 상대가 수락하면 서로의 채팅옆에 '하트'가 표시되며 계속 채팅을 이어갈 수 있습니다." +
                                "\n\n" +
                                "# 상대가 거절하면 상대방은 채팅방에서 자동으로 나가며 서로의 친구등록이 삭제됩니다.")
                        .setPositiveButton("신청", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mChatID != null) {
                                    mChatMemberRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            int i = (int) dataSnapshot.getChildrenCount();
                                            for (DataSnapshot friendItem : dataSnapshot.getChildren()) {
                                                User friendUser = friendItem.getValue(User.class);
                                                if (!friendUser.getUid().equals(mCurrentUser.getUid()) && friendUser.isAfterCount() == true && i > 1) {
                                                    AfterEvent();
                                                    fcmListener();
                                                    mChatMemberRef.child(friendUser.getUid()).child("afterCount").setValue(false);
                                                    return;
                                                }
                                                if (!friendUser.getUid().equals(mCurrentUser.getUid()) && friendUser.isAfterCount() == false) {
                                                    Toast.makeText(ChatActivity.this, "이미 애프터 신청을 하셨습니다", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                                } else {
                                    Toast.makeText(ChatActivity.this, "대화를 먼저 나눈뒤 실행하세요", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("취소", null)
                        .show();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mChatID != null) {
            removeMessageListener();
        }
        mUserRef.child(mCurrentUser.getUid()).child("status").setValue(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mUserRef.child(mCurrentUser.getUid()).child("status").setValue(true);

        if(mChatID != null) {
            // 총 메세지의 카운터를 가져온다
            // onChildAdded 에서 호출한 변수의 값이 총메세지의 값과 크거나 같다면, 포커스를 맨아래로 내려준다
            mChatMessageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    long totalMessageCount = dataSnapshot.getChildrenCount();
                    mMessageEventListener.setTotalMessageCount(totalMessageCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            messageListAdapter.clearItem(); // 2개씩 붙어 나오기 때문에 clear 호출
            addChatListener();
            addMessageListener();
        }
    }

    private void initTotalUnreadCount() { // 메세지를 읽지않은 사용자가 방에 들어갈때 읽음으로 표시하기 위해 0 으로 초기화
        mChatRef.child("totalUnreadCount").setValue(0);
    }

    private void addChatListener() { // 방 제목 리스너
        mChatRef.child("title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String title = dataSnapshot.getValue(String.class);
                if(title != null) {
                    mToolbar.setTitle(title+ "님과 대화중");
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void removeMessageListener() {
        mChatMessageRef.removeEventListener(mMessageEventListener);
    }

    private void addMessageListener() {
        mChatMessageRef.addChildEventListener(mMessageEventListener);
    }

    MessageEventListener mMessageEventListener = new MessageEventListener();

    private class MessageEventListener implements ChildEventListener {

        private long totalMessageCount;
        private long callCount = 1;

        public void setTotalMessageCount(long totalMessageCount) {
            this.totalMessageCount = totalMessageCount;
        }

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

                            if(mutableMessage.getMessageType() == Message.MessageType.TEXT) {
                                TextMessage mutableTextMessage = mutableData.getValue(TextMessage.class);
                                mutableTextMessage.setReadUserList(mutableReadUserList);
                                mutableTextMessage.setUnreadCount(mutableUnreadCount);
                                mutableData.setValue(mutableTextMessage);
                            } else {
                                AfterMessage mutableAfterMessage = mutableData.getValue(AfterMessage.class);
                                mutableAfterMessage.setReadUserList(mutableReadUserList);
                                mutableAfterMessage.setUnreadCount(mutableUnreadCount);
                                mutableData.setValue(mutableAfterMessage);
                            }
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                            // 읽는 순간 0 으로 초기화.
                            // Timer, TimeTask 를 이용하여 0.5초 딜레이를 준다
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    initTotalUnreadCount();
                                }
                            }, 500);
                        }
                    });
                }
            }
            //ui
            if(item.getMessageType() == Message.MessageType.TEXT) {
                TextMessage textMessage = dataSnapshot.getValue(TextMessage.class);
                messageListAdapter.addItem(textMessage);
            } else if (item.getMessageType() == Message.MessageType.EXIT) {
                messageListAdapter.addItem(item);
            } else if (item.getMessageType() == Message.MessageType.AFTER) {
                messageListAdapter.addItem(item);
            }
            if(callCount >= totalMessageCount) {
                // 스크롤을 맨 마지막으로 내린다
                recyclerView.scrollToPosition(messageListAdapter.getItemCount() - 1);
            }
            callCount ++;
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            // 실시간 반영
            // 변경된 메세지 ex)unreadCount
            // 어댑터쪽에 변경된 메세지 데이터를 전달하고 메세지 아이디 번호로 해당 메세지의 위치를 알아내서 메세지 리스트의 값을 변경한다
            Message item = dataSnapshot.getValue(Message.class);

            if(item.getMessageType() == Message.MessageType.TEXT) {
                TextMessage textMessage = dataSnapshot.getValue(TextMessage.class);
                messageListAdapter.updateItem(textMessage);
            } else if (item.getMessageType() == Message.MessageType.AFTER) {
                AfterMessage afterMessage = dataSnapshot.getValue(AfterMessage.class);
                messageListAdapter.updateItem(afterMessage);
            } else {
                ExitMessage exitMessage = dataSnapshot.getValue(ExitMessage.class);
                messageListAdapter.updateItem(exitMessage);
            }

        }
        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {}
    }

    public void onSendEvent(View v) {
        if(mChatID != null) {
            mChatMemberRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int i = (int) dataSnapshot.getChildrenCount();
                    if(i > 1) {
                        sendMessage();
                        fcmListener();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else {
            createChat();
        }
    }

    private Message.MessageType messageType = Message.MessageType.TEXT;

    private void AfterEvent() {
        DatabaseReference afterMessageRef = mFirebaseDB.getReference("chat_messages").child(mChatID);
        final AfterMessage afterMessage = new AfterMessage();
        String messageID = afterMessageRef.push().getKey();
        afterMessage.setMessageUser(new User(mCurrentUser.getUid(), mCurrentUser.getEmail(), mCurrentUser.getDisplayName()));
        afterMessage.setMessageDate(new Date());
        afterMessage.setMessageID(messageID);
        afterMessage.setChatID(mChatID);
        afterMessage.setAfterButton(true);
        afterMessage.setMessageText("(애프터 메세지)");
        afterMessageRef.child(messageID).setValue(afterMessage);

        mChatMemberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> chatMemberIterator = dataSnapshot.getChildren().iterator();
                while (chatMemberIterator.hasNext()) {
                    User charMember = chatMemberIterator.next().getValue(User.class);
                    mFirebaseDB.getReference("users")
                            .child(charMember.getUid())
                            .child("chats")
                            .child(mChatID)
                            .child("lastMessage")
                            .setValue(afterMessage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private Message message = new Message();

    private void sendMessage() {
        // 메세지 키 생성
        mChatMessageRef = mFirebaseDB.getReference("chat_messages").child(mChatID);
        // chat_message > {chat_id} > {message_id} > messageinfo
        String messageID = mChatMessageRef.push().getKey();
        String messageText = chat_message.getText().toString();

        if(messageType == Message.MessageType.TEXT) {
            if(messageText.isEmpty()) {
                return;
            }
            message = new TextMessage();
            ((TextMessage)message).setMessageText(messageText);
        }

        mChatMemberRef.child(mCurrentUser.getUid()).child("afterYes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue().equals(true)) {
                    message.setAfterYes(true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        message.setMessageDate(new Date());
        message.setChatID(mChatID);
        message.setMessageID(messageID);
        message.setMessageType(messageType);
        message.setMessageUser(new User(mCurrentUser.getUid(), mCurrentUser.getEmail(), mCurrentUser.getDisplayName()));
        message.setReadUserList(Arrays.asList(new String[]{mCurrentUser.getUid()})); // 자기 자신은 읽었으므로
        String [] uids = getIntent().getStringArrayExtra("uids");
        if(uids != null) {
            message.setUnreadCount(uids.length - 1);
        }
        chat_message.setText("");
        messageType = Message.MessageType.TEXT; // PHOTO 전송후 다시 기본 TEXT 로

        // 상대방 정보 가져오기
        mChatMemberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                // unreadCount 세팅하기 위한 상대방의 수를 가져옴.
                long memberCount = dataSnapshot.getChildrenCount();
                message.setUnreadCount((int) (memberCount - 1));

                mChatMessageRef.child(message.getMessageID()).setValue(message, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                        Iterator<DataSnapshot> memberIterator = dataSnapshot.getChildren().iterator();
                        while(memberIterator.hasNext()) {
                            // 대화 상대 한명한명 돌면서 정보를 읽는다
                            User chatMember = memberIterator.next().getValue(User.class);
                            mUserRef
                                    .child(chatMember.getUid())
                                    .child("chats")
                                    .child(mChatID)
                                    .child("lastMessage")
                                    .setValue(message);
                            if(!chatMember.getUid().equals(mCurrentUser.getUid())) {
                                mUserRef  // 공유되는 증가카운트는 transaction 을 이용하여 처리하자
                                        .child(chatMember.getUid())
                                        .child("chats")
                                        .child(mChatID)
                                        .child("totalUnreadCount")
                                        .runTransaction(new Transaction.Handler() {
                                            @NonNull
                                            @Override
                                            public Transaction.Result doTransaction(@NonNull final MutableData mutableData) {
                                                // 삼항연산자 : 값이 null 일때 true > 0 , false > mutableData.getValue(long.class) 를 넣어준다
                                                long totalUnreadCount = mutableData.getValue(long.class) == null ? 0 : mutableData.getValue(long.class);
                                                mutableData.setValue(totalUnreadCount + 1);

                                                return Transaction.success(mutableData);
                                            }

                                            @Override
                                            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

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
        mChatID = mChatRef.push().getKey();
        mChatRef = mChatRef.child(mChatID);
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
                                addChatListener();
                                // 한번 메세지 보내면 못보내도록 true 로 변경
                                addMessageListener();
                                isSentMessage = true;
                                ChatFragment.JOINED_ROOM = mChatID;
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
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fcmListener() {
        mChatMemberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot item : dataSnapshot.getChildren()) {
                    User userItem = item.getValue(User.class);
                    if(!userItem.getUid().equals(mCurrentUser.getUid())) {
                        final String token = userItem.getDeviceToken();
                        Log.d(TAG, token);
                        mUserRef.child(userItem.getUid()).child("status").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue().equals(false)) {
                                    Gson gson = new Gson();
                                    final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

                                    NotificationModel notificationModel = new NotificationModel();
                                    notificationModel.to = token;
                                    notificationModel.data.title = mCurrentUser.getDisplayName();
                                    notificationModel.data.text = "메시지가 도착했습니다";

                                    RequestBody body = RequestBody.create(mediaType, gson.toJson(notificationModel));
                                    Request request = new Request.Builder()
                                            .url("https://fcm.googleapis.com/fcm/send")
                                            .header("Content-Type", "application/json")
                                            .addHeader("Authorization", "key=AAAAW99exTw:APA91bFhQZjaCxnlkNrx4RgbP0YMbXyh-F-Va4y7mJp5lr8p17WVprO4gH53wF97aH_dYY_eK-m0qAC0s6dMYEjqnOghvaoqlq5kLnKacVliLNpvpGcDJ0CUbPfFEopRVErjt9UEZQdv")
                                            .post(body)
                                            .build();
                                    OkHttpClient okHttpClient = new OkHttpClient();
                                    okHttpClient.newCall(request).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {

                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

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
