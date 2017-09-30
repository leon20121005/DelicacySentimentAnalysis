import java.net.URL;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class PIXNETCrawler
{
    private final String HOME_URL = "http://zineblog.com.tw/blog/post/45501739";
    private final String HOME_CONDITION = "a[href]:contains(【)";
    private final String ADDRESS_CONDITION = "span:contains(地址)";
    private final String ARTICLE_CONDITION = "div.entry-content";
    private final int MAX_ARTICLE = 1000;

    private List<Comment> _commentList;
    private Comment _currentComment;

    private int _ioExceptionTimes;
    private int _indexOutOfBoundsExceptionTimes;

    public PIXNETCrawler()
    {
        _commentList = new ArrayList<Comment>();
    }

    //儲存結果至文字檔案
    public boolean SaveResult()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("List size: ").append(_commentList.size()).append("\n");

        for (Comment comment : _commentList)
        {
            builder.append("\n").append("Title: ").append(comment.GetShopName());
            builder.append("\n").append("Link: ").append(comment.GetShopLink());
            builder.append("\n").append("Address: ").append(comment.GetShopAddress());
            builder.append("\n").append("Content: ").append(comment.GetContent());
            builder.append("\n");
        }
        builder.deleteCharAt(builder.length() - 1);

        try (PrintWriter writer = new PrintWriter("result.txt"))
        {
            writer.println(builder.toString());
            return true;
        }
        catch (FileNotFoundException exception)
        {
            System.out.println(exception.getMessage() + " in SaveResult()");
            return false;
        }
    }

    public List<Comment> GetContent()
    {
        try
        {
            Document doc = Jsoup.connect(HOME_URL).get();
            String title = doc.title();
            Elements links = doc.select(HOME_CONDITION);

            System.out.println("Processing " + title);

            for (int i = 0; i < links.size() && i < MAX_ARTICLE; i++)
            {
                _currentComment = new Comment();
                _currentComment.SetShopName(links.get(i).text()); //Home底下每個文章的Title
                _currentComment.SetShopLink(links.get(i).attr("href")); //Home底下每個文章的連結
                GetAddress(links.get(i).attr("href")); //找出文章裡面的地址
                GetArticle(links.get(i).attr("href")); //找出文章裡面評論的部分
                _commentList.add(_currentComment);
            }
        }
        catch (IOException exception)
        {
            System.out.println(exception.getMessage() + " in GetConent(), current list size: " + Integer.toString(_commentList.size()));
            _ioExceptionTimes++;
        }

        System.out.println("Process finished with " + Integer.toString(_commentList.size()) + " results");
        System.out.println("IOException occurred " + Integer.toString(_ioExceptionTimes) + " times");
        System.out.println("IndexOutOfBoundsException occurred " + Integer.toString(_indexOutOfBoundsExceptionTimes) + " times");

        return _commentList;
    }

    private void GetAddress(String link)
    {
        try
        {
            Document doc = Jsoup.connect(link).get();
            Elements addresses = doc.select(ADDRESS_CONDITION);
            String[] segments = addresses.get(0).toString().split("<br>");

            for (String segment : segments)
            {
                if (segment.contains("地址"))
                {
                    _currentComment.SetShopAddress(segment);
                }
            }
        }
        catch (IOException exception)
        {
            System.out.println(exception.getMessage() + " in GetAddress(), current list size: " + Integer.toString(_commentList.size()));
            _ioExceptionTimes++;
        }
        catch (IndexOutOfBoundsException exception)
        {
            System.out.println(exception.getMessage() + " in GetAddress(), current list size: " + Integer.toString(_commentList.size()));
            _currentComment.SetShopAddress("none");
            _indexOutOfBoundsExceptionTimes++;
        }
    }

    private void GetArticle(String link)
    {
        try
        {
            Document doc = Jsoup.connect(link).get();
            Elements articles = doc.select(ARTICLE_CONDITION);
            String[] segments = articles.get(0).text().split(" ");
            StringBuilder builder = new StringBuilder();

            for (String segment : segments)
            {
                builder.append(segment).append("\n");
            }
            builder.deleteCharAt(builder.length() - 1);
            _currentComment.SetContent(builder.toString());
        }
        catch (IOException exception)
        {
            System.out.println(exception.getMessage() + " in GetArticle(), current list size: " + Integer.toString(_commentList.size()));
            _ioExceptionTimes++;
        }
        catch (IndexOutOfBoundsException exception)
        {
            System.out.println(exception.getMessage() + " in GetArticle(), current list size: " + Integer.toString(_commentList.size()));
            _currentComment.SetContent("none");
            _indexOutOfBoundsExceptionTimes++;
        }
    }
}