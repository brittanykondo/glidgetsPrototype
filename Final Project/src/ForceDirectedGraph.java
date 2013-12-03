import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import processing.core.PApplet;

/**Manages the elements of a force directed graph visualization which evolves over time
 * This does not generate the layout, only reads in a data file containing pre-computed 
 * coordinates
 * */
public class ForceDirectedGraph {	 
	 public ArrayList<Node> nodes;
     public ArrayList<Edge> edges;  
     public ArrayList <Node> aggregatedNodes;
     PApplet parent;
     public int numTimeSlices;
     public int currentView;
     public int nextView;
     public boolean dragging;
     public boolean multipleNodes;
     public int selectedNode;
     public int releasedNode;
     public int selectedEdge;
     
     /**Creates a new graph manager which generates or parses and saves the data
      * necessary for drawing the dynamic graph
      * @param p   Processing applet for drawing the graph elements
      * @param dataFile   name of the data file with connectivity and node positions
      * @param time   the number of time slices
      * */
     public ForceDirectedGraph(PApplet p,String dataFile,int time){
    	 this.parent = p; 
    	 this.numTimeSlices = time;
    	 this.currentView = 0;
    	 this.nextView = 1;
    	 this.dragging = false;
    	 this.selectedNode = -1;
    	 this.releasedNode = -1;
    	 this.selectedEdge = -1;
    	 this.aggregatedNodes = new ArrayList<Node>();
    	 this.multipleNodes = false;
         readGraphDataFile(dataFile);            
     }     
     
     /**Calls the display function for all nodes and edges, which will
      * render them onto the screen for a certain view
      * */
     public void drawGraph(int view){       	
		 //Render the edges
    	 for (int row = 0;row<this.edges.size();row++)
			 this.edges.get(row).display(this.nodes,view);  	    		 
		  
		 //Render the edge hint path (if an edge is selected) or the node hint path (no dragging)
		 if (selectedEdge !=-1){
			 if (selectedEdge ==-2){ //Show that two nodes are never connected
				 Edge blankEdge = new Edge(this.parent,"",this.selectedNode,this.releasedNode,this.numTimeSlices);
				 blankEdge.drawHintPath(this.nodes);
			 }else{ //Otherwise draw the regular hint path
				 this.edges.get(selectedEdge).drawHintPath(this.nodes);
			 }			 
		 }else if (this.selectedNode != -1){
			 this.nodes.get(this.selectedNode).drawHintPath(this.currentView,0);
		 }
		 
		 //Render the nodes
		 for (int i = 0;i<this.nodes.size();i++)
			 this.nodes.get(i).display(view); 
		 
		 //Update the view
	     this.currentView = view;
		 this.nextView = view++;
     }
     /**Handles the mouse down listener for all nodes in the graph.
      * For now, only one node can be clicked at the same time
      * */
     public void selectNodes(){  
    	 //Re-set event variables
    	 this.releasedNode = -1;
    	 this.selectedEdge = -1;
    	 //this.selectedNode = -1;
    	 
    	 int selected = -1;
    	 for (int i = 0;i<this.nodes.size();i++){
    		selected = this.nodes.get(i).selectNode(this.currentView); 
    		if (selected == this.selectedNode) {
    			//Start dragging around node
    		}
            if (selected !=-1){
            	this.selectedNode = selected;
            }    			
    	 }	    	 
    	 this.dragging = true;
     }
     /**Handles the mouse up listener for all nodes in the graph.
      * If the mouse up event is on a different node than what is
      * selected, want to draw the edge hint path in between nodes    
      * */
     public void releaseNodes(){  
    	 int released = -1;
    	 for (int i = 0;i<this.nodes.size();i++){
    		released = this.nodes.get(i).releaseNode(this.currentView,this.selectedNode);
    		if (released != -1) this.releasedNode = released;
    	 }
    	 this.dragging = false;    	
     }
     
