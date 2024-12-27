package com.olelllka.chat_service.rest.controller;

import com.olelllka.chat_service.domain.dto.ChatDto;
import com.olelllka.chat_service.domain.dto.ListOfChatsDto;
import com.olelllka.chat_service.domain.dto.MessageDto;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.mapper.impl.ChatMapperImpl;
import com.olelllka.chat_service.mapper.impl.MessageMapperImpl;
import com.olelllka.chat_service.rest.exception.ValidationException;
import com.olelllka.chat_service.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/chats")
public class ChatController {

    @Autowired
    private ChatService chatService;
    @Autowired
    private ChatMapperImpl chatMapper;
    @Autowired
    private MessageMapperImpl messageMapper;

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

    @DeleteMapping("/{chat_id}")
    public ResponseEntity deleteChat(@PathVariable String chat_id) {
        chatService.deleteChat(chat_id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{chat_id}/messages")
    public ResponseEntity<Page<MessageDto>> getAllMessagesForChat(@PathVariable String chat_id, Pageable pageable) {
        Page<MessageEntity> messages = chatService.getMessagesForChat(chat_id, pageable);
        Page<MessageDto> result = messages.map(messageMapper::toDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PatchMapping("/{chat_id}/messages/{msg_id}")
    public ResponseEntity<MessageDto> updateSpecificMessage(@PathVariable String chat_id,
                                                            @PathVariable String msg_id,
                                                            @Valid @RequestBody MessageDto updatedMsg,
                                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().stream().map(err -> err.getDefaultMessage()).collect(Collectors.joining(" "));
            throw new ValidationException(msg);
        }
        MessageEntity updated = chatService.updateMessage(chat_id, msg_id, updatedMsg);
        return new ResponseEntity<>(messageMapper.toDto(updated), HttpStatus.OK);
    }

    @DeleteMapping("/{chat_id}/messages/{msg_id}")
    public ResponseEntity deleteSpecificMessage(@PathVariable String chat_id,
                                                @PathVariable String msg_id) {
        chatService.deleteSpecificMessage(chat_id, msg_id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    private ListOfChatsDto mapToListOfChats(ChatEntity chat) {
        return ListOfChatsDto.builder()
                .id(chat.getId())
                .participants(chat.getParticipants())
                .build();
    }
}
