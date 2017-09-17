import java.util.List;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class LexiconBasedMethod
{
    private static List<String> _adverbList;
    private static List<String> _positiveList;
    private static List<String> _negativeList;

    public LexiconBasedMethod()
    {
        _positiveList = new ArrayList<String>();
        _negativeList = new ArrayList<String>();
        _adverbList = new ArrayList<String>();
    }
    //讀取字典
    public static void readTxt() throws IOException
    {
        //public static void readTxt(String [] argv) throws IOException
		FileReader positiveReader = new FileReader("NTUSD_positive_unicode.txt");
        FileReader negativeReader = new FileReader("NTUSD_negative_unicode.txt");
        FileReader adverbReader = new FileReader("NTUSD_adverb_unicode.txt");
		BufferedReader positiveBufferReader = new BufferedReader(positiveReader);
        BufferedReader negativeBufferReader = new BufferedReader(negativeReader);
        BufferedReader adverbBufferReader = new BufferedReader(adverbReader);
		while (positiveBufferReader.ready())
        {
            _positiveList.add(positiveBufferReader.readLine());
			//br.readLine();
		}
		positiveReader.close();

        while (negativeBufferReader.ready())
        {
            _negativeList.add(negativeBufferReader.readLine());
		}
		negativeReader.close();

        while (adverbBufferReader.ready())
        {
            _adverbList.add(adverbBufferReader.readLine());
		}
		adverbReader.close();
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
                if (adv == element.GetTerm())
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
                if (positive == element.GetTerm())
                {
                    if (isAdv)
                    {
                        score = score + 2 * 1;
                        isAdv = false;
                    }
                    else
                    {
                        score = score - 1;
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
                if (negative == element.GetTerm())
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