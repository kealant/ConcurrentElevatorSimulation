import java.util.concurrent.*;
import  java.util.concurrent.atomic.*; 
import java.util.*; 
import java.io.*; 


public class ElevatorController{
    public boolean goingUp; 
    public boolean goingDown;
    private boolean doorsOpen;  
    private int currentFloor; 
    public int currentTime; 
    private int numPeople; 
    private int currentWeight; 
    private int maxWeight; 
    private static final AtomicInteger elevatorNumberGenerator = new AtomicInteger(1);  
    private int elevatorNumber;
    private Elevator elevator; 
    // a person will be added to the waiting queue when we the current time has reached there arrival time 
    public ConcurrentHashMap<Integer,ConcurrentLinkedQueue<Person>> waiting; 
    // all the people who will want to use the elevator 
    public ConcurrentHashMap<Integer,ConcurrentLinkedQueue<Person>> request; 
    public ConcurrentHashMap<Integer,ConcurrentLinkedQueue<Person>> inElevator;   
    // the request hashmap maps the request to the floor they are at 
    // the person making the request will join the linked queue for that floor 
    // (hashmap) {floor 0 -> linkedQueue{0 : [person1 - > person 2 -> 3}
    
    public ElevatorController(){
		// decide which direction the elevator is going 
		this.goingUp = true; 
		this.goingDown = false; 
		this.doorsOpen = false; 
		// all the request the elevator must handle 
		this.request = new ConcurrentHashMap<Integer,ConcurrentLinkedQueue<Person>>();
		// all of the people who have arrived and are waiting on a floor
		this.waiting = new ConcurrentHashMap<Integer,ConcurrentLinkedQueue<Person>>();
		// all the people currently in the elevator 
		this.inElevator = new ConcurrentHashMap<Integer,ConcurrentLinkedQueue<Person>>(); 
		this.currentFloor = 1; 
		// time will go up in every time the elevator goes up a floor 
		this.currentTime = 0; 
		this.currentWeight = 0 ;
		// maximum weight limit 
		this.maxWeight = 500; 
		// assign each elevator a number
		this.elevatorNumber = elevatorNumberGenerator.getAndIncrement(); 







	}
	

	// add a person into the in elevator, request or waiting queue 
	public synchronized void addPerson(Person person, int floor, ConcurrentHashMap<Integer,ConcurrentLinkedQueue<Person>> hashmap){
		ConcurrentLinkedQueue<Person> tmp = new ConcurrentLinkedQueue<Person>(); 
		if (hashmap.containsKey(floor)){
			tmp = hashmap.get(floor);
			tmp.add(person);
			hashmap.put(floor, tmp); 
		}
		else{
			tmp.add(person); 
			hashmap.put(floor, tmp); 
			
		}
		
	}
	

	
    // add a person into the in elevator, request or waiting queue to the tail 
	public synchronized void addPersonFirst(Person person, int floor, ConcurrentHashMap<Integer,ConcurrentLinkedQueue<Person>> hashmap){
		ConcurrentLinkedQueue<Person> oldQueue = new ConcurrentLinkedQueue<Person>(); 
	    ConcurrentLinkedQueue<Person> newQueue = new ConcurrentLinkedQueue<Person>(); 
		if (hashmap.containsKey(floor)){
			oldQueue = hashmap.get(floor); 
			newQueue.add(person); 
			for (Person p : oldQueue){
				newQueue.add(p);  
			}
			hashmap.put(floor, newQueue); 
		}
		else{
			oldQueue.add(person); 
			hashmap.put(floor, oldQueue); 
			
		}
		
	}
	
	// remove a person from either the inElevator, request or waiting queue 
	public synchronized Person removeAPerson(int floor, ConcurrentHashMap<Integer,ConcurrentLinkedQueue<Person>> hashmap) {
		ConcurrentLinkedQueue<Person> tmp = new ConcurrentLinkedQueue<Person>(); 
		if (hashmap.containsKey(floor)){
			tmp = hashmap.get(floor);
			Person personRemoved = tmp.poll();
			hashmap.put(floor, tmp); 
			if (hashmap.get(floor).isEmpty()){
				hashmap.remove(floor); 
			}
			
			return personRemoved; 

		}
		return null; 


		
	}
	
