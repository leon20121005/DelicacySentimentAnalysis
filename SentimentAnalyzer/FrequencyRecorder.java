package SentimentAnalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FrequencyRecorder
{
    private HashMap<String, Integer> frequencyPoisitiveMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> frequencyNegativeMap = new HashMap<String, Integer>();

    // add the positive frequency of the specific string
    public synchronized void AddPositiveFrequency(String _string)
    {
        if (!frequencyPoisitiveMap.containsKey(_string))
        {
            frequencyPoisitiveMap.put(_string, 1);
        }
        else
        {
            frequencyPoisitiveMap.put(_string, frequencyPoisitiveMap.get(_string) + 1);
        }
    }

    // add the negative frequency of the specific string
    public synchronized void AddNegativeFrequency(String _string)
    {
        if (!frequencyNegativeMap.containsKey(_string))
        {
            frequencyNegativeMap.put(_string, 1);
        }
        else
        {
            frequencyNegativeMap.put(_string, frequencyNegativeMap.get(_string) + 1);
        }
    }

    // get the positive frequency of the specific string
    public int GetPositiveFrequency(String _string)
    {
        if (frequencyPoisitiveMap.containsKey(_string))
        {
            return frequencyPoisitiveMap.get(_string);
        }
        return 0;
    }

    // get the negative frequency of the specific string
    public int GetNegativeFrequency(String _string)
    {
        if (frequencyNegativeMap.containsKey(_string))
        {
            return frequencyNegativeMap.get(_string);
        }
        return 0;
    }

    // get a Set containing all the recorded strings
    public Set<String> GetRecordedStrings()
    {
        Set<String> output = new HashSet<String>(frequencyPoisitiveMap.keySet());
        output.addAll(frequencyNegativeMap.keySet());
        return output;
    }

    public String GetFrequentWordsString(int _base)
    {
        String output = new String();
        for (String key : GetRecordedStrings())
        {
            int p = GetPositiveFrequency(key);
            int n = GetNegativeFrequency(key);
            if (p + n >= _base)
            {
                output += (key + "(" + (p + n) + ") ");
            }
        }
        return output;
    }

    // return an ArrayList containing top ten positive words
    public ArrayList<String> GetTopTenPositiveWords()
    {
        ArrayList<String> output = new ArrayList<String>();
        ArrayList<Map.Entry<String, Integer>> list_entry = new ArrayList<Map.Entry<String, Integer>>(frequencyPoisitiveMap.entrySet());
        Collections.sort
        (
            list_entry,
            new Comparator<Map.Entry<String, Integer>>()
            {
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2)
                {
                    return o2.getValue() - o1.getValue();
                }
            }
        );
        for (Map.Entry<String, Integer> entry : list_entry)
        {
            if (output.size() == 10)
            {
                break;
            }
            output.add(entry.getKey());
        }
        return output;
    }

    // return an ArrayList containing top ten negative words
    public ArrayList<String> GetTopTenNegativeWords()
    {
        ArrayList<String> output = new ArrayList<String>();
        ArrayList<Map.Entry<String, Integer>> list_entry = new ArrayList<Map.Entry<String, Integer>>(frequencyNegativeMap.entrySet());
        Collections.sort
        (
            list_entry,
            new Comparator<Map.Entry<String, Integer>>()
            {
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2)
                {
                    return o2.getValue() - o1.getValue();
                }
            }
        );
        for (Map.Entry<String, Integer> entry : list_entry)
        {
            if (output.size() == 10)
            {
                break;
            }
            output.add(entry.getKey());
        }
        return output;
    }

    // print the frequencies of positive and negative
    public void PrintFrequency()
    {
        for (String key : frequencyPoisitiveMap.keySet())
        {
            System.out.println(key + "(+" + frequencyPoisitiveMap.get(key) + ")");
        }
        for (String key : frequencyNegativeMap.keySet())
        {
            System.out.println(key + "(-" + frequencyNegativeMap.get(key) + ")");
        }
    }
}