import java.io.*;
import java.util.*;

public class UserRepository 
{
   private String filePath = "users.txt";
   
   UserRepository()
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

   public void save(User user)
   {
      try(FileWriter writer = new FileWriter(filePath , true))
      {
         writer.write(user.getId()+" , "+user.getUserName()+" , "+user.getPassword()+"\n");
      }

      catch(IOException e)  //برای ارور های ممکن
      {
         e.printStackTrace();
      }
   }

   private List<User> makeArrayOfAllUsers()
   {
      List<User> users = new ArrayList<>();

      try(BufferedReader reader = new BufferedReader(new FileReader(filePath)))
      {
         String line;

         while ((line = reader.readLine()) != null) 
         {
            String[] userData = line.split(" , ");
            
            User newUser = new User(userData[0], userData[1], userData[2]);

            users.add(newUser);
         }
      }

      catch(IOException e)  //برای ارور های ممکن
      {
         e.printStackTrace();
      }

      return users;
   }

   public boolean isExists(String id)
   {
      List<User> users = makeArrayOfAllUsers();

      for(int i=0 ; i<users.size() ; i++)
      {
         if((users.get(i).getId()).equals(id))
         {
            return true;
         }
      }

      return false;
   }

   public User findUserById(User user)
   {
      List<User> users = makeArrayOfAllUsers();

      for(int i=0 ; i<users.size() ; i++)
      {
         if((users.get(i).getId()).equals(user.getId()))
         {
            return users.get(i);
         }
      }

      return null;
   }
}
