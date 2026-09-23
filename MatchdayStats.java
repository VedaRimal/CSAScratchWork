import java.util.Scanner;

public class MatchdayStats {
   public static void main(String[] args) {
      String teamPlayed = "Ingraham";
      boolean gameToday = true;
      int numPlayers = 8;
      double averageMatchLength = 1.30;
      boolean ballard = true;
      System.out.print("Who did Ballard play today? ");
      Scanner scan = new Scanner(System.in);
      teamPlayed = scan.nextLine();
      System.out.println("Ballard played " + teamPlayed + " and " + numPlayers + " players played.");
      System.out.println("The average match length was " + averageMatchLength + " hours.");
      scan.close();
      
   }
}