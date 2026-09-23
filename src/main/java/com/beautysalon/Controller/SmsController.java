package com.beautysalon.Controller;

import com.beautysalon.service.SmsService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/sms")
public class SmsController
{
    @Autowired
    private SmsService smsService;

    @PostMapping("/enviar")
    public ResponseEntity<String> enviarSms(@RequestParam @NotBlank String telefone,
                                            @RequestParam @NotBlank @Size(max = 160) String mensagem) {
        try {
            String resposta = smsService.enviarSms(telefone, mensagem);
            return ResponseEntity.ok(resposta);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Falha ao enviar SMS: " + e.getMessage());
        }
    }
}
