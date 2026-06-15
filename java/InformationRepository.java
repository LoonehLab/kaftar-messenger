import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class InformationRepository 
{
    private String dataOfUserPath;
    private String pvPath;
    private String groupsPath;
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
}
