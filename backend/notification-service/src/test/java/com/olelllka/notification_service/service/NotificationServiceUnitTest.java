package com.olelllka.notification_service.service;

import com.olelllka.notification_service.TestDataUtil;
import com.olelllka.notification_service.domain.entity.NotificationEntity;
import com.olelllka.notification_service.feign.ProfileFeign;
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
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceUnitTest {

    @Mock
    private NotificationRepository repository;
    @Mock
    private ProfileFeign profileFeign;
    @InjectMocks
    private NotificationServiceImpl service;

    @Test
    public void testThatGetNotificationsForUserReturnsPageOfResults() {
        // given
        UUID id = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 1);
        Page<NotificationEntity> expected = new PageImpl<>(List.of(TestDataUtil.createNotificationEntity()));
        // when
        when(profileFeign.getProfileById(id)).thenReturn(ResponseEntity.ok().build());
        when(repository.findByUserId(id, pageable)).thenReturn(expected);
        Page<NotificationEntity> result = service.getNotificationsForUser(id, pageable);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getTotalElements(), expected.getTotalElements())
        );
    }

    @Test
    public void testThatGetNotificationsForUserThrowsException() {
        // given
        UUID id = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 1);
        Page<NotificationEntity> expected = new PageImpl<>(List.of(TestDataUtil.createNotificationEntity()));
        // when
        when(profileFeign.getProfileById(id)).thenReturn(ResponseEntity.notFound().build());
        assertThrows(NotFoundException.class, () -> service.getNotificationsForUser(id, pageable));
        verify(repository, never()).findByUserId(id, pageable);
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
    public void testThatDeleteNotificationsForSpecificUserThrowsException() {
        // given
        UUID userId = UUID.randomUUID();
        // when
        when(profileFeign.getProfileById(userId)).thenReturn(ResponseEntity.notFound().build());
        // then
        assertThrows(NotFoundException.class, () -> service.deleteNotificationsForSpecificUser(userId));
        verify(repository, never()).deleteByUserId(userId);
    }

    @Test
    public void testThatDeleteNotificationsForSpecificUserWorks() {
        // given
        UUID userId = UUID.randomUUID();
        // when
        when(profileFeign.getProfileById(userId)).thenReturn(ResponseEntity.ok().build());
        service.deleteNotificationsForSpecificUser(userId);
        // then
        verify(repository, times(1)).deleteByUserId(userId);
    }
}
