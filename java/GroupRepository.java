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
                    writer.write(group.getId() + "\n" + group.getName() + "\n" + group.getOwner().getId() + "\n");
               }
               catch(IOException e)
               {
                    e.printStackTrace();
               }
            }

            if(!memberFile.exists()) memberFile.createNewFile();
            if(!messageFile.exists()) messageFile.createNewFile();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void changeData(String id , String name , String ownerId)
    {
        try(FileWriter writer = new FileWriter(dataPath, false))
        {
            writer.write(id + "\n" + name + "\n" + ownerId + "\n");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void addMessage(Message message)
    {
        try(FileWriter writer = new FileWriter(messagePath, true))
        {
            String cleanText = message.getText().replace("║", " ").replace("\n", " ");
            message.setText(cleanText);
            writer.write("║" + message.getId() + " , " + message.getSenderId() + " , " + 
                         message.getReceiverId() + " , " + message.getTime() + " , " + 
                         message.getEdited() + " , " + message.getSeen() + " , " + cleanText + "\n");
        }
        catch(IOException e) 
        {
           e.printStackTrace();
        } 
    }

    public List<Message> getArrayOfMessages()
    {
        List<Message> array = new ArrayList<>();
        File file = new File(messagePath);
        
        if(!file.exists()) return array;

        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            String line;
            while ((line = reader.readLine()) != null) 
            {
               if (line.trim().isEmpty() || !line.startsWith("║")) continue;

               String rawMessage = line.substring(1);
               String[] messageData = rawMessage.split(" , ", 7);
               
               if(messageData.length < 7) continue;

               int newId = Integer.parseInt(messageData[0].trim());
               String sender = messageData[1].trim();
               String receiver = messageData[2].trim();
               long time = Long.parseLong(messageData[3].trim());
               boolean edited = Boolean.parseBoolean(messageData[4].trim());
               boolean seen = Boolean.parseBoolean(messageData[5].trim());
               String text = messageData[6];

               Message newMessage = new Message(newId, sender, receiver, text, time, edited, false, seen);
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
        try(FileWriter writer = new FileWriter(messagePath, false))
        {
            writer.write(group.getId() + "\n" + group.getName() + "\n" + group.getOwner().getId() + "\n");
            
            for(Message m : keeper)
            {
                if(mess.getId() == m.getId()) continue;
                else
                {
                    String messageStr = "║" + m.getId() + " , " + m.getSenderId() + " , " + 
                                        m.getReceiverId() + " , " + m.getTime() + " , " + 
                                        m.getEdited() + " , " + m.getSeen() + " , " + m.getText() + "\n";
                    writer.write(messageStr);
                }
            }
        }
        catch(IOException e) 
        {
           e.printStackTrace();
        } 
    }

    public void addMember(User member)
    {
        try(FileWriter writer = new FileWriter(memberPath, true))
        {
            writer.write(member.getId() + "\n");
        }
        catch(IOException e) 
        {
           e.printStackTrace();
        } 
    }

    public List<String> getArrayOfMembersId()
    {
        List<String> ids = new ArrayList<>();
        File file = new File(memberPath);
        
        if(!file.exists()) {
            return ids;
        }

        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
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

        try(FileWriter writer = new FileWriter(memberPath, false))
        {
            writer.write("");
        }
        catch(IOException e) 
        {
           e.printStackTrace();
        } 

        for(String i : ids)
        {
            if(i.equals(member.getId()))
            {
                continue;
            }
            else
            {
                UserRepository userRepository = UserRepository.getInstance();
                User newUser = userRepository.findUserById(i);
                if(newUser != null) {
                    addMember(newUser);
                }
            }
        }
    }
}