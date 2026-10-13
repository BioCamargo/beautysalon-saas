package com.beautysalon.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO contendo o relatório de execução da importação em lote de clientes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportacaoResultadoDTO {

    private int totalLinhasLidas;
    private int totalImportados;
    private int totalDuplicadosIgnorados;
    private int totalErros;

    @Builder.Default
    private List<String> alertas = new ArrayList<>();

    public boolean isSucessoGeral() {
        return totalImportados > 0;
    }
}
