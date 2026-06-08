import java.util.*;

public class User
{
    private String userName;
    private String id;
    private String password;
    private String bio;
    private String phoneNumber;
    private Security security;////////////
    private List<PV> pv = new ArrayList<>();/////////////
    private List<Group> groups = new ArrayList<>();///////////////
    //private List<Contact>;

    User(String id , String userName , String password)
    {
        this.id = id;
        this.userName = userName;
        this.password = password;
        bio = "";
        phoneNumber = "";
    }

    public String getUserName(){ return userName;}
    public String getId(){ return id;}
    public String getPassword(){ return password;}
    public String getBio(){ return bio;}
    public String getPhoneNumber(){ return phoneNumber;}

    public void setUserName(String userName){ this.userName = userName; }
    public void setId(String id){ this.id = id; }
    public void setPassword(String password){ this.password = password; }
    public void setBio(String bio){ this.bio = bio; }
    public void setPhoneNumber(String phoneNumber){ this.phoneNumber = phoneNumber; }
}