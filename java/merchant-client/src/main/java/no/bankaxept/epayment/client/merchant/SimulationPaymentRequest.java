package no.bankaxept.epayment.client.merchant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import no.bankaxept.epayment.client.base.SimulationRequest;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(value = {"simulationValues"})
public class SimulationPaymentRequest extends PaymentRequest implements SimulationRequest {

    private List<String> simulationValues = new ArrayList<>();

    public SimulationPaymentRequest(PaymentRequest paymentRequest) {
        this.inStore(paymentRequest.isInStore());
        this.amount(paymentRequest.getAmount());
        this.amountBreakdown(paymentRequest.getAmountBreakdown());
        this.automaticCapture(paymentRequest.isAutomaticCapture());
        this.encryptedCardholderAuthenticationData(paymentRequest.getEncryptedCardholderAuthenticationData());
        this.merchantAggregatorId(paymentRequest.getMerchantAggregatorId());
        this.merchantId(paymentRequest.getMerchantId());
        this.merchantName(paymentRequest.getMerchantName());
        this.merchantDisplayName(paymentRequest.getMerchantDisplayName());
        this.merchantOrderReference(paymentRequest.getMerchantOrderReference());
        this.merchantOrderMessage(paymentRequest.getMerchantOrderMessage());
        this.merchantReference(paymentRequest.getMerchantReference());
        this.messageId(paymentRequest.getMessageId());
        this.inStore(paymentRequest.isInStore());
        this.transactionTime(paymentRequest.getTransactionTime());

    }

    public SimulationPaymentRequest simulationValue(String simulationValue) {
        this.simulationValues.add(simulationValue);
        return this;
    }

    public SimulationPaymentRequest simulationValues(List<String> simulationValues) {
        this.simulationValues.addAll(simulationValues);
        return this;
    }

    public List<String> getSimulationValues() {
        return simulationValues;
    }

}
