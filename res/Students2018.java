// Sample JDBC code illustrating the execution and processing of a 
// query that does not modify  the database. 
// Dr. M. Liu, with modifications by E.E.Buckalew
import java.io.*;
import java.util.*;
import java.sql.*;

class Students2018 {	
   public static void main (String args []){
      Connection conn = null;
      PreparedStatement  stmt = null;
      ResultSet rset = null; 
      String userId = null;
      char[ ] pword = null;   	

      try {

         // Load the mysql JDBC driver
         Class.forName ("com.mysql.jdbc.Driver").newInstance(); 
         System.out.println ("Driver class found and loaded.");   
      }
      catch (Exception ex) {
         System.out.println("Driver not found");
         System.out.println(ex);
      };

      try {
	 // get the userId and password
	 Console console = System.console();
	 userId = console.readLine("Enter your mySQL userId: ");
	 pword = console.readPassword("Enter your mySQL password: ");

  	 // Now make the mySQL connection (userId is both database and user)
  	 // this way you can run it on your own database
  	 conn = DriverManager.getConnection(
  	   "jdbc:mysql://ambari-head.csc.calpoly.edu/" + "STUDENTS" ,
	   userId, new String(pword));

	 System.out.println ("connected.");

	 // Create a Statement- a PreparedStatement object is more
         // appropriate than a Statement object
         String query =          	
	    "SELECT * " +
	    "FROM list S, teachers T " +
	    "WHERE S.Classroom = T.Classroom AND S.Grade=5"; 
         System.out.println("QUERY STRING is:\n" + query + "\n");
         stmt = conn.prepareStatement(query);
         	
         // Exceute the query and obtain the result set 
         rset = stmt.executeQuery();
         	
         // Iterate through the result set and print the attribute values
	 // which we will get by attribute name rather than position
         int i = 1; 
         System.out.println("FirstName LastName  Grade"
	    + "\tClassroom\tFirst    \tLast\tClassroom");
         while (rset.next ()){
            System.out.print (rset.getString ("S.FirstName"));
            System.out.print ("\t" + rset.getString ("S.LastName"));
            System.out.print ("\t   " + rset.getInt ("Grade"));
            System.out.print ("\t" + rset.getInt ("S.Classroom"));
            System.out.print ("\t" + rset.getString ("T.FirstName"));
            System.out.print ("\t" + rset.getString ("T.LastName"));
            System.out.print ("\t" + rset.getInt ("T.classroom"));
            System.out.println( );
            i++;
         } //end while
         rset.close( );
      } //end try
      catch (Exception ex){
  	     ex.printStackTrace( );
      }
      finally {
      	 try {
             stmt.close( );
             conn.close( ); 
         }
         catch (Exception ex) {
   	        ex.printStackTrace( );    
   	 }    	
      }    	
  
   } //end main
   
} //end class
