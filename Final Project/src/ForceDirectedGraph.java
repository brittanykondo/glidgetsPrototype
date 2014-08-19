import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import java.util.Arrays;
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
     public ArrayList <Integer> aggregatedEdges_Nodes; //All nodes attached to selected edges
     public ArrayList<Integer> aggregatedPersistence;    
     public ArrayList <String> timelineLabels; //Labels to appear along the time slider + embedded slider
     
     public boolean inGlobalView;
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
     public int aggregate;
     
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
    	 this.aggregate = 0;
    	
    	 this.aggregatedNodes = new ArrayList<Integer>();
    	 this.aggregatedEdges = new ArrayList<Edge>();
    	 this.aggregatedEdges_Nodes = new ArrayList<Integer>();
    	 this.aggregatedPersistence = new ArrayList<Integer>();
    	 
    	 this.keyPressed = -1;
    	 this.draggingEdge = null;
    	 this.onDraggingEdge = false;
    	 this.inGlobalView = false;    	
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
    	
	     if (this.aggregatedEdges.size()>0){ //Case 1: Need to draw an edge hint path	    	 
			 renderEdges(view);
			 renderNodes(view);				
			 drawEdgeHintPaths(this.drawingView);
		 }else if (this.aggregatedNodes.size()>0){ //Case 2: draw node hint paths			 
			 renderEdges(view);				
			 drawNodeHintPaths(view);			 
			 renderNodes(view);			 
		 }else{ //Case 3: Just render both edges and nodes without hint paths
			 //clearQueries();			 
			 renderEdges(view);
			 renderNodes(view);		 
		 }		   	      
     } 
     /**Draws all elements that ever existed in the network
      * */
     public void drawAllElements(){
    	 for (int i = 0;i<this.edges.size();i++){
  		      this.edges.get(i).display(this.nodes,-1,true,true);		       	
	     }   	 
	   	 for (int i = 0;i<this.nodes.size();i++){	   		   
	   		   this.nodes.get(i).display(-1,this.aggregatedNodes,1,false); 
	   	  } 
     }
     /**Updates the node's position when the user drags it to a new positon (in drag and
      * drop mode)
      * */
     public void updateNodePosition(float x,float y){
    
    	 //First see which node is selected    	 
    	 int nodeId = -1;
    	 Node currentNode;
    	 for (int i = 0;i<this.nodes.size();i++){
     		currentNode = this.nodes.get(i);   
     		if (parent.dist(x, y, currentNode.x, currentNode.y)<=(currentNode.RADIUS/2)){
     			nodeId = currentNode.id;
     			break;
     		}            
     	 }   
    	 
    	 if (nodeId==-1) return;
    	 
    	 //Then, update the position in array of nodes
    	 this.nodes.get(nodeId).x = x;
    	 this.nodes.get(nodeId).y = y;    	 
     }
     
     /**Finds the min distance from the mouse point to a point on the line and decides if the edge
      * is selected and dragged along
      * */
     public void selectEdge(){     	 
    	 //if (this.draggingEdge ==null) return;
    	 this.onDraggingEdge = false;
    	 
    	 for (int i=0;i<this.aggregatedEdges.size();i++){
    		 Coordinate startPt = this.aggregatedEdges.get(i).hintCoords.get(0);
        	 Coordinate endPt = this.aggregatedEdges.get(i).hintCoords.get(this.numTimeSlices);    	 
        	 float [] minPoint = minDistancePoint(parent.mouseX,parent.mouseY,startPt.x,startPt.y,endPt.x,endPt.y);
        	 if (parent.dist(minPoint[0],minPoint[1],parent.mouseX,parent.mouseY)<=10){ //Rough check to see if mouse was clicked on the anchor
        		 this.onDraggingEdge = true;
        		 this.inGlobalView = false;
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
    	 //releaseEdgeAnchor();
    	 this.onDraggingEdge = false;
    	 this.dragging = false;
    	 this.draggingEdge = null;    	
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
         
         this.drawingView = this.currentView;
         //Check if mouse is beyond the last view point
         if (minDist[2]>1 && this.nextView==(this.numTimeSlices-1)){
        	 Coordinate lastPoint = this.draggingEdge.hintCoords.get(this.numTimeSlices);
        	 float [] minDist2 = minDistancePoint(parent.mouseX,parent.mouseY,pt2.x,pt2.y,lastPoint.x,lastPoint.y);
        	 if (minDist2[2]<1){ //Beyond the second last point (pt2)
        		 newPoint= new Coordinate (minDist2[0],minDist2[1]);
        	 }else{
        		 newPoint = new Coordinate(lastPoint.x,lastPoint.y);
        	 }
        	 this.drawingView = this.nextView;
         }      
         
         this.animateGraph(this.currentView, this.nextView, this.interpAmount);	            
         this.draggingEdge.animateAnchor(newPoint.x,newPoint.y,this.timelineLabels.get(this.drawingView));        	 
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
		    	this.drawingView = this.nextView;
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
	    	 this.drawingView = this.currentView;
    	}    	 
     }
     /** Checks where the mouse is w.r.t the hint path
      * */
     public void dragAroundNode(){   	
    	 if (this.selectedNode==-1) return;
    	
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
	   	int fixAnchor = 0;
		   	
	   	if (this.nextView == (this.numTimeSlices-1) && mouseAngle < Math.PI && mouseAngle >= 0 
     			&& this.mouseAngle <= parent.TWO_PI && this.mouseAngle > Math.PI){ //Prevent wrapping around from last view to first vie
	   		fixAnchor = 1;
	   		mouseAngle = (float) (Math.PI*2 - 0.02);	   	   		
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
	   	
	 	float lastAngle = n.hintAngles.get(this.numTimeSlices-1).x;	   	
	    //System.out.println(this.currentView+" "+this.nextView+" "+this.drawingView+" "+this.interpAmount);	       
        animateGraph(this.currentView, this.nextView, this.interpAmount);
        if (this.nextView == (this.numTimeSlices-1) && 
 	   			(this.mouseAngle>=lastAngle && this.mouseAngle<=parent.TWO_PI)){ //Detect when last view is entered
 	   		this.drawingView = nextView;   		
 	   	}else{
        	this.drawingView = this.currentView;
        	
        }       
        n.animateAnchor(mouseAngle-parent.HALF_PI,fixAnchor,this.timelineLabels.get(this.drawingView));
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
		 for (int i = 0;i<this.nodes.size();i++){
			 if (this.aggregatedEdges_Nodes.size()>0){
				 this.nodes.get(i).display(view,this.aggregatedEdges_Nodes,1,true); 
			 }else{
				 this.nodes.get(i).display(view,this.aggregatedNodes,1,false); 				
			 }
		 }
			 
     }
     
     /**Draws all the edges on the screen for the specified view
      * @param view  the current view of the visualization
      * */
     public void renderEdges(int view){
    	ArrayList<Integer> edgesToHighlight = findIncidentEdgesToHighlight();
    	 	
    	 for (int row = 0;row<this.edges.size();row++){
    		 if (edgesToHighlight.contains(row) && this.selectedEdge==-1){    		    
    		     this.edges.get(row).display(this.nodes, view, false,true);     		    
    		 }else{
    			 this.edges.get(row).display(this.nodes, view, false,false);    	 
    		 } 
    	 }
			 
     }
     /**In the current aggregated node query, finds the incident edges that should be highlighted
      * */
     public ArrayList<Integer> findIncidentEdgesToHighlight(){
    	 ArrayList<Integer>edges = new ArrayList<Integer>();
    	 ArrayList<Integer>edgesToHighlight = new ArrayList<Integer>();
    	 for (int i=0;i<this.aggregatedNodes.size();i++){
    		 edges=this.nodes.get(this.aggregatedNodes.get(i)).incidentEdges;
    		 for (int j=0;j<edges.size();j++){
    			 if (!edgesToHighlight.contains(edges.get(j)))
    			      edgesToHighlight.add(edges.get(j));
    		 }
    	 }
    	 return edgesToHighlight;
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
    	 boolean selectedDisappeared = false;
    	 
    	 for (int i = 0;i<this.nodes.size();i++){
    		 if (aggregatedNodes.contains(new Integer(i))){
    			 selectedDisappeared = true;
    		 }else{
    			 selectedDisappeared = false;
    		 }
    		 if (this.inGlobalView){
    			 selectedDisappeared = true;
    		 }
    		savedSelections = this.nodes.get(i).selectNode(this.drawingView,selectedDisappeared);  
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
                	//this.aggregatedEdges.clear();
                	//System.out.println("clearing edges");
                	//this.aggregatedEdges_Nodes.clear();       		
            	
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
    		 this.inGlobalView = false;
    		 this.selectedSameNode = false;
    	 }
     }
     /**Handles the mouse up listener for all nodes in the graph.
      * If the mouse up event is on a different node than what is
      * selected, want to draw the edge hint path in between nodes    
      * */
     public void releaseNodes(){    	      		
    	 if (!this.draggingNode && this.selectedSameNode){
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
	    		released = this.nodes.get(i).releaseNode(this.drawingView,this.selectedNode,this.inGlobalView);
	    		if (released != -1) this.releasedNode = released;    		
	      }		
	    	 
	     if (this.selectedNode != -1 && this.releasedNode !=-1){    		 
	    	connectNodes();
	    }
     }        
     /**Re-sets the selected and released node after the edge hint path is drawn.
      * Draws the hint path for the edge joined by selected and released node
      * */
     public void connectNodes(){    	
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
    	     this.aggregatedEdges_Nodes.remove(new Integer(toAdd.node1));
    	     this.aggregatedEdges_Nodes.remove(new Integer(toAdd.node2));
         }else if (!(this.inGlobalView && this.selectedEdge==-2)){
        	 this.aggregatedEdges.add(toAdd);        	 
        	 this.aggregatedEdges_Nodes.add(toAdd.node1);
        	 this.aggregatedEdges_Nodes.add(toAdd.node2);
         }
         this.aggregateEdgeHintPaths();         
     }
     /**Clears all queries on the screen (this is triggered when the background is clicked)
      * */
     public void clearQueries(){      	
    	this.aggregatedNodes.clear();
    	this.aggregatedPersistence.clear();
    	this.aggregatedEdges.clear();  
    	this.aggregatedEdges_Nodes.clear();
    	this.selectedEdge = -1;
    	this.selectedNode = -1;
    	this.releasedNode = -1;
     }
 
     /**When multiple nodes have been selected, and the user has specified aggregation, an aggregated hint path is drawn on all of them to show 
      * when they disappear/reappear at the same time slices. Otherwise, glyphs are revealed independently
      * */
     public void aggregateNodeHintPaths(){
    	 
    	 if (this.aggregatedNodes.size()==0) return; //No hints to show or aggregate

    	 this.aggregatedPersistence.clear();    	 
    	 Node currentNode;
    	 for (int i=0;i<this.aggregatedNodes.size();i++){    		 
    		 currentNode = this.nodes.get(this.aggregatedNodes.get(i));
    		 for (int t=0;t<this.numTimeSlices;t++){
    			 if (i==0) {
    				 this.aggregatedPersistence.add(currentNode.persistence.get(t));
    			 }else if (this.aggregatedPersistence.get(t)==1 && currentNode.persistence.get(t)==0){
    				this.aggregatedPersistence.set(t, 0);    				 				 
    			 }    			
    		 }    		
    	 }   	    	  	
     }
     /** Draws the hint path of node(s) selected
      * @view the current view of the visualization
      * */
     public void drawNodeHintPaths(int view){      	
    	 for (int i=0;i<this.aggregatedNodes.size();i++){     		 
    		 if (this.aggregate ==0){    			
    			 this.nodes.get(this.aggregatedNodes.get(i)).drawHintPath(view);
    		 }else{    			
    			 this.nodes.get(this.aggregatedNodes.get(i)).drawAggregatedHintPath(this.aggregatedPersistence,view);
    		 }    		
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
     public void drawEdgeHintPaths(int view){ 
    	 Edge e;
    	 for (int i=0;i<this.aggregatedEdges.size();i++){
    		 if (this.aggregate ==0){ //Reveal glyphs independently
    			 e = this.aggregatedEdges.get(i);
    			 this.aggregatedEdges.get(i).drawHintPath(this.nodes, e.persistence,view,this.aggregate);
    		 }else{ //Aggregate glyphs
    			 this.aggregatedEdges.get(i).drawHintPath(this.nodes, this.aggregatedPersistence,view,this.aggregate);
    		 }   		 
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
      * */
     public void animateGraph(int start, int end, float interpolation){
    	 ArrayList<Integer>edges = new ArrayList<Integer>();
    	 ArrayList<Integer>edgesToHighlight = new ArrayList<Integer>();
    	 for (int i=0;i<this.aggregatedNodes.size();i++){
    		 edges=this.nodes.get(this.aggregatedNodes.get(i)).incidentEdges;
    		 for (int j=0;j<edges.size();j++){
    			 if (!edgesToHighlight.contains(edges.get(j)))
    			      edgesToHighlight.add(edges.get(j));
    		 }
    	 }   	 
    	 for (int row = 0;row<this.edges.size();row++){     
    		 if (edgesToHighlight.contains(row) && this.selectedEdge==-1){    		    
    		     this.edges.get(row).animate(this.nodes, start, end, interpolation,true);     		    
    		 }else{
    			 this.edges.get(row).animate(this.nodes, start, end, interpolation,false);    	 
    		 } 	                 	   	
 	     } 
    	
    	 //Only draw one type of hint path at a time
    	 if (this.aggregatedEdges.size() >0){    		
    		 drawEdgeHintPaths(this.drawingView);
    	 }else{
    		 drawNodeHintPaths(this.drawingView);
    	 }  	   	 
    	 
    	 for (int i = 0;i<this.nodes.size();i++){
    		  if (this.aggregatedEdges_Nodes.size()>0){
    			  this.nodes.get(i).animate(start, end, interpolation,this.aggregatedEdges_Nodes,true);
    		  }else{
    			  this.nodes.get(i).animate(start, end, interpolation,this.aggregatedNodes,false);    			  
    		  }  		      	
  	     }    	      	
     }     
     /** Calls functions for showing global persistence values for each node and edge
      * */
     /**public void showLocalPersistence(int view){
    	 for (int i = 0;i<this.edges.size();i++){
    		  this.edges.get(i).display(this.nodes,view,false,false);
		      this.edges.get(i).drawGlobalPersistenceHighlights(this.nodes,view,false,1);  	
	     }
    	 
    	 for (int i = 0;i<this.nodes.size();i++){
    		   this.nodes.get(i).drawGlobalPersistenceHighlights(view,false,1);
    		   this.nodes.get(i).display(view,this.aggregatedNodes,1,false); 
    	  }     	 
     } */ 
     /** Calls functions for showing global persistence values for EVERY node and edge (slider disabled)
      * */   
     public void showGlobalPersistence(){
    	 ArrayList<Integer> edgesToHighlight = findIncidentEdgesToHighlight();
    	     			 
    	 for (int i = 0;i<this.edges.size();i++){   		      
	   		   if (this.aggregatedNodes.size()>0){ //Highlight incident edges of selected nodes
	   			   if (edgesToHighlight.contains(i)){	   				  
	   				  this.edges.get(i).drawGlobalPersistenceHighlights(this.nodes,-1,true,1); 
	   			   }else{
	   				  this.edges.get(i).drawGlobalPersistenceHighlights(this.nodes,-1,true,0.2f);  
	   			   }
	   		   }else if (this.aggregatedEdges.size()>0){ //Highlight selected, aggregated edges
	   			  int found = findEdge(this.aggregatedEdges,this.edges.get(i));
	   			  if (found!=-1){
	   				this.edges.get(i).drawGlobalPersistenceHighlights(this.nodes,-1,true,1); 
	   			  }else{
	   				this.edges.get(i).drawGlobalPersistenceHighlights(this.nodes,-1,true,0.2f);  
	   			  }
	   		   }else{ //Highlight all edges
	   			  this.edges.get(i).drawGlobalPersistenceHighlights(this.nodes,-1,true,1); 
	   		   }	       	
	     }   	 
    	 
	   	 for (int i = 0;i<this.nodes.size();i++){
	   		 if (this.aggregatedNodes.size()>0){ //Highlight selected, aggregated nodes
	   			if (this.aggregatedNodes.contains(i)){
	   				this.nodes.get(i).drawGlobalPersistenceHighlights(-1,true,1);	   				 
	   			}else{
	   				this.nodes.get(i).drawGlobalPersistenceHighlights(-1,true,0.2f);	   				
	   			}	   			
	   		 }else if (this.aggregatedEdges.size()>0){//Fade all but the nodes connected to selected edges
	   			boolean found = findNodeInEdges(this.aggregatedEdges,i);
	   			if (found){
	   				this.nodes.get(i).drawGlobalPersistenceHighlights(-1,true,1);	   
	   			}else{
	   				this.nodes.get(i).drawGlobalPersistenceHighlights(-1,true,0.2f);	   
	   			}	   						 
	   		 }else{ //Highlight all nodes
	   			this.nodes.get(i).drawGlobalPersistenceHighlights(-1,true,1);	   			
	   		 } 		   
	   		 
	   	  }     				 
     }     
     /**Searches for a node ID in an array of edges
      * */
     public boolean findNodeInEdges(ArrayList<Edge> e, int nodeId){
    	 for (int i=0;i<e.size();i++){
    		 if (e.get(i).node1==nodeId || e.get(i).node2==nodeId){
    			 return true;
    		 }
    	 }
    	 return false;
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
       	  Node newNode;
       	  this.nodes = new ArrayList<Node>();
       	  this.edges = new ArrayList <Edge>	();
       	  
       	InputStream fs = this.getClass().getResourceAsStream(filename);
		scan = new Scanner(fs);
		
		while(scan.hasNext())
		{   				
			String line;
			line = scan.nextLine();
			String[] items = line.split(" ");
			//System.out.println(items[0]);
			if (items[0].equals("node")){ //Save the node
				nodeId = Integer.parseInt(items[1]); 
				if (items[4] != null){
					newNode = new Node(this.parent,nodeId,items[4],this.numTimeSlices);
				}else{
					newNode = new Node(this.parent,nodeId,""+nodeId,this.numTimeSlices);
				}				
				//When the node positions are fixed....
				nodeX = Float.parseFloat(items[2]);
				nodeY = Float.parseFloat(items[3]);				
				newNode.x = nodeX;
				newNode.y = nodeY;
				this.nodes.add(newNode);
			}else if (items[0].equals("time")){ //Save the time slice				
				nodesDone = true;    
				time = Integer.parseInt(items[1]); 				
			}else if (items[0].equals("timeline")){
				String [] labels = scan.nextLine().split(" ");			
				this.timelineLabels = new ArrayList<String>(Arrays.asList(labels));
			}else{
				if (nodesDone){ //Save the edge information       						
					newEdge = new Edge (this.parent,"",Integer.parseInt(items[0]),Integer.parseInt(items[1]),this.numTimeSlices);
					//System.out.println(Integer.parseInt(items[0])+" "+Integer.parseInt(items[1]));
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
						this.nodes.get(nodeId).persistence.add(0);
					}else{           						
						this.nodes.get(nodeId).persistence.add(1);
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
