package com.beautysalon.DTO.billing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsaasWebhookDTO {

    private String id;
    private String event;
    private String dateCreated;
    private AsaasPaymentDTO payment;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AsaasPaymentDTO {
        private String id;
        private String customer;
        private String subscription;
        private String installment;
        private BigDecimal value;
        private BigDecimal netValue;
        private String billingType;
        private String status;
        private LocalDate dueDate;
        private LocalDate paymentDate;
        private String invoiceUrl;
        private String bankSlipUrl;
        private String externalReference;
    }
}
