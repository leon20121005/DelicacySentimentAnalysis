package SentimentAnalyzer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SentimentAnalyzer
{
    private static int NumberOfThread = 4;
    // dictionary and Segmenter
    private static SentimentalDictionary dict;
    private static SegmentChinese seg;

    // reader and IO filenames
    private TextReader txtReader;
    private String filenameOpinion = new String("opinion.txt");
    private String filenameResult = new String("result.txt");
    // number of positive answers and the total number of opinions
    private int positive;
    private int total_opinions;
    // frequency recorder
    private FrequencyRecorder f_rec;
    // OutputWriter
    private FileWriter fw;

    // each Callable holds one opinion, returning the output string generated
    public class SACallable implements Callable<String>
    {
        private int index;

        SACallable(int _index)
        {
            index = _index;
        }

        public String call()
        {
            String output = new String();
            try
            {
                int total_rate = 0;
                ArrayList<String> opinion = txtReader.GetTextByIndex(index);
                ArrayList<String> keywords = new ArrayList<String>();
                ArrayList<String> keyadvs = new ArrayList<String>();
                for (String sentence : opinion)
                {
                    // get one sentence in the "index th" opinion
                    // flag = 2 if any adv is found
                    int rate = 0, flag = 1;
                    // disassemble the sentence and check adverbs
                    for (int length = sentence.length(); length > 0; length--)
                    {
                        for (int endIndex = sentence.length(); endIndex >= length; endIndex--)
                        {
                            String word = sentence.substring(endIndex - length, endIndex);
                            // key adverb found
                            if (dict.checkAdverb(word))
                            {
                                flag = 2;
                                keyadvs.add(word);
                                sentence = sentence.replaceAll(word, "");
                                length = sentence.length() + 1;
                                break;
                            }
                        }
                    }
                    // disassemble the sentence and check sentimental words
                    for (int length = sentence.length(); length > 0; length--)
                    {
                        for (int endIndex = sentence.length(); endIndex >= length; endIndex--)
                        {
                            String word = sentence.substring(endIndex - length, endIndex);
                            // keyword found
                            if (dict.GetWordScore(word) != 0)
                            {
                                rate += flag * dict.GetWordScore(word);
                                keywords.add(word);
                                sentence = sentence.replaceAll(word, "");
                                length = sentence.length() + 1;
                                break;
                            }
                        }
                    }
                    // check if the shifter exists
                    if (sentence.contains("?�") || sentence.contains("?"))
                    {
                        rate *= -1;
                    }
                    total_rate += rate;
                }
                synchronized(SentimentAnalyzer.this)
                {
                    if (total_rate >= 0)
                    {
                        positive += 1;
                    }
                }
                output = String.format(Locale.getDefault(), "NO.%d: rate = ", total_rate) + (total_rate >= 0 ? " (Positive)\n" : " (Negative)\n");
                for (String sentence : opinion)
                {
                    String after_seg =  seg.SegWords(sentence, " ");
                    output += (after_seg + " "); // print detail
                    for (String segSentence : after_seg.split(" "))
                    {
                        if (segSentence.length() <= 1)
                        {
                            continue;
                        }
                        else if (total_rate >= 0)
                        {
                            f_rec.AddPositiveFrequency(segSentence);
                        }
                        else
                        {
                            f_rec.AddNegativeFrequency(segSentence);
                        }
                    }
                }
                output += "\nKeyWords Found: ";
                for (String word : keywords)
                {
                    output += String.format(Locale.getDefault(), "%s(%d) ", word, dict.GetWordScore(word));
                }
                for (String word : keyadvs)
                {
                    output += String.format(Locale.getDefault(), "%s(adv) ", word);
                }
                output += "\n\n";
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return output;
        }
    }

    // setup the static SegmentChinese, KeywordFinder and SentimentalDictionary before work()
    public SentimentAnalyzer()
    {
        seg = SegmentChinese.GetInstance();
        KeywordFinder.GetInstance();
        dict = SentimentalDictionary.GetInstance();
    }

    // set specific I/O files
    public SentimentAnalyzer(String inputFile, String outputFile)
    {
        seg = SegmentChinese.GetInstance();
        KeywordFinder.GetInstance();
        dict = SentimentalDictionary.GetInstance();
        filenameOpinion = inputFile;
        filenameResult = outputFile;
    }

    // set specific dictionary files and remove the old instance to make the new one available
    public static void SetDictionary(String positiveDictionary, String negativeDictionary, String adverbDictionary)
    {
        SentimentalDictionary.RemoveInstance();
        SentimentalDictionary.SetFileName(positiveDictionary, negativeDictionary, adverbDictionary);
    }

    // set specific training data and remove the old instance to make the new one available
    public static void SetTrainingData(String trainingFile, String trainingAnswer)
    {
        SentimentalDictionary.RemoveInstance();
        KeywordFinder.removeInstance();
        KeywordFinder.SetFileName(trainingFile, trainingAnswer);
    }

    public static void SetPosNegSORate(double pos_rate, double neg_rate)
    {
        SentimentalDictionary.RemoveInstance();
        KeywordFinder.removeInstance();
        KeywordFinder.SetSORate(pos_rate, neg_rate);
    }

    public static void SetSORate(double _rate)
    {
        SentimentalDictionary.RemoveInstance();
        KeywordFinder.removeInstance();
        KeywordFinder.SetSORate(_rate, _rate);
    }

    public static void SetNumberOfThread(int _numberOfThread)
    {
        NumberOfThread = _numberOfThread;
        KeywordFinder.SetNumberOfThreads(_numberOfThread);
    }

    private void analyze() throws IOException, InterruptedException, ExecutionException
    {
        positive = 0;
        total_opinions = txtReader.GetSize();
        System.out.println("Now Analyzing...");
        ExecutorService executor = Executors.newFixedThreadPool(NumberOfThread);
        ArrayList<Future<String>> resList = new ArrayList<Future<String>>();
        for (int i = 0 ; i < total_opinions; i++)
        {
            Callable<String> task = new SACallable(i);
            Future<String> result = executor.submit(task);
            resList.add(result);
        }
        executor.shutdown();
        for (Future<String> result : resList)
        {
            fw.write(result.get());
        }
    }

    public void Work()
    {
        try
        {
            long beginTime = System.currentTimeMillis();
            // reading
            txtReader = new TextReader();
            txtReader.ReadText(filenameOpinion);
            // create recorder
            f_rec = new FrequencyRecorder();
            fw = new FileWriter(filenameResult);
            // start analyzing
            analyze();
            // output message
            System.out.println("Completed!");
            System.out.println("Time for Analyzing: " + ((System.currentTimeMillis() - beginTime) / 1000.0) + " second(s)");
            System.out.println("Number of Words in Dictionary: " + dict.GetSize());
            System.out.println(String.format(Locale.getDefault(), "Positive/Negative: %d/%d", positive , total_opinions - positive));
            System.out.println("Frequent Words(>=500): " + f_rec.GetFrequentWordsString(500));
            fw.write("Top Ten Keywords from Positive Opinions: ");
            for (String s : f_rec.GetTopTenPositiveWords())
            {
                fw.write(String.format(Locale.getDefault(), "%s(%d) ", s, f_rec.GetPositiveFrequency(s)));
            }
            fw.write("\nTop Ten Keywords from Negative Opinions: ");
            for (String s : f_rec.GetTopTenNegativeWords())
            {
                fw.write(String.format(Locale.getDefault(), "%s(%d) ", s, f_rec.GetNegativeFrequency(s)));
            }
            fw.write("\n");
            fw.flush();
            fw.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}