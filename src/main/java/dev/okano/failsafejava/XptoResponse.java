package dev.okano.failsafejava;

public class XptoResponse {

    private final int statusCode;
    private final String response;

    public XptoResponse(int statusCode, String response) {
        this.statusCode = statusCode;
        this.response = response;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponse() {
        return response;
    }
}
