package com.perrakis.wifipasswordstealer;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadFileTask extends AsyncTask<String, Void, String>
{

    @Override
    protected String doInBackground(String... urls)
    {
        try {
            // Create a URL object
            URL url = new URL(urls[0]);

            // Create a HttpURLConnection object
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Set request method to GET
            conn.setRequestMethod("GET");

            // Connect to the server
            conn.connect();

            // Get the response code
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the input stream
                InputStream inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                // Close the streams
                reader.close();
                inputStream.close();

                // Return the response data
                return stringBuilder.toString();
            } else {
                // Log error if response code is not OK
                Log.e("HTTP GET Request", "Server returned HTTP response code: " + responseCode);
            }
        } catch (IOException e) {
            // Log exception
            Log.e("HTTP GET Request", "Error: " + e.getMessage(), e);
        }

        // Return null if there was an error
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (result != null) {
            // Handle the response data here
            Log.d("HTTP GET Response", result);
        } else {
            // Handle null or error response
            Log.e("HTTP GET Response", "Error: Response is null or empty");
        }
    }
}