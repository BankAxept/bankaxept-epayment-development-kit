module no.bankaxept.epayment.client.test {
    requires org.junit.jupiter.api;
    requires no.bankaxept.epayment.sdk.baseclient;
    requires wiremock.jre8;
    requires org.assertj.core;
    exports no.bankaxept.client.test;
}