package com.puntomartinez.millete.family.domain.ports.out;

public interface EmailSenderPort {
    void sendInvitationEmail(String toEmail, String invitationToken);
}