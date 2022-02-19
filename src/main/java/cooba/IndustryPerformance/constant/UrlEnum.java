package cooba.IndustryPerformance.constant;

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
    造紙("/taiex/30-pulp-and-paper-industry");

    UrlEnum(String url) {
        this.url = url;
    }

    private static final String baseUrl = "https://statementdog.com/";
    private String url;

    public String getUrl() {
        return baseUrl + url;
    }
}
