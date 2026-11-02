public class ProjectEuler {
   
   public static int problem1() {
      int num = 0;
      int sum = 0;
      while (num < 1000) {
         if (num % 5 == 0 || num % 3 == 0) {
            sum += num;
              
         }
         num ++;
      }
      return sum;
      
   }
    
   public static void main(String[] args) {
      System.out.println("The answer to problem 1 is: " + problem1());    
   }  
}