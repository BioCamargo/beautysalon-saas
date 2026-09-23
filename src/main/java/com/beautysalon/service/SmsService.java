package com.beautysalon.service;


import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SmsService {

    //URL de uma API de SMS fake para testes (pode substituir por uma real depois)
    private static final String SMS_API_URL = "https://run.mocky.io/v3/8eb88272-d769-448c-ae73-8d6a6e5f8a6a";

    private final OkHttpClient httpClient = new OkHttpClient();

    public String enviarSms(String telefone, String mensagem) throws IOException {
        // 1. Criar o corpo da requisição (JSON)
        String json = String.format(
                "{\"to\":\"%s\",\"message\":\"%s\"}",
                telefone, mensagem
        );

        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        // 2. Criar a requisição HTTP POST
        Request request = new Request.Builder()
                .url(SMS_API_URL)
                .post(body)
                .build();

        // 3. Enviar a requisição e processar a resposta
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erro na API: " + response.code());
            }
            return response.body().string(); // Resposta da API (ex: "SMS enviado")
        }
    }
}
