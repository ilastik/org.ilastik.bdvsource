/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ilastik.bdvsource;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author chaubold
 */
public class HttpRequest {
    private String address;
    
    public HttpRequest(String address)
    {
        this.address = address;
    }
    
    public String post(String body)
    {
        return executeRequest("POST", body);
    }
    
    public String post()
    {
        return this.post("");
    }
    
    public String get()
    {
        return executeRequest("GET", "");
    }
    
    private String executeRequest(String method, String body) {
        HttpURLConnection connection = null;

        try {
            //Create connection
            URL url = new URL(address);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            
            if(body.length() > 0 && method.equals("POST"))
            {
                connection.setRequestProperty("Content-Type", "application/json");

                connection.setRequestProperty("Content-Length",
                        Integer.toString(body.getBytes().length));
                connection.setRequestProperty("Content-Language", "en-US");
                
                connection.setUseCaches(false);
                connection.setDoOutput(true);
        
                try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                    wr.writeBytes(body);
                }
                catch(IOException ex)
                {
                    System.out.println("org.ilastik.bdvsource.HttpRequest.executeRequest() failed to write body");
                }
            }

            connection.connect();
            System.out.println("org.ilastik.bdvsource.HttpRequest.executeRequest(): response code " + connection.getResponseCode());
            
            //Get Response  
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
