import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import processing.core.PApplet;
import processing.core.PFont;

/**Manages the elements of a force directed graph visualization which evolves over time
 * This does not generate the layout, only reads in a data file containing pre-computed 
 * coordinates
 * */
public class ForceDirectedGraph {	 
	 public ArrayList<Node> nodes;
     public ArrayList<Edge> edges;  
     public ArrayList <Integer> aggregatedNodes; //Only stores the id's of the nodes
     public ArrayList <Edge> aggregatedEdges;
     public ArrayList<Integer> aggregatedPersistence;
     
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
     public boolean selectedSameNode;
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
    	 this.selectedSameNode = false;    	 
    	 this.mouseAngle = 0;
    	 this.pinnedView = -1;
    	
    	 this.aggregatedNodes = new ArrayList<Integer>();
    	 this.aggregatedEdges = new ArrayList<Edge>();
    	 this.aggregatedPersistence = new ArrayList<Integer>();
    	 
    	 this.keyPressed = -1;
    	 this.draggingEdge = null;
    	 this.onDraggingEdge = false;
         readGraphDataFile(dataFile);            
     }   
     /**Handles mouse pressed event, to interact with the graph elements
      * */
     public void selectElement(){
    	 this.selectEdge();	//See if an edge is dragged on	
	     
    	 if (!this.onDraggingEdge) //If not, look at the nodes
			this.selectNodes();	
     }
     /** Handles the mouse released event, to interact with graph elements
      * */
     public void releaseElements(){
    	 if (this.onDraggingEdge){
    		 this.releaseEdge();
    	 }else{
    		 this.releaseNodes();
    	 }
     }
     /**Calls the display function for all nodes and edges, which will
      * render them onto the screen for a certain view
      * */
     public void drawGraph(int view){      	
    	 
	     if (selectedEdge !=-1){ //Case 1: Need to draw an edge hint path
	    	 //this.drawEdgeHintPaths();	
			 renderEdges(view);
			 renderNodes(view);	
			 this.drawEdgeHintPaths();
		 }else if (this.selectedNode != -1){ //Case 2: draw node hint paths			 
			 renderEdges(view);				
			 drawNodeHintPaths(view);
			 renderNodes(view);
		 }else{ //Case 3: Just render both edges and nodes without hint paths
			 clearQueries();			 
			 renderEdges(view);
			 renderNodes(view);		 
		 }		   	      
     } 
    
     /**Finds the min distance from the mouse point to a point on the line and decides if the edge
      * is selected and dragged along
      * */
     public void selectEdge(){     	 
    	 //if (this.draggingEdge ==null) return;
    	 this.onDraggingEdge = false;
    	 
    	 for (int i=0;i<this.aggregatedEdges.size();i++){
    		 Coordinate startPt = this.aggregatedEdges.get(i).hintCoords.get(0);
        	 Coordinate endPt = this.aggregatedEdges.get(i).hintCoords.get(this.numTimeSlices-1);    	 
        	 float [] minPoint = minDistancePoint(parent.mouseX,parent.mouseY,startPt.x,startPt.y,endPt.x,endPt.y);
        	 if (parent.dist(minPoint[0],minPoint[1],parent.mouseX,parent.mouseY)<=10){ //Rough check to see if mouse was clicked on the anchor
        		 this.onDraggingEdge = true;
        		 this.draggingEdge = this.aggregatedEdges.get(i);
        		 this.pinnedView = this.drawingView;
        		 return;
        	 }
    	 }
    	/** Coordinate startPt = this.draggingEdge.hintCoords.get(0);
    	 Coordinate endPt = this.draggingEdge.hintCoords.get(this.numTimeSlices-1);    	 
    	 float [] minPoint = minDistancePoint(parent.mouseX,parent.mouseY,startPt.x,startPt.y,endPt.x,endPt.y);
    	 if (parent.dist(minPoint[0],minPoint[1],parent.mouseX,parent.mouseY)<=5){ //Rough check to see if mouse was clicked on the anchor
    		 this.onDraggingEdge = true;
    		 this.pinnedView = this.drawingView;
    	 }else {
    		 //Re-set the interaction variables to clear the hint path    
    		 this.onDraggingEdge = false;     
    		// this.releasedNode = -1;
        	 //this.selectedEdge = -1;    	 
        	 //this.selectedNode = -1; 
        	 //this.draggingEdge = null;           	
    	 }  */    	
     }
     
     /**Releases an edge after the dragging has stopped, snaps to the nearest view
      * */
     public void releaseEdge(){    	
    	 releaseEdgeAnchor();
    	 this.onDraggingEdge = false;
    	 this.dragging = false;
    	 this.draggingEdge = null;    	
     }
     
     /**"Snaps" to a view when dragging along an edge is stopped
      * */
     public void releaseEdgeAnchor(){    	 
    	 Coordinate pt1 = this.draggingEdge.hintCoords.get(this.currentView);
    	 Coordinate pt2 = this.draggingEdge.hintCoords.get(this.nextView);        	 
         float currentDist = calculateDistance(pt1.x,pt1.y,parent.mouseX,parent.mouseY);
  		 float nextDist = calculateDistance(pt2.x,pt2.y,parent.mouseX,parent.mouseY);  		
  		 setDrawingView(currentDist,nextDist);
  		 this.removeDisappearedEdges();
     }
     
     /** Checks where the mouse is w.r.t the edge
      * */
     public void dragAlongEdge(){ 	 
    	
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
         System.out.println(this.currentView+" "+this.nextView);
         this.animateGraph(this.currentView, this.nextView, this.interpAmount, new int []{this.draggingEdge.node1,this.draggingEdge.node2}, this.pinnedView); 
         this.drawEdgeHintPaths();
         this.draggingEdge.animateAnchor(newPoint.x,newPoint.y);        
         this.keepDisappearingNodes(1);    	 
     }
     /**Draws partial versions of nodes when dragging along a hint path
      * @param type:
      * 0: if a node hint path is being dragged around (only show the node label(s))
      * 1: if dragging along an edge hint path (show label + wireframe outline of node(s))
      * */
     public void keepDisappearingNodes(int type){
    	 if (type ==0){           	 
    		 for (int i=0;i<this.aggregatedNodes.size();i++){
    			 PFont font = parent.createFont("Droid Sans",12,true);
    			 parent.textFont(font);	   	  
    			 parent.fill(247,244,249,255);
    			 parent.textAlign(parent.CENTER);
    			 int nodeId = this.aggregatedNodes.get(i);
       	   	  	 parent.text(nodeId, this.nodes.get(nodeId).x, this.nodes.get(nodeId).y +4); 
    		 }        	  
    	 }else if (type==1){
    		 ArrayList<Integer> nodes = new ArrayList<Integer>();
    		 for (int i=0;i<this.aggregatedEdges.size();i++){
    			 Edge currentEdge = this.aggregatedEdges.get(i);
    			 //TODO: not an elegant way of draw the wire frame, re-factor this later
    			 if (!nodes.contains(currentEdge.node1)){
    				  Node n = this.nodes.get(currentEdge.node1);
    				 
    				  parent.noFill();
    		    	  parent.stroke(206,18,86,255);
    		    	  parent.strokeWeight(3);    		    	  
    		    	  parent.ellipse(n.x,n.y,n.RADIUS,n.RADIUS);  
    		    	  
    		    	  PFont font = parent.createFont("Droid Sans",12,true);
    			   	  parent.textFont(font);	   	  
    			   	  parent.fill(247,244,249,255);
    			   	  parent.textAlign(parent.CENTER);
    			   	  parent.text(n.label, n.x,n.y+4); 
    			 }
    			 if (!nodes.contains(currentEdge.node2)){
	    		      Node n = this.nodes.get(currentEdge.node2);
	    		     
	    		      parent.noFill(); 
	   		    	  parent.stroke(206,18,86,255);
	   		    	  parent.strokeWeight(3);    		    	  
	   		    	  parent.ellipse(n.x,n.y,n.RADIUS,n.RADIUS);  
	   		    	  
	   		    	  PFont font = parent.createFont("Droid Sans",12,true);
	   			   	  parent.textFont(font);	   	  
	   			   	  parent.fill(247,244,249,255);
	   			   	  parent.textAlign(parent.CENTER);
	   			   	  parent.text(n.label, n.x,n.y+4); 
    			 }
    			
    		 }
    	 }
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
    	    return ((term1*term1)+(term2*term2));
      }
     /**Moves the visualization forward by one time step
      * Adjusts the view variables accordingly
      * */
     public void moveForward(){
    	 if (this.nextView < this.numTimeSlices-1){ 
	    		this.currentView = nextView;
		    	this.nextView++;
		    	this.interpAmount = 0;
	    	}    	 
     }
     /**Moves the visualization back in time by one time step
      * Adjusts the view variables accordingly
      * */
     public void moveBackward(){
    	 if (this.currentView >0){
    		 this.nextView = this.currentView;
	    	 this.currentView--;
	    	 this.interpAmount = 1;
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
	   	 
	   	float currentAngle = n.hintAngles.get(this.currentView).x; 
	   	float nextAngle = n.hintAngles.get(this.currentView).y;    	 
	   	float bounds = checkBounds(mouseAngle,currentAngle,nextAngle);
	   	
	   	//Prevent wrapping around the node hint path
	   	float lastAngle = n.hintAngles.get(this.numTimeSlices-1).x;
	   	int fixAnchor = 0;
	   	
	   	if (this.nextView == (this.numTimeSlices-1) && 
	   			(this.mouseAngle<=lastAngle && this.mouseAngle>Math.PI  && 
	   			((mouseAngle >lastAngle && mouseAngle <= Math.PI*2)||(mouseAngle>=0 && mouseAngle < Math.PI)))){ //Prevent wrapping around from last view to first vie
	   		fixAnchor = 1;
	   		mouseAngle = lastAngle;	   	   		
	   	}else if (this.currentView ==0 && mouseAngle > Math.PI && mouseAngle <= Math.PI*2 
     			&& this.mouseAngle >= 0 && this.mouseAngle < Math.PI){ //Prevent wrapping around from first to last view    		
    		 mouseAngle = (float) 0.02;
    		 fixAnchor = 1;
    	 }
	   	
	   	//Change views or update the view
	   	if (fixAnchor !=1){
		    if (bounds == 0){		    
		    	this.interpAmount = Math.abs(mouseAngle -currentAngle)/(nextAngle-currentAngle);		    	
		    }else if (bounds == 1) { //At current	
		    	moveBackward();			    				   
		    }else{ //At next	    	
		    	moveForward();		    	
		   } 
	    }
		    //System.out.println(this.currentView+" "+this.nextView+" "+this.drawingView+" "+this.interpAmount);	       
	   	   
		    animateGraph(this.currentView, this.nextView, this.interpAmount,new int [] {n.id,-1},this.pinnedView);
		    this.drawNodeHintPaths(-1);
		    n.animateAnchor(mouseAngle-parent.HALF_PI,fixAnchor);		   
		    this.keepDisappearingNodes(0);		    
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
    	 
    	 //Re-set event variables
    	 this.releasedNode = -1;
    	 this.selectedEdge = -1;    	 
    	 this.selectedNode = -1;      	
    	 this.selectedSameNode = false;
    	 //this.draggingNode = false;
    	 
    	 int selected = -1;
    	 //int selectionType = -1;
    	 int [] savedSelections;
    	 
    	 for (int i = 0;i<this.nodes.size();i++){
    		savedSelections = this.nodes.get(i).selectNode(this.drawingView);  
    		selected = savedSelections[0];
    		//selectionType = savedSelections[1];
    		
            if (selected !=-1)	{         
            	
            	if (!this.aggregatedNodes.contains(new Integer(selected))){
            		this.aggregatedNodes.add(selected);
            	}else{
            		this.selectedSameNode = true;
            	}
            	
            	this.aggregateNodeHintPaths();
            	this.selectedNode = selected;              	
            }
    	 }    
    	this.dragging = true;
     }
     /**Checks, during a mouse drag event, if the node should be dragged around
      * Otherwise, the node will be de-selected from an aggregated query
      * */
     public void isNodeDragged(){
    	 if (this.selectedSameNode){
    		 this.draggingNode = true;    		
    		 this.selectedSameNode = false;
    	 }
     }
     /**Handles the mouse up listener for all nodes in the graph.
      * If the mouse up event is on a different node than what is
      * selected, want to draw the edge hint path in between nodes    
      * */
     public void releaseNodes(){     	 
    	 
    	 if (this.draggingNode){ //Snap to view after dragging around the node
    		 releaseNodeAnchor();    		
    	 }else if (this.selectedSameNode){
    		 if (this.aggregatedNodes.contains(new Integer(this.selectedNode))){ //Node has already been selected, de-select it
         		this.aggregatedNodes.remove(new Integer(this.selectedNode));
         	}
    	 }else { //See if an edge hint path should be drawn
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
	    		released = this.nodes.get(i).releaseNode(this.drawingView,this.selectedNode);
	    		if (released != -1) this.releasedNode = released;    		
	      }
		// System.out.println("finding released "+this.selectedNode+" "+this.releasedNode);
	    	 
	    	 if (this.selectedNode != -1 && this.releasedNode !=-1){    		 
	    		 connectNodes();
	    	 }
     }
     /**"Snaps" to a view when dragging around the node is stopped
      * */
     public void releaseNodeAnchor(){     	 
    	 Node n = this.nodes.get(this.selectedNode);
    	 float current = n.hintAngles.get(this.currentView).x;
	   	 float next = n.hintAngles.get(this.currentView).y;
         
         float nextDist = Math.abs(this.mouseAngle - next);
  		 float currentDist = Math.abs(this.mouseAngle - current);
  		 setDrawingView(currentDist,nextDist);  	
  		 this.removeDisappearedNodes();
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
     /**When "snapping" to a view after dragging around a node, need to remove the node
      * hint paths where a node does not exist (which could cancel the entire query)
      * */
     public void removeDisappearedNodes(){
    	 ArrayList <Integer> removedNodes = new ArrayList <Integer>();
    	 for (int i=0;i<this.aggregatedNodes.size();i++){
    		 Node currentNode = this.nodes.get(this.aggregatedNodes.get(i));    		 
    		 if (currentNode.coords.get(this.drawingView)==null){    			
    			 removedNodes.add(currentNode.id);    			 
    		 }
    	 }
    	 this.aggregatedNodes.removeAll(removedNodes);
    	 this.aggregateNodeHintPaths();
     }
     /**When "snapping" to a view after dragging around a node, need to remove the node
      * hint paths where a node does not exist (which could cancel the entire query)
      * */
     public void removeDisappearedEdges(){
    	 ArrayList <Edge> newEdges = new ArrayList <Edge>();
    	 for (int i=0;i<this.aggregatedEdges.size();i++){
    		if (this.aggregatedEdges.get(i).persistence.get(this.drawingView)==1){
    			newEdges.add(this.aggregatedEdges.get(i));
    		}
    	 }
    	 this.aggregatedEdges.clear();
    	 this.aggregatedEdges = newEdges;
    	 this.aggregateEdgeHintPaths();
     }
     /**Re-sets the selected and released node after the edge hint path is drawn.
      * Draws the hint path for the edge joined by selected and released node
      * */
     public void connectNodes(){
    	// System.out.println("selecting"+this.selectedEdge);
    	 Edge e = new Edge(this.parent,"",this.selectedNode,this.releasedNode,this.numTimeSlices);    	
         this.selectedEdge = findEdge(this.edges,e);  
         this.selectedEdge = (this.selectedEdge==-1)?-2:this.selectedEdge; //Need to know if no connection exists between the nodes, in this case set the selectedEdge to -2 
                                                                          //(-1 already means no edge is currently selected)
         Edge toAdd;
         if (this.selectedEdge==-2){ //Non-existent edge
	    	 toAdd = e;
	     }else{
	    	 toAdd = this.edges.get(this.selectedEdge);   
	     }
         
         //Aggregate the edge hint paths if more than one edge is selected
         if (findEdge(this.aggregatedEdges,toAdd)!=-1){  //De-selecting an edge
    	     this.aggregatedEdges = removeEdge(this.aggregatedEdges,toAdd);    		        	 
         }else{ //Adding an edge to the aggregation
        	 this.aggregatedEdges.add(toAdd);
         }
         this.aggregateEdgeHintPaths();
         this.aggregatedNodes.clear();
     }
     /**Clears all queries on the screen (this is triggered when the background is clicked)
      * */
     public void clearQueries(){
    	//this.keyPressed = -1;    	
    	this.aggregatedNodes.clear();
    	this.aggregatedPersistence.clear();
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
    	 
    	 this.aggregatedPersistence.clear();    	 
    	 Node currentNode;
    	 for (int i=0;i<this.aggregatedNodes.size();i++){    		 
    		 currentNode = this.nodes.get(this.aggregatedNodes.get(i));
    		 for (int t=0;t<this.numTimeSlices;t++){
    			 if (i==0) {
    				 this.aggregatedPersistence.add((currentNode.coords.get(t)!=null)?1:0);
    			 }else if (this.aggregatedPersistence.get(t)==1 && currentNode.coords.get(t)==null){
    				this.aggregatedPersistence.set(t, 0);    				 				 
    			 }
    			 //System.out.println(this.aggregatedPersistence.get(t));
    		 }    		
    	 }   	
    	  	
     }
     /** Draws the hint path of node(s) selected
      * @view the current view of the visualization
      * */
     public void drawNodeHintPaths(int view){
    	 for (int i=0;i<this.aggregatedNodes.size();i++){    		 
    		 this.nodes.get(this.aggregatedNodes.get(i)).drawAggregatedHintPath(this.aggregatedPersistence,view);
    	 }
     }
     /**When multiple edges are selected and the key is released,
      * an aggregated hint path is drawn on all of them to show 
      * when they disappear/reappear at the same time slices
      * */
     public void aggregateEdgeHintPaths(){
    	 
    	 if (this.aggregatedEdges.size()==0) return;
    	 
    	 this.aggregatedPersistence.clear();    	 
    	 Edge currentEdge;
    	 for (int i=0;i<this.aggregatedEdges.size();i++){      		
    			 currentEdge =this.aggregatedEdges.get(i);
        		 for (int t=0;t<this.numTimeSlices;t++){
        			 if (i==0) {
        				 this.aggregatedPersistence.add(currentEdge.persistence.get(t));
        			 }else if (aggregatedPersistence.get(t)==1 && currentEdge.persistence.get(t)==0){
        				this.aggregatedPersistence.set(t, 0);    				 				 
        			 }
        		 } 	    		    		
    	 }   	  	
     }
     /**Draws hint paths for all selected edge(s) */
     public void drawEdgeHintPaths(){    	 
    	 for (int i=0;i<this.aggregatedEdges.size();i++){
    		 this.aggregatedEdges.get(i).drawHintPath(this.nodes, this.aggregatedPersistence);
    	 }
     }
     /** Finds an edge in an ArrayList of edges
      *  @param Arraylist of edges to search within
      *  @param e the edge to search for
      *  @return index of the edge that was found, -1 otherwise
      * */
     int findEdge(ArrayList<Edge> edges, Edge e){
		  for (int i=0;i<edges.size();i++){
			  if (e.equalTo(edges.get(i))){
				  return i;
			  }
		  }
		  return -1;
     }
     /** Finds an edge in an ArrayList of edges and removes it
      *  @param Arraylist of edges to search within
      *  @param e the edge to remove 
      * */
     ArrayList<Edge> removeEdge(ArrayList<Edge> edges, Edge e){    	 
		  for (int i=0;i<edges.size();i++){
			  if (e.equalTo(edges.get(i))){
				  edges.remove(i);
				  return edges;
			  }
		  }
		  return edges;
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
    	 Node n = null;
    	 if (this.selectedNode !=-1)
    		 n = this.nodes.get(this.selectedNode);
    	 
    	 
    	 for (int row = 0;row<this.edges.size();row++){     
    		 if (this.selectedNode!=-1 && n.incidentEdges.contains(row) && this.selectedEdge==-1){    		    
    		     this.edges.get(row).animate(this.nodes, start, end, interpolation,true);     		    
    		 }else{
    			 this.edges.get(row).animate(this.nodes, start, end, interpolation,false);    	 
    		 } 	                 	   	
 	     }    	 
    	
    	 for (int i = 0;i<this.nodes.size();i++){
  		      this.nodes.get(i).animate(start, end, interpolation);	
  	     }         	
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
       	  float nodeX=0.0f, nodeY=0.0f;
       	  
       	  this.nodes = new ArrayList<Node>();
       	  this.edges = new ArrayList <Edge>	();
       	  
       	InputStream fs = this.getClass().getResourceAsStream(filename);
		scan = new Scanner(fs);
		
		while(scan.hasNext())
		{   				
			String line;
			line = scan.nextLine();
			String[] items = line.split(" ");
			if (items[0].equals("node")){ //Save the node
				nodeId = Integer.parseInt(items[1]);       					
				this.nodes.add(new Node(this.parent,nodeId,items[1],this.numTimeSlices));
				//When the node positions are fixed....
				nodeX = Float.parseFloat(items[2]);
				nodeY = Float.parseFloat(items[3]);
			}else if (items[0].equals("time")){ //Save the time slice
				nodesDone = true;    
				time = Integer.parseInt(items[1]);       				
			}else{
				if (nodesDone){ //Save the edge information       						
					newEdge = new Edge (this.parent,"",Integer.parseInt(items[0]),Integer.parseInt(items[1]),this.numTimeSlices);
					foundEdge = findEdge(this.edges,newEdge);
					if (foundEdge==-1){ //If edge doesn't exist, create it       							
						newEdge.persistence.set(time, 1);
						this.edges.add(newEdge);
					}else{//Otherwise just update the persistence info
						this.edges.get(foundEdge).persistence.set(time, 1);
					}           					
				}else{//Save the node coordinates
					//When the node postions are changing 
					/**if (items[0].equals("null")){ //Node does not appear for this time slice
						this.nodes.get(nodeId).coords.add(null);
					}else{           						
						this.nodes.get(nodeId).coords.add(new Coordinate(Float.parseFloat(items[0]),Float.parseFloat(items[1])));
					} */        
					//When the node positions are fixed
					if (items[0].equals("0")){ //Node does not appear for this time slice
						this.nodes.get(nodeId).coords.add(null);
					}else{           						
						this.nodes.get(nodeId).coords.add(new Coordinate(nodeX,nodeY));
					}
				}
			}       				
			
		}
       	  
       	  //Set the node degree changes over time
       	  for (int i=0;i<this.nodes.size();i++){
       		  this.nodes.get(i).setNodeDegree(this.edges);
       	  }       	 
     }
}
