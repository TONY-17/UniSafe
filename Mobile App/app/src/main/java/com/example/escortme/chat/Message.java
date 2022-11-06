package com.example.escortme.chat;
import com.example.escortme.chat.MemberData;

public class Message {
    private String text;
    private boolean isCurrentUser;
    private MemberData memberData;



    public Message(String text, boolean isCurrentUser, MemberData memberData) {
        this.text = text;
        this.isCurrentUser = isCurrentUser;
        this.memberData = memberData;
    }

    public String getText() {
        return text;
    }

    public boolean isCurrentUser() {
        return isCurrentUser;
    }


    public MemberData getMemberData() {
        return memberData;
    }


}
