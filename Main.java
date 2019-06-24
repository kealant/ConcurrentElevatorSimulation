// this will create 100 random people
import java.util.*; 
import java.lang.*; 
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.*; 
import java.util.Random;
public class Main{
	
	public  static void main(String[] args) throws InterruptedException{
		
	  // create elevator 
	  ElevatorController elevatorController = new ElevatorController(); 
	  Elevator elevator = new Elevator(elevatorController);
	  


	  
	  
	  // create thread pool 
	  ExecutorService executor = Executors.newFixedThreadPool(100);

	 
	  // create 100 random people
	  Person[] people = new Person[100];  
	  for (int i=0; i<100; i++){
		  people[i] = new Person(elevatorController);
	  }
	  
	  for (Person p: people){
		  executor.submit(p); 
	  }
	
	
      // start the elevator and wait for request
	  elevator.start(); 
	
      // shutdown executor 
	  executor.shutdown();
	  executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
		

	}
	
}








