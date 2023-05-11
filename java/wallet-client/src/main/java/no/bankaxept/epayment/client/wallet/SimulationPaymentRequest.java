package no.bankaxept.epayment.client.wallet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import no.bankaxept.epayment.client.base.SimulationRequest;
import no.bankaxept.epayment.client.wallet.bankaxept.PaymentRequest;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(value = {"simulationValues"})
public class SimulationPaymentRequest extends PaymentRequest implements SimulationRequest {

  private final List<String> simulationValues = new ArrayList<>();

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
