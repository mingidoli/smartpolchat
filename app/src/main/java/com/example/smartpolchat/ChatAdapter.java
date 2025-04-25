package com.example.smartpolchat;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatList;
    private final Context context;
    private final RecyclerView recyclerView;  // 🔥 추가

    public ChatAdapter(List<ChatMessage> chatList, Context context, RecyclerView recyclerView) {
        this.chatList = chatList;
        this.context = context;
        this.recyclerView = recyclerView;
    }

    @Override
    public int getItemViewType(int position) {
        return chatList.get(position).type;
    }

    public static class TextViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftBubble, rightBubble;
        TextView textMessageLeft, textTimeLeft;
        TextView textMessageRight, textTimeRight;

        public TextViewHolder(View view) {
            super(view);
            leftBubble = view.findViewById(R.id.left_bubble);
            rightBubble = view.findViewById(R.id.right_bubble);
            textMessageLeft = view.findViewById(R.id.text_message);
            textTimeLeft = view.findViewById(R.id.text_time);
            textMessageRight = view.findViewById(R.id.text_message_right);
            textTimeRight = view.findViewById(R.id.text_time_right);
        }
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView imageTime;

        public ImageViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.image_chat);
            imageTime = view.findViewById(R.id.image_time);
        }
    }

    public static class ButtonViewHolder extends RecyclerView.ViewHolder {
        LinearLayout buttonContainer;

        public ButtonViewHolder(View view) {
            super(view);
            buttonContainer = view.findViewById(R.id.button_container);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == ChatMessage.TYPE_IMAGE) {
            return new ImageViewHolder(inflater.inflate(R.layout.item_chat_image, parent, false));
        } else if (viewType == ChatMessage.TYPE_BUTTON) {
            return new ButtonViewHolder(inflater.inflate(R.layout.item_chat_button, parent, false));
        } else {
            return new TextViewHolder(inflater.inflate(R.layout.item_chat, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatMessage chat = chatList.get(position);

        if (holder instanceof TextViewHolder) {
            TextViewHolder h = (TextViewHolder) holder;

            if (chat.type == ChatMessage.TYPE_USER) {
                h.leftBubble.setVisibility(View.GONE);
                h.rightBubble.setVisibility(View.VISIBLE);
                h.textMessageRight.setText(chat.message.replace("\\n", "\n"));
                h.textTimeRight.setText(chat.time);
            } else {
                h.rightBubble.setVisibility(View.GONE);
                h.leftBubble.setVisibility(View.VISIBLE);
                h.textMessageLeft.setText(chat.message.replace("\\n", "\n"));
                h.textTimeLeft.setText(chat.time);
            }

        } else if (holder instanceof ImageViewHolder) {
            ImageViewHolder h = (ImageViewHolder) holder;
            h.imageView.setImageResource(chat.imageResId);
            h.imageTime.setText(chat.time);

            h.imageView.setOnClickListener(v -> {
                Intent intent = new Intent(context, FullScreenImageActivity.class);
                intent.putExtra("imageResId", chat.imageResId);
                context.startActivity(intent);
            });

        } else if (holder instanceof ButtonViewHolder) {
            ButtonViewHolder h = (ButtonViewHolder) holder;
            h.buttonContainer.removeAllViews();

            for (RuleEntry.ButtonEntry btn : chat.buttons) {
                Button dynamicBtn = new Button(context);
                dynamicBtn.setText(btn.label);

                dynamicBtn.setOnClickListener(v -> {
                    int resId = context.getResources().getIdentifier(btn.image, "drawable", context.getPackageName());
                    if (resId != 0) {
                        ChatMessage imgMsg = new ChatMessage(ChatMessage.TYPE_IMAGE, null, getCurrentTime(), resId);
                        chatList.add(imgMsg);
                        notifyItemInserted(chatList.size() - 1);
                        recyclerView.smoothScrollToPosition(chatList.size() - 1); // 🔥 부드러운 자동 스크롤
                    } else {
                        Toast.makeText(context, "이미지 '" + btn.image + "' 를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });

                h.buttonContainer.addView(dynamicBtn);
            }
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("a h:mm", Locale.getDefault());
        return sdf.format(new Date());
    }
}

