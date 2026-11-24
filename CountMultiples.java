public class CountMultiples {
   public static int countMultiples(int largestNum, int numMultiples) {
      int count = 0;
      for(int i = 1; i <= largestNum; i++) {
         int cycle = i;
         if(cycle % numMultiples == 0) {
            count++;
         }
      }
      return count;
   }
   
   public static void main(String[] args) {
      System.out.println(countMultiples(20,3));
      System.out.println(countMultiples(100,5));
      System.out.println(countMultiples(12,4));
      System.out.println(countMultiples(5,10));
      System.out.println(countMultiples(15,1));
      System.out.println(countMultiples(7,7));
      System.out.println(countMultiples(10,2));
      System.out.println(countMultiples(0,3));
   }
}