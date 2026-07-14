package dao;

import entity.*;

import java.io.*;
import java.util.*;

public class UserRepository 
{
   private String filePath = ".." + File.separator + "data" + File.separator + "users.txt";
   
   private UserRepository()
   {
    try
    {
      File file = new File(filePath);
      File folder = new File(".." + File.separator + "data");
      if(!folder.exists()) folder.mkdir();
      if(!file.exists()) file.createNewFile();
    }

    catch(IOException e) 
    {
      e.printStackTrace();
    }
    
   }

   private static class UserRepositoryInner
   {
      private static final UserRepository INSTANCE = new UserRepository();
   }

   public static UserRepository getInstance(){ return UserRepositoryInner.INSTANCE; }

   public void save(User user)
   {
      try(FileWriter writer = new FileWriter(filePath , true))
      {
         writer.write(user.getId()+" , "+user.getUserName()+" , "+user.getFullName()+" , "+user.getPassword()+"\n");
      }

      catch(IOException e)  
      {
         e.printStackTrace();
      }
   }

   public void changeFullName(String userId ,String newName)
   {
      List<User> users = getAllUsers();

      try(FileWriter writer = new FileWriter(filePath , false))
      {
         for(int i=0 ; i<users.size() ; i++)
         {
            if(userId.equals(users.get(i).getId()))
            {
               writer.write(users.get(i).getId()+" , "+users.get(i).getUserName()+" , "+newName+" , "+users.get(i).getPassword()+"\n");
            }

            else
            {
               writer.write(users.get(i).getId()+" , "+users.get(i).getUserName()+" , "+users.get(i).getFullName()+" , "+users.get(i).getPassword()+"\n");
            }
         }
      }

      catch(IOException e)  
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
            if (userData.length >= 4) {
                  User newUser = new User(userData[0], userData[1], userData[2], userData[3], true);
                  users.add(newUser);
            }
         }
      }

      catch(IOException e)  
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

   public User findUserById(String id)
   {
      List<User> users = makeArrayOfAllUsers();

      for(int i=0 ; i<users.size() ; i++)
      {
         if((users.get(i).getId()).equals(id))
         {
            return users.get(i);
         }
      }

      return null;
   }

   public List<User> getAllUsers()
   {
      return makeArrayOfAllUsers();
   }

   public User findUserByAnyField(String username, String fullName) {
      for(User u : makeArrayOfAllUsers()) {
         if (!username.isEmpty() && u.getUserName().equalsIgnoreCase(username)) return u;
         if (!fullName.isEmpty() && u.getFullName().equalsIgnoreCase(fullName)) return u;
      }
      return null;
   }
}
