import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.FileNotFoundException;
import java.lang.NullPointerException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class IPeenCrawler
{
    private final String ROOT_URL = "http://www.ipeen.com.tw";
    private final String SHOP_CONDITION = "a[data-label='店名'][href]";
    private final String ADDRESS_CONDITION = "a[data-label='上方地址']";
    private final String COMMENT_LINK_CONDITION = "a[itemprop='discussionUrl url'][href]";
    private final String COMMENT_CONDITION = "div.description";
    private final int MAX_SHOP = 5;
    private final int MAX_COMMENT_PER_SHOP = 2;
    private String _shopListURL;
    private List<Comment> _commentList;
    private Comment _currentComment;

    public IPeenCrawler()
    {
        _commentList = new ArrayList<Comment>();
    }

    //根據類別搜尋美食
    public String Search(String category)
    {
        _shopListURL = "/search/taiwan/000/0-0-0-0/" + category + "/?p=1";
        return GetShopInformation();
    }

    //根據類別和地區搜尋美食
    public String Search(String category, String area)
    {
        _shopListURL = "/search/taiwan/000/0-0-0-0/" + category + "/?p=1&adkw=" + area;
        return GetShopInformation();
    }

    //根據類別和地區搜尋美食，以綜合評價高低作為排序
    public String SearchTop(String category, String area)
    {
        _shopListURL = "/search/taiwan/000/0-0-0-0/" + category + "/?p=1&adkw=" + area + "&so=sat";
        return GetShopInformation();
    }

    //儲存結果至文字檔案
    public boolean SaveResult()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("List size: ").append(_commentList.size()).append("\n");

        for (Comment comment : _commentList)
        {
            builder.append("\n").append("Name: ").append(comment.GetShopName());
            builder.append("\n").append("Link: ").append(comment.GetShopLink());
            builder.append("\n").append("Address: ").append(comment.GetShopAddress());
            builder.append("\n").append("Content: ").append(comment.GetContent());
            builder.append("\n");
        }
        try (PrintWriter writer = new PrintWriter("result.txt"))
        {
            writer.println(builder.toString());
            return true;
        }
        catch (FileNotFoundException exception)
        {
            return false;
        }
    }

    private String GetShopInformation()
    {
        try
        {
            Document doc = Jsoup.connect(ROOT_URL + _shopListURL).get();
            Elements shopLinks = doc.select(SHOP_CONDITION);
            int index = 0;

            // _builder.append(doc.title()).append("\n");

            for (Element shopLink : shopLinks)
            {
                if (index++ == MAX_SHOP)
                {
                    break;
                }
                _currentComment = new Comment();
                _currentComment.SetShopName(shopLink.text()); //設定要放入Comment List的評論的商店名字
                _currentComment.SetShopLink(ROOT_URL + shopLink.attr("href")); //設定要放入Comment List的評論的商店連結
                _currentComment.SetShopAddress(GetAddress(ROOT_URL + shopLink.attr("href"))); //設定要放入Comment List的評論的商店地址
                GetComments(ROOT_URL + shopLink.attr("href"));
            }
        }
        catch (IOException exception)
        {
            return "Error: " + exception.getMessage();
        }
        return "Process successfully completed";
    }

    private String GetAddress(String link)
    {
        try
        {
            Document doc = Jsoup.connect(link).get();
            Element address = doc.select(ADDRESS_CONDITION).first();
            return "Address: " + address.text();
        }
        catch (IOException exception)
        {
            return "Error: " + exception.getMessage();
        }
        catch (NullPointerException exception)
        {
            return "Address: none";
        }
    }

    private void GetComments(String link)
    {
        try
        {
            Document doc = Jsoup.connect(link).get();
            Elements commentLinks = doc.select(COMMENT_LINK_CONDITION);
            int index = 0;

            for (Element commentLink : commentLinks)
            {
                if (index++ == MAX_COMMENT_PER_SHOP)
                {
                    break;
                }
                GetComment(ROOT_URL + commentLink.attr("href"));
            }
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
        catch (IndexOutOfBoundsException exception)
        {
            exception.printStackTrace();
        }
    }

    //從評論的列表中連進去得到一篇篇評論的內容
    private void GetComment(String link)
    {
        try
        {
            Document doc = Jsoup.connect(link).get();
            Element comment = doc.select(COMMENT_CONDITION).first();
            String[] segments = ReviseComment(comment.text());
            StringBuilder builder = new StringBuilder();

            for (String segment : segments)
            {
                if (!"".equals(segment))
                {
                    builder.append(segment).append("\n");
                }
            }
            builder.deleteCharAt(builder.length() - 1);
            _currentComment.SetContent(builder.toString()); //設定要放入Comment List的評論的內容
            _commentList.add(new Comment(_currentComment));  //放入Comment List
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    //修正評論裡面的雜訊並且把評論切割成句子的字串陣列
    private String[] ReviseComment(String comment)
    {
        return comment.replace("\u00a0", "").split(" ");
    }
}