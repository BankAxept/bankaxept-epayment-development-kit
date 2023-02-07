package no.bankaxept.epayment.test.client;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@WireMockTest
public @interface BaseClientWireMockTest {
}
