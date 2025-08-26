package com.example.demo.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class GameRoom {
    private String roomId;
    private String playerX;
    private String playerO;
    private String currentTurn; // "X" hoặc "O"
    private String[][] board;   // trạng thái bàn cờ 3x3

    public GameRoom(String roomId, String creator) {
        this.roomId = roomId;
        this.playerX = creator;
        this.board = new String[15][15];
        this.currentTurn = "X";
    }

    public boolean isFull() {
        return playerX != null && playerO != null;
    }
}
