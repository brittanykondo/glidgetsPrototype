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
     public int drawingView;
     public boolean dragging;
     public boolean draggingNode;
     public int keyPressed;
     public int selectedNode;
     public int releasedNode;
     public int selectedEdge;
     public float interpAmount;
     public int startView;
     public float mouseAngle;
     public Edge draggingEdge;
     public boolean onDraggingEdge;
     public int pinnedView;
     
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
    	 this.drawingView = 0;
    	 this.interpAmount = 0;
    	 this.dragging = false;
    	 this.draggingNode = false;
    	 this.selectedNode = -1;
    	 this.releasedNode = -1;
    	 this.selectedEdge = -1;
    	 this.mouseAngle = 0;
    	 this.pinnedView = -1;
    	
    	 this.aggregatedNodes = new ArrayList<Integer>();
    	 this.aggregatedEdges = new ArrayList<Edge>();
    	 this.keyPressed = -1;
    	 this.draggingEdge = null;
    	 this.onDraggingEdge = false;
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
			 if (selectedEdge ==-2){ //Show that two nodes are never connected
				 Edge blankEdge = new Edge(this.parent,"",this.selectedNode,this.releasedNode,this.numTimeSlices);
				 blankEdge.drawHintPath(this.nodes,null,view);
				 this.draggingEdge = blankEdge;
			 }else{ //Otherwise draw the regular hint path
				 this.edges.get(selectedEdge).drawHintPath(this.nodes,null,view);
				 this.draggingEdge = this.edges.get(selectedEdge);
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
	   //this.currentView = view;
	   //this.nextView = view++;	   
     }   
     /**Finds the min distance from the mouse point to a point on the line and decides if the edge
      * is selected and dragged along
      * */
     public void selectEdge(){    	 
    	 Coordinate pt1 = this.draggingEdge.hintCoords.get(this.currentView);
    	 if (parent.dist(pt1.x,pt1.y,parent.mouseX,parent.mouseY)<=3){ //Rough check to see if mouse was clicked on the anchor
    		 this.onDraggingEdge = true;
    		 this.pinnedView = this.currentView;
    	 }else{
    		 //Re-set the interaction variables to clear the hint path
    		 this.onDraggingEdge = false;     
    		 this.releasedNode = -1;
        	 this.selectedEdge = -1;    	 
        	 this.selectedNode = -1; 
        	 this.draggingEdge = null;        	
    	 }    	 
     }
     
     /**Releases an edge after the dragging has stopped, snaps to the nearest view
      * */
     public void releaseEdge(){    	
    	 releaseEdgeAnchor();
    	 this.onDraggingEdge = false;    	 
     }
     
     /**"Snaps" to a view when dragging along an edge is stopped
      * */
     public void releaseEdgeAnchor(){    	 
    	 Coordinate pt1 = this.draggingEdge.hintCoords.get(this.currentView);
    	 Coordinate pt2 = this.draggingEdge.hintCoords.get(this.nextView);         
         float nextDist = calculateDistance(pt1.x,pt1.y,parent.mouseX,parent.mouseY);
  		 float currentDist = calculateDistance(pt2.x,pt2.y,parent.mouseX,parent.mouseY);
  		 setDrawingView(currentDist,nextDist);
     }
     
     /** Checks where the mouse is w.r.t the edge
      * */
     public void dragAlongEdge(){ 	 
    	 //System.out.println(this.currentView+" "+this.nextView);
    	 //Get the two points of the line segment currently dragged along
    	 Coordinate pt1 = this.draggingEdge.hintCoords.get(this.currentView);
    	 Coordinate pt2 = this.draggingEdge.hintCoords.get(this.nextView);
         float [] minDist = minDistancePoint(parent.mouseX,parent.mouseY,pt1.x,pt1.y,pt2.x,pt2.y);
         Coordinate newPoint; //The new point to draw on the line
         float t = minDist[2]; //To test whether or not the dragged point will pass pt1 or pt2
       
         if (t<0){ //Passed current        	 
             moveBackward();               
             newPoint = new Coordinate(pt1.x,pt1.y);
         }else if (t>1){ //Passed next        	
             moveForward();                     
             newPoint= new Coordinate(pt2.x,pt2.y);
         }else{ //Some in between the views (pt1 and pt2)                         
             newPoint= new Coordinate (minDist[0],minDist[1]);             
             this.interpAmount = t;              
         }
         
         this.draggingEdge.animateHintPath(this.nodes, newPoint.x,newPoint.y);
         this.animateGraph(this.currentView, this.nextView, this.interpAmount, new int []{this.draggingEdge.node1,this.draggingEdge.node2}, this.pinnedView); 
    	 //System.out.println("dragging edge"+t+" "+this.currentView+" "+this.nextView);
     }
     
     /** Finds the minimum distance between a point at (x,y), with respect
      * to a line segment defined by points (pt1_x,pt1_y) and (pt2_x,pt2_y)
      * Code based on: http://stackoverflow.com/questions/849211/shortest
      * -distance-between-a-point-and-a-line-segment
      * Formulas can be found at: http://paulbourke.net/geometry/pointlineplane/
      * @return the point on the line at the minimum distance and the t parameter, as an array: [x,y,t]
      * */
     public float [] minDistancePoint (float x,float y,float pt1_x,float pt1_y,float pt2_x,float pt2_y){

    	   float distance = calculateDistance(pt1_x,pt1_y,pt2_x,pt2_y);
    	   //Two points of the line segment are the same
    	   if (distance == 0) return new float [] {pt1_x,pt1_y,0};

    	   float t = ((x - pt1_x) * (pt2_x - pt1_x) + (y - pt1_y) * (pt2_y - pt1_y)) / distance;
    	   if (t < 0) return new float [] {pt1_x,pt1_y,t}; //Point projection goes beyond pt1
    	   if (t > 1) return new float [] {pt2_x,pt2_y,t}; //Point projection goes beyond pt2

    	   //Otherwise, point projection lies on the line somewhere
    	    float minX = pt1_x + t*(pt2_x-pt1_x);
    	    float minY = pt1_y + t*(pt2_y-pt1_y);
    	    return new float [] {minX,minY,t};
    	}
     /** Calculates the distance between two points
      * (x1,y1) is the first point
      * (x2,y2) is the second point
      * @return the distance, avoiding the square root
      * */
     public float calculateDistance (float x1,float y1, float x2,float y2){
    	    float term1 = x1 - x2;
    	    float term2 = y1 - y2;
    	    return (term1*term1)+(term2*term2);
      }
     /**Moves the visualization forward by one time step
      * Adjusts the view variables accordingly
      * */
     public void moveForward(){
    	 if (this.nextView < this.numTimeSlices-1){ 
	    		this.currentView = nextView;
		    	this.nextView++;
	    	}
     }
     /**Moves the visualization back in time by one time step
      * Adjusts the view variables accordingly
      * */
     public void moveBackward(){
    	 if (this.currentView >0){
    		 this.nextView = this.currentView;
	    	 this.currentView--;
    	}
     }
     /** Checks where the mouse is w.r.t the hint path
      * */
     public void dragAroundNode(){   	
    	 
    	 Node n = this.nodes.get(this.selectedNode);	   	 
    	 //Find the angle of the mouse w.r.t the node's center point
	   	 float adj = parent.mouseX - n.x;
	   	 float opp = n.y - parent.mouseY;
	   	 float mouseAngle = (float) Math.atan2(adj,opp);
	 
	   	 if (mouseAngle < 0)	mouseAngle = (float) ((Math.PI - mouseAngle*(-1))+Math.PI);
	   	 
	   	 float currentAngle = n.hintAngles.get(this.currentView).x + Math.abs(n.hintAngles.get(this.currentView).y - n.hintAngles.get(this.currentView).x);
	   	 float nextAngle = n.hintAngles.get(this.nextView).x + Math.abs(n.hintAngles.get(this.nextView).y - n.hintAngles.get(this.nextView).x);
	   	 float bounds = checkBounds(mouseAngle,currentAngle,nextAngle);    
	   	System.out.println(currentAngle*180/Math.PI+" "+nextAngle*180/Math.PI+" "+mouseAngle*180/Math.PI);
	        //Change views or update the view
		    if (bounds == 0){		    
		    	this.interpAmount = Math.abs(mouseAngle -currentAngle)/(nextAngle-currentAngle);		    	
		    }else if (bounds == 1) { //At current	
		    	moveBackward();			    				   
		    }else{ //At next
		    	moveForward(); 			    		        
		    }   
		   //System.out.println(this.currentView+" "+this.nextView+" "+this.drawingView);
		    n.animateHintPath(mouseAngle-parent.HALF_PI);
		    animateGraph(this.currentView, this.nextView, this.interpAmount,new int [] {n.id,-1},this.pinnedView);
		    
		    this.mouseAngle = mouseAngle;
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
     /**Sets the view variables as the provided parameters
      * @param current the current view
      * @param next  the next view
      * @param drawing the view to draw the graph at
      * */
    public void updateView(int current,int next,int drawing){
    	this.currentView = current;
    	this.nextView = next;
    	this.drawingView = drawing;
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
            	if (selected == previousNode && this.keyPressed==-1) {
            		this.draggingNode = true;
            		this.pinnedView = this.currentView;
            	}
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
    	 
    	 if (this.draggingNode){ //Snap to view after dragging around the node
    		 releaseNodeAnchor();    		
    	 }else{ //See if an edge hint path should be drawn
    		 findReleasedNode();
    	 }    	 
    	 
    	 //Re-set the interaction variables
    	 this.dragging = false; 
    	 this.draggingNode = false;    	 
     }
     /**Tests whether the mouse was released on a different node to show the edge hint path
      * */
     public void findReleasedNode(){
    	 int released = -1;
		 for (int i = 0;i<this.nodes.size();i++){
	    		released = this.nodes.get(i).releaseNode(this.currentView,this.selectedNode);
	    		if (released != -1) this.releasedNode = released;    		
	    	 }
	    	 
	    	 if (this.selectedNode != -1 && this.releasedNode !=-1){    		 
	    		 connectNodes();
	    	 }
     }
     /**"Snaps" to a view when dragging around the node is stopped
      * */
     public void releaseNodeAnchor(){ 
    	 
    	 Node n = this.nodes.get(this.selectedNode);
    	 float current = n.hintAngles.get(this.currentView).x + (n.hintAngles.get(this.currentView).y - n.hintAngles.get(this.currentView).x);
	   	 float next = n.hintAngles.get(this.nextView).x + (n.hintAngles.get(this.nextView).y - n.hintAngles.get(this.nextView).x);
         
         float nextDist = Math.abs(this.mouseAngle - next);
  		 float currentDist = Math.abs(this.mouseAngle - current);
  		 setDrawingView(currentDist,nextDist);  		  		
     }
     /**Updates the view variables and sets the drawing variables.  This is called when the visualization
      * should "snap" to a view 
      * @param currentDist, nextDist  the distance from the mouse to the current and next view markers on 
      *                               the hint path
      * */
     public void setDrawingView(float currentDist,float nextDist){
    	 if (currentDist < nextDist){ //Snap to current view  			
  			 this.drawingView = this.currentView;
  		 }else{ //Snap to next view  			
  			 if (this.nextView<(this.numTimeSlices-1)){
  				 this.currentView = this.nextView;
  				 this.nextView++;
  				 this.drawingView = this.currentView;
  			 }else{
  				 this.drawingView = this.nextView;
  			 }
  		 } 
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
    		 this.aggregatedEdges.get(i).drawHintPath(this.nodes, aggregatedPersistence,this.currentView);
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
      * @param pinned the object(s) (node, edge) that should be pinned during the animation (set to -1 if none)
      * @param pinnedView the view to pin their position to (set to -1 if none)
      * */
     public void animateGraph(int start, int end, float interpolation,int [] pinned,int pinnedView){    	
    	 for (int row = 0;row<this.edges.size();row++){     		 
 	          this.edges.get(row).animate(this.nodes, start, end, interpolation);    	        	   	
 	     }    	 
    	
    	 for (int i = 0;i<this.nodes.size();i++){
  		      this.nodes.get(i).animate(start, end, interpolation,pinned,pinnedView);	
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
