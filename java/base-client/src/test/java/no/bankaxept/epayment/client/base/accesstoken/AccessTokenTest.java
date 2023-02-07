package no.bankaxept.epayment.client.base.accesstoken;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class AccessTokenTest {

  private final Clock fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  @Test
  public void should_parse_json() throws IOException {
    var parsedToken = AccessToken.parse(readJsonFromFile("token-response.json"), fixedClock);
    assertThat(parsedToken.getToken()).isEqualTo("a-token");
    assertThat(parsedToken.getExpiry()).isEqualTo(fixedClock.instant().plusSeconds(3600));
  }

  @Test
  public void should_parse_json_different_order_other_fields() throws IOException {
    var parsedToken = AccessToken.parse(readJsonFromFile("token-response2.json"), fixedClock);
    assertThat(parsedToken.getToken()).isEqualTo("a-token");
    assertThat(parsedToken.getExpiry()).isEqualTo(fixedClock.instant().plusSeconds(3600));
  }


  @Test
  public void should_throw_if_unparsable() {
    assertThatThrownBy(() -> AccessToken.parse("garbage")).isInstanceOf(IllegalArgumentException.class);
  }

  private String readJsonFromFile(String filename) throws IOException {
    return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
        .getResource(filename)).getPath())));
  }

}
