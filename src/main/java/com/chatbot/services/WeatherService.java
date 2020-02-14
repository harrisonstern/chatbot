package com.chatbot.services;

import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class WeatherService {

    public String getForcast(String city) throws IOException {
        URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q="+city+"&appid=f38a0d1e0b43ff340e495b346de0d47a&units=metric");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
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
