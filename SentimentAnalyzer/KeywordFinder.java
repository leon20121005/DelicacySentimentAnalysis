package SentimentAnalyzer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.ExtendedSSLSession;

public class KeywordFinder
{
    //static keywordfinder shared for all analyzers
    private static KeywordFinder finder;
    //training and answer filename
    private static String fileNameOfTraining = new String("");
    private static String fileNameOfAnswer = new String("");
    private static int NumberOfThread = 4;

    //set so_rate threshold
    private static double positiveSORate = 3d;
    private static double negativeSORate = 3d;

    //setup Segmenter
    private static SegmentChinese segmentChinese = SegmentChinese.GetInstance();

    //create dictionary and textreader
    private SentimentalDictionary dictionary = new SentimentalDictionary();
    private TextReader textReader = new TextReader();
    //create frequency recorder
    private FrequencyRecorder frequencyRecorder = new FrequencyRecorder();

    //an arraylist,it contains the answers of training data
    private ArrayList<Boolean> ans  = new ArrayList<Boolean>();
    private int numberOfAnsIsPositive = 0;
    private int numberOfAnsIsNegative = 0;

    //each runnalbe object hold one opinion, separating the opinion into words
    public class FrequencyRunnable implements Runnable
    {
        private int _index;
        FrequencyRunnable(int index)
        {
            _index = index;
        }
        public void run()
        {
            ArrayList<String> opinion = textReader.GetTextByIndex(_index);
            for (String sentence : opinion)
            {
                try
                {
                    for (String subSentence : segmentChinese.GetSegmentList(sentence))
                    {
                        if (subSentence.length() <= 1)
                        {
                            continue;
                        }
                        else if (ans.get(_index))
                        {
                            frequencyRecorder.AddPositiveFrequency(subSentence);
                        }
                        else
                        {
                            frequencyRecorder.AddNegativeFrequency(subSentence);
                        }
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    // each Runnable Object holds one string, determining whether if should be added to the dictionary or not
    public class DictionaryRunnable implements Runnable
    {
        private String s;
        DictionaryRunnable(String _s)
        {
            s = _s;
        }
        public void run()
        {
            if (SO(s) > positiveSORate)
            {
                dictionary.AddPositiveWord(s);
            }
            else if (SO(s) < -negativeSORate)
            {
                dictionary.AddNegativeWord(s);
            }
        }

    }

    //return prepared finder,if not found,then create one
    public static KeywordFinder GetInstance()
    {
        if (finder == null)
        {
            synchronized(KeywordFinder.class)
            {
                if (finder == null)
                {
                    finder = new KeywordFinder();
                    long beginTime = System.currentTimeMillis();
                    finder.ReadTrainingData();
                    finder.Train();
                    finder.PrintToFile();
                    System.out.println("Time for training" + (System.currentTimeMillis() - beginTime) / 1000.0 + "second(s)");
                    return finder;
                }
            }
        }
        return finder;
    }

    //remoce current finder due to some setting change
    public static void removeInstance()
    {
        finder = null;
    }

    public static void SetFileName(String filenameTrain, String filenameAnswer)
    {
        fileNameOfTraining = filenameTrain;
        fileNameOfAnswer = filenameAnswer;
    }

    public static void SetSORate(double positiveRate, double negativeRate)
    {
        positiveSORate = positiveRate;
        negativeSORate = negativeRate;
    }

    public static void SetNumberOfThreads(int numberthread)
    {
        NumberOfThread = numberthread;
    }

    private double SO(String string)
    {
        //so formula
        return Math.log(((double)frequencyRecorder.GetPositiveFrequency(string) + 0.1) / ((double)frequencyRecorder.GetNegativeFrequency(string) + 0.1) * ((double)numberOfAnsIsNegative + 0.1) / ((double)numberOfAnsIsPositive + 0.1));
    }

    //
    public void ReadTrainingData()
    {
        try
        {
            // readAnswer
            System.out.println("Accessing " + fileNameOfAnswer);
            FileReader fr = new FileReader(fileNameOfAnswer);
            BufferedReader br = new BufferedReader(fr);
            String temp = br.readLine();
            while (temp != null)
            {
                if (temp.trim().equals("P"))
                {
                    ans.add(true);
                    numberOfAnsIsPositive += 1;
                }
                else
                {
                    ans.add(false);
                    numberOfAnsIsNegative += 1;
                }
                temp = br.readLine();
            }
            br.close();
            // readText
            textReader.ReadText(fileNameOfTraining);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void Train()
    {
        int n = textReader.GetSize();
        assert(n == ans.size() && n != 0);
        ExecutorService frequencyExecutor = Executors.newFixedThreadPool(NumberOfThread);
        ExecutorService dictionaryExecutor = Executors.newFixedThreadPool(NumberOfThread);
        for (int i = 0; i < n; i++)
        {
            Runnable task = new FrequencyRunnable(i);
            frequencyExecutor.execute(task);
        }
        frequencyExecutor.shutdown();
        while (!frequencyExecutor.isTerminated())
        {
        }
        for (String s : frequencyRecorder.GetRecordedStrings())
        {
            Runnable task = new DictionaryRunnable(s);
            dictionaryExecutor.execute(task);
        }
        dictionaryExecutor.shutdown();
        while (!dictionaryExecutor.isTerminated())
        {
        }
    }

    public void PrintToFile()
    {
        try
        {
            System.out.println("Saving Results into \"pos_by_training.txt\" and \"neg_by_training.txt\"");
            FileWriter fileWriterPositive = new FileWriter("pos_by_training.txt");
            for (String s : dictionary.GetPositiveArrayList())
            {
                fileWriterPositive.write(s + "\n");
            }
            FileWriter fileWriterNegative = new FileWriter("neg_by_training.txt");
            for (String s : dictionary.GetNegativeArrayList())
            {
                fileWriterNegative.write(s + "\n");
            }
            fileWriterPositive.flush();
            fileWriterNegative.flush();
            fileWriterPositive.close();
            fileWriterNegative.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}