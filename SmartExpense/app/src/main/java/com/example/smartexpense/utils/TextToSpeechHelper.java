package com.example.smartexpense.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import java.util.Locale;

public class TextToSpeechHelper {
    private static TextToSpeechHelper instance;
    private TextToSpeech tts;
    private boolean isInitialized = false;

    private TextToSpeechHelper(Context context) {
        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                String language = LocaleHelper.getLanguage(context);
                int result;
                if ("vi".equals(language)) {
                    result = tts.setLanguage(new Locale("vi", "VN"));
                } else {
                    result = tts.setLanguage(Locale.US);
                }
                
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTSHelper", "Language is not supported or missing data");
                } else {
                    isInitialized = true;
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
            if ("vi".equals(language)) {
                tts.setLanguage(new Locale("vi", "VN"));
            } else {
                tts.setLanguage(Locale.US);
            }
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "WarningTTS");
        } else {
            Log.w("TTSHelper", "TTS not initialized yet or not available");
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
