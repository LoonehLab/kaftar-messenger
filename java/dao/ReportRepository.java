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
                String lineTrimmed = line.trim();
                if (lineTrimmed.isEmpty()) continue;
                String[] data = lineTrimmed.split(" , ");
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

    public void delete() {
        File file = new java.io.File(filePath);
        if (file.exists() && file.delete()) {
            try {
                file.createNewFile();
            }
            catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }
}
