public class Message 
{
    private String id;
    private String senderId;
    private String receiverId;
    private String text;
    private long time;

    Message(String id , String senderId , String receiverId , String text , long time)
    {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.time = time;
    }

    public String getId(){ return id;}
    public String getSenderId(){ return senderId;}
    public String getReceiverId(){ return receiverId;}
    public String getText(){ return text;}
    public long getTime(){ return time;}

    public void setText(String text){ this.text = text; }
}
