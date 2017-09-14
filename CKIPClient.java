import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class CKIPClient
{
    private String _userName;
    private String _password;
    private String _serverIP;
    private int _serverPort;
    private String _receivedRawText;
    private List<String> _sentenceList;
    private List<Term> _termList;

    public CKIPClient(String userName, String password, String serverIP, int serverPort)
    {
        _userName = userName;
        _password = password;
        _serverIP = serverIP;
        _serverPort = serverPort;
        _sentenceList = new ArrayList<String>();
        _termList = new ArrayList<Term>();
    }

    public CKIPClient(String userName, String password)
    {
        this(userName, password, "140.109.19.104", 1501);
    }

    //建立和CKIP伺服器的Socket連線並傳送資料和接收資料
    public void SendRequest(String article)
    {
        try
        {
            Socket socket = new Socket(_serverIP, _serverPort);
            OutputStreamWriter osWriter = new OutputStreamWriter(socket.getOutputStream());
            PrintWriter printWriter = new PrintWriter(osWriter);
            printWriter.println(CreateDocument(article).asXML());
            printWriter.flush();

            InputStreamReader isReader = new InputStreamReader(socket.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(isReader);

            _receivedRawText = bufferedReader.readLine();

            bufferedReader.close();
            socket.close();
        }
        catch (java.net.UnknownHostException exception)
        {
            exception.printStackTrace();
        }
        catch (java.io.IOException exception)
        {
            exception.printStackTrace();
        }
    }

    public void ParseResult()
    {
        ParseSentence();
        ParseTerm();
    }

    //建立要傳送到CKIP的XML格式檔案
    private Document CreateDocument(String content)
    {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("wordsegmentation").addAttribute("version", "0.1");
        root.addElement("option").addAttribute("showcategory", "1");
        root.addElement("authentication").addAttribute("username", _userName).addAttribute("password", _password);

        //輸入為null的話就加入測試字串
        if (content == null)
        {
            root.addElement("text").addText("吐司烤的酥脆，吃起來很香，由於我有加月見蛋，他們便在吐司中心挖了一個洞並放入這個半熟蛋，形成有特色的擺盤樣式，蛋汁為吐司的乾燥增加了些溼潤，吃起來更順口，讓整個口感上升了一個層次；可惜的是吃到後來蛋汁讓吐司便得有些軟綿，吃起來感覺不是很好，如果蛋汁的量要再少一些會比較恰當。整體來說味道有中上的程度，滿分5顆星我可以給到3.7左右，但價格算是偏貴的，想要偶爾一次的聚餐會是個不錯的選擇。");
        }
        else
        {
            root.addElement("text").addText(content);
        }

        return document;
    }

    //從接收的原始XML中提取每個句子並且儲存到_sentenceList
    private void ParseSentence()
    {
        try
        {
            Document document = DocumentHelper.parseText(_receivedRawText);
            Element root = document.getRootElement();
            Element next;
            for (Iterator<?> i = root.elementIterator("result"); i.hasNext();)
            {
                next = (Element) i.next();
                //找sentence
                for (Iterator<?> j = next.elementIterator(); j.hasNext();)
                {
                    Element element = (Element) j.next();
                    _sentenceList.add(element.getText());
                }
            }
        }
        catch (org.dom4j.DocumentException exception)
        {
            exception.printStackTrace();
        }
    }

    //從_sentenceList中提取每個詞(詞性)並且儲存到_termList
    private void ParseTerm()
    {
        for (String sentence : _sentenceList)
        {
            for (String t : sentence.split("　"))
            {
                if (!"".equals(t))
                {
                    Pattern pattern;
                    Matcher matcher;
                    pattern = Pattern.compile("(\\S*)\\((\\S*)\\)");
                    matcher = pattern.matcher(t);
                    if (matcher.find())
                    {
                        Term term = new Term(matcher.group(1), matcher.group(2));
                        _termList.add(term);
                    }
                }
            }
        }
    }

    public String GetReceivedRawText()
    {
        return _receivedRawText;
    }

    public List<String> GetSentenceList()
    {
        return _sentenceList;
    }

    public List<Term> GetTermList()
    {
        return _termList;
    }
}