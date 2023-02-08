package no.bankaxept.epayment.client.tokenrequestor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import no.bankaxept.epayment.client.base.SimulationRequest;

@JsonIgnoreProperties(value = {"simulationValues"})
public class SimulationEnrolCardRequest extends EnrolCardRequest implements SimulationRequest {

  private List<String> simulationValues = new ArrayList<>();

  public SimulationEnrolCardRequest simulationValue(String simulationValue) {
    this.simulationValues.add(simulationValue);
    return this;
  }

  public SimulationEnrolCardRequest simulationValues(List<String> simulationValues) {
    this.simulationValues.addAll(simulationValues);
    return this;
  }

  public List<String> getSimulationValues() {
    return simulationValues;
  }

}
