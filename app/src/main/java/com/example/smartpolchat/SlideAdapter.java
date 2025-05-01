package com.example.smartpolchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SlideAdapter extends RecyclerView.Adapter<SlideAdapter.SlideViewHolder> {

    private final Context context;
    private final List<SlideEntry> slideList;
    private final ChatAdapter.OnImageRequestListener listener;

    public SlideAdapter(Context context, List<SlideEntry> slideList, ChatAdapter.OnImageRequestListener listener) {
        this.context = context;
        this.slideList = slideList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_slide, parent, false);
        return new SlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        SlideEntry slide = slideList.get(position);
        holder.itemView.setTag("slide_" + position); // 높이 재측정용 태그

        // 🔹 따닥 출력 없이 전체 텍스트 즉시 출력
        holder.textView.setText(slide.text != null ? slide.text : "");

        holder.buttonContainer.removeAllViews();
        if (slide.buttons != null) {
            for (ButtonEntry b : slide.buttons) {
                Button btn = new Button(context);
                btn.setText(b.label);
                btn.setTextSize(14f);
                btn.setAllCaps(false);
                btn.setBackgroundTintList(context.getColorStateList(R.color.teal_700));
                btn.setTextColor(context.getColor(R.color.white));
                btn.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onImageRequested(b.image);
                    }
                });
                holder.buttonContainer.addView(btn);
            }
        }
    }

    @Override
    public int getItemCount() {
        return slideList.size();
    }

    static class SlideViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        LinearLayout buttonContainer;

        public SlideViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.slide_text);
            buttonContainer = itemView.findViewById(R.id.slide_button_container);
        }
    }
}
