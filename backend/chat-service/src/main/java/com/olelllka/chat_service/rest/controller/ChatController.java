package com.olelllka.chat_service.rest.controller;

import com.olelllka.chat_service.domain.dto.ChatDto;
import com.olelllka.chat_service.domain.dto.ListOfChatsDto;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.mapper.impl.ChatMapperImpl;
import com.olelllka.chat_service.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/chats")
public class ChatController {

    @Autowired
    private ChatService chatService;
    @Autowired
    private ChatMapperImpl chatMapper;

    @GetMapping("/users/{user_id}")
    public ResponseEntity<Page<ListOfChatsDto>> getAllChatsForUser(Pageable pageable, @PathVariable String user_id) {
        Page<ChatEntity> entities = chatService.getChatsForUser(user_id, pageable);
        Page<ListOfChatsDto> result = entities.map(this::mapToListOfChats);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<ChatDto> createNewChat(@RequestParam String userId1,
                                                 @RequestParam String userId2) {
        ChatEntity entity = chatService.createNewChat(userId1, userId2);
        return new ResponseEntity<>(chatMapper.toDto(entity), HttpStatus.CREATED);
    }

    @DeleteMapping("")

    private ListOfChatsDto mapToListOfChats(ChatEntity chat) {
        return ListOfChatsDto.builder()
                .id(chat.getId())
                .participants(chat.getParticipants())
                .build();
    }
}
