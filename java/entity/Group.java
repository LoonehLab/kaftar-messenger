package entity;

import dao.*;

import java.util.*;

public class Group extends Chat
{
    private String id;
    private List<User> members;
    private User owner;
    private String avatar = "";
    private List<Message> messages;
    private int numOfMessages;
    private String name;

    private dao.GroupRepository groupRepository;

    public Group(User owner , String name , User ... mem)
    {
        this(String.valueOf(System.currentTimeMillis()), owner, name, true, mem);
    }

    public Group(String id , User owner , String name , User ... mem)
    {
        this(id, owner, name, false, mem);
    }

    private Group(String id , User owner , String name , boolean isNew , User ... mem)
    {
        this.id = id;
        this.name = name;
        this.numOfMessages = 0;

        members = new ArrayList<>();
        for(User m : mem)
        {
            members.add(m);

            if(m == owner)
            {
                this.owner = m;
                if(isNew)
                {
                    m.groupCreated();
                }
            }

            m.addGroup(this);
        }

        messages = new ArrayList<>();

        groupRepository = new GroupRepository(this);
        this.avatar = groupRepository.getAvatar(); 
        this.messages = groupRepository.getArrayOfMessages();
        this.numOfMessages = this.messages.size();

        for(String memberId : groupRepository.getArrayOfMembersId())
        {
            boolean alreadyMember = false;
            for(User m : members)
            {
                if(m.getId().equals(memberId))
                {
                    alreadyMember = true;
                    break;
                }
            }
            if(alreadyMember) continue;

            User member = UserRepository.getInstance().findUserById(memberId);
            if(member != null)
            {
                members.add(member);
                member.addGroup(this);
            }
        }
    }

    public void send(Message text)
    {
        messages.add(text);
        numOfMessages++;
        text.setId(messages.size());

        for(User u : members)
        {
            if(u.getId().equals(text.getSenderId()))
            {
                u.addSentMessage(text);

                for(User v : members)
                {
                    if(!v.getId().equals(u.getId()))
                    {
                        v.addReceivedMessage(text);
                    }
                }
                break;
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
        user.getInformationRepository().removeGroup(this); 
        groupRepository.removeMember(user);
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
        groupRepository.addMember(user);
    }


    public int getNumOfMessages(){ return numOfMessages; }
    public String getId(){ return id; }
    public String getName(){ return name; }
    public String getAvatar(){ return this.avatar; }
    public List<User> getMembers() { return members; }
    public List<Message> getMessages() { return messages; }
    public User getOwner(){ return owner; }
    public GroupRepository getGroupRepository(){ return groupRepository; }

    public void setName(String name) { this.name = name; }
    public void setAvatar(String avatar){ this.avatar = avatar; }
}
