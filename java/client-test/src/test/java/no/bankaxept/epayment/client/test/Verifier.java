package no.bankaxept.epayment.client.test;

import no.bankaxept.epayment.client.base.ClientError;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.concurrent.Flow;

public final class Verifier {

  public static <T> void verifyBadRequest(Mono<T> publisher) {
    verifyBadRequest(publisher, "Missing field: messageId");
  }

  public static <T> void verifyBadRequest(Mono<T> publisher, String messageStart) {
    StepVerifier.create(publisher)
        .expectErrorMatches(
            t -> t instanceof ClientError && t.getMessage().startsWith(messageStart)
        )
        .verify();
  }

  public static <T> void verifyForbiddenRequest(Mono<T> publisher) {
    StepVerifier.create(publisher)
        .expectErrorMatches(t -> t instanceof ClientError && t.getMessage().equals(""))
        .verify();
  }

}
