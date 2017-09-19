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
            root.addElement("text").addText("店家有分成吧檯前的個人桌以及雙人桌、四人桌，甚至可以二、四人桌合併的六人桌因此不論是一個人來還是跟朋友，甚至是家人都非常的合適而店內採用簡單的日式風格搭配可愛的北歐風格搭配讓人沒有壓迫感~另外店家還很貼心的在每個椅子底下會放籃子可以把包包等東西放在裡這對隨身帶著大包包的人來說非常的貼心~~另外這邊有附冷、熱飲是無限量自取的哦~還有醬料區檸檬醬油是完全用新鮮檸檬特製調配，非常適合沾炊煮食材以及杜老爺冰淇淋~~店家很大方是使用貴一些些的杜老爺冰淇淋不是客惟您~菜單這家店最特別的就是有三種不同種類的火鍋方式一個是蒸煮鍋、一個是石頭鍋、另一種則是常見的涮涮鍋另外也有一些單點的食材可以選擇~不過可以建議大家可以先點主鍋，等到吃不夠時再點因為店家的份量滿大的~此外還有一項秘密餐點~等等介紹喜歡的話別忘記按個讚持續關注哦");
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