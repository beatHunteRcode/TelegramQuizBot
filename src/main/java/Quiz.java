import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Quiz {

    public final List<Question> questionsList;
    public int rightAnswersCount;
    public int currentQuestionIndex;
    public boolean isQuizzing;
    public long userId;

    public Map<Integer, Boolean> resultsMap;

    public Quiz(Long userId, List<Question> questionsList) {
        this.userId = userId;
        this.questionsList = questionsList;

        resultsMap = new HashMap<>();
    }

    public void start() {
        restart();
    }

    public void restart() {
        rightAnswersCount = 0;
        currentQuestionIndex = 0;
        isQuizzing = true;
    }

}
