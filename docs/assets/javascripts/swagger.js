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
    const observer = new MutationObserver(function (mutationsList, observer) {
        Object.keys(swaggerUrls).forEach(id => {
            const swaggerElement = document.getElementById(id);
            if (swaggerElement !== null && !swaggerElement.hasChildNodes()) {
                loadSwaggerUIBundle(id, swaggerUrls[id]);
            }
        });
    });
    const targetNode = document.body;
    const config = {childList: true, subtree: true};
    observer.observe(targetNode, config);
});
