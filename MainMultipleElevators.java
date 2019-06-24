// this will create 2 elevators
import java.util.*; 
import java.lang.*; 
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.*; 
import java.util.Random;
public class MainMultipleElevators{
	
	public  static void main(String[] args) throws InterruptedException{
	  
	  
	  // create two elevators 
	  ElevatorController elevatorController1 = new ElevatorController(); 
	  ElevatorController elevatorController2 = new ElevatorController(); 
	  Elevator elevator1 = new Elevator(elevatorController1);
	  Elevator elevator2 = new Elevator(elevatorController2);

	  


	  
	  
	  // create thread pool 
	  ExecutorService executor = Executors.newFixedThreadPool(100);

	 
	  // create 100 random people
	  Person[] people = new Person[100];  
	  for (int i=0; i<50; i++){
		  people[i] = new Person(elevatorController1);

	  }
	  
	  
	  for (int i=50; i<100; i++){
		  people[i] = new Person(elevatorController2);

	  }
	  for(int i = 0; i<100; i++){
		  executor.submit(people[i]);
	  }
	
	
	
	  // start the elevators and wait for request
	  elevator1.start(); 
	  elevator2.start();


	  executor.shutdown();
	  executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
		

	}
	
}








