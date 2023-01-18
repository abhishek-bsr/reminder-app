package com.reminder;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

public class Handler {
    private JSONObject responseBuilder(Object value) {
        JSONObject responseObject = new JSONObject();
        responseObject.put("reminders", value);

        return responseObject;
    }

    protected void ErrorResponseHandler(String errorMessage, int statusCode, HttpServletResponse response,
            PrintWriter output) {
        JSONObject errorObjects = new JSONObject();
        errorObjects.put("status_code", HttpServletResponse.SC_BAD_REQUEST);
        errorObjects.put("error", errorMessage);

        response.setStatus(statusCode);
        JSONObject responseObject = responseBuilder(errorObjects);
        output.println(responseObject);
    }

    protected void SuccessResponseHandler(Object successMessage, int statusCode, HttpServletResponse response,
            PrintWriter output) {
        response.setStatus(statusCode);
        JSONObject responseObject = responseBuilder(successMessage);
        output.println(responseObject);
    }
}
