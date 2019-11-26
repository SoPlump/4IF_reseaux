package fr.reseaux.httpserver.server;

public class Response {

    public enum ResponseCode {

        STATUS_404(404),
        STATUS_200(200),
        STATUS_500(500);

        int statusCode = 0;

        ResponseCode(int statusCode) {
            this.statusCode = statusCode;
        }
    }

    private ResponseCode statusCode;

    private String responseHeader;

    private String responseBody;


}
