import java.util.Scanner;

public class LexiconBasedMethod
{
    public static void main(String[] args) throws Exception
    {

    }

    //計算字典法分數
    public int CalcuateScore (List<Term> termList)
    {
        int score = 0;
        int isAdv = 0;
        int iscpmplete = 0;
        for(Term element : termList)
        {
            iscpmplete = 0;
            for(Term Adv : AdvList)
            {
                if(Adv.GetTerm = element.GetTerm)
                {
                    isAdv = 1;
                    iscpmplete = 1;
                    break;
                }
            }

            if(iscpmplete = 1)
            {
                continue;
            }
            
            for(Term positive : positiveList)
            {
                if(positive.GetTerm = element.GetTerm)
                {
                    if(isAdv)
                    {
                        score = score + 2 * 1;
                        isAdv = 0;
                    }
                    else
                    {
                        score = score - 1;
                    }
                    iscpmplete = 1;
                    break;
                }
            }

            if(iscpmplete = 1)
            {
                continue;
            }

            for(Term negative : negativeList)
            {
                if(negative.GetTerm = element.GetTerm)
                {
                    if(isAdv)
                    {
                        score = score - 2 * 1;
                        isAdv = 0;
                    }
                    else
                    {
                        score = score - 1;
                    }
                    break;
                }
            }
        }
    }
}