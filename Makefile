MAIN = InnReservations

InnReservations: 
	javac *.java
	java -cp .:mysql-connector-java-5.1.18-bin.jar InnReservations

handin:

clean :
	rm *.class
