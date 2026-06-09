import java.util.*;

public class Group extends Chat
{
    private String id;
    private List<User> members;
    private User owner;
    private List<Message> messages;
    private int numOfMessages;
    private String name;

    Group(User owner , String name , User ... mem)
    {
        this.name = name;
        this.numOfMessages = 0;

        members = new ArrayList<>();
        for(User m : mem)
        {
            (this.members).add(m);

            if(m == owner)
            {
                this.owner = m;
                m.groupCreated();
            }

            m.addGroup(this);
        }

        id = (owner.getId())+Integer.toString(owner.getHowManyGroupCreated());
        messages = new ArrayList<>();
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

    public void leftOrRemove(User user)
    {
        members.remove(user);
        user.addGroup(this);
    }

    public void deleteGroupForAll()
    {
        deleteChat();

        for(User u : members)
        {
            leftOrRemove(u);
        }
    }

    public void addMember(User user)
    {
        members.add(user);
    }

    public void setName(String name) { this.name= name; }
}
