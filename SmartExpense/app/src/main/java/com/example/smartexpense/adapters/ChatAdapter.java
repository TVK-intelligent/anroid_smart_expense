package com.example.smartexpense.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartexpense.R;
import com.example.smartexpense.models.ChatMessage;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        if (message.isReceived()) {
            return TYPE_RECEIVED;
        } else {
            return TYPE_SENT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (getItemViewType(position) == TYPE_SENT) {
            ((SentViewHolder) holder).bind(message);
        } else {
            ((ReceivedViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private static android.text.Spanned formatMarkdown(String text) {
        if (text == null) return new android.text.SpannableString("");

        // 1. Escape HTML special characters
        String formatted = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

        // 2. Parse bold markers: **text** -> <b>text</b>
        formatted = formatted.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");

        // 3. Parse headers: e.g. ### Header -> <b>Header</b>
        formatted = formatted.replaceAll("(?m)^###\\s+(.*)$", "<br/><b>$1</b>");
        formatted = formatted.replaceAll("(?m)^##\\s+(.*)$", "<br/><b>$1</b>");
        formatted = formatted.replaceAll("(?m)^#\\s+(.*)$", "<br/><b>$1</b>");

        // 4. Parse bullet points: e.g. * Item -> • Item
        formatted = formatted.replaceAll("(?m)^[\\*\\-]\\s+(.*)$", "• $1");

        // 5. Convert line breaks \n to <br/>
        formatted = formatted.replace("\n", "<br/>");

        // 6. Clean consecutive <br/>
        formatted = formatted.replace("<br/><br/>", "<br/>");

        // 7. Render using Html
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return android.text.Html.fromHtml(formatted, android.text.Html.FROM_HTML_MODE_LEGACY);
        } else {
            return android.text.Html.fromHtml(formatted);
        }
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageBody;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageBody = itemView.findViewById(R.id.tv_message_body);
        }

        public void bind(ChatMessage message) {
            tvMessageBody.setText(formatMarkdown(message.getText()));
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageBody;

        public ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageBody = itemView.findViewById(R.id.tv_message_body);
        }

        public void bind(ChatMessage message) {
            tvMessageBody.setText(formatMarkdown(message.getText()));
        }
    }
}
