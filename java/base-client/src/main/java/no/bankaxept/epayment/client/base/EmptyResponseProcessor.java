package no.bankaxept.epayment.client.base;

import no.bankaxept.epayment.client.base.http.HttpResponse;

import java.util.concurrent.Flow;

public class EmptyResponseProcessor extends MapOperator<HttpResponse, Response> {
    public EmptyResponseProcessor(Flow.Publisher<HttpResponse> publisher) {
        super(publisher, httpResponse -> httpResponse.getStatus().toResponse());
    }
}
