import * as $ from "jquery";

export class DatalayerComponent {

    private dataLayerEndPoint: string;

    constructor() {
        this.dataLayerEndPoint = "/tracking/customer-connection-event";
    }

    init() {
        //@ts-ignore
        let timer = setInterval(() => {
            if (typeof trackCustomerConn != 'undefined' && trackCustomerConn) {
                $.get(`${this.dataLayerEndPoint}`, function (responseJSON) {
                    if (responseJSON) {
                        window.dataLayer = window.dataLayer || [];
                        window.dataLayer.push(responseJSON);
                    }
                });
                clearInterval(timer);
            }
        }, 50)
    }

}

