import java.util.*;

public class PV extends Chat 
{
    protected User firstPerson;
    protected User secondPerson;
    protected int numOfMessages;

    private boolean isDeleted;

    protected List<Message> messages;

    PV(User first , User second)
    {
        firstPerson = first;
        secondPerson = second;
        numOfMessages = 0;
        isDeleted = false;

        messages = new ArrayList<>();

    }


    public void send(Message text)
    {
        if(numOfMessages == 0)
        {
            if(!firstPerson.getPv().contains(this))
            {
                firstPerson.addPv(this);
            }

            if(!secondPerson.getPv().contains(this))
            {
                secondPerson.addPv(this);
            }
        }

        if(isDeleted)
        {
            isDeleted = false;
        }
        
        text.setChat((PV)this);
        messages.add(text);
        numOfMessages++;
        text.setId(messages.size());

        if(text.getSenderId() == firstPerson.getId())
        {
            firstPerson.addSentMessage(text);
            secondPerson.addReceivedMessage(text);
        }

        else if(text.getSenderId() == secondPerson.getId())
        {
            firstPerson.addReceivedMessage(text);
            secondPerson.addSentMessage(text);
        }
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
