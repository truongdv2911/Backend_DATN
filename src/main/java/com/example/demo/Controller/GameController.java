package com.example.demo.Controller;

import com.example.demo.DTOs.MoveDTO;
import com.example.demo.Entity.GameRoom;
import com.example.demo.Service.GameRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.SimpleTimeZone;

@Controller
@RequiredArgsConstructor
public class GameController {
    private final GameRoomService gameRoomService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/find-match")
    public void findMatch(String playerId){
        GameRoom room = gameRoomService.findMatch(playerId);
        simpMessagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(), room);
    }

    @MessageMapping("/move/{roomId}")
    public void makeMove(@DestinationVariable String roomId, MoveDTO move) {
        GameRoom room = gameRoomService.getRoom(roomId);
        if (room != null) {
            room.getBoard()[move.getRow()][move.getCol()] = move.getSymbol();
            room.setCurrentTurn(move.getSymbol().equals("X") ? "O" : "X");

            simpMessagingTemplate.convertAndSend("/topic/room/" + roomId, move);
        }
    }
}
