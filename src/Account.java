import java.util.ArrayList;
import java.util.List;


public class Account {
    private String username;
    private String authToken;
    private List<Message> messageBox;
    public Account(String username, String authToken, List<Message> messageBox) {
        this.username = username;
        this.authToken = authToken;
        this.messageBox = messageBox;
    }
    public Account(String username,String authToken)
    {

        this.authToken=authToken;
        this.username = username;
        for (int i=0;i<username.length();i++)
        {
            char ch=username.charAt(i);
            if (!(Character.isLetter(ch) || ch=='_'))
            {
                    this.username=null;
                    break;
            }
        }
        this.messageBox = new ArrayList<>();
    }
    public String getUsername()
    {
        return username;
    }
    public String getAuthToken()
    {
        return authToken;
    }
    public List<Message> getMessageBox()
    {
        return messageBox;
    }
    public void addMessage(Message message)
    {
        this.messageBox.add(message);
    }

}
