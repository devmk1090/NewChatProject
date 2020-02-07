package com.devkproject.newchatproject.adapters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.devkproject.newchatproject.ChatActivity;
import com.devkproject.newchatproject.R;
import com.devkproject.newchatproject.model.AfterMessage;
import com.devkproject.newchatproject.model.Message;
import com.devkproject.newchatproject.model.PhotoMessage;
import com.devkproject.newchatproject.model.TextMessage;
import com.devkproject.newchatproject.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder> {

    private static final String TAG = "MessageListAdapter";
    private ArrayList<Message> messageList;
    private SimpleDateFormat messageDateFormat = new SimpleDateFormat("MM/dd a\n hh:mm");

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference chatRef;
    private DatabaseReference userChatRef;
    private DatabaseReference mChatMemberRef;
    private DatabaseReference userRef;

    public MessageListAdapter() {
        messageList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        chatRef = FirebaseDatabase.getInstance().getReference().child("chat_messages");
        userChatRef = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentUser.getUid()).child("chats");
        mChatMemberRef = FirebaseDatabase.getInstance().getReference("chat_members");
        userRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public void addItem(Message item) {
        messageList.add(item);
        notifyDataSetChanged();
    }
    public void updateItem (Message item) {
        int position = getItemPosition(item.getMessageID());
        if(position < 0) {
            return;
        }
        messageList.set(position, item);
        notifyItemChanged(position);
    }

    public void clearItem() {
        messageList.clear();
    }

    public int getItemPosition (String messageID) {
        int position = 0;
        for(Message message : messageList) {
            if(message.getMessageID().equals(messageID)) {
                return position;
            }
            position++;
        }
        return -1;
    }

    public Message getItem(int position) {
        return messageList.get(position);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 뷰를 이용한 뷰홀더 리턴
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_message_item, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        // 전달받은 뷰 홀더를 이용한 뷰 구현 (실제 뷰를 구현하는 부분)
        final Message item = getItem(position);

        TextMessage textMessage = null;
        PhotoMessage photoMessage = null;
        AfterMessage afterMessage = null;
        if(item instanceof TextMessage) {
            textMessage = (TextMessage) item;
        } else if (item instanceof PhotoMessage) {
            photoMessage = (PhotoMessage) item;
        } else if (item instanceof AfterMessage) {
            afterMessage = (AfterMessage) item;
        }
        // 내가 보낸 메세지인지, 받은 메세지인지 판별
        if(mCurrentUser.getUid().equals(item.getMessageUser().getUid())) {
            //내가 보낸 메세지
            if(item.getMessageType() == Message.MessageType.TEXT) {
                holder.sendTxt.setText(textMessage.getMessageText());
                holder.sendTxt.setVisibility(View.VISIBLE);
                holder.sendImage.setVisibility(View.GONE);
            }
            else if(item.getMessageType() == Message.MessageType.PHOTO) {
                Glide.with(holder.sendArea)
                        .load(photoMessage.getPhotoUrl())
                        .into(holder.sendImage);
                holder.sendTxt.setVisibility(View.GONE);
                holder.sendImage.setVisibility(View.VISIBLE);
            }
            else if(item.getMessageType() == Message.MessageType.AFTER) {
                holder.afterTxt.setText(item.getMessageUser().getUserNickname() + "님이 애프터 신청을 하셨습니다");
                holder.afterTxt.setVisibility(View.VISIBLE);
            }
            if(item.getUnreadCount() > 0) {
                holder.sendUnreadCount.setText(String.valueOf(item.getUnreadCount()));
            } else {
                holder.sendUnreadCount.setText("");
            }
            if(item.getMessageType() == Message.MessageType.AFTER) {
                holder.afterArea.setVisibility(View.VISIBLE);
                holder.yourArea.setVisibility(View.GONE);
                holder.sendArea.setVisibility(View.GONE);
                holder.exitArea.setVisibility(View.GONE);
            } else {
                holder.sendDate.setText(messageDateFormat.format(item.getMessageDate()));
                holder.yourArea.setVisibility(View.GONE);
                holder.sendArea.setVisibility(View.VISIBLE);
                holder.exitArea.setVisibility(View.GONE);
                holder.afterArea.setVisibility(View.GONE);
            }

        }
        else {
            // 상대방이 보낸 경우
            if (item.getMessageType() == Message.MessageType.TEXT) {
                holder.rcvTxt.setText(textMessage.getMessageText());
                holder.rcvTxt.setVisibility(View.VISIBLE);
                holder.rcvImage.setVisibility(View.GONE);
            } else if (item.getMessageType() == Message.MessageType.PHOTO) {
                Glide.with(holder.yourArea)
                        .load(photoMessage.getPhotoUrl())
                        .into(holder.rcvImage);
                holder.rcvTxt.setVisibility(View.GONE);
                holder.rcvImage.setVisibility(View.VISIBLE);
            } else if (item.getMessageType() == Message.MessageType.EXIT) {
                holder.exitTxt.setText(item.getMessageUser().getUserNickname() + "님이 방에서 나가셨습니다");
            } else if (item.getMessageType() == Message.MessageType.AFTER) {
                holder.afterTxt.setText(item.getMessageUser().getUserNickname() + "님이 애프터 신청을 하셨습니다");
                holder.afterTxt.setVisibility(View.VISIBLE);
                holder.afterYesButton.setVisibility(View.VISIBLE);
                holder.afterNoButton.setVisibility(View.VISIBLE);
                if(item.getMessageUser().isAfterCount() == false) {
                    holder.afterYesButton.setEnabled(true);
                    holder.afterNoButton.setEnabled(true);
                } else {
                    holder.afterYesButton.setEnabled(false);
                    holder.afterNoButton.setEnabled(false);
                }
                holder.afterYesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mChatMemberRef.child(item.getChatID()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot friendItem : dataSnapshot.getChildren()) {
                                    User friendUser = friendItem.getValue(User.class);
                                    if(friendUser.getUid().equals(mCurrentUser.getUid()) && friendUser.isAfterCount() == false) {
                                        mChatMemberRef.child(item.getChatID()).child(friendUser.getUid()).child("afterCount").setValue(true);
                                        return;
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        chatRef.child(item.getChatID()).child(item.getMessageID()).child("messageUser").child("afterCount").setValue(true);
                    }
                });
                holder.afterNoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
//                        mChatMemberRef.child(item.getChatID()).addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                for(DataSnapshot friendItem : dataSnapshot.getChildren()) {
//                                    User friendUser = friendItem.getValue(User.class);
//                                    mChatMemberRef.child(item.getChatID()).child(friendUser.getUid()).child("chatStop").setValue(true);
//                                }
//                            }
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                            }
//                        });

                        // 애프터 버튼 거절시 절차
                        // 1. user -> chats 삭제(실시간으로 방없어짐)
                        // 2. chat_messages 해당방의 메세지 모두 삭제
                        // 3. 상대방 -> chats 삭제
                        // 4. chat_members 삭제
                        // 5. 나와 친구의 친구 등록 삭제
                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setMessage("채팅에서 나가집니다")
                                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ((ChatActivity)v.getContext()).finish();
                                        userChatRef.child(item.getChatID()).removeValue(new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                chatRef.child(item.getChatID()).removeValue(new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                        userRef.child(item.getMessageUser().getUid()).child("chats").child(item.getChatID()).removeValue(new DatabaseReference.CompletionListener() {
                                                            @Override
                                                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                mChatMemberRef.child(item.getChatID()).removeValue(new DatabaseReference.CompletionListener() {
                                                                    @Override
                                                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                        userRef.child(mCurrentUser.getUid()).child("friends").child(item.getMessageUser().getUid()).removeValue(new DatabaseReference.CompletionListener() {

                                                                            @Override
                                                                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                                userRef.child(item.getMessageUser().getUid()).child("friends").child(mCurrentUser.getUid()).removeValue(new DatabaseReference.CompletionListener() {

                                                                                    @Override
                                                                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                        //  ((ChatActivity)v.getContext()).stopChat(item.getChatID());

                                    }
                                })
                                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }
                });
            }
            if(item.getUnreadCount() > 0) {
                holder.rcvUnreadCount.setText(String.valueOf(item.getUnreadCount()));
            } else {
                holder.rcvUnreadCount.setText("");
            }
            if(item.getMessageUser().getProfileImageUrl() != null) {
                Glide.with(holder.yourArea)
                        .load(item.getMessageUser().getProfileImageUrl())
                        .into(holder.rcvProfileImage);
            }
            if(item.getMessageType() == Message.MessageType.EXIT) {
                holder.exitArea.setVisibility(View.VISIBLE);
                holder.yourArea.setVisibility(View.GONE);
                holder.sendArea.setVisibility(View.GONE);
                holder.afterArea.setVisibility(View.GONE);
            } else {
                holder.rcvDate.setText(messageDateFormat.format(item.getMessageDate()));
                holder.yourArea.setVisibility(View.VISIBLE);
                holder.sendArea.setVisibility(View.GONE);
                holder.afterArea.setVisibility(View.GONE);
            }
            if(item.getMessageType() == Message.MessageType.AFTER){
                holder.afterArea.setVisibility(View.VISIBLE);
                holder.exitArea.setVisibility(View.GONE);
                holder.yourArea.setVisibility(View.GONE);
                holder.sendArea.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout yourArea, sendArea, exitArea;
        private RelativeLayout afterArea;
        private CircleImageView rcvProfileImage;
        private TextView  rcvTxt, exitTxt, rcvUnreadCount, rcvDate, sendUnreadCount, sendDate, sendTxt, afterTxt;
        private ImageView sendImage, rcvImage, sendAfterImage;
        private Button afterYesButton, afterNoButton;

        public MessageViewHolder(@NonNull View v) {
            super(v);
            yourArea = (LinearLayout) v.findViewById(R.id.yourChatArea);
            sendArea = (LinearLayout) v.findViewById(R.id.myChatArea);
            exitArea = (LinearLayout) v.findViewById(R.id.exitArea);
            afterArea = (RelativeLayout) v.findViewById(R.id.afterArea);
            rcvProfileImage = (CircleImageView) v.findViewById(R.id.rcvProfile);
            rcvTxt = (TextView) v.findViewById(R.id.rcvTxt);
            exitTxt = (TextView) v.findViewById(R.id.exitTxt);
            rcvImage = (ImageView) v.findViewById(R.id.rcvImage);
            rcvUnreadCount = (TextView) v.findViewById(R.id.rcvUnreadCount);
            rcvDate = (TextView) v.findViewById(R.id.rcvDate);
            sendUnreadCount = (TextView) v.findViewById(R.id.sendUnreadCount);
            sendDate = (TextView) v.findViewById(R.id.sendDate);
            sendTxt = (TextView) v.findViewById(R.id.sendTxt);
            sendImage = (ImageView) v.findViewById(R.id.sendImage);
            afterTxt = (TextView) v.findViewById(R.id.afterTxt);
            afterYesButton = (Button) v.findViewById(R.id.afterYesButton);
            afterNoButton = (Button) v.findViewById(R.id.afterNoButton);
            sendAfterImage = (ImageView) v.findViewById(R.id.sendAfterImage);
        }
    }
}
