package com.example.smartpolchat;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import java.text.SimpleDateFormat;
import java.util.*;

import me.relex.circleindicator.CircleIndicator3;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatList;
    private final Context context;
    private final RecyclerView recyclerView;

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
        LinearLayout buttonContainer; // ✅ 버튼 컨테이너 추가

        public TextViewHolder(View view) {
            super(view);
            leftBubble = view.findViewById(R.id.left_bubble);
            rightBubble = view.findViewById(R.id.right_bubble);
            textMessageLeft = view.findViewById(R.id.text_message);
            textTimeLeft = view.findViewById(R.id.text_time);
            textMessageRight = view.findViewById(R.id.text_message_right);
            textTimeRight = view.findViewById(R.id.text_time_right);
            buttonContainer = view.findViewById(R.id.button_container); // ✅ 버튼 컨테이너 찾기
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

    public static class SlideViewHolder extends RecyclerView.ViewHolder {
        ViewPager2 viewPager;
        CircleIndicator3 indicator;

        public SlideViewHolder(View view) {
            super(view);
            viewPager = view.findViewById(R.id.slide_viewpager);
            indicator = view.findViewById(R.id.indicator);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == ChatMessage.TYPE_IMAGE) {
            return new ImageViewHolder(inflater.inflate(R.layout.item_chat_image, parent, false));
        } else if (viewType == ChatMessage.TYPE_SLIDE) {
            return new SlideViewHolder(inflater.inflate(R.layout.item_chat_slide, parent, false));
        } else {
            return new TextViewHolder(inflater.inflate(R.layout.item_chat, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        ChatMessage chat = chatList.get(position);

        if (holder instanceof TextViewHolder) {
            TextViewHolder h = (TextViewHolder) holder;

            if (chat.type == ChatMessage.TYPE_USER) {
                h.leftBubble.setVisibility(View.GONE);
                h.rightBubble.setVisibility(View.VISIBLE);
                h.textMessageRight.setText(chat.message != null ? chat.message.replace("\\n", "\n") : "");
                h.textTimeRight.setText(chat.time);
            } else {
                h.rightBubble.setVisibility(View.GONE);
                h.leftBubble.setVisibility(View.VISIBLE);
                h.textMessageLeft.setText(chat.message != null ? chat.message.replace("\\n", "\n") : "");
                h.textTimeLeft.setText(chat.time);

                // ✅ 버튼 처리
                h.buttonContainer.removeAllViews();
                if (chat.buttons != null && !chat.buttons.isEmpty()) {
                    h.buttonContainer.setVisibility(View.VISIBLE);
                    for (RuleEntry.ButtonEntry btn : chat.buttons) {
                        Button dynamicBtn = new Button(context);
                        dynamicBtn.setText(btn.label);
                        dynamicBtn.setBackgroundResource(R.drawable.rounded_button);

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,   // ✅ 가로 길이를 텍스트 크기 기반으로
                                dpToPx(48)
                        );
                        params.setMargins(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8)); // ✅ 좌우 여백 주기
                        dynamicBtn.setLayoutParams(params);
                        dynamicBtn.setAllCaps(false);
                        dynamicBtn.setTextSize(14);
                        dynamicBtn.setGravity(Gravity.CENTER);
                        dynamicBtn.setSingleLine(true);
                        dynamicBtn.setEllipsize(TextUtils.TruncateAt.END);

                        dynamicBtn.setPadding(dpToPx(24), dpToPx(0), dpToPx(24), dpToPx(0));

                        dynamicBtn.setOnClickListener(v -> {
                            int resId = context.getResources().getIdentifier(btn.image, "drawable", context.getPackageName());
                            if (resId != 0) {
                                ChatMessage imgMsg = new ChatMessage(ChatMessage.TYPE_IMAGE, null, getCurrentTime(), resId);
                                chatList.add(imgMsg);
                                notifyItemInserted(chatList.size() - 1);
                                recyclerView.smoothScrollToPosition(chatList.size() - 1);
                            }
                        });

                        h.buttonContainer.addView(dynamicBtn);
                    }
                } else {
                    h.buttonContainer.setVisibility(View.GONE);
                }
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

        } else if (holder instanceof SlideViewHolder) {
            SlideViewHolder h = (SlideViewHolder) holder;

            ViewPager2 viewPager = h.viewPager;
            viewPager.setClipToPadding(false);
            viewPager.setClipChildren(false);
            viewPager.setOffscreenPageLimit(3);

            RecyclerView innerPagerRecycler = (RecyclerView) viewPager.getChildAt(0);
            innerPagerRecycler.setClipToPadding(false);
            innerPagerRecycler.setPadding(40, 0, 40, 0);

            SlideAdapter adapter = new SlideAdapter(chat.slides, context);
            h.viewPager.setAdapter(adapter);

            if (h.indicator != null) {
                h.indicator.setViewPager(viewPager);
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

    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
