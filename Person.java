import java.util.*;
import java.io.*; 
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Person extends Thread{
	private static final AtomicInteger idGenerator = new AtomicInteger(1);   
    private int arrivalTime;
	private int id; 
    public int arrivalFloor;
    public int destinationFloor;
    private int baggageWeight;
    private int personWeight;
    Random random = new Random();
    private ElevatorController elevatorController; 
    private ElevatorControllerGUI elevatorControllerGUI; 


    // constructor for the elevator without GUI 
    public Person(ElevatorController elevatorController){
        this.id =  idGenerator.getAndIncrement();
         this.arrivalTime = setArrivalTime();
         this.arrivalFloor =  setArrivalFloor();
		 this.destinationFloor = setDestinationFloor();
         this.baggageWeight = setLuggageWeight();
         this.personWeight = setPassengerWeight();
         this.random = new Random();
         this.elevatorController = elevatorController;
    }
    
    // constructor for the elevator with GUI 
    public Person(ElevatorControllerGUI elevatorController){
         this.id =  idGenerator.getAndIncrement();
         this.arrivalTime = random.nextInt((30-1)+1)+1; 
         this.arrivalFloor =   random.nextInt((4-2)+1)+2; 
		 this.destinationFloor =  random.nextInt((10-5)+1)+5;      
		 this.baggageWeight = setLuggageWeight();
         this.personWeight = setPassengerWeight();
         this.random = new Random();
         this.elevatorControllerGUI = elevatorController;
    }



    public synchronized int setPassengerWeight(){
		// weight between 40 - 100 
        return random.nextInt((100-40)+1)+40;
    }

    public synchronized int setArrivalFloor(){
		//  random arrival floor
        return  random.nextInt((10-1)+1)+0;
    }
    
    public synchronized int setDestinationFloor(){
		// ensure arrival floor != destination floor
		int randomFloor = random.nextInt((10-1)+1)+0;
		while(this.arrivalFloor == randomFloor){
			randomFloor = random.nextInt((10-1)+1)+0;
		}
		
		return randomFloor; 
	}
	
	

	// this is used in the MainWeight.java to demo an overweight elevator 
	// each person is set to 200 meaning > 3 people will overload the elevator 
	
	public synchronized void setWeight(int weight){
		this.personWeight = weight; 
	}	
	
	
	public synchronized int setArrivalTime(){
		 // random time between 0-30
		 return random.nextInt((30-1)+1)+1;

	}

    public synchronized int setLuggageWeight(){
		// 0 - 30 baggage weight
        return random.nextInt((30-0)+1)+0;
    }
    
    public synchronized int getTotalWeight(){
		return  this.baggageWeight + this.personWeight;
	}

    public synchronized long getId(){
		return this.id;
	}

    public synchronized int getDestinationFloor(){
		return  this.destinationFloor;
	}
	
	
	 public synchronized int getArrivalFloor(){
		return  this.arrivalFloor;
	}
	
	    public synchronized int getArrivalTime(){
		return  this.arrivalTime;
	}

    public synchronized String toString(){
		// passengers details 
		String personalDetails = String.format("id: %s, with weight %d arrivalFloor: %s, destinationFloor: %s and arrivalTime: %s\n",this.id, this.getTotalWeight(), this.arrivalFloor, this.destinationFloor, this.arrivalTime);
		return personalDetails; 
		
		
	}
	

	
   // write to output.dat
   public synchronized void writeOutput(){
		String outputStr = String.format("Person (Thread ID) %s makes request at time %s starting at floor %s with the destination floor %s.\n", this.id, this.arrivalTime, this.arrivalFloor, this.destinationFloor); 

		

    	  
    	  try{
			  
			 File file =new File("request.dat");
    	     if(!file.exists()){
    	 	     file.createNewFile();
    	    }
			  FileWriter fileWritter = new FileWriter(file.getName(),true);
			  BufferedWriter bw = new BufferedWriter(fileWritter);
              bw.write(outputStr);
              bw.close();
    	  
    	  }
    	 catch(IOException e){
    	   System.out.println("Exception occurred:");
    	   e.printStackTrace();
      }
		
	}
    

    public void run(){
        try{
			// write to output
			writeOutput();
			// request an elevator  
            elevatorController.makeRequest(this);
        }
        catch(Exception e1){
			
			try{
				writeOutput();
				elevatorControllerGUI.makeRequest(this);
		    }
		    
		    catch(Exception e2){
				e2.printStackTrace(); 
			}


        }

    }

}



