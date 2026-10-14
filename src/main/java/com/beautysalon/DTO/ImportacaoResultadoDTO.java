package com.beautysalon.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO contendo o relatório de execução da importação em lote de clientes.
 */
@Data
@Builder
public class ImportacaoResultadoDTO {

    private int totalLinhasLidas;
    private int totalImportados;
    private int totalDuplicadosIgnorados;
    private int totalErros;

    @Builder.Default
    private List<String> alertas = new ArrayList<>();

    public ImportacaoResultadoDTO() {
        this.alertas = new ArrayList<>();
    }

    public ImportacaoResultadoDTO(int totalLinhasLidas, int totalImportados, int totalDuplicadosIgnorados, int totalErros, List<String> alertas) {
        this.totalLinhasLidas = totalLinhasLidas;
        this.totalImportados = totalImportados;
        this.totalDuplicadosIgnorados = totalDuplicadosIgnorados;
        this.totalErros = totalErros;
        this.alertas = alertas != null ? alertas : new ArrayList<>();
    }

    public boolean isSucessoGeral() {
        return totalImportados > 0;
    }
}
