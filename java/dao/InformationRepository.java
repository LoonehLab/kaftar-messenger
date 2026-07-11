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
                writer.write(pv.getSecondPerson().getId()+" , "+pv.getIsPinnedForFirst()+"\n");
            }
            else if(pv.getSecondPerson() == user)
            {
                writer.write(pv.getFirstPerson().getId()+" , "+pv.getIsPinnedForSecond()+"\n");
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
            int index = -1;

            for(int i=0 ; i<group.getMembers().size() ; i++)
            {
                if(group.getMembers().get(i).getId().equals(user.getId()))
                {
                    index = i;
                }
            }

            if(index != -1)
            writer.write(group.getId()+" , "+group.getIsPin().get(index)+"\n");

            else
            {
                writer.write(group.getId()+" , "+false+"\n");
            }
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
            writer.write(user.getBio());
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
                String[] parts = line.split(" , ");
                if(!parts[0].isEmpty()) 
                {
                    ids.add(parts[0].trim());
                }
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }

        return ids;
    }

    public List<Boolean> getListOfPvPinned()
    {
        List<Boolean> isPinned = new ArrayList<>();
        File file = new File(userDirectory, "pv.txt");
        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            String line;
            while ((line = reader.readLine()) != null) 
            {
                String[] parts = line.split(" , ");
                if(!parts[1].isEmpty()) 
                {
                    isPinned.add(Boolean.parseBoolean(parts[1]));
                }
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }

        return isPinned;
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
                String[] parts = line.split(" , ");
                if(!parts[0].isEmpty()) 
                {
                    ids.add(parts[0].trim());
                }
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }

        return ids;
    }

    public List<Boolean> getListOfGroupPinned()
    {
        List<Boolean> isPinned = new ArrayList<>();
        File file = new File(userDirectory, "group.txt");
        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            String line;
            while ((line = reader.readLine()) != null) 
            {
                String[] parts = line.split(" , ");
                if(!parts[1].isEmpty()) 
                {
                    isPinned.add(Boolean.parseBoolean(parts[1]));
                }
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }

        return isPinned;
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

    /*public int getHowManyGroupCreated()
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
    }*/

    public String getBio()
    {
        String bio = null;
        File file = new File(userDirectory, "bio.txt");
        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            String line = reader.readLine();
            bio = line;

            if(bio == null)
            {
                return null;
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
        List<Boolean> pin = getListOfPvPinned();
        
        for(int i=0 ; i<ids.size() ; i++)
        {
            if((pv.getFirstPerson().getId()).equals(user.getId()))
            {
                if(ids.get(i).equals(pv.getSecondPerson().getId()))
                {
                    ids.remove(ids.get(i));
                    pin.remove(pin.get(i));
                    break;
                }
            }

            if((pv.getSecondPerson().getId()).equals(user.getId()))
            {
                if(ids.get(i).equals(pv.getFirstPerson().getId()))
                {
                    ids.remove(ids.get(i));
                    pin.remove(pin.get(i));
                    break;
                }
            }
        }

        File file = new File(userDirectory, "pv.txt");
        try(FileWriter writer = new FileWriter(file , false))
        {
            for(int i=0 ; i<ids.size() ; i++)
            {
                writer.write(ids.get(i)+" , "+pin.get(i)+"\n");
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void changePinPv(PV pv)
    {
        List<String> ids = getPvIds();
        List<Boolean> pin = getListOfPvPinned();

        for(int i=0 ; i<ids.size() ; i++)
        {
            if((pv.getFirstPerson().getId()).equals(user.getId()))
            {
                if(ids.get(i).equals(pv.getSecondPerson().getId()))
                {
                    if(pv.getIsPinnedForFirst())
                    {
                        pv.setPinForFirst(false);
                        pin.set(i, false);
                        break;
                    }

                    else
                    {
                        pv.setPinForFirst(true);
                        pin.set(i, true);
                        break;
                    }
                }
            }

            if((pv.getSecondPerson().getId()).equals(user.getId()))
            {
                if(ids.get(i).equals(pv.getFirstPerson().getId()))
                {
                    if(pv.getIsPinnedForSecond())
                    {
                        pv.setPinForSecond(false);
                        pin.set(i, false);
                        break;
                    }

                    else
                    {
                        pv.setPinForSecond(true);
                        pin.set(i, true);
                        break;
                    }
                }
            }
        }

        File file = new File(userDirectory, "pv.txt");
        try(FileWriter writer = new FileWriter(file , false))
        {
            for(int i=0 ; i<ids.size() ; i++)
            {
                writer.write(ids.get(i)+" , "+pin.get(i)+"\n");
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
        List<Boolean> pin = getListOfGroupPinned();
        
        for(int i=0 ; i<ids.size() ; i++)
        {
            if(ids.get(i).equals(group.getId()))
            {
                ids.remove(ids.get(i));
                pin.remove(pin.get(i));
                break;
            }
        }

        File file = new File(userDirectory, "group.txt");
        try(FileWriter writer = new FileWriter(file , false))
        {
            for(int i=0 ; i<ids.size() ; i++)
            {
                writer.write(ids.get(i)+" , "+pin.get(i)+"\n");
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void changePinGroup(Group group)
    {
        List<String> ids = getGroupIds();
        List<Boolean> pin = getListOfGroupPinned();

        for(int i=0 ; i<ids.size() ; i++)
        {
            if((group.getId()).equals(ids.get(i)))
            {
                if(pin.get(i))
                {
                    group.setPinForMember(ids.get(i), false);
                    pin.set(i, false);
                    break;
                }

                else
                {
                    group.setPinForMember(ids.get(i), true);
                    pin.set(i, true);
                    break;
                }
            }
        }

        File file = new File(userDirectory, "group.txt");
        try(FileWriter writer = new FileWriter(file , false))
        {
            for(int i=0 ; i<ids.size() ; i++)
            {
                writer.write(ids.get(i)+" , "+pin.get(i)+"\n");
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

    public boolean getPvIsPinned(PV pv)
    {
        File file = new File(userDirectory, "pv.txt");

        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            String line;

            while((line = reader.readLine()) != null)
            {
                String[] parts = line.split(" , ");

                if(pv.getFirstPerson() == user)
                {
                    if(parts[0].equals(pv.getSecondPerson().getId()))
                    {
                        return Boolean.parseBoolean(parts[1]);
                    }
                }

                else if(pv.getSecondPerson() == user)
                {
                    if(parts[0].equals(pv.getFirstPerson().getId()))
                    {
                        return Boolean.parseBoolean(parts[1]);
                    }
                }
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    public boolean getGroupIsPinned(Group group)
    {
        File file = new File(userDirectory, "group.txt");

        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            String line;

            while((line = reader.readLine()) != null)
            {
                String[] parts = line.split(" , ");

                if(group.getId().equals(parts[0]))
                {
                    
                    return Boolean.parseBoolean(parts[1]);
                }
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }

        return false;
    }
}