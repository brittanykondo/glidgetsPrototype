import java.util.ArrayList;
import processing.core.*;
/**A node on a graph, for displaying it and saving important properties
 * */

//TODO: need fast look up of when node disappears and re-appears from view (e.g, coords at a view is null)
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
      
      //Class Constants
      static int MIN_WEIGHT = 2;
      static int MAX_WEIGHT = 8;
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
    			  for (int t=0;t<this.numTimeSlices;t++){
    				  if (currentEdge.persistence.get(t)==1){
    					  degreeCount = this.degrees.get(t)+1;
    					  if (degreeCount>this.maxDegree) this.maxDegree = degreeCount;
    					  this.degrees.set(t, degreeCount);
    				  }
    			  }
    		  }
    	  }
    	  //System.out.println(this.maxDegree);
      }
      
      /** Renders the node at the specified position and saves the positions in class variables
       *  @param x,y the position coordinates
       *  @param alpha the amount of transparency (0 to 255)
       * */
      void drawNode(float x, float y,int alpha){    	  
    	  
    	  parent.fill(206,18,86,alpha);
    	  parent.noStroke();
    	  parent.ellipse(x,y,RADIUS,RADIUS);  
    	  
    	  PFont font = parent.createFont("Arial",12,true);
	   	  parent.textFont(font);	   	  
	   	  parent.fill(247,244,249,alpha);
	   	  parent.text(this.label, x,y);	   	
	   	 
    	  this.x = x;
	   	  this.y = y;
      }
      /** Visualizes the overall node persistence (how often is it displayed?) 
       *  at a certain time slice
       * */
      //TODO: what about nodes that are not in the view?
      void displayGlobalPersistence(int view){    	  
    	  if (this.coords.get(view)!=null){
    		  this.globalPersistence = calculateGlobalPersistence();  
    		  drawNode(this.coords.get(view).x, this.coords.get(view).y,(int)(255*this.globalPersistence));   
    	  }     	  
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
       * @param selectedNode the currently selected node (if any)
       * @return index of the selected, -1 otherwise
       * */
      int selectNode(int view,int selectedNode){
    	  Coordinate coord = this.coords.get(view);    	 
    	  if (coord !=null && parent.mousePressed && parent.dist(coord.x,coord.y,parent.mouseX,parent.mouseY)<=RADIUS && this.id!=selectedNode){    			    		 
    		  return this.id;
    	  }    	 
    	  return -1;
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
       * */
      void drawHintPath(){
    	  float interval = parent.TWO_PI/this.numTimeSlices;       	 
    	  float startAngle, endAngle;
    	  int alpha;
    	  for (int i=0;i<this.numTimeSlices;i++){
    		  startAngle = i*interval;
    		  endAngle = startAngle + interval;
    		  if (this.coords.get(i)==null){
    			  parent.stroke(189, 189, 189);
    			  parent.strokeWeight(MIN_WEIGHT);
    		  }else{
    			  //alpha= (int)(((float)this.degrees.get(i)/this.maxDegree)*255);    			  
    			  parent.stroke(250, 159, 181,255);    	   
    			  parent.strokeWeight(MIN_WEIGHT+(int)(((float)this.degrees.get(i)/this.maxDegree)*MAX_WEIGHT));
    		  }    		  
        	  
        	  parent.strokeCap(parent.SQUARE);
        	  parent.noFill();
        	  parent.arc(this.x, this.y, RADIUS+5, RADIUS+5, startAngle, endAngle);
    	  }    	 
      }
      
      /**Animates a node by interpolating its position between two time slices
       * @param start the starting time slice
       * @param end the ending time slice
       * @interpolation the amount to interpolate by
       * */
      void animate(int start,int end, float interpolation){
    	  Coordinate startPosition = this.coords.get(start);
    	  Coordinate endPosition = this.coords.get(end);
    	  float x0,y0,x1,y1,x,y;
    	  if (startPosition != null && endPosition !=null){
    		  x0 = startPosition.x;
    		  y0 = startPosition.y;
    		  x1 = endPosition.x;
    		  y1 = endPosition.y;
    		  x = (x1 - x0)*interpolation + x0;
    		  y = y0 +(y1-y0)*((x-x0)/(x1-x0));    		 
    		  drawNode(x,y,255);   		  
    	  }else if (startPosition==null && endPosition!=null){ //Node is fading in    		  
    		  drawNode(endPosition.x,endPosition.y,(int)(interpolation*255));
    	  }else if (startPosition!=null && endPosition==null){ //Node is fading out
    		  drawNode(startPosition.x,startPosition.y,(int)(interpolation*255));
    	  }    
    	  //TODO: try to offset fading effect closer to when the time slice is switched
      }       
}
