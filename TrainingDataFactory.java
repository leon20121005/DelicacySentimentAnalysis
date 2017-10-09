import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class TrainingDataFactory
{
    public TrainingDataFactory()
    {
    }

    public boolean GenerateTrainingData(List<Comment> commentList)
    {
        StringBuilder builder = new StringBuilder();

        for (Comment comment : commentList)
        {
            if (comment.GetContent() != null)
            {
                builder.append(comment.GetContent()).append("\n");
            }
        }
        builder.deleteCharAt(builder.length() - 1);

        try
        {
            PrintWriter writer = new PrintWriter("delicacy_training_data.txt", "UTF-8");
            writer.println(builder.toString());
            writer.flush();
            writer.close();

            return true;
        }
        catch (FileNotFoundException | UnsupportedEncodingException exception)
        {
            return false;
        }
    }
}