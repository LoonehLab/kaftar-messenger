public class SavedMessage extends PV
{
   SavedMessage(User first , User second)
   {
    super(first, second);
   } 

   @Override
   public void deletePv()
   {
    return;
   }

   @Override
   public void send(Message text)
   {
      text.setChat((PV)this);
      messages.add(text);
      numOfMessages++;
      text.setId(messages.size());

      firstPerson.addMessageInSavedMessage(text);
   }

}
