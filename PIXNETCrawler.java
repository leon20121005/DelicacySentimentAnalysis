import java.net.URL;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileNotFoundException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class PIXNETCrawler
{
    private final String HOME_URL = "http://zineblog.com.tw/blog/post/45501739";
    private final String HOME_CONDITION = "a[href]:contains(【)";
    private final String ADDRESS_CONDITION = "span:contains(地址)";
	private final String ARTICLE_CONDITION = "div.entry-content";
	private final int MAX_ARTICLE = 5;
    private StringBuilder _builder;

    public PIXNETCrawler()
    {
        _builder = new StringBuilder();
    }

    public String GetContent()
    {
		try
		{
			Document doc = Jsoup.connect(HOME_URL).get();
			String title = doc.title();
			Elements links = doc.select(HOME_CONDITION);

			_builder.append(title).append("\n");

			for (int i = 0; i < MAX_ARTICLE; i++)
			{
				_builder.append("\n").append("Title: ").append(links.get(i).text()); //Home底下每個文章的Title
				_builder.append("\n").append("Link: ").append(links.get(i).attr("href")); //Home底下每個文章的連結
				GetAddress(links.get(i).attr("href")); //找出文章裡面的地址
				GetArticle(links.get(i).attr("href")); //找出文章裡面評論的部分
			}
		}
		catch (IOException e)
		{
			_builder.append("\n").append("Error: ").append(e.getMessage());
		}
        return _builder.toString();
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
                    _builder.append("\n").append("Address: ").append(segment);
                }
            }
        }
        catch (IOException e)
        {
            _builder.append("Error: ").append(e.getMessage());
        }
        catch (IndexOutOfBoundsException e)
        {
            _builder.append("\n").append("Address: none");
        }
    }

	private void GetArticle(String link)
	{
        try
        {
            Document doc = Jsoup.connect(link).get();
            Elements articles = doc.select(ARTICLE_CONDITION);
            String[] segments = articles.get(0).text().split(" ");

            for (String segment : segments)
            {
				_builder.append("\n").append("Content: ").append(segment);
            }
			_builder.append("\n");
        }
        catch (IOException e)
        {
            _builder.append("Error: ").append(e.getMessage()).append("\n");
        }
        catch (IndexOutOfBoundsException e)
        {
            _builder.append("\n").append("Content: none").append("\n");
        }
	}
}