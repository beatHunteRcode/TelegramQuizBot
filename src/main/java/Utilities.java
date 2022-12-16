import java.util.Arrays;
import java.util.List;

public class Utilities {

    public static String BOT_USERNAME = "null";

    public static final String BOT_TOKEN_FILE_PATH = "./input/bot_token";
    public static final String CONFIG_FILE_PATH = "./input/config.json";
    public static final String QUESTION_REPLIES_FILE_PATH = "./input/question_replies.json";
    public static final String HELLO_IMAGES_URLS_FILE_PATH = "./input/hello_images.json";
    public static final String RESTART_QUIZ_URLS_IMAGES_FILE_PATH = "./input/restart_quiz_images.json";
    public static final String HELLO_PHRASES_FILE_PATH = "./input/hello_phrases.json";
    public static final String RESTART_QUIZ_PHRASES_FILE_PATH = "./input/restart_quiz_phrases.json";
    public static final String QUESTIONS_FILE_PATH = "./input/questions.json";
    public static final String REWARDS_FILE_PATH = "./input/rewards.json";
    public static final String MAIN_COMPLIMENTS_FILE_PATH = "./input/main_compliments.json";

    public static int getRndIntInRange(int min, int max){
        return (int) (Math.random()*((max-min)+1))+min;
    }

}
