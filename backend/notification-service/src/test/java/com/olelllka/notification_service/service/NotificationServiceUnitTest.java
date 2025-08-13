package com.olelllka.notification_service.service;

import com.olelllka.notification_service.TestDataUtil;
import com.olelllka.notification_service.domain.entity.NotificationEntity;
import com.olelllka.notification_service.repository.NotificationRepository;
import com.olelllka.notification_service.rest.exception.AuthException;
import com.olelllka.notification_service.rest.exception.ForbiddenException;
import com.olelllka.notification_service.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

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
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MongoTemplate template;
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
        String jwt = "jwt";
        NotificationEntity entity = TestDataUtil.createNotificationEntity();
        NotificationEntity expected = TestDataUtil.createNotificationEntity();
        List<String> ids = List.of("1");
        Query updateQuery = new Query(Criteria.where("id").in(ids));
        Update update = new Update().set("read", true);
        expected.setRead(true);
        // when
        when(jwtUtil.extractId(jwt)).thenReturn(entity.getUserId().toString());
        when(template.query(NotificationEntity.class).matching(any(Query.class)).all()).thenReturn(List.of(entity));
        // then
        service.updateReadNotifications(ids, jwt);
        verify(template.update(NotificationEntity.class).matching(updateQuery)
                .apply(update), times(1)).all();
    }

    @Test
    public void testThatUpdateReadNotificationThrowsForbiddenException() {
        // given
        String id = "id";
        String jwt = "jwt";
        // when
        when(jwtUtil.extractId(jwt)).thenReturn(UUID.randomUUID().toString());
        when(template.query(NotificationEntity.class).matching(any(Query.class)).all()).thenReturn(List.of());
        // then
        assertThrows(ForbiddenException.class, () -> service.updateReadNotifications(List.of(id), jwt));
        verify(template, never()).update(any());
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
