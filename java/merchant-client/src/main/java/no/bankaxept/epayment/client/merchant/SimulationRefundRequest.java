package no.bankaxept.epayment.client.merchant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import no.bankaxept.epayment.client.base.SimulationRequest;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(value = {"simulationValues"})
public class SimulationRefundRequest extends RefundRequest implements SimulationRequest {

    private List<String> simulationValues = new ArrayList<>();

    public SimulationRefundRequest simulationValue(String simulationValue) {
        this.simulationValues.add(simulationValue);
        return this;
    }

    public SimulationRefundRequest simulationValues(List<String> simulationValues) {
        this.simulationValues.addAll(simulationValues);
        return this;
    }

    public List<String> getSimulationValues() {
        return simulationValues;
    }

}
