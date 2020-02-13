package com.chatbot.services;

import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class WeatherService {

    public String getForcast() throws IOException {
        URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=malmo&appid=f38a0d1e0b43ff340e495b346de0d47a");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

//        Map<String, String> parameters = new HashMap<>();
//        parameters.put("q", "malmo");
//        parameters.put("appid", "f38a0d1e0b43ff340e495b346de0d47a");
//
//        con.setConnectTimeout(5000);
//        con.setReadTimeout(5000);
//
//        con.setDoOutput(true);
//        DataOutputStream out = new DataOutputStream(con.getOutputStream());
//        out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
//        out.flush();
//        out.close();

        int status = con.getResponseCode();

//        BufferedReader in = new BufferedReader(
//                new InputStreamReader(con.getInputStream()));
//        String inputLine;
//        StringBuffer content = new StringBuffer();
//        while ((inputLine = in.readLine()) != null) {
//            content.append(inputLine);
//        }
//        in.close();
//
//        con.disconnect();
//
//        status = con.getResponseCode();
//
//        Reader streamReader = null;
//
//        if (status > 299) {
//            streamReader = new InputStreamReader(con.getErrorStream());
//        } else {
//            streamReader = new InputStreamReader(con.getInputStream());
//        }

        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader inn = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            StringBuffer response = new StringBuffer();
            String inputLine;
            while ((inputLine = inn.readLine()) != null) {
                response.append(inputLine);
            }
            inn.close();

            // print result
            return (response.toString());
        } else {
           return "";
        }
    }



}
