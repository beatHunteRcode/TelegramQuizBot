import java.util.List;

public class Question {

    public String text;
    public List<Answer> answers;
    public List<String> imagesURLsList;

    public Question(String text, List<Answer> answers, List<String> imagesURLsList) {
        this.text = text;
        this.answers = answers;
        this.imagesURLsList = imagesURLsList;
    }
}
