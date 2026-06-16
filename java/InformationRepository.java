import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InformationRepository 
{
    private String dataOfUserPath;
    private String pvPath;
    private String groupsPath;
    private String allGroupsPath;
    private String bioPath;
    private String contactsPath;

    private User user;

    public InformationRepository(User user)
    {
        dataOfUserPath = user.getId()+".txt";
        pvPath = user.getId()+"pv.txt";
        groupsPath = user.getId()+"group.txt";
        bioPath = user.getId()+"bio.txt";
        contactsPath = user.getId()+"contact.txt";
        allGroupsPath = "groups.txt";
        this.user = user;

        try
        {
            File dataFile = new File(dataOfUserPath);
            if(!dataFile.exists())
            {
               dataFile.createNewFile();
            }

            File pvFile = new File(pvPath);
            if(!pvFile.exists())
            {
               pvFile.createNewFile();
            }

            File groupFile = new File(groupsPath);
            if(!groupFile.exists())
            {
               groupFile.createNewFile();
            }

            File bioFile = new File(bioPath);
            if(!bioFile.exists())
            {
               bioFile.createNewFile();
            }

            File contactFile = new File(contactsPath);
            if(!contactFile.exists())
            {
               contactFile.createNewFile();
            }

            File allGroupsFile = new File(allGroupsPath);
            if(!allGroupsFile.exists())
            {
                allGroupsFile.createNewFile();
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }


        try(FileWriter writer = new FileWriter(dataOfUserPath , true))
        {
           writer.write(user.getId()+" , "+user.getUserName()+" , "+user.getPassword());
        }

        catch(IOException e) 
        {
           e.printStackTrace();
        }

        try(FileWriter writer = new FileWriter(pvPath , true))
        {
            for(int i=0 ; i<user.getPv().size() ; i++)
            {
                if(i == user.getPv().size()-1)
                {
                    if(((user.getPv()).get(i)).getFirstPerson() == user)
                    {
                        writer.write(((user.getPv()).get(i)).getSecondPerson().getId());
                    }

                    else
                    {
                        writer.write(((user.getPv()).get(i)).getFirstPerson().getId());
                    }   
                }

                else
                {
                    if(((user.getPv()).get(i)).getFirstPerson() == user)
                    {
                        writer.write(((user.getPv()).get(i)).getSecondPerson().getId()+" , ");
                    }

                    else
                    {
                        writer.write(((user.getPv()).get(i)).getFirstPerson().getId()+" , ");
                    }
                }
            }
        }

        catch(IOException e) 
        {
           e.printStackTrace();
        }

        try(FileWriter writer = new FileWriter(groupsPath , true))
        {
           for(int i=0 ; i<user.getGroups().size() ; i++)
           {
            if(i == user.getGroups().size()-1)
            {
                writer.write(user.getGroups().get(i).getId());
            }

            else
            {
               writer.write(user.getGroups().get(i).getId()+" , "); 
            }
           }
        }

        catch(IOException e) 
        {
           e.printStackTrace();
        }

        try(FileWriter writer = new FileWriter(bioPath , true))
        {
           writer.write(user.getBio());
        }

        catch(IOException e) 
        {
           e.printStackTrace();
        }

        try(FileWriter writer = new FileWriter(contactsPath , true))
        {
           for(int i=0 ; i<user.getContacts().size() ; i++)
           {
            if(i == user.getContacts().size()-1)
            {
                writer.write(user.getContacts().get(i).getId());
            }

            else
            {
                writer.write(user.getContacts().get(i).getId()+" , ");
            }
           }
        }

        catch(IOException e) 
        {
           e.printStackTrace();
        }
    }

    public void savePv(PV pv)
    {
        try(FileWriter writer = new FileWriter(pvPath , true))
        {
            if(pv.getFirstPerson() == user)
            {
                writer.write(" , "+pv.getSecondPerson().getId());
            }

            else if(pv.getSecondPerson() == user)
            {
                writer.write(" , "+pv.getFirstPerson().getId());
            }
        }

        catch(IOException e) 
        {
           e.printStackTrace();
        }
    }

    public void saveGroup(Group group)
    {
        try(FileWriter writer = new FileWriter(groupsPath , true))
        {
            writer.write(" , "+group.getId());
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public List<String> getPvIds()
    {
        List<String> ids = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(pvPath)))
        {
            String line = reader.readLine();
            if(line != null && !line.isEmpty())
            {
                String[] arr = line.split(" , ");
                for(String s : arr)
                    ids.add(s.trim());
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }

        return ids;
    }

    public List<String> getGroupIds()
    {
        List<String> ids = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(groupsPath)))
        {
            String line = reader.readLine();
            if(line != null && !line.isEmpty())
            {
                String[] arr = line.split(" , ");
                for(String s : arr)
                    ids.add(s.trim());
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }

        return ids;
    }

    public Group findGroup(String groupId)
    {
        try(BufferedReader reader = new BufferedReader(new FileReader(allGroupsPath)))
        {
            String line;
            while((line = reader.readLine()) != null)
            {
                String[] data = line.split(" , ");
                if(data[0].equals(groupId))
                {
                    User tempUser = new User(data[2], "" ,"", true);
                    User owner = UserRepository.getInstance().findUser(tempUser);
                    if(owner == null) return null;
                    return new Group(owner, data[1], owner);
                }
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    //bayad method benevisi ke etelaat ro list kone va bede , baraye remove etelaat bayad benevisi 
}
