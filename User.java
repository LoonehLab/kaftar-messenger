public class User
{
    private String userName;
    private String id;
    private String password;

    User(String id , String userName , String password)
    {
        this.id = id;
        this.userName = userName;
        this.password = password;
    }

    public String getUserName(){ return userName;}
    public String getId(){ return id;}
    public String getPassword(){ return password;}

    public void setUserName(String userName){ this.userName = userName; }
    public void setId(String id){ this.id = id; }
    public void setPassword(String password){ this.password = password; }
}