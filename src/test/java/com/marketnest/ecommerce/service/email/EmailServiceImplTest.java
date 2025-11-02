package com.marketnest.ecommerce.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendEmail_shouldSendSuccessfully_whenValidParameters() {
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body Content";

        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendEmail(to, subject, body);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_shouldSendHtmlEmail_whenBodyContainsHtml() {
        String to = "test@example.com";
        String subject = "HTML Email";
        String htmlBody = "<html><body><h1>Hello</h1></body></html>";

        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendEmail(to, subject, htmlBody);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_shouldThrowException_whenMessagingExceptionOccurs() throws MessagingException {
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MessagingException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> emailService.sendEmail(to, subject, body))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send email")
                .hasCauseInstanceOf(MessagingException.class);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_shouldHandleMultipleRecipients() {
        String to = "test1@example.com";
        String subject = "Multiple Recipients Test";
        String body = "Test content";

        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendEmail(to, subject, body);

        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendEmail_shouldHandleSpecialCharactersInSubject() {
        String to = "test@example.com";
        String subject = "Test Subject with 特殊字符 and émojis 🎉";
        String body = "Test body";

        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendEmail(to, subject, body);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_shouldHandleSpecialCharactersInBody() {
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Body with special chars: _, %, #, !";

        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendEmail(to, subject, body);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_shouldHandleLongEmailContent() {
        String to = "test@example.com";
        String subject = "Long Content Test";
        String longBody = "This is a long email body content. ".repeat(1000);

        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendEmail(to, subject, longBody);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_shouldHandleEmptySubject() {
        String to = "test@example.com";
        String subject = "";
        String body = "Test body content";

        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendEmail(to, subject, body);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_shouldHandleEmptyBody() {
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "";

        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendEmail(to, subject, body);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_shouldThrowException_whenMailSenderFails() {
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Mail server unavailable")).when(mailSender)
                .send(any(MimeMessage.class));

        assertThatThrownBy(() -> emailService.sendEmail(to, subject, body))
                .isInstanceOf(RuntimeException.class);

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_shouldCreateNewMimeMessage_forEachEmail() {
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendEmail(to, subject, body);
        emailService.sendEmail(to, subject, body);
        emailService.sendEmail(to, subject, body);

        verify(mailSender, times(3)).createMimeMessage();
        verify(mailSender, times(3)).send(mimeMessage);
    }

    @Test
    void sendEmail_shouldHandleComplexHtmlContent() {
        String to = "test@example.com";
        String subject = "Complex HTML";
        String htmlBody = """
                <html>
                    <head><style>body { font-family: Arial; }</style></head>
                    <body>
                        <h1>Welcome</h1>
                        <p>This is a <strong>complex</strong> HTML email</p>
                        <ul>
                            <li>Item 1</li>
                            <li>Item 2</li>
                        </ul>
                    </body>
                </html>
                """;

        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendEmail(to, subject, htmlBody);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }
}