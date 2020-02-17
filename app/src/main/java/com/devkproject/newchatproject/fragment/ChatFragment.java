package com.devkproject.newchatproject.fragment;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devkproject.newchatproject.ChatActivity;
import com.devkproject.newchatproject.MainActivity;
import com.devkproject.newchatproject.R;
import com.devkproject.newchatproject.adapters.ChatListAdapter;
import com.devkproject.newchatproject.customviews.RecyclerViewItemClickListener;
import com.devkproject.newchatproject.model.Chat;
import com.devkproject.newchatproject.model.ExitMessage;
import com.devkproject.newchatproject.model.Message;
import com.devkproject.newchatproject.model.Notification;
import com.devkproject.newchatproject.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.Iterator;

public class ChatFragment extends Fragment {

    private static final String TAG = "ChatFragment";
    private FirebaseUser mCurrentUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mChatRef;
    private DatabaseReference mChatMemberRef;
    private RecyclerView chatRecyclerView;
    private ChatListAdapter chatListAdapter;
    public static String JOINED_ROOM = "";
    public static final int JOIN_ROOM_REQUEST_CODE = 100;
    private Context mContext;
    private Notification mNotification;
    private DatabaseReference mChatMessageRef;

    public ChatFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View chatView = inflater.inflate(R.layout.fragment_chat, container, false);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mChatRef = mFirebaseDatabase.getReference("users").child(mCurrentUser.getUid()).child("chats");
        mChatMemberRef = mFirebaseDatabase.getReference("chat_members");
        mChatMessageRef = mFirebaseDatabase.getReference("chat_messages");

