import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import processing.core.PApplet;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
/**Creates a graph and layout using the JUNg framework
 * */
public class GraphManager {
	 public Graph graph;
	 public FRLayout<Integer,Edge> layout;
	 public ArrayList<Node> nodes;
     public ArrayList<ArrayList<Edge>> edges;
     public ArrayList<ArrayList<Integer>> nodesWithEdges;
     public PrintWriter output;
     
     PApplet parent;
     public int numTimeSlices;
     
     /**Creates a new graph manager which generates or parses and saves the data
      * necessary for drawing the dynamic graph
      * @param p processing applet for drawing the graph elements 
      * @param saveData, true if data generated by the JUNG layout should be saved in a text file    
      * */
     public GraphManager(PApplet p,boolean saveData){
    	 this.parent = p;      	
         //readVanDeBunt2();
    	 readVanDeBunt();
         //generateGraphs();
         //generateAverageGraph();
    	 //readReutersNetwork();
    	 generateAverageGraph();
         if (saveData){
        	saveGraphData("savedData.txt"); //Doesn't work
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
        	
    		 this.layout = new FRLayout <Integer,Edge>(this.graph);
             //this.layout.setSize(new Dimension(700,700));
    		 this.layout.setSize(new Dimension(1085,485));
             saveNodePositions(i);              
    	 }         
     }
     public void generateAverageGraph (){
    	 //Assuming the edges and nodes have already been read from the original file..
    	 ArrayList <Integer> allNodes = new ArrayList<Integer>();
    	 ArrayList <Edge> allEdges = new ArrayList<Edge>();
    	 
    	 ArrayList <Edge> timeSliceEdge;
    	 ArrayList <Integer> timeSliceNode;
    	 int edgeNum = 0;
    	 this.graph = new UndirectedSparseGraph<Integer,Edge>();  
    	 Integer currentNode;
    	 Edge currentEdge;
    	 
    	 for (int i=0;i<this.numTimeSlices;i++){
    		 timeSliceEdge = this.edges.get(i);    		
    		 if (timeSliceEdge!=null){    			
    			 for (int j=0;j<timeSliceEdge.size();j++){  
    				 currentEdge = timeSliceEdge.get(j);    				 
    				 if (!findEdge(allEdges,currentEdge)){
    					 allEdges.add(currentEdge); 
    					 this.graph.addEdge(edgeNum,currentEdge.node1,currentEdge.node2);
    					 edgeNum++;
    				 }    				    	        	
    			 }
    		 }
    		 timeSliceNode = this.nodesWithEdges.get(i);
    		 if (timeSliceNode !=null){
    			 for (int j=0;j<timeSliceNode.size();j++){
    				 currentNode = timeSliceNode.get(j);
    				 if (!allNodes.contains(currentNode)){
    					 allNodes.add(currentNode); 
    					// System.out.println(currentNode+" "+allNodes.size());
    					 this.graph.addVertex(currentNode);
    				 }
    			 }
    		 }
    	 }     	
    	 //Hack here: node 17 does not have any edges (not stored in nodeswithedges array), add it at the very end
    	 this.layout = new FRLayout <Integer,Edge>(this.graph); 
    	 //this.layout.setRepulsionMultiplier(0.5);
    	 //this.layout.setAttractionMultiplier(99);
		 this.layout.setMaxIterations(1000000000);
		 this.layout.setSize(new Dimension(1250,600));
		
		 allNodes.add(new Integer(17));
		 Collections.sort(allNodes);
		 
		 this.output = parent.createWriter("reuters_saved.txt");
		 
		 //Print out the average graph info (ideally, should save to a text file)
		 //Print out the node, nodeId, position and persistence at each time slice
		 for (int i=0;i<allNodes.size();i++){	
			 System.out.println("node "+i+" "+this.layout.getX(i)+" "+this.layout.getY(i));
			 this.output.println("node "+i+" "+this.layout.getX(i)+" "+this.layout.getY(i));
			 for (int j=0;j<this.numTimeSlices;j++){
				  if (this.nodesWithEdges.get(j).contains(i)){	   	   			
			   	      System.out.println("1");	
			   	     this.output.println("1");
		   		  }else{	   			
		   			  System.out.println("0");
		   			this.output.println("0");
		   		  }
			 }
		 }
		//Print out the edge info
			for (int i=0;i<this.numTimeSlices;i++){				
				System.out.println("time "+i);
				this.output.println("time "+i);
				for (int j=0;j<this.edges.get(i).size();j++){
					currentEdge = this.edges.get(i).get(j);					
					System.out.println(currentEdge.node1+" "+currentEdge.node2);
					this.output.println(currentEdge.node1+" "+currentEdge.node2);
				}
			}
		 this.output.flush();
		 this.output.close();
     }
     /**Finds an edge in an arraylist of edges
      * */
     public boolean findEdge(ArrayList<Edge> edges,Edge e){
    	 for (int i=0;i<edges.size();i++){    					
			 if ((e.node1 == edges.get(i).node1 && e.node2 == edges.get(i).node2) ||
					 (e.node2 == edges.get(i).node1 && e.node2 == edges.get(i).node1)){
				    return true;			
			 }
		 } 
    	 return false;
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
    
     /**Reads the text file containing time-varying data of undergraduate student's friendship
      * Originally used in an experiment by Van De Bunt (1999)
      * */
     public void readVanDeBunt(){
    	  String filename = "vanDeBunt_all_time_30.txt";
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
       						this.nodes.add(new Node(this.parent,nodeCounter,""+nodeCounter,this.numTimeSlices));	       						  						
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
       	  //System.out.println(this.numTimeSlices);
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
    
    public void readReutersNetwork(){
    	int timeSlices = 10;
    	String filename = "Days.txt";
     	  Scanner scan;
     	  
     	  int nodeCounter = 0;      	  
     	  int type = 0;
     	  this.nodes = new ArrayList<Node>();
     	  this.edges = new ArrayList <ArrayList<Edge>>();
     	  this.nodesWithEdges = new ArrayList <ArrayList<Integer>>();
     	  String line;
     	  String[] items;
     	  int timeInterval = -1;
     	  int edgeCounter = 0;
     	  
     	  try {
     			scan = new Scanner(new File(filename));
     			while(scan.hasNext())
     			{   				
     				
     				line = scan.nextLine();
     				items = line.replaceAll("(^\\s+|\\s+$)", "").split("\\s+");
     				if (items[0].equals("Vertices")){ //Read in vertices      					
     					type=1;
     				}else if (items[0].equals("Edges")){//Read in edges
     					type =2;
     				}else{
     					if (type==1){
     						int [] persistence = stringToIntArray(items[2]);
     						Node nodeToAdd = new Node(parent,Integer.parseInt(items[0]),items[1],timeSlices);
     						boolean addIt = false;
     						for (int i=0;i<persistence.length;i++){
     							if(persistence[i]<=10){
     								addIt = true;
     							}
     						}
     						if (addIt){
     							this.nodes.add(nodeToAdd);     							
     						}     						
     					}else if (type==2){
     						int [] timeSliceNum = stringToIntArray(items[3]);     					    
     						if (timeSliceNum[0]>timeSlices) break;
     						
     						if (timeSliceNum[0]!=timeInterval){
     							this.nodesWithEdges.add(new ArrayList<Integer>());
     							this.edges.add(new ArrayList<Edge>());
     						}
     						int nodeId1 = Integer.parseInt(items[0]);
     						int nodeId2 = Integer.parseInt(items[1]);
     						int adjustedNode1Id =getNodeId(nodeId1);
     						int adjustedNode2Id =getNodeId(nodeId2);
     						this.edges.get(timeSliceNum[0]-1).add(new Edge(parent,""+edgeCounter,adjustedNode1Id,adjustedNode2Id,timeSlices));
     						
     						ArrayList<Integer> temp = this.nodesWithEdges.get(timeSliceNum[0]-1);
     						if (!temp.contains(adjustedNode1Id)){
     							this.nodesWithEdges.get(timeSliceNum[0]-1).add(adjustedNode1Id);
     						}
     						
     						if (!temp.contains(adjustedNode2Id)){
     							this.nodesWithEdges.get(timeSliceNum[0]-1).add(adjustedNode2Id);
     						}
     						timeInterval = timeSliceNum[0]-1;
     						edgeCounter++;
     						
     					}
     				}
     			}	       			
     		} catch (FileNotFoundException e) {			
     			e.printStackTrace();
     		} 
     	  for (int i=0;i<this.nodes.size();i++){
     		  this.nodes.get(i).id = i;
     	  }
     	  this.numTimeSlices = timeSlices;          	  
    }
    public int getNodeId(int id){    	
    	for (int i=0;i<this.nodes.size();i++){
    		if (this.nodes.get(i).id==id) return i;
    	}
    	return -1;
    }
    //http://stackoverflow.com/questions/7646392/convert-string-to-int-array-in-java
    public int [] stringToIntArray(String arr){
    	String[] items = arr.replaceAll("\\[", "").replaceAll("\\]", "").split(",");

    	int[] results = new int[items.length];

    	for (int i = 0; i < items.length; i++) {
    	    try {
    	        results[i] = Integer.parseInt(items[i]);
    	    } catch (NumberFormatException nfe) {};
    	}
    	return results;
    }
    
    /** Finds an edge in an ArrayList of edges
     *  @param Arraylist of edges to search within
     *  @param e the edge to search for
     *  @return index of the edge that was found, -1 otherwise
     * */
    int find(ArrayList<Edge> edges, Edge e){
		  for (int i=0;i<edges.size();i++){
			  if (e.equalTo(edges.get(i))){
				  return i;
			  }
		  }
		  return -1;
    }
	 
}
