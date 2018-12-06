/* 
 * Robert Hensley
 * rehensle@calpoly.edu
 * CSC 365 Project A UI
*/

import java.sql.*;
import java.util.*;
import java.io.*;
import java.lang.*;
import java.text.*;
import java.math.*;

// main function. Contains main program loop
public class InnReservations 
{
   private static Connection conn = null;
   private static String userID = null;
   public static void main(String args[]) 
   {
      String url = null;
      String pword = null;

      // get url, userID and password
      try (BufferedReader br = new BufferedReader(new FileReader("ServerSettings.txt"))) 
      {
         url = br.readLine();
         userID = br.readLine();
         pword = br.readLine();
      }
      catch (Exception ex)
      {
         System.out.println("Invalid format for ServerSettings.txt");
         System.out.println(ex);
      }

      // Load the mysql JDBC driver 
      try {
         Class.forName("com.mysql.jdbc.Driver").newInstance();
      } catch (Exception ex) {
         System.out.println("Driver not found");
         System.out.println(ex);
      }

      try {

         // Make the mySQL connection
         conn = DriverManager.getConnection(url, userID, pword);

         // create tables
         createTables();
         
         boolean exit = false;
         Scanner input = new Scanner(System.in);

         // clear the screen to freshen up the display
         clearScreen();

         while (!exit) 
         {
            displayMain();

            char option = input.nextLine().toLowerCase().charAt(0);

            switch(option) 
            {
               case 'a':   adminLoop();
                  break;
               case 'o':   ownerLoop();
                  break;
               case 'g':   guestLoop();
                  break;
               case 'q':   exit = true;
                  break;
            }
         }

         // Close the connection and input
         conn.close();
         input.close();

      }
      catch (Exception ex) {
         ex.printStackTrace( ); // for debugging
      }

   }

   private static void createTables()
   {
      try {
         String query = null;
         Statement stmt = null;

         // Create "rooms" Table (if it doesn't exist)
         query = "CREATE TABLE IF NOT EXISTS myRooms "
               + "LIKE INN.rooms";
         stmt = conn.createStatement();
         stmt.execute(query);

         // Create "reservations" Table (if it doesn't exist)
         query = "CREATE TABLE IF NOT EXISTS myReservations "
               + "LIKE INN.reservations";
         stmt = conn.createStatement();
         stmt.execute(query);
      }
      catch (Exception ex) {
         ex.printStackTrace( ); // for debugging
      }

   }

   // Main UI display
   private static void displayMain() 
   {
      // Clear the screen
      clearScreen();

      // Display UI
      System.out.println("Welcome. Please choose your role:\n\n"
         + "- (A)dmin\n"
         + "- (O)wner\n"
         + "- (G)uest\n"
         + "- (Q)uit\n");
   }

/* ------------- Admin Functions ------------- */

   // Program loop for admin subsystem
   private static void adminLoop() 
   {
      boolean exit = false;
      Scanner input = new Scanner(System.in);

      while (!exit) 
      {
         displayAdmin();
         String table = "";

         String[] tokens = input.nextLine().split(" ");
         char option = tokens[0].toLowerCase().charAt(0);

         if(tokens.length > 1)
            table = tokens[1];

         switch(option) {
            case 'v':   displayTable(table);
                        break;
            case 'c':   clearDB();
                        break;
            case 'l':   loadDB();
                        break;
            case 'r':   removeDB();
                        break;
            case 'b':   exit = true;
                        break;
         }
      }

   }

   // Admin UI display
   private static void displayAdmin() 
   {

      // Clear the screen -- only if it makes sense to do it
      // clearScreen();

      String status = getStatus();
      String res = getTableCounts("myReservations");
      String rooms = getTableCounts("myRooms");

      // Display UI
      // add your own information for the state of the database
      System.out.println("Welcome, Admin.\n\n"
         + "Current Status: " + status + "\n"   // AR-1 Status
         + "Reservations: " + res + "\n"        // AR-1 Reservations
         + "Rooms: " + rooms + "\n\n"           // AR-1 Rooms
         + "Choose an option:\n"
         + "- (V)iew [table name] - Displays table contents\n"
         + "- (C)lear - Deletes all table contents\n"                   
         + "- (L)oad - Loads all table contents\n"
         + "- (R)emove - Removes tables\n"
         + "- (B)ack - Goes back to main menu\n");

   }

