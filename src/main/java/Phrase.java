public class Phrase {

    private String phrase;
    private String inlineButtonText;

    public Phrase() {
    }

    public Phrase(String phrase, String inlineButtonText) {
        this.phrase = phrase;
        this.inlineButtonText = inlineButtonText;
    }

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    public String getInlineButtonText() {
        return inlineButtonText;
    }

    public void setInlineButtonText(String inlineButtonText) {
        this.inlineButtonText = inlineButtonText;
    }
}
