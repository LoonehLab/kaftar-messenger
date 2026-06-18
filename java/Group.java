import java.util.*;

public class Group extends Chat
{
    private String id;
    private List<User> members;
    private User owner;
    private List<Message> messages;
    private int numOfMessages;
    private String name;

    private GroupRepository groupRepository;

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

        groupRepository = new GroupRepository(this);
        this.messages = groupRepository.getArrayOfMessages();
    }

    public void send(Message text)
    {
        text.setChat((Group)this);
        messages.add(text);
        numOfMessages++;
        text.setId(messages.size());

        for(User u : members)
        {
            if(u.getId() == text.getSenderId())
            {
                u.addSentMessage(text);

                for(User v : members)
                {
                    if(v != u)
                    {
                        v.addReceivedMessage(text);
                    }
                }

                return;
            }
        }
        groupRepository.addMessage(text);
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
        user.removeGroup(this);
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


    public int getNumOfMessages(){ return numOfMessages; }
    public String getId(){ return id; }
    public String getName(){ return name; }
    public List<User> getMembers() { return members; }
    public List<Message> getMessages() { return messages; }
    public User getOwner(){ return owner; }

    public void setName(String name) { this.name = name; }
}
