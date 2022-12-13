public class MainCompliment {

    private String text;
    private String keyboardButtonText;
    private String imageURL;

    public MainCompliment() {
    }

    public MainCompliment(String text, String keyboardButtonText, String imageURL) {
        this.text = text;
        this.keyboardButtonText = keyboardButtonText;
        this.imageURL = imageURL;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getKeyboardButtonText() {
        return keyboardButtonText;
    }

    public void setKeyboardButtonText(String keyboardButtonText) {
        this.keyboardButtonText = keyboardButtonText;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
