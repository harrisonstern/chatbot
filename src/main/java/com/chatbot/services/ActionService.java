package com.chatbot.services;

import com.chatbot.model.Session;
import com.chatbot.model.User;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.dialogflow.v2.model.GoogleCloudDialogflowV2Context;
import com.google.api.services.dialogflow.v2.model.GoogleCloudDialogflowV2WebhookRequest;
import com.google.api.services.dialogflow.v2.model.GoogleCloudDialogflowV2WebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public ActionService(UserServiceImpl userService, SessionServiceImpl sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;
    }

    @Autowired
    private JacksonFactory jacksonFactory;

    public String process(String rawRequest) throws IOException {
        GoogleCloudDialogflowV2WebhookRequest request = jacksonFactory.createJsonParser(rawRequest)
                .parse(GoogleCloudDialogflowV2WebhookRequest.class);

        List<GoogleCloudDialogflowV2Context> contexts = request.getQueryResult().getOutputContexts();

        // Parse out the UUID from the getSession string
        String UUIDString = request.getSession().split("/")[4];
        UUID sessionUUID = UUID.fromString(UUIDString);

        Session session;

        try {
            session = sessionService.getSession(sessionUUID);
            logger.info("processing request from old session id = {}", sessionUUID);

        } catch (NoSuchElementException e) {
            session = new Session();
            session.setSessionUUID(sessionUUID);
            User user = new User();
            session.setUser(user);
            user.setSession(session);
            sessionService.addSession(session);

            logger.info("processing request from new session id = {}", sessionUUID);
        }

        String responseText = "";
        switch (request.getQueryResult().getIntent().getDisplayName()) {
            case "checkout":

                logger.info("updating user information if provided");

                Map<String, Object> parameters = request.getQueryResult().getParameters();

                // update the user info from the parameters
                addUserInformationFromParameters(session, parameters);

//                // Check if all of the slots have already been filled:
//                if (request.getQueryResult().getAllRequiredParamsPresent() != null) {
//
//                    logger.info("all the required parameters were provided. returning default response");
//
//                    // Return the default fullfillment text defined in the intent
//                    responseText = request.getQueryResult().getFulfillmentText();
//
//                    // Otherwise generate the response text based on missing parameters:
//                } else {
//
//                    logger.info("Missing some of the required parameters. Modifying the default response.");
//
//                    responseText = generateResponseForMissingParameterAndModifyOutputContext(session, parameters, contexts);
//
//                }

        }

        logger.info("request = {}", request);
        GoogleCloudDialogflowV2WebhookResponse response = new GoogleCloudDialogflowV2WebhookResponse();
        //response.setFulfillmentText(responseText);
        response.setOutputContexts(contexts);

        StringWriter stringWriter = new StringWriter();
        JsonGenerator jsonGenerator = jacksonFactory.createJsonGenerator(stringWriter);
        jsonGenerator.serialize(response);
        jsonGenerator.flush();
        return stringWriter.toString();
    }

    private void addUserInformationFromParameters(Session session, Map<String, Object> parameters) {
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            switch (entry.getKey()) {
                case "lastName":
                    if (((String) entry.getValue()).trim().length() > 0) {
                        session.getUser().setName((String) entry.getValue());
                    }

                    break;
                case "roomNumber":
                    System.out.println("Room Number: " + (String) entry.getValue());
                    session.getUser().setRoomNumber(Integer.parseInt((String) entry.getValue()));
                    break;
                case "reservationNumber":
                    if ((entry.getValue() + "").trim().length() > 0) {
                        session.getUser().setReservationNumber(entry.getValue() + "");
                    }
                    break;
            }
        }

        sessionService.saveSession(session);
    }
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

    private String getQuote(String typeOfQuote) {
        String responseText;
        responseText = "This is a great quote from category: " + typeOfQuote;
        return responseText;
    }
}