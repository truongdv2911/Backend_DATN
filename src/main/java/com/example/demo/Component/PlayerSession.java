package com.example.demo.Component;

import com.example.demo.Enum.Difficulty;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

public class PlayerSession {
    private Difficulty currentDifficulty = Difficulty.MEDIUM;
    private final Queue<Boolean> lastResults = new LinkedList<>();
    private static final int HISTORY_SIZE = 5;

    public Difficulty getCurrentDifficulty() {
        return currentDifficulty;
    }

    public void recordOutcome(String result) {
        Boolean outcome = null;

        if ("WIN".equalsIgnoreCase(result)) {
            outcome = true;
        } else if ("LOSE".equalsIgnoreCase(result)) {
            outcome = false;
        }
        // DRAW -> outcome = null -> không tính

        if (outcome != null) {
            if (lastResults.size() >= HISTORY_SIZE) {
                lastResults.poll();
            }
            lastResults.add(outcome);
            adjustDifficulty();
        }
    }

    private void adjustDifficulty() {
        // Cần ít nhất 2 kết quả để xét
        if (lastResults.size() < 2) return;

        // Lấy ra 2 kết quả cuối cùng
        Boolean[] arr = lastResults.toArray(new Boolean[0]);
        Boolean last = arr[arr.length - 1];
        Boolean secondLast = arr[arr.length - 2];

        // Nếu cả 2 trận đều WIN
        if (Boolean.TRUE.equals(last) && Boolean.TRUE.equals(secondLast)) {
            if (currentDifficulty == Difficulty.EASY) {
                currentDifficulty = Difficulty.MEDIUM;
            } else if (currentDifficulty == Difficulty.MEDIUM) {
                currentDifficulty = Difficulty.HARD;
            }
        }

        // Nếu cả 2 trận đều LOSE
        else if (Boolean.FALSE.equals(last) && Boolean.FALSE.equals(secondLast)) {
            if (currentDifficulty == Difficulty.HARD) {
                currentDifficulty = Difficulty.MEDIUM;
            } else if (currentDifficulty == Difficulty.MEDIUM) {
                currentDifficulty = Difficulty.EASY;
            }
        }
    }
}
