import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;

public class Edge {
      int node1, node2; //Id's of the connected nodes 
      String label;
      PApplet parent;
      ArrayList<Integer> persistence; //1 if the edge is drawn, 0 if it disappears
      float globalPersistence;
      int numTimeSlices;    
      ArrayList <Coordinate> hintCoords; //Stores the coordinates along the hint path
      
      
      Edge(PApplet p,String l, int n1, int n2, int numTimeSlices){
    	  this.parent = p;
    	  this.label = l;
    	  this.node1 = n1;
    	  this.node2 = n2;
    	  if (numTimeSlices>0){//Initialize the array storing persistence information
    		  this.persistence = new ArrayList <Integer>();
    		  for (int i=0;i<numTimeSlices;i++){
    			  this.persistence.add(0);
    		  }
    	  }
    	  this.numTimeSlices = numTimeSlices;   
    	  this.hintCoords = new ArrayList <Coordinate>();
      }
      /**Draws an edge at a certain moment in time
       * @param nodes indexed list of all nodes in the graph
       * @param view the current time slice to draw
       * */
      void display(ArrayList<Node> nodes, int view){   	     	 
    	  Node n1 = nodes.get(this.node1);
    	  Node n2 = nodes.get(this.node2);
    	  if (n1.coords.get(view)!=null && n2.coords.get(view)!=null && this.persistence.get(view)!=0){ //Safety Check
    		  drawEdge(n1.coords.get(view).x,n1.coords.get(view).y,n2.coords.get(view).x,n2.coords.get(view).y,1,1);
    	  }
      } 
      /** Visualizes the overall edge persistence (how often is it displayed?) 
       *  at a certain time slice
       *  @param nodes all nodes in the graph
       *  @param view  the view to draw the edges at
       * */
      //TODO: what about edges that are not in the current view?
      void displayGlobalPersistence(ArrayList<Node> nodes,int view){   
    	  Node n1 = nodes.get(this.node1);
    	  Node n2 = nodes.get(this.node2);
    	  if (n1.coords.get(view)!=null && n2.coords.get(view)!=null && this.persistence.get(view)!=0){
    		  this.globalPersistence = calculateGlobalPersistence();  
    		  //Stroke thickness: 
    		  drawEdge(n1.coords.get(view).x,n1.coords.get(view).y,n2.coords.get(view).x,n2.coords.get(view).y,1,(float)(this.globalPersistence*10));      		
    	  }     	  
      }
      /** Adds highlights to the edges to show global persistence
       * */  
      //TODO: experiment with different designs
      void drawGlobalPersistenceHighlights(){
    	          	  
      }
      /**Calculates the overall persistence: 
       * (number of time slices - number of disappearances)/number of time slices
       * @return the global persistence measure (as probability)
       * */
      float calculateGlobalPersistence(){    	  
    	  int disappearanceCount = 0;
    	  for (int i=0;i<this.persistence.size();i++){
    		  if (this.persistence.get(i)==0) disappearanceCount++;
    	  }    	 
    	  return (float)(this.numTimeSlices - disappearanceCount)/this.numTimeSlices;
      }     
  
      /** Checks to see if two edges are equal.
       *  Two edges are considered equal if they connected to the same nodes
       *  @param e the edge to compare with this (calling object)
       *  @return true if they are equal, false otherwise
       * */
      boolean equalTo(Edge e){
    	  if ((this.node1 == e.node1 && this.node2==e.node2) || 
    			  (this.node1==e.node2 && this.node2 == e.node1)){ //Consider repeated edges
    		  return true;
    	  }
    	  return false;
      }
      
      /**Prints the contents of an edge */
      void print(){
    	  System.out.println("Id "+this.label+" node 1: "+this.node1+" node 2: "+this.node2);
      }
     
