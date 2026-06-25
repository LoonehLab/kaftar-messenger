package entity;

import dao.*;

import java.util.*;

public class User
{
    private String userName;
    private String id;
    private String fullName;
    private String password;
    private String avatar;
    private String bio;
    private String phoneNumber;
    //private Security security;
    
    private List<PV> pv;
    private List<Group> groups;
    private List<User> contacts;

    private List<Message> messageOfSavedMessage;

    private List<Message> sentMessages;
    private List<Message> receivedMessages;

    private InformationRepository informationRepository;

    public User(String userName, String fullName, String password) {
        int randomSuffix = new java.util.Random().nextInt(9) + 1; // Random digits
        this.id = String.valueOf(System.currentTimeMillis()) + randomSuffix;
        this.userName = userName;
        this.fullName = fullName;
        this.password = password;
        initFields(false);
    }

    public User(String id, String userName, String fullName, String password, boolean loadMode) {
        this.id = id;
        this.userName = userName;
        this.fullName = fullName;
        this.password = password;
        initFields(loadMode);
    }

    private void initFields(boolean loadMode) {
        avatar = "";
        bio = "";
        phoneNumber = "";
        pv = new ArrayList<>();
        groups = new ArrayList<>();
        sentMessages = new ArrayList<>();
        receivedMessages = new ArrayList<>();
        contacts = new ArrayList<>();
        messageOfSavedMessage = new ArrayList<>();
        if(!loadMode) informationRepository = new InformationRepository(this);
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

    public void removePv(PV p)
    {
        pv.remove(p);
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
        // howManyGroupsCreate++;
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
    public String getFullName() { return fullName; }
    public String getPassword(){ return password;}
    public String getAvatar() { return avatar; }
    public String getBio(){ return bio;}
    public String getPhoneNumber(){ return phoneNumber;}
    public List<PV> getPv(){ return pv; }
    public List<Group> getGroups(){ return groups; }
    public List<User> getContacts(){ return contacts; }
    public InformationRepository getInformationRepository()
    {
        if(informationRepository == null)
            informationRepository = new InformationRepository(this);
        return informationRepository;
    }

    public void setUserName(String userName){ this.userName = userName; }
    public void setId(String id){ this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPassword(String password){ this.password = password; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setBio(String bio){ this.bio = bio; }
    public void setPhoneNumber(String phoneNumber){ this.phoneNumber = phoneNumber; }
}