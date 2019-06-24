// this will create 2 elevators
import java.util.*; 
import java.lang.*; 
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.*; 
import java.util.Random;
public class MainGUI{
	
	public  static void main(String[] args) throws InterruptedException{
		
	  ElevatorControllerGUI elevatorController = new ElevatorControllerGUI(); 
	  Elevator elevator = new Elevator(elevatorController);
	  
	  // start the elevator and wait for request
	  elevator.start(); 
	  
	  
	  // create thread pool 
	  ExecutorService executor = Executors.newFixedThreadPool(5);

	 
	  // create random people
	  Person[] people = new Person[5];  
	  for (int i=0; i<5; i++){
		  people[i] = new Person(elevatorController);
	  }
	  
	  
	  
	 for(Person p: people){
		 executor.submit(p); 
	 }



	  executor.shutdown();
	  executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);


	}
	
}








