package com.backend.escort.helpers.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;


// Reads data from the application.properties
@Configuration
public class EmailConfig {

    @Value("${mail.protocol}")
    private String protocol;
    @Value("${mail.smtp.host}")
    private String host;
    @Value("${mail.smtp.port}")
    private Integer port;
    @Value("${mail.support.username}")
    private String name;
    @Value("${mail.support.password}")
    private String password;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl jms = new JavaMailSenderImpl();
        jms.setUsername(name);
        jms.setPassword(password);
        jms.setProtocol(protocol);
        jms.setHost(host);
        jms.setPort(port);
        jms.setJavaMailProperties(properties());
        return jms;
    }

    private Properties properties() {
        Properties p = new Properties();
        p.setProperty("mail.smtp.auth", "true");
        p.setProperty("mail.smtp.starttls.enable", "true");
        p.setProperty("mail.debug", "false");
/*        p.setProperty("input.encoding", "UTF-8");
        p.setProperty("output.encoding", "UTF-8");*/
        p.setProperty("resource.loader", "class");
        p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        return p;
    }



}