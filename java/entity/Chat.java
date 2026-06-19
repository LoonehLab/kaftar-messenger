package entity;

public abstract class Chat 
{
    //background 

    public abstract void send(Message text);
    public abstract void edit(String newText , Message mes);
    public abstract void deleteMessage(Message mes);
    public abstract void deleteChat();
}
