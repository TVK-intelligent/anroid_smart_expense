package com.example.smartexpense.models;

import java.util.List;

public class ChatResponse {
    private String reply;
    private List<String> suggestions;

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}
