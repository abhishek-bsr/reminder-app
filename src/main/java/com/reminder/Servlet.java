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
    private static JSONArray reminderList = new JSONArray();
    private static String CHARACTER_ENCODING = "UTF-8";
    private static String CONTENT_TYPE = "application/json";
    private static int UNPROCESSABLE_ENTITY = 422;
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
            int limitParam = request.getParameter("limit") == null ? reminderList.length()
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

                for (; offsetParam < reminderList.length(); offsetParam++) {
                    // check for <completed> param and <important> param
                    // if both are null, skip checking fields in JSONObject
                    if (completedParam == null && importantParam == null) {
                        reminders.put(reminderList.getJSONObject(offsetParam));
                        limitParam = limitParam - 1;

                        if (limitParam == 0)
                            break;

                        continue;
                    }

                    if (completedParam != null) {
                        Boolean isCompleted = reminderList.getJSONObject(offsetParam).getBoolean("is_completed");
                        if (isCompleted.equals(completedParam)) {
                            reminders.put(reminderList.getJSONObject(offsetParam));
                            limitParam = limitParam - 1;
                        }
                    }
                    if (importantParam != null) {
                        Boolean isImportant = reminderList.getJSONObject(offsetParam).getBoolean("is_important");
                        if (isImportant.equals(importantParam)) {
                            reminders.put(reminderList.getJSONObject(offsetParam));
                            limitParam = limitParam - 1;
                        }
                    }

                    if (limitParam <= 0)
                        break;
                }

                response.setStatus(HttpServletResponse.SC_OK);
                JSONObject responseObject = utility.responseBuilder("reminders", reminders);
                output.println(responseObject);
            } else {
                // GET request for 1 reminder only
                requestId = requestId.replace("/", ""); // omit '/' from path to convert into proper <id> field

                int getObjectIndex = utility.checkReminderIdExists(reminderList, Integer.parseInt(requestId));
                if (getObjectIndex != -1) {
                    // data exists in JSONArray
                    JSONObject reminder = reminderList.getJSONObject(getObjectIndex);

                    response.setStatus(HttpServletResponse.SC_OK);
                    JSONObject responseObject = utility.responseBuilder("reminders", reminder);
                    output.println(responseObject);
                } else {
                    // data not found
                    JSONObject errorObjects = new JSONObject();
                    String errorMessage = String.format("Reminder id " + "<" + requestId + ">" + " not found");
                    errorObjects.put("status_code", HttpServletResponse.SC_NOT_FOUND);
                    errorObjects.put("error", errorMessage);

                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    JSONObject responseObject = utility.responseBuilder("reminders", errorObjects);
                    output.println(responseObject);
                }
            }
        } catch (Exception err) {
            // error occuring during <Integer> or <Boolean> parsing
            JSONObject errorObjects = new JSONObject();
            errorObjects.put("status_code", UNPROCESSABLE_ENTITY);
            errorObjects.put("error", err.toString());

            response.setStatus(UNPROCESSABLE_ENTITY);
            JSONObject responseObject = utility.responseBuilder("reminders", errorObjects);
            output.println(responseObject);
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
                JSONObject errorObjects = new JSONObject();
                String errorMessage = String.format("Reminder id " + "<" + requestId + ">" + " not found");
                errorObjects.put("status_code", HttpServletResponse.SC_NOT_FOUND);
                errorObjects.put("error", errorMessage);

                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                JSONObject responseObject = utility.responseBuilder("reminders", errorObjects);
                output.println(responseObject);
            } else {
                int reminderId = reminderList.getJSONObject(getObjectIndex).getInt("id");
                reminderList.remove(getObjectIndex);

                String info = String.format("<" + reminderId + ">" + " has been deleted");

                response.setStatus(HttpServletResponse.SC_OK);
                JSONObject reminderIdObject = new JSONObject().put("message", info);
                JSONObject responseObject = utility.responseBuilder("reminders", reminderIdObject);
                output.println(responseObject);
            }
        } else {
            ArrayList<String> reminderIds = new ArrayList<>(); // get all <id>
            for (int i = 0; i < reminderList.length(); i++) {
                String reminderId = reminderList.getJSONObject(i).getString("id");
                reminderIds.add(reminderId);
            }
            reminderList = new JSONArray(); // blank JSONArray list (erase all data)

            String info = String.format("<" + reminderIds.toString() + ">" + " have been deleted");

            response.setStatus(HttpServletResponse.SC_OK);
            JSONObject reminderIdsObject = new JSONObject().put("message", info);
            JSONObject responseObject = utility.responseBuilder("reminders", reminderIdsObject);
            output.println(responseObject);
        }
    }

    /*
     * @method POST /reminders      add new data
     * 
     * @field name                  String  REQUIRED
     * @field tag_color             String  REQUIRED
     * @field is_completed          Boolean REQUIRED
     * @field is_important          Boolean REQUIRED
     * @field reminder_utc          String  REQUIRED
     * @field frequency             String  REQUIRED
     * @field note                  String  REQUIRED
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        PrintWriter output = response.getWriter();
        response.setCharacterEncoding(CHARACTER_ENCODING);
        response.setContentType(CONTENT_TYPE);

        String requestBody = request.getReader().lines().collect(Collectors.joining());
        try {
            JSONObject requestJsonObject = new JSONObject(requestBody).getJSONObject("reminders");

            // missing field(s) in request body
            if (requestJsonObject.has("name") || requestJsonObject.has("tag_color")
                    || requestJsonObject.has("is_completed") || requestJsonObject.has("is_important")
                    || requestJsonObject.has("frequency") || requestJsonObject.has("note")) {
                String reminderName = requestJsonObject.getString("name");
                String tagColor = requestJsonObject.getString("tag_color");
                boolean isCompleted = requestJsonObject.getBoolean("is_completed");
                boolean isImportant = requestJsonObject.getBoolean("is_important");
                String reminderNote = requestJsonObject.getString("note");

                // String to enum <Frequency>
                try {
                    Instant reminderUtc = Instant.parse(requestJsonObject.getString("reminder_utc"));
                    Frequency frequency = Frequency.valueOf(requestJsonObject.getString("frequency"));

                    int nameExists = utility.checkReminderNameExists(reminderList, reminderName);
                    if (nameExists != -1) {
                        reminderList.getJSONObject(nameExists).put("tag_color", tagColor);
                        reminderList.getJSONObject(nameExists).put("is_completed", isCompleted);
                        reminderList.getJSONObject(nameExists).put("is_important", isImportant);
                        reminderList.getJSONObject(nameExists).put("frequency", frequency);
                        reminderList.getJSONObject(nameExists).put("reminder_utc", reminderUtc);
                        reminderList.getJSONObject(nameExists).put("note", reminderNote);

                        String info = String.format("Reminder <"
                                + reminderList.getJSONObject(nameExists).getInt("id") + ">" + " has been added");

                        response.setStatus(HttpServletResponse.SC_CREATED);
                        JSONObject reminderIdObject = new JSONObject().put("message", info);
                        JSONObject responseObject = utility.responseBuilder("reminders", reminderIdObject);
                        output.println(responseObject);
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

                        response.setStatus(HttpServletResponse.SC_CREATED);
                        JSONObject reminderIdObject = new JSONObject().put("message", info);
                        JSONObject responseObject = utility.responseBuilder("reminders", reminderIdObject);
                        output.println(responseObject);
                    }
                } catch (Exception error) {
                    // unable to parse into <Frequency> enum
                    JSONObject errorObjects = new JSONObject();
                    errorObjects.put("status_code", UNPROCESSABLE_ENTITY);
                    errorObjects.put("error", error.toString());

                    response.setStatus(UNPROCESSABLE_ENTITY);
                    JSONObject responseObject = utility.responseBuilder("reminders", errorObjects);
                    output.println(responseObject);
                }
            } else {
                // missing fields in body
                String errorMessage = "Missing one or many fields";
                JSONObject errorObjects = new JSONObject();
                errorObjects.put("status_code", UNPROCESSABLE_ENTITY);
                errorObjects.put("error", errorMessage);
    
                response.setStatus(UNPROCESSABLE_ENTITY);
                JSONObject responseObject = utility.responseBuilder("reminders", errorObjects);
                output.println(responseObject);
            }
        } catch (Exception error) {
            // Unable to parse into JSONObject
            JSONObject errorObjects = new JSONObject();
            errorObjects.put("status_code", HttpServletResponse.SC_BAD_REQUEST);
            errorObjects.put("error", error.toString());

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JSONObject responseObject = utility.responseBuilder("reminders", errorObjects);
            output.println(responseObject);
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
                JSONObject errorObjects = new JSONObject();
                errorObjects.put("status_code", HttpServletResponse.SC_NOT_FOUND);
                errorObjects.put("error", errorMessage);

                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                JSONObject responseObject = utility.responseBuilder("reminders", errorObjects);
                output.println(responseObject);
            } else {
                String requestBody = request.getReader().lines().collect(Collectors.joining());
                try {
                    JSONObject requestJsonObject = new JSONObject(requestBody).getJSONObject("reminders");
                    JSONObject reminder = reminderList.getJSONObject(getObjectIndex);

                    for (String key : requestJsonObject.keySet()) {
                        if (key.equals("name") || key.equals("id"))
                            continue;
                        else if (key.equals("reminder_utc")) {
                            Instant updatedReminderUtc = utility.getUtcTime(requestJsonObject.getString(key));

                            if (updatedReminderUtc == null) {
                                String errorMessage = String.format("Field <reminder_utc> is not in proper form");
                                JSONObject errorObjects = new JSONObject();
                                errorObjects.put("status_code", UNPROCESSABLE_ENTITY);
                                errorObjects.put("error", errorMessage);
                
                                response.setStatus(UNPROCESSABLE_ENTITY);
                                JSONObject responseObject = utility.responseBuilder("reminders", errorObjects);
                                output.println(responseObject);
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

                    response.setStatus(HttpServletResponse.SC_OK);
                    JSONObject reminderIdObject = new JSONObject().put("message", info);
                    JSONObject responseObject = utility.responseBuilder("reminders", reminderIdObject);
                    output.println(responseObject);
                } catch (Exception error) {
                    // Unable to parse into JSONObject
            JSONObject errorObjects = new JSONObject();
            errorObjects.put("status_code", HttpServletResponse.SC_BAD_REQUEST);
            errorObjects.put("error", error.toString());

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JSONObject responseObject = utility.responseBuilder("reminders", errorObjects);
            output.println(responseObject);
                }
            }
        } else {
            // error in parameter
            String errorMessage = "Reminder <id> not provided as parameter";
            JSONObject errorObjects = new JSONObject();
            errorObjects.put("status_code", HttpServletResponse.SC_BAD_REQUEST);
            errorObjects.put("error", errorMessage);

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JSONObject responseObject = utility.responseBuilder("reminders", errorObjects);
            output.println(responseObject);
        }
    }
}
