import java.util.ArrayList;
import processing.core.*;
/**A node on a graph, for displaying it and saving important properties
 * */

public class Node {
      float x, y;      
      int id;
      String label;
      double value;     
    
      PApplet parent;
      ArrayList<Coordinate> coords; //Ordered by time
      ArrayList<Integer> degrees; //Ordered by time, degree of the node
      float globalPersistence; //Percentage of time the node does not disappear
      int numTimeSlices;      
      boolean dragging;
      int maxDegree; //For scaling visualizations of relative degree amount (only w.r.t to this node's degree changes)
      ArrayList <Coordinate> hintAngles; //View angles along the hint path
      ArrayList <Integer> incidentEdges; //Edges that the node forms with other nodes over time
      
      //Class Constants
      static int MIN_WEIGHT = 4;
      static int MAX_WEIGHT = 15;
      static final float RADIUS = 30;
      
      /** Constructor for creating a Node
       * @param p Reference to processing applet for drawing with processing commands
       * @param id unique id to discern nodes
       * @param l label to be drawn inside the node
       * @param t  the number of time slices in the dataset
       * */
      Node(PApplet p,int id,String l,int t){
    	   
    	  //Save the parameters
    	  this.label = l;
    	  this.parent = p;
    	  this.id = id;    
    	  this.numTimeSlices = t; 
    	  
    	  //Initialize other class variables
    	  this.x = 0;
    	  this.y = 0;  
    	  this.dragging = false;    	  
    	  this.coords = new ArrayList<Coordinate>();
    	  this.degrees = new ArrayList<Integer>();
    	  this.maxDegree = -1;
    	  setHintAngles();
      }        
      
      /**Renders the node at its position at a certain moment in time
       * */
      void display(int view){       	
    	  if (this.coords.get(view)!=null){
    		  drawNode(this.coords.get(view).x, this.coords.get(view).y,255);    		  
    	  }    	  	  
      }
      /**Finds and saved the degree (number of edges incident on the node) of the node
       * for each time slice
       * @param an array of all edges present in the graph (for each time slice) 
       * */
      void setNodeDegree(ArrayList<Edge> edges){
    	  this.incidentEdges = new ArrayList <Integer>();
    	  //Initialize the array for saving degree over time
    	  for (int i=0;i<this.numTimeSlices;i++){
    		  this.degrees.add(0);
    	  }
    	  //For each time slice, count the number of edges containing the node
    	  Edge currentEdge;
    	  int degreeCount;    	  
    	  
    	  for (int i=0;i<edges.size();i++){
    		  currentEdge = edges.get(i);
    		  if (currentEdge.node1==this.id || currentEdge.node2 == this.id){    
    			  this.incidentEdges.add(i);    			  
    			  for (int t=0;t<this.numTimeSlices;t++){
    				  if (currentEdge.persistence.get(t)==1){
    					  degreeCount = this.degrees.get(t)+1;
    					  if (degreeCount>this.maxDegree) this.maxDegree = degreeCount;
    					  this.degrees.set(t, degreeCount);
    				  }
    			  }
    		  }
    	  }    	 
      }
      /**Saves the hint path angles which separate different views along the hint path
       * (to check when the mouse is dragging along it)
       * */
      void setHintAngles(){
    	  this.hintAngles = new ArrayList<Coordinate>();
    	  float interval = parent.TWO_PI/(this.numTimeSlices-1);       	 
    	  float startAngle, endAngle;    	 
    	  for (int i=0;i<this.numTimeSlices-1;i++){    		  
    		  //First, calculate the angles for the current time interval
    		  startAngle = (i*interval);    		  
    		  endAngle = (startAngle + interval);       		 
    		  
    		  //Add the start and end angles for the current time slice (i), off-setting it by half pi
    		  //So that the beginning of time is at the top of the node (like a clock)
    		  
    		  this.hintAngles.add(new Coordinate(startAngle, endAngle));
    		  //System.out.println(i+" "+startAngle*180/Math.PI+" "+endAngle*180/Math.PI);
    	  }
      }

