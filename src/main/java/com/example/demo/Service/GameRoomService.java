package com.example.demo.Service;

import com.example.demo.Entity.GameRoom;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameRoomService {
    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();
    private String waitingPlayerId = null;

    public synchronized GameRoom findMatch(String playerId) {
        if (waitingPlayerId == null) {
            waitingPlayerId = playerId;
            GameRoom room = new GameRoom(UUID.randomUUID().toString(), playerId);
            rooms.put(room.getRoomId(), room);
            return room;
        } else {
            String opponent = waitingPlayerId;
            waitingPlayerId = null;

            GameRoom room = rooms.values().stream()
                    .filter(r -> opponent.equals(r.getPlayerX()) && !r.isFull())
                    .findFirst()
                    .orElseThrow();

            room.setPlayerO(playerId);
            return room;
        }
    }

    public GameRoom getRoom(String roomId) {
        return rooms.get(roomId);
    }
}
