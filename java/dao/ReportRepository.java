package dao;

import entity.*;

import java.util.*;
import java.io.*;

public class ReportRepository 
{
    private String filePath = ".." + File.separator + "data" + File.separator + "reports.txt";

    private ReportRepository()
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

    private static class ReportRepositoryInner
    {
        private static final ReportRepository INSTANCE =
            new ReportRepository();
    }

    public static ReportRepository getInstance()
    {
        return ReportRepositoryInner.INSTANCE;
    }

    public void saveReport(Message msg)
    {
        try(FileWriter writer = new FileWriter(filePath,true))
        {
            writer.write(msg.getSenderId()+" , "+msg.getText()+" , "+msg.getId()+"\n");
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public Map<Integer , String[]> getReports()
    {
        int counter = 1;
        Map<Integer , String[]> reports = new HashMap<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath)))
        {
            String line;
            while((line = reader.readLine()) != null)
            {
                String[] data = line.split(" , ");
                reports.put(counter, data);
                counter++;
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }

        return reports;
    }

    public void delete(int messageId) {
        File file = new File(filePath);
        if (!file.exists()) return;
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) 
        {
            String line;
            while ((line = reader.readLine()) != null) 
            {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] data = line.split(" , ");
                if (data.length > 0 && !data[2].trim().equals(String.valueOf(messageId)))
                {
                    lines.add(line);
                }
            }
        }
        catch (IOException e) { e.printStackTrace(); }


        try (FileWriter writer = new FileWriter(file, false)) {
            for (String l : lines)
            {
                writer.write(l + "\n");
            }
                
        }
        catch (IOException e) { e.printStackTrace(); }
    }
}