   // AR-1. Current Status Display

   private static String getStatus()
   {
      try {
         Statement s = conn.createStatement();
         ResultSet result = s.executeQuery(
            "SELECT COUNT(*) " +
            "FROM INFORMATION_SCHEMA.TABLES " +
            "WHERE TABLE_SCHEMA = '" + userID + "'");
         result.next();

         if(result.getString(1).equals("0"))
            return "no database";
         if(getTableCounts("myRooms").equals("0") ||
            getTableCounts("myReservations").equals("0"))
            return "empty";
      }
      catch (Exception ee) {
         System.out.println("ee170: " + ee);
         return "error";
      }

      return "full";
   }

   private static String getTableCounts(String table)
   {
      try {
         Statement s = conn.createStatement();
         ResultSet result = s.executeQuery("SELECT COUNT(*) FROM " + table);
         result.next();
         return result.getString(1);
      }
      catch (Exception ee) {
         return "<null>";
      }

   }

   // AR-2. Table display

   private static void displayMyRooms()
   {
      /* lengths of VarChar columns (default) */
      int RN = 8;
      int BT = 7;
      int D = 7;
      String header;
      PreparedStatement stmt = null;
      ResultSet rset = null;

      // get max lengths of variables
      try 
      {
         String lengthQ = "SELECT MAX(CHAR_LENGTH(RoomName)) AS maxRN, " 
                        + "MAX(CHAR_LENGTH(BedType)) AS maxBT, " 
                        + "MAX(CHAR_LENGTH(Decor)) AS maxD " 
                        + "FROM myRooms";

         // max length of RoomName
         stmt = conn.prepareStatement(lengthQ);
         rset = stmt.executeQuery();
         rset.next();
         RN = (rset.getInt("maxRN") > RN) ? rset.getInt("maxRN") : RN; 
         BT = (rset.getInt("maxBT") > BT) ? rset.getInt("maxBT") : BT; 
         D  = (rset.getInt("maxD")  > D)  ? rset.getInt("maxD")  : D; 
      }
      catch (Exception ex){
          ex.printStackTrace();
      }
      finally {
         try {
             stmt.close();
         }
         catch (Exception ex) {
            ex.printStackTrace( );    
         }    	
      }

      // print tuples
      try
      {

         stmt = conn.prepareStatement("SELECT * FROM myRooms");
         rset = stmt.executeQuery();
         header = "\n RoomId | "
                + String.format("%-" + RN + "s | ", "RoomName")
                + "Beds | "
                + String.format("%-" + BT + "s | ", "BedType")
                + "MaxOcc | BasePrice | "
                + String.format("%-" + D + "s", "Decor");
         

         System.out.println(header);
         System.out.println(new String(new char[header.length()]).replace("\0", "-"));
               
         while (rset.next())
         {
            System.out.print(String.format(" %-6s | ", rset.getString("RoomId")));
            System.out.print(String.format("%-" + RN + "s | ", rset.getString("RoomName")));
            System.out.print(String.format("%-4s | ", rset.getInt("Beds")));
            System.out.print(String.format("%-" + BT + "s | ", rset.getString("BedType")));
            System.out.print(String.format("%-6s | ", rset.getInt("MaxOcc")));
            System.out.print(String.format("%-9s | ", rset.getInt("BasePrice")));
            System.out.println(String.format("%-" + D + "s", rset.getString("Decor")));
         }
         System.out.println();
         rset.close();
      }
      catch (Exception ex){
          ex.printStackTrace();
      }
      finally {
         try {
             stmt.close();
         }
         catch (Exception ex) {
            ex.printStackTrace( );    
         }    	
      }
   }

   private static void displayTable(String table)
   {

      if(getStatus().equals("no database"))
         return;
      
      if(table.equals("myRooms"))
         displayMyRooms();

      if(table.equals("myReservations"))
         displayMyReservations("");


   }

   // AR-3. Clear database (remove content of tables)

   private static void clearDB()
   {
      if(getStatus().equals("no database"))
         return;

      clearTable("myRooms");
      clearTable("myReservations");
   }

