/**Reads and parses a data file containing network dataset (separate functions for each file), generates a force-directed layout for the network,
 * and writes out a file that is readable by Glidgets (RunApp.java).  Generates the layout using the JUNG library (FRLayout):
 * See online documentation for more info: http://jung.sourceforge.net/doc/api/edu/uci/ics/jung/algorithms/layout/FRLayout.html
 * 
 *  Requires the JUNG libraries: http://jung.sourceforge.net/download.html
 *  
 * */
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import processing.core.PApplet;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class GraphGenerator {
	public Graph graph;
	public FRLayout<Integer,Edge> layout;
	public ArrayList<Node> nodes;
    public ArrayList<ArrayList<Edge>> edges;    
    public ArrayList<String> timeLabels;
    public PrintWriter output;
    public int width;
    public int height;
      
    PApplet parent;
    public int numTimeSlices;
    String outfile;
    
    /**Creates a new graph manager which generates or parses and saves the data
     * necessary for drawing the dynamic graph
     * @param p reference to the processing applet (for writing a file)
     * @param t the number of time slices in the dataset
     * */
    public GraphGenerator(PApplet p,int t){
   	      this.parent = p;      
   	      this.numTimeSlices = t;
   	      this.nodes = new ArrayList<Node>();
   	      this.edges = new ArrayList<ArrayList<Edge>>(); //All edges that exist at each time point
   	      this.timeLabels = new ArrayList<String>();
    }
    
    /**Reads the data file and generates a graph layout which is written to a data file, 
     * named outfile.txt.  This file can be read by Glidgets to generate a visualization.
     * @param outfile the name of the file containing saved graph data
     * @param graphWidth, graphHeight  The dimensions of the graph  
     * */
    public void process(String of,int graphWidth,int graphHeight){  
    	 this.outfile = of;    	
    	 this.width = graphWidth;
    	 this.height = graphHeight;
    	 
    	 //Read the graph data (uncomment for desired dataset)
      	 readVanDeBunt();         
    	 //readWC();
    	 
    	 addElements(this.nodes,this.edges); //Add all elements to a Jung graph data structure       
      	 createLayout(0.75,0.75,1000,this.width,this.height);
      	 saveGraphData(this.outfile,true,this.nodes,this.edges,this.timeLabels);
    }
    /** Creates a JUNG graph layout to re-position nodes stored in the graph, runs the layout for a specified amount
     * of iterations.
     * @param r  repulsion factor 
     * @param a  attraction factor
     * @param it number of iterations for running the algorithm
     * @param w, h drawing dimensions of the graph
     * */
    public void createLayout (double r, double a, int it,int w, int h){    	 
   	 //Create the JUNG layout and set some parameters
   	 this.layout = new FRLayout <Integer,Edge>(this.graph); 
   	 this.layout.setRepulsionMultiplier(r);
   	 this.layout.setAttractionMultiplier(a);
     this.layout.setMaxIterations(it);  
     this.layout.setSize(new Dimension(w,h));
	 
     while (!this.layout.done()){ //Keep applying the FR algorithm until max iterations reached
	     this.layout.step();
	  }	
    }
    /**Saves the graph data to a file that is readable by RunGlidgets.java
     * 
     * Format of the output file:
     *  
     *  timeline
     *  time1Label time2Label etc... (to appear on the time slider)
     *  
     *  node nodeId(unique) nodeXPosition nodeYPosition nodeLabel
     *  persistence at t=0
     *  .... to t=totalTimeSlices
     *  ....for all nodes
     *  
     *  time timeSliceNumber
     *  node1 node2 (edges)
     *  ...for all time slices 
     *  
     *  @param out name of the output file
     *  @param useLayout  true, if FR layout should be applied to node positions  
     *
     * */
    public void saveGraphData(String out,boolean useLayout,ArrayList<Node>nodes, ArrayList<ArrayList<Edge>> edges,ArrayList<String> timeLabels){
    	this.output = parent.createWriter(out+".txt");		 
    	
    	//Save the time line labels     
        this.output.println("timeline");
        int numTimeSlices = timeLabels.size();
        for (int i=0;i<numTimeSlices;i++){        	
        	this.output.print(timeLabels.get(i)+" ");
        }
        this.output.println();
    	//Save the node info
    	for (int i=0;i<nodes.size();i++){	
    		if (useLayout){
    			 this.output.println("node "+i+" "+this.layout.getX(i)+" "+this.layout.getY(i)+" "+nodes.get(i).label);
    		}else{
    			 this.output.println("node "+i+" "+nodes.get(i).x+" "+nodes.get(i).y+" "+nodes.get(i).label);
    		}
    		
    		 for (int j=0;j<this.numTimeSlices;j++){
    			  this.output.println(nodes.get(i).persistence.get(j));
    		 }
    	 }
    	
    	//Save the edge info
    	 Edge currentEdge;   	 
        for (int i=0;i<numTimeSlices;i++){				
    			//System.out.println("time "+i); //Debugging
    			this.output.println("time "+i);
    			for (int j=0;j<edges.get(i).size();j++){
    				currentEdge = edges.get(i).get(j);						
    				this.output.println(currentEdge.node1+" "+currentEdge.node2);
    			}
        }    
        
    	 this.output.flush();
    	 this.output.close();
    }
    /**Adds all elements to the JUNG graph using the arrays used to save the data from the data file
     * */
    public void addElements(ArrayList<Node>nodes,ArrayList<ArrayList<Edge>>edges){
    	 this.graph = new UndirectedSparseGraph<Integer,Edge>(); //Create a new undirected graph to save elements
    	 
    	//Add all the nodes
    	for (int i=0;i<nodes.size();i++){
    		this.graph.addVertex(nodes.get(i).id);
    	}
    	
    	//Add all the edges
    	int edgeNum = 0;
    	ArrayList<Edge> allEdges = new ArrayList<Edge>();
    	Edge currentEdge;
    	for (int i=0;i<edges.size();i++){
    		for (int j=0;j<edges.get(i).size();j++){
    			currentEdge = edges.get(i).get(j);
    			if (!findEdge(allEdges,currentEdge)){
    				this.graph.addEdge(edgeNum,currentEdge.node1, currentEdge.node2);
    				allEdges.add(currentEdge);
    				edgeNum++;
    			}    			
    		}
    	}
   	 //Just a test to see if it worked..
     //System.out.println(this.graph.toString());
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
    /**Reads the text file containing time-varying data of undergraduate student's friendship
     * Originally used in an experiment by Van De Bunt (1999)
     * */
    public void readVanDeBunt(){
   	       String filename = "vanDeBunt_all.txt";
      	  Scanner scan;
      	  int time = 0;
      	  int nodeCounter = 0;      	
      	  ArrayList <ArrayList<Integer>> nodesWithEdges = new ArrayList <ArrayList<Integer>>();
      	 
      	  try {
      			scan = new Scanner(new File(filename));
      			while(scan.hasNext())
      			{   				
      				String line;
      				line = scan.nextLine();
      				String[] items = line.split(" ");
      				if (items[0].equals("time")){       					
      					time = Integer.parseInt(items[1]);
      					this.timeLabels.add(""+time);
      					nodeCounter = 0;  
      					this.edges.add(new ArrayList <Edge>());
      				}else{
      					if (time==0){ //The first time stamp, create all 32 nodes        						
      						this.nodes.add(new Node(this.parent,nodeCounter,""+nodeCounter,this.numTimeSlices));	       						  						
      					}
      					//Find the edges for each time stamp					
      					parseLine_vanDeBunt(nodesWithEdges,nodeCounter,time,items);       					
      					nodeCounter++;       					
      				}				
      			}	       			
      		} catch (FileNotFoundException e) {			
      			e.printStackTrace();
      		}  
      	  
      	  ArrayList <Integer> n = new ArrayList <Integer>();
      	  Integer currentNode;
      	  for (int i=0;i<this.nodes.size();i++){
      		  currentNode = this.nodes.get(i).id;
      		for (int j=0;j<nodesWithEdges.size();j++){
         		 n = nodesWithEdges.get(j);
         		 if (n.contains(currentNode)){
         			 this.nodes.get(i).persistence.add(1);
         		 }else{
         			this.nodes.get(i).persistence.add(0);
         		 }
         		  
         	  }
      	  }
      	  
      	  
    }
    /**Parses one line of the van de bunt data set and sets the edges
     * */
   public void parseLine_vanDeBunt(ArrayList<ArrayList<Integer>> nodesWithEdges,int nodeNumber, int time, String [] line){
   	Edge newEdge;    	    	
   	nodesWithEdges.add(new ArrayList<Integer>());
   	
   	for (int i=0;i<line.length;i++){
   		int relation = Integer.parseInt(line[i]);      		
   		if (i!= nodeNumber  &&(relation == 1 || relation==2)){ //Only consider best friend or friend relations as a connection     			
   			newEdge = new Edge (this.parent,"",i,nodeNumber,0);    			
   			this.edges.get(time).add(newEdge);  
   			
   			if (!nodesWithEdges.get(time).contains(i)){
   				nodesWithEdges.get(time).add(i);
   			}
   			if (!nodesWithEdges.get(time).contains(nodeNumber)){
   				nodesWithEdges.get(time).add(nodeNumber);
   			}  			
   		}
   	}     	
   	
   }  
   
   /** Reads the dataset representing history of FIFA World Cup matches
    * */
   public void readWC (){
	   String filename = "HistoryWC.txt";
  	  Scanner scan;
  	  int time = -1;     	
  	  int nodeId;
  	  ArrayList<Integer> allNodes = new ArrayList<Integer>();
  	  boolean readNode = true;
  	  
  	  try {
  			scan = new Scanner(new File(filename));
  			while(scan.hasNext())
  			{   				
  				String line;
  				line = scan.nextLine();
  				String[] items = line.trim().split(" ");
  				if (items[0].equals("edges")){ //Reading edge info
  					readNode = false;
  				}
  				
  				if (readNode && !(items[0].equals("nodes"))){
  					nodeId = Integer.parseInt(items[0])-1;  					
  					if (!allNodes.contains(nodeId)){
  						 this.nodes.add(new Node(this.parent,nodeId,items[1].substring(1,items[1].length()-1),this.numTimeSlices)); 
       				     this.nodes.get(nodeId).initPersistence();
       				     allNodes.add(nodeId);
       				 } 
  				}else if (!readNode && !(items[0].equals("edges"))){
  							
  					int endIndex = items[3].length()-2;
  					int currentTime;
  					System.out.println(endIndex);
  					if (endIndex ==1){
  						Character t = items[3].charAt(1);
  						currentTime = Character.getNumericValue(t)-1;  
  					}else{
  					    currentTime = Integer.parseInt(items[3].substring(1,endIndex+1))-1;
  					}
  								
  	  				if (time != currentTime){ //Switching time points
  	  					time = currentTime;
  	  				    this.edges.add(new ArrayList<Edge>());  
  	  				}  
  	  			   
  	  			    
  	  			    int n1 = Integer.parseInt(items[0])-1;					
					int n2 = Integer.parseInt(items[1])-1;
					if (allNodes.contains(n1) && allNodes.contains(n2)){ //Safety check
						this.edges.get(time).add(new Edge(this.parent,"",n1,n2,this.numTimeSlices));     					
						this.nodes.get(n1).persistence.set(time,1);
						this.nodes.get(n2).persistence.set(time,1); 
					}  
  	  				
  				} 			
  									
  			}	       			
  		} catch (FileNotFoundException e) {			
  			e.printStackTrace();
  		}  
   }
   
   /** Converts a String to an integer array
    * http://stackoverflow.com/questions/7646392/convert-string-to-int-array-in-java
    * */  
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
