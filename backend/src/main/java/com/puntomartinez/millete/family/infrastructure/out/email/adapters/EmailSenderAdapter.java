package com.puntomartinez.millete.family.infrastructure.out.email.adapters;

import com.puntomartinez.millete.family.domain.ports.out.EmailSenderPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailSenderAdapter implements EmailSenderPort {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${spring.mail.from:contact@millete.online}")
    private String fromEmail;

    public EmailSenderAdapter(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendInvitationEmail(String toEmail, String invitationToken) {
        String inviteLink = frontendUrl + "/join-family?token=" + invitationToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("¡Te han invitado a unirte a una Unidad Familiar!");
        message.setText("Hola,\n\n" +
                "Has sido invitado a unirte a una Unidad Familiar para ayudar a llevar el control de los gastos.\n" +
                "Por favor, haz clic en el siguiente enlace para aceptar la invitación y registrarte:\n\n" +
                inviteLink + "\n\n" +
                "Este enlace caducará en 24 horas.\n\n" +
                "El equipo de Family Budget.");
        mailSender.send(message);
    }
}