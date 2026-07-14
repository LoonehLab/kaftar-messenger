package entity;

public class Message 
{
    private int id;
    private String senderId;
    private String receiverId;
    private String text;
    private long time;
    private boolean edited;
    private boolean deleted;

    private boolean seen;


    public Message(int id , String senderId , String receiverId , String text , long time , boolean edited, boolean deleted)
    {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.time = time;
        this.edited = edited;
        this.deleted = deleted;

        this.seen = false;
    }

    public Message(int id , String senderId , String receiverId , String text , long time , boolean edited, boolean deleted , boolean seen)
    {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.time = time;
        this.edited = edited;
        this.deleted = deleted;

        this.seen = seen;
    }

    public int getId(){ return id;}
    public String getSenderId(){ return senderId;}
    public String getReceiverId(){ return receiverId;}
    public String getText(){ return text;}
    public long getTime(){ return time;}
    public boolean getEdited(){ return edited; }
    public boolean getDeleted(){ return deleted; }
    public boolean getSeen(){ return seen; }

    public void setText(String text)
    {
        this.text = text;
        edited = true;
    }

    public void setDeleted(boolean isDeleted){ deleted = isDeleted;}
    public void setEdited(boolean isEdited){ edited = isEdited;}
    public void setId(int id){ this.id = id;}

    
    public void messageSeen(){ this.seen = true; }
}
