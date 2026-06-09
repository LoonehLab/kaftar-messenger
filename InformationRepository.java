import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class InformationRepository 
{
    private String filePath;
    private User user;

    InformationRepository(User user)
    {
        filePath = user.getId();
        this.user = user;

        try
        {
            File file = new File(filePath);

            if(!file.exists())
            {
               file.createNewFile();
            }
        }

        catch(IOException e)
        {
            e.printStackTrace();
        }


        try(FileWriter writer = new FileWriter(filePath , true))
        {
           writer.write("ID: "+user.getId()+"\n"
                        +"UserName: "+user.getUserName()+"\n"
                        +"Password: "+user.getPassword()+"\n"
                        +"Bio: "+user.getBio()+"\n"
                        );
        }

        catch(IOException e) 
        {
           e.printStackTrace();
        }
    }
}
