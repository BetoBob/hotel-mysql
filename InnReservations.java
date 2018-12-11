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

   // AR-1. Current Status Display

   private static String getStatus()
   {
      String query = "SELECT COUNT(*)"
                   + " FROM information_schema.tables"
                   + " WHERE table_schema = '"  + userID + "'"
                   + " AND table_name = 'myRooms'"
                   + " OR table_name = 'myReservations'";

      try {
         Statement s = conn.createStatement();
         ResultSet result = s.executeQuery(query);
         result.next();

         if(result.getString(1).equals("0") || 
            result.getString(1).equals("1"))
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

   private static void displayMyReservations(String whereClause)
   {
      String query = "SELECT * FROM myReservations "
                   + whereClause;
      String header;

      /* lengths of columns subject to change in length */
      int C = 5;
      int LN = 8;
      int FN = 9;

      String lengthQ = "SELECT MAX(CHAR_LENGTH(Code)) AS maxC, "
                     + "MAX(CHAR_LENGTH(LastName)) AS maxLN, " 
                     + "MAX(CHAR_LENGTH(FirstName)) AS maxFN " 
                     + "FROM myReservations "
                     + whereClause;;

      PreparedStatement stmt = null;
      ResultSet rset = null;

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

   private static void displayMyRooms(String whereClause)
   {
      String query = "SELECT * FROM myRooms " + whereClause;
      String header;

      /* lengths of VarChar columns (default) */
      int RN = 8;
      int BT = 7;
      int D = 7;

      String lengthQ = "SELECT MAX(CHAR_LENGTH(RoomName)) AS maxRN, " 
                        + "MAX(CHAR_LENGTH(BedType)) AS maxBT, " 
                        + "MAX(CHAR_LENGTH(Decor)) AS maxD " 
                        + "FROM myRooms "
                        + whereClause;

      PreparedStatement stmt = null;
      ResultSet rset = null;

      // get max lengths of variables
      try 
      {
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

         stmt = conn.prepareStatement(query);
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
         displayMyRooms(" ");

      if(table.equals("myReservations"))
         displayMyReservations(" ");

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

/* ------------- Owner Functions ------------- */

   // Convert month name to month number
   private static int monthNum(String month) {
      switch (month) {
         case "1":  case "january":   return 1;
         case "2":  case "february":  return 2;
         case "3":  case "march":     return 3;
         case "4":  case "april":     return 4;
         case "5":  case "may":       return 5;
         case "6":  case "june":      return 6;
         case "7":  case "july":      return 7;
         case "8":  case "august":    return 8;
         case "9":  case "september": return 9;
         case "10": case "october":   return 10;
         case "11": case "november":  return 11;
         case "12": case "december":  return 12;
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

   private static boolean checkDates(String date1, String date2)
   {
      String query = "SELECT DATEDIFF(" + date2 + ", " + date1 + ") AS diff";

      try {
         Statement s = conn.createStatement();
         ResultSet result = s.executeQuery(query);
         result.next();
         
         if(result.getInt("diff") >= 0)
            return true;
      }
      catch (Exception ee) {
         System.out.println("ee170: " + ee);
      }

      return false;
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
   private static void displayOneOcc(String date)
   {
      String query = "SELECT DISTINCT RoomId, RoomName, 'occupied' AS Status"
                   + " FROM myReservations re JOIN myRooms ro ON (re.Room = ro.RoomId)"
                   + " WHERE DATEDIFF(CheckIn,  DATE(" + date + ")) <= 0"
                   + "   AND DATEDIFF(CheckOut, DATE(" + date + ")) > 0"
                   + " UNION" 
                   + " SELECT DISTINCT RoomId, RoomName, 'empty' AS Status"  
                   + " FROM myRooms"
                   + " WHERE RoomId NOT IN ("
                     + " SELECT DISTINCT RoomId"
                     + " FROM myReservations re JOIN myRooms ro ON (re.Room = ro.RoomId)"
                     + " WHERE DATEDIFF(CheckIn,  DATE(" + date + ")) <= 0"
                     + "   AND DATEDIFF(CheckOut, DATE(" + date + ")) > 0"
                  + " ) ORDER BY RoomId";

      String header;

      /* lengths of VarChar columns (default) */
      int RN = 8;
      String lengthQ = "SELECT MAX(CHAR_LENGTH(RoomName)) AS maxRN FROM myRooms";

      PreparedStatement stmt = null;
      ResultSet rset = null;

      // get max lengths of variables
      try 
      {
         // max length of RoomName
         stmt = conn.prepareStatement(lengthQ);
         rset = stmt.executeQuery();
         rset.next();
         RN = (rset.getInt("maxRN") > RN) ? rset.getInt("maxRN") : RN; 
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

         header = "\n RoomId | "
                + String.format("%-" + RN + "s | ", "RoomName")
                + "Status    ";         

         System.out.println(header);
         System.out.println(new String(new char[header.length()]).replace("\0", "-"));
               
         while (rset.next())
         {
            System.out.print(String.format(" %-6s | ", rset.getString("RoomId")));
            System.out.print(String.format("%-" + RN + "s | ", rset.getString("RoomName")));
            System.out.println(rset.getString("Status"));
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

   private static void displayMultiOcc(String date1, String date2)
   {
      String query = "SELECT DISTINCT RoomId, RoomName, 'fully occupied' AS Status"
                   + " FROM myReservations re JOIN myRooms ro ON (re.Room = ro.RoomId)"
                   + " WHERE DATEDIFF(CheckIn,  DATE(" + date1 + ")) <= 0"
                   + "   AND DATEDIFF(CheckOut, DATE(" + date2 + ")) > 0"
                   + " UNION"
                   + " SELECT DISTINCT RoomId, RoomName, 'partially occupied' AS Status"
                   + " FROM myReservations re JOIN myRooms ro ON (re.Room = ro.RoomId)"
                   + " WHERE (DATEDIFF(CheckIn, DATE(" + date1 + ")) > 0"
                   + "   AND DATEDIFF(CheckIn, DATE(" + date2 + ")) <= 0)"
                   + "    OR (DATEDIFF(CheckOut, DATE(" + date1 + ")) > 0"
                   + " AND DATEDIFF(CheckOut, DATE(" + date2 + ")) <= 0)"
                   + " UNION"
                   + " SELECT DISTINCT RoomId, RoomName, 'empty' AS STATUS"
                   + " FROM myRooms"
                   + " WHERE RoomId NOT IN ("
                     + " SELECT DISTINCT RoomId"
                     + " FROM myReservations re JOIN myRooms ro ON (re.Room = ro.RoomId)"
                     + " WHERE (DATEDIFF(CheckIn,  DATE(" + date1 + ")) <= 0" 
                     + "   AND DATEDIFF(CheckOut, DATE(" + date2 + ")) > 0)"  
                     + "    OR (DATEDIFF(CheckIn, DATE(" + date1 + ")) > 0"
                     + "   AND DATEDIFF(CheckIn, DATE(" + date2 + ")) <= 0)"    
                     + "    OR (DATEDIFF(CheckOut, DATE(" + date1 + ")) > 0"
                     + "   AND DATEDIFF(CheckOut, DATE(" + date2 + ")) <= 0))"; 

      String header;

      /* lengths of VarChar columns (default) */
      int RN = 8;
      String lengthQ = "SELECT MAX(CHAR_LENGTH(RoomName)) AS maxRN FROM myRooms";

      PreparedStatement stmt = null;
      ResultSet rset = null;

      // get max lengths of variables
      try 
      {
         // max length of RoomName
         stmt = conn.prepareStatement(lengthQ);
         rset = stmt.executeQuery();
         rset.next();
         RN = (rset.getInt("maxRN") > RN) ? rset.getInt("maxRN") : RN; 
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

         header = "\n RoomId | "
                + String.format("%-" + RN + "s | ", "RoomName")
                + "Status          ";         

         System.out.println(header);
         System.out.println(new String(new char[header.length()]).replace("\0", "-"));
               
         while (rset.next())
         {
            System.out.print(String.format(" %-6s | ", rset.getString("RoomId")));
            System.out.print(String.format("%-" + RN + "s | ", rset.getString("RoomName")));
            System.out.println(rset.getString("Status"));
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

   private static void occupancyMenu()
   {
      if(getStatus().equals("no databse"))
         return;
      
      clearScreen(); // required

      int dates = getNumDates();
      String date1 = "0000-00-00";
      String date2 = "0000-00-00";
      String room = "000";
      String where = " ";
      
      if(dates == 1)
      {
         System.out.print("Enter a date [month] [day]: ");
         date1 = getDate();

         displayOneOcc(date1);

         room = getRoomCodeOrQ();

         while(!(room.toUpperCase().equals("Q")))
         {
            where = "WHERE Room = '" + room + "'"
                  + "  AND DATEDIFF(CheckIn,  DATE(" + date1 + ")) <= 0"
                  + "  AND DATEDIFF(CheckOut, DATE(" + date1 + ")) > 0";

            displayDetailedReservations(where);

            room = getRoomCodeOrQ();
         }
      }

      if(dates == 2)
      {
         System.out.print("\nEnter a start date [month] [day]: ");
         date1 = getDate();
         System.out.print("Enter an end date  [month] [day]: ");
         date2 = getDate();

         if(checkDates(date1, date2) == false)
            return;

         displayMultiOcc(date1, date2);

         room = getRoomCodeOrQ();

         while(!(room.toUpperCase().equals("Q")))
         {
            where = "WHERE Room = '" + room + "'"
                  + "  AND ((DATEDIFF(CheckIn,  DATE(" + date1 + ")) <= 0"     // FirstDate inner
                  + "  AND DATEDIFF(CheckOut,  DATE(" + date2 + ")) > 0)"     // LastDate inner
                  + "   OR (DATEDIFF(CheckIn,  DATE(" + date1 + ")) > 0"      // FirstDate outer left
                  + "  AND  DATEDIFF(CheckIn,  DATE(" + date2 + ")) <= 0)"    // LastDate outer left
                  + "   OR (DATEDIFF(CheckOut, DATE(" + date1 + ")) > 0"      // FirstDate outer right
                  + "  AND  DATEDIFF(CheckOut, DATE(" + date2 + ")) <= 0))";   // LastDate outer right
            
            displayDetailedReservations(where);

            room = getRoomCodeOrQ();
         }
      }
      
   }

   // OR-2. Revenue

   private static void displayRevenue(String opt, String optAs)
   {
      String query = "SELECT RoomId, RoomName, Month, " + opt
                   + " FROM ("
                     + " SELECT ro.RoomId, ro.RoomName,"
                     + " MONTH(CheckOut) AS monthId, MONTHNAME(CheckOut) AS Month, "
                     + optAs
                     + " FROM myReservations re JOIN myRooms ro ON(re.Room = ro.RoomId)"
                     + " WHERE YEAR(CheckOut) = 2010"
                     + " GROUP BY ro.RoomId, ro.RoomName, monthId, Month"
                     + " UNION" 
                     + " SELECT ro.RoomId, ro.RoomName,"
                     + " 13 AS monthId, 'Total' AS Month, "
                     + optAs
                     + " FROM myReservations re JOIN myRooms ro ON(re.Room = ro.RoomId)"
                     + " WHERE YEAR(CheckOut) = 2010"
                     + " GROUP BY ro.RoomId, ro.RoomName"
                   + " ) AS rt"
                   + " ORDER BY RoomId, monthId";

      String header;

      /* lengths of VarChar columns (default) */
      int RN = 8;
      PreparedStatement stmt = null;
      ResultSet rset = null;

      String lengthQ = "SELECT MAX(CHAR_LENGTH(RoomName)) AS maxRN FROM myRooms";

      // get max lengths of variables
      try 
      {
         // max length of RoomName
         stmt = conn.prepareStatement(lengthQ);
         rset = stmt.executeQuery();
         rset.next();
         RN = (rset.getInt("maxRN") > RN) ? rset.getInt("maxRN") : RN; 
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

         header = "\n RoomId | "
                + String.format("%-" + RN + "s | ", "RoomName")
                + "Month     | " + opt;         

         System.out.println(header);
         System.out.println(new String(new char[header.length()]).replace("\0", "-"));
               
         while (rset.next())
         {
            System.out.print(String.format(" %-6s | ", rset.getString("RoomId")));
            System.out.print(String.format("%-" + RN + "s | ", rset.getString("RoomName")));
            System.out.print(String.format("%-9s | ", rset.getString("Month")));
            System.out.println(String.format("%" + opt.length() + "s", rset.getString(opt)));
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

   // Revenue and volume data subsystem -- option to continue or quit
   private static void revenueData() {

      if(getStatus().equals("no database"))
         return;

      clearScreen();

      Scanner input = new Scanner(System.in);
      char opt = '_';
      
      while(opt != 'q')
      {

         System.out.print("Type (c)ount, (d)ays, or (r)evenue to view "
            + "different table data (or (q)uit to exit): ");

         opt = input.next().toLowerCase().charAt(0);

         if(opt == 'c')
            displayRevenue("Reservations", 
               "COUNT(*) AS Reservations");
         
         if(opt == 'd')
            displayRevenue("days_occupied", 
               "SUM(DATEDIFF(CheckOut, CheckIn)) AS days_occupied");

         if(opt == 'r')
            displayRevenue("Revenue", 
               "SUM(DATEDIFF(CheckOut, CheckIn) * Rate) AS Revenue");

      }

   }

   // OR-3. Reservations

   // get the reservation code or a 'q' response to back up the menu
   private static String getReservCodeOrQ() 
   {
      Scanner input = new Scanner(System.in);
      System.out.print("Enter reservation code for more details "
	      + "(or (q)uit to exit): ");
      String rvCode = input.next();
      return rvCode;
   }

   private static String getRoomId()
   {
      Scanner input = new Scanner(System.in);
      System.out.print("\nWould you like to enter a RoomId? (Y/N): ");
      String confirm = input.next();

      if(!confirm.toUpperCase().equals("Y"))
         return " ";

      System.out.print("Enter a three character RoomId: ");
      String roomid = input.next();
      return roomid;
   }

   private static void browseRes()
   {
      String date1 = "'0000-00-00'";
      String date2 = "'0000-00-00'";
      String where = " ";
      String detailedWhere = " ";
      String roomid = " ";
      String code = " ";

      if(getStatus().equals("no database"))
         return;

      clearScreen();

      System.out.print("Enter a start date [month] [day]: ");
      date1 = getDate();
      System.out.print("Enter an end date  [month] [day]: ");
      date2 = getDate();

      if(checkDates(date1, date2) == false)
         return;

      roomid = getRoomId();
      
      if(roomid.equals(" "))
      {
         where = "WHERE DATEDIFF(CheckIn,  DATE(" + date1 + ")) >= 0"
               + "  AND DATEDIFF(CheckIn, DATE(" + date2 + ")) <= 0";
      }
      else
      {
         where = "WHERE Room = '" + roomid + "'"
               + "  AND (DATEDIFF(CheckIn, DATE(" + date1 + ")) >= 0"
               + "  AND DATEDIFF(CheckIn, DATE(" + date2 + ")) <= 0) ";
      }

      displayMyReservations(where);
      
      code = getReservCodeOrQ();

      while(!code.toLowerCase().equals("q"))
      {
         detailedWhere = where + "  AND Code = " + code;
         displayDetailedReservations(detailedWhere);
         code = getReservCodeOrQ();
      }

   }

   // OR-4. Rooms

   private static void displayDetailedRooms(String roomid)
   {
      String query = "SELECT ro.*,"
                   + "  SUM(DATEDIFF(re.CheckOut, re.CheckIn)) AS total_nights,"
                   + "  SUM(DATEDIFF(re.CheckOut, re.CheckIn)) / 365 AS percent_occupied,"
                   + "  SUM(DATEDIFF(re.CheckOut, re.CheckIn) * ro.BasePrice) AS total_revenue,"
                   + "  SUM(DATEDIFF(re.CheckOut, re.CheckIn) * ro.BasePrice) / ("
                   + "     SELECT SUM(rev) FROM ("
                   + "        SELECT SUM(DATEDIFF(re.CheckOut, re.CheckIn) * ro.BasePrice) AS rev"
                   + "        FROM myRooms ro JOIN myReservations re ON (ro.RoomId = re.Room)"
                   + "        WHERE YEAR(CheckOut) = 2010"
                   + "     ) AS st"
                   + "  ) AS percent_revenue"
                   + " FROM myRooms ro JOIN myReservations re ON (ro.RoomId = re.Room)"
                   + " WHERE YEAR(CheckOut) = 2010 AND ro.RoomId = " + roomid
                   + " GROUP BY ro.RoomId, ro.RoomName, ro.Beds, ro.BedType,"
                   + " ro.MaxOcc, ro.BasePrice, ro.Decor";
      
      String header;

      /* lengths of VarChar columns (default) */
      int RN = 8;
      int BT = 7;
      int D = 7;

      String lengthQ = "SELECT MAX(CHAR_LENGTH(RoomName)) AS maxRN, " 
                     + "MAX(CHAR_LENGTH(BedType)) AS maxBT, " 
                     + "MAX(CHAR_LENGTH(Decor)) AS maxD " 
                     + "FROM myRooms";

      PreparedStatement stmt = null;
      ResultSet rset = null;

      // get max lengths of variables
      try 
      {
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

         stmt = conn.prepareStatement(query);
         rset = stmt.executeQuery();
         header = "\n RoomId | "
                + String.format("%-" + RN + "s | ", "RoomName")
                + "Beds | "
                + String.format("%-" + BT + "s | ", "BedType")
                + "MaxOcc | BasePrice | "
                + String.format("%-" + D + "s | ", "Decor")
                + "total_nights | percent_occupied | total_revenue | percent_revenue";
         

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
            System.out.print(String.format("%-" + D + "s | ", rset.getString("Decor")));

            System.out.print(String.format("%12s | ", rset.getString("total_nights")));
            System.out.print(String.format("%16s | ", rset.getString("percent_occupied")));
            System.out.print(String.format("%13s | ", rset.getString("total_revenue")));
            System.out.println(String.format("%15s", rset.getString("percent_revenue")));
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

   // potentially useful for Rooms Viewing Subsystem -- gets option to
   // view room code or reservations room code or exit
   private static void viewRooms() 
   {
      String option, roomCode;

      displayMyRooms(" ");

      Scanner input = new Scanner(System.in);

      System.out.print("Type (v)iew [room code] or "
      + "(r)eservations [room code], or (q)uit to exit: ");
      
      option = input.next().toLowerCase();
      
      while (!option.equals("q"))
      {
         if (option.equals("v") || option.equals("r"))
         {
            System.out.print("Enter a room code: ");
            roomCode = " '" + input.next().toUpperCase() + "'";

            if(option.equals("v"))
               displayDetailedRooms(roomCode);
            else
               displayDetailedReservations("WHERE ro.RoomId = " + roomCode);
         }

         System.out.print("Type (v)iew [room code] or "
         + "(r)eservations [room code], or (q)uit to exit: ");
         
         option = input.next().toLowerCase();
      }
         
   }

   // OR-5. Detailed reservation information
   // adds RoomName, MaxOcc and BasePrice
   private static void displayDetailedReservations(String whereClause)
   {
      String query = "SELECT *"
                   + " FROM myReservations re"
                   + "     JOIN myRooms ro ON (ro.RoomId = re.Room) "
                   + whereClause;
      String header;
      PreparedStatement stmt = null;
      ResultSet rset = null;


      /* lengths of columns subject to change in length */
      int C = 5;  // Code
      int LN = 8; // LastName
      int FN = 9; // FirstName
      int RN = 9; // RoomName

      String lengthQ = "SELECT MAX(CHAR_LENGTH(re.Code)) AS maxC, "
                     + "MAX(CHAR_LENGTH(re.LastName)) AS maxLN, " 
                     + "MAX(CHAR_LENGTH(re.FirstName)) AS maxFN, " 
                     + "MAX(CHAR_LENGTH(ro.RoomName)) AS maxRN " 
                     + "FROM myReservations re "
                     + "     JOIN myRooms ro ON (ro.RoomId = re.Room) "
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
         RN = (rset.getInt("maxRN") > RN) ? rset.getInt("maxRN") : RN; 
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
         header = "\n Code  | Room | "
                + String.format("%-" + RN + "s | ", "RoomName")
                + "MaxOcc | BasePrice | CheckIn    | CheckOut   | Rate | "
                + String.format("%-" + LN + "s | ", "LastName")
                + String.format("%-" + FN + "s | ", "FirstName")
                + "Adults | Kids ";
         
         System.out.println(header);
         System.out.println(new String(new char[header.length()]).replace("\0", "-"));
               
         while (rset.next())
         {
            System.out.print(String.format("%6s | ", rset.getInt("re.Code")));
            System.out.print(String.format("%-4s | ", rset.getString("re.Room")));
            System.out.print(String.format("%-" + RN + "s | ", rset.getString("ro.RoomName")));
            System.out.print(String.format("%6s | ", rset.getInt("ro.MaxOcc")));
            System.out.print(String.format("%9s | ", rset.getInt("ro.BasePrice")));
            System.out.print(String.format("%10s | ", rset.getString("re.CheckIn")));
            System.out.print(String.format("%10s | ", rset.getString("re.CheckOut")));
            System.out.print(String.format("%4s | ", rset.getInt("re.Rate")));
            System.out.print(String.format("%-" + LN + "s | ", rset.getString("re.LastName")));
            System.out.print(String.format("%-" + FN + "s | ", rset.getString("re.FirstName")));
            System.out.print(String.format("%6s | ", rset.getInt("re.Adults")));
            System.out.println(String.format("%4s", rset.getInt("re.Kids")));
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

   // during the display of a database table you may offer the option
   // to stop the display (since there are many reservations):
   //    System.out.print("Type (q)uit to exit: ");
   //    etc.

   // Owner UI display
   private static void displayOwner() {
      // Clear the screen
      clearScreen();

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
            case 'd':   revenueData();
                        break;
            case 's':   browseRes();
                        break;
            case 'r':   viewRooms();
                        break;
            case 'b':   exit = true;
                        break;
         }
      }
   }

/* ------------- Guest Functions ------------- */

   // R-1. Rooms and Rates

   private static char availabilityOrGoBack() {
      Scanner input = new Scanner(System.in);

      System.out.print("Enter (a)vailability, or "
	 + "(b)ack to go back: ");
      char option = input.next().toLowerCase().charAt(0);

      return option;
   }

   private static void roomsAndRates()
   {
      // startup
      displayMyRooms(" ");

      String code = getRoomCodeOrQ().toUpperCase();
      if(!code.equals("Q"))
      {
         displayMyRooms("WHERE RoomId = '" + code + "'");
         char aChoice = availabilityOrGoBack();
         if(aChoice == 'a')
            checkAvailability(code);
         else
            roomsAndRates();

      }
      
   }

   // R-2. Checking Room Availability

   private static boolean availabilityQuery(String roomid, String date1, String date2)
   {
      String query = "SELECT status"
                   + " FROM ("
                     + " SELECT DISTINCT ro.RoomId, 'occupied' AS status"
                     + " FROM myReservations re JOIN myRooms ro ON (re.Room = ro.RoomId)"
                     + " WHERE (DATEDIFF(CheckIn,  DATE(" + date1 + ")) <= 0"
                     + " AND DATEDIFF(CheckOut,  DATE(" + date2 + ")) > 0)" 
                     + " OR (DATEDIFF(CheckIn,  DATE(" + date1 + ")) > 0"  
                     + " AND DATEDIFF(CheckIn,   DATE(" + date2 + ")) <= 0)" 
                     + " OR (DATEDIFF(CheckOut, DATE(" + date1 + ")) > 0"  
                     + " AND DATEDIFF(CheckOut,  DATE(" + date2 + ")) < 0)"
                     + " UNION"
                     + " SELECT DISTINCT RoomId, 'empty' AS status"
                     + " FROM myRooms"
                     + " WHERE RoomId NOT IN ("
                        + " SELECT DISTINCT re.Room"
                        + " FROM myReservations re JOIN myRooms ro ON (re.Room = ro.RoomId)"
                        + " WHERE (DATEDIFF(CheckIn,  DATE(" + date1 + ")) <= 0"
                        + " AND DATEDIFF(CheckOut,  DATE(" + date2 + ")) > 0)"
                        + " OR (DATEDIFF(CheckIn,  DATE(" + date1 + ")) > 0"
                        + " AND DATEDIFF(CheckIn,   DATE(" + date2 + ")) <= 0)" 
                        + " OR (DATEDIFF(CheckOut, DATE(" + date1 + ")) > 0"  
                        + " AND DATEDIFF(CheckOut,  DATE(" + date2 + ")) < 0)"   
                   + " )) AS tb"
                   + " WHERE RoomId = '" + roomid + "'";

      PreparedStatement stmt = null;
      ResultSet rset = null;

      try
      {
         stmt = conn.prepareStatement(query);
         rset = stmt.executeQuery();
         rset.next();
         if(rset.getString("status").equals("empty"))
            return true;
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
      
      return false;

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

   private static void checkAvailability(String code)
   {
      String date1, date2;
      char choice;

      System.out.print("\nEnter a CheckIn date  [month] [day]: ");
      date1 = getDate();
      System.out.print("Enter a CheckOut date [month] [day]: ");
      date2 = getDate();

      if(availabilityQuery(code, date1, date2))
      {
         System.out.println("\nStatus: Empty");
         // print prices R-3 here
         choice = reserveOrGoBack();
         if(choice == 'r')
            System.out.println("reserveDat");
         else
            roomsAndRates();
      }
      else
      {
         System.out.println("\nStatus: Occupied\n");
         
         choice = availabilityOrGoBack();
         if(choice == 'a')
            checkAvailability(code);
         else
            roomsAndRates();
      }
      
   }

   // R-3 Pricing

   // R-4 Reservations

   // R-5 Completing a reservation

   // R-6 Updating the databse

   // Guest UI display
   private static void displayGuest() {
      // Clear the screen
      clearScreen();

      // Display UI
      System.out.println("Welcome, Guest.\n\n"
         + "Choose an option:\n"
         + "- (R)ooms - View rooms and rates\n"
         + "- (S)tays - View availability for your stay\n"
         + "- (B)ack - Goes back to main menu\n");
   }

   // Program loop for guest subsystem
   private static void guestLoop() {
      boolean exit = false;
      Scanner input = new Scanner(System.in);

      while (!exit) {
         displayGuest();

         char option = input.next().toLowerCase().charAt(0);

         switch(option) {
            case 'r':   clearScreen();
                        roomsAndRates();
                        break;
            case 's':   System.out.println("viewStays\n");
                        break;
            case 'b':   exit = true;
                        break;
         }
      }
   }

/* ------------- Shared Methods ------------- */

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
