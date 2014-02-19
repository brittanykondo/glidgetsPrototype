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
    		  drawEdge(n1.coords.get(view).x,n1.coords.get(view).y,n2.coords.get(view).x,n2.coords.get(view).y,255,1);
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
    	
    	  parent.stroke(67,162,202,255);  
    	  parent.line(end.x, end.y, endPoint1.x, endPoint1.y);
    	  parent.line(start.x, start.y, endPoint2.x, endPoint2.y);   	  
    	  
    	  //Short gylphs along edges with thickness encoding persistence
    	  /**Coordinate start = pointOnLine(c2.x,c2.y,c1.x,c1.y,-NODE_RADIUS/2);
    	  Coordinate end = pointOnLine(c1.x,c1.y,c2.x,c2.y,-NODE_RADIUS/2);    	  
    	  
    	  float lineHalfDist = euclideanDistance(start.x,start.y,end.x,end.y)/8;    	  
    	  Coordinate endPoint1 = pointOnLine(start.x,start.y,end.x,end.y,-lineHalfDist*0.3f);    	  
    	  Coordinate endPoint2 = pointOnLine(end.x,end.y,start.x,start.y,-lineHalfDist*0.3f);
    	
    	  parent.stroke(67,162,202,150);  
    	  parent.strokeWeight(7*this.globalPersistence); 
    	  parent.line(end.x, end.y, endPoint1.x, endPoint1.y);
    	  parent.line(start.x, start.y, endPoint2.x, endPoint2.y);  */
    	  
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
    	  int startPersistence = this.persistence.get(start);
    	  int endPersistence = this.persistence.get(end);
    	  
    	  int interpolationAmt;      
    	  
    	  if (startPersistence!=0 && endPersistence!=0){      		  
    		  drawEdge(n1.x,n1.y,n2.x,n2.y,150,1);
    	  }else if (startPersistence==0 && endPersistence==1){ //Fading in
    		  interpolationAmt = easeInExpo(interpolation,0,150,1);
    		  drawEdge(n1.x,n1.y,n2.x,n2.y,interpolationAmt,1);
    	  } else if (startPersistence==1 && endPersistence==0) { //Fading out    		  
    		  interpolationAmt = easeOutExpo((1-interpolation),0,150,1);
    		  drawEdge(n1.x,n1.y,n2.x,n2.y,interpolationAmt,1);
    	  }
      }
      /** Renders the edge between the specified coordinates
       *  @param x0,y0,x1,y1 the coordinates
       *  @param interp amount for setting the alpha value
       *  @param weight the thickness of the stroke
       * */
      void drawEdge(float x0, float y0, float x1, float y1,int interp,float weight){
    	  //int alpha = (int)(interp*100); //Scale down the transparency    	  
    	  parent.strokeWeight(weight);    	
    	  parent.stroke(253, 224, 221,interp); 
		  parent.line(x0, y0,x1,y1);		  
      }
      /** Function to compute the transparency of a node fading in
       *  From: http://www.gizma.com/easing/#cub1
       *  @param t current time
       *  @param b start value
       *  @param c change in value
       *  @param d duration
       * */
      int easeInExpo(float t, float b, float c, float d) {
    	  return (int)(c * Math.pow( 2, 10 * (t/d - 1) ) + b);    	   
      }
      /** Function to compute the transparency of a node fading out
       *  From: http://www.gizma.com/easing/#cub1
       *  @param see easeInExpo();
       * */
      int easeOutExpo(float t, float b, float c, float d) {
    	  return (int) (c * ( -Math.pow( 2, -10 * t/d ) + 1 ) + b);
    	}
      /**Draws a dotted line from start to end
       * */
      void drawDottedLine(float startX,float startY,float endX, float endY){
    	 //Relative Length Dashes
    	  /**int numSegments = 5;
    	  float interval = 0.2f;    	  
    	  float interpX,interpY,interpolation=0,prevX=startX,prevY=startY;   
    	  
    	  for (int i=0;i<numSegments;i++){
    		  
    		  interpX = (endX - startX)*interpolation + startX;
    		  interpY = startY +(endY-startY)*((interpX-startX)/(endX-startX)); 
    		  if (i%2==0){     			  
    			  parent.line(prevX,prevY,interpX,interpY);
    		  }   		  
    		  
    		  interpolation +=interval;
    		  prevX = interpX;
    		  prevY = interpY; 
    	  }*/
    	  //Fixed length dashes (doesn't draw anything for nodes that are too close)
    	  float desiredDashLength = 4.0f;
    	  float segmentLength = parent.dist(startX,startY,endX,endY);   
    	  
    	  if (segmentLength<=8.0f) { //If edge is too short, just draw a solid line for now
    		  parent.line(startX,startY,endX,endY);
    		  return;    		  
    	  }
    	  
    	  float interval = desiredDashLength/segmentLength;
          System.out.println(interval+" segment "+segmentLength);
    	  
          float interpX,interpY,interpolation=0,prevX=startX,prevY=startY; 
    	  int i=0;
    	  while (interpolation <= 1){
    		  interpX = (endX - startX)*interpolation + startX;
    		  interpY = startY +(endY-startY)*((interpX-startX)/(endX-startX)); 
    		  if (i%2==0){     			  
    			  parent.line(prevX,prevY,interpX,interpY);    			  
    		  }   	   		  
    		  interpolation +=interval;
    		  i++;
    		  prevX = interpX;
    		  prevY = interpY; 
    	  }
    	 /** for (int i=0;i<numSegments;i++){
    		  
    		  interpX = (endX - startX)*interpolation + startX;
    		  interpY = startY +(endY-startY)*((interpX-startX)/(endX-startX)); 
    		  if (i%2==0){     			  
    			  parent.line(prevX,prevY,interpX,interpY);
    			  System.out.println(parent.dist(prevX, prevY, interpX, interpY));
    		  }   		  
    		  
    		  interpolation +=interval;
    		  prevX = interpX;
    		  prevY = interpY; 
    	  }*/
      }
      /** Visualizes the edge persistence across all time slices to guide interaction
       *  @param nodes an array of all nodes in the graph    
       *  @param persistence an array of persistence values (if null, then set it to this object's persistence)
       * */
      void drawHintPath(ArrayList <Node> nodes,ArrayList<Integer>persistence){
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
         
    	  prevX=start.x;
    	  prevY=start.y;
    	  interpolation = 0;    	 
    	  parent.strokeCap(parent.SQUARE);
    	  parent.strokeWeight(12);
    	  
    	  //Highlight surrounding the edge
    	  /**
    	  for (int i=0;i<this.numTimeSlices;i++){ 
    		 
    		  interpX = (end.x - start.x)*interpolation + start.x;
    		  interpY = start.y +(end.y-start.y)*((interpX-start.x)/(end.x-start.x));  		  
    		  
    		  
    		  if (persistence.get(i)==0){
    			  parent.stroke(189, 189, 189,255);
    		  }else{
    			 // parent.stroke(206,18,86,170);  
    			  parent.stroke(67,162,202,255); 
    		  }     
    		  this.hintCoords.add(new Coordinate(interpX,interpY)); //Save the coordinates along the hint path    		  
    		  parent.line(prevX,prevY,interpX,interpY);
    		  
    		  interpX = (end.x - start.x)*interpolation + start.x;
    		  interpY = start.y +(end.y-start.y)*((interpX-start.x)/(end.x-start.x)); 
    		  
    		  interpolation +=interval;
    		  prevX = interpX;
    		  prevY = interpY;    		 
    	  }
    	  
    	  parent.strokeWeight(5);
    	  parent.stroke(25,25,25,255);
    	  parent.line(start.x, start.y, end.x, end.y);
    	  */
    	  parent.strokeWeight(4);
    	  //Dotted line
    	  for (int i=0;i<this.numTimeSlices;i++){     		  
    		  interpX = (end.x - start.x)*interpolation + start.x;
    		  interpY = start.y +(end.y-start.y)*((interpX-start.x)/(end.x-start.x)); 
    		  if (persistence.get(i)==0){
    			  parent.stroke(189, 189, 189,255);
    			  drawDottedLine(prevX,prevY,interpX,interpY);    			  
    		  }else{
    			 // parent.stroke(206,18,86,170);  
    			  parent.stroke(67,162,202,255); 
    			  parent.line(prevX,prevY,interpX,interpY);    		
    			  
    		  }     		      		  
             
    		  this.hintCoords.add(new Coordinate(interpX,interpY)); //Save the coordinates along the hint path	 
    		 
    		 // System.out.println(i+" "+this.hintCoords.get(i).x+" "+this.hintCoords.get(i).y);
    		  
    		  interpolation +=interval;
    		  prevX = interpX;
    		  prevY = interpY;    		 
    	  }	  
    	    
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
     *  @param newX,newY the position of the mouse projected onto the hint path (according to the min distance)     *  
     * */
    void animateAnchor(float newX, float newY){  	  	 
    	//drawHintPath(nodes,persistence);
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
