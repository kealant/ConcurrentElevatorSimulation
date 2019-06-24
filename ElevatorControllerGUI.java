// GUI 
import java.awt.EventQueue;
import javax.swing.*;
import javax.swing.JFrame;
import java.awt.GridBagLayout;
import java.awt.*;
import javax.swing.*;
// Program
import java.util.*; 
import java.io.*; 
import java.util.concurrent.*;
import  java.util.concurrent.atomic.*; 


public class ElevatorControllerGUI{
    private boolean goingUp; 
    private boolean goingDown;
    private boolean doorsOpen;  
    private  int currentFloor; 
    private int currentTime; 
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
    private grid gui; 
    public ElevatorControllerGUI(){
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
		this.maxWeight = 500; 
		// assign each elevator a number
		this.elevatorNumber = elevatorNumberGenerator.getAndIncrement(); 
		gui = new grid(); 
		gui.display(); 





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
	
	
	// add a person into the in elevator, request or waiting queue in first 
	// this is mainly used when a fault happens 
	// the people who are in the elevator at the time should be put first in queue for next elevator 
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
	
	   	// A person wants to request the elevator, add them into request hashmap 
	public synchronized void makeRequest(Person person){
		addPerson(person,person.getArrivalTime(), request); 

		
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
	

    
    
    // if the persons arrival time is <= current time add them to the waiting queue 
    public  synchronized void acceptRequest(){
	   Person personWaiting = removeAPerson(currentTime, request); 
	   while(personWaiting != null){
		   gui.addPerson(personWaiting); 
		   addPerson(personWaiting,personWaiting.getArrivalFloor(), waiting); 
		   removeAPerson(personWaiting.getArrivalFloor(), request);
		   personWaiting  =   removeAPerson(currentTime, request); 

	   }
			
		
		
		
	}
    
   
    // this will decide which floor the elevator goes to next 
    public synchronized void changeFloor(int minFloor,int maxFloor) throws InterruptedException{
	   
	  // add people to the elevator gui 
      gui.addPeopleToElevator(inElevator, currentFloor); 
	   
	   
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
		 
		 	   if (waiting.isEmpty() && inElevator.isEmpty()){
				String sleepingOutput = String.format("Elevator %d is sleeping on floor %d\n",this.elevatorNumber, this.currentFloor);
		        writeOutput(sleepingOutput); 
				System.out.printf(sleepingOutput); 	 			
			}
		  
		  

		  

	      //change floor on GUI
		  gui.changeFloor(currentFloor);
		  
		  

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
				
				// check if is overloaded 
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
				// remove from the waiting in GUI
				gui.removePerson(personEntering); 
				// print/output persons details
				writeOutput(personEntering.toString()); 
				System.out.println(personEntering); 
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
		 gui.emptyElevator(personLeaving); 
		 // print heading to show people are leaving 
		 if(!(headingPrinted)){
			String exitingElevator = String.format("*******************************\nLetting people out on floor %s...\n",currentFloor); 
			writeOutput(exitingElevator); 
			System.out.print(exitingElevator); 			
			headingPrinted = true; 
	     }
	     
	    // print/output who's leaving elevator
	    writeOutput(personLeaving.toString()); 
        System.out.println(personLeaving); 
        // adjust weight according to the people who are leaving
        this.currentWeight-= personLeaving.getTotalWeight() ;
        // remove person from the elevator GUI
	    personLeaving = removeAPerson(currentFloor,inElevator); 
	  
     	}
		
	  
	   
	 }

   

    
   
   public synchronized void transferPeople(ElevatorController faulty, ElevatorController backUp){
	   // outptut a fault has occured 
	   // print out that another elevator is being dispatched 
	   String faultWarning = "**********FAULT PEOPLE BEING TRANSFERRED TO ANOTHER ELEVATOR***********\n**********Starting backup elevator from floor 1************************\n"; 
	   System.out.println(faultWarning);
	   writeOutput(faultWarning);   
	   
	   
	   
	   
	   // update values
	   
	   backUp.request = faulty.request;
	   backUp.waiting = backUp.waiting;
	   backUp.currentTime = backUp.currentTime  + 1; 
	   
	   // transfer people in elevator to current floor of backup elevator waiting queue 
	   for ( Map.Entry<Integer,ConcurrentLinkedQueue<Person>> entry : inElevator.entrySet()) {
		  for (Person person : entry.getValue()){
			  addPersonFirst(person, currentFloor,backUp.waiting); 
			  System.out.printf("Person %s is exiting on floor %d and waiting for backup elevator\n", person.getId(), currentFloor); 
		  }
	   }

	   
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

	public synchronized int getTime(){
		return this.currentTime;
	}
	
}
	



class grid {

	private JLabel[] elevatorFloors;
	private Thread pause;
	private ConcurrentHashMap<Dimension, JLabel> people;
	private JFrame frame;
	private JLabel elevatorFloor1; 
	private JLabel elevatorFloor2;
	private JLabel elevatorFloor3;
	private JLabel elevatorFloor4;
	private JLabel elevatorFloor5;
	private JLabel elevatorFloor6;
	private JLabel elevatorFloor7;
	private JLabel elevatorFloor8;
	private JLabel elevatorFloor9;
	private JLabel elevatorFloor10;
    private  ConcurrentHashMap<Person,JLabel> assignedLabels;
    private ConcurrentHashMap<Dimension,JLabel> peopleInElevator;
    private ConcurrentHashMap<Integer, ConcurrentLinkedQueue<JLabel>> filledElevatorSpots;
    private int count;
    private Image person;
    private Image img;


	public void display(){
	   frame.setVisible(true);
	}
	



	
	public grid() {
		initialize();
		filledElevatorSpots =  new ConcurrentHashMap<Integer, ConcurrentLinkedQueue<JLabel>>();
		assignedLabels = new ConcurrentHashMap<Person,JLabel>();
	    

	}

	public void initialize() {
		frame = new JFrame("Elevator Simulator");
		frame.setBounds(100, 100, 600, 1000);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 1, 1,1,1,1,1,1,1,1,1,1,1,1,1};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[] {0.0, 0.2,0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1};
		gridBagLayout.rowWeights = new double[]{0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, Double.MIN_VALUE};
		frame.getContentPane().setLayout(gridBagLayout);

		
		int floor = 10;
		for(int i=0;i<10;i++) {
			JLabel tmpLabel = new JLabel("" + floor);
			GridBagConstraints tmp = new GridBagConstraints();
			tmp.insets = new Insets(0, 0, 5, 5);
			tmp.gridx = 0;
			tmp.gridy = i;
			frame.getContentPane().add(tmpLabel, tmp);
			floor--;

		}
		
	    person = new ImageIcon(this.getClass().getResource("greenPersonSmall.png")).getImage();
		Image img = new ImageIcon(this.getClass().getResource("/elevator.jpg")).getImage();


	    elevatorFloor10 = new JLabel();
	    elevatorFloor10.setLayout(new GridLayout(2,5));
		GridBagConstraints gbc_elevatorFloor10 = new GridBagConstraints();
		gbc_elevatorFloor10.insets = new Insets(0, 0, 5, 5);
		gbc_elevatorFloor10.gridx = 1;
		gbc_elevatorFloor10.gridy = 0;
		elevatorFloor10.setIcon(new ImageIcon(img));
		frame.getContentPane().add(elevatorFloor10, gbc_elevatorFloor10);
		
		
		elevatorFloor9 = new JLabel();
		elevatorFloor9.setLayout(new GridLayout(2,5));
		GridBagConstraints gbc_elevatorFloor9 = new GridBagConstraints();
		gbc_elevatorFloor9.insets = new Insets(0, 0, 5, 5);
		gbc_elevatorFloor9.gridx = 1;
		gbc_elevatorFloor9.gridy = 1;
		elevatorFloor9.setIcon(new ImageIcon(img));
		frame.getContentPane().add(elevatorFloor9, gbc_elevatorFloor9);
		
		


		elevatorFloor8 = new JLabel();
		elevatorFloor8.setLayout(new GridLayout(2,5));
		GridBagConstraints gbc_elevatorFloor8 = new GridBagConstraints();
		gbc_elevatorFloor8.insets = new Insets(0, 0, 5, 5);
		gbc_elevatorFloor8.gridx = 1;
		gbc_elevatorFloor8.gridy = 2;
		elevatorFloor8.setIcon(new ImageIcon(img));
		frame.getContentPane().add(elevatorFloor8, gbc_elevatorFloor8);





		
	    elevatorFloor7 = new JLabel();
	    elevatorFloor7.setLayout(new GridLayout(2,5));
		GridBagConstraints gbc_elevatorFloor7 = new GridBagConstraints();
		gbc_elevatorFloor7.insets = new Insets(0, 0, 5, 5);
		gbc_elevatorFloor7.gridx = 1;
		gbc_elevatorFloor7.gridy = 3;
		elevatorFloor7.setIcon(new ImageIcon(img));
		frame.getContentPane().add(elevatorFloor7, gbc_elevatorFloor7);
		
		elevatorFloor6 = new JLabel();
		elevatorFloor6.setLayout(new GridLayout(2,5));
		GridBagConstraints gbc_elevatorFloor6 = new GridBagConstraints();
		gbc_elevatorFloor6.insets = new Insets(0, 0, 5, 5);
		gbc_elevatorFloor6.gridx = 1;
		gbc_elevatorFloor6.gridy = 4;
		elevatorFloor6.setIcon(new ImageIcon(img));
		frame.getContentPane().add(elevatorFloor6, gbc_elevatorFloor6);
		
		elevatorFloor5 = new JLabel();
		elevatorFloor5.setLayout(new GridLayout(2,5));
		GridBagConstraints gbc_elevatorFloor5 = new GridBagConstraints();
		gbc_elevatorFloor5.insets = new Insets(0, 0, 5, 5);
		gbc_elevatorFloor5.gridx = 1;
		gbc_elevatorFloor5.gridy = 5;
		elevatorFloor5.setIcon(new ImageIcon(img));
		frame.getContentPane().add(elevatorFloor5, gbc_elevatorFloor5);
		
		elevatorFloor4 = new JLabel();
		elevatorFloor4.setLayout(new GridLayout(2,5));
		GridBagConstraints gbc_elevatorFloor4 = new GridBagConstraints();
		gbc_elevatorFloor4.insets = new Insets(0, 0, 5, 5);
		gbc_elevatorFloor4.gridx = 1;
		gbc_elevatorFloor4.gridy = 6;
		elevatorFloor4.setIcon(new ImageIcon(img));
		frame.getContentPane().add(elevatorFloor4, gbc_elevatorFloor4);
		
		elevatorFloor3 = new JLabel();
		elevatorFloor3.setLayout(new GridLayout(2,5));
		GridBagConstraints gbc_elevatorFloor3 = new GridBagConstraints();
		gbc_elevatorFloor3.insets = new Insets(0, 0, 5, 5);
		gbc_elevatorFloor3.gridx = 1;
		gbc_elevatorFloor3.gridy = 7;
		elevatorFloor3.setIcon(new ImageIcon(img));
		frame.getContentPane().add(elevatorFloor3, gbc_elevatorFloor3);
		
		elevatorFloor2 = new JLabel();
		elevatorFloor2.setLayout(new GridLayout(2,5));
		GridBagConstraints gbc_elevatorFloor2 = new GridBagConstraints();
		gbc_elevatorFloor2.insets = new Insets(0, 0, 5, 5);
		gbc_elevatorFloor2.gridx = 1;
		gbc_elevatorFloor2.gridy = 8;
		elevatorFloor2.setIcon(new ImageIcon(img));
		frame.getContentPane().add(elevatorFloor2, gbc_elevatorFloor2);
		
		elevatorFloor1 = new JLabel();
		elevatorFloor1.setLayout(new GridLayout(2,5));
		GridBagConstraints gbc_elevatorFloor1 = new GridBagConstraints();
		gbc_elevatorFloor1.insets = new Insets(0, 0, 5, 5);
		gbc_elevatorFloor1.gridx = 1;
		gbc_elevatorFloor1.gridy = 9;
		elevatorFloor1.setIcon(new ImageIcon(img));
		frame.getContentPane().add(elevatorFloor1, gbc_elevatorFloor1);
		
		
		 
		

	    elevatorFloors = new JLabel[] {elevatorFloor1, elevatorFloor2,elevatorFloor3,elevatorFloor4,elevatorFloor5,elevatorFloor6,elevatorFloor7,elevatorFloor8,elevatorFloor9,elevatorFloor10};
	    
		
	

		

	    // create all the people
		people = new ConcurrentHashMap<Dimension, JLabel>();
		for (int x = 2;x<10;x++) {
			
			for (int y =0;y<=10;y++) {
				
				JLabel tmpLabel = new JLabel();
				GridBagConstraints tmp = new GridBagConstraints();
				tmp.insets = new Insets(0, 0, 5, 5);
				tmp.gridx = x;
				tmp.gridy = y;
				tmpLabel.setIcon(new ImageIcon(person));
				people.put(new Dimension(x, y),tmpLabel);
				tmpLabel.setVisible(false);
 				frame.getContentPane().add(tmpLabel, tmp);
			}
		}


       
       
        // hide all elevator floors initially
	 	for (JLabel l : elevatorFloors) {
	 		l.setVisible(false);
	 	}
	 	
	 	// set elevator visible at bottom floor 
	 	elevatorFloors[0].setVisible(true); 
	 
		
		
		
		




	}
	
	
	public int getCurrentFloor(){
		int currentFloor = 0; 
		for(int i = 0;i<=9;i++) {
		if(elevatorFloors[i].isVisible()) {
						currentFloor = i;
						break; 
							    }
						}
		return currentFloor; 
	}


	
    
	public synchronized void changeFloor(int floorNumber) {


				 new Thread(new Runnable() {
						 
						 public void run() {
							 try {
								int currentFloor = 0; 
								for(int i = 0;i<=9;i++) {
									if(elevatorFloors[i].isVisible()) {
										currentFloor = i;
										break; 
							    	}
								}

								if(currentFloor > floorNumber) {
								   for(int j=currentFloor;j>=floorNumber;j--) {
										 elevatorFloors[j].setVisible(true);
										 Thread.sleep(1000);
										 elevatorFloors[j].setVisible(false);
										 elevatorFloors[j-1].setVisible(true);

								 }
							 
								}
								else {

										for(int x=currentFloor;x<floorNumber-1;x++) {
											 elevatorFloors[x].setVisible(true);
											 Thread.sleep(1000);
											 elevatorFloors[x].setVisible(false);
											 elevatorFloors[x+1].setVisible(true);

											}
									}
							    
								}
							 
								

							 catch(InterruptedException e) {
								 
							 }
							 
						 }
					 }).start(); 				 
			}
	


 
	
	

	
	public synchronized void removePersonFromElevator(Person person){
		for(int i = 0; i<6;i++){
			JLabel tmp = peopleInElevator.get(new Dimension(person.getDestinationFloor(), i));
			String id  = String.valueOf(person.getId());
			if (tmp.getText().equals(id)){
				tmp.setVisible(false); 
			}

			
		}
	}
  

	public synchronized void emptyElevator(Person person){
				ConcurrentLinkedQueue<JLabel> tmp = new ConcurrentLinkedQueue<JLabel>(); 
				int id = (int)person.getId();
				if (filledElevatorSpots.containsKey(id)){
					tmp = filledElevatorSpots.get((int)person.getId());
					for(JLabel jlabel :tmp){
							jlabel.setVisible(false); 

						
					}
				}
			
			
			
		}
		
		
		public synchronized void addPeopleToElevator(ConcurrentHashMap<Integer,ConcurrentLinkedQueue<Person>> currentlyInElevator, int currentFloor){
		    ConcurrentLinkedQueue<Person> tmp = new ConcurrentLinkedQueue<Person>(); 
			for (Integer key: currentlyInElevator.keySet()) {
                 tmp = currentlyInElevator.get(key);
                 for(Person person: tmp){
					  int id = (int)person.getId();
					  JLabel over = new JLabel();
					  over.setIcon(new ImageIcon(new ImageIcon(this.getClass().getResource("greenPersonSmall.png")).getImage()));
					  over.setText(""+id); 
					  over.setVisible(true);
					  elevatorFloors[currentFloor].add(over);
					  ConcurrentLinkedQueue<JLabel> tmpLabels = new ConcurrentLinkedQueue<JLabel>(); 
         			   if (filledElevatorSpots.containsKey(id)){
							tmpLabels = filledElevatorSpots.get(id);
							tmpLabels.add(over);
							filledElevatorSpots.put(id, tmpLabels); 
						}
					  else{
						tmpLabels.add(over); 
						filledElevatorSpots.put(id, tmpLabels); 
						}
			
		
				 }
				}
			
		}
	
		
		
		
		
	
	 
		
		
	
	public synchronized void addPerson(Person person) {
		int floor = 10 -person.getArrivalFloor();
		String id  = String.valueOf(person.getId());
		JLabel tmpLabel; 
		// max floor is (9,9) 
		for(int y = 2; y<=10;y++){
			tmpLabel = people.get(new Dimension(y,floor));
			if (!(tmpLabel.isVisible())){
				tmpLabel.setVisible(true); 
				tmpLabel.setText(id); 
				assignedLabels.put(person, tmpLabel); 
				break; 
			}
		}
	}
	
	
	public synchronized void removePerson(Person person){
	    	


	
				 new Thread(new Runnable() {
						 
						 public void run() {
							 try{
								 Thread.sleep(1000); 
								 JLabel assignedLabel = assignedLabels.get(person);
								 assignedLabel.setVisible(false); 

								}
							 
								

							 catch(InterruptedException e) {
								 
							 }
							 
						 }
					 }).start(); 			
	
	}
		

	}
	



