package entity;

public abstract class Chat 
{
    private String chatState = "normal";
    public String getChatState() {
        return chatState;
    }
    public void setChatState(User user, String type, String chatId, String chatState) {
        this.chatState = chatState;
        user.getInformationRepository().saveChatState(chatId, type, chatState);
    }
    public abstract void send(Message text);
    public abstract void edit(String newText , Message mes);
    public abstract void deleteMessage(Message mes);
    public abstract void deleteChat();
}