      /**Animates an edge by re-drawing it according to the interpolated position of
       * the nodes it is attached to
       * @param nodes an array of all nodes in the graph
       * @param start the starting time slice
       * @param end the ending time slice
       * @param interpolation the amount to interpolate by
       * */
      void animate(ArrayList<Node> nodes,int start,int end, float interpolation){
    	  Node n1 = nodes.get(this.node1);
    	  Node n2 = nodes.get(this.node2);
    	  if (this.persistence.get(start)!=0 && this.persistence.get(end)!=0){ //Safety Check     		  
    		  drawEdge(n1.x,n1.y,n2.x,n2.y,1,1);
    	  }else if (this.persistence.get(start)==1 || this.persistence.get(end)==1){ //Should appear in either of the neighbor time slices
    		  drawEdge(n1.x,n1.y,n2.x,n2.y,interpolation,1);
    	  }    	  
      }
      /** Renders the edge between the specified coordinates
       *  @param x0,y0,x1,y1 the coordinates
       *  @param interp amount for setting the alpha value
       *  @param weight the thickness of the stroke
       * */
      void drawEdge(float x0, float y0, float x1, float y1,double interp,float weight){
    	  int alpha = (int)(interp*100); //Scale down the transparency
    	  parent.strokeWeight(weight);    	
    	  parent.stroke(253, 224, 221,alpha); 
		  parent.line(x0, y0,x1,y1);		  
      }
      /** Visualizes the edge persistence across all time slices to guide interaction
       *  @param nodes an array of all nodes in the graph    
       *  @param persistence an array of persistence values (if null, then set it to this object's persistence)
       * */
      void drawHintPath(ArrayList <Node> nodes,ArrayList<Integer>persistence,int view){
    	  this.hintCoords.clear();
    	  
    	  if (persistence == null)
    		  persistence = this.persistence;
    	  
    	  Node n1 = nodes.get(this.node1);
    	  Node n2 = nodes.get(this.node2);    	  
    	  
    	  //Want the first time slice on the hint path to be the node thats closest to the left side of the screen
    	  //(this corresponds to the layout of the time slider)
    	  if (n1.x > n2.x){ //Swap them
    		  Node temp = n1;
    		  n1 = n2;
    		  n2 = temp;
    	  }
    	  
    	  float interval = (float)1/(this.numTimeSlices-1);    	      	  
    	  float interpX,interpY,interpolation=0,prevX=n1.x,prevY=n1.y;    	  
    	  
    	  for (int i=0;i<this.numTimeSlices;i++){     		  
    		  interpX = (n2.x - n1.x)*interpolation + n1.x;
    		  interpY = n1.y +(n2.y-n1.y)*((interpX-n1.x)/(n2.x-n1.x)); 
    		  if (persistence.get(i)==0){
    			  parent.stroke(189, 189, 189,170);
    		  }else{
    			  parent.stroke(206,18,86,170);    	    	  
    		  } 
    		  parent.strokeWeight(4);   
    		  parent.line(prevX,prevY,interpX,interpY);     		  
             
    		  this.hintCoords.add(new Coordinate(interpX,interpY));
    		  
    		  if (i==view){ //Draw an indicator showing current time (perpendicular to the edge)
                  parent.stroke(255); 
                  parent.strokeWeight(3); 
                  ArrayList<Coordinate> coords = findPerpendicularLine(prevX,prevY,interpX,interpY,5.0f);                   
                  parent.line(coords.get(0).x, coords.get(0).y, coords.get(1).x, coords.get(1).y);
               }
    		  interpolation +=interval;
    		  prevX = interpX;
    		  prevY = interpY;    		 
    	  }    	  
      }
   /**Finds the unit vector perpendicular to a line defined by the given points.
    * Unit vector can be multiplied by a factor to increase it's length
    * @param x1,y1 the first point on the line
    * @param x, y2 the second point on the line
    * @param weight the length of the perpendicular line
    * @return the two coordinates comprising the perpendicular line
    * Equation courtesy of: http://stackoverflow.com/questions/133897/how-do-you-
    * find-a-point-at-a-given-perpendicular-distance-from-a-line
    * */
   ArrayList<Coordinate> findPerpendicularLine(float x1, float y1, float x2, float y2, float weight){
	   ArrayList <Coordinate> lineCoords = new ArrayList <Coordinate>();
	   float dx = x1-x2;
       float dy = y1-y2;
       float dist = (float) Math.sqrt(dx*dx + dy*dy);
        dx = dx/dist;
        dy = dy/dist;
        lineCoords.add(new Coordinate(x1 + weight*dy, y1 - weight*dx));
        lineCoords.add(new Coordinate(x1 - weight*dy, y1 + weight*dx));
        return lineCoords;
   }
}
