import java.util.List;
import java.util.Scanner;

public class main
{
    public static void main(String[] args) throws Exception
    {
        IPeenCrawler crawler = new IPeenCrawler();
        System.out.println(crawler.Search("炸雞"));
        crawler.SaveResult();

        PIXNETCrawler pixnet = new PIXNETCrawler();
        // pixnet.GetContent();
        // pixnet.SaveResult();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the CKIP Account ID:");
        String id = scanner.nextLine();
        System.out.println("Enter the CKIP Account password:");
        String password = scanner.nextLine();

        CKIPClient client = new CKIPClient(id, password);
        client.SendRequest(null);
        client.ParseResult();

        System.out.println(client.GetReceivedRawText());

        List<String> sentenceList = client.GetSentenceList();
        for (String sentence : sentenceList)
        {
            System.out.println(sentence);
        }

        List<Term> termList = client.GetTermList();
        for (Term term : termList)
        {
            System.out.println(term.GetTerm() + " (" + term.GetTag() + ")");
        }

        LexiconBasedMethod lexiconBasedMethod = new LexiconBasedMethod();
        lexiconBasedMethod.readTxt();
        int _score = lexiconBasedMethod.CalculateScore(termList);
        System.out.println("我是分數");
        System.out.println(_score);
    }
}