package dao;

import entity.*;

import java.io.*;
import java.util.*;

public class GroupRepository
{
    private String directoryPath;
    private File folder;
    private String avatarPath;
    private String memberPath;
    private String messagePath;
    private String deletePath;
    private String editedPath;
    private String dataPath;
    private Group group;

    public GroupRepository(Group group)
    {
        this.group = group;
        directoryPath = ".." + File.separator + "data" + File.separator + "groups" + File.separator + group.getId();
        
        folder = new File(directoryPath);
        if(!folder.exists())
        {
           folder.mkdirs();
        }

        File dataFile = new File(folder, group.getId() + "data.txt");
        File avatarFile = new File(folder, group.getId() + "avatar.txt");
        File memberFile = new File(folder, group.getId() + "member.txt");
        File messageFile = new File(folder, group.getId() + "message.txt");
        File deletedMessageFile = new File(folder, group.getId() + "delete.txt");
        File editedMessageFile = new File(folder, group.getId() + "edited.txt");

        this.dataPath = dataFile.getAbsolutePath();
        this.avatarPath = avatarFile.getAbsolutePath();
        this.memberPath = memberFile.getAbsolutePath();
        this.messagePath = messageFile.getAbsolutePath();
        this.deletePath = deletedMessageFile.getAbsolutePath();
        this.editedPath = editedMessageFile.getAbsolutePath();

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
            if(!avatarFile.exists()) avatarFile.createNewFile();
            if(!memberFile.exists()) memberFile.createNewFile();
            if(!messageFile.exists()) messageFile.createNewFile();
            if(!deletedMessageFile.exists()) deletedMessageFile.createNewFile();
            if(!editedMessageFile.exists()) editedMessageFile.createNewFile();
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

    public void addDeletedMessage(Message message)
    {
        try(FileWriter writer = new FileWriter(deletePath, true))
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

    public void addEditedMessage(Message message)
    {
        try(FileWriter writer = new FileWriter(editedPath, true))
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

    public List<Message> getArrayOfDeletedMessages()
    {
        List<Message> array = new ArrayList<>();
        File file = new File(deletePath);
        
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

               Message newMessage = new Message(newId, sender, receiver, text, time, edited, true, seen);
               array.add(newMessage);
            }
        }
        catch(IOException e) 
        {
           e.printStackTrace();
        } 

        return array;
    }

    public List<Message> getArrayOfEditedMessages()
    {
        List<Message> array = new ArrayList<>();
        File file = new File(editedPath);
        
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

               Message newMessage = new Message(newId, sender, receiver, text, time, edited, true, seen);
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
                if(mess.getId() == m.getId())
                {
                    addDeletedMessage(mess);
                    continue;
                } 
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

    public void deleteGroupMessages()
    {
        List<Message> messages = getArrayOfMessages();
        for(int i=0 ; i<messages.size() ; i++)
        {
            removeMessage(messages.get(i));
        }

        try(FileWriter writer = new FileWriter(messagePath , false))
        {
            writer.write("");
        }

        catch(IOException e) 
        {
           e.printStackTrace();
        } 
    }

    public void deleteGroup()
    {
        File[] files = folder.listFiles();

        if(files != null)
        {
            for(File f : files)
            {
                f.delete();
            }
        }

        folder.delete();
    }

    public void saveAvatar(String base64Image)
    {
        try(FileWriter writer = new FileWriter(avatarPath, false)) {
            writer.write(base64Image != null ? base64Image : "");
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public String getAvatar()
    {
        File file = new File(avatarPath);
        if (!file.exists()) return "";
        StringBuilder sb = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public void editMessage(String newText , Message message)
    {
        List<Message> messages = getArrayOfMessages();

        for(int i=0 ; i<messages.size() ; i++)
        {
            if(message.getId() == (messages.get(i).getId()))
            {
                addEditedMessage(message);
                messages.get(i).setText(newText);
            }

            addMessage(message);
        }
    }
}