	// A person wants to request the elevator, add them into request hashmap 
	public synchronized void makeRequest(Person person){
		addPerson(person,person.getArrivalTime(), request); 

		
	}
    
    
    // if the persons arrival time is <= current time add them to the waiting queue 
    public  synchronized void acceptRequest(){
	   Person personWaiting = removeAPerson(currentTime, request); 
	   while(personWaiting != null){
		   addPerson(personWaiting,personWaiting.getArrivalFloor(), waiting); 
		   removeAPerson(personWaiting.getArrivalFloor(), request);
		   personWaiting  =   removeAPerson(currentTime, request); 
	   }
			
		
		
		
	}
    
   
    // this will decide which floor the elevator goes to next 
    public synchronized void changeFloor(int minFloor,int maxFloor) throws InterruptedException{
	    
	    
     // Example for below - this will tell which direction the elevator should go 
     // Current floor -> 5
     // InElevator Keys -> 1,2,3
     // these keys are the peoples destination  floor
     // max = 3 
     // since max is less then current floor the elevator must go down 

	 if(!(inElevator.isEmpty())){
       int max = Collections.max(inElevator.keySet());
       if(max < currentFloor){
		   this.goingUp = false;
		   this.goingDown = true;                   
	   }
	   else{
		   this.goingUp = true;
		   this.goingDown = false;  
	   }
       
       }
       
       else{

	      // if there is no one in the elevator and its going up
	      // and there are no people waiting on the upper floors
	      // go down 
	      if(!(waiting.isEmpty()) && goingUp == true){
			 int max = Collections.max(waiting.keySet());
			 if (max < currentFloor){
				this.goingUp = false;
				this.goingDown = true; 
			 }
		  }

	   }
    
    

	  // if there are people waiting or in the elevator 
      if(!(waiting.isEmpty()) || !(inElevator.isEmpty()) ){
		  
		 String elevatorFloorString =  String.format("Elevator %d is on floor %s\n",this.elevatorNumber,this.currentFloor);
		 
		 // write to output.dat 
		 writeOutput(elevatorFloorString); 
		 
		 // print what floor the elevator is on 
	    System.out.printf(elevatorFloorString); 	 
	    
	    // check if the next floor is max or min then decide direction   
		if (goingUp == true){
			   
			   if(this.currentFloor == maxFloor){
				    currentFloor--;	   
				    goingUp = false; 
				    goingDown = true; 			    
			   }
		       else{
				   currentFloor++;
				}
	       }
			
		   else{
			   
			   if(currentFloor == minFloor){
					goingUp = true; 
				    goingDown = false; 	
				    currentFloor++;
		   
				 }
				else{
			         currentFloor--;
                 }
			 }  
			 
		}
		
	   //elevator sleeping if no people waiting or in the elevator 
       if (waiting.isEmpty() && inElevator.isEmpty()){
		        String sleepingOutput = String.format("Elevator %d is sleeping on floor %d\n",this.elevatorNumber, this.currentFloor);
		        writeOutput(sleepingOutput); 
				System.out.printf(sleepingOutput); 			
			}
		
		  
		  // update the time
		  this.currentTime++; 	 


    }


