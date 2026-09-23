package com.beautysalon.Implementacao;


import com.beautysalon.Inteface.EmailService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class MockEmailService implements EmailService {

    @Override
    public void enviarLembrete(String destinatario, String nomeCliente) {
        System.out.println("\n=== E-MAIL SIMULADO ===");
        System.out.println("Para: " + destinatario);
        System.out.println("Assunto: Lembrete Beauty Salon");
        System.out.println("Corpo: Olá " + nomeCliente + "! Este é um lembrete simulado.");
        System.out.println("=======================\n");
    }
}
