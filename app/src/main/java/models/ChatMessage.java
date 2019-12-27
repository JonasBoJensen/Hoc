package models;

import android.net.Uri;

public class ChatMessage {

    private String messageText, messageUser;
    private String messageTime;
    private String messageIconUrl;


    public ChatMessage(String messageUser, String messageText, String messageTime, String messageIconUrl) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.messageTime = messageTime;
        this.messageIconUrl = messageIconUrl;
//
//        // Initialize to current time


    }

    public ChatMessage(){

    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public String getMessageIconUrl() {
        return messageIconUrl;
    }

    public void setMessageIcon(String messageIconUrl) {

        this.messageIconUrl = messageIconUrl;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }
}
