import java.io.*;
import java.util.*;

public class GroupRepository
{
    private String directoryPath;
    private String memberPath;
    private String messagePath;
    private String dataPath;
    private Group group;

    GroupRepository(Group group)
    {
        this.group = group;
        directoryPath = ".." + File.separator + "data" + File.separator + "groups" + File.separator + group.getId();
        
        File folder = new File(directoryPath);
        if(!folder.exists())
        {
           folder.mkdirs();
        }

        File dataFile = new File(folder, group.getId() + "data.txt");
        File memberFile = new File(folder, group.getId() + "member.txt");
        File messageFile = new File(folder, group.getId() + "message.txt");

        this.dataPath = dataFile.getAbsolutePath();
        this.memberPath = memberFile.getAbsolutePath();
        this.messagePath = messageFile.getAbsolutePath();

        try
        {
            if(!dataFile.exists())
            {
               dataFile.createNewFile();

               try(FileWriter writer = new FileWriter(dataFile, true))
                {
                    writer.write(group.getId()+"\n"+group.getName()+"\n"+group.getOwner().getId()+"\n");
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }

            if(!memberFile.exists())
            {
               memberFile.createNewFile();
            }

            if(!messageFile.exists())
            {
                messageFile.createNewFile();
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void changeData(String id , String name , String ownerId)
    {
        try(FileWriter writer = new FileWriter(dataPath,false))
        {
            writer.write(id+"\n"+name+"\n"+ownerId+"\n");
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void addMessage(Message message)
    {
        try(FileWriter writer = new FileWriter(messagePath , true))
        {
            writer.write(message.getText()+" , "+message.getId()+" , "+message.getSenderId()+" , "+message.getReceiverId()+" , "+message.getTime()+" , "+message.getEdited()+" , "+message.getSeen()+"\n");
        }

        catch(IOException e) 
        {
           e.printStackTrace();
        } 
    }

    public List<Message> getArrayOfMessages()
    {
        List<Message> array = new ArrayList<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(messagePath)))
        {
            String line;

           while ((line = reader.readLine()) != null) 
           {
               String[] messageData = line.split(" , ");

               int newId = Integer.parseInt(messageData[1]);
 
               Message newMessage = new Message(newId, messageData[2], messageData[3], messageData[0] , Long.parseLong(messageData[4]) ,  Boolean.parseBoolean(messageData[5]) , false, Boolean.parseBoolean(messageData[6]));

               array.add(newMessage);
           }
        }

        catch(IOException e) 
        {
           e.printStackTrace();
        } 

        return array;
    }

    public void removeMessage(Message mess)
    {
        List<Message> keeper = this.getArrayOfMessages();

        try(FileWriter writer = new FileWriter(messagePath , false))
        {
            writer.write("");
        }

        catch(IOException e) 
        {
           e.printStackTrace();
        } 

        for(Message m : keeper)
        {
            if(mess.getId() == m.getId())
            {
                continue;
            }

            else
            {
                addMessage(m);
            }
        }
    }

    public void addMember(User member)
    {
        try(FileWriter writer = new FileWriter(memberPath , true))
        {
            writer.write(member.getId()+"\n");
        }

        catch(IOException e) 
        {
           e.printStackTrace();
        } 
    }

    public List<String> getArrayOfMembersId()
    {
        List<String> ids = new ArrayList<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(memberPath)))
        {
            String line;

           while ((line = reader.readLine()) != null) 
           {
               ids.add(line);
           }
        }

        catch(IOException e) 
        {
           e.printStackTrace();
        }

        return ids;
    }

    public void removeMember(User member)
    {
        List<String> ids = getArrayOfMembersId();

        for(String i : ids)
        {
            if(i == member.getId())
            {
                continue;
            }

            else
            {
                try(FileWriter writer = new FileWriter(memberPath , false))
                {
                    writer.write("");
                }

                catch(IOException e) 
                {
                   e.printStackTrace();
                } 

                UserRepository userRepository = UserRepository.getInstance();

                User newUser = userRepository.findUserById(i);

                addMember(newUser);
            }
        }
    }

    //delete group

}