package no.bankaxept.epayment.client.merchant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import no.bankaxept.epayment.client.base.SimulationRequest;

@JsonIgnoreProperties(value = {"simulationValues"})
public class SimulationCutOffRequest extends CutOffRequest implements SimulationRequest {

  private final List<String> simulationValues = new ArrayList<>();

  public List<String> getSimulationValues() {
    return simulationValues;
  }
}
