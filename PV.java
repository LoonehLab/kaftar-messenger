import java.util.*;

public class PV extends Chat 
{
    private User firstPerson;
    private User secondPerson;
    private int numOfMessages;

    boolean isDeleted;

    private List<Message> messages;

    PV(User first , User second)
    {
        firstPerson = first;
        secondPerson = second;
        numOfMessages = 0;
        isDeleted = false;

        messages = new ArrayList<>();

        firstPerson.addPv(this);
        secondPerson.addPv(this);
    }


    public void send(Message text)
    {
        messages.add(text);
        numOfMessages++;
        text.setId(numOfMessages);
    }

    public void edit(String newText , Message mes)
    {
        mes.setText(newText);
    }

    public void deleteMessage(Message mes)
    {
        mes.setDeleted(true);
        numOfMessages--;
    }

    public void deleteChat()
    {
        for(Message m : messages)
        {
            deleteMessage(m);
        }

        numOfMessages = 0;
    }

    public void deletePv()
    {
        deleteChat();
        isDeleted = true;
    }


    public User getFirstPerson(){ return firstPerson; }
    public User getSecondPerson(){ return secondPerson; }
    public int getNumOfMessages(){ return numOfMessages; }
    public List<Message> messages() { return messages; }
    public boolean getIsDeleted() { return isDeleted; }
}
