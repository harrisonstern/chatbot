package com.chatbot.services;

import com.chatbot.model.Session;
import com.chatbot.model.User;
import com.fasterxml.jackson.core.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.dialogflow.v2.model.GoogleCloudDialogflowV2Context;
import com.google.api.services.dialogflow.v2.model.GoogleCloudDialogflowV2WebhookRequest;
import com.google.api.services.dialogflow.v2.model.GoogleCloudDialogflowV2WebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ActionService {

    private final Logger logger = LoggerFactory.getLogger(ActionService.class);

    private UserServiceImpl userService;
    private SessionServiceImpl sessionService;
    private WeatherService weatherService;

    public ActionService(UserServiceImpl userService, SessionServiceImpl sessionService, WeatherService weatherService) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.weatherService = weatherService;
    }

    @Autowired
    private JacksonFactory jacksonFactory;

    public String process(String rawRequest) throws IOException {
        GoogleCloudDialogflowV2WebhookRequest request = jacksonFactory.createJsonParser(rawRequest)
                .parse(GoogleCloudDialogflowV2WebhookRequest.class);

        List<GoogleCloudDialogflowV2Context> contexts = request.getQueryResult().getOutputContexts();

        // Parse out the session id from the getSession string
        String sessionString = request.getSession();
        String idString = request.getSession().split("/")[4];

        Session session;

        try {
            session = sessionService.getSession(idString);

        } catch (NoSuchElementException e) {
            session = new Session();
            session.setSessionID(idString);
            User user = new User();
            session.setUser(user);
            session.setFallbackCount(0);
            user.setSession(session);
            sessionService.addSession(session);
        }

        GoogleCloudDialogflowV2WebhookResponse response = getResponse(session,request);

        StringWriter stringWriter = new StringWriter();
        JsonGenerator jsonGenerator = jacksonFactory.createJsonGenerator(stringWriter);
        jsonGenerator.serialize(response);
        jsonGenerator.flush();
        return stringWriter.toString();
    }

    private GoogleCloudDialogflowV2WebhookResponse getResponse(Session session, GoogleCloudDialogflowV2WebhookRequest request) throws IOException{

        // Create the response
        GoogleCloudDialogflowV2WebhookResponse response = new GoogleCloudDialogflowV2WebhookResponse();
        String fulfillmentText;
        // Switch for each intent to add additional functionality:

        logger.info("Processing request with the following intent: {}", request.getQueryResult().getIntent().getDisplayName());
        switch (request.getQueryResult().getIntent().getDisplayName()) {
            case "service.request.roomService":

                String[] suggestions = {"Can I get more towels", "Clean my room", "I need a new room key", "Pull my car around"};

                fulfillmentText = request.getQueryResult().getFulfillmentText();
                fulfillmentText += createSuggestionsJSON(suggestions);
                response.setFulfillmentText(fulfillmentText);
                break;

            case "information.request.nearestAirport":

                Point2D airportCoord = new Point2D.Double(55.630073, 12.652950);
                Point2D coords[] = {airportCoord};
                fulfillmentText = request.getQueryResult().getFulfillmentText();
                fulfillmentText += createCoordinatesJSON(coords);
                response.setFulfillmentText(fulfillmentText);
                break;

            case "sauna.hours - yes":

                Point2D suanaCoord = new Point2D.Double(55.608201, 12.977496);
                Point2D suanaCoords[] = {suanaCoord};
                fulfillmentText = request.getQueryResult().getFulfillmentText();
                fulfillmentText += createCoordinatesJSON(suanaCoords);
                response.setFulfillmentText(fulfillmentText);
                break;

            case "information.request.turnOnTV":

                fulfillmentText = request.getQueryResult().getFulfillmentText();
                fulfillmentText += createVideoJSON("https://www.youtube.com/watch?v=CpIqkLEGKsk");
                response.setFulfillmentText(fulfillmentText);
                break;

            case "Default Fallback Intent":
                System.out.println(session.getFallbackCount());
                int currentFallbackCount = session.getFallbackCount();
                if (currentFallbackCount == 2){
                    fulfillmentText = "Sorry I am having trouble understanding. Please go to reception for additional help.";
                    response.setFulfillmentText(fulfillmentText);
                }
                else{
                    session.setFallbackCount(session.getFallbackCount() + 1);
                    sessionService.saveSession(session);
                }
                break;

            case "information.request.weather":
               System.out.println(weatherService.getForcast());
               break;


        }

        return response;
    }

    private String createSuggestionsJSON(String[] suggestions) throws IOException {

        JsonFactory factory = new JsonFactory();
        StringWriter stringWriter = new StringWriter();
        com.fasterxml.jackson.core.JsonGenerator generator = factory.createGenerator(stringWriter);

        generator.writeStartObject();
        generator.writeFieldName("suggestions");
        generator.writeStartArray();

        for (String suggestion : suggestions)
            generator.writeString(suggestion);

        generator.writeEndArray();
        generator.writeEndObject();
        generator.close();

        return stringWriter.toString();
    }

    private String createVideoJSON(String url) throws IOException{
        JsonFactory factory = new JsonFactory();
        StringWriter stringWriter = new StringWriter();
        com.fasterxml.jackson.core.JsonGenerator generator = factory.createGenerator(stringWriter);

        generator.writeStartObject();
        generator.writeStringField("video",url);
        generator.writeEndObject();
        generator.close();

        return stringWriter.toString();
    }

    private String createCoordinatesJSON(Point2D[] coords) throws IOException {

        JsonFactory factory = new JsonFactory();
        StringWriter stringWriter = new StringWriter();
        com.fasterxml.jackson.core.JsonGenerator generator = factory.createGenerator(stringWriter);

        generator.writeStartObject();
        generator.writeFieldName("mapCoordinates");
        generator.writeStartArray();

        for (Point2D coord : coords) {
            generator.writeStartObject();
            generator.writeNumberField("lat",coord.getX());
            generator.writeNumberField("lng",coord.getY());
            generator.writeEndObject();
        }

        generator.writeEndArray();
        generator.writeEndObject();
        generator.close();

        return stringWriter.toString();
    }
}

