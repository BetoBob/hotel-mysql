// Sample JDBC mySQL/Connector J program by Dr. Alex Dekhtyar
// with revisions by E.E. Buckalew
//
import java.sql.*;
import java.util.*;
import java.io.*;
import java.lang.*;

public class JDBCTestMysql1 { 
   private static Connection conn = null;

   public static void main(String args[]) { 
      String userID = null;
      char[ ] pword = null; 

      // Load the mysql JDBC driver 
      try {
	 	Class.forName("com.mysql.jdbc.Driver").newInstance();
	 	System.out.println ("Driver class found and loaded."); 
      }
      catch (Exception ex) {
	 System.out.println("Driver not found");
	 System.out.println(ex);
      };

      // get the userID and password
      Console console = System.console();
      userID = console.readLine("Enter your mySQL userID: ");
      pword = console.readPassword("Enter your mySQL password: ");


      try {

  	 // Now make the mySQL connection (userID is both database and user)
  	 // this way you can run it on your own database
  	 conn = DriverManager.getConnection(
  	   "jdbc:mysql://ambari-head.csc.calpoly.edu/" + userID ,
	   userID, new String(pword) );

      } 
      catch (Exception ex) {
	 System.out.println("Could not open connection");
	 System.out.println(ex);
      };
      System.out.println("\nConnected to mySQL\n");

      // now we're going to create a table and populate it
      try {
	 String table = "CREATE TABLE Books "
	    + "(LibCode INT, Title VARCHAR(50), "
	    + "Author VARCHAR (50), PRIMARY KEY (LibCode))";
	 System.out.println("The CREATE TABLE statement:\n" + table + "\n");

	 Statement s1 = conn.createStatement();
	 System.out.println("createStatement ok");

	 // the next statement executes our CREATE TABLE statement
	 s1.executeUpdate(table);

      }
      catch (Exception ee) {
	 System.out.println("ee96: " + ee);
      }

      // insert three tuples into Books
      System.out.println("Insert three tuples into Books");
      try {
	 Statement s2 = conn.createStatement();
	 s2.executeUpdate("INSERT INTO Books VALUES(1,"
	    + "'Database Systems','Ullman')");
	 s2.executeUpdate("INSERT INTO Books VALUES(2,"
	    + "'Artificial Intelligence', 'Russel, Norvig')");   
	 s2.executeUpdate("INSERT INTO Books VALUES(3,"
	    + "'Problem Solving in C', 'Hanly, Koffman')");   

      }
      catch (Exception ee) {
	 System.out.println("ee112: " + ee);
      }

      // now we're going to print the tuples in the table Books
      try {
	 Statement s3 = conn.createStatement();       
	 ResultSet result = s3.executeQuery("SELECT Title, Author FROM Books");
	 System.out.println("The tuples we just inserted:");
	 boolean f = result.next(); 
	 while (f) {
	    String s = result.getString(1);
	    String a = result.getString(2);
	    System.out.println(s+", "+ a);
	    f=result.next();
	 } 
      }
      catch (Exception ee) {
	 System.out.println("ee129: " + ee);
      }
       
      // now to insert more tuples, this time with a PreparedStatement
      try {  
	 // set up the PreparedStatement in psText
	 String psText = "INSERT INTO Books VALUES(?,?,?)";
	 PreparedStatement ps = conn.prepareStatement(psText); 

	 // prepare the parameter values
	 // put integer value 4 into position 1
	 ps.setInt(1, 4);
	 // put string value "A Guide to LaTeX" into position 2
	 ps.setString(2, "A Guide to LaTeX");
	 // put string value "Kopka, Daly" into position 3
	 ps.setString(3, "Kopka, Daly");

	 // actually do the INSERT
	 ps.executeUpdate();
      }
      catch (Exception e03) {
	 System.out.println("e03: " + e03);
      }

      // Now let's see what the table's tuples are -- we'll print them
      try {
	 Statement s4 = conn.createStatement();
	 ResultSet result = s4.executeQuery("SELECT Title, Author FROM Books");
	 boolean f = result.next(); 
	 System.out.println("\nThe full set of tuples we inserted:");
	 while (f) {
	    String s = result.getString(1);
	    String a = result.getString(2);
	    System.out.println(s+", "+ a);
	    f=result.next();
	 }

	 // guess what -- we don't need this table after all
	 s4.executeUpdate("DROP TABLE Books");
      }
      catch (Exception ee) {
	 System.out.println("ee170: " + ee);
      }
       
      // we don't need this connection either
      try {
	 conn.close();
      }
      catch (Exception ex) {
	 System.out.println("ex177: Unable to close connection");
      };
       
   } // end main

} // end Class JDBCTestMysql1
