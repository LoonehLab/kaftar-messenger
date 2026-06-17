import java.util.*;

public class PV extends Chat 
{
    protected User firstPerson;
    protected User secondPerson;
    protected int numOfMessages;

    private boolean isDeleted;

    protected List<Message> messages;

    private PvRepository pvRepository;

    PV(User first , User second)
    {
        firstPerson = first;
        secondPerson = second;
        numOfMessages = 0;
        isDeleted = false;

        messages = new ArrayList<>();

        pvRepository = new PvRepository(this);

    }


    public void send(Message text)
    {
        if(numOfMessages == 0)
        {
            firstPerson.addPv(this);
            secondPerson.addPv(this);
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

    public void deletePv(String forWho)
    {
        deleteChat();
        isDeleted = true;

        if(forWho.equals("both"))
        {
            firstPerson.removePv(this);
            secondPerson.removePv(this);
        }

        else if(forWho.equals("first"))
        {
            firstPerson.removePv(this);
        }

        else if(forWho.equals("second"))
        {
            secondPerson.removePv(this);
        }
    }


    public User getFirstPerson(){ return firstPerson; }
    public User getSecondPerson(){ return secondPerson; }
    public int getNumOfMessages(){ return numOfMessages; }
    public List<Message> messages() { return messages; }
    public boolean getIsDeleted() { return isDeleted; }
    public PvRepository getPvRepository(){ return pvRepository; }
}