//    private void addUserInformationFromParameters(Session session, Map<String, Object> parameters) {
//        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
//            switch (entry.getKey()) {
//                case "lastName":
//                    if (((String) entry.getValue()).trim().length() > 0) {
//                        session.getUser().setName((String) entry.getValue());
//                    }
//
//                    break;
//                case "roomNumber":
//                    System.out.println("Room Number: " + (String) entry.getValue());
//                    session.getUser().setRoomNumber(Integer.parseInt((String) entry.getValue()));
//                    break;
//                case "reservationNumber":
//                    if ((entry.getValue() + "").trim().length() > 0) {
//                        session.getUser().setReservationNumber(entry.getValue() + "");
//                    }
//                    break;
//            }
//        }
//
//        sessionService.saveSession(session);
//    }
//    private String generateResponseForMissingParameterAndModifyOutputContext(Session session, Map<String, Object> parameters, List<GoogleCloudDialogflowV2Context> contexts) {
//        boolean foundReservationNumber = false;
//        boolean foundLastName = false;
//        boolean foundRoomNumber = false;
//
//        boolean lastNameNeeded = false;
//        boolean roomNumberNeeded = false;
//        boolean reservationNumberNeeded = false;
//
//        String returnString = "";
//        for (Map.Entry<String, Object> entry : parameters.entrySet())
//            switch (entry.getKey()) {
//
//                case "lastName":
//                    lastNameNeeded = true;
//                    if (((String) entry.getValue()).trim().length() == 0) {
//                        if (session.getUser().getName() != null) {
//                            foundLastName = true;
//                            removeParameterContext("lastname", contexts);
//                        }
//                    }
//                    else
//                        foundLastName = true;
//
//                    break;
//
//                case "roomNumber":
//                    roomNumberNeeded = true;
//                    if (((String) entry.getValue()).isEmpty()) {
//                        if (session.getUser().getRoomNumber() != null) {
//                            foundRoomNumber = true;
//                            removeParameterContext("roomnumber", contexts);
//                        }
//                    }
//                    else
//                        foundRoomNumber = true;
//                    break;
//
//                case "reservationNumber":
//                    reservationNumberNeeded = true;
//                    if ((entry.getValue() + "").trim().length() == 0) {
//                        if (session.getUser().getReservationNumber() != null) {
//                            foundReservationNumber = true;
//                            removeParameterContext("reservationnumber", contexts);
//                        }
//                    }
//                    else
//                        foundReservationNumber = true;
//                    break;
//
//            }
//
//        if (foundLastName == lastNameNeeded && foundReservationNumber == reservationNumberNeeded && foundRoomNumber == roomNumberNeeded) {
//            returnString += "Looks like we already have all the information we need.";
//        } else if ((foundLastName != lastNameNeeded) && lastNameNeeded) {
//            returnString += "Please input your last name.";
//        } else if ((foundRoomNumber != roomNumberNeeded) && roomNumberNeeded) {
//            returnString += "Please input your room number.";
//        } else if ((foundReservationNumber != reservationNumberNeeded) && reservationNumberNeeded) {
//            returnString += "Please input your reservation number.";
//        }
//
//        return returnString;
//    }
//
//    private void removeParameterContext(String parameterName, List<GoogleCloudDialogflowV2Context> contexts) {
//        contexts.forEach(context -> {
//            if (context.getName().contains(parameterName)) {
//                logger.info("Removing the following parameter from slot filling since we already have user information: {}", parameterName);
//                context.setLifespanCount(0);
//
//            }
//        });
//    }


//CODE FOR ADDING USERS

//    Map<String, Object> parameters = request.getQueryResult().getParameters();
//
//                // update the user info from the parameters
//                addUserInformationFromParameters(session, parameters);
//                logger.info("request = {}", request);
//
//
//
//
//                // response.setOutputContexts(contexts);
//
//
////                // Check if all of the slots have already been filled:
////                if (request.getQueryResult().getAllRequiredParamsPresent() != null) {
////
////                    logger.info("all the required parameters were provided. returning default response");
////
////                    // Return the default fullfillment text defined in the intent
////                    responseText = request.getQueryResult().getFulfillmentText();
////
////                    // Otherwise generate the response text based on missing parameters:
////                } else {
////
////                    logger.info("Missing some of the required parameters. Modifying the default response.");
////
////                    responseText = generateResponseForMissingParameterAndModifyOutputContext(session, parameters, contexts);
////
////                }