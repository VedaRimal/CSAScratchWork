public class Burger {
   // instance variable
   private int numPatties;
   private boolean isTasty;
   private double gramsOfSauce;
   private String burgerName;
   private int numberOfBites;
   // constructors
   // no argument constructor
   public Burger() {
      numPatties = 2;
      isTasty = true;
      gramsOfSauce = 30;
      burgerName = "Craig";
   }

   public Burger(int numPatties, boolean isTasty, double gramsOfSauce, String burgerName) {
      this.numPatties = numPatties;
      this.isTasty = isTasty;
      this.gramsOfSauce = gramsOfSauce;
      this.burgerName = burgerName;
      this.numberOfBites = 0;
   }

   // Methods
   public void bite() {
      System.out.println("Chomp...");
      numberOfBites++;
   }
   
   public int getNumberOfBites() {
      return numberOfBites;
   }
   
   public static String getBurgerRecipe() {
      return "Cook burger, put on bun, add condiments";
   }
   
   public static void main(String[] args) {
      Burger burger1 = new Burger();
      Burger burger2 = new Burger(2, true, 25, "redmill");
      burger2.bite();
      burger2.bite();
      burger2.bite();
      burger2.bite();
      burger2.bite();
      System.out.println(burger2.getNumberOfBites());
      System.out.println(Burger.getBurgerRecipe());
   }
}