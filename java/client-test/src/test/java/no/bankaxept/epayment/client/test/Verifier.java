package no.bankaxept.epayment.client.test;

import reactor.adapter.JdkFlowAdapter;
import reactor.test.StepVerifier;
import java.util.concurrent.Flow;

public final class Verifier {

  public static <T> void verifyBadRequest(Flow.Publisher<T> publisher) {
    StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(publisher))
        .expectErrorMatches(
            t -> t instanceof IllegalArgumentException && t.getMessage().startsWith("Illegal value for parameter: ")
        )
        .verify();
  }

  public static <T> void verifyForbiddenRequest(Flow.Publisher<T> publisher) {
    StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(publisher))
        .expectErrorMatches(
            t -> t instanceof IllegalArgumentException && t.getMessage().equals("")
        )
        .verify();
  }

}
