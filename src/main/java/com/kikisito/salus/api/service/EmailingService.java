package com.kikisito.salus.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailingService {
    @Autowired
    private final JavaMailSender javaMailSender;

    @Autowired
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.from-header}")
    private String fromHeader;

    @Value("${APP_HOST}")
    private String host;

    public void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            // Ponemos la URL de la app como variable de Thymeleaf
            variables.put("appUrl", host);

            // Procesa la plantilla y las variables
            Context context = new Context();
            variables.forEach(context::setVariable);
            String content = templateEngine.process("mail/" + templateName, context);

            // Crea y envía el mensaje
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromHeader);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("No se ha podido enviar el email", e);
        }
    }
}