      /** Renders the node at the specified position and saves the positions in class variables
       *  @param x,y the position coordinates
       *  @param alpha the amount of transparency (0 to 255)
       * */
      void drawNode(float x, float y,int alpha){    	  
    	  
    	  parent.fill(206,18,86,alpha);
    	  parent.noStroke();
    	  parent.ellipse(x,y,RADIUS,RADIUS);  
    	  
    	  PFont font = parent.createFont("Droid Sans",12,true);
	   	  parent.textFont(font);	   	  
	   	  parent.fill(247,244,249,alpha);
	   	  parent.textAlign(parent.CENTER);
	   	  parent.text(this.label, x,y+4);  	   	  
	   	 
    	  this.x = x;
	   	  this.y = y;
      }
      /** Visualizes the overall node persistence (how often is it displayed?) 
       *  at a certain time slice
       * */     
      void displayGlobalPersistence(int view){    	  
    	  if (this.coords.get(view)!=null){
    		  this.globalPersistence = calculateGlobalPersistence();  
    		  drawGlobalPersistenceHighlights();    		     
    	  }     	  
      }
      /** Adds highlights to the nodes to show global persistence
       * */       
      void drawGlobalPersistenceHighlights(){
    	  //Piechart Glyph:
    	  //parent.stroke(206,18,86,170);   
    	  parent.stroke(67,162,202,255);   
    	  parent.strokeWeight(MIN_WEIGHT);
    	  parent.strokeCap(parent.SQUARE);
    	  parent.noFill();   
    	  float startAngle = 0;
    	  float endAngle = parent.TWO_PI*this.globalPersistence;
    	  parent.arc(this.x, this.y, RADIUS+MIN_WEIGHT, RADIUS+MIN_WEIGHT, startAngle-parent.HALF_PI, endAngle-parent.HALF_PI);        	  
      }
      /**Calculates the overall persistence: 
       * (number of time slices - number of disappearances)/number of time slices
       * @return the global persistence measure (as probability)
       * */
      float calculateGlobalPersistence(){    	  
    	  int disappearanceCount = 0;
    	  for (int i=0;i<this.coords.size();i++){
    		  if (this.coords.get(i)==null) disappearanceCount++;
    	  }    	 
    	  return (float)(this.numTimeSlices - disappearanceCount)/this.numTimeSlices;
      }
      
      /**Checks to see if the mouse event is in the node's circular area
       * @param view  the current view       
       * @return index of the selected, -1 otherwise
       * */
      int [] selectNode(int view){
    	  Coordinate coord = this.coords.get(view);    	 
    	  if (coord !=null && parent.mousePressed && parent.dist(coord.x,coord.y,parent.mouseX,parent.mouseY)<=(RADIUS/2)){  //Clicking directly on a node  			    		 
    		  return new int [] {this.id,1};
    	  } else if (coord !=null && parent.mousePressed && parent.dist(coord.x,coord.y,parent.mouseX,parent.mouseY)<=(RADIUS/2)+MAX_WEIGHT){  //Drawing around a node or clicking near it  			    		 
    		  return new int [] {this.id,2};
    	  }   	 
    	  return new int [] {-1,-1};
      }
      
      /**Checks to see if the mouse up event is in the node's circular area
       * @param view  the current view
       * @param selectedNode the currently selected node (if any)
       * @return index of the released if it is a different node than what is selected
       *  -1 otherwise 
       * */
      int releaseNode(int view, int selectedNode){
    	  Coordinate coord = this.coords.get(view);    	  
    	  if (coord != null && parent.dist(coord.x,coord.y,parent.mouseX,parent.mouseY)<=RADIUS && this.id!=selectedNode){    		 
    		  return this.id;   		  
    	  }    	 
    	  return -1;
      }
     
      /** Visualizes the node persistence across all time slices to guide interaction        
       *  @param currentView current time slice of the visualization       
       * */      
      void drawHintPath(int currentView,float interpolation){    	 
    	  //int alpha;
    	  float startAngle,endAngle,weight;
    	  for (int i=0;i<this.numTimeSlices-1;i++){
    		 
    		  if (this.coords.get(i)==null){
    			  parent.stroke(189, 189, 189,255);
    			  parent.strokeWeight(MIN_WEIGHT);
    			  weight = MIN_WEIGHT;
    		  }else{
    			  //alpha= (int)(((float)this.degrees.get(i)/this.maxDegree)*255); Experiment with transparency encoding the persistence   			  
    			  //parent.stroke(206,18,86,170); 
    			  parent.stroke(67,162,202,255); 
    			  weight = MIN_WEIGHT+(int)(((float)this.degrees.get(i)/this.maxDegree)*MAX_WEIGHT);
    			  parent.strokeWeight(weight);
    		  }    		  
        	  
        	  parent.strokeCap(parent.SQUARE);
        	  parent.noFill();   
        	  startAngle = this.hintAngles.get(i).x;
        	  endAngle = this.hintAngles.get(i).y;
        	  parent.arc(this.x, this.y, RADIUS+weight, RADIUS+weight, startAngle-parent.HALF_PI, endAngle-parent.HALF_PI);          	  
        	 
    	  }    	 
      }
      /**Animates the anchor around the hint path (when dragging around a selected node)
       * @param mouseAngle the angle to draw the anchor at 
       * @param fixAnchor 1 if anchor should be fixed at the angle, 0 otherwise
       * */
      void animateHintPath(float mouseAngle,int fixAnchor){
    	  float startAngle,endAngle,weight;
    	  for (int i=0;i<this.numTimeSlices-1;i++){
    		 
    		  if (this.coords.get(i)==null){
    			  parent.stroke(189, 189, 189,255);
    			  parent.strokeWeight(MIN_WEIGHT);
    			  weight = MIN_WEIGHT;
    		  }else{    			  		  
    			  //parent.stroke(206,18,86,170); 
    			  parent.stroke(67,162,202,255);
    			  weight = MIN_WEIGHT+(int)(((float)this.degrees.get(i)/this.maxDegree)*MAX_WEIGHT);
    			  parent.strokeWeight(weight);
    		  }    		  
        	  
        	  parent.strokeCap(parent.SQUARE);
        	  parent.noFill();   
        	  startAngle = this.hintAngles.get(i).x;
        	  endAngle = this.hintAngles.get(i).y;
        	  parent.arc(this.x, this.y, RADIUS+weight, RADIUS+weight, startAngle-parent.HALF_PI, endAngle-parent.HALF_PI);      	   
    	  }
    	  
    	  //Animate the anchor
    	  //parent.stroke(206,18,86,255); 
    	  parent.stroke(67,162,202,255); 
          parent.strokeWeight(4);          
          float x1 = (float) (this.x + RADIUS/2*Math.cos(mouseAngle));
          float y1 = (float) (this.y + RADIUS/2*Math.sin(mouseAngle));     
          
          if (fixAnchor==1){
        	  parent.line(x1, y1, x1, parent.mouseY);
          }else{
        	  parent.line(x1, y1, parent.mouseX, parent.mouseY);
          }
          
      }
     
