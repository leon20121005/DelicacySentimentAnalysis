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
        client.Send();

        System.out.println(client.GetReceivedRawText());
        System.out.println(client.GetSentenceList().get(0));
        System.out.println(client.GetTermList().get(0).GetTerm());
        System.out.println(client.GetTermList().get(0).GetTag());
    }
}