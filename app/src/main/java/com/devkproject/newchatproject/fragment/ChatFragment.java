package com.devkproject.newchatproject.fragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devkproject.newchatproject.ChatActivity;
import com.devkproject.newchatproject.R;
import com.devkproject.newchatproject.adapters.ChatListAdapter;
import com.devkproject.newchatproject.customviews.RecyclerViewItemClickListener;
import com.devkproject.newchatproject.model.Chat;
import com.devkproject.newchatproject.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class ChatFragment extends Fragment {

    private FirebaseUser mCurrentUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mChatRef;
    private DatabaseReference mChatMemberRef;
    private RecyclerView chatRecyclerView;
    private ChatListAdapter chatListAdapter;

    public ChatFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View chatView = inflater.inflate(R.layout.fragment_chat, container,false);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mChatRef = mFirebaseDatabase.getReference("users").child(mCurrentUser.getUid()).child("chats");
        mChatMemberRef = mFirebaseDatabase.getReference("chat_members");

        chatRecyclerView = (RecyclerView) chatView.findViewById(R.id.chat_room_recyclerView);
        chatListAdapter = new ChatListAdapter();
        chatRecyclerView.setAdapter(chatListAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getContext(), new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Chat chat = chatListAdapter.getItem(position);

                Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                chatIntent.putExtra("chat_id", chat.getChatID());
                startActivity(chatIntent);
            }
        }));

        addChatListener();

        return chatView;
    }

    private void addChatListener() {
        mChatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot chatDataSnapshot, @Nullable String s) {
                // 방에 대한 정보를 얻어오고
                // ui 갱신 시켜주는 메서드로 방의 정보를 전달
                final Chat chatRoom = chatDataSnapshot.getValue(Chat.class);
                mChatMemberRef.child(chatRoom.getChatID()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        long memberCount = dataSnapshot.getChildrenCount();
                        Iterator<DataSnapshot> memberIterator = dataSnapshot.getChildren().iterator();
                        StringBuffer memberStringBuffer = new StringBuffer();

                        int loopCount = 1;
                        while(memberIterator.hasNext()) {
                            User member = memberIterator.next().getValue(User.class);
                            if ( !mCurrentUser.getUid().equals(member.getUid())) {
                                memberStringBuffer.append(member.getUserNickname());
                                if ( memberCount - loopCount >= 1 ) {
                                    memberStringBuffer.append("  ");
                                }
                            }
                            if ( loopCount == memberCount ) {
                                // users/uid/chats/{chat_id}/title
                                String title = memberStringBuffer.toString();
                                if ( chatRoom.getTitle() == null ) {
                                    chatDataSnapshot.getRef().child("title").setValue(title);
                                } else if (!chatRoom.getTitle().equals(title)){
                                    chatDataSnapshot.getRef().child("title").setValue(title);
                                }
                                chatRoom.setTitle(title);
                                drawUI( chatRoom );
                            }
                            loopCount++;
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                // 기존의 방제목과 방 멤버의 이름들을 가져와서 타이틀화 시켰을때 같지 않을 경우 방제목을 업데이트 시켜준다.
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // 변경된 방의 정보를 수신
                // 내가 보낸 메세지가 아닐 경우와 마지막 메세지가 수정이 되었다면 -> 노티출력
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void drawUI(Chat chat) {
        chatListAdapter.addItem(chat);
    }

}
