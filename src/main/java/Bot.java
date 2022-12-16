import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class Bot extends TelegramLongPollingBot {

    private final List<Question> questionsList;
    private final List<String> helloImagesURLsList;
    private final List<String> questionRepliesList;
    private final List<String> restartQuizImagesURLsList;
    private final List<Phrase> helloPhrasesList;
    private final List<Phrase> restartQuizPhrasesList;
    private final List<MainCompliment> mainComplimentList;

    private final Map<Long, Quiz> usersQuizzesMap;

    private int requiredRightAnswersLow;
    private int requiredRightAnswersMiddle;
    private int requiredRightAnswersHigh;

    private Reward noReward;
    private Reward lowReward;
    private Reward middleReward;
    private Reward highReward;

    private MainCompliment currentMainCompliment;

    public Bot() {
        questionsList = new ArrayList<>();
        helloImagesURLsList = new ArrayList<>();
        questionRepliesList = new ArrayList<>();
        helloPhrasesList = new ArrayList<>();
        restartQuizImagesURLsList = new ArrayList<>();
        restartQuizPhrasesList = new ArrayList<>();
        mainComplimentList = new ArrayList<>();
        usersQuizzesMap = new HashMap<>();
    }

    @Override
    public String getBotUsername() {
        return Utilities.BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        String token = "";
        try {
            BufferedReader reader = null;
            reader = new BufferedReader(new FileReader(Utilities.BOT_TOKEN_FILE_PATH));
            token = reader.readLine();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return token;
    }

    @Override
    public void onRegister() {
        super.onRegister();

        initializeConfig();

        initializeQuestionReplies(questionRepliesList);

        initializeImages(Utilities.HELLO_IMAGES_URLS_FILE_PATH, helloImagesURLsList);
        initializeImages(Utilities.RESTART_QUIZ_URLS_IMAGES_FILE_PATH, restartQuizImagesURLsList);

        initializePhrases(Utilities.HELLO_PHRASES_FILE_PATH, helloPhrasesList);
        initializePhrases(Utilities.RESTART_QUIZ_PHRASES_FILE_PATH, restartQuizPhrasesList);

        initializeRewards();

        initializeMainCompliments(mainComplimentList);

        createQuestions();

        printParsedConfig();
        printParsedQuestionReplies();
        printParsedHelloImages();
        printParsedRestartQuizImages();
        printParsedHelloPhrases();
        printParsedRestartQuizPhrases();
        printParsedRewards();
        printParsedMainCompliments();

        printParsedQuestions();

        System.out.println("-------------- BOT CONFIGURED AND READY TO USE --------------");
    }

    private void initializeConfig() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(new File(Utilities.CONFIG_FILE_PATH), JsonNode.class);
            Utilities.BOT_USERNAME = node.findValue("bot_username").asText();
            requiredRightAnswersLow = node.findValue("required_right_answers_low").asInt();
            requiredRightAnswersMiddle = node.findValue("required_right_answers_middle").asInt();
            requiredRightAnswersHigh = node.findValue("required_right_answers_high").asInt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeQuestionReplies(List<String> questionRepliesList) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(new File(Utilities.QUESTION_REPLIES_FILE_PATH), JsonNode.class);
            List<JsonNode> replies = node.findValue("question_replies").findValues("text");
            if (!replies.isEmpty()) {
                for (JsonNode reply : replies)
                questionRepliesList.add(reply.asText());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeRewards() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(new File(Utilities.REWARDS_FILE_PATH), JsonNode.class);
            noReward = new Reward(
                    node.findValue("no_reward").findValue("text").asText(),
                    node.findValue("no_reward").findValue("image_url").asText()
            );
            lowReward = new Reward(
                    node.findValue("low_reward").findValue("text").asText(),
                    node.findValue("low_reward").findValue("image_url").asText()
            );
            middleReward = new Reward(
                    node.findValue("middle_reward").findValue("text").asText(),
                    node.findValue("middle_reward").findValue("image_url").asText()
            );
            highReward = new Reward(
                    node.findValue("high_reward").findValue("text").asText(),
                    node.findValue("high_reward").findValue("image_url").asText()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void initializePhrases(String filePath, List<Phrase> phrasesList) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(new File(filePath), JsonNode.class);
            ArrayNode phrases = (ArrayNode) node.findValue("phrases");
            if (phrases != null) {
                for (int i = 0; i < phrases.size(); i++) {
                    Phrase phrase = new Phrase();
                    phrase.setPhrase(phrases.get(i).findValue("phrase").asText());
                    phrase.setInlineButtonText(phrases.get(i).findValue("inlineButtonText").asText());
                    phrasesList.add(phrase);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeImages(String filePath, List<String> imagesURLsList) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(new File(filePath), JsonNode.class);
            ArrayNode urls = (ArrayNode) node.findValue("images_urls");
            if (urls != null) {
                for (int i = 0; i < urls.size(); i++) {
                    imagesURLsList.add(urls.get(i).findValue("url").asText());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeMainCompliments(List<MainCompliment> mainComplimentList) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(new File(Utilities.MAIN_COMPLIMENTS_FILE_PATH), JsonNode.class);
            ArrayNode mainCompliments = (ArrayNode) node.findValue("main_compliments");
            if (mainCompliments != null) {
                for (int i = 0; i < mainCompliments.size(); i++) {
                    MainCompliment mainCompliment = new MainCompliment();
                    mainCompliment.setText(mainCompliments.get(i).findValue("text").asText());
                    mainCompliment.setKeyboardButtonText(mainCompliments.get(i).findValue("keyboardButtonText").asText());
                    mainCompliment.setImageURL(mainCompliments.get(i).findValue("image_url").asText());

                    mainComplimentList.add(mainCompliment);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createQuestions() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(new File(Utilities.QUESTIONS_FILE_PATH), JsonNode.class);
            ArrayNode questions = (ArrayNode) node.findValue("questions");
            for (int i = 0; i < questions.size(); i++) {
                String text = questions.get(i).findValue("text").asText();
                ArrayNode answers = (ArrayNode) questions.get(i).findValue("answers");
                List<Answer> parsedAnswersList = new ArrayList<>();
                for (int j = 0; j < answers.size(); j++) {
                    parsedAnswersList.add(new Answer(
                            answers.get(j).findValue("text").asText(),
                            answers.get(j).findValue("isRight").asBoolean()
                    ));
                }
                ArrayNode photosURLs = (ArrayNode) questions.get(i).findValue("images_urls");
                List<String> photosURLsList = new ArrayList<>();
                if (photosURLs != null) {
                    for (int j = 0; j < photosURLs.size(); j++) {
                        photosURLsList.add(photosURLs.get(j).findValue("url").asText());
                    }
                }
                Question question = new Question(text, parsedAnswersList, photosURLsList);
                questionsList.add(question);
            }
        } catch (IOException e) {
            System.err.println("Error during parsing file:\n" + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        handleUpdate(update);
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }

    private ReplyKeyboardMarkup getMainComplimentReplyKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(currentMainCompliment.getKeyboardButtonText()));
        keyboard.add(row1);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    private void sendText(Long who, String what) {
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString()) //Who are we sending a message to
                .text(what).build();    //Message content
        try {
            execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }

    private void sendTextWithDocument(Long who, String text, String documentPath) {
        File document = new File(documentPath);
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(who.toString());
        sendDocument.setDocument(new InputFile().setMedia(document));
        sendDocument.setCaption(text);
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendTextWithImage(Long who, String text, String imagePath) {
        File image = new File(imagePath);
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(who.toString());
        sendPhoto.setPhoto(new InputFile().setMedia(image));
        sendPhoto.setCaption(text);
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendTextWithKeyboard(Long who, String what, ReplyKeyboard keyboard) {
        SendMessage sm = SendMessage.builder().chatId(who.toString())
                .parseMode("HTML").text(what)
                .replyMarkup(keyboard).build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendTextWithImageAndKeyboard(Long who, String text, String imagePath, ReplyKeyboard keyboard) {
        File image = new File(imagePath);
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(who.toString());
        sendPhoto.setPhoto(new InputFile().setMedia(image));
        sendPhoto.setCaption(text);
        sendPhoto.setReplyMarkup(keyboard);
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendTextWithDocumentAndKeyboard(Long who, String text, String documentPath, ReplyKeyboard keyboard) {
        File document = new File(documentPath);
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(who.toString());
        sendDocument.setDocument(new InputFile().setMedia(document));
        sendDocument.setCaption(text);
        sendDocument.setReplyMarkup(keyboard);
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            User user = update.getCallbackQuery().getFrom();
            Long id = user.getId();
            handleCallbackQuery(id, update.getCallbackQuery());
            if (usersQuizzesMap.get(id).currentQuestionIndex == questionsList.size() && usersQuizzesMap.get(id).isQuizzing) {
                usersQuizzesMap.get(id).isQuizzing = false;
                sendResultsMessage(id);
                sendRewardMessage(id);
                sendRestartQuizMessage(id);
            }
        } else {
            Message msg = update.getMessage();
            User user = msg.getFrom();
            Long id = user.getId();

            if (update.getMessage().getText().equals("/start")) {
                sendHelloMessage(id);
            }
            else if (update.getMessage().getText().equals(currentMainCompliment.getKeyboardButtonText())) {
                sendMainComplimentMessage(id, currentMainCompliment);
            }
        }
    }

    private void handleCallbackQuery(Long userId, CallbackQuery callbackQuery) {
        switch (callbackQuery.getData()) {
            case "startQuiz":
                if (!usersQuizzesMap.containsKey(userId)) {
                    Quiz quiz = new Quiz(userId, questionsList);
                    quiz.start();
                    usersQuizzesMap.put(userId, quiz);
                } else {
                    Quiz quiz = usersQuizzesMap.get(userId);
                    quiz.start();
                }
                if (usersQuizzesMap.get(userId).isQuizzing) {
                    createQuizQuestion(userId);
                }
                break;
            case "true":
                if (usersQuizzesMap.get(userId).isQuizzing) {
                    usersQuizzesMap.get(userId).resultsMap.put(usersQuizzesMap.get(userId).currentQuestionIndex, true);
                    usersQuizzesMap.get(userId).rightAnswersCount++;
                    usersQuizzesMap.get(userId).currentQuestionIndex++;
                    sendText(userId, questionRepliesList.get(
                            Utilities.getRndIntInRange(0, questionRepliesList.size() - 1)));
                    if (usersQuizzesMap.get(userId).currentQuestionIndex < usersQuizzesMap.get(userId).questionsList.size()) {
                        createQuizQuestion(userId);
                    }
                }
                break;
            case "false":
                if (usersQuizzesMap.get(userId).isQuizzing) {
                    usersQuizzesMap.get(userId).resultsMap.put(usersQuizzesMap.get(userId).currentQuestionIndex, false);
                    usersQuizzesMap.get(userId).currentQuestionIndex++;
                    sendText(userId, questionRepliesList.get(
                            Utilities.getRndIntInRange(0, questionRepliesList.size() - 1)));
                    if (usersQuizzesMap.get(userId).currentQuestionIndex < questionsList.size()) {
                        createQuizQuestion(userId);
                    }
                }
                break;
        }
    }

    private void createQuizQuestion(Long userId) {
        Question currentQuestion = questionsList.get(usersQuizzesMap.get(userId).currentQuestionIndex);
        String questionText = currentQuestion.text;
        List<InlineKeyboardButton> keyboardButtons = new ArrayList<>();
        for (Answer answer : currentQuestion.answers) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(answer.text);
            if (answer.isRight) {
                button.setCallbackData("true");
            } else {
                button.setCallbackData("false");
            }
            keyboardButtons.add(button);
        }
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsList = new ArrayList<>();
        for (InlineKeyboardButton button : keyboardButtons) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rowsList.add(row);
        }
        Collections.shuffle(rowsList);
        keyboardMarkup.setKeyboard(rowsList);

        if (currentQuestion.imagesURLsList.isEmpty()) {
            sendTextWithKeyboard(userId,
                    "Вопрос " + (usersQuizzesMap.get(userId).currentQuestionIndex + 1) + ": " + questionText,
                    keyboardMarkup);
        } else {
            String url =
                    currentQuestion.imagesURLsList.
                            get(Utilities.getRndIntInRange(0, currentQuestion.imagesURLsList.size() - 1));
            if (    url.contains(".gif") ||
                    url.contains(".mp4") ||
                    url.contains(".mpeg") ||
                    url.contains(".avi") ||
                    url.contains(".mov") ||
                    url.contains(".webm")
            ) {
                sendTextWithDocumentAndKeyboard(userId,
                        "Вопрос " + (usersQuizzesMap.get(userId).currentQuestionIndex + 1) + ": " + questionText,
                        url,
                        keyboardMarkup);
            }
            else {
                sendTextWithImageAndKeyboard(
                        userId,
                        "Вопрос " + (usersQuizzesMap.get(userId).currentQuestionIndex + 1) + ": " + questionText,
                        url,
                        keyboardMarkup);
            }
        }
    }

    private void sendHelloMessage(Long userId) {
        Phrase phrase = helloPhrasesList.get(Utilities.getRndIntInRange(0, helloPhrasesList.size() - 1));
        InlineKeyboardButton startQuizButton = InlineKeyboardButton.builder()
                .text(phrase.getInlineButtonText()).callbackData("startQuiz")
                .build();
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder().
                keyboardRow(Collections.singletonList(startQuizButton)).build();
        if (helloImagesURLsList.isEmpty()) {
            sendTextWithKeyboard(userId, phrase.getPhrase(), keyboard);
        } else {
            String url = helloImagesURLsList.get(Utilities.getRndIntInRange(0, helloImagesURLsList.size() - 1));
            if (    url.contains(".gif") ||
                    url.contains(".mp4") ||
                    url.contains(".mpeg") ||
                    url.contains(".avi") ||
                    url.contains(".mov") ||
                    url.contains(".webm")
            ) {
                sendTextWithDocumentAndKeyboard(
                        userId,
                        phrase.getPhrase(),
                        helloImagesURLsList.get(Utilities.getRndIntInRange(0, helloImagesURLsList.size() - 1)),
                        keyboard);
            }
            else {
                sendTextWithImageAndKeyboard(
                        userId,
                        phrase.getPhrase(),
                        helloImagesURLsList.get(Utilities.getRndIntInRange(0, helloImagesURLsList.size() - 1)),
                        keyboard);
            }
        }
    }

    private void sendRestartQuizMessage(Long userId) {
        Phrase restartQuizPhrase = restartQuizPhrasesList.get(Utilities.getRndIntInRange(0, restartQuizPhrasesList.size() - 1));
        InlineKeyboardButton restartQuizButton = InlineKeyboardButton.builder()
                .text(restartQuizPhrase.getInlineButtonText()).callbackData("startQuiz")
                .build();
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder().
                keyboardRow(Collections.singletonList(restartQuizButton)).build();

        if (restartQuizImagesURLsList.isEmpty()) {
            sendTextWithKeyboard(userId, restartQuizPhrase.getPhrase(), keyboard);
        } else {
            String url = restartQuizImagesURLsList.
                    get(Utilities.getRndIntInRange(0, restartQuizImagesURLsList.size() - 1));
            if (    url.contains(".gif") ||
                    url.contains(".mp4") ||
                    url.contains(".mpeg") ||
                    url.contains(".avi") ||
                    url.contains(".mov") ||
                    url.contains(".webm")
            ) {
                sendTextWithDocumentAndKeyboard(
                        userId,
                        restartQuizPhrase.getPhrase(),
                        restartQuizImagesURLsList.get(Utilities.getRndIntInRange(0, restartQuizImagesURLsList.size() - 1)),
                        keyboard);
            }
            else {
                sendTextWithImageAndKeyboard(
                        userId,
                        restartQuizPhrase.getPhrase(),
                        restartQuizImagesURLsList.get(Utilities.getRndIntInRange(0, restartQuizImagesURLsList.size() - 1)),
                        keyboard);
            }
        }

    }

    private void sendRewardMessage(Long userId) {
        currentMainCompliment = mainComplimentList.get(Utilities.getRndIntInRange(0, mainComplimentList.size() - 1));
        Reward reward = new Reward();
        if (usersQuizzesMap.get(userId).rightAnswersCount < requiredRightAnswersLow) {
            reward.setText(noReward.getText());
            reward.setImageUrl(noReward.getImageUrl());
        }
        else if (
                        usersQuizzesMap.get(userId).rightAnswersCount >= requiredRightAnswersLow &&
                        usersQuizzesMap.get(userId).rightAnswersCount < requiredRightAnswersMiddle
        ) {
            reward.setText(lowReward.getText());
            reward.setImageUrl(lowReward.getImageUrl());
        }
        else if (
                        usersQuizzesMap.get(userId).rightAnswersCount >= requiredRightAnswersMiddle &&
                        usersQuizzesMap.get(userId).rightAnswersCount < requiredRightAnswersHigh
        ) {
            reward.setText(middleReward.getText());
            reward.setImageUrl(middleReward.getImageUrl());
        }
        else if (usersQuizzesMap.get(userId).rightAnswersCount >= requiredRightAnswersHigh) {
            reward.setText(highReward.getText());
            reward.setImageUrl(highReward.getImageUrl());
        }

        if (reward.getImageUrl().equals("")) {
            sendTextWithKeyboard(userId, reward.getText(), getMainComplimentReplyKeyboard());
        }
        else {
            if (    reward.getImageUrl().contains(".gif") ||
                    reward.getImageUrl().contains(".mp4") ||
                    reward.getImageUrl().contains(".mpeg") ||
                    reward.getImageUrl().contains(".avi") ||
                    reward.getImageUrl().contains(".mov") ||
                    reward.getImageUrl().contains(".webm")
            ) {
                sendTextWithDocumentAndKeyboard(userId, reward.getText(), reward.getImageUrl(), getMainComplimentReplyKeyboard());
            }
            else {
                sendTextWithImageAndKeyboard(userId, reward.getText(), reward.getImageUrl(), getMainComplimentReplyKeyboard());
            }
        }
    }

    private void sendResultsMessage(Long userId) {
        StringBuilder sb = new StringBuilder();
        Quiz quiz = usersQuizzesMap.get(userId);
        sb.append("Результаты: \n");
        for (int i = 0; i < quiz.resultsMap.size(); i++) {
            sb.append("\tВопрос ").append(i + 1).append(": ");
            if (quiz.resultsMap.get(i)) {
                sb.append("✅");
            } else {
                sb.append("❌");
            }
            sb.append("\n");
        }
        sb.append("\n\n");
        sb.append("Правильных ответов: ").append(usersQuizzesMap.get(userId).rightAnswersCount);
        sendText(userId, sb.toString());
    }

    private void sendMainComplimentMessage(Long id, MainCompliment compliment) {
        if (compliment.getImageURL().equals("")) {
            sendText(id, compliment.getText());
        }
        else {
            if (    compliment.getImageURL().contains(".gif") ||
                    compliment.getImageURL().contains(".mp4") ||
                    compliment.getImageURL().contains(".mpeg") ||
                    compliment.getImageURL().contains(".avi") ||
                    compliment.getImageURL().contains(".mov") ||
                    compliment.getImageURL().contains(".webm")
            ) {
                sendTextWithDocument(id, compliment.getText(), compliment.getImageURL());
            }
            else {
                sendTextWithImage(id, compliment.getText(), compliment.getImageURL());
            }
        }
    }

    private void printParsedConfig() {
        StringBuilder sb = new StringBuilder();
        sb.append("CONFIG (").append(Utilities.CONFIG_FILE_PATH).append("):\n");
        sb.append("\trequiredRightAnswersLow: ").append(requiredRightAnswersLow).append("\n");
        sb.append("\trequiredRightAnswersMiddle: ").append(requiredRightAnswersMiddle).append("\n");;
        sb.append("\trequiredRightAnswersHigh: ").append(requiredRightAnswersHigh).append("\n");;
        sb.append("\n");
        System.out.println(sb);
    }

    private void printParsedQuestionReplies() {
        StringBuilder sb = new StringBuilder();
        sb.append("QUESTION REPLIES (").append(Utilities.QUESTION_REPLIES_FILE_PATH).append("):\n");
        for (String reply : questionRepliesList) {
            sb.append("\t[").append(reply).append("]\n");
        }
        sb.append("\n");
        System.out.println(sb);
    }

    private void printParsedQuestions() {
        System.out.println("----------------- QUESTIONS ----------------- (" + Utilities.QUESTIONS_FILE_PATH + ")\n");
        for (Question question : questionsList) {
            StringBuilder sb = new StringBuilder();
            for (String imageURL : question.imagesURLsList) {
                sb.append("[").append(imageURL).append("]\n");
            }
            sb.append(question.text).append(":\n");
            for (Answer answer : question.answers) {
                sb.append("\t").append(answer.text);
                if (answer.isRight) {
                    sb.append(" (X)");
                }
                sb.append("\n");
            }
            System.out.println(sb);
        }
    }

    private void printParsedHelloImages() {
        StringBuilder sb = new StringBuilder();
        sb.append("HELLO images (").append(Utilities.HELLO_IMAGES_URLS_FILE_PATH).append("):\n");
        for (String helloImageURL : helloImagesURLsList) {
            sb.append("\t[").append(helloImageURL).append("]\n");
        }
        sb.append("\n");
        System.out.println(sb);
    }

    private void printParsedHelloPhrases() {
        StringBuilder sb = new StringBuilder();
        sb.append("HELLO phrases (").append(Utilities.HELLO_PHRASES_FILE_PATH).append("):\n");
        for (Phrase phrase : helloPhrasesList) {
            sb.append("\t[").append(phrase.getPhrase()).append(" ; ").append(phrase.getInlineButtonText()).append("]\n");
        }
        sb.append("\n");
        System.out.println(sb);
    }

    private void printParsedRestartQuizImages() {
        StringBuilder sb = new StringBuilder();
        sb.append("RESTART QUIZ images (").append(Utilities.RESTART_QUIZ_URLS_IMAGES_FILE_PATH).append("):\n");
        for (String helloImageURL : restartQuizImagesURLsList) {
            sb.append("\t[").append(helloImageURL).append("]\n");
        }
        sb.append("\n");
        System.out.println(sb);
    }

    private void printParsedRestartQuizPhrases() {
        StringBuilder sb = new StringBuilder();
        sb.append("RESTART QUIZ phrases (").append(Utilities.RESTART_QUIZ_PHRASES_FILE_PATH).append("):\n");
        for (Phrase phrase : restartQuizPhrasesList) {
            sb.append("\t[").append(phrase.getPhrase()).append(" ; ").append(phrase.getInlineButtonText()).append("]\n");
        }
        sb.append("\n");
        System.out.println(sb);
    }

    private void printParsedRewards() {
        StringBuilder sb = new StringBuilder();
        sb.append("REWARDS (").append(Utilities.REWARDS_FILE_PATH).append("):\n");
        sb.append("\tnoReward: [").append(noReward.getText()).append(" ; ").append(noReward.getImageUrl()).append("]\n");
        sb.append("\tlowReward: [").append(lowReward.getText()).append(" ; ").append(lowReward.getImageUrl()).append("]\n");
        sb.append("\tmiddleReward: [").append(middleReward.getText()).append(" ; ").append(middleReward.getImageUrl()).append("]\n");
        sb.append("\thighReward: [").append(highReward.getText()).append(" ; ").append(highReward.getImageUrl()).append("]\n");
        sb.append("\n");
        System.out.println(sb);
    }

    private void printParsedMainCompliments() {
        StringBuilder sb = new StringBuilder();
        sb.append("MAIN COMPLIMENTS (").append(Utilities.MAIN_COMPLIMENTS_FILE_PATH).append("):\n");
        for (MainCompliment mainCompliment : mainComplimentList) {
            sb.append("\t[").
                    append(mainCompliment.getText()).append(" ; ")
                    .append(mainCompliment.getKeyboardButtonText()).append(" ; ")
                    .append(mainCompliment.getImageURL())
                    .append("]\n");
        }
        sb.append("\n");
        System.out.println(sb);
    }

}
