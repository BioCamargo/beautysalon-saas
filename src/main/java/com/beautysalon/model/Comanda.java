package com.beautysalon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Comanda de Atendimento do Cliente, contendo Serviços, Produtos de Revenda e Comissões.
 */
@Entity
@Table(name = "comandas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"empresa", "caixa", "cliente", "itens"})
@EqualsAndHashCode(of = "id")
public class Comanda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String numeroComanda; // Ex: CMD-00123

    @Column(nullable = false)
    private LocalDateTime dataAbertura;

    private LocalDateTime dataFechamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente; // Pode ser nulo para venda de balcão anônima

    private String clienteNomeAvulso; // Caso não tenha cadastro completo

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caixa_id")
    private Caixa caixa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_id")
    private Agendamento agendamento; // Se originado de um agendamento

    @Builder.Default
    private BigDecimal subtotalServicos = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal subtotalProdutos = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal desconto = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal acrescimo = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal totalComissoes = BigDecimal.ZERO;

    private String formaPagamento; // DINHEIRO, PIX, CARTAO_CREDITO, CARTAO_DEBITO, VOUCHER, MULTIPLO

    @Column(nullable = false)
    @Builder.Default
    private String status = "ABERTA"; // ABERTA, PAGA, CANCELADA

    private String cupomAplicado;

    private String observacoes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"comandas"})
    private Empresa empresa;

    @OneToMany(mappedBy = "comanda", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ComandaItem> itens = new ArrayList<>();

    public void recalcularTotais() {
        BigDecimal servicos = BigDecimal.ZERO;
        BigDecimal produtos = BigDecimal.ZERO;
        BigDecimal comissoes = BigDecimal.ZERO;

        for (ComandaItem item : itens) {
            if ("SERVICO".equals(item.getTipo())) {
                servicos = servicos.add(item.getValorTotal());
            } else {
                produtos = produtos.add(item.getValorTotal());
            }
            if (item.getValorComissao() != null) {
                comissoes = comissoes.add(item.getValorComissao());
            }
        }

        this.subtotalServicos = servicos;
        this.subtotalProdutos = produtos;
        this.totalComissoes = comissoes;

        BigDecimal total = servicos.add(produtos);
        if (desconto != null) {
            total = total.subtract(desconto);
        }
        if (acrescimo != null) {
            total = total.add(acrescimo);
        }
        this.valorTotal = total.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total;
    }
}
