package com.reminder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

@WebServlet(name = "Servlet", urlPatterns = "/api/v1/reminders/*")
public class Servlet extends HttpServlet {
    private static Utils utility = new Utils();
    private static Handler handler = new Handler();

    private static String CHARACTER_ENCODING = "UTF-8";
    private static String CONTENT_TYPE = "application/json";
    private static int UNPROCESSABLE_ENTITY = 422;

    private static JSONArray reminderList = new JSONArray();
    private static int counter = 0;

    /*
     * @method GET /reminders           list all reminders
     * @method GET /reminders/${id}     list reminder with corresponding <id>
     *
     * @param <limit>                   limit number of rows from query
     * @param <offset>                  omit specified number of rows from query
     * @param <completed>               get corresponding flag from query
     * @param <important>               get corresponding flag from query
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        PrintWriter output = response.getWriter();
        response.setCharacterEncoding(CHARACTER_ENCODING);
        response.setContentType(CONTENT_TYPE);

        // check path URI
        String requestId = request.getPathInfo();
        try {
            int limitParam = request.getParameter("limit") == null ? 10
                    : Integer.parseInt(request.getParameter("limit"));
            int offsetParam = request.getParameter("offset") == null ? 0
                    : Integer.parseInt(request.getParameter("offset"));

            Boolean completedParam = request.getParameter("completed") == null ? null
                    : Boolean.parseBoolean(request.getParameter("completed"));
            Boolean importantParam = request.getParameter("important") == null ? null
                    : Boolean.parseBoolean(request.getParameter("important"));

            if (requestId == null || requestId.length() == 1) {
                JSONArray reminders = new JSONArray();
                limitParam = utility.adjustParamlengthIfRequired(reminderList.length(), limitParam);
                offsetParam = utility.adjustParamlengthIfRequired(reminderList.length(), offsetParam);

                for (; offsetParam < reminderList.length() && limitParam > 0; offsetParam++) {
                    Boolean isCompleted = reminderList.getJSONObject(offsetParam).getBoolean("is_completed");
                    Boolean isImportant = reminderList.getJSONObject(offsetParam).getBoolean("is_important");
                    
                    limitParam = utility.addRemindersAgainstParams(reminderList.getJSONObject(offsetParam), reminders,
                            completedParam, importantParam, isCompleted, isImportant, offsetParam, limitParam);
                }

                handler.SuccessResponseHandler(reminders, HttpServletResponse.SC_OK, response, output);
            } else {
                // GET request for 1 reminder only
                requestId = requestId.replace("/", ""); // omit '/' from path to convert into proper <id> field

                int getObjectIndex = utility.checkReminderIdExists(reminderList, Integer.parseInt(requestId));
                if (getObjectIndex != -1) {
                    // data exists in JSONArray
                    handler.SuccessResponseHandler(reminderList.getJSONObject(getObjectIndex),
                            HttpServletResponse.SC_OK, response, output);
                } else {
                    // data not found
                    String errorMessage = String.format("Reminder id " + "<" + requestId + ">" + " not found");
                    handler.ErrorResponseHandler(errorMessage.toString(), UNPROCESSABLE_ENTITY, response, output);
                }
            }
        } catch (Exception error) {
            // error occuring during <Integer> or <Boolean> parsing
            handler.ErrorResponseHandler(error.toString(), UNPROCESSABLE_ENTITY, response, output);
        }
    }

    /*
     * @method DELETE /reminders            delete all reminders
     * @method DELETE /reminders/${id}      delete a reminder corresponging to <id>
     */
    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        PrintWriter output = response.getWriter();
        response.setCharacterEncoding(CHARACTER_ENCODING);
        response.setContentType(CONTENT_TYPE);

