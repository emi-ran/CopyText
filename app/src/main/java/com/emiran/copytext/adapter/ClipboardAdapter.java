package com.emiran.copytext.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emiran.copytext.R;
import com.emiran.copytext.model.ClipboardItem;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClipboardAdapter extends RecyclerView.Adapter<ClipboardAdapter.ViewHolder> {
    
    private List<ClipboardItem> items = new ArrayList<>();
    private OnItemClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("tr"));
    
    public interface OnItemClickListener {
        void onCopyClick(ClipboardItem item);
        void onDeleteClick(ClipboardItem item);
        void onPinClick(ClipboardItem item);
        void onItemChecked(ClipboardItem item, boolean isChecked);
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    
    public void setItems(List<ClipboardItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_clipboard, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClipboardItem item = items.get(position);
        holder.bind(item);
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView titleText;
        private TextView textContent;
        private TextView timestampText;
        private MaterialButton copyButton;
        private MaterialButton deleteButton;
        private MaterialButton pinButton;
        private CheckBox checkBox;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            textContent = itemView.findViewById(R.id.textContent);
            timestampText = itemView.findViewById(R.id.timestampText);
            copyButton = itemView.findViewById(R.id.copyButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            pinButton = itemView.findViewById(R.id.pinButton);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
        
        public void bind(ClipboardItem item) {
            // Başlık varsa göster, yoksa gizle
            if (item.getTitle() != null && !item.getTitle().trim().isEmpty()) {
                titleText.setVisibility(View.VISIBLE);
                titleText.setText(item.getTitle());
            } else {
                titleText.setVisibility(View.GONE);
            }
            
            textContent.setText(item.getText());
            timestampText.setText(formatTimestamp(item.getTimestamp()));
            
            // Pin butonunun ikonunu ve rengini ayarla
            if (item.isPinned()) {
                pinButton.setIconResource(R.drawable.ic_pin);
                pinButton.setIconTint(android.content.res.ColorStateList.valueOf(itemView.getContext().getColor(R.color.primary)));
            } else {
                pinButton.setIconResource(R.drawable.ic_unpin);
                pinButton.setIconTint(android.content.res.ColorStateList.valueOf(itemView.getContext().getColor(R.color.secondary)));
            }
            
            copyButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCopyClick(item);
                }
            });
            
            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(item);
                }
            });
            
            pinButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPinClick(item);
                }
            });
            
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onItemChecked(item, isChecked);
                }
            });
        }
        
        private String formatTimestamp(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;
            
            if (diff < 60000) { // 1 dakika
                return "Az önce";
            } else if (diff < 3600000) { // 1 saat
                long minutes = diff / 60000;
                return minutes + " dakika önce";
            } else if (diff < 86400000) { // 1 gün
                long hours = diff / 3600000;
                return hours + " saat önce";
            } else {
                return dateFormat.format(new Date(timestamp));
            }
        }
    }
} 