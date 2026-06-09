public class SavedMessage extends PV
{
   SavedMessage(User first , User second)
   {
    super(first, second);
    secondPerson = first;
   } 

   @Override

   public void deletePv()
   {
    return;
   }
}
