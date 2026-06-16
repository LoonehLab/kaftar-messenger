import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GroupRepository
{
    private String filePath = "groups.txt";

    private GroupRepository()
    {
        try
        {
            File file = new File(filePath);
            if(!file.exists()) file.createNewFile();
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private static class GroupRepositoryInner
    {
        private static final GroupRepository INSTANCE =
            new GroupRepository();
    }

    public static GroupRepository getInstance()
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
                    User tempUser = new User(data[2],"","",true);
                    User owner = UserRepository.getInstance().findUser(tempUser);
                    if(owner == null)return null;
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

    public List<Group> findAll()
    {
        List<Group> groups = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath)))
        {
            String line;
            while((line = reader.readLine()) != null)
            {
                String[] data = line.split(" , ");
                User tempUser = new User(data[2],"","",true);
                User owner = UserRepository.getInstance().findUser(tempUser);
                if(owner != null)
                    groups.add(new Group(owner,data[1],owner));
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }

        return groups;
    }
}