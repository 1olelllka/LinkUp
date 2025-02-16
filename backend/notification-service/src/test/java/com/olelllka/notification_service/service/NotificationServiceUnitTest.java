package com.olelllka.notification_service.service;

import com.olelllka.notification_service.TestDataUtil;
import com.olelllka.notification_service.domain.entity.NotificationEntity;
import com.olelllka.notification_service.repository.NotificationRepository;
import com.olelllka.notification_service.rest.exception.NotFoundException;
import com.olelllka.notification_service.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceUnitTest {

    @Mock
    private NotificationRepository repository;
    @InjectMocks
    private NotificationServiceImpl service;

    @Test
    public void testThatGetNotificationsForUserReturnsPageOfResults() {
        // given
        UUID id = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 1);
        Page<NotificationEntity> expected = new PageImpl<>(List.of(TestDataUtil.createNotificationEntity()));
        // when
        when(repository.findByUserId(id, pageable)).thenReturn(expected);
        Page<NotificationEntity> result = service.getNotificationsForUser(id, pageable);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getTotalElements(), expected.getTotalElements())
        );
    }

    @Test
    public void testThatUpdateReadNotificationReturnsUpdatedNotification() {
        // given
        String id = "id";
        NotificationEntity entity = TestDataUtil.createNotificationEntity();
        NotificationEntity expected = TestDataUtil.createNotificationEntity();
        expected.setRead(true);
        // when
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        NotificationEntity result = service.updateReadNotification(id);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getRead(), true)
        );
    }

    @Test
    public void testThatUpdateReadNotificationThrowsException() {
        // given
        String id = "id";
        // when
        when(repository.findById(id)).thenReturn(Optional.empty());
        // then
        assertThrows(NotFoundException.class, () -> service.updateReadNotification(id));
        verify(repository, never()).save(any(NotificationEntity.class));
    }

    @Test
    public void testThatDeleteNotificationsForSpecificUserWorks() {
        // given
        UUID userId = UUID.randomUUID();
        // when
        service.deleteNotificationsForSpecificUser(userId);
        // then
        verify(repository, times(1)).deleteByUserId(userId);
    }
}