   private static void clearTable(String table)
   {
      try {
         Statement s = conn.createStatement();
	      s.executeUpdate("DELETE FROM " + table);
      }
      catch (Exception ee) {
         System.out.println(ee);
      }
   }

   // AR-4. Load/Reload DB

   private static void loadDB()
   {
      // checking for full table (just return)
      if(getStatus().equals("full"))
         return;
      
      // check if no database (create tables)
      if(getStatus().equals("no database"))
         createTables();

      // populate the tables
      loadDB_helper("INN-build-rooms.sql");
      loadDB_helper("INN-build-reservations.sql");
   }

   private static void loadDB_helper(String file_name)
   {
      try 
      {
         Statement s = conn.createStatement();
         Scanner sc = new Scanner(new File(file_name));
         while (sc.hasNextLine()) 
         {
            PreparedStatement ps = conn.prepareStatement(sc.nextLine() + sc.nextLine()); 
            ps.executeUpdate();
         }
         sc.close();
      }
      catch (Exception ee) {
         System.out.println(ee);
      }
   }

   // AR-5. Database Removal (remove all tables)

   private static void removeDB()
   {
      if(getStatus().equals("no database"))
         return;

      dropTable("myRooms");
      dropTable("myReservations");
   }

   private static void dropTable(String table)
   {
      try {
         Statement s = conn.createStatement();
	      s.executeUpdate("DROP TABLE " + table);
      }
      catch (Exception ee) {
         System.out.println(ee);
      }
   }

/* ------------- Owner Functions ------------- */

   // Program loop for owner subsystem
   private static void ownerLoop() 
   {
      boolean exit = false;
      Scanner input = new Scanner(System.in);

      while (!exit) {
         displayOwner();

         String[] tokens = input.nextLine().toLowerCase().split("\\s");
         char option = tokens[0].charAt(0);
         char dataOpt = 0;

         if (tokens.length == 2)
            dataOpt = tokens[1].charAt(0);

         switch(option) {
            case 'o':   occupancyMenu();
                        break;
            case 'd':   System.out.println("revenueData\n");
                        break;
            case 's':   System.out.println("browseRes()\n");
                        break;
            case 'r':   System.out.println("viewRooms\n");
                        break;
            case 'b':   exit = true;
                        break;
         }
      }
   }

   // during the display of a database table you may offer the option
   // to stop the display (since there are many reservations):
   //    System.out.print("Type (q)uit to exit: ");
   //    etc.

   // Owner UI display
   private static void displayOwner() {
      // Clear the screen
      // clearScreen();

      // Display UI
      System.out.println("Welcome, Owner.\n\n"
         + "Choose an option:\n"
         + "- (O)ccupancy - View occupancy of rooms\n"
         + "- (D)ata [(c)ounts|(d)ays|(r)evenue] - View data on "
            + "counts, days, or revenue of each room\n"
         + "- (S)tays - Browse list of reservations\n"
         + "- (R)ooms - View list of rooms\n"
         + "- (B)ack - Goes back to main menu\n");
   }

   // Convert month name to month number
   private static int monthNum(String month) {
      switch (month) {
         case "january":   return 1;
         case "february":  return 2;
         case "march":     return 3;
         case "april":     return 4;
         case "may":       return 5;
         case "june":      return 6;
         case "july":      return 7;
         case "august":    return 8;
         case "september": return 9;
         case "october":   return 10;
         case "november":  return 11;
         case "december":  return 12;
      }

      return 0; // default
   }

   // Get a date from input
   private static String getDate() {
      Scanner input = new Scanner(System.in);

      String monthName = input.next();
      int month = monthNum(monthName);
      int day = input.nextInt();
      String date = "'2010-" + month + "-" + day + "'";
      return date;
   }
 
   // ask how many dates will be entered
   private static int getNumDates() {
      Scanner input = new Scanner(System.in);

      System.out.print("Enter number of dates (1 or 2): ");

      int numDates = input.nextInt();
      while (numDates != 1 && numDates != 2) {
         System.out.print("Enter number of dates (1 or 2): ");
         numDates = input.nextInt();
      }
      return numDates;
   }

