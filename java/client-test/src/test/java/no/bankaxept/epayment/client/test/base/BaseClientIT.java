package no.bankaxept.epayment.client.test.base;

import no.bankaxept.epayment.client.base.BaseClient;
import java.net.MalformedURLException;
import java.net.URL;

public class BaseClientIT {

  private BaseClient testClient() throws MalformedURLException {
    return new BaseClient.Builder(new URL("https://api.epp-stoetest.cloud/access-token/v1/accesstoken"))
        .withScheduledToken(
            new URL("https://api.epp-stoetest.cloud/access-token/v1/accesstoken"),
            System.getenv("CLIENT_ID"),
            System.getenv("CLIENT_SECRET")
        ).build();
  }

}
