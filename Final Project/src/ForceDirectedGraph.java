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
     public ArrayList <Integer> aggregatedNodes; //Only stores the id's of the nodes
     public ArrayList <Edge> aggregatedEdges;
     PApplet parent;
     public int numTimeSlices;
     public int currentView;
     public int nextView;
     public boolean dragging;
     public boolean draggingNode;
     public int keyPressed;
     public int selectedNode;
     public int releasedNode;
     public int selectedEdge;
     public float interpAmount;
     
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
    	 this.interpAmount = 0;
    	 this.dragging = false;
    	 this.draggingNode = false;
    	 this.selectedNode = -1;
    	 this.releasedNode = -1;
    	 this.selectedEdge = -1;
    	 this.aggregatedNodes = new ArrayList<Integer>();
    	 this.aggregatedEdges = new ArrayList<Edge>();
    	 this.keyPressed = -1;
         readGraphDataFile(dataFile);            
     }     
     
     /**Calls the display function for all nodes and edges, which will
      * render them onto the screen for a certain view
      * */
     public void drawGraph(int view){      	
    	 
	   if (keyPressed ==1){ //Case 1: 'n' key is pressed, multiple nodes can be selected, aggregate the hint paths
		   renderEdges(view);		
		   aggregateNodeHintPaths();
		   renderNodes(view);
	   }else if (keyPressed ==2){ //Case 2: 'e' key is pressed, multiple edges can be selected, aggregate the hint paths
		   renderEdges(view);		  
		   aggregateEdgeHintPaths();
		   renderNodes(view);
	   } else if (selectedEdge !=-1){ //Case 3: Need to draw an edge hint path
			 renderEdges(view);
			 renderNodes(view);			
			 for (int i = 0;i<this.nodes.size();i++)
				 this.nodes.get(i).display(view); 
			 if (selectedEdge ==-2){ //Show that two nodes are never connected
				 Edge blankEdge = new Edge(this.parent,"",this.selectedNode,this.releasedNode,this.numTimeSlices);
				 blankEdge.drawHintPath(this.nodes,null);
			 }else{ //Otherwise draw the regular hint path
				 this.edges.get(selectedEdge).drawHintPath(this.nodes,null);
			 }				
		 }else if (this.selectedNode != -1){ //Case 4: draw a node's hint path			 
			 renderEdges(view);			 
			 this.nodes.get(this.selectedNode).drawHintPath(view,0);	
			 renderNodes(view);
		 }else{ //Case 5: Just render both edges and nodes without hint paths
			 renderEdges(view);
			 renderNodes(view);		 
		 }	
	   
	   //Set the view variables
	    this.currentView = view;
		this.nextView = view++;
     }
    /** public void selectNodeAnchor(){
    	  Node n = this.nodes.get(this.selectedNode);
    	  float startAngle = n.hintAngles.get(this.currentView).x;
   	      float endAngle = n.hintAngles.get(this.currentView).y;
	   	  float drawingAngle = endAngle-startAngle;
	      float anchorX = (float) (n.x + n.RADIUS*Math.cos(startAngle+drawingAngle));
	      float anchorY = (float) (n.y + n.RADIUS*Math.sin(startAngle+drawingAngle));                         
	      
	      float mouseX = (float) (n.x + n.RADIUS*Math.cos(startAngle+drawingAngle));
	      float mouseY = (float) (n.x + n.RADIUS*Math.cos(startAngle+drawingAngle));
     }*/
    
     /** Checks where the mouse is w.r.t the hint path
      * */
     public void dragAroundNode(){   	 
    	 Node n = this.nodes.get(this.selectedNode);
	   	 
    	 //Find the angle of the mouse w.r.t the node's center point
	   	 float adj = parent.mouseX - n.x;
	   	 float opp = n.y - parent.mouseY;
	   	 float mouseAngle = (float) Math.atan2(adj,opp);
	 
	   	 if (mouseAngle < 0)	mouseAngle = (float) ((Math.PI - mouseAngle*(-1))+Math.PI);
	   	 
	   	 float currentAngle = n.hintAngles.get(this.currentView).x + (n.hintAngles.get(this.currentView).y - n.hintAngles.get(this.currentView).x);
	   	 float nextAngle = n.hintAngles.get(this.nextView).x + (n.hintAngles.get(this.nextView).y - n.hintAngles.get(this.nextView).x);
	   	 float bounds = checkBounds(mouseAngle,currentAngle,nextAngle);    
	   	System.out.println(currentAngle*180/Math.PI+" "+nextAngle*180/Math.PI+" "+mouseAngle*180/Math.PI);
	        //Change views or update the view
		    if (bounds == 0){		    
		    	this.interpAmount = Math.abs(mouseAngle -currentAngle)/(nextAngle-currentAngle);		    	
		    }else if (bounds == 1) { //At current	
		    	if (this.currentView >0){ //Move backward in time
		    		 this.nextView = this.currentView;
			    	 this.currentView--;
		    	}			    				   
		    }else{ //At next
		    	if (this.nextView < this.numTimeSlices-1){ //Move forward in time
		    		this.currentView = nextView;
			    	this.nextView++;
		    	} 			    		        
		    }   
		    parent.stroke(206,18,86,255); 
            parent.strokeWeight(n.MIN_WEIGHT);
            
            float x1 = (float) (n.x + n.RADIUS*Math.cos(mouseAngle - parent.HALF_PI));
            float y1 = (float) (n.y + n.RADIUS*Math.sin(mouseAngle - parent.HALF_PI));                         
            parent.line(x1, y1, n.x, n.y);
		    //n.drawHintPath(this.currentView,this.interpAmount);
		    animateGraph(this.currentView, this.nextView, this.interpAmount);
     }
     /** Checks if the mouse is in bounds defined by a and b, updates the interpolation amount
      *  @param mouse: the mouse position
      *  @return 0 if in between views
      *          1 if passed current
      *          2 if passed next
      * */
     public int checkBounds(float mouse,float a,float b){
   	//Resolve the boundaries for comparison, start is lower value, end is higher
   	    float start,end;
   	    if (a>b){
   	        end = a;
   	        start =b;
   	    }else{
   	        start = a;
   	        end = b;
   	    }

   	    //Check if the mouse is between start and end values
   	    if (mouse <= start) {    	       
   	        return 1;
   	    }else if (mouse >= end) {    	       
   	        return 2;
   	    }
   	    return 0;
     }        
  
     /**Draws all the nodes on the screen for the specified view
      * @param view  the current view of the visualization
      * */
     public void renderNodes(int view){    	
		 for (int i = 0;i<this.nodes.size();i++)
			 this.nodes.get(i).display(view); 
     }
     
     /**Draws all the edges on the screen for the specified view
      * @param view  the current view of the visualization
      * */
     public void renderEdges(int view){
    	 for (int row = 0;row<this.edges.size();row++)
			 this.edges.get(row).display(this.nodes,view); 
     }
     /**Handles the mouse down listener for all nodes in the graph.
      * For now, only one node can be clicked at the same time
      * */
     public void selectNodes(){  
    	 int previousNode = this.selectedNode;
    	 //Re-set event variables
    	 this.releasedNode = -1;
    	 this.selectedEdge = -1;    	 
    	 this.selectedNode = -1;    	 
    	 int selected = -1;
    	 
    	 for (int i = 0;i<this.nodes.size();i++){
    		selected = this.nodes.get(i).selectNode(this.currentView);     		
            if (selected !=-1)	{
            	if (selected == previousNode) this.draggingNode = true;
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
    	 
    	 if (this.selectedNode != -1 && this.releasedNode !=-1){    		 
    		 connectNodes();
    	 }
    	 this.dragging = false; 
    	 this.draggingNode = false;
     }
     
     /**Re-sets the selected and released node after the edge hint path is drawn.
      * Draws the hint path for the edge joined by selected and released node
      * */
     public void connectNodes(){
    	 Edge e = new Edge(this.parent,"",this.selectedNode,this.releasedNode,this.numTimeSlices);    	
         this.selectedEdge = find(this.edges,e);  
         this.selectedEdge = (this.selectedEdge==-1)?-2:this.selectedEdge; //Need to know if no connection exists between the nodes, in this case set the selectedEdge to -2 
                                                                           //(-1 already means no edge is currently selected)
         if (this.keyPressed==2){ //Need to aggregate the hint path  
        	     if (this.selectedEdge==-2){ //Non-existent edge
        	    	 this.aggregatedEdges.add(e);
        	     }else{
        	    	 this.aggregatedEdges.add(this.edges.get(this.selectedEdge));   
        	     }        		        	 
         }
     }
     /**Allows for multiple nodes to be selected if a key is held down.
      * */
     public void selectMultipleNodes(){
    	 this.selectedNode = -1;
    	 int selected = -1;
    	 for (int i = 0;i<this.nodes.size();i++){
    		selected = this.nodes.get(i).selectNode(this.currentView); 
    		if (selected != -1) {
    			if (!this.aggregatedNodes.contains(i)){
    				this.aggregatedNodes.add(i);
    			}    	   			
    		}
    	 }	
    	 this.keyPressed = 1;
     }
     /**Allows multiple edges to be selected if a key (e) is held down.
      * */
     public void selectMultipleEdges(){    	
    	 this.keyPressed = 2;
     }
     /**Allows for multiple nodes to be released when a key is released
      * */
     public void releaseMultipleNodes(){
    	this.keyPressed = -1;
    	this.aggregatedNodes.clear();    	
     }
     /**Allows for multiple nodes to be released when a key is released
      * */
     public void releaseMultipleEdges(){
    	this.keyPressed = -1;
    	this.aggregatedEdges.clear();  
    	this.selectedEdge = -1;
    	this.selectedNode = -1;
    	this.releasedNode = -1;
     }
     /**When multiple nodes have been selected and the key is released,
      * an aggregated hint path is drawn on all of them to show 
      * when they disappear/reappear at the same time slices
      * */
     public void aggregateNodeHintPaths(){
    	 
    	 if (this.aggregatedNodes.size()==0) return;
    	 
    	 ArrayList<Integer> aggregatedPersistence = new ArrayList<Integer>();
    	 Node currentNode;
    	 for (int i=0;i<this.aggregatedNodes.size();i++){    		 
    		 currentNode = this.nodes.get(this.aggregatedNodes.get(i));
    		 for (int t=0;t<this.numTimeSlices;t++){
    			 if (i==0) {
    				 aggregatedPersistence.add((currentNode.coords.get(t)!=null)?1:0);
    			 }else if (aggregatedPersistence.get(t)==1 && currentNode.coords.get(t)==null){
    				aggregatedPersistence.set(t, 0);    				 				 
    			 }
    		 }    		
    	 }    	
    	//Draw the hint paths for the aggregated nodes
    	 for (int i=0;i<this.aggregatedNodes.size();i++){
    		 this.nodes.get(this.aggregatedNodes.get(i)).drawAggregatedHintPath(0, 0, aggregatedPersistence);
    	 }    	
     }
     /**When multiple edges are selected and the key is released,
      * an aggregated hint path is drawn on all of them to show 
      * when they disappear/reappear at the same time slices
      * */
     public void aggregateEdgeHintPaths(){
    	 
    	 if (this.aggregatedEdges.size()==0) return;
    	 
    	 ArrayList<Integer> aggregatedPersistence = new ArrayList<Integer>();
    	 Edge currentEdge;
    	 for (int i=0;i<this.aggregatedEdges.size();i++){      		
    			 currentEdge =this.aggregatedEdges.get(i);
        		 for (int t=0;t<this.numTimeSlices;t++){
        			 if (i==0) {
        				 aggregatedPersistence.add(currentEdge.persistence.get(t));
        			 }else if (aggregatedPersistence.get(t)==1 && currentEdge.persistence.get(t)==0){
        				aggregatedPersistence.set(t, 0);    				 				 
        			 }
        		 } 	 
    		    		
    	 }    	
    	//Draw the hint paths for the aggregated edges
    	 for (int i=0;i<this.aggregatedEdges.size();i++){
    		 this.aggregatedEdges.get(i).drawHintPath(this.nodes, aggregatedPersistence);
    	 }    	
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
    	 for (int row = 0;row<this.edges.size();row++){     		 
 	          this.edges.get(row).animate(this.nodes, start, end, interpolation);    	        	   	
 	     }
    	 //System.out.println(start+" "+end+" "+interpolation+" "+this.selectedNode);
    	 for (int i = 0;i<this.nodes.size();i++){
  		   this.nodes.get(i).animate(start, end, interpolation,this.selectedNode);	
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
