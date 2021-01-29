package com.events.eventsmanagement.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;

@Component
public class EmailSenderImpl {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendMail(String receiver, String subject, String content) {

        SimpleMailMessage msg = new SimpleMailMessage();

        msg.setTo(receiver);
        msg.setSubject(subject);
        msg.setText(content);

        javaMailSender.send(msg);
    }

    public void sendMailWithAttachements(String receiver, String fileToAttach) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(receiver);
        helper.setText("<html><body><h1>Your Receipt!</h1><body></html>", true);
        FileSystemResource file = new FileSystemResource(new File(fileToAttach));
        helper.addAttachment("receipt.xlsx", file);
        helper.setSubject("Your Bought Items List");
        javaMailSender.send(message);
    }

}
