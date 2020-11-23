package com.yyj.stydyroom.study.util.beans;

import com.google.gson.annotations.SerializedName;

public class ChatRoom {
    private int id;
    @SerializedName("room_id")
    private int roomId;
    @SerializedName("room_type")
    private int roomType;
    @SerializedName("room_name")
    private String roomName;

    public void setId(int id) {
        this.id = id;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public void setRoomType(int roomType) {
        this.roomType = roomType;
    }

    public int getId() {
        return id;
    }

    public int getRoomId() {
        return roomId;
    }

    public int getRoomType() {
        return roomType;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomName() {
        return roomName;
    }
}