   // get the room code or a 'q' response to back up the menu
   private static String getRoomCodeOrQ() {
      Scanner input = new Scanner(System.in);
      System.out.print("Enter room code for more details "
	 + "(or (q)uit to exit): ");
      String roomCode = input.next();
      return roomCode;
   }

   // OR-1. Occupancy overview
   private static void occupancyMenu()
   {
      clearScreen(); // required
      
      if(getNumDates() == 1)
      {

      }
      
   }

   // OR-2. Revenue

   // OR-3. Reservations

   // OR-4. Rooms

/* ------------- Guest Functions ------------- */

   // Program loop for guest subsystem
   private static void guestLoop() {
      boolean exit = false;
      Scanner input = new Scanner(System.in);

      while (!exit) {
         displayGuest();

         char option = input.next().toLowerCase().charAt(0);

         switch(option) {
            case 'r':   System.out.println("roomsAndRates\n");
                        break;
            case 's':   System.out.println("viewStays\n");
                        break;
            case 'b':   exit = true;
                        break;
         }
      }
   }

   // Guest UI display
   private static void displayGuest() {
      // Clear the screen
      // clearScreen();

      // Display UI
      System.out.println("Welcome, Guest.\n\n"
         + "Choose an option:\n"
         + "- (R)ooms - View rooms and rates\n"
         + "- (S)tays - View availability for your stay\n"
         + "- (B)ack - Goes back to main menu\n");
   }

   // R-1. Rooms and Rates

   // R-2. Checking Room Availability

   // R-3 Pricing

   // R-4 Reservations

   // R-5 Completing a reservation

   // R-6 Updating the databse

/* ------------- Shared Functions ------------- */

   // Clears the console screen when running interactive
   private static void clearScreen() 
   {
      Console c = System.console();
      if (c != null) 
      {
         // Clear screen for the first time
         System.out.print("\033[H\033[2J");
         System.out.flush();
         //c.writer().print(ESC + "[2J");
         //c.flush();

         // Clear the screen again and place the cursor in the top left
         System.out.print("\033[H\033[1;1H");
         System.out.flush();
         //c.writer().print(ESC + "[1;1H");
         //c.flush();
      }
   }

   // AR-2. Table Display
   // OR-5. Detailed reservation information
   private static void displayMyReservations(String whereClause)
   {
      String query = "SELECT * "
                   + "FROM myReservations "
                   + whereClause;
      String header;
      PreparedStatement stmt = null;
      ResultSet rset = null;


      /* lengths of columns subject to change in length */
      int C = 5;
      int LN = 8;
      int FN = 9;

      String lengthQ = "SELECT MAX(CHAR_LENGTH(Code)) AS maxC, "
                        + "MAX(CHAR_LENGTH(LastName)) AS maxLN, " 
                        + "MAX(CHAR_LENGTH(FirstName)) AS maxFN " 
                        + "FROM myReservations "
                        + whereClause;

      // get max lengths of variables
      try 
      {
         stmt = conn.prepareStatement(lengthQ);
         rset = stmt.executeQuery();
         rset.next();
         C  = (rset.getInt("maxC")  > C)  ? rset.getInt("maxC")  :  C; 
         LN = (rset.getInt("maxLN") > LN) ? rset.getInt("maxLN") : LN; 
         FN = (rset.getInt("maxFN") > FN) ? rset.getInt("maxFN") : FN; 
      }
      catch (Exception ex){
          ex.printStackTrace();
      }
      finally {
         try {
             stmt.close();
         }
         catch (Exception ex) {
            ex.printStackTrace( );    
         }    	
      }

      // print tuples
      try
      {

         stmt = conn.prepareStatement(query);
         rset = stmt.executeQuery();
         header = "\n Code  | Room | CheckIn    | CheckOut   | Rate | "
                + String.format("%-" + LN + "s | ", "LastName")
                + String.format("%-" + FN + "s | ", "FirstName")
                + "Adults | Kids";
         
         System.out.println(header);
         System.out.println(new String(new char[header.length()]).replace("\0", "-"));
               
         while (rset.next())
         {
            System.out.print(String.format("%6s | ", rset.getInt("Code")));
            System.out.print(String.format("%-4s | ", rset.getString("Room")));
            System.out.print(String.format("%10s | ", rset.getString("CheckIn")));
            System.out.print(String.format("%10s | ", rset.getString("CheckOut")));
            System.out.print(String.format("%4s | ", rset.getInt("Rate")));
            System.out.print(String.format("%-" + LN + "s | ", rset.getString("LastName")));
            System.out.print(String.format("%-" + FN + "s | ", rset.getString("FirstName")));
            System.out.print(String.format("%6s | ", rset.getInt("Adults")));
            System.out.println(String.format("%4s", rset.getInt("Kids")));
         }
         System.out.println();
         rset.close();
      }
      catch (Exception ex){
          ex.printStackTrace();
      }
      finally {
         try {
             stmt.close();
         }
         catch (Exception ex) {
            ex.printStackTrace( );    
         }    	
      }
   }

