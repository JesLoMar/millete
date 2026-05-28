package com.puntomartinez.millete.family.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

@DisplayName("FamilyInvitation - Modelo de dominio")
class FamilyInvitationTest {

    @Test
    @DisplayName("Debe ser aceptable si PENDING y no expirada")
    void shouldBeAcceptable() {
        FamilyInvitation invitation = new FamilyInvitation();
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusHours(24));
        invitation.setActive(true);

        assertThat(invitation.isAcceptable()).isTrue();
    }

    @Test
    @DisplayName("No debe ser aceptable si ha expirado")
    void shouldNotBeAcceptableIfExpired() {
        FamilyInvitation invitation = new FamilyInvitation();
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().minusHours(1));
        invitation.setActive(true);

        assertThat(invitation.isAcceptable()).isFalse();
    }

    @Test
    @DisplayName("No debe ser aceptable si no está activa")
    void shouldNotBeAcceptableIfInactive() {
        FamilyInvitation invitation = new FamilyInvitation();
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusHours(24));
        invitation.setActive(false);

        assertThat(invitation.isAcceptable()).isFalse();
    }

    @Test
    @DisplayName("No debe ser aceptable si no es PENDING")
    void shouldNotBeAcceptableIfNotPending() {
        FamilyInvitation invitation = new FamilyInvitation();
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setExpiresAt(LocalDateTime.now().plusHours(24));
        invitation.setActive(true);

        assertThat(invitation.isAcceptable()).isFalse();
    }

    @Test
    @DisplayName("markAsAccepted debe cambiar estado y fecha")
    void shouldMarkAsAccepted() {
        FamilyInvitation invitation = new FamilyInvitation();
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusHours(24));
        invitation.setActive(true);

        invitation.markAsAccepted();

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(invitation.getModifiedAt()).isNotNull();
    }
}