      /** Draws an aggregated hint path (following a persistence array)
       *  @param currentView current time slice of the visualization
       *  @param interpolation amount to animate the anchor by (if visible)     
       *  @param persistence Array of persistence information for the aggregated nodes  
       * */      
      void drawAggregatedHintPath(int currentView,float interpolation,ArrayList<Integer> persistence){    	  	 
    	  // parent.strokeWeight(5);
    	  for (int i=0;i<this.numTimeSlices-1;i++){ 
    		  //For now, just drawing the original degree amount when node is present, not aggregating it
    		  float weight;
    		  if (persistence.get(i)==0){
    			  parent.stroke(189, 189, 189,255);
    			  parent.strokeWeight(MIN_WEIGHT);
    			  weight = MIN_WEIGHT;
    		  }else{    			  
    			  parent.stroke(67,162,202,255); 
    			  weight = MIN_WEIGHT+(int)(((float)this.degrees.get(i)/this.maxDegree)*MAX_WEIGHT);
    			  parent.strokeWeight(weight);
    		  }    		     		  
        	  
        	  parent.strokeCap(parent.SQUARE);
        	  parent.noFill();
        	//Offset by half pi so that beginning of time lies on top of the node (like a clock layout)
        	  parent.arc(this.x, this.y, RADIUS+weight, RADIUS+weight, this.hintAngles.get(i).x-parent.HALF_PI, this.hintAngles.get(i).y-parent.HALF_PI);        	
    	  }    	 
      }
      /**Animates a node by interpolating its position between two time slices
       * @param start the starting time slice
       * @param end the ending time slice
       * @param interpolation the amount to interpolate by
       * @param pinned the node that should be pinned during the animation (set to -1 if none)
       * @param pinnedView the view to pin their position to (set to -1 if none)
       * */
      void animate(int start,int end, float interpolation,int [] pinned,int pinnedView){
    	  Coordinate startPosition = this.coords.get(start);
    	  Coordinate endPosition = this.coords.get(end);    	  
    	  Coordinate interpPosition;
    	 
    	  if (startPosition != null && endPosition !=null){    		
    		  if (pinned[0] == this.id || pinned[1]==this.id){    			  
    			  drawNode(this.coords.get(pinnedView).x,this.coords.get(pinnedView).y,255); 
    		  }else{
    			  interpPosition = interpolatePosition(startPosition,endPosition,interpolation);
        		  drawNode(interpPosition.x,interpPosition.y,255); 
    		  }
    	  }else if (startPosition==null && endPosition!=null){ //Node is fading in    		  
    		  drawNode(endPosition.x,endPosition.y,(int)(interpolation*255));
    	  }else if (startPosition!=null && endPosition==null){ //Node is fading out
    		  drawNode(startPosition.x,startPosition.y,(int)(interpolation*255));
    	  }     	  
      }  
      
      /**Linearly interpolates the position of a node 
       * @param c0 first coordinate of a point on the line
       * @param c1 second coordinate of a point on the line
       * @return Coordinate: new coordinates using the lerp formula
       * */
      Coordinate interpolatePosition(Coordinate c0, Coordinate c1, float interpAmount){
    	  float x0 = c0.x;
    	  float y0 = c0.y;
    	  float x1 = c1.x;
    	  float y1 = c1.y;
    	  float x = (x1 - x0)*interpAmount + x0;
		  float y = y0 +(y1-y0)*((x-x0)/(x1-x0)); 
		  return new Coordinate(x,y);
      }
}
