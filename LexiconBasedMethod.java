import java.util.List;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class LexiconBasedMethod
{
    private List<String> _adverbList;
    private List<String> _positiveList;
    private List<String> _negativeList;

    public LexiconBasedMethod()
    {
        _positiveList = new ArrayList<String>();
        _negativeList = new ArrayList<String>();
        _adverbList = new ArrayList<String>();
    }

    //讀取字典
    public void readTxt() throws IOException
    {
        //public static void readTxt(String [] argv) throws IOException
        BufferedReader positiveBufferReader = new BufferedReader(new InputStreamReader(new FileInputStream("NTUSD_positive_unicode.txt"), "utf8"));
        BufferedReader negativeBufferReader = new BufferedReader(new InputStreamReader(new FileInputStream("NTUSD_negative_unicode.txt"), "utf8"));
        BufferedReader adverbBufferReader = new BufferedReader(new InputStreamReader(new FileInputStream("NTUSD_adverb_unicode.txt"), "utf8"));
        //positiveBufferReader.read();
        //negativeBufferReader.read();
        //adverbBufferReader.read();

        while (positiveBufferReader.ready())
        {
            _positiveList.add(positiveBufferReader.readLine());
            //br.readLine();
        }

        for (String e : _positiveList)
        {
            System.out.println("我是正面詞:" + e.toString());
        }

        while (negativeBufferReader.ready())
        {
            _negativeList.add(negativeBufferReader.readLine());
        }

        /*for (String e : _negativeList)
        {
            System.out.println("我是負面詞:" + e.toString());
        }*/

        while (adverbBufferReader.ready())
        {
            _adverbList.add(adverbBufferReader.readLine());
        }

        for (String e : _adverbList)
        {
            System.out.println("我是副詞:" + e.toString());
        }
    }

    //計算字典法分數
    public int CalculateScore(List<Term> termList)
    {
        int score = 0;
        boolean isAdv = false;
        int isComplete = 0;

        for (Term element : termList)
        {
            isComplete = 0;
            for (String  adv : _adverbList)
            {
                if (adv.equals(element.GetTerm()))
                {
                    isAdv = true;
                    isComplete = 1;
                    break;
                }
            }

            if (isComplete == 1)
            {
                continue;
            }

            for (String positive : _positiveList)
            {
                if (positive.equals(element.GetTerm()))
                {
                    if (isAdv)
                    {
                        score = score + 2 * 1;
                        isAdv = false;
                    }
                    else
                    {
                        score = score + 1;
                    }
                    isComplete = 1;
                    break;
                }
            }

            if (isComplete == 1)
            {
                continue;
            }

            for (String negative : _negativeList)
            {
                if (negative.equals(element.GetTerm()))
                {
                    if (isAdv)
                    {
                        score = score - 2 * 1;
                        isAdv = false;
                    }
                    else
                    {
                        score = score - 1;
                    }
                    break;
                }
            }
        }
        return score;
    }
}