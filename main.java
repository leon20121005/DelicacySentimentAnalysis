import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

public class main
{
    public static void main(String[] args) throws Exception
    {
        //ActionAnalysis.Run();
        // IPeenCrawler crawler = new IPeenCrawler();
        // System.out.println(crawler.Search("炸雞"));
        // crawler.SaveResult();

        // ZingblogCrawler crawler = new ZingblogCrawler();
        // crawler.GetArticles();
        // crawler.SaveResult();

        long startTime = System.nanoTime();

        List<Comment> commentList = CrawlDelicacyComment();
        ComputeEvaluation(commentList);

        SqlFactory factory = new SqlFactory();
        factory.GenerateSqlFile(commentList);
        System.out.println("跑好了");

        //TrainingDataFactory trainingFactory = new TrainingDataFactory();
        //trainingFactory.GenerateTrainingData(commentList);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("Process time (sec): ");
        System.out.println(duration / 1000000000);

        // Scanner scanner = new Scanner(System.in);
        // System.out.println("Enter the CKIP Account ID:");
        // String id = scanner.nextLine();
        // System.out.println("Enter the CKIP Account password:");
        // String password = scanner.nextLine();

        // CKIPClient client = new CKIPClient(id, password);
        // client.SendRequest(null);
        // client.ParseResult();

        // System.out.println(client.GetReceivedRawText());

        // List<String> sentenceList = client.GetSentenceList();
        // for (String sentence : sentenceList)
        // {
        //     System.out.println(sentence);
        // }

        // List<Term> termList = client.GetTermList();
        // for (Term term : termList)
        // {
        //     System.out.println(term.GetTerm() + " (" + term.GetTag() + ")");
        // }

        // LexiconBasedMethod lexiconBasedMethod = new LexiconBasedMethod();
        // lexiconBasedMethod.readTxt();
        // int _score = lexiconBasedMethod.CalculateScore(termList);
        // System.out.println("我是分數");
        // System.out.println(_score);
    }

    private static List<Comment> CrawlDelicacyComment()
    {
        PIXNETDelicacyExplorer explorer = new PIXNETDelicacyExplorer();
        List<String> bloggerList = explorer.GetDelicacyBlogger();

        List<Comment> commentList = new ArrayList<>();

        for (String blogger : bloggerList)
        {
            String jsonData = explorer.SendRequest(blogger);
            commentList.addAll(explorer.ParseDelicacyComment(jsonData));
            break;
        }
        return commentList;
    }

    //根據每一篇comment的URL解析出美食評論內容並算出分數存入evaluation
    private static void ComputeEvaluation(List<Comment> commentList)
    {
        PIXNETDelicacyExplorer explorer = new PIXNETDelicacyExplorer();
        int counter = 0;
        double score = 0;

        for (Comment comment : commentList)
        {
            explorer.ParseCommentContent(comment, comment.GetShopLink());
            String outFilename = ".\\result\\out" + Integer.toString(counter) + ".txt";
            score = ActionAnalysis.Analysis(comment.GetContent(), outFilename);
            score = (score + 10) / 2;
            System.out.println("這篇分數是" + score);
            comment.SetEvaluation(10);

            if (counter++ > 100)
            {
                break;
            }
        }
    }
}