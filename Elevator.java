import java.util.concurrent.*;
import java.util.*; 
import java.util.concurrent.atomic.*; 
import java.io.*; 



public class Elevator extends Thread{
   private int maxFloor;
   private int minFloor;
   private int faultTime;
   private int elevatorNumber; 
   private ElevatorController elevatorController; 
   private ElevatorControllerGUI elevatorControllerGUI; 
   private Elevator backUpElevator; 
   private boolean createFault; 
   private volatile AtomicBoolean faulty;
   Random random = new Random(); 
   
   // normal constructor 
   public Elevator(ElevatorController elevatorController){
      this.elevatorController = elevatorController; 
      this.maxFloor = 10;
      this.minFloor = 0; 
      this.faulty =  new AtomicBoolean(false);
   }
   
   // for an elevator with a gui 
   public Elevator(ElevatorControllerGUI elevatorController){
      this.elevatorControllerGUI = elevatorController; 
      this.maxFloor = 10;
      this.minFloor = 1; 
      this.faulty =  new AtomicBoolean(false);
   }
   
   
   // this is the constructor if you wish to create a fault
   // create fault must be set to true 
   public Elevator(ElevatorController elevatorController, boolean createFault){
      this.elevatorController = elevatorController; 
      this.maxFloor = 10;
      this.minFloor = 1; 
      this.faultTime = random.nextInt((10-5)+1)+5;
      this.createFault = createFault; 
      this.faulty =  new AtomicBoolean(false);
   } 
   
   
 
   
   
   public void run(){
   try{
	   // write that an elevator has started 
	   writeOutput(); 
	   
	   
			while(!(faulty.get())){
				{
		    
				// control the elevators actions 
				elevatorController.acceptRequest(); 
				elevatorController.enterElevator(); 	
				elevatorController.exitElevator(); 	
				elevatorController.changeFloor(minFloor,maxFloor);
				// create a random fault, this will create one after a random time between 0 - 10 if createFault = true 
				randomFault(); 
				// sleep for 100 ticks between floors
				Thread.sleep(100); 
			}
		  }
		  
	  }
		  
		  
		  
		  
	 catch(Exception e){
		 
		 try{
			 	// write that an elevator has started 
				writeOutput(); 
				
				// if a GUI elevator has been created 
				while(!(faulty.get())){
							{
		    
							// control the elevators actions 
							elevatorControllerGUI.acceptRequest(); 
							elevatorControllerGUI.enterElevator(); 	
							elevatorControllerGUI.exitElevator(); 	
							elevatorControllerGUI.changeFloor(minFloor,maxFloor);
							// sleep for 2000 ticks between floors to show movement within GUI 
							Thread.sleep(2000); 
							}
			

				}
			}
			 catch(Exception e2){
				e.printStackTrace(); 
				e2.printStackTrace(); 
			}
		 

		 
	 }
	 

   }




   	public synchronized void writeOutput(){
		// this will output when a new elevator has started 
		String headerStars = "*********************************************************************************************";					
		String headText = " 						The elevator has started					"; 		
		String heading = String.format("%s\n%s\n%s\n", headerStars, headText,headerStars);

    	  
    	  try{
			  
			 // write to output.dat
			 File file =new File("output.dat");
    	     if(!file.exists()){
    	 	     file.createNewFile();
    	    }
			  FileWriter fileWritter = new FileWriter(file.getName(),true);
			  BufferedWriter bw = new BufferedWriter(fileWritter);
              bw.write(heading);
              bw.close();
    	  
    	  }
    	 catch(IOException e){
    	   System.out.println("Exception occurred:");
    	   e.printStackTrace();
      }
		
	}
	

   // create a random fault
   public synchronized void randomFault() {
	   
	   
	   if(elevatorController.getTime() == faultTime && createFault == true){
				// create a backup elevator 
				ElevatorController backUpElevatorController = new ElevatorController(); 
				elevatorController.transferPeople(elevatorController, backUpElevatorController); 
				backUpElevator = new Elevator(backUpElevatorController);
				backUpElevator.start(); 
				stopCurrentThread(); 
				
		}


	  
	   
   }
   
   
   // create a faulty elevator 
   public synchronized void stopCurrentThread(){
	    this.faulty.set(true); 
   }

   public synchronized void sleepWhileEmpty() throws InterruptedException{
            
            synchronized(this){
				
				while(elevatorController.request.isEmpty()){
					wait(); 
				}
				
				notifyAll(); 
			}

   }

}
