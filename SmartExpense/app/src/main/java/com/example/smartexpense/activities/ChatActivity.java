package com.example.smartexpense.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartexpense.R;
import com.example.smartexpense.adapters.ChatAdapter;
import com.example.smartexpense.api.ApiClient;
import com.example.smartexpense.models.ChatMessage;
import com.example.smartexpense.models.ChatRequest;
import com.example.smartexpense.models.ChatResponse;
import com.example.smartexpense.utils.TextToSpeechHelper;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;

    private ImageView btnBack, btnTtsToggle, btnMic, btnSend;
    private EditText etMessage;
    private RecyclerView rvChatMessages;
    private LinearLayout layoutSuggestionsContainer;
    private MaterialCardView cardSend, cardMic;

    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messageList = new ArrayList<>();
    private int userId = 1;
    private boolean isTtsEnabled = true;

    private View viewAiGlow;
    private TextView tvAiStateTitle, tvAiStateDesc;
    private android.animation.ObjectAnimator breathingAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        userId = getSharedPreferences("smart_expense_prefs", MODE_PRIVATE).getInt("user_id", 1);
        isTtsEnabled = getSharedPreferences("smart_expense_prefs", MODE_PRIVATE).getBoolean("enable_chat_tts", true);

        btnBack = findViewById(R.id.btn_back);
        btnTtsToggle = findViewById(R.id.btn_tts_toggle);
        btnMic = findViewById(R.id.btn_mic);
        btnSend = findViewById(R.id.btn_send);
        etMessage = findViewById(R.id.et_message);
        rvChatMessages = findViewById(R.id.rv_chat_messages);
        layoutSuggestionsContainer = findViewById(R.id.layout_suggestions_container);
        cardSend = findViewById(R.id.card_send);
        cardMic = findViewById(R.id.card_mic);

        viewAiGlow = findViewById(R.id.view_ai_glow);
        tvAiStateTitle = findViewById(R.id.tv_ai_state_title);
        tvAiStateDesc = findViewById(R.id.tv_ai_state_desc);

        setupRecyclerView();
        setupActions();
        updateTtsToggleButton();
        startBreathingAnimation();

        // Welcome introduction message from Assistant
        addAssistantMessage("Xin chào! Tôi là Trợ lý ảo tài chính SmartExpense của bạn. 🙋‍♂️\n\nHôm nay, bạn có câu hỏi nào cần tư vấn về ví tiền, tình hình chi tiêu tháng này hay cách tối ưu hóa tiết kiệm không? Hãy nhắn tin hoặc nói trực tiếp với tôi nhé!");
        showSuggestions(new String[]{"Ví tôi còn bao nhiêu?", "Thống kê chi tiêu tháng này", "Tình hình ngân sách", "Tư vấn tiết kiệm"});
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(messageList);
        rvChatMessages.setLayoutManager(new LinearLayoutManager(this));
        rvChatMessages.setAdapter(chatAdapter);
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());

        btnTtsToggle.setOnClickListener(v -> {
            isTtsEnabled = !isTtsEnabled;
            getSharedPreferences("smart_expense_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("enable_chat_tts", isTtsEnabled)
                    .apply();
            updateTtsToggleButton();
            Toast.makeText(this, isTtsEnabled ? "Đã BẬT đọc câu trả lời" : "Đã TẮT đọc câu trả lời", Toast.LENGTH_SHORT).show();
        });

        cardSend.setOnClickListener(v -> sendMessage());
        btnSend.setOnClickListener(v -> sendMessage());

        cardMic.setOnClickListener(v -> startSpeechToText());
        btnMic.setOnClickListener(v -> startSpeechToText());
    }

    private void updateTtsToggleButton() {
        if (isTtsEnabled) {
            btnTtsToggle.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
            btnTtsToggle.setColorFilter(getResources().getColor(R.color.primary));
        } else {
            btnTtsToggle.setImageResource(android.R.drawable.ic_lock_silent_mode);
            btnTtsToggle.setColorFilter(getResources().getColor(R.color.text_secondary));
        }
    }

    private void sendMessage() {
        String query = etMessage.getText().toString().trim();
        if (query.isEmpty()) return;

        etMessage.setText("");
        addUserMessage(query);
        setAiState(1); // Set state to THINKING

        // Call backend API
        ChatRequest request = new ChatRequest(userId, query);
        ApiClient.getApiService().chat(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChatResponse res = response.body();
                    addAssistantMessage(res.getReply());
                    if (res.getSuggestions() != null && !res.getSuggestions().isEmpty()) {
                        showSuggestions(res.getSuggestions().toArray(new String[0]));
                    } else {
                        showSuggestions(new String[]{"Ví tiền của tôi", "Chi tiêu tháng này", "Tư vấn tiết kiệm"});
                    }

                    // Speak response if enabled
                    if (isTtsEnabled) {
                        setAiState(2); // Set state to SPEAKING
                        TextToSpeechHelper.getInstance(ChatActivity.this)
                                .speak(ChatActivity.this, res.getReply());
                        new android.os.Handler().postDelayed(() -> setAiState(0), Math.max(3000, res.getReply().length() * 60));
                    } else {
                        setAiState(0); // Back to IDLE
                    }
                } else {
                    setAiState(0); // Back to IDLE
                    addAssistantMessage("Xin lỗi, tôi không thể xử lý câu hỏi này lúc này. Vui lòng thử lại sau!");
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                setAiState(0); // Back to IDLE
                // Smart Offline Fallback if server is not running
                addAssistantMessage("⚠️ **Không thể kết nối máy chủ Spring Boot**\n\nĐể đảm bảo an toàn dữ liệu, xin vui lòng kiểm tra xem máy chủ backend đã được bật hay chưa. Dưới đây là một số mẹo tài chính bạn có thể áp dụng:\n\n• Luôn tuân thủ quy tắc 50/30/20.\n• Hạn chế chi tiêu các danh mục không thiết yếu.\n• Thường xuyên ghi chép giao dịch đầy đủ.");
                showSuggestions(new String[]{"Ví tôi còn bao nhiêu?", "Thống kê chi tiêu tháng này", "Tư vấn tiết kiệm"});
            }
        });
    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Đang nghe... Hãy nói câu hỏi tài chính của bạn");
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Thiết bị không hỗ trợ nhận dạng giọng nói", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                etMessage.setText(spokenText);
                sendMessage(); // Auto-send spoken message!
            }
        }
    }

    private void addUserMessage(String text) {
        messageList.add(new ChatMessage(text, false));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    private void addAssistantMessage(String text) {
        messageList.add(new ChatMessage(text, true));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (!messageList.isEmpty()) {
            rvChatMessages.smoothScrollToPosition(messageList.size() - 1);
        }
    }

    private void showSuggestions(String[] suggestions) {
        layoutSuggestionsContainer.removeAllViews();
        for (String suggestion : suggestions) {
            TextView chip = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 4, 8, 4);
            chip.setLayoutParams(params);
            chip.setText(suggestion);
            chip.setTextColor(getResources().getColor(R.color.primary));
            chip.setTextSize(13f);
            chip.setPadding(32, 16, 32, 16);
            chip.setBackgroundResource(R.drawable.bg_suggestion_chip);
            chip.setClickable(true);
            chip.setFocusable(true);

            chip.setOnClickListener(v -> {
                etMessage.setText(suggestion);
                sendMessage(); // Auto send suggestions on click!
            });

            layoutSuggestionsContainer.addView(chip);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (breathingAnimator != null) {
            breathingAnimator.cancel();
        }
        TextToSpeechHelper.getInstance(this).stop();
    }

    private void startBreathingAnimation() {
        if (viewAiGlow == null) return;
        breathingAnimator = android.animation.ObjectAnimator.ofFloat(viewAiGlow, "alpha", 0.3f, 1.0f);
        breathingAnimator.setDuration(1500); // 1.5 seconds per breath
        breathingAnimator.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        breathingAnimator.setRepeatMode(android.animation.ValueAnimator.REVERSE);
        breathingAnimator.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        breathingAnimator.start();
    }

    private void setAiState(int state) {
        if (tvAiStateTitle == null || tvAiStateDesc == null || breathingAnimator == null) return;
        
        if (state == 0) { // IDLE
            tvAiStateTitle.setText("Hệ thống AI đang sẵn sàng");
            tvAiStateDesc.setText("Chạm nút Micro để nói chuyện trực tiếp bằng giọng nói");
            breathingAnimator.setDuration(1500); // Normal soft breath
        } else if (state == 1) { // THINKING
            tvAiStateTitle.setText("Trợ lý AI đang suy nghĩ...");
            tvAiStateDesc.setText("Đang phân tích thông tin tài chính cá nhân của bạn");
            breathingAnimator.setDuration(400); // Fast pulse representing activity!
        } else if (state == 2) { // SPEAKING
            tvAiStateTitle.setText("Trợ lý AI đang trả lời...");
            tvAiStateDesc.setText("Nghe giọng nói tư vấn hoặc đọc trực tiếp bên dưới");
            breathingAnimator.setDuration(800); // Medium fluid breathing during speech
        }
    }
}
