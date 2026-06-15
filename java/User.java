import java.util.*;

public class User
{
    private String userName;
    private String id;
    private String password;
    private String bio;
    private String phoneNumber;
    //private Security security;
    
    private List<PV> pv;
    private List<Group> groups;
    private List<User> contacts;

    private SavedMessage savedMessage;
    private List<Message> messageOfSavedMessage;

    private List<Message> sentMessages;
    private List<Message> receivedMessages;

    private int howManyGroupsCreate;

    private InformationRepository informationRepository;


    User(String id , String userName , String password)
    {
        this.id = id;
        this.userName = userName;
        this.password = PasswordHasher.hashPassword(password, PasswordHasher.getSalt());
        bio = "";
        phoneNumber = "";

        pv = new ArrayList<>();
        groups = new ArrayList<>();
        sentMessages = new ArrayList<>();
        receivedMessages = new ArrayList<>();
        contacts = new ArrayList<>();

        savedMessage = new SavedMessage(this, this);
        messageOfSavedMessage = new ArrayList<>();

        howManyGroupsCreate = 0;

        informationRepository = new InformationRepository(this);
    }

    public void addGroup(Group g)
    {
        groups.add(g);
    }

    public void removeGroup(Group g)
    {
        groups.remove(g);
    }

    public void addPv(PV p)
    {
        pv.add(p);
    }

    public boolean addContact (User contact)
    {
        for(User c : contacts)
        {
            if((contact.getId()).equals(c.getId()))
            {
                return false;
            }
        }

        if(contact.getId().equals(id))
        {
            return false;
        }

        else
        {
            contacts.add(contact);
            return true;
        }
    }

    public boolean removeContact(User contact)
    {
        if(contact.getId().equals(id))
        {
            return false;
        } 

        for(User c : contacts)
        {
            if((contact.getId()).equals(c.getId()))
            {
                contacts.remove(contact);
            }
        }

        return false;
    }

    public void groupCreated()
    {
        howManyGroupsCreate++;
    }

    public void addSentMessage(Message mess)
    {
        sentMessages.add(mess);
    }

    public void addReceivedMessage(Message mess)
    {
        receivedMessages.add(mess);
    }

    public void addMessageInSavedMessage(Message text)
    {
        messageOfSavedMessage.add(text);
    }

    public String getUserName(){ return userName;}
    public String getId(){ return id;}
    public String getPassword(){ return password;}
    public String getBio(){ return bio;}
    public String getPhoneNumber(){ return phoneNumber;}
    public List<PV> getPv(){ return pv; }
    public List<Group> getGroups(){ return groups; }
    public List<User> getContacts(){ return contacts; }
    public int getHowManyGroupCreated() { return howManyGroupsCreate; }

    public void setUserName(String userName){ this.userName = userName; }
    public void setId(String id){ this.id = id; }
    public void setPassword(String password){ this.password = password; }
    public void setBio(String bio){ this.bio = bio; }
    public void setPhoneNumber(String phoneNumber){ this.phoneNumber = phoneNumber; }
}