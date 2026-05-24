package com.example.smartexpense.models;

public class ChatMessage {
    private String text;
    private boolean isReceived; // true: Assistant, false: User

    public ChatMessage(String text, boolean isReceived) {
        this.text = text;
        this.isReceived = isReceived;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isReceived() {
        return isReceived;
    }

    public void setReceived(boolean received) {
        isReceived = received;
    }
}
