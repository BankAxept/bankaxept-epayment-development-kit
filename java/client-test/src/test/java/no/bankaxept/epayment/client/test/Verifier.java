package no.bankaxept.epayment.client.test;

import no.bankaxept.epayment.client.base.ClientError;
import reactor.adapter.JdkFlowAdapter;
import reactor.test.StepVerifier;
import java.util.concurrent.Flow;

public final class Verifier {

  public static <T> void verifyBadRequest(Flow.Publisher<T> publisher) {
    verifyBadRequest(publisher, "Illegal value for parameter: ");
  }

  public static <T> void verifyBadRequest(Flow.Publisher<T> publisher, String messageStart) {
    StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(publisher))
        .expectErrorMatches(
            t -> t instanceof ClientError && t.getMessage().startsWith(messageStart)
        )
        .verify();
  }

  public static <T> void verifyForbiddenRequest(Flow.Publisher<T> publisher) {
    StepVerifier.create(JdkFlowAdapter.flowPublisherToFlux(publisher))
        .expectErrorMatches(
            t -> t instanceof ClientError && t.getMessage().equals("")
        )
        .verify();
  }

}