        // check path URI
        String requestId = request.getPathInfo();
        if (requestId != null && requestId.length() != 1) {

            requestId = requestId.replace("/", ""); // omit '/' from path to convert into proper <id> field
            int getObjectIndex = utility.checkReminderIdExists(reminderList, Integer.parseInt(requestId));
            if (getObjectIndex == -1) {
                // data not found
                String errorMessage = String.format("Reminder id " + "<" + requestId + ">" + " not found");
                handler.ErrorResponseHandler(errorMessage, HttpServletResponse.SC_NOT_FOUND, response, output);
            } else {
                int reminderId = reminderList.getJSONObject(getObjectIndex).getInt("id");
                reminderList.remove(getObjectIndex);

                String info = String.format("Reminder <" + reminderId + ">" + " has been deleted");
                JSONObject reminderIdObject = new JSONObject().put("message", info);
                handler.SuccessResponseHandler(reminderIdObject, HttpServletResponse.SC_OK, response, output);
            }
        } else {
            ArrayList<Integer> reminderIds = new ArrayList<>(); // get all <id>
            for (int i = 0; i < reminderList.length(); i++) {
                Integer reminderId = reminderList.getJSONObject(i).getInt("id");
                reminderIds.add(reminderId);
            }
            reminderList = new JSONArray(); // blank JSONArray list (erase all data)
            counter = 0; // empty count

            String info = String.format("Reminder <" + reminderIds.toString() + ">" + " have been deleted");
            JSONObject reminderIdsObject = new JSONObject().put("message", info);
            handler.SuccessResponseHandler(reminderIdsObject, HttpServletResponse.SC_OK, response, output);
        }
    }

    /*
     * @method POST /reminders      add new data
     * 
     * @field name                  String  REQUIRED
     * @field tag_color             String  OPTIONAL
     * @field is_completed          Boolean OPTIONAL
     * @field is_important          Boolean OPTIONAL
     * @field reminder_utc          String  REQUIRED
     * @field frequency             String  OPTIONAL
     * @field note                  String  OPTIONAL
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        PrintWriter output = response.getWriter();
        response.setCharacterEncoding(CHARACTER_ENCODING);
        response.setContentType(CONTENT_TYPE);

        String requestBody = request.getReader().lines().collect(Collectors.joining());
        try {
            JSONObject requestJsonObject = new JSONObject(requestBody).getJSONObject("reminders");

            // check <name> field in request body
            if (requestJsonObject.has("name")) {
                String reminderName = requestJsonObject.getString("name");
                String tagColor = requestJsonObject.has("tag_color") != false ? requestJsonObject.getString("tag_color")
                        : "#f1f1f1";
                Boolean isCompleted = requestJsonObject.has("is_completed") != false
                        ? requestJsonObject.getBoolean("is_completed")
                        : false;
                Boolean isImportant = requestJsonObject.has("is_important") != false
                        ? requestJsonObject.getBoolean("is_important")
                        : false;
                Object reminderNote = requestJsonObject.has("note") != false ? requestJsonObject.getString("note")
                        : JSONObject.NULL;

                // String to enum <Frequency>
                try {
                    Instant reminderUtc = Instant.parse(requestJsonObject.getString("reminder_utc"));
                    Frequency frequency = requestJsonObject.has("frequency") != false
                            ? Frequency.valueOf(requestJsonObject.getString("frequency"))
                            : Frequency.DAILY;

                    int getIndex = utility.checkReminderNameExists(reminderList, reminderName);
                    if (getIndex != -1) {
                        utility.addExistingData(reminderList, getIndex, tagColor, isCompleted, isImportant, reminderUtc, frequency, reminderNote);

                        String info = String.format("Reminder <"
                                + reminderList.getJSONObject(getIndex).getInt("id") + ">" + " has been added");
                        JSONObject reminderIdObject = new JSONObject().put("message", info);
                        handler.SuccessResponseHandler(reminderIdObject, HttpServletResponse.SC_CREATED, response,
                                output);
                    } else {
                        // create new object <Model>
                        Model reminder = new Model(++counter, reminderName, tagColor, isCompleted,
                                isImportant, reminderUtc, frequency, reminderNote);

                        // store in JSONArray pool
                        utility.storeDataInList(reminderList, reminder.getUuid(), reminderName, tagColor,
                                isCompleted, isImportant,
                                reminder.getReminderUtc().toString(), frequency, reminderNote);

                        // object to display for successful POST request
                        String info = String
                                .format("Reminder <" + reminder.getUuid() + ">" + " has been added");
                        JSONObject reminderIdObject = new JSONObject().put("message", info);
                        handler.SuccessResponseHandler(reminderIdObject, HttpServletResponse.SC_CREATED, response,
                                output);
                    }
                } catch (Exception error) {
                    // unable to parse into <Frequency> enum
                    handler.ErrorResponseHandler(error.toString(), UNPROCESSABLE_ENTITY, response, output);
                }
            } else {
                // <name> field in body
                String errorMessage = "Missing <name> field in request body";
                handler.ErrorResponseHandler(errorMessage, UNPROCESSABLE_ENTITY, response, output);
            }
        } catch (Exception error) {
            // Unable to parse into JSONObject
            handler.ErrorResponseHandler(error.toString(), HttpServletResponse.SC_BAD_REQUEST, response, output);
        }
    }

    /*
     * @method PUT /reminders/${id}     update existing data corresponding to <id>
     * 
     * @field tag_color                 String  OPTIONAL
     * @field is_completed              Boolean OPTIONAL
     * @field is_important              Boolean OPTIONAL
     * @field reminder_utc              String  OPTIONAL
     * @field frequency                 String  OPTIONAL
     * @field note                      String  OPTIONAL
     */
    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        PrintWriter output = response.getWriter();
        response.setCharacterEncoding(CHARACTER_ENCODING);
        response.setContentType(CONTENT_TYPE);

        // check path URI
        String requestId = request.getPathInfo();
        if (requestId != null && requestId.length() != 1) {

            requestId = requestId.replace("/", ""); // omit '/' from path to convert into proper <id> field
            int getObjectIndex = utility.checkReminderIdExists(reminderList, Integer.parseInt(requestId));

            if (getObjectIndex == -1) {
                // <id> not found
                String errorMessage = String.format("Reminder id " + "<" + requestId + ">" + " not found");
                handler.ErrorResponseHandler(errorMessage, HttpServletResponse.SC_NOT_FOUND, response, output);
            } else {
                String requestBody = request.getReader().lines().collect(Collectors.joining());
                try {
                    JSONObject requestJsonObject = new JSONObject(requestBody).getJSONObject("reminders");
                    JSONObject reminder = reminderList.getJSONObject(getObjectIndex);

                    for (String key : requestJsonObject.keySet()) {
                        if (key.equals("name") || key.equals("id"))
                            continue;

                        if (key.equals("reminder_utc")) {
                            Instant updatedReminderUtc = utility.getUtcTime(requestJsonObject.getString(key));
                            if (updatedReminderUtc == null) {
                                String errorMessage = String.format("Field <reminder_utc> is not in proper form");
                                handler.ErrorResponseHandler(errorMessage, UNPROCESSABLE_ENTITY, response, output);
                                return;
                            } else
                                reminder.put(key, requestJsonObject.get(key));
                        } else
                            reminder.put(key, requestJsonObject.get(key));
                    }
                    reminderList.put(getObjectIndex, reminder); // replace old JSONObject with new JSONObject built in
                                                                // JSONArray <reminderList>
                    int reminderId = reminder.getInt("id"); // get <id> from JSONObject

                    String info = String.format("Reminder <" + reminderId + ">" + " has been updated");
                    JSONObject reminderIdObject = new JSONObject().put("message", info);
                    handler.SuccessResponseHandler(reminderIdObject, HttpServletResponse.SC_OK, response, output);
                } catch (Exception error) {
                    // Unable to parse into JSONObject
                    handler.ErrorResponseHandler(error.toString(), HttpServletResponse.SC_BAD_REQUEST, response,
                            output);
                }
            }
        } else {
            // error in parameter
            String errorMessage = "Reminder <id> not provided as parameter";
            handler.ErrorResponseHandler(errorMessage, HttpServletResponse.SC_BAD_REQUEST, response, output);
        }
    }
}
