import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SqlFactory
{
    private final String GEOCODING_API_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private final String GEOCODING_API_KEY = "";
    private final int HTTP_REQUEST_TIMEOUT = 10000;

    public SqlFactory()
    {
    }

    public boolean GenerateSqlFile(List<Comment> commentList)
    {
        StringBuilder shopsBuilder = new StringBuilder();
        StringBuilder commentsBuilder = new StringBuilder();
        StringBuilder thumbnailsBuilder = new StringBuilder();

        for (Comment comment : commentList)
        {
            shopsBuilder.append(GenerateInsertShopSql(comment)).append("\n");
            commentsBuilder.append(GenerateInsertCommentSql(comment)).append("\n");
            thumbnailsBuilder.append(GenerateInsertThumbSql(comment)).append("\n");
        }
        shopsBuilder.deleteCharAt(shopsBuilder.length() - 1);
        commentsBuilder.deleteCharAt(commentsBuilder.length() - 1);
        thumbnailsBuilder.deleteCharAt(thumbnailsBuilder.length() - 1);

        try
        {
            PrintWriter writer = new PrintWriter("populate_shops.sql", "UTF-8");
            writer.println(shopsBuilder.toString());
            writer.flush();
            writer.close();

            writer = new PrintWriter("populate_comments.sql", "UTF-8");
            writer.println(commentsBuilder.toString());
            writer.flush();
            writer.close();

            writer = new PrintWriter("populate_thumbnails.sql", "UTF-8");
            writer.println(thumbnailsBuilder.toString());
            writer.flush();
            writer.close();

            return true;
        }
        catch (FileNotFoundException | UnsupportedEncodingException exception)
        {
            return false;
        }
    }

    private double[] QueryLatLng(String address)
    {
        String apiJsonResult = SendRequest(address);
        return ParseLatLng(apiJsonResult);
    }

    private String SendRequest(String address)
    {
        String response = "";

        try
        {
            String addressParameter = "address=" + address;
            String apiKeyParameter = "key=" + GEOCODING_API_KEY;
            String queryURL = GEOCODING_API_URL + "?" + addressParameter + "&" + apiKeyParameter;

            System.out.println(queryURL);

            URL url = new URL(queryURL);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(HTTP_REQUEST_TIMEOUT);
            connection.setConnectTimeout(HTTP_REQUEST_TIMEOUT);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

                while ((line = reader.readLine()) != null)
                {
                    response += line;
                }
            }
            else
            {
                System.out.println("HTTP Connection response code: " + responseCode);
            }
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
        return response;
    }

    private double[] ParseLatLng(String jsonText)
    {
        double[] latLng = new double[2];
        JSONObject jsonObject;
        JSONArray results;

        try
        {
            jsonObject = new JSONObject(jsonText);
            String isSuccess = jsonObject.getString("status");

            if (isSuccess.equals("OK"))
            {
                results = jsonObject.getJSONArray("results");

                JSONObject tuple = results.getJSONObject(0);
                JSONObject geometry = tuple.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");

                latLng[0] = location.getDouble("lat");
                latLng[1] = location.getDouble("lng");
            }
            else
            {
                System.out.println("No location result found");
            }
        }
        catch (JSONException exception)
        {
            exception.printStackTrace();
        }
        return latLng;
    }

    private String GenerateInsertShopSql(Comment comment)
    {
        String instruction;
        instruction = "INSERT INTO shops (name, evaluation, address, latitude, longitude)\n";
        instruction += ("SELECT * FROM (SELECT '" + EscapeSequence(comment.GetShopName()) + "'");
        instruction += (", " + Double.toString(10));
        instruction += (", '" + comment.GetShopAddress() + "'");
        instruction += (", " + Double.toString(comment.GetLatitude()) + " AS lat, " + Double.toString(comment.GetLongitude()) + " AS lng");
        instruction += ") AS temp\n";
        instruction += "WHERE NOT EXISTS (SELECT name FROM shops WHERE name = '" + EscapeSequence(comment.GetShopName()) + "')\n";
        instruction += "LIMIT 1;";

        return instruction;
    }

    private String GenerateInsertCommentSql(Comment comment)
    {
        String instruction;
        instruction = "INSERT INTO comments (title, url, address, evaluation, shop_id)\n";
        instruction += ("SELECT '" + EscapeSequence(comment.GetTitle()) + "'");
        instruction += (", '" + comment.GetShopLink() + "'");
        instruction += (", '" + comment.GetShopAddress() + "'");
        instruction += (", " + Double.toString(comment.GetEvaluation()) + ", shops.id\n");
        instruction += "FROM shops\n";
        instruction += ("WHERE name = '" + EscapeSequence(comment.GetShopName()) + "'\n");
        instruction += ("AND NOT EXISTS (SELECT url FROM comments WHERE url = '" + comment.GetShopLink() + "')\n");
        instruction += "LIMIT 1;";

        return instruction;
    }

    private String GenerateInsertThumbSql(Comment comment)
    {
        String instruction;
        instruction = "INSERT INTO thumbnails (url, shop_id)\n";
        instruction += ("SELECT '" + comment.GetThumbLink() + "', shops.id\n");
        instruction += ("FROM shops\n");
        instruction += ("WHERE name = '" + EscapeSequence(comment.GetShopName()) + "'\n");
        instruction += ("AND NOT EXISTS (SELECT url FROM thumbnails WHERE url = '" + comment.GetThumbLink() + "')\n");
        instruction += "LIMIT 1;";

        return instruction;
    }

    //將原始字串內部的單引號改成轉義序列
    private String EscapeSequence(String string)
    {
        if (string.contains("\\\'"))
        {
            return string;
        }
        return string.replace("'", "\\\'");
    }
}

// INSERT INTO shops (name, evaluation, address, latitude, longitude)
// SELECT * FROM (SELECT '@name', 10, '@address', @latitude AS lat, @longitude AS lng) AS temp
// WHERE NOT EXISTS (SELECT name FROM shops WHERE name = '@name')
// LIMIT 1;

// INSERT INTO comments (title, url, address, evaluation, shop_id)
// SELECT '@title', '@url', '@address', 10, shops.id
// FROM shops
// WHERE name = '@name'
// AND NOT EXISTS (SELECT url FROM comments WHERE url = '@url')
// LIMIT 1;

// INSERT INTO thumbnails (url, shop_id)
// SELECT '@thumb', shops.id
// FROM shops
// WHERE name = '@name'
// AND NOT EXISTS (SELECT url FROM thumbnails WHERE url = '@thumb')
// LIMIT 1;