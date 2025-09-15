package com.olelllka.chat_service.rest.controller;

import com.olelllka.chat_service.domain.dto.ErrorMessage;
import com.olelllka.chat_service.domain.dto.ListOfChatsDto;
import com.olelllka.chat_service.domain.dto.MessageDto;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.mapper.impl.ChatMapperImpl;
import com.olelllka.chat_service.mapper.impl.MessageMapperImpl;
import com.olelllka.chat_service.rest.exception.ValidationException;
import com.olelllka.chat_service.service.ChatService;
import com.olelllka.chat_service.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Chat Service API Endpoints", description = "All of the endpoints for chat service")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final MessageService messageService;
    private final ChatMapperImpl chatMapper;
    private final MessageMapperImpl messageMapper;

    @Operation(summary = "Get all chats for user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched page of chats for user"),
            @ApiResponse(responseCode = "401", description = "Authorization error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            })
    })
    @GetMapping("/users/{user_id}")
    public ResponseEntity<Page<ListOfChatsDto>> getAllChatsForUser(@RequestHeader(name="Authorization") String header,
                                                                    Pageable pageable,
                                                                   @PathVariable UUID user_id) {
        Page<ChatEntity> entities = chatService.getChatsForUser(user_id, pageable, header.substring(7));
        Page<ListOfChatsDto> result = entities.map(this::mapToListOfChats);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get chat by two users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched chat"),
            @ApiResponse(responseCode = "401", description = "Authorization error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            }),
            @ApiResponse(responseCode = "404", description = "Chat not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            })
    })
    @GetMapping("")
    public ResponseEntity<ChatEntity> getChatByTwoUsers(@RequestParam(name = "user1") UUID user1,
                                                        @RequestParam(name= "user2") UUID user2,
                                                        @RequestHeader(name="Authorization") String authHeader) {
        ChatEntity chat = chatService.getChatByTwoUsers(user1, user2, authHeader.substring(7));
        return new ResponseEntity<>(chat, HttpStatus.OK);
    }

    @Operation(summary = "Delete chat for two users and all of the messages there")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the chat"),
            @ApiResponse(responseCode = "401", description = "Authorization error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            })
    })
    @DeleteMapping("/{chat_id}")
    public ResponseEntity deleteChat(@RequestHeader(name="Authorization") String header,
                                    @PathVariable String chat_id) {
        chatService.deleteChat(chat_id, header.substring(7));
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Get all messages for chat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched all of the messages for chat"),
            @ApiResponse(responseCode = "401", description = "Authorization error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            })
    })
    @GetMapping("/{chat_id}/messages")
    public ResponseEntity<Page<MessageDto>> getAllMessagesForChat(@RequestHeader(name="Authorization") String header,
                                                                    @PathVariable String chat_id, Pageable pageable) {
        Page<MessageEntity> messages = messageService.getMessagesForChat(chat_id, pageable, header.substring(7));
        Page<MessageDto> result = messages.map(messageMapper::toDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Update specific message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the message"),
            @ApiResponse(responseCode = "401", description = "Authorization error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            }),
            @ApiResponse(responseCode = "400", description = "Validation error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            }),
            @ApiResponse(responseCode = "404", description = "Message not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            })
    })
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

    @Operation(summary = "Delete specific message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the message"),
            @ApiResponse(responseCode = "401", description = "Authorization error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            })
    })
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
