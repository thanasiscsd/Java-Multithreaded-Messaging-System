
public class Message {
    private static int nextId=1; //static counter for unique ID
    private int id;
    private boolean isRead;
    private String sender;
    private String receiver;
    private String body;
    public Message(boolean isRead, String sender, String receiver, String body) {
        this.id=nextId++;
        this.isRead = isRead;
        this.sender = sender;
        this.receiver = receiver;
        this.body = body;
    }
    public boolean getIsRead() {
          return isRead;
    }

    public String getSender() {
         return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getBody() {
        return body;
    }

    public int getId() {
        return id;
    }

    public void markAsRead() {
        this.isRead=true;
    }

}
