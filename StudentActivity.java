public class StudentActivity {

   // instance variables
   private String name;
   private String category;
   private int numberOfStudents;
   private double financialRequirements;
   private boolean scholarshipsAvailable;
   private double fundsAvailable;
   private double meetingCost;
}
   // constructors
   public StudentActivity() {
      name = "Cheese of the Month Club";  
      category = "Recreational";
      numberOfStudents = 25;
      financialRequirements = 0.0;
      scholarshipsAvailable = false;
      fundsAvailable = 100.0;
      meetingCost = 10.0;
   }
   public StudentActivity(String name, String category, int numberOfStudents, double financialRequirements, boolean scholarshipsAvailable ) {
                                 
      this.name = name;
      this.category = category;
      this.numberOfStudents = numberOfStudents;
      this.financialRequirements = financialRequirements;
      this.scholarshipsAvailable = scholarshipsAvailable;
   }
   
   // methods
   public void meet() {
      // decrement the budget by the monthly meeting cost
         fundsAvailable -= meetingCost;
         System.out.println("Subtracted " + meetingCost + " from fundsAvailible. Funds remaining");
         
}