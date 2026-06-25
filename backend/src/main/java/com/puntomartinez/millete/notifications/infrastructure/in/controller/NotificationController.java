package com.puntomartinez.millete.notifications.infrastructure.in.controller;

import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.domain.ports.in.DeleteNotificationUseCase;
import com.puntomartinez.millete.notifications.domain.ports.in.GetNotificationsUseCase;
import com.puntomartinez.millete.notifications.domain.ports.in.MarkNotificationAsReadUseCase;
import com.puntomartinez.millete.notifications.infrastructure.in.controller.dto.NotificationResponseDTO;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final GetNotificationsUseCase getNotificationsUseCase;
    private final MarkNotificationAsReadUseCase markNotificationAsReadUseCase;
    private final DeleteNotificationUseCase deleteNotificationUseCase;

    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getNotifications(Authentication authentication) {
        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        List<Notification> notifications = getNotificationsUseCase.getUserNotifications(jwtUser.getId());
        return ResponseEntity.ok(notifications.stream().map(this::toDto).toList());
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        long count = getNotificationsUseCase.getUnreadCount(jwtUser.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(Authentication authentication, @PathVariable UUID notificationId) {
        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        markNotificationAsReadUseCase.markAsRead(jwtUser.getId(), notificationId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID notificationId) {
        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        deleteNotificationUseCase.delete(jwtUser.getId(), notificationId);
        return ResponseEntity.noContent().build();
    }

    private NotificationResponseDTO toDto(Notification notification) {
        return new NotificationResponseDTO(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getMetadata(),
                notification.isRead(),
                notification.isActionRequired(),
                notification.getActionedAt(),
                notification.getCreatedAt(),
                notification.getExpiresAt()
        );
    }
}