    public synchronized void enterElevator() throws InterruptedException{
		
		



	   boolean headingPrinted = false; 
	   // remove a person who's waiting at the current floor
	   Person personEntering = removeAPerson(currentFloor, waiting); 
	   // while there is people waiting on the current floor
       while(personEntering!=null){
		        
		        
				// print heading 
		   	    if (!(headingPrinted)){
					String enteringElevator = String.format("Stopping on floor %d for people\n**************************\nAllowing people in on floor %s...\n",this.currentFloor, this.currentFloor);
					writeOutput(enteringElevator);
					System.out.print(enteringElevator); 
					headingPrinted = true; 
				}
				
				// check if elevator will become overloaded 
				int combinedWeight =  this.currentWeight + personEntering.getTotalWeight();
				
				if (combinedWeight >= maxWeight){
					// if the elevator is over weight 
					// the last person in exits the elevator and is put back at the front of the queue on that floor 
					addPersonFirst(personEntering, currentFloor, waiting); 
					String weightWarning = String.format("**********************************************************\nElevator %d too heavy with combined weight %d and max weight limit %d\nPerson %s is leaving the elevator\n**********************************************************\n", this.elevatorNumber, combinedWeight, maxWeight, personEntering); 
					writeOutput(weightWarning);
					System.out.print(weightWarning); 
					break; 
				}
				
				// add the persons weight to the current weight 
				this.currentWeight += personEntering.getTotalWeight(); 
				// add the person to the in elevator 
				addPerson(personEntering, personEntering.getDestinationFloor(),inElevator);
				// print person's details
				writeOutput(personEntering.toString()); 
				System.out.print(personEntering); 
				// remove another waiting person on the current floor 
		        personEntering = removeAPerson(currentFloor, waiting); 

	   }


		
	}
   
   
   public synchronized void exitElevator() throws InterruptedException{
      
      // remove a person from the current floor who's in the elevator        
	  Person personLeaving = removeAPerson(currentFloor, this.inElevator);   
      boolean headingPrinted = false; 
      // while there are people on this floor remove them from the elevator
      while(personLeaving!=null){
		 // print heading to show people are leaving 
		 if(!(headingPrinted)){
			String exitingElevator = String.format("*******************************\nLetting people out on floor %s...\n",currentFloor); 
			writeOutput(exitingElevator); 
			System.out.print(exitingElevator); 
			headingPrinted = true; 
	     }
	     
	    // print/output who's leaving elevator
	     writeOutput(personLeaving.toString()); 
        System.out.print(personLeaving); 
        // adjust weight according to the people who are leaving
        this.currentWeight-= personLeaving.getTotalWeight() ;
        // remove another person from this floor 
	    personLeaving = removeAPerson(currentFloor,inElevator); 
	  
     	}
		
	  
	   
	 }

   
   
   
   public synchronized void transferPeople(ElevatorController faulty, ElevatorController backUp) {
	   // outptut a fault has occured 
	   // print out that another elevator is being dispatched 
	   String faultWarning = "**********FAULT PEOPLE BEING TRANSFERRED TO ANOTHER ELEVATOR***********\n**********Starting backup elevator from floor 1************************\n"; 
	   System.out.println(faultWarning);
	   writeOutput(faultWarning);   
	   
	   
	   // transfer over current values 
	   
	   backUp.request = faulty.request;
	   backUp.waiting = faulty.waiting;
	   backUp.currentTime = faulty.currentTime ; 
	   
	   // transfer people in elevator to current floor of backup elevator waiting queue 
	   for ( Map.Entry<Integer,ConcurrentLinkedQueue<Person>> entry : inElevator.entrySet()) {
		  for (Person person : entry.getValue()){
			  addPersonFirst(person, currentFloor,backUp.waiting); 
			  String backupWarning = String.format("Person %d is exiting on floor %d and waiting for backup elevator\n", person.getId(), this.currentFloor);
			  writeOutput(backupWarning); 
			  System.out.print(backupWarning); 
		  }
	   }

	   
   }
	 

 

	public synchronized int getTime(){
		return this.currentTime;
	}
	
	
	public synchronized void writeOutput(String outputString) {
		try{
			  
			 // write to output.dat
			 File file =new File("output.dat");
    	     if(!file.exists()){
    	 	     file.createNewFile();
    	    }
			  FileWriter fileWritter = new FileWriter(file.getName(),true);
			  BufferedWriter bw = new BufferedWriter(fileWritter);
              bw.write(outputString);
              bw.close();
    	  
    	  }
    	 catch(IOException e){
    	   System.out.println("Exception occurred:");
    	   e.printStackTrace();
      }
		
	}
	
	

    
}







