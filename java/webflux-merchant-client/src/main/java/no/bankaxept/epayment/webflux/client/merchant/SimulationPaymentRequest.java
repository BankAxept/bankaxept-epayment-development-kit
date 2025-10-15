package no.bankaxept.epayment.webflux.client.merchant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import no.bankaxept.epayment.client.base.SimulationRequest;

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
