package com.devkproject.newchatproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devkproject.newchatproject.R;
import com.devkproject.newchatproject.fragment.ChatFragment;
import com.devkproject.newchatproject.model.Chat;
import com.devkproject.newchatproject.model.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatHolder> {

    private ArrayList<Chat> mChatList;
    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd\naa hh:mm");
    private ChatFragment mChatFragment;

    public ChatListAdapter() {
        mChatList = new ArrayList<>();
    }

    public void setFragment(ChatFragment chatFragment) {
        this.mChatFragment = chatFragment;
    }

    public void addItem(Chat chat) {
        mChatList.add(chat);
        notifyDataSetChanged();
    }

    public void removeItem(Chat chat) {
        int position = getItemPosition(chat.getChatID());
        if(position > -1) {
            mChatList.remove(position);
            notifyDataSetChanged();
        }
    }

    public void updateItem(Chat chat) {
        int changedItemPosition = getItemPosition(chat.getChatID());
        if(changedItemPosition > -1) {
            mChatList.set(changedItemPosition, chat);
            notifyItemChanged(changedItemPosition);
        }
    }

    public Chat getItem(int position) {
        return this.mChatList.get(position);
    }

    private int getItemPosition(String chatID){
        int position = 0;
        for(Chat currentItem : mChatList) {
            if(currentItem.getChatID().equals(chatID)) {
                return position;
            }
            position ++;
        }
        return -1;
    }

    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_chat_room_list, parent, false);
        return  new ChatHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHolder holder, int position) {
        final Chat item = getItem(position);
        if(item.getLastMessage() != null) {
            if(item.getLastMessage().getMessageType() == Message.MessageType.TEXT) {
                holder.lastMessage.setText(item.getLastMessage().getMessageText());
            } else if (item.getLastMessage().getMessageType() == Message.MessageType.EXIT) {
                holder.lastMessage.setText(item.getLastMessage().getMessageUser().getUserNickname() + "님이 나가셨습니다.");
            } else if (item.getLastMessage().getMessageType() == Message.MessageType.AFTER) {
                holder.lastMessage.setText("( 애프터 신청 메세지)");
            }
            holder.lastMsgDate.setText(sdf.format(item.getCreateDate()));
        }

        holder.title.setText(item.getTitle());
        holder.rootView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(mChatFragment != null) {
                    mChatFragment.leaveChat(item);
                }
                return true;
            }
        });
        if(item.getTotalUnreadCount() > 0) {
            holder.totalUnreadCounter.setText(String.valueOf(item.getTotalUnreadCount()));
            holder.totalUnreadCounter.setVisibility(View.VISIBLE);
        } else {
            holder.totalUnreadCounter.setVisibility(View.GONE);
            holder.totalUnreadCounter.setText("");

        }
    }

    @Override
    public int getItemCount() {
        return mChatList.size();
    }

    public static class ChatHolder extends RecyclerView.ViewHolder {

        private CircleImageView chat_image;
        private TextView title, lastMessage, totalUnreadCounter, lastMsgDate;
        private LinearLayout rootView;

        public ChatHolder(@NonNull View itemView) {
            super(itemView);
            chat_image = (CircleImageView) itemView.findViewById(R.id.chat_item_image);
            title = (TextView) itemView.findViewById(R.id.chat_item_title);
            lastMessage = (TextView) itemView.findViewById(R.id.chat_item_lastMessage);
            totalUnreadCounter = (TextView) itemView.findViewById(R.id.chat_item_totalUnreadCounter);
            lastMsgDate = (TextView) itemView.findViewById(R.id.chat_item_lastMsgDate);
            rootView = (LinearLayout) itemView.findViewById(R.id.rootView);
        }
    }
}
