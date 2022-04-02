package cooba.IndustryPerformance.enums;

public enum UrlEnum {
    //財報狗
    智慧電網("/taiex/36-smart-grid-industry"),
    太陽能("/taiex/25-solar-energy-industry"),
    電池("/taiex/23-battery-industry"),
    電動車("/taiex/20-electric-vehicle-industry"),
    鋼鐵("/taiex/24-steel-industry"),
    交通航運("/taiex/3-transportation-industry"),
    風力發電("/taiex/37-風力發電"),
    LED照明("/taiex/22-led-industry"),
    建材營造("/taiex/2-construction-industry"),
    製藥("/taiex/26-pharmaceutical-industry"),
    金融("/taiex/4-financial-services-industry"),
    汽電共生("/taiex/35-汽電共生"),
    PCB("/taiex/8-pcb-industry"),
    其他("/taiex/6-other-industries"),
    造紙("/taiex/30-pulp-and-paper-industry"),
    平面顯示器("/taiex/13-flat-panel-display-industry"),
    半導體("/taiex/19-semiconductor-industry"),
    紡織("/taiex/10-textile-industry"),
    電腦及週邊設備("/taiex/12-computer-and-peripheral-equipment-manufacturing-industry"),
    醫療器材("/taiex/18-medical-device-industry"),
    電機機械("/taiex/11-machinery-industry"),
    石化及塑橡膠("/taiex/9-petrochemical-industry"),
    食品("/taiex/5-food-industry"),
    汽車("/taiex/15-car-industry"),
    通信網路("/taiex/14-network-communication-industry"),
    軟體服務("/taiex/32-software-industry"),
    水泥("/taiex/1-cement-industry"),
    連接器("/taiex/16-connector-industry"),
    觸控面板("/taiex/28-touch-panel-industry"),
    油電燃氣("/taiex/21-oil-gas-and-electric-industry"),
    被動元件("/taiex/31-passive-component-industry"),
    休閒娛樂("/taiex/17-entertainment-industry"),
    食品生技("/taiex/27-food-biotechnology-industry"),
    電子商務("/taiex/34-e-commerce-industry"),
    再生醫療("/taiex/29-regenerative-medicine-industry"),
    貿易百貨("/taiex/7-retail-industry"),
    文化創意("/taiex/33-cultural-and-creative-industry");

    UrlEnum(String url) {
        this.url = url;
    }

    private static final String baseUrl = "https://statementdog.com/";
    private String url;

    public String getUrl() {
        return baseUrl + url;
    }
}
