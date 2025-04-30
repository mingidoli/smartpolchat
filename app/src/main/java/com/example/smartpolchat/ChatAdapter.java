package com.example.smartpolchat;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnImageRequestListener {
        void onImageRequested(String imageName);
    }

    private final List<ChatMessage> chatList;
    private final Context context;
    private final OnImageRequestListener listener;
    private final RecyclerView recyclerView; // 🔹 RecyclerView 참조 추가

    public ChatAdapter(Context context, List<ChatMessage> chatList, RecyclerView recyclerView, OnImageRequestListener listener) {
        this.context = context;
        this.chatList = chatList;
        this.listener = listener;
        this.recyclerView = recyclerView;
    }

    @Override
    public int getItemViewType(int position) {
        return chatList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ChatMessage.TYPE_USER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_user, parent, false);
            return new UserMessageViewHolder(view);
        } else if (viewType == ChatMessage.TYPE_IMAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_image, parent, false);
            return new ImageViewHolder(view);
        } else if (viewType == ChatMessage.TYPE_SLIDE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_slide_group, parent, false);
            return new SlideGroupViewHolder(view);
        } else if (viewType == ChatMessage.TYPE_NOTICE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_notice, parent, false);
            return new NoticeViewHolder(view);  // ✅ 공지 ViewHolder
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
            return new BotMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessage = chatList.get(position);

        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).textMessageUser.setText(chatMessage.getMessage());
            ((UserMessageViewHolder) holder).textTimeUser.setText(chatMessage.getTime());

        } else if (holder instanceof BotMessageViewHolder) {
            BotMessageViewHolder botHolder = (BotMessageViewHolder) holder;
            botHolder.textMessageBot.setText(chatMessage.getMessage());
            botHolder.textTimeBot.setText(chatMessage.getTime());

            botHolder.gptAvatar.setVisibility(View.VISIBLE);
            botHolder.gptAvatar.setImageResource(R.drawable.gpt_bot);

            botHolder.buttonContainer.removeAllViews();
            List<ButtonEntry> buttons = chatMessage.getButtons();
            if (buttons != null && !buttons.isEmpty()) {
                for (ButtonEntry b : buttons) {
                    Button btn = new Button(context);
                    btn.setText(b.label);
                    btn.setTextSize(14f);
                    btn.setAllCaps(false);
                    btn.setBackgroundTintList(context.getColorStateList(R.color.teal_700));
                    btn.setTextColor(context.getColor(R.color.white));
                    btn.setOnClickListener(v -> {
                        if (listener != null) listener.onImageRequested(b.image);
                    });
                    botHolder.buttonContainer.addView(btn);
                }
            }

        } else if (holder instanceof ImageViewHolder) {
            ImageViewHolder imgHolder = (ImageViewHolder) holder;
            String imageName = chatMessage.getImageName();
            if (imageName != null) {
                int resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
                imgHolder.imageView.setImageResource(resId);
                imgHolder.imageView.setOnClickListener(v -> {
                    Intent intent = new Intent(context, ImageZoomActivity.class);
                    intent.putExtra("imageName", imageName);
                    context.startActivity(intent);
                });
            }
        }

        else if (holder instanceof NoticeViewHolder) {
            ((NoticeViewHolder) holder).noticeText.setText(chatMessage.getMessage());
        }

        else if (holder instanceof SlideGroupViewHolder) {
            SlideGroupViewHolder slideHolder = (SlideGroupViewHolder) holder;
            List<SlideEntry> slides = chatMessage.getSlides();

            if (slideHolder.gptAvatar != null) {
                slideHolder.gptAvatar.setVisibility(View.VISIBLE);
                slideHolder.gptAvatar.setImageResource(R.drawable.gpt_bot);
            }

            if (slides != null && !slides.isEmpty()) {
                SlideAdapter adapter = new SlideAdapter(context, slides, listener);
                slideHolder.slideViewPager.setAdapter(adapter);
                slideHolder.slideViewPager.setOffscreenPageLimit(slides.size()); // 🔹 핵심

                slideHolder.slideViewPager.postDelayed(() -> {
                    View firstSlide = slideHolder.slideViewPager.findViewWithTag("slide_0");
                    if (firstSlide != null) {
                        firstSlide.measure(
                                View.MeasureSpec.makeMeasureSpec(slideHolder.slideViewPager.getWidth(), View.MeasureSpec.EXACTLY),
                                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                        );
                        int height = firstSlide.getMeasuredHeight();
                        ViewGroup.LayoutParams layoutParams = slideHolder.slideViewPager.getLayoutParams();
                        layoutParams.height = height;
                        slideHolder.slideViewPager.setLayoutParams(layoutParams);

                        // 🔹 스크롤 자동 이동
                        recyclerView.smoothScrollToPosition(holder.getAdapterPosition());
                    }
                }, 50);

                slideHolder.indicatorContainer.removeAllViews();
                int slideCount = slides.size();
                ImageView[] dots = new ImageView[slideCount];

                for (int i = 0; i < slideCount; i++) {
                    dots[i] = new ImageView(context);
                    dots[i].setImageResource(i == 0 ? R.drawable.active_dot : R.drawable.inactive_dot);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(30, 30);
                    params.setMargins(12, 0, 12, 0);
                    dots[i].setLayoutParams(params);
                    slideHolder.indicatorContainer.addView(dots[i]);
                }

                slideHolder.slideViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int pos) {
                        for (int i = 0; i < dots.length; i++) {
                            dots[i].setImageResource(i == pos ? R.drawable.active_dot : R.drawable.inactive_dot);
                        }

                        slideHolder.slideViewPager.postDelayed(() -> {
                            View view = slideHolder.slideViewPager.findViewWithTag("slide_" + pos);
                            if (view != null) {
                                view.measure(
                                        View.MeasureSpec.makeMeasureSpec(slideHolder.slideViewPager.getWidth(), View.MeasureSpec.EXACTLY),
                                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                                );
                                int height = view.getMeasuredHeight();
                                ViewGroup.LayoutParams layoutParams = slideHolder.slideViewPager.getLayoutParams();
                                layoutParams.height = height;
                                slideHolder.slideViewPager.setLayoutParams(layoutParams);

                                // 🔹 스크롤 자동 이동
                                recyclerView.smoothScrollToPosition(holder.getAdapterPosition());
                            }
                        }, 50);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textMessageUser, textTimeUser;
        public UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessageUser = itemView.findViewById(R.id.text_message_user);
            textTimeUser = itemView.findViewById(R.id.text_time_user);
        }
    }

    static class BotMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textMessageBot, textTimeBot;
        LinearLayout buttonContainer;
        ImageView gptAvatar;

        public BotMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessageBot = itemView.findViewById(R.id.text_message_bot);
            textTimeBot = itemView.findViewById(R.id.text_time_bot);
            buttonContainer = itemView.findViewById(R.id.button_container_bot);
            gptAvatar = itemView.findViewById(R.id.gpt_avatar);
        }
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
        }
    }

    static class SlideGroupViewHolder extends RecyclerView.ViewHolder {
        ViewPager2 slideViewPager;
        LinearLayout indicatorContainer;
        ImageView gptAvatar;

        public SlideGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            slideViewPager = itemView.findViewById(R.id.slide_view_pager);
            indicatorContainer = itemView.findViewById(R.id.indicator_container);
            gptAvatar = itemView.findViewById(R.id.gpt_avatar);
        }
    }

    static class NoticeViewHolder extends RecyclerView.ViewHolder {
        TextView noticeText;

        public NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            noticeText = itemView.findViewById(R.id.notice_text);
        }
    }
}
