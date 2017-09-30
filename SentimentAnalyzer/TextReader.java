package SentimentAnalyzer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class TextReader
{
    //opinionList is put all opinion
    //one row is an opinion,every opinion split into string and put into arraylist
    private ArrayList<ArrayList<String>> opinionList = new ArrayList<ArrayList<String>>();

    public void ReadText(String filename) throws IOException
    {
        try
        {
            System.out.println("Accessing" + filename);
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferReader = new BufferedReader(fileReader);
            String tempString;
            while (bufferReader.ready())
            {
                //read file and replace punctuation marks,letters and number with space
                //split one sentence into string by space,then put into arraylist
                tempString = bufferReader.readLine();
                String rawString = tempString.replaceAll("//pP", "").replaceAll("[a-zA-Z0-9]", " ");
                String[] splitData = rawString.split(" ");
                ArrayList<String> opinion = new ArrayList<String>();
                for (String element : splitData)
                {
                    opinion.add(element);
                }
                opinionList.add(opinion);
                bufferReader.close();
            }
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Text file not found");
            //exception stack trace,can get program run trail
            e.printStackTrace();
        }
    }

    //get opinionList size
    public int GetSize()
    {
        return opinionList.size();
    }

    //use index to get text in opinionList
    public ArrayList<String> GetTextByIndex(int index)
    {
        if (index > opinionList.size())
        {
            //for exception(不確定)
            return new ArrayList<String>();
        }
        else
        {
            return opinionList.get(index);
        }
    }
}