export class Component {
    constructor() {}

    private disableButton() {
        this.fetchInitFromDataLayer()
    }

    private fetchInitFromDataLayer() {
        const init = {};

        // @ts-ignore : dl not in scope of TS
        Object.assign(init, dataLayer.find(el => el.event == "initialization"));
        init.event = "virtualPageView";
        delete init["gtm.uniqueEventId"];
        // @ts-ignore : datalayer not in scope of TS
        dataLayer.push(init);
    }

}
