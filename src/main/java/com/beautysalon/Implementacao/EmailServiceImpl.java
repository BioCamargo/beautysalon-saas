package com.beautysalon.Implementacao;

import com.beautysalon.Inteface.EmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    @Override
    public void enviarLembrete(String destinatario, String nomeCliente) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinatario);
            message.setSubject("Lembrete Beauty Salon");
            message.setText("Olá " + nomeCliente + "!\n\nEste é um lembrete do seu agendamento conosco.");

            //mailSender.send(message);
            System.out.println("E-mail enviado para: " + destinatario);
        } catch (Exception e) {
            System.err.println("Erro ao enviar e-mail: " + e.getMessage());
            throw new RuntimeException("Falha ao enviar e-mail", e);
        }
    }

}
