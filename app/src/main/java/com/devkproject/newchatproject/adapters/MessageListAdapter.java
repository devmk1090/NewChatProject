package com.devkproject.newchatproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.devkproject.newchatproject.R;
import com.devkproject.newchatproject.model.Message;
import com.devkproject.newchatproject.model.PhotoMessage;
import com.devkproject.newchatproject.model.TextMessage;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder> {

    private ArrayList<Message> messageList;
    private String userID;
    private SimpleDateFormat messageDateFormat = new SimpleDateFormat("MM/dd a\n hh:mm");

    public MessageListAdapter() {
        messageList = new ArrayList<>();
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void addItem(Message item) {
        messageList.add(item);
        notifyDataSetChanged();
    }
    public void upadteItem (Message item) {
        int position = getItemPosition(item.getMessageID());
        if(position < 0) {
            return;
        }
        messageList.set(position, item);
        notifyItemChanged(position);
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
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        // 전달받은 뷰 홀더를 이용한 뷰 구현 (실제 뷰를 구현하는 부분)
        Message item = getItem(position);

        TextMessage textMessage = null;
        PhotoMessage photoMessage = null;

        if(item instanceof TextMessage) {
            textMessage = (TextMessage) item;
        }
        else if (item instanceof PhotoMessage) {
            photoMessage = (PhotoMessage) item;
        }
        // 내가 보낸 메세지인지, 받은 메세지인지 판별
        if(userID.equals(item.getMessageUser().getUid())) {
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
            if(item.getUnreadCount() > 0) {
                holder.sendUnreadCount.setText(String.valueOf(item.getUnreadCount()));
            } else {
                holder.sendUnreadCount.setText("");
            }
            holder.sendDate.setText(messageDateFormat.format(item.getMessageDate()));
            holder.yourArea.setVisibility(View.GONE);
            holder.sendArea.setVisibility(View.VISIBLE);
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
            holder.rcvDate.setText(messageDateFormat.format(item.getMessageDate()));
            holder.yourArea.setVisibility(View.VISIBLE);
            holder.sendArea.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout yourArea;
        private LinearLayout sendArea;
        private LinearLayout exitArea;
        private CircleImageView rcvProfileImage;
        private TextView  rcvTxt;
        private TextView  exitTxt;
        private ImageView rcvImage;
        private TextView rcvUnreadCount;
        private TextView rcvDate;
        private TextView sendUnreadCount;
        private TextView sendDate;
        private TextView sendTxt;
        private ImageView sendImage;

        public MessageViewHolder(@NonNull View v) {
            super(v);
            yourArea = (LinearLayout) v.findViewById(R.id.yourChatArea);
            sendArea = (LinearLayout) v.findViewById(R.id.myChatArea);
            exitArea = (LinearLayout) v.findViewById(R.id.exitArea);
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
        }
    }
}
