package SentimentAnalyzer;

import java.io.IOException;
import java.util.ArrayList;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;
import com.huaban.analysis.jieba.SegToken;

public class SegmentChinese
{
    //a static segmented shared for all analyzers
    private static SegmentChinese segment;

    protected JiebaSegmenter jiebaSegmenter;

    public SegmentChinese()
    {
        jiebaSegmenter = new JiebaSegmenter();
    }

    //return already prepared segment, if not found, then create it
    public static SegmentChinese GetInstance()
    {
        if (segment == null)
        {
            synchronized(SegmentChinese.class)
            {
                if (segment == null)
                {
                    segment = new SegmentChinese();
                    return segment;
                }
            }
        }
        return segment;
    }

    //segment txt and save by arraylist
    public ArrayList<String> GetSegmentList(String txt) throws IOException
    {
        ArrayList<String> output = new ArrayList<String>();
        for (SegToken token : jiebaSegmenter.process(txt, SegMode.INDEX))
        {
            if (!token.word.isEmpty())
            {
                output.add(token.word);
            }
        }
        return output;
    }

    //segment txt and save by string, every word split by wordSplit
    public String SegWords(String txt, String wordSpilt) throws IOException
    {
        String output = new String("");
        for (SegToken token : jiebaSegmenter.process(txt, SegMode.INDEX))
        {
            output += (token.word + wordSpilt);
        }
        return output;
    }
}