package com.example.smartexpense.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import java.util.Locale;

public class TextToSpeechHelper {
    private static TextToSpeechHelper instance;
    private TextToSpeech tts;
    private boolean isInitialized = false;
    private String pendingText;

    private TextToSpeechHelper(Context context) {
        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                String language = LocaleHelper.getLanguage(context);
                int result;
                if ("vi".equals(language)) {
                    result = tts.setLanguage(new Locale("vi", "VN"));
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.w("TTSHelper", "Vietnamese voice data not found, falling back to English US");
                        result = tts.setLanguage(Locale.US);
                    }
                } else {
                    result = tts.setLanguage(Locale.US);
                }
                
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w("TTSHelper", "US English also not supported, trying default Locale");
                    result = tts.setLanguage(Locale.getDefault());
                }
                
                isInitialized = true;
                if (pendingText != null) {
                    speak(context.getApplicationContext(), pendingText);
                    pendingText = null;
                }
            } else {
                Log.e("TTSHelper", "TextToSpeech initialization failed");
            }
        });
    }

    public static synchronized TextToSpeechHelper getInstance(Context context) {
        if (instance == null) {
            instance = new TextToSpeechHelper(context);
        }
        return instance;
    }

    public void speak(Context context, String text) {
        android.content.SharedPreferences sp = context.getSharedPreferences("smart_expense_prefs", Context.MODE_PRIVATE);
        boolean enabled = sp.getBoolean("enable_voice_warnings", true);
        if (!enabled) return;

        if (isInitialized && tts != null) {
            String language = LocaleHelper.getLanguage(context);
            String speakText = text;
            if ("vi".equals(language)) {
                int res = tts.setLanguage(new Locale("vi", "VN"));
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(Locale.US);
                    speakText = translateToEnglish(text);
                }
            } else {
                tts.setLanguage(Locale.US);
                speakText = translateToEnglish(text);
            }
            tts.speak(cleanMarkdown(speakText), TextToSpeech.QUEUE_FLUSH, null, "WarningTTS");
        } else {
            Log.w("TTSHelper", "TTS not initialized yet. Queuing text: " + text);
            this.pendingText = text;
        }
    }

    private String translateToEnglish(String text) {
        if (text == null) return "";
        
        // 1. Normal Spending
        if (text.contains("Chi tiêu ở mức bình thường")) {
            return "Spending is at a normal level.";
        }
        
        // 2. Safe Burn Rate
        if (text.contains("Tốc độ chi tiêu đang nằm trong mức an toàn")) {
            return "Spending rate is within the safe limit.";
        }
        
        // 3. Anomaly warning translation
        if (text.contains("chi tiêu mới có giá trị") && text.contains("cao bất thường")) {
            try {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile("giá trị (\\S+)đ.*trung bình lịch sử \\((\\S+)đ\\)");
                java.util.regex.Matcher m = p.matcher(text);
                if (m.find()) {
                    String value = m.group(1);
                    String avg = m.group(2);
                    return "Warning: System detected a new transaction of " + value + " VND, which is abnormally high compared to your historical average of " + avg + " VND for this category.";
                }
            } catch (Exception ignored) {}
            return "Warning: Abnormally high spending detected compared to your historical average.";
        }
        
        // 4. Burn rate alert translation
        if (text.contains("Tốc độ chi tiêu của bạn đang quá nhanh")) {
            try {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile("tiêu hết (\\S+)% ngân sách.*trôi qua (\\S+)% \\((\\d+)/(\\d+) ngày\\).*tối đa (\\S+)đ/ngày");
                java.util.regex.Matcher m = p.matcher(text);
                if (m.find()) {
                    String spentPct = m.group(1);
                    String timePct = m.group(2);
                    String d1 = m.group(3);
                    String d2 = m.group(4);
                    String dailyLimit = m.group(5);
                    return "Your spending speed is too fast! You have spent " + spentPct + "% of your budget while only " + timePct + "% of the cycle has passed (" + d1 + " out of " + d2 + " days). To meet your goal, you should spend a maximum of " + dailyLimit + " VND per day for the rest of the cycle.";
                }
            } catch (Exception ignored) {}
            return "Your spending speed is too fast! Please reduce your expenses to stay within your budget.";
        }

        return text;
    }

    private String cleanMarkdown(String text) {
        if (text == null) return "";
        // Strip bold/italic asterisks
        String cleaned = text.replaceAll("\\*\\*", "");
        cleaned = cleaned.replaceAll("\\*", "");
        // Strip headers
        cleaned = cleaned.replaceAll("#+", "");
        // Strip underscores
        cleaned = cleaned.replaceAll("_", "");
        // Strip bullet points at start of lines
        cleaned = cleaned.replaceAll("(?m)^\\s*[-•+]\\s+", "");
        return cleaned.trim();
    }

    public void stop() {
        if (tts != null) {
            tts.stop();
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        isInitialized = false;
        instance = null;
    }
}
