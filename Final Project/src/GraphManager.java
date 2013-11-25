import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import processing.core.PApplet;

import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
/**Creates a graph and layout using the JUNg framework
 * */
public class GraphManager {
	 public Graph graph;
	 public SpringLayout<Integer,Edge> layout;
	 public ArrayList<Node> nodes;
     public ArrayList<ArrayList<Edge>> edges;
     public ArrayList<ArrayList<Integer>> nodesWithEdges;
     PApplet parent;
     public int numTimeSlices;
     
     /**Creates a new graph manager which generates or parses and saves the data
      * necessary for drawing the dynamic graph
      * @param p processing applet for drawing the graph elements 
      * @param saveData, true if data generated by the JUNG layout should be saved in a text file    
      * */
     public GraphManager(PApplet p,boolean saveData){
    	 this.parent = p;      	
         readVanDeBunt();
         generateGraphs();
         if (saveData){
        	//saveGraphData("savedData.txt"); //Doesn't work
         }    	   		   	 
     }
     
     /**Uses the JUNG to generate a set of layouts for the graph at each time slice      
      * */
     public void generateGraphs(){
    	 ArrayList <Edge> allEdges;
    	 ArrayList <Integer> allNodes;
    	 for (int i=0;i<this.numTimeSlices;i++){
    		 this.graph = new UndirectedSparseGraph<Integer,Edge>();  
    		//Add the edges for the specific time slice
    		 allEdges = this.edges.get(i);
    		 if (allEdges!=null){
    			 for (int j=0;j<allEdges.size();j++){    				    	 
    	        	 this.graph.addEdge(j,allEdges.get(j).node1,allEdges.get(j).node2);
    			 }
    		 }
    		 //Add the nodes for the time slice
    		 allNodes = this.nodesWithEdges.get(i);
    		 if (allNodes!=null){
    			 for (int j=0;j<allNodes.size();j++){    				    	 
    				 this.graph.addVertex(allNodes.get(j));     
    			 }
    		 }   		
        	
    		 this.layout = new SpringLayout <Integer,Edge>(this.graph);
             this.layout.setSize(new Dimension(700,700));              
             saveNodePositions(i);              
    	 }         
     }
     
     /** Writes each node's position (x,y) for every time slice and the edges
      *  that exist in each time slice to a file.
      *  Might want to save this information because the force directed layout is random
      *  The format of the file is:
      *  node number
      *  (x,y)   
      *  ... last time slice ..for each node
      *  time #
      *  node1 node2
      *  ... all edges...for each time slice
      * */
     //TODO: get the file writing to work, for now just printing to console
     public void saveGraphData(String fileName){			
		     PrintWriter outfile = null;		  
			 Coordinate currentNodeCoord;
			 Edge currentEdge;
			try
			{				
			    outfile = new PrintWriter(new FileWriter (fileName));				    
				//Write all nodes to the file
				for (int i=0;i<this.nodes.size();i++){	
					//outfile.println("node "+i);
					System.out.println("node "+i);
					for (int j=0;j<this.nodes.get(i).coords.size();j++){
						currentNodeCoord = this.nodes.get(i).coords.get(j);
						if (currentNodeCoord!=null){							
							//outfile.println(currentNodeCoord.x+" "+currentNodeCoord.y);
							System.out.println(currentNodeCoord.x+" "+currentNodeCoord.y);
						}else{
							//outfile.println("null");
							System.out.println("null");
						}						
					}
									
				}	
				//Write all edges to the file
				for (int i=0;i<this.numTimeSlices;i++){
					//outfile.println("time "+i);
					System.out.println("time "+i);
					for (int j=0;j<this.edges.get(i).size();j++){
						currentEdge = this.edges.get(i).get(j);
						//outfile.println(currentEdge.node1+" "+currentEdge.node2);
						System.out.println(currentEdge.node1+" "+currentEdge.node2);
					}
				}
				
				outfile.close();				
		}catch (IOException e){
			System.out.println("File not found");
		}
     }
     
     /**Saves the node positions generated by a graph layout in a class
      * variable, does not save the coordinates to a file
      * */
     public void saveNodePositions(int view){
	   	  Node currentNode;
	   	  for (int i=0;i<this.nodes.size();i++){
	   		  currentNode = this.nodes.get(i);	   		  
	   		  if (this.nodesWithEdges.get(view).contains(i)){	   	   			
		   	      currentNode.coords.add(new Coordinate((float) this.layout.getX(i),(float) this.layout.getY(i)));			       		  	
	   		  }else{	   			
	   			  currentNode.coords.add(null);
	   		  }
	   		 this.nodes.set(i, currentNode);	   		    		  
	   	  }   	  
     }
     /**Calls the display function for all nodes and edges, which will
      * render them onto the screen for a certain view
      * */
     public void drawGraph(int view){    	   	  
    	
		 for (int row = 0;row<this.edges.get(view).size();row++){ 			
  	          this.edges.get(view).get(row).display(this.nodes,view);    	  	    	        	   	
  	     }	 
		 
		 for (int i = 0;i<this.nodes.size();i++){
  		   this.nodes.get(i).display(view);    	
  	     }  
     }
     /**Reads the text file containing time-varying data of undergraduate student's friendship
      * Originally used in an experiment by Van De Bunt (1999)
      * */
     public void readVanDeBunt(){
    	  String filename = "vanDeBunt_all.txt";
       	  Scanner scan;
       	  int time = 0;
       	  int nodeCounter = 0;      	  
       	 
       	  this.nodes = new ArrayList<Node>();
       	  this.edges = new ArrayList <ArrayList<Edge>>();
       	  this.nodesWithEdges = new ArrayList <ArrayList<Integer>>();
       	  
       	  try {
       			scan = new Scanner(new File(filename));
       			while(scan.hasNext())
       			{   				
       				String line;
       				line = scan.nextLine();
       				String[] items = line.split(" ");
       				if (items[0].equals("time")){
       					time = Integer.parseInt(items[1]);
       					nodeCounter = 0;  
       					this.edges.add(new ArrayList <Edge>());
       				}else{
       					if (time==0){ //The first time stamp, create all 32 nodes        						
       						this.nodes.add(new Node(this.parent,nodeCounter,""+nodeCounter));	       						  						
       					}
       					//Find the edges for each time stamp					
       					parseLine_vanDeBunt(nodeCounter,time,items);       					
       					nodeCounter++;       					
       				}				
       			}	       			
       		} catch (FileNotFoundException e) {			
       			e.printStackTrace();
       		}  
       	  this.numTimeSlices = time;        	
     }
     /**Parses one line of the van de bunt data set and sets the edges
      * */
    public void parseLine_vanDeBunt(int nodeNumber, int time, String [] line){
    	Edge newEdge;    	    	
    	this.nodesWithEdges.add(new ArrayList<Integer>());
    	
    	for (int i=0;i<line.length;i++){
    		int relation = Integer.parseInt(line[i]);      		
    		if (i!= nodeNumber  &&(relation == 1 || relation==2)){ //Only consider best friend or friend relations as a connection     			
    			newEdge = new Edge (this.parent,"",i,nodeNumber,0);    			
    			this.edges.get(time).add(newEdge);   
    			if (!this.nodesWithEdges.get(time).contains(i)){
    				this.nodesWithEdges.get(time).add(i);
    			}
    			if (!this.nodesWithEdges.get(time).contains(nodeNumber)){
    				this.nodesWithEdges.get(time).add(nodeNumber);
    			}    			
    		}
    	}     	
    	
    }
	 
}
