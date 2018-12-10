MAIN = InnReservations

InnReservations: 
	javac *.java
	java -cp .:mysql-connector-java-5.1.18-bin.jar InnReservations

run :
	java -cp .:mysql-connector-java-5.1.18-bin.jar InnReservations

clean :
	rm *.class
