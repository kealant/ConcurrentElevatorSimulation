// this will stimulate a fault occuring after a random time within the first 10 seconds
// this will start a backup elevator and remove the people currently in  the elevator to a backup elevator 
import java.util.*; 
import java.lang.*; 
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.*; 
import java.util.Random;
public class MainFault{
	
	public  static void main(String[] args) throws InterruptedException{
		
	  ElevatorController elevatorController = new ElevatorController(); 
	  
	  // if the boolean true is added to the constructor a fault will be created
	  Elevator elevator = new Elevator(elevatorController, true);
	  
	  // start the elevator and wait for request
	  elevator.start(); 

	  
	  
	  // create thread pool 
	  ExecutorService executor = Executors.newFixedThreadPool(20);

	 
	  // create 20 random people to show the transfer of people from faulty elevator to backup elevator
	  Person[] people = new Person[20];  
	  for (int i=0; i<20; i++){
		  people[i] = new Person(elevatorController);
	  }
	  
	  for (Person p: people){
		  executor.submit(p); 
	  }


	  executor.shutdown();
	  executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
		

	}
	
}








