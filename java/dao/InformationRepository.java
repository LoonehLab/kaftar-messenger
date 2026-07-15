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
            File lastSeenFile = new File(folder, "lastSeen.txt");
            if(!lastSeenFile.exists())
            {
            lastSeenFile.createNewFile();
            }
            File blockFile = new File(folder, "block.txt");
            if(!blockFile.exists())
            {
            blockFile.createNewFile();
            }
        }
        catch(IOException e) 
        {
        e.printStackTrace();
        }
    }

    public void saveBlockedUser(User user)
    {
        File file = new File(userDirectory, "block.txt");
        try(FileWriter writer = new FileWriter(file, true))
        {
            writer.write(user.getId()+"\n");
        }
        catch(IOException e) 
        {
        e.printStackTrace();
        }
    }

    public void saveLastSeen(long lastSeen)
    {
        File file = new File(userDirectory, "lastSeen.txt");
        try(FileWriter writer = new FileWriter(file, false))
        {
            writer.write(String.valueOf(lastSeen));
        }
        catch(IOException e) 
        {
        e.printStackTrace();
        }
    }

    public long getLastSeen()
    {
        long result = 0;

        File file = new File(userDirectory, "lastSeen.txt");
        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            String line;
            if ((line = reader.readLine()) != null) 
            {
                if(!line.isEmpty()) 
                {
                    result = Long.parseLong(line);
                }
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    public void unreadMessagesPlusOne(Chat chat)
    {
        String fileName = "";
        int count = 0;

        if(chat instanceof PV)
        {
            fileName = "pv.txt";
            File file = new File(userDirectory, fileName);
            PV pv = (PV)chat;
            List<String> lines = new ArrayList<>();
            String targetId = (pv.getFirstPerson().getId()).equals(user.getId()) ? pv.getSecondPerson().getId() : pv.getFirstPerson().getId();
            if (file.exists()) 
            {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) 
                {
                    String line;
                    while ((line = reader.readLine()) != null) 
                    {
                        String[] parts = line.split(":");
                        if(parts[0].equals(targetId))
                        {
                            count = Integer.parseInt((parts[1].split(","))[1])+1;
                            String[] help = line.split(",");
                            lines.add(help[0]+","+count);
                        }

                        else
                        {
                            lines.add(line);
                        }
                    }

                    try(FileWriter writer = new FileWriter(file , false))
                    {
                        for(String v : lines)
                        {
                            writer.write(v+"\n");
                        }
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                catch(IOException e) { e.printStackTrace(); }
            }
        }

        else
        {
            fileName = "group.txt";
            File file = new File(userDirectory, fileName);
            Group group = (Group)chat;
            List<String> lines = new ArrayList<>();
            String targetId = group.getId();
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) 
                {
                    String line;
                    while ((line = reader.readLine()) != null) 
                    {
                        String[] parts = line.split(":");
                        if(parts[0].equals(targetId))
                        {
                            count = Integer.parseInt((parts[1].split(","))[1])+1;
                            String[] help = line.split(",");
                            lines.add(help[0]+","+count);
                        }

                        else
                        {
                            lines.add(line);
                        }
                    }

                    try(FileWriter writer = new FileWriter(file , false))
                    {
                        for(String v : lines)
                        {
                            writer.write(v+"\n");
                        }
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                catch(IOException e) { e.printStackTrace(); }
            }
        }
    }

    public void readMessages(Chat chat)
    {
        String fileName = "";

        if(chat instanceof PV)
        {
            fileName = "pv.txt";
            File file = new File(userDirectory, fileName);
            PV pv = (PV)chat;
            List<String> lines = new ArrayList<>();
            String targetId = (pv.getFirstPerson().getId()).equals(user.getId()) ? pv.getSecondPerson().getId() : pv.getFirstPerson().getId();
            if (file.exists()) 
            {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) 
                {
                    String line;
                    while ((line = reader.readLine()) != null) 
                    {
                        String[] parts = line.split(":");
                        if(parts[0].equals(targetId))
                        {
                            String[] help = line.split(",");
                            lines.add(help[0]+",0");
                        }

                        else
                        {
                            lines.add(line);
                        }
                    }

                    try(FileWriter writer = new FileWriter(file , false))
                    {
                        for(String v : lines)
                        {
                            writer.write(v+"\n");
                        }
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                catch(IOException e) { e.printStackTrace(); }
            }
        }

        else
        {
            fileName = "group.txt";
            File file = new File(userDirectory, fileName);
            Group group = (Group)chat;
            List<String> lines = new ArrayList<>();
            String targetId = group.getId();
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) 
                {
                    String line;
                    while ((line = reader.readLine()) != null) 
                    {
                        String[] parts = line.split(":");
                        if(parts[0].equals(targetId))
                        {
                            String[] help = line.split(",");
                            lines.add(help[0]+",0");
                        }

                        else
                        {
                            lines.add(line);
                        }
                    }

                    try(FileWriter writer = new FileWriter(file , false))
                    {
                        for(String v : lines)
                        {
                            writer.write(v+"\n");
                        }
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                catch(IOException e) { e.printStackTrace(); }
            }
        }
    }

    public int getUreadMessagesCount(Chat chat)
    {
        String fileName = "";

        if(chat instanceof PV)
        {
            fileName = "pv.txt";
            File file = new File(userDirectory, fileName);
            PV pv = (PV)chat;
            String targetId = (pv.getFirstPerson().getId()).equals(user.getId()) ? pv.getSecondPerson().getId() : pv.getFirstPerson().getId();
            if (file.exists()) 
            {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) 
                {
                    String line;
                    while ((line = reader.readLine()) != null) 
                    {
                        String[] parts = line.split(":");
                        if(parts[0].equals(targetId))
                        {
                            return Integer.parseInt((parts[1].split(","))[1]);
                        }
                    }
                }
                catch(IOException e) { e.printStackTrace(); }
            }
        }

        else
        {
            fileName = "group.txt";
            File file = new File(userDirectory, fileName);
            Group group = (Group)chat;
            String targetId = group.getId();
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) 
                {
                    String line;
                    while ((line = reader.readLine()) != null) 
                    {
                        String[] parts = line.split(":");
                        if(parts[0].equals(targetId))
                        {
                            return Integer.parseInt((parts[1].split(","))[1]);
                        }
                    }
                }
                catch(IOException e) { e.printStackTrace(); }
            }
        }

        return 0;
    }
    
    public void savePv(PV pv)
    {
        File file = new File(userDirectory, "pv.txt");
        try(FileWriter writer = new FileWriter(file, true))
        {
            if(pv.getFirstPerson() == user)
            {
                writer.write(pv.getSecondPerson().getId()+":normal,0\n");
            }
            else if(pv.getSecondPerson() == user)
            {
                writer.write(pv.getFirstPerson().getId()+":normal,0\n");
            }
        }
        catch(IOException e) { e.printStackTrace(); }
    }

    public void saveGroup(Group group)
    {
        File file = new File(userDirectory, "group.txt");
        try(FileWriter writer = new FileWriter(file, true))
        {
            writer.write(group.getId()+":normal,0\n");
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
        File file = new File(userDirectory, "bio.txt");
        try(FileWriter writer = new FileWriter(file , false))
        {
            writer.write(user.getBio() != null ? user.getBio() : "");
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

    public void saveChatState(String chatId, String type, String state) {
        String fileName = "pv".equalsIgnoreCase(type) ? "pv.txt" : "group.txt";
        File file = new File(userDirectory, fileName);
        List<String> lines = new ArrayList<>();
        boolean found = false;
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(chatId + ":")) {
                        String[] parts = line.split(",");
                        lines.add(chatId + ":" + state+","+parts[1]);
                        found = true;
                    } else if (!line.isEmpty()) {
                        lines.add(line);
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!found){ lines.add(chatId + ":" + state+","+"0"); }
        try (FileWriter writer = new FileWriter(file, false)) {
            for (String l : lines) writer.write(l + "\n");
            }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getChatState(String chatId, String type) {
        String fileName = "pv".equalsIgnoreCase(type) ? "pv.txt" : "group.txt";
        File file = new File(userDirectory, fileName);
        if (!file.exists()) return "normal";
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null)
            {
                if (line.startsWith(chatId + ":"))
                {
                    String[] part = line.split(":");
                    return part[1].split(",")[0].trim();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return "normal";
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
                    ids.add(line.split(":")[0].trim());
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
                    ids.add(line.split(":")[0].trim());
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

    public List<String> getBlockedUsersId()
    {
        List<String> ids = new ArrayList<>();
        File file = new File(userDirectory, "block.txt");
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
        String targetId = (pv.getFirstPerson().getId()).equals(user.getId()) ? pv.getSecondPerson().getId() : pv.getFirstPerson().getId();
        File file = new File(userDirectory, "pv.txt");
        List<String> lines = new ArrayList<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                if (!line.startsWith(targetId + ":") && !line.isEmpty())
                {
                    lines.add(line);
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        try(FileWriter writer = new FileWriter(file , false))
        {
            for(String v : lines)
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
        if (group == null || group.getId() == null) {
            System.out.println("DEBUG: Group or Group ID is null!");
            return;
        }

        File file = new File(userDirectory, "group.txt");
        if (!file.exists()) {
            System.out.println("DEBUG: group.txt does not exist for user: " + user.getId());
            return;
        }

        List<String> lines = new ArrayList<>();
        String targetId = group.getId().trim();

        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(":");
                String currentGroupId = parts[0].trim();
                if (!currentGroupId.equals(targetId))
                {
                    lines.add(line);
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        try(FileWriter writer = new FileWriter(file , false))
        {
            for(String v : lines)
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

    public void removeBlockedUser(User user)
    {
        List<String> ids = getBlockedUsersId();
        
        for(String c : ids)
        {
            if(c.equals(user.getId()))
            {
                ids.remove(c);
                break;
            }
        }

        File file = new File(userDirectory, "block.txt");
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
