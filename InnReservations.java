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
   public static void main(String args[]) 
   {
      String url = null;
      String userID = null;
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
         System.out.println ("Driver class found and loaded."); 
      }
      catch (Exception ex) {
         System.out.println("Driver not found");
         System.out.println(ex);
      }

      try {
         String query = null;
         Statement stmt = null;

         // Make the mySQL connection
         conn = DriverManager.getConnection(url, userID, pword);

         // Create "rooms" Table (if it doesn't exist)
         query = "CREATE TABLE IF NOT EXISTS rooms "
               + "LIKE INN.rooms";
         stmt = conn.createStatement();
         stmt.execute(query);

         // Create "reservations" Table (if it doesn't exist)
         query = "CREATE TABLE IF NOT EXISTS reservations "
               + "LIKE INN.reservations";
         stmt = conn.createStatement();
         stmt.execute(query);
         
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

   // Main UI display
   private static void displayMain() 
   {
      // Clear the screen
      // clearScreen();

      // Display UI
      System.out.println("Welcome. Please choose your role:\n\n"
         + "- (A)dmin\n"
         + "- (O)wner\n"
         + "- (G)uest\n"
         + "- (Q)uit\n");
   }

   // Program loop for admin subsystem
   private static void adminLoop() 
   {
      boolean exit = false;
      Scanner input = new Scanner(System.in);

      while (!exit) 
      {
         displayAdmin();

         String[] tokens = input.nextLine().toLowerCase().split(" ");
         char option = tokens[0].charAt(0);
	      System.out.println("option chosen: " + option);

         switch(option) {
            case 'v':   System.out.println("displayTable\n");
                        break;
            case 'c':   System.out.println("clearDB\n");
                        break;
            case 'l':   System.out.println("loadDB\n");
                        break;
            case 'r':   System.out.println("removeDB\n");
                        break;
            case 'b':   exit = true;
                        break;
         }
      }

      input.close();
   }

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
            case 'o':   System.out.println("occupancyMenu\n");
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

   // Clears the console screen when running interactive
   private static void clearScreen() {
      Console c = System.console();
      if (c != null) {

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

   // Admin UI display
   private static void displayAdmin() {

      // Clear the screen -- only if it makes sense to do it
      // clearScreen();

      // Display UI
      // add your own information for the state of the database
      System.out.println("Welcome, Admin.\n\n"
         + "Current Status: " + "<put in state informnation>" + "\n"
         + "Reservations: " + "<put in count of reservations>" + "\n"
         + "Rooms: " + "<put in room information>" + "\n\n"
         + "Choose an option:\n"
         + "- (V)iew [table name] - Displays table contents\n"
         + "- (C)lear - Deletes all table contents\n"
         + "- (L)oad - Loads all table contents\n"
         + "- (R)emove - Removes tables\n"
         + "- (B)ack - Goes back to main menu\n");

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


   // Get a date from input
   private static String getDate() {
      Scanner input = new Scanner(System.in);

      String monthName = input.next();
      int month = monthNum(monthName);
      int day = input.nextInt();
      String date = "'2010-" + month + "-" + day + "'";
      return date;
   }

   // Convert month name to month number
   private static int monthNum(String month) {
      switch (month) {
         case "january": return 1;
         case "february": return 2;
         case "march": return 3;
         case "april": return 4;
         case "may": return 5;
         case "june": return 6;
         case "july": return 7;
         case "august": return 8;
         case "september": return 9;
         case "october": return 10;
         case "november": return 11;
         case "december": return 12;
      }

      return 0;
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
   private static char askIfQuit() {
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
