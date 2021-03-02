package xyz.kail.demo.spring.context.support.mail;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 *
 */
public class MailMain {

    public static void main(String[] args) throws IOException, MessagingException {
        Properties keys = new Properties();
        keys.load(MailMain.class.getResourceAsStream("/key.properties"));
        String username = keys.getProperty("mail.smtp.username");
        String password = keys.getProperty("mail.smtp.password");
        String receiver = keys.getProperty("mail.smtp.receiver");
        System.out.println(username);
        System.out.println(password);
        System.out.println(receiver);

        System.out.println(StandardCharsets.UTF_8.displayName());

        /*
         *
         */
        JavaMailSender javaMailSender = newMailSender(username, password);

        MimeMessage message = javaMailSender.createMimeMessage();
        message.setFrom(username);
        message.setRecipients(Message.RecipientType.TO, receiver);

        // 指定编码
        message.setSubject("【Test By Kail】中文主题", StandardCharsets.UTF_8.displayName());
        //
        message.setContent("中文主题内容", "text/html;charset=" + StandardCharsets.UTF_8.displayName());

//        javaMailSender.send(message);
    }

    private static JavaMailSender newMailSender(String username, String password) {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.from", username);
        properties.setProperty("mail.smtp.auth", "false");
        properties.setProperty("mail.smtp.starttls.enable", "false");
        properties.setProperty("mail.smtp.starttls.required", "false");
        properties.setProperty("mail.debug", "false");

        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost("smtp.exmail.qq.com");
        javaMailSender.setUsername(username);
        javaMailSender.setPassword(password);
        javaMailSender.setJavaMailProperties(properties);
        javaMailSender.setDefaultEncoding(StandardCharsets.UTF_8.displayName());
        return javaMailSender;
    }

}
