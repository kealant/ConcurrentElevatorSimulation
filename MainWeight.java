// this changes the person weight to show an overloaded elevator 
import java.util.*; 
import java.lang.*; 
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.*; 
import java.util.Random;
public class MainWeight{
	
	public  static void main(String[] args) throws InterruptedException{
		
	  // create elevator 
	  ElevatorController elevatorController = new ElevatorController(); 
	  Elevator elevator = new Elevator(elevatorController);
	  


	  
	  
	  // create thread pool 
	  ExecutorService executor = Executors.newFixedThreadPool(5);

	 
	  // create 100 random people
	  Person[] people = new Person[5];  
	  for (int i=0; i<5; i++){
		  people[i] = new Person(elevatorController);
		  // this will change the persons weight to show an overloaded elevator 
		  people[i].setWeight(300); 
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




