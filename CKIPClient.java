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
    public void Send()
    {
        try
        {	
            Socket socket = new Socket(_serverIP, _serverPort);
            OutputStreamWriter osWriter = new OutputStreamWriter(socket.getOutputStream());
            PrintWriter printWriter = new PrintWriter(osWriter);
            printWriter.println(CreateDocument().asXML());
            printWriter.flush();

            InputStreamReader isReader = new InputStreamReader(socket.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(isReader);

            _receivedRawText = bufferedReader.readLine();

            bufferedReader.close();
            socket.close();
        }
        catch (java.net.UnknownHostException e)
        {
            e.printStackTrace();
        }
        catch (java.io.IOException e)
        {
            e.printStackTrace();
        }	
        ParseSentence();
        ParseTerm();
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

    //建立要傳送到CKIP的XML格式檔案
    private Document CreateDocument()
    {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("wordsegmentation").addAttribute("version", "0.1");
        root.addElement("option").addAttribute("showcategory", "1");
        root.addElement("authentication").addAttribute("username", _userName).addAttribute("password", _password);
        root.addElement("text").addText("這是一個測試");

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
                for (Iterator<?> j = next.elementIterator(); j.hasNext();)
                {
                    Element element = (Element) j.next();
                    _sentenceList.add(element.getText());
                }
            }
        }
        catch (org.dom4j.DocumentException e)
        {
            e.printStackTrace();
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
}