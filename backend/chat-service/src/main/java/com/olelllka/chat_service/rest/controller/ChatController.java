package com.olelllka.chat_service.rest.controller;

import com.olelllka.chat_service.domain.dto.ListOfChatsDto;
import com.olelllka.chat_service.domain.dto.MessageDto;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.mapper.impl.ChatMapperImpl;
import com.olelllka.chat_service.mapper.impl.MessageMapperImpl;
import com.olelllka.chat_service.rest.exception.ValidationException;
import com.olelllka.chat_service.service.ChatService;
import com.olelllka.chat_service.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final MessageService messageService;
    private final ChatMapperImpl chatMapper;
    private final MessageMapperImpl messageMapper;

    @GetMapping("/users/{user_id}")
    public ResponseEntity<Page<ListOfChatsDto>> getAllChatsForUser(@RequestHeader(name="Authorization") String header,
                                                                    Pageable pageable,
                                                                   @PathVariable UUID user_id) {
        Page<ChatEntity> entities = chatService.getChatsForUser(user_id, pageable, header.substring(7));
        Page<ListOfChatsDto> result = entities.map(this::mapToListOfChats);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<ChatEntity> getChatByTwoUsers(@RequestParam(name = "user1") UUID user1,
                                                        @RequestParam(name= "user2") UUID user2,
                                                        @RequestHeader(name="Authorization") String authHeader) {
        ChatEntity chat = chatService.getChatByTwoUsers(user1, user2, authHeader.substring(7));
        return new ResponseEntity<>(chat, HttpStatus.OK);
    }

    @DeleteMapping("/{chat_id}")
    public ResponseEntity deleteChat(@RequestHeader(name="Authorization") String header,
                                    @PathVariable String chat_id) {
        chatService.deleteChat(chat_id, header.substring(7));
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{chat_id}/messages")
    public ResponseEntity<Page<MessageDto>> getAllMessagesForChat(@RequestHeader(name="Authorization") String header,
                                                                    @PathVariable String chat_id, Pageable pageable) {
        Page<MessageEntity> messages = messageService.getMessagesForChat(chat_id, pageable, header.substring(7));
        Page<MessageDto> result = messages.map(messageMapper::toDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PatchMapping("/messages/{msg_id}")
    public ResponseEntity<MessageDto> updateSpecificMessage(@RequestHeader(name="Authorization") String header,
                                                            @PathVariable String msg_id,
                                                            @Valid @RequestBody MessageDto updatedMsg,
                                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().stream().map(err -> err.getDefaultMessage()).collect(Collectors.joining(" "));
            throw new ValidationException(msg);
        }
        MessageEntity updated = messageService.updateMessage(msg_id, updatedMsg, header.substring(7));
        return new ResponseEntity<>(messageMapper.toDto(updated), HttpStatus.OK);
    }

    @DeleteMapping("/messages/{msg_id}")
    public ResponseEntity deleteSpecificMessage(@RequestHeader(name="Authorization") String header,
                                                @PathVariable String msg_id) {
        messageService.deleteSpecificMessage(msg_id, header.substring(7));
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    private ListOfChatsDto mapToListOfChats(ChatEntity chat) {
        return ListOfChatsDto.builder()
                .id(chat.getId())
                .participants(chat.getParticipants())
                .lastMessage(chat.getLastMessage())
                .time(chat.getTime())
                .build();
    }
}
