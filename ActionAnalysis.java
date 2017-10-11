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
    private static String filePositive = new String("positive.txt");
    private static String fileNegative = new String("negative.txt");
    private static String fileAdverb = new String("adverb.txt");
    private static String fileOpinion = new String("opinion.txt");
    private static String fileTrain = new String("delicacy_training_data.txt");
    private static String fileAnswer = new String("delicacy_training_answer.txt");
    private static String valueSO = new String("3");
    private static String valueThread = new String("4");

    public ActionAnalysis()
    {
        filePositive = "positive.txt";
        fileNegative = "negative.txt";
        fileAdverb = "adverb.txt";
        fileOpinion = "opinion.txt";
        fileTrain = "delicacy_training_data.txt";
        fileAnswer = "delicacy_training_answer.txt";
        valueSO = "3";
        valueThread = "4";
    }

    public static void Run()
    {
    //    try
    //    {
    //         System.setOut(new PrintStream("log.txt"));
    //         System.setErr(new PrintStream("log.txt"));
            SentimentAnalyzer.SetDictionary(filePositive, fileNegative, fileAdverb);
            SentimentAnalyzer.SetTrainingData(fileTrain, fileAnswer);
            SentimentAnalyzer.SetSORate(Double.parseDouble(valueSO));
            SentimentAnalyzer.SetNumberOfThread(Integer.parseInt(valueThread));
            SentimentAnalyzer sa = new SentimentAnalyzer(fileOpinion, "out.txt");
            sa.Work();
        //     BufferedReader br = new BufferedReader(new FileReader("log.txt"));
        //     String tmp = br.readLine();
        //     while (tmp != null)
        //     {
        //         System.out.println(tmp + "\n");
        //         tmp = br.readLine();
        //     }
        //     br.close();
        // }
        // catch (IOException ioe)
        // {
        //     ioe.printStackTrace();
        // }
        return;
    }
    
    public static double Analysis(String comment, String outFileName)
    {
    	double score = 0;
        SentimentAnalyzer.SetDictionary(filePositive, fileNegative, fileAdverb);
        SentimentAnalyzer.SetSORate(Double.parseDouble(valueSO));
        SentimentAnalyzer.SetNumberOfThread(Integer.parseInt(valueThread));
        SentimentAnalyzer sa = new SentimentAnalyzer(comment, outFileName);
        score = sa.WorkCommentAnalysis();
        return(score);
    }
}