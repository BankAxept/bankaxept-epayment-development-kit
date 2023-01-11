package no.bankaxept.epayment.client.base.accesstoken;


public interface Scope {
    String getValue();

    enum BankIDScope implements Scope {
        openid, ciba("permissions/ciba"), client("permissions/client"), approve_card("permissions/approve_card");

        private final String value;

        BankIDScope(String value) {
            this.value = value;
        }

        BankIDScope() {
            this.value = this.name();
        }

        @Override
        public String getValue() {
            return value;
        }
    }
}
