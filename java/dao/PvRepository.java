package dao;

import entity.*;

import java.io.*;
import java.util.*;

public class PvRepository 
{
    private String filePath;
    private String deletedPath;
    private String editedPath;
    private PV pv;
    private File file;
    private File deletedMessages;
    private File editedMessages;

    public PvRepository(PV pv)
    {
        this.pv = pv;
        String pvsDirectory = ".." + File.separator + "data" + File.separator + "pvs";
        
        File folder = new File(pvsDirectory);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String fileName = pv.getFirstPerson().getId() + pv.getSecondPerson().getId() + ".txt";
        file = new File(folder, fileName);

        String delete = pv.getFirstPerson().getId() + pv.getSecondPerson().getId() + "deletedMessages.txt";
        deletedMessages = new File(folder, delete);

        String edit = pv.getFirstPerson().getId() + pv.getSecondPerson().getId() + "editedMessages.txt";
        editedMessages = new File(folder, edit);

        if (!file.exists()) 
        {
            String reverseFileName = pv.getSecondPerson().getId() + pv.getFirstPerson().getId() + ".txt";
            File reverseFile = new File(folder, reverseFileName);

            String reverseDelete = pv.getSecondPerson().getId() + pv.getFirstPerson().getId() + "deletedMessages.txt";
            File reverseDeletedMessages = new File(folder, reverseDelete);

            String reverseEdit = pv.getSecondPerson().getId() + pv.getFirstPerson().getId() + "editedMessages.txt";
            File reverseEditedMessages = new File(folder, reverseEdit);

            if (reverseFile.exists()) 
            {
                file = reverseFile;
                deletedMessages = reverseDeletedMessages;
                editedMessages = reverseEditedMessages;
            }
            else 
            {
                try 
                {
                    file.createNewFile();
                    deletedMessages.createNewFile();
                    editedMessages.createNewFile();
                    try (FileWriter writer = new FileWriter(file, true)) 
                    {
                        writer.write(pv.getFirstPerson().getId() + "\n" + pv.getSecondPerson().getId() + "\n" + pv.getIsDeleted() + "\n");
                    }
                } 
                catch (IOException e) 
                {
                    e.printStackTrace();
                }
            }
        }
        filePath = file.getAbsolutePath();
        deletedPath = deletedMessages.getAbsolutePath();
        editedPath = editedMessages.getAbsolutePath();
    }

    public void deletPvMessages()
    {
        try(FileWriter writer = new FileWriter(filePath , false))
        {
            writer.write(pv.getFirstPerson().getId()+"\n"+pv.getSecondPerson().getId()+"\n"+pv.getIsDeleted()+"\n");
        }

        catch(IOException e) 
        {
           e.printStackTrace();
        } 
    }

    public boolean deletePv()
    {
        boolean mainDeleted = false;
        if (file.exists()) mainDeleted = file.delete();
        if (deletedMessages.exists()) deletedMessages.delete();
        if (editedMessages.exists()) editedMessages.delete();
        return mainDeleted;
    }

    public void addMessage(Message message) {
    try (FileWriter writer = new FileWriter(filePath, true)) {
        String cleanText = message.getText().replace("║", " ").replace("\n", " ");
        message.setText(cleanText);
        writer.write("║" + message.getId() + " , " + message.getSenderId() + " , " + 
                     message.getReceiverId() + " , " + message.getTime() + " , " + 
                     message.getEdited() + " , " + message.getSeen() + " , " + cleanText + "\n");
    } catch (IOException e) {
        e.printStackTrace();
    }
    }

    public List<Message> getArrayOfMessages() {
        List<Message> array = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return array;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // Sender
            reader.readLine(); // Receiver
            reader.readLine(); // isDeleted

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || !line.startsWith("║")) continue;
                String rawMessage = line.substring(1); 
                String[] messageData = rawMessage.split(" , ", 7); 
                if (messageData.length < 7) continue;
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return array;
    }

    public void removeMessage(Message mess) {
        addDeletedMessage(mess);
        List<Message> keeper = this.getArrayOfMessages();
        try (FileWriter writer = new FileWriter(filePath, false)) {
            writer.write(pv.getFirstPerson().getId() + "\n" + pv.getSecondPerson().getId() + "\n" + pv.getIsDeleted() + "\n");      
            for (Message m : keeper) {
                if (mess.getId() == m.getId()) {
                    continue;
                }
                String messageStr = "║" + m.getId() + " , " + m.getSenderId() + " , " + 
                                    m.getReceiverId() + " , " + m.getTime() + " , " + 
                                    m.getEdited() + " , " + m.getSeen() + " , " + m.getText() + "\n";
                writer.write(messageStr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void editMessage(Message mess, String newText) {
        addEditedMessage(mess);
        List<Message> keeper = this.getArrayOfMessages();
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(pv.getFirstPerson().getId() + "\n" + pv.getSecondPerson().getId() + "\n" + pv.getIsDeleted() + "\n");      
            for (Message m : keeper) {
                if (mess.getId() == m.getId()) {
                    String messageStr = "║" + mess.getId() + " , " + mess.getSenderId() + " , " + 
                                        mess.getReceiverId() + " , " + mess.getTime() + " , " + 
                                        mess.getEdited() + " , " + mess.getSeen() + " , " + newText + "\n";
                    writer.write(messageStr);
                } else {
                    String messageStr = "║" + m.getId() + " , " + m.getSenderId() + " , " + 
                                        m.getReceiverId() + " , " + m.getTime() + " , " + 
                                        m.getEdited() + " , " + m.getSeen() + " , " + m.getText() + "\n";
                    writer.write(messageStr);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addDeletedMessage(Message message)
    {
        try (FileWriter writer = new FileWriter(deletedMessages, true)) 
        {
        String cleanText = message.getText().replace("║", " ").replace("\n", " ");
        message.setText(cleanText);
        writer.write("║" + message.getId() + " , " + message.getSenderId() + " , " + 
                     message.getReceiverId() + " , " + message.getTime() + " , " + 
                     message.getEdited() + " , " + message.getSeen() + " , " + cleanText + "\n");
        } 
        catch (IOException e) { e.printStackTrace(); }
    }

    public void addEditedMessage(Message message)
    {
        try (FileWriter writer = new FileWriter(editedMessages, true)) 
        {
        String cleanText = message.getText().replace("║", " ").replace("\n", " ");
        message.setText(cleanText);
        writer.write("║" + message.getId() + " , " + message.getSenderId() + " , " + 
                     message.getReceiverId() + " , " + message.getTime() + " , " + 
                     message.getEdited() + " , " + message.getSeen() + " , " + cleanText + "\n");
        } 
        catch (IOException e) { e.printStackTrace(); }
    }

    public List<Message> getArrayOfDeletedMessages()
    {
        List<Message> array = new ArrayList<>();
        File file = new File(deletedPath);
        
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
}
