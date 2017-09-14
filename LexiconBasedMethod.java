import java.util.List;

public class LexiconBasedMethod
{
    //計算字典法分數
    public int CalcuateScore(List<Term> termList)
    {
        int score = 0;
        boolean isAdv = false;
        int isComplete = 0;

        List<Term> advList = null;
        List<Term> positiveList = null;
        List<Term> negativeList = null;
        
        for (Term element : termList)
        {
            isComplete = 0;
            for (Term adv : advList)
            {
                if (adv.GetTerm() == element.GetTerm())
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

            for (Term positive : positiveList)
            {
                if (positive.GetTerm() == element.GetTerm())
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

            for (Term negative : negativeList)
            {
                if (negative.GetTerm() == element.GetTerm())
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