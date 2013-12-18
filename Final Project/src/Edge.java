import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;

public class Edge {	
	  static final float NODE_RADIUS = 30;	  
	  
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
    		  drawGlobalPersistenceHighlights(n1.coords.get(view),n2.coords.get(view));
    		  //Stroke thickness: 
    		  //drawEdge(n1.coords.get(view).x,n1.coords.get(view).y,n2.coords.get(view).x,n2.coords.get(view).y,1,(float)(this.globalPersistence*10));      		
    	  }     	  
      }
      /** Adds highlights to the edges to show global persistence
       * */  
      //TODO: experiment with different designs
      void drawGlobalPersistenceHighlights(Coordinate c1, Coordinate c2){
    	  //Barchart gylphs along edges:
    	  Coordinate start = pointOnLine(c2.x,c2.y,c1.x,c1.y,-NODE_RADIUS/2);
    	  Coordinate end = pointOnLine(c1.x,c1.y,c2.x,c2.y,-NODE_RADIUS/2);    	  
    	  
    	  float lineHalfDist = euclideanDistance(start.x,start.y,end.x,end.y)/8;    	  
    	  Coordinate endPoint1 = pointOnLine(start.x,start.y,end.x,end.y,-lineHalfDist*this.globalPersistence);    	  
    	  Coordinate endPoint2 = pointOnLine(end.x,end.y,start.x,start.y,-lineHalfDist*this.globalPersistence);
    	
    	  parent.stroke(67,162,202,150);  
    	  parent.line(end.x, end.y, endPoint1.x, endPoint1.y);
    	  parent.line(start.x, start.y, endPoint2.x, endPoint2.y);    	  
      }
      /** Calculates the distance between two points
       * (x1,y1) is the first point
       * (x2,y2) is the second point
       * @return the distance, avoiding the square root
       * */
      public float euclideanDistance (float x1,float y1, float x2,float y2){
     	    float term1 = x1 - x2;
     	    float term2 = y1 - y2;
     	    return (float) Math.sqrt((term1*term1)+(term2*term2));
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
    	  Coordinate start = pointOnLine(n2.x,n2.y,n1.x,n1.y,-NODE_RADIUS/2);
    	  Coordinate end = pointOnLine(n1.x,n1.y,n2.x,n2.y,-NODE_RADIUS/2);
    	  
    	  //Want the first time slice on the hint path to be the node thats closest to the left side of the screen
    	  //(this corresponds to the layout of the time slider)
    	  if (start.x > end.x){ //Swap them
    		  Coordinate temp = start;
    		  start = end;
    		  end = temp;
    	  }
    	  
    	  float interval = (float)1/(this.numTimeSlices-1);    	      	  
    	  float interpX,interpY,interpolation=0,prevX=start.x,prevY=start.y;    	  
    	  
    	  for (int i=0;i<this.numTimeSlices;i++){     		  
    		  interpX = (end.x - start.x)*interpolation + start.x;
    		  interpY = start.y +(end.y-start.y)*((interpX-start.x)/(end.x-start.x)); 
    		  if (persistence.get(i)==0){
    			  parent.stroke(189, 189, 189,150);
    		  }else{
    			 // parent.stroke(206,18,86,170);  
    			  parent.stroke(67,162,202,150); 
    		  } 
    		  parent.strokeWeight(4);   
    		  parent.line(prevX,prevY,interpX,interpY);     		  
             
    		  this.hintCoords.add(new Coordinate(interpX,interpY)); //Save the coordinates along the hint path	  
    		  
    		  interpolation +=interval;
    		  prevX = interpX;
    		  prevY = interpY;    		 
    	  }
    	  
    	  //Draw an indicator showing current time (perpendicular to the edge)
          parent.stroke(67,162,202,255); 
          parent.strokeWeight(4); 
          //A little bit of a hack, but set the points depending on which view to draw at
          Coordinate coord1,coord2;
          if (view<this.numTimeSlices-1){
        	  coord1 = this.hintCoords.get(view);
        	  coord2 = this.hintCoords.get(this.numTimeSlices-1);
          }else{
        	  coord2 = this.hintCoords.get(view-1);
        	  coord1 = this.hintCoords.get(this.numTimeSlices-1);
          }
          ArrayList<Coordinate> coords = findPerpendicularLine(coord1.x,coord1.y,coord2.x,coord2.y,6.0f);                   
          parent.line(coords.get(0).x, coords.get(0).y, coords.get(1).x, coords.get(1).y);	  
      }
      
    /**Finds the point on a line defined by x1-y1 and x2-y2 that is distance
     * away from x2-y2
     * @param x1,y1 and x2,y2 the end points of the line
     * @param distance the units away from x2,y2 the desired point lies
     * @return the new point
     * Code based on: http://stackoverflow.com/questions/1800138/given-a-start-and-end-point
     * -and-a-distance-calculate-a-point-along-a-line?rq=1
     * */
    Coordinate pointOnLine(float x1, float y1, float x2, float y2, float distance){
    	float vx = x2 - x1;
    	float vy = y2 - y1;
    	float mag = (float) Math.sqrt(vx*vx + vy*vy);
    	vx /= mag;
    	vy /= mag;
    	
    	float px = (float)x1 + vx * (mag + distance);
    	float py = (float)y1 + vy * (mag + distance);
    	
    	return new Coordinate(px,py);
    }
    /** Draws the hint path, then animates the indicator according to mouse dragging along the edge
     *  @param nodes a list of all nodes in the dataset
     *  @param newX,newY the position of the mouse projected onto the hint path (according to the min distance)     *  
     * */
    void animateHintPath(ArrayList<Node> nodes,float newX, float newY){  	  	    	  
  	  //First, draw the hint path
  	  for (int i=1;i<this.numTimeSlices;i++){   		 
  		  if (persistence.get(i)==0){
  			  parent.stroke(189, 189, 189,255);
  		  }else{
  			  //parent.stroke(206,18,86,255);
  			  parent.stroke(67,162,202,255);
  		  } 
  		  parent.strokeWeight(4);
  		  parent.line(this.hintCoords.get(i-1).x,this.hintCoords.get(i-1).y,this.hintCoords.get(i).x,this.hintCoords.get(i).y); 		   		 
  	  }
  	   //Then, animate the indicator according to mouse movement
        parent.stroke(67,162,202,255); 
        parent.strokeWeight(4);          
        Coordinate endC = this.hintCoords.get(this.numTimeSlices-1);       	
        ArrayList<Coordinate> coords = findPerpendicularLine(newX,newY,endC.x,endC.y,6.0f);                   
        parent.line(coords.get(0).x, coords.get(0).y, coords.get(1).x, coords.get(1).y);   
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
