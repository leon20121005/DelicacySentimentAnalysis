import SentimentAnalyzer.SentimentAnalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;

public class ActionAnalysis
{
    private String filePositive = new String("positive.txt");
    private String fileNegative = new String("negative.txt");
    private String fileAdverb = new String("adverb.txt");
    private String fileOpinion = new String("Opinion.txt");
    private String fileTrain = new String("train.txt");
    private String fileAnswer = new String("answer.txt");
    private String valueSO = new String("3");
    private String valueThread = new String("4");

    public ActionAnalysis()
    {
        filePositive = "positive.txt";
        fileNegative = "negative.txt";
        fileAdverb = "adverb.txt";
        fileOpinion = "Opinion.txt";
        fileTrain = "train.txt";
        fileAnswer = "answer.txt";
        valueSO = "3";
        valueThread = "4";
    }

    public static void Run()
    {
        try
        {
            System.setOut( new PrintStream("./log/log.txt") );
            System.setErr( new PrintStream("./log/log.txt") );
            SentimentAnalyzer.SetDictionary( filePositive, fileNegative, fileAdverb);
            SentimentAnalyzer.SetTrainingData( fileTrain, fileAnswer);
            SentimentAnalyzer.SetSORate( Double.parseDouble(valueSO));
            SentimentAnalyzer.SetNumberOfThread( Integer.parseInt(valueThread));
            SentimentAnalyzer sa = new SentimentAnalyzer(fileOpinion, "result.txt");
            sa.Work();
            BufferedReader br = new BufferedReader( new FileReader("./log/log.txt") );
            String tmp = br.readLine();
            while(tmp != null)
            {
                System.out.println(tmp + "\n");
                tmp = br.readLine();
            }
            br.close();
        }
        catch (IOException ioe)	
        {
            ioe.printStackTrace();
        }
        return;
    }
}