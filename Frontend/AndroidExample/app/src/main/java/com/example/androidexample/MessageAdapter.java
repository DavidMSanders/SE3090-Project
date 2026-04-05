package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import androidx.appcompat.widget.PopupMenu;


public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnMessageMenuActionListener {
        void onDirectMessage(Message message);
        void onEditPermissions(Message message);
        void onEditMessage(Message message);
    }
    public static final int Type_Our_Message = 0;
    public static final int Type_Other_Message = 1;
    public static final int Type_Announcement_Message = 2;
    public static final int Type_Direct_Message = 3;
    public static final int Type_Trainer_Message = 4;

    private final List<Message> messageList;
    private final int currentUserId;

    //dir+perms edits
    private final boolean canManageMembers;
    private final OnMessageMenuActionListener menuActionListener;

    public MessageAdapter(List<Message> messageList,
                          int currentUserId,
                          boolean canManageMembers,
                          OnMessageMenuActionListener menuActionListener) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.canManageMembers = canManageMembers;
        this.menuActionListener = menuActionListener;
    }


    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);

        // Priority:
        // announcement > direct > trainer > our normal > other normal

        if ("ANNOUNCEMENT".equalsIgnoreCase(message.getMessageType())) {
            return Type_Announcement_Message;

        } else if ("DIRECT".equalsIgnoreCase(message.getMessageType())
                && (message.getOriginatorId() == currentUserId
                || (message.getTargetUserId() != null
                && message.getTargetUserId() == currentUserId))) {
            return Type_Direct_Message;

        } else if ("TRAINER".equalsIgnoreCase(message.getMessageType())) {
            return Type_Trainer_Message;

        } else if (message.getOriginatorId() == currentUserId) {
            return Type_Our_Message;

        } else {
            return Type_Other_Message;
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if(viewType == Type_Our_Message) {
            View view = inflater.inflate(R.layout.our_message_box, parent, false);
            return new OurMessageViewHolder(view);

        } else if(viewType == Type_Other_Message) {
            View view = inflater.inflate(R.layout.other_user_message_box, parent, false);
            return new OtherMessageViewHolder(view);

        } else if(viewType == Type_Announcement_Message) {
            View view = inflater.inflate(R.layout.announcement_message_box, parent, false);
            return new AnnouncementMessageViewHolder(view);
        }
        else if (viewType == Type_Direct_Message) {
            View view = inflater.inflate(R.layout.direct_message_box, parent, false);
            return new DirectMessageViewHolder(view);

        }
        else  { //trainer
            View view = inflater.inflate(R.layout.trainer_message_box, parent, false);
            return new TrainerMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message msg = messageList.get(position);

        if (holder instanceof OurMessageViewHolder) {
            OurMessageViewHolder h = (OurMessageViewHolder) holder;
            h.bind(msg);
            h.btnMessageMenu.setOnClickListener(v -> showMessageMenu(v, msg));

        } else if (holder instanceof OtherMessageViewHolder) {
            OtherMessageViewHolder h = (OtherMessageViewHolder) holder;
            h.bind(msg);
            h.btnMessageMenu.setOnClickListener(v -> showMessageMenu(v, msg));

        } else if (holder instanceof AnnouncementMessageViewHolder) {
            AnnouncementMessageViewHolder h = (AnnouncementMessageViewHolder) holder;
            h.bind(msg);
            h.btnMessageMenu.setOnClickListener(v -> showMessageMenu(v, msg));

        } else if (holder instanceof DirectMessageViewHolder) {
            DirectMessageViewHolder h = (DirectMessageViewHolder) holder;
            h.bind(msg);
            h.btnMessageMenu.setOnClickListener(v -> showMessageMenu(v, msg));

        } else if (holder instanceof TrainerMessageViewHolder) {
            TrainerMessageViewHolder h = (TrainerMessageViewHolder) holder;
            h.bind(msg);
            h.btnMessageMenu.setOnClickListener(v -> showMessageMenu(v, msg));
        }
    }

    public void addMessage(Message message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }

    public void updateMessage(Message messageUpdated) {
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).getMessageId() == messageUpdated.getMessageId()) {
                messageList.set(i, messageUpdated);
                notifyItemChanged(i);
                return;
            }
        }
    }

    public void removeMessageById(long messageId) {
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).getMessageId() == messageId) {
                messageList.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }

    static class OurMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUsername;
        private final TextView tvMessageText;
        private final TextView tvMessageTime;

        private final ImageButton btnMessageMenu;


        OurMessageViewHolder(View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            btnMessageMenu = itemView.findViewById(R.id.btnMessageMenu);
        }
        void bind(Message message) {
            tvUsername.setText(message.getUsername());
            tvMessageText.setText(message.getTextBody());
            tvMessageTime.setText(formatTime(message.getCreatedTimestamp()));

        }
    }

    static class OtherMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUsername;
        private final TextView tvMessageText;
        private final TextView tvMessageTime;

        private final ImageButton btnMessageMenu;

        OtherMessageViewHolder(View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            btnMessageMenu = itemView.findViewById(R.id.btnMessageMenu);
        }

        void bind(Message message) {
            tvUsername.setText(message.getUsername());
            tvMessageText.setText(message.getTextBody());
            tvMessageTime.setText(formatTime(message.getCreatedTimestamp()));

        }
    }
    static class AnnouncementMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAnnouncementHeader;
        private final TextView tvMessageText;
        private final TextView tvMessageTime;
        private final ImageButton btnMessageMenu;

        AnnouncementMessageViewHolder(View itemView) {
            super(itemView);
            tvAnnouncementHeader = itemView.findViewById(R.id.tvAnnouncementHeader);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            btnMessageMenu = itemView.findViewById(R.id.btnMessageMenu);
        }

        void bind(Message message) {
            String username = message.getUsername();
            tvAnnouncementHeader.setText("ANNOUNCEMENT - " + username);
            tvMessageText.setText(message.getTextBody());
            tvMessageTime.setText(formatTime(message.getCreatedTimestamp()));

        }
    }

    static class DirectMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUsername;
        private final TextView tvMessageText;
        private final TextView tvMessageTime;
        private final ImageButton btnMessageMenu;

        DirectMessageViewHolder(View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);

            btnMessageMenu = itemView.findViewById(R.id.btnMessageMenu);
        }

        void bind(Message message) {
            tvUsername.setText(message.getUsername());
            tvMessageText.setText(message.getTextBody());
            tvMessageTime.setText(formatTime(message.getCreatedTimestamp()));

        }
    }
    static class TrainerMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUsername;
        private final TextView tvMessageText;
        private final TextView tvMessageTime;

        private final ImageButton btnMessageMenu;

        TrainerMessageViewHolder(View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            btnMessageMenu = itemView.findViewById(R.id.btnMessageMenu);
        }

        void bind(Message message) {
            tvUsername.setText(message.getUsername());
            tvMessageText.setText(message.getTextBody());
            tvMessageTime.setText(formatTime(message.getCreatedTimestamp()));

        }
    }
    private static String formatTime(String rawTimestamp) {
        try {
            java.time.LocalDateTime dt = java.time.LocalDateTime.parse(rawTimestamp);
            java.time.format.DateTimeFormatter formatter =
                    java.time.format.DateTimeFormatter.ofPattern("h:mm a");
            return dt.format(formatter);
        } catch (Exception e) {
            return rawTimestamp == null ? "" : rawTimestamp;
        }
    }


    //popup menu helper:
    private void showMessageMenu(View anchor, Message message) {
        PopupMenu popupMenu = new PopupMenu(anchor.getContext(), anchor);

        boolean isOwnMessage = message.getOriginatorId() == currentUserId;

        // Direct Message -> only show on other people's messages
        if (!isOwnMessage) {
            popupMenu.getMenu().add(0, 1, 0, "Direct Message");
        }

        // Edit Permissions -> only show if current user can manage members
        // and only on other people's messages
        if (!isOwnMessage && canManageMembers) {
            popupMenu.getMenu().add(0, 2, 1, "Edit Permissions");
        }

        // Edit Message -> only show on your own messages
        if (isOwnMessage) {
            popupMenu.getMenu().add(0, 3, 2, "Edit Message");
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == 1) {
                if (menuActionListener != null) {
                    menuActionListener.onDirectMessage(message);
                }
                return true;
            } else if (itemId == 2) {
                if (menuActionListener != null) {
                    menuActionListener.onEditPermissions(message);
                }
                return true;
            } else if (itemId == 3) {
                if (menuActionListener != null) {
                    menuActionListener.onEditMessage(message);
                }
                return true;
            }

            return false;
        });

        popupMenu.show();
    }

}
