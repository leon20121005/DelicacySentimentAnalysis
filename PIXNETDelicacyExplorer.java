import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PIXNETDelicacyExplorer
{
    private final String DELICACY_RANKING_LIST_URL = "https://blogranking.events.pixnet.net/?category=14";
    private final String DELICACY_BLOGGER_CONDITION = "a.blogger-result";
    private final String GET_ARTICLES_API_URL = "https://emma.pixnet.cc/blog/articles?user=a12425";
    private final String JSON_CALLBACK_PARAMETER = "callback=json";
    private final String CONTENT_CONDITION = "div.article-content-inner";

    private final int HTTP_REQUEST_TIMEOUT = 10000;

    public PIXNETDelicacyExplorer()
    {
    }

    //解析部落客排行列表中<a href="/blogger?user=USER_ID" class="blogger-result"></a>的USER_ID
    public List<String> GetDelicacyBlogger()
    {
        try
        {
            Document document = Jsoup.connect(DELICACY_RANKING_LIST_URL).get();
            Elements links = document.select(DELICACY_BLOGGER_CONDITION);
            List<String> delicacyBloggerList = new ArrayList<>();

            for (Element link : links)
            {
                String[] segments = link.attr("href").split("="); //把 "/blogger?user=USER_ID" 切成 "/blogger?user" 以及 "USER_ID"
                delicacyBloggerList.add(segments[1]);
            }
            return delicacyBloggerList;
        }
        catch (IOException exception)
        {
            return null;
        }
    }

    //根據USER_ID向PIXNET的API發送HTTP Request查詢某該使用者的文章列表
    public String SendRequest(String userID)
    {
        String response = "";

        try
        {
            String userParameter = "user=" + userID;
            String queryURL = GET_ARTICLES_API_URL + "?" + JSON_CALLBACK_PARAMETER + "&" + userParameter;
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

    //解析部落客的文章列表(JSON格式)裡，類別是美味食記的文章的標題、連結、地址以及座標(內容和評價分數暫時為null)
    public List<Comment> ParseDelicacyComment(String jsonText)
    {
        List<Comment> commentList = new ArrayList<>();
        JSONObject jsonObject;
        JSONArray articles;

        try
        {
            jsonObject = new JSONObject(jsonText);
            int isError = jsonObject.getInt("error");

            if (isError == 0)
            {
                articles = jsonObject.getJSONArray("articles");

                for (int index = 0; index < articles.length(); index++)
                {
                    JSONObject tuple = articles.getJSONObject(index);
                    
                    if (tuple.getString("site_category").equals("美味食記"))
                    {
                        Comment comment = new Comment();
                        comment.SetShopName(tuple.getString("title"));
                        comment.SetShopLink(tuple.getString("link").replace("\\", "")); //刪除JSON在編碼網頁URL的"/"時加入的"\"
                        comment.SetShopAddress(tuple.getString("address"));
                        comment.SetLatitude(tuple.getJSONObject("location").getDouble("latitude"));
                        comment.SetLongitude(tuple.getJSONObject("location").getDouble("longitude"));
                        commentList.add(comment);
                    }
                }
            }
            else
            {
                System.out.println("No delicacy article found");
            }
        }
        catch (JSONException exception)
        {
            exception.printStackTrace();
        }
        return commentList;
    }

    //根據文章網頁URL解析出美食評論內容並存入該comment
    public void ParseCommentContent(Comment comment, String commentURL)
    {
        try
        {
            Document document = Jsoup.connect(commentURL).get();
            Elements content = document.select(CONTENT_CONDITION);
            String[] segments = content.get(0).text().replace("\u00a0", "").split(" ");
            StringBuilder builder = new StringBuilder();

            for (String segment : segments)
            {
                builder.append(segment).append("\n");
            }
            builder.deleteCharAt(builder.length() - 1);
            comment.SetContent(builder.toString());
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
        catch (IndexOutOfBoundsException exception)
        {
            exception.printStackTrace();
            comment.SetContent("none");
        }
    }
}