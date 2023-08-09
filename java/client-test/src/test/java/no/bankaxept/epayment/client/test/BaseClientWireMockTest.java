package no.bankaxept.epayment.client.test;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@WireMockTest
public @interface BaseClientWireMockTest {
}