   // get the reservation code or a 'q' response to back up the menu
   private static String getReservCodeOrQ() {
      Scanner input = new Scanner(System.in);
      System.out.print("Enter reservation code for more details "
	 + "(or (q)uit to exit): ");
      String rvCode = input.next();
      return rvCode;
   }

   // Revenue and volume data subsystem -- option to continue or quit
   private static char revenueData() {
      Scanner input = new Scanner(System.in);
      char opt;
         System.out.print("Type (c)ount, (d)ays, or (r)evenue to view "
            + "different table data (or (q)uit to exit): ");
         opt = input.next().toLowerCase().charAt(0);

	 return opt;
   }

   // potentially useful for Rooms Viewing Subsystem -- gets option to
   // view room code or reservations room code or exit
   private static String viewRooms() {
      Scanner input = new Scanner(System.in);
	 System.out.print("Type (v)iew [room code] or "
	    + "(r)eservations [room code], or (q)uit to exit: ");

	 char option = input.next().toLowerCase().charAt(0);
	 String roomCode = String.valueOf(option);
	 if (option != 'q')
	    roomCode = roomCode + " '" + input.next() + "'";
	 return roomCode;
   }

   // ask user if they wish to quit
   private static char askIfQuit() 
   {
      Scanner input = new Scanner(System.in);

	   System.out.print("Enter (q)uit to quit: ");
	   char go = input.next().toLowerCase().charAt(0);

	   return go;
   }


   // ask user if they wish to go back
   private static char askIfGoBack() {
      Scanner input = new Scanner(System.in);

	   System.out.print("Enter (b)ack to go back: ");
	   char go = input.next().toLowerCase().charAt(0);

	   return go;
   }


   // potentially useful for check availability subsystem
   private static char availabilityOrGoBack() {
      Scanner input = new Scanner(System.in);

      System.out.print("Enter (a)vailability, or "
	 + "(b)ack to go back: ");
      char option = input.next().toLowerCase().charAt(0);

      return option;
   }

   // Check availability subsystem:
   // ask if they want to place reservation or renege
   private static char reserveOrGoBack() {
      Scanner input = new Scanner(System.in);

      System.out.print("Enter (r)eserve to place a reservation, "
	 + "or (b)ack to go back: ");
      char option = input.next().toLowerCase().charAt(0);

      return option;
   }

   // Get the user's first name (for making a reservation)
   private static String getFirstName() {
      Scanner input = new Scanner(System.in);

      System.out.print("Enter your first name: ");
      String firstName = "'" + input.next() + "'";
      return firstName;
   }

   // Get the user's last name (for making a reservation)
   private static String getLastName() {
      Scanner input = new Scanner(System.in);

      System.out.print("Enter your last name: ");
      String lastName = "'" + input.next() + "'";
      return lastName;
   }

   // Get the number of adults for a reservation
   private static int getNumAdults() {
      Scanner input = new Scanner(System.in);

      System.out.print("Enter number of adults: ");
      int numAdults = input.nextInt();
      return numAdults;
   }

   // Get the number of children for a reservation
   private static int getNumChildren() {
      Scanner input = new Scanner(System.in);

      System.out.print("Enter number of children: ");
      int numChildren = input.nextInt();
      return numChildren;
   }

   // get discount for a room reservation
   private static String getDiscount() {
      Scanner input = new Scanner(System.in);

      System.out.print("Enter discount (AAA or AARP, if applicable): ");
      String dsName = input.nextLine().toUpperCase();

      return dsName;
   }

}