     /**Re-sets the selected and released node after the edge hint path is drawn.
      * Draws the hint path for the edge joined by selected and released node
      * */
     public void connectNodes(){
    	 Edge e = new Edge(this.parent,"",this.selectedNode,this.releasedNode,this.numTimeSlices);    	
         this.selectedEdge = find(this.edges,e);  
         this.selectedEdge = (this.selectedEdge==-1)?-2:this.selectedEdge; //Need to know if no connection exists between the nodes, in this case set the selectedEdge to -2 
                                                                           //(-1 already means no edge is currently selected)
     }
     /**Allows for multiple nodes to be selected if a key is held down.
      * */
     public void selectMultipleNodes(){
    	 this.selectedNode = -1;
    	 int selected = -1;
    	 for (int i = 0;i<this.nodes.size();i++){
    		selected = this.nodes.get(i).selectNode(this.currentView); 
    		if (selected != -1) {
    			this.aggregatedNodes.add(this.nodes.get(i));
    			this.selectedNode = selected;
    			System.out.println("selecting node "+this.selectedNode);
    		}
    	 }	
    	 this.multipleNodes = true;
     }
     /**When multiple nodes have been selected and the key is released,
      * an aggregated hint path is drawn on all of them to show 
      * when they disappear/reappear at the same time slices
      * */
     public void aggregateNodes(){
    	 ArrayList<Integer> aggregatedPersistence = new ArrayList<Integer>();
    	 Node current;
    	 for (int i=0;i<this.aggregatedNodes.size();i++){
    		 current = this.aggregatedNodes.get(i);
    		 for (int t=0;t<this.numTimeSlices;t++){
    			 if (i==0) {
    				 aggregatedPersistence.add((current.coords.get(t)!=null)?1:0);
    			 }else{
    				 if (aggregatedPersistence.get(t)==1 && current.coords.get(t)!=null){
    					 aggregatedPersistence.set(t, 1);
    				 }    				 
    			 }
    		 }    		
    	 }
    	 //TODO: draw the hint paths now, check if aggregation is correct
    	 this.aggregatedNodes.clear();
    	 this.multipleNodes = false;
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
     /**Animates the graph between time slices, node positions are interpolated
      * Animates in response to the slider being dragged, therefore speed is 
      * dependent on dragging speed.
      * @param start the starting time slice
      * @param end the ending time slice
      * @param interpolation the amount to interpolation the motion by
      * */
     public void animateGraph(int start, int end, float interpolation){
    	 for (int i = 0;i<this.nodes.size();i++){
    		   this.nodes.get(i).animate(start, end, interpolation,this.selectedNode);	
    	  }
    	 for (int row = 0;row<this.edges.size();row++){     		 
 	          this.edges.get(row).animate(this.nodes, start, end, interpolation);    	        	   	
 	     }
    	 this.currentView = start;
    	 this.nextView = end;
     }
     
     /** Calls functions for showing global persistence values for each edge and node
      * */
     public void drawGlobalPersistence(int view){
    	 for (int i = 0;i<this.nodes.size();i++){
    		   this.nodes.get(i).displayGlobalPersistence(view) ;   	
    	  }
    	 for (int i = 0;i<this.edges.size();i++){
  		      this.edges.get(i).displayGlobalPersistence(this.nodes,view) ;   	
  	     }
     }
     /**Reads the text file containing the node positions and edges for each time slice
      * */
     public void readGraphDataFile(String filename){    	 
       	  Scanner scan;
       	  int time = 0;
       	  int nodeId = 0;	 
       	  boolean nodesDone = false;
       	  Edge newEdge;
       	  int foundEdge;
       	  
       	  this.nodes = new ArrayList<Node>();
       	  this.edges = new ArrayList <Edge>	();
       	  
       	  //Read in the file
       	  try {
       			scan = new Scanner(new File(filename));
       			while(scan.hasNext())
       			{   				
       				String line;
       				line = scan.nextLine();
       				String[] items = line.split(" ");
       				if (items[0].equals("node")){ //Save the node
       					nodeId = Integer.parseInt(items[1]);       					
       					this.nodes.add(new Node(this.parent,nodeId,items[1],this.numTimeSlices));
       				}else if (items[0].equals("time")){ //Save the time slice
       					nodesDone = true;    
       					time = Integer.parseInt(items[1]);       				
       				}else{
       					if (nodesDone){ //Save the edge information       						
       						newEdge = new Edge (this.parent,"",Integer.parseInt(items[0]),Integer.parseInt(items[1]),this.numTimeSlices);
       						foundEdge = find(this.edges,newEdge);
       						if (foundEdge==-1){ //If edge doesn't exist, create it       							
       							newEdge.persistence.set(time, 1);
       							this.edges.add(newEdge);
       						}else{//Otherwise just update the persistence info
       							this.edges.get(foundEdge).persistence.set(time, 1);
       						}           					
           				}else{//Save the node coordinates
           					if (items[0].equals("null")){ //Node does not appear for this time slice
           						this.nodes.get(nodeId).coords.add(null);
           					}else{           						
           						this.nodes.get(nodeId).coords.add(new Coordinate(Float.parseFloat(items[0]),Float.parseFloat(items[1])));
           					}       						
           				}
       				}       				
       				
       			}	       			
       		} catch (FileNotFoundException e) {			
       			e.printStackTrace();
       		}
       	  
       	  //Set the node degree changes over time
       	  for (int i=0;i<this.nodes.size();i++){
       		  this.nodes.get(i).setNodeDegree(this.edges);
       	  }
     }
}
