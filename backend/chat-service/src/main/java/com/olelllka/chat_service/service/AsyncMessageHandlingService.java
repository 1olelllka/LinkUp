package com.olelllka.chat_service.service;

import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.repository.MessageRepository;
import com.olelllka.chat_service.rest.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AsyncMessageHandlingService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;

    @Async
    public void saveMessageToDatabase(MessageEntity entity) {
        chatRepository.findById(entity.getChatId()).map((chat) -> {
            chat.setLastMessage(entity.getContent());
            chat.setTime(entity.getCreatedAt());
            return chatRepository.save(chat);
        }).orElseThrow(() -> new NotFoundException("Chat with such id does not exist."));
        messageRepository.save(entity);
    }

    @Async
    public void updateChatsLastMessageInDatabase(MessageEntity entity) {
        chatRepository.findById(entity.getChatId()).map((chat) -> {
            try {
                MessageEntity lastMessage = messageRepository.findByChatId(chat.getId(), PageRequest.of(0, 1))
                        .getContent().getFirst();
                if (Objects.equals(lastMessage.getId(), entity.getId())) {
                    Optional.of(entity.getContent()).ifPresent(chat::setLastMessage);
                }
            } catch (NoSuchElementException ignored) {};
            return chatRepository.save(chat);
        }).orElseThrow(() -> new NotFoundException("Chat with such id does not exist."));
    }

    @Async
    public void deleteMessageFromDatabase(MessageEntity entity) {
        chatRepository.findById(entity.getChatId()).map(chat -> {
            chat.setLastMessage("*The message was deleted*");
            return chatRepository.save(chat);
        });
        messageRepository.deleteById(entity.getId());
    }

}
