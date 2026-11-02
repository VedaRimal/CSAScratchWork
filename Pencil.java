public class Pencil {
   public static void main(String[] args) {
      Pencil writingPencil = new Pencil();
      System.out.println(writingPencil.sharpen());
      System.out.println(writingPencil.write("Hello"));   
   }

   public static String write(String wordToWrite) {
      return wordToWrite;
      
   }
   
   public static String sharpen() {
      return "Bzzzz";
      
   }
   
   public static String erase() {
      return "shh";
   
   }

}