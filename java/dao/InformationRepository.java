package dao;

import entity.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InformationRepository 
{
    private String userDirectory;
    //private String dataOfUserPath;

    private User user;

    public InformationRepository(User user)
    {
        userDirectory = ".." + File.separator + "data" + File.separator + "users" + File.separator + user.getId();
        this.user = user;
        try
        {
            File folder = new File(userDirectory);
            if(!folder.exists())
            {
            folder.mkdirs();
            }
            File pvFile = new File(folder, "pv.txt");
            if(!pvFile.exists())
            {
            pvFile.createNewFile();
            }
            File groupFile = new File(folder, "group.txt");
            if(!groupFile.exists())
            {
            groupFile.createNewFile();
            }
            File avatarFile = new File(folder, "avatar.txt");
            if(!avatarFile.exists()) 
            { 
            avatarFile.createNewFile(); 
            }
            File bioFile = new File(folder, "bio.txt");
            if(!bioFile.exists())
            {
            bioFile.createNewFile();
            }
            File contactFile = new File(folder, "contact.txt");
            if(!contactFile.exists())
            {
            contactFile.createNewFile();
            }
        }
        catch(IOException e) 
        {
        e.printStackTrace();
        }
    }

    public void savePv(PV pv)
    {
        File file = new File(userDirectory, "pv.txt");
        try(FileWriter writer = new FileWriter(file, true))
        {
            if(pv.getFirstPerson() == user)
            {
                writer.write(pv.getSecondPerson().getId()+"\n");
            }
            else if(pv.getSecondPerson() == user)
            {
                writer.write(pv.getFirstPerson().getId()+"\n");
            }
        }
        catch(IOException e) 
        {
        e.printStackTrace();
        }
    }

    public void saveGroup(Group group)
    {
        File file = new File(userDirectory, "group.txt");
        try(FileWriter writer = new FileWriter(file, true))
        {
            writer.write(group.getId()+"\n");
        }
        catch(IOException e) 
        {
        e.printStackTrace();
        }
    }

    public void saveAvatar() {
        File file = new File(userDirectory, "avatar.txt");
        try(FileWriter writer = new FileWriter(file, false)) {
            writer.write(user.getAvatar() != null ? user.getAvatar() : "");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void saveBioAndOtherData()
    {
        try(FileWriter writer = new FileWriter("bio.txt" , false))
        {
            writer.write(String.valueOf(user.getHowManyGroupCreated())+"\n"+user.getBio());
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void saveContact(User contact)
    {
        File file = new File(userDirectory, "contact.txt");
        try(FileWriter writer = new FileWriter(file , true))
        {
            writer.write(contact.getId()+"\n");
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public List<String> getPvIds()
    {
        List<String> ids = new ArrayList<>();
        File file = new File(userDirectory, "pv.txt");
        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            String line;
            while ((line = reader.readLine()) != null) 
            {
                if(!line.isEmpty()) 
                {
                    ids.add(line.trim());
                }
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
        File file = new File(userDirectory, "group.txt");
        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            String line;
            while ((line = reader.readLine()) != null) 
            {
                if(!line.isEmpty()) 
                {
                    ids.add(line.trim());
                }
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }

        return ids;
    }

    public String getAvatar() {
        File file = new File(userDirectory, "avatar.txt");
        StringBuilder sb = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public int getHowManyGroupCreated()
    {
        int howManyGroupsCreate = -1;
        File file = new File(userDirectory, "bio.txt");
        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            String line = reader.readLine();
            howManyGroupsCreate = Integer.parseInt(line);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return howManyGroupsCreate;
    }

    public String getBio()
    {
        int counter = 0;
        String bio = null;
        File file = new File(userDirectory, "bio.txt");
        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            String line;

            while ((line = reader.readLine()) != null) 
            {
                if(counter == 0)
                {
                    counter++;
                }

                if(counter == 1)
                {
                    bio = line;
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return bio;
    }

    public List<String> getContactsId()
    {
        List<String> ids = new ArrayList<>();
        File file = new File(userDirectory, "contact.txt");
        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            String line;

            while ((line = reader.readLine()) != null) 
            {
                if(!line.isEmpty()) 
                {
                    ids.add(line.trim());
                }
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }

        return ids;
    }

    public void removePv(PV pv)
    {
        List<String> ids = getPvIds();
        
        for(String p : ids)
        {
            if((pv.getFirstPerson().getId()).equals(user.getId()))
            {
                if(p.equals(pv.getSecondPerson().getId()))
                {
                    ids.remove(p);
                    break;
                }
            }

            if((pv.getSecondPerson().getId()).equals(user.getId()))
            {
                if(p.equals(pv.getFirstPerson().getId()))
                {
                    ids.remove(p);
                    break;
                }
            }
        }

        File file = new File(userDirectory, "pv.txt");
        try(FileWriter writer = new FileWriter(file , false))
        {
            for(String v : ids)
            {
                writer.write(v+"\n");
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void removeGroup(Group group)
    {
        List<String> ids = getGroupIds();
        
        for(String g : ids)
        {
            if(g.equals(group.getId()))
            {
                ids.remove(g);
                break;
            }
        }

        File file = new File(userDirectory, "group.txt");
        try(FileWriter writer = new FileWriter(file , false))
        {
            for(String v : ids)
            {
                writer.write(v+"\n");
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void removeContact(User contact)
    {
        List<String> ids = getContactsId();
        
        for(String c : ids)
        {
            if(c.equals(contact.getId()))
            {
                ids.remove(c);
                break;
            }
        }

        File file = new File(userDirectory, "contact.txt");
        try(FileWriter writer = new FileWriter(file , false))
        {
            for(String v : ids)
            {
                writer.write(v+"\n");
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
