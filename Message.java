public class Message 
{
    private int id;
    private String senderId;
    private String receiverId;
    private String text;
    private long time;
    private boolean edited;
    private int numOfEdits;
    private boolean deleted;

    Message(int id , String senderId , String receiverId , String text , long time , boolean edited , int numOfEdits , boolean deleted)
    {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.time = time;
        this.edited = edited;
        this.numOfEdits = numOfEdits;
        this.deleted = deleted;
    }

    public int getId(){ return id;}
    public String getSenderId(){ return senderId;}
    public String getReceiverId(){ return receiverId;}
    public String getText(){ return text;}
    public long getTime(){ return time;}
    public boolean getEdited(){ return edited; }
    public int getNumOfEdits(){ return numOfEdits; }
    public boolean getDeleted(){ return deleted; }

    public void setText(String text)
    {
        this.text = text;
        numOfEdits++;
        edited = true;
    }

    public void setDeleted(boolean isDeleted){ deleted = isDeleted;}
    public void setId(int id){ this.id = id;}
}