        chatRecyclerView = (RecyclerView) chatView.findViewById(R.id.chat_room_recyclerView);
        chatListAdapter = new ChatListAdapter();
        chatListAdapter.setFragment(this);
        chatRecyclerView.setAdapter(chatListAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getContext(), new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Chat chat = chatListAdapter.getItem(position);
                Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                chatIntent.putExtra("chat_id", chat.getChatID());
                startActivityForResult(chatIntent, JOIN_ROOM_REQUEST_CODE);
            }
        }));
        mContext = getActivity();
        mNotification = new Notification(mContext);

        addChatListener();
        return chatView;
    }

    private void addChatListener() {
        mChatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot chatDataSnapshot, @Nullable String s) {
                drawUI(chatDataSnapshot, DrawType.ADD);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot chatDataSnapshot, @Nullable String s) {
                // 변경된 방의 정보를 실시간 수신하는 코드
                // 내가 보낸 메세지가 아닐 경우와 마지막 메세지가 수정이 되었다면 -> 노티출력
                // 현재 액티비티가 ChatActivity 이고 chat_id 가 같다면 노티x

                drawUI(chatDataSnapshot, DrawType.UPDATE);
                Chat updatedChat = chatDataSnapshot.getValue(Chat.class);

                // totalUnread 의 변경, title 의 변경, lastMessage 변경시에 호출됨
                if (updatedChat.getLastMessage() != null) {
                    if(updatedChat.getLastMessage().getMessageType() == Message.MessageType.EXIT) {
                        return;
                    }
                    if (!updatedChat.getLastMessage().getMessageUser().getUid().equals(mCurrentUser.getUid())) {
                        if (!updatedChat.getChatID().equals(JOINED_ROOM)) {
                            // notification code
                            Intent chatIntent = new Intent (mContext, ChatActivity.class);
                            chatIntent.putExtra("chat_id", updatedChat.getChatID());
                            mNotification
                                    .setData(chatIntent)
                                    .setTitle(updatedChat.getLastMessage().getMessageUser().getUserNickname())
                                    .setText(updatedChat.getLastMessage().getMessageText())
                                    .notification();
                        }
                    }
                }
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // 방의 실시간 삭제
                Chat item = dataSnapshot.getValue(Chat.class);
                chatListAdapter.removeItem(item);
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void drawUI(final DataSnapshot chatDataSnapshot, final DrawType drawType) {

        final Chat chatRoom = chatDataSnapshot.getValue(Chat.class);
        mChatMemberRef.child(chatRoom.getChatID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long memberCount = dataSnapshot.getChildrenCount();
                Iterator<DataSnapshot> memberIterator = dataSnapshot.getChildren().iterator();
                StringBuffer memberStringBuffer = new StringBuffer();

                // 방에 한명밖에 없는 경우 방을 사용하지 못하게 처리
                if(memberCount <= 1){
                    chatRoom.setTitle("대화상대가 없는 방입니다");
                    chatDataSnapshot.getRef().child("title").setValue(chatRoom.getTitle());
                    chatDataSnapshot.getRef().child("disabled").setValue(true);
                    if(drawType == DrawType.ADD) {
                        chatListAdapter.addItem(chatRoom);
                    } else {
                        chatListAdapter.updateItem(chatRoom);
                    }
                    return;
                }

                // 멤버에 따라서 타이틀 변경
                int loopCount = 1;
                while(memberIterator.hasNext()) {
                    User member = memberIterator.next().getValue(User.class);
                    if ( !mCurrentUser.getUid().equals(member.getUid())) {
                        memberStringBuffer.append(member.getUserNickname());
                        if ( memberCount - loopCount > 1 ) {
                            memberStringBuffer.append(", ");
                        }
                    }
                    if ( loopCount == memberCount ) {
                        // users/uid/chats/{chat_id}/title
                        String title = memberStringBuffer.toString();
                        if (chatRoom.getTitle() == null ) {
                            chatDataSnapshot.getRef().child("title").setValue(title);
                        } else if (!chatRoom.getTitle().equals(title)){
                            chatDataSnapshot.getRef().child("title").setValue(title);
                        }
                        chatRoom.setTitle(title);
                        if(drawType == DrawType.ADD) {
                            chatListAdapter.addItem(chatRoom);
                        } else {
                            chatListAdapter.updateItem(chatRoom);
                        }
                    }
                    loopCount++;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void leaveChat(final Chat chat) {
        final DatabaseReference messageRef = mFirebaseDatabase.getReference("chat_messages").child(chat.getChatID());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), 3);
        builder.setTitle("선택된 대화방을 나가시겠습니까?")
                .setIcon(R.drawable.ic_warning_black_24dp)
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // 대화방의 마지막 사람이 나가면 챗멤버 삭제
                        mChatMemberRef.child(chat.getChatID()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                int i = (int) dataSnapshot.getChildrenCount();
                                if (i == 1) {
                                    Log.d(TAG, String.valueOf(i));
                                    mChatMessageRef.child(chat.getChatID()).removeValue(new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                        }
                                    });
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                        });
                        // users > {uid} > chats 나의 대화방 목록 제거
                        mChatRef.child(chat.getChatID()).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                // chat_members > {chat_id} > {user_id} 채팅 멤버 목록에서 제거


                                //  (나가기 메세지) chat_messages > {chat_id} > {message_id} > {exit 메세지} 발송

                                final ExitMessage exitMessage = new ExitMessage();
                                final String messageID = messageRef.push().getKey();

                                exitMessage.setMessageUser(new User(mCurrentUser.getUid(), mCurrentUser.getEmail(), mCurrentUser.getDisplayName()));
                                exitMessage.setMessageDate(new Date());
                                exitMessage.setMessageID(messageID);
                                exitMessage.setChatID(chat.getChatID());
                                mChatMemberRef.child(chat.getChatID()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        int i = (int) dataSnapshot.getChildrenCount();
                                        if(i > 1) {
                                            messageRef.child(messageID).setValue(exitMessage);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                mChatMemberRef
                                        .child(chat.getChatID())
                                        .child(mCurrentUser.getUid())
                                        .removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        // messages > {chat_id} unreadCount 에서 제거
                                        // getReadUserList 내가 있다면 읽은거니 pass, 없다면 unreadCount -1

                                        // (나가기 메세지) 채팅방의 멤버정보, 방정보를 각각 가져오고 라스트 메세지 수정
                                        mChatMemberRef
                                                .child(chat.getChatID())
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                Iterator<DataSnapshot> chatMemberIterator = dataSnapshot.getChildren().iterator();
                                                while(chatMemberIterator.hasNext()) {
                                                    User chatMember = chatMemberIterator.next().getValue(User.class);
                                                    mFirebaseDatabase
                                                            .getReference("users")
                                                            .child(chatMember.getUid())
                                                            .child("chats")
                                                            .child(chat.getChatID())
                                                            .child("lastMessage")
                                                            .setValue(exitMessage);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                        mFirebaseDatabase.getReference("messages").child(chat.getChatID()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                Iterator<DataSnapshot> messageIterator = dataSnapshot.getChildren().iterator();

                                                while(messageIterator.hasNext()) {
                                                    DataSnapshot messageSnapshot = messageIterator.next();
                                                    Message currentMessage = messageSnapshot.getValue(Message.class);
                                                    if(!currentMessage.getReadUserList().contains(mCurrentUser.getUid())) {
                                                        // 현재 위치 messages > {chat_id}
                                                        messageSnapshot.child("unreadCount").getRef().setValue(currentMessage.getUnreadCount() - 1);
                                                    }
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                                        });
                                    }
                                });

                                // 대화방의 타이틀이 변경된 것을 리스너가 감지하여 방 이름을 업데이트 해야함.
                                mChatMemberRef.child(chat.getChatID()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Iterator<DataSnapshot> memberIterator = dataSnapshot.getChildren().iterator();

                                        while(memberIterator.hasNext()) {
                                            // 방 참여자의 uid 정보를 가져오기 위해 정보 조회
                                            User chatMember = memberIterator.next().getValue(User.class);
                                            // 해당 참여자의 방 정보 업데이트를 위하여 방이름을 임의로 업데이트 진행
                                            mFirebaseDatabase.getReference("users")
                                                    .child(chatMember.getUid())
                                                    .child("chats")
                                                    .child(chat.getChatID())
                                                    .child("title")
                                                    .setValue("");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        });
                    }
                }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == JOIN_ROOM_REQUEST_CODE) {
            JOINED_ROOM = "";
        }
    }

    private enum DrawType {
        ADD, UPDATE
    }
}
