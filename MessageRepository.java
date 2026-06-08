import java.io.*;
import java.util.*;

public class MessageRepository 
{
    private final String filePath = "massages.txt";

    MessageRepository()
    {
        try
        {
            File file = new File(filePath);

            if(!file.exists())
            {
               file.createNewFile();
            }
        }

        catch(IOException e)  //برای ارور های ممکن
        {
            e.printStackTrace();
        }
    }

    public void save(Message message)
    {
      try(FileWriter writer = new FileWriter(filePath , true))
      {
         writer.write(message.getId()+" , "+message.getSenderId()+" , "+message.getReceiverId()+" , "+message.getText()+" , "+message.getTime()+" , "+message.getEdited()+" , "+message.getNumOfEdits()+" , "+message.getDeleted()+"\n");
      }

      catch(IOException e)  //برای ارور های ممکن
      {
         e.printStackTrace();
      }
    }

    private List<Message> makeArrayOfAllMessages()
   {
      List<Message> messages = new ArrayList<>();

      try(BufferedReader reader = new BufferedReader(new FileReader(filePath)))
      {
         String line;

         while ((line = reader.readLine()) != null) 
         {
            String[] messageData = line.split(" , ");
            
            Message newMessage = new Message(messageData[0] , messageData[1] , messageData[2] , messageData[3] , Integer.parseInt(messageData[4]) , Boolean.parseBoolean(messageData[5]) , Integer.parseInt(messageData[6]) , Boolean.parseBoolean(messageData[7]));

            messages.add(newMessage);
         }
      }

      catch(IOException e)  //برای ارور های ممکن
      {
         e.printStackTrace();
      }

      return messages;
   }
}
