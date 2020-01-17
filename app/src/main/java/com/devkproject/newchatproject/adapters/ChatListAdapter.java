package com.devkproject.newchatproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devkproject.newchatproject.R;
import com.devkproject.newchatproject.model.Chat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatHolder> {

    private ArrayList<Chat> mChatList;
    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd\naa hh:mm");

    public ChatListAdapter() {
        mChatList = new ArrayList<>();
    }

    public void addItem(Chat chat) {
        mChatList.add(chat);
        notifyDataSetChanged();
    }

    public Chat getItem(int position) {
        return this.mChatList.get(position);
    }

    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_chat_item, parent, false);
        return  new ChatHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHolder holder, int position) {
        Chat item = getItem(position);
        //holder.chat_image
        holder.lastMessage.setText(item.getLastMessage().getMessageText());
        holder.title.setText(item.getTitle());
        holder.lastMsgDate.setText(sdf.format(item.getCreateDate()));
        if(item.getTotalUnreadCount() > 0) {
            holder.totalUnreadCounter.setText(String.valueOf(item.getTotalUnreadCount()));
        }
    }

    @Override
    public int getItemCount() {
        return mChatList.size();
    }

    public static class ChatHolder extends RecyclerView.ViewHolder {

        private CircleImageView chat_image;
        private TextView title, lastMessage, totalUnreadCounter, lastMsgDate;


        public ChatHolder(@NonNull View itemView) {
            super(itemView);
            chat_image = (CircleImageView) itemView.findViewById(R.id.chat_item_image);
            title = (TextView) itemView.findViewById(R.id.chat_item_title);
            lastMessage = (TextView) itemView.findViewById(R.id.chat_item_lastMessage);
            totalUnreadCounter = (TextView) itemView.findViewById(R.id.chat_item_totalUnreadCounter);
            lastMsgDate = (TextView) itemView.findViewById(R.id.chat_item_lastMsgDate);
        }
    }
}
