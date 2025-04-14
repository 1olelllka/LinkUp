package com.olelllka.notification_service.service;

import com.olelllka.notification_service.TestDataUtil;
import com.olelllka.notification_service.domain.entity.NotificationEntity;
import com.olelllka.notification_service.repository.NotificationRepository;
import com.olelllka.notification_service.rest.exception.AuthException;
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
    @Mock
    private JWTUtil jwtUtil;
    @InjectMocks
    private NotificationServiceImpl service;

    @Test
    public void testThatGetNotificationsForUserReturnsPageOfResults() {
        // given
        UUID id = UUID.randomUUID();
        String jwt = "jwt";
        Pageable pageable = PageRequest.of(0, 1);
        Page<NotificationEntity> expected = new PageImpl<>(List.of(TestDataUtil.createNotificationEntity()));
        // when
        when(jwtUtil.extractId(jwt)).thenReturn(id.toString());
        when(repository.findByUserId(id, pageable)).thenReturn(expected);
        Page<NotificationEntity> result = service.getNotificationsForUser(id, pageable, "jwt");
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getTotalElements(), expected.getTotalElements())
        );
    }

    @Test
    public void testThatGetNotificationsForUserThrowsAuthException() {
        // given
        UUID id = UUID.randomUUID();
        String jwt = "jwt";
        Pageable pageable = PageRequest.of(0, 1);
        // when
        when(jwtUtil.extractId(jwt)).thenReturn(UUID.randomUUID().toString());
        assertThrows(AuthException.class, () -> service.getNotificationsForUser(id, pageable, "jwt"));
        // then
        verify(repository, never()).findByUserId(any(UUID.class), any(Pageable.class));
    }

    @Test
    public void testThatUpdateReadNotificationReturnsUpdatedNotification() {
        // given
        String id = "id";
        String jwt = "jwt";
        NotificationEntity entity = TestDataUtil.createNotificationEntity();
        NotificationEntity expected = TestDataUtil.createNotificationEntity();
        expected.setRead(true);
        // when
        when(jwtUtil.extractId(jwt)).thenReturn(entity.getUserId().toString());
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        NotificationEntity result = service.updateReadNotification(id, jwt);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getRead(), true)
        );
    }

    @Test
    public void testThatUpdateReadNotificationThrowsAuthException() {
        // given
        String id = "id";
        String jwt = "jwt";
        NotificationEntity entity = TestDataUtil.createNotificationEntity();
        // when
        when(jwtUtil.extractId(jwt)).thenReturn(UUID.randomUUID().toString());
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        // then
        assertThrows(AuthException.class, () -> service.updateReadNotification(id, jwt));
        verify(repository, never()).save(any(NotificationEntity.class));

    }

    @Test
    public void testThatUpdateReadNotificationThrowsNotFoundException() {
        // given
        String id = "id";
        String jwt = "jwt";
        // when
        when(repository.findById(id)).thenReturn(Optional.empty());
        // then
        assertThrows(NotFoundException.class, () -> service.updateReadNotification(id, jwt));
        verify(repository, never()).save(any(NotificationEntity.class));
        verify(jwtUtil, never()).extractId(anyString());
    }

    @Test
    public void testThatDeleteSpecificNotificationWorks() {
        // given
        String id = "id";
        String jwt = "jwt";
        NotificationEntity entity = TestDataUtil.createNotificationEntity();
        // when
        when(repository.existsById(id)).thenReturn(true);
        when(jwtUtil.extractId(jwt)).thenReturn(entity.getUserId().toString());
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        service.deleteSpecificNotification(id, jwt);
        // then
        verify(repository, times(1)).deleteById(id);
    }

    @Test
    public void testThatDeleteSpecificNotificationThrowsAuthException() {
        // given
        String id = "id";
        String jwt = "jwt";
        NotificationEntity entity = TestDataUtil.createNotificationEntity();
        // when
        when(repository.existsById(id)).thenReturn(true);
        when(jwtUtil.extractId(jwt)).thenReturn(UUID.randomUUID().toString());
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        assertThrows(AuthException.class, () -> service.deleteSpecificNotification(id, jwt));
        // then
        verify(repository, never()).deleteById(id);
    }

    @Test
    public void testThatDeleteNotificationsForSpecificUserWorks() {
        // given
        UUID userId = UUID.randomUUID();
        String jwt = "jwt";
        // when
        when(jwtUtil.extractId(jwt)).thenReturn(userId.toString());
        service.deleteNotificationsForSpecificUser(userId, jwt);
        // then
        verify(repository, times(1)).deleteByUserId(userId);
    }

    @Test
    public void testThatDeleteNotificationForSpecificUserThrowsAuthException() {
        // given
        UUID userId = UUID.randomUUID();
        String jwt = "jwt";
        // when
        when(jwtUtil.extractId(jwt)).thenReturn(UUID.randomUUID().toString());
        // then
        assertThrows(AuthException.class, () -> service.deleteNotificationsForSpecificUser(userId, jwt));
    }
}
