const swaggerUrls = {
    "swagger-integrator-merchant-bankaxept": {
        name: "BankAxept ePayment Merchant API - ePayment Platform",
        url: "https://raw.githubusercontent.com/BankAxept/bankaxept-epayment-development-kit/main/openapi/integrator/merchant/bankaxept.yaml"
    },
    "swagger-integrator-partner-bankaxept": {
        name: "BankAxept ePayment Merchant API - Merchant Server",
        url: "https://raw.githubusercontent.com/BankAxept/bankaxept-epayment-development-kit/main/openapi/integrator/merchant/partner.yaml"
    },
    "swagger-epp-components": {
        name: "BankAxept ePayment platform components",
        url: "https://raw.githubusercontent.com/BankAxept/bankaxept-epayment-development-kit/main/openapi/integrator/components.yaml"
    },
    "swagger-integrator-token-requestor-callback": {
        name: "BankAxept ePayment Token Requestor API - Merchant Server",
        url: "https://raw.githubusercontent.com/BankAxept/bankaxept-epayment-development-kit/main/openapi/integrator/token-requestor/partner.yaml"
    },
    "swagger-integrator-token-requestor-bankaxept": {
        name: "BankAxept ePayment Token Requestor API - ePayment Server",
        url: "https://raw.githubusercontent.com/BankAxept/bankaxept-epayment-development-kit/main/openapi/integrator/token-requestor/bankaxept.yaml"
    },
    "swagger-integrator-accesstoken-bankaxept": {
        name: "BankAxept ePayment Authentication components",
        url: "https://raw.githubusercontent.com/BankAxept/bankaxept-epayment-development-kit/main/openapi/access-token/bankaxept.yaml"
    }
}

const loadSwaggerUIBundle = function (id, url) {
    window.ui = SwaggerUIBundle({
        url: url.url,
        dom_id: "#" + id,
        presets: [
            SwaggerUIBundle.presets.apis,
            SwaggerUIBundle.SwaggerUIStandalonePreset
        ]
    });
}

document.addEventListener('DOMContentLoaded', function () {
    const loadAllSwaggerUIBundles = function () {
        Object.keys(swaggerUrls).forEach(id => {
            const swaggerElement = document.getElementById(id);
            if (swaggerElement !== null && !swaggerElement.hasChildNodes()) {
                loadSwaggerUIBundle(id, swaggerUrls[id]);
            }
        });
    };

    // Initial load
    loadAllSwaggerUIBundles();

    // Observe for future changes
    const observer = new MutationObserver(function (mutationsList, observer) {
        loadAllSwaggerUIBundles();
    });

    const targetNode = document.body;
    const config = {childList: true, subtree: true};
    observer.observe(targetNode, config);
});
