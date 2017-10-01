package SentimentAnalyzer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class SentimentalDictionary
{
    //static dictionary share for all analyzers
    private static SentimentalDictionary dictionary;

    //positive,negative,adv filename
    private static String positiveFilenName = new String("positive.txt");
    private static String negativeFileName = new String("negative.txt");
    private static String adverbFileName = new String("advrb.txt");

    //a HashMap hoiding sentimental words as keys
    private HashMap<String, Integer> myDictionary = new HashMap<String, Integer>();
    //a HashMap holding adverb as keys
    private HashMap<String, Boolean> myAdverb = new HashMap<String, Boolean>();

    //return the prepared dictionary,if not found,then creat one
    public static SentimentalDictionary GetInstance()
    {
        if (dictionary == null)
        {
            synchronized(SentimentalDictionary.class)
            {
                if (dictionary == null)
                {
                    dictionary = new SentimentalDictionary();
                    dictionary.MakeDictionary();
                    return dictionary;
                }
            }
        }
        return dictionary;
    }

    //remove the current dictionary due to some setting change
    public static void RemoveInstance()
    {
        dictionary = null;
    }

    public static void SetFileName(String posfilename, String negfilename, String advfilename)
    {
        positiveFilenName = posfilename;
        negativeFileName = negfilename;
        adverbFileName = advfilename;
    }

    //add positive word into dictionary
    public synchronized void AddPositiveWord(String string)
    {
        if (myDictionary.containsKey(string))
        {
            myDictionary.put(string, myDictionary.get(string) + 1);
        }
        else
        {
            myDictionary.put(string, 1);
        }
    }

    //add negative word into dictionary
    public synchronized void AddNegativeWord(String string)
    {
        if (myDictionary.containsKey(string))
        {
            myDictionary.put(string, myDictionary.get(string) - 1);
        }
        else
        {
            myDictionary.put(string, -1);
        }
    }

    //get score of sentimental word,return 0 when not found
    public int GetWordScore(String string)
    {
        if (string.isEmpty() || !myDictionary.containsKey(string))
        {
            return 0;
        }
        if (myDictionary.get(string) > 0)
        {
            return 1;
        }
        return -1;
    }

    //check word if adverb
    public boolean checkAdverb(String string)
    {
        if (myAdverb.containsKey(string) && !string.isEmpty())
        {
            return true;
        }
        return false;
    }

    //return positive words arraylist
    public ArrayList<String> GetPositiveArrayList()
    {
        ArrayList<String> outputList = new ArrayList<String>();
        for (String word : myDictionary.keySet())
        {
            if (myDictionary.get(word) > 0)
            {
                outputList.add(word);
            }
        }
        return outputList;
    }

    //return negative words ArrayList
    public ArrayList<String> GetNegativeArrayList()
    {
        ArrayList<String> outputList = new ArrayList<String>();
        for (String word : myDictionary.keySet())
        {
            if (myDictionary.get(word) < 0)
            {
                outputList.add(word);
            }
        }
        return outputList;
    }

    //get numbers of words in the dictionary(positive + negative + adverb)
    public int GetSize()
    {
        return myDictionary.size() + myAdverb.size();
    }

    //print dictionary
    public void PrintDictionary()
    {
        for (String word : myDictionary.keySet())
        {
            System.out.println(word + "," + myDictionary.get(word));
        }
    }

    // put the words into the the HashMaps from files(positive sentimental words, negative sentimental words, adverbs)
    public void MakeDictionary()
    {
        try
        {
            //access positive words
            String[] filenames = {positiveFilenName, "./docs/pos_by_training.txt"};
            for (String filename : filenames)
            {
                System.out.println("ACCESSING" + filename);
                FileReader fr = new FileReader(filename);
                BufferedReader br = new BufferedReader(fr);
                String temp = br.readLine();
                while (temp != null)
                {
                    AddPositiveWord(temp.trim());
                    temp = br.readLine();
                }
                br.close();
            }
        }
        catch (Exception e)
        {
            System.out.println("File of Positive word not found");
            e.printStackTrace();
        }

        try
        {
            //access negative words
            String[] filenames = {negativeFileName, "./docs/neg_by_training.txt"};
            for (String filename : filenames)
            {
                System.out.println("ACCESSING" + filename);
                FileReader fr = new FileReader(filename);
                BufferedReader br = new BufferedReader(fr);
                String temp = br.readLine();
                while (temp != null)
                {
                    AddNegativeWord(temp.trim());
                    temp = br.readLine();
                }
                br.close();
            }
        }
        catch (Exception e)
        {
            System.out.println("File of Negative word not found");
            e.printStackTrace();
        }

        try
        {
            //access adverb words
            System.out.println("ACCESSING" + adverbFileName);
            FileReader fr = new FileReader(adverbFileName);
            BufferedReader br = new BufferedReader(fr);
            String temp = br.readLine();
            while (temp != null)
            {
                myAdverb.put(temp.trim(), true);
                temp = br.readLine();
            }
            br.close();
        }
        catch (Exception e)
        {
            System.out.println("File of Adverb word not found");
            e.printStackTrace();
        }
    }
}