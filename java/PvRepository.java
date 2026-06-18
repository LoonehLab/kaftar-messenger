import java.io.*;
import java.util.*;

public class PvRepository 
{
    private String filePath;
    private PV pv;

    PvRepository(PV pv)
    {
        this.pv = pv;
        String pvsDirectory = ".." + File.separator + "data" + File.separator + "pvs";
        
        File folder = new File(pvsDirectory);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String fileName = pv.getFirstPerson().getId() + pv.getSecondPerson().getId() + ".txt";
        File file = new File(folder, fileName);

        if (!file.exists()) 
        {
            String reverseFileName = pv.getSecondPerson().getId() + pv.getFirstPerson().getId() + ".txt";
            File reverseFile = new File(folder, reverseFileName);

            if (reverseFile.exists()) 
            {
                file = reverseFile;
            }
            else 
            {
                try 
                {
                    file.createNewFile();
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
    }

    //fekr konam file kolan pak she behtare
    public void deletPv()
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

    public void addMessage(Message message)
    {
        try(FileWriter writer = new FileWriter(filePath , true))
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
        int counter = 1;

        try(BufferedReader reader = new BufferedReader(new FileReader(filePath)))
        {
            String line;

           while ((line = reader.readLine()) != null) 
           {
            if(counter == 1 || counter == 2 || counter == 3)
            {
                counter++;
                continue;
            }

            else
            {
                String[] messageData = line.split(" , ");

                int newId = Integer.parseInt(messageData[1]);

                Message newMessage = new Message(newId, messageData[2], messageData[3], messageData[0] , Long.parseLong(messageData[4]) ,  Boolean.parseBoolean(messageData[5]) , false, Boolean.parseBoolean(messageData[6]));

                array.add(newMessage);
            }
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

        try(FileWriter writer = new FileWriter(filePath , false))
        {
            writer.write(pv.getFirstPerson().getId()+"\n"+pv.getSecondPerson().getId()+"\n"+pv.getIsDeleted()+"\n");
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
                try(FileWriter writer = new FileWriter(filePath , false))
                {
                    writer.write("");
                }

                catch(IOException e) 
                {
                   e.printStackTrace();
                } 
                addMessage(m);
            }
        }
    }

}
