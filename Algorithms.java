public class Algorithms {

   public static void main(String[] args) {
      System.out.println(sumAllNumbersLessThan(3));
   }
   
   public static int sumAllNumbersLessThan(int num) {
      int value = 0;
      
      for (int i = 1; 1 < num; i++) {
         value += i;
      }
      return value;
   }
}