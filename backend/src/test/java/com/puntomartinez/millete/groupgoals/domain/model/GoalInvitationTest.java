package com.puntomartinez.millete.groupgoals.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

@DisplayName("FamilyInvitation - Modelo de dominio")
class GoalInvitationTest {

    @Test
    @DisplayName("Debe ser aceptable si PENDING y no expirada")
    void shouldBeAcceptable() {
        GoalInvitation invitation = new GoalInvitation();
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusHours(24));
        invitation.setActive(true);

        assertThat(invitation.isAcceptable()).isTrue();
    }

    @Test
    @DisplayName("No debe ser aceptable si ha expirado")
    void shouldNotBeAcceptableIfExpired() {
        GoalInvitation invitation = new GoalInvitation();
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().minusHours(1));
        invitation.setActive(true);

        assertThat(invitation.isAcceptable()).isFalse();
    }

    @Test
    @DisplayName("No debe ser aceptable si no está activa")
    void shouldNotBeAcceptableIfInactive() {
        GoalInvitation invitation = new GoalInvitation();
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusHours(24));
        invitation.setActive(false);

        assertThat(invitation.isAcceptable()).isFalse();
    }

    @Test
    @DisplayName("No debe ser aceptable si no es PENDING")
    void shouldNotBeAcceptableIfNotPending() {
        GoalInvitation invitation = new GoalInvitation();
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setExpiresAt(LocalDateTime.now().plusHours(24));
        invitation.setActive(true);

        assertThat(invitation.isAcceptable()).isFalse();
    }

    @Test
    @DisplayName("markAsAccepted debe cambiar estado y fecha")
    void shouldMarkAsAccepted() {
        GoalInvitation invitation = new GoalInvitation();
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusHours(24));
        invitation.setActive(true);

        invitation.markAsAccepted();

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(invitation.getModifiedAt()).isNotNull();
    }
}
