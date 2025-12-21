package org.teodor.api.client;

import io.restassured.RestAssured;
import org.teodor.config.ConfigManager;


public class ClientApiController {


    private static final String url = ConfigManager.getConfig().getClientApiUrl();


    public String getSchedule() {
        return RestAssured
                .given()
                .relaxedHTTPSValidation()
                .get(url)
                .asString();


    }
}
