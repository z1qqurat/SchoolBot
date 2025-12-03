package org.teodor.api;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class AbstractController {
    protected RequestSpecification getRS() {
        return RestAssured.with();
    }

    protected Response GET(String url, RequestSpecification rs) {
        return executeRequest(Method.GET, url, rs);
    }

    protected Response POST(String url, RequestSpecification rs) {
        return executeRequest(Method.POST, url, rs);
    }

    private Response executeRequest(Method method, String url, RequestSpecification rs) {
        return rs.request(method, url);
    }
}
