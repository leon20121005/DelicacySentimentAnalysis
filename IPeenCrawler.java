import java.net.URL;
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
    private final int MAX_COMMENT_PER_SHOP = 1;
    private StringBuilder _builder;
    private String _shopListURL;

    public IPeenCrawler()
    {
        _builder = new StringBuilder();
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
        try (PrintWriter writer = new PrintWriter("result.txt"))
        {   
            writer.println(_builder.toString());
            return true;
        }
        catch (FileNotFoundException e)
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

			_builder.append(doc.title()).append("\n");

            for (Element shopLink : shopLinks)
            {
                if (index++ == MAX_SHOP)
                {
                    break;
                }
                _builder.append("\n").append("Title: ").append(shopLink.text()); //List底下每個文章的Title
				_builder.append("\n").append("Link: ").append(ROOT_URL + shopLink.attr("href")); //List底下每個文章的連結
                _builder.append("\n").append(GetAddress(ROOT_URL + shopLink.attr("href"))); //每個文章裡面的地址
                _builder.append("\n").append(GetComments(ROOT_URL + shopLink.attr("href"))); //每個文章裡面的所有評論
            }
		}
		catch (IOException e)
		{
			_builder.append("\n").append("Error: ").append(e.getMessage());
		}
        return _builder.toString();        
    }

    private String GetAddress(String link)
    {
        try
        {
            Document doc = Jsoup.connect(link).get();
            Element address = doc.select(ADDRESS_CONDITION).first();
            return "Address: " + address.text();
        }
        catch (IOException e)
        {
            return "Error: " + e.getMessage();
        }
        catch (NullPointerException e)
        {
            return "Address: none";
        }
    }

	private String GetComments(String link)
	{
        try
        {
            Document doc = Jsoup.connect(link).get();
            Elements commentLinks = doc.select(COMMENT_LINK_CONDITION);
            StringBuilder builder = new StringBuilder();
            int index = 0;

            for (Element commentLink : commentLinks)
            {
                if (index++ == MAX_COMMENT_PER_SHOP)
                {
                    break;
                }
                builder.append(GetComment(ROOT_URL + commentLink.attr("href"))).append("\n");
            }
            return builder.toString();
        }
        catch (IOException e)
        {
            return "Error: " + e.getMessage();
        }
        catch (IndexOutOfBoundsException e)
        {
            return "Comment: none";
        }
	}

    //從評論的列表中連進去得到一篇篇評論的內容
    private String GetComment(String link)
	{
        try
        {
            Document doc = Jsoup.connect(link).get();
            Element comment = doc.select(COMMENT_CONDITION).first();
            StringBuilder builder = new StringBuilder();
            String[] segments = ReviseComment(comment.text());

            builder.append("Comment: ").append("\n");

            for (String segment : segments)
            {
                if (!"".equals(segment))
                {
                    builder.append(segment).append("\n");
                }
            }
            return builder.toString();
        }
        catch (IOException e)
        {
            return "Error: " + e.getMessage();
        }
	}

    //修正評論裡面的雜訊並且把評論切割成句子的字串陣列
    private String[] ReviseComment(String comment)
    {
        return comment.replace("\u00a0", "").split(" ");
    }
}