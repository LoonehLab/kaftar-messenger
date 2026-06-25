package dao;

import entity.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AllGroupsRepository
{
    private String filePath = ".." + File.separator + "data" + File.separator + "groups.txt";

    private AllGroupsRepository()
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

    private static class GroupRepositoryInner
    {
        private static final AllGroupsRepository INSTANCE =
            new AllGroupsRepository();
    }

    public static AllGroupsRepository getInstance()
    {
        return GroupRepositoryInner.INSTANCE;
    }

    public void save(Group group)
    {
        try(FileWriter writer = new FileWriter(filePath,true))
        {
            writer.write(group.getId()+" , "+group.getName()+" , "+group.getOwner().getId()+"\n");
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public Group findById(String groupId)
    {
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath)))
        {
            String line;
            while((line = reader.readLine()) != null)
            {
                String[] data = line.split(" , ");
                if(data[0].equals(groupId))
                {
                    User owner = UserRepository.getInstance().findUserById(data[2]);
                    if(owner == null)return null;
                    return new Group(data[0], owner, data[1], owner);
                }
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public List<Group> findAll()
    {
        List<Group> groups = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath)))
        {
            String line;
            while((line = reader.readLine()) != null)
            {
                String[] data = line.split(" , ");
                User owner = UserRepository.getInstance().findUserById(data[2]);
                if(owner != null)
                    groups.add(new Group(data[0], owner, data[1], owner));
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }

        return groups;
    }
}