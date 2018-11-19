
import java.io.Serializable;

final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;

    // Here is where you should implement the chat message object.
    // Variables, Constructors, Methods, etc.

    private String message;
    private int type;
    private String recipient;
    private boolean isListMessage;


    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

    public ChatMessage(String message, int type) {
        this.message = message;
        this.type = type;
        if(message.startsWith("/msg")) {
            recipient = message.substring(5, message.indexOf(" ", 5));
        }
        if (message.startsWith("/list")) {
          isListMessage = true;
        } else {
            isListMessage = false;
        }
    }

    public ChatMessage(String message, int type, String recipient) {
        this(message,type);
        if(message.startsWith("/msg")) {
            recipient = message.substring(5, message.indexOf(" ", 5));
        }
        this.recipient = recipient;
    }

    public String getRecipient() {
        return recipient;
    }

}
