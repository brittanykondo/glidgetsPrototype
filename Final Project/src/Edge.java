import java.awt.Color;
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
      
      //Display colours     
      static Colours getColours = new Colours();
      static Color presentColour = getColours.BlueGreen;         
      static Color absentColour = getColours.LightGrey;      
      static Color yearMarkColour = getColours.BlueSugarOrangeDarker; 
      static Color anchorColour = getColours.DarkOrange;  
      static Color yearLabel = getColours.Ink;    
      static Color edgeColour = getColours.Ink; 
    
      
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
       * @param showDisappeared true if edges not in the current view should be drawn
       * */
      void display(ArrayList<Node> nodes, int view,boolean showDisappeared,boolean highlight){  
    	  //Safety check
    	  if (this.node1>=nodes.size() || this.node2>=nodes.size()) return;
    	  
    	  Node n1 = nodes.get(this.node1);
    	  Node n2 = nodes.get(this.node2);
    	  if (showDisappeared){
    		  drawEdge(n1.x,n1.y,n2.x,n2.y,255,1.5f);
    	  }else{
    		  if (n1.coords.get(view)!=null && n2.coords.get(view)!=null && this.persistence.get(view)!=0){ //Safety Check
    			  if (highlight){
            		  parent.strokeWeight(2);    	
                	  parent.stroke(presentColour.getRGB()); 
            		  parent.line(n1.x,n1.y,n2.x,n2.y);
            	  }else{
            		  drawEdge(n1.x,n1.y,n2.x,n2.y,255,1.5f);
            	  }        		  
        	  }
    	  }    	  
      } 
      /** Adds highlights to the edges to show global persistence
       *  @param nodes all nodes in the network
       *  @param view  the view to show the highlights for
       *  @param showAllHighlights true if all highlights for every edge should be drawn  
       *  */      
      void drawGlobalPersistenceHighlights(ArrayList<Node>nodes,int view,boolean showAllHighlights,float fade){
    	  if (showAllHighlights || this.persistence.get(view)!=0) {    	  
	          this.hintCoords.clear();    	  
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
	    	  
	    	  float interval = (float)1/(this.numTimeSlices);    	      	  
	    	  float interpX,interpY,interpolation=0,prevX=start.x,prevY=start.y;         
	    	     	  	 
	    	  parent.strokeCap(parent.SQUARE);   
	    	  
	    	  //Calculate the hint path coordinates
	    	  for (int i=0;i<this.numTimeSlices;i++){   
	    		  //System.out.println(persistence.get(i)+" "+i);
	    		  interpX = (end.x - start.x)*interpolation + start.x;
	    		  interpY = start.y +(end.y-start.y)*((interpX-start.x)/(end.x-start.x));             
	    		  this.hintCoords.add(new Coordinate(interpX,interpY)); //Save the coordinates along the hint path   		  
	    		  interpolation +=interval;
	    		  prevX = interpX;
	    		  prevY = interpY;    		 
	    	  }	    	 
	    	  this.hintCoords.add(new Coordinate(end.x,end.y));
	    	  drawEdge(n1.x,n1.y,n2.x,n2.y,(int)(255*fade),1.5f);
	    	  
	    	  parent.strokeWeight(5);
	    	 
	    	  //Draw the hint path      	 
	    	  for (int i=0;i<this.numTimeSlices;i++){     		         		  
	    		  if (persistence.get(i)==1){
	    			  /**if (i==view){
	    				  parent.stroke(presentColour.getRed(),presentColour.getBlue(),presentColour.getGreen(),presentColour.getAlpha()*fade);
	    			  }else{
	    				  parent.stroke(yearMarkColour.getRed(),yearMarkColour.getBlue(),yearMarkColour.getGreen(),yearMarkColour.getAlpha()*fade));
	    			  }*/
	    			  start = this.hintCoords.get(i);
	    			  end = this.hintCoords.get(i+1);
	    			  float offsetX = (end.x - start.x)*(0.01f)+ start.x;
		    		  float offsetY = start.y +(end.y-start.y)*((offsetX-start.x)/(end.x-start.x));   
	    			  parent.stroke(presentColour.getRed(),presentColour.getGreen(),presentColour.getBlue(),(255*fade));
	    			  parent.line(offsetX,offsetY,this.hintCoords.get(i+1).x,this.hintCoords.get(i+1).y);    			      			
	    		  }       		 
	    	  } 	    	 
    	  }    	  
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
       * @param highlight  whether or not to highlight the edge (if incident on a dragged node)
       * */
      void animate(ArrayList<Node> nodes,int start,int end, float interpolation,boolean highlight){
    	  Node n1 = nodes.get(this.node1);
    	  Node n2 = nodes.get(this.node2);
    	  int startPersistence = this.persistence.get(start);
    	  int endPersistence = this.persistence.get(end);
    	  
    	  int interpolationAmt = 0;      
    	  if (startPersistence==1 && endPersistence==1){
    		  interpolationAmt = 255;
    	  }else if (startPersistence==0 && endPersistence==1){ //Fading in
    		  interpolationAmt = easeInExpo(interpolation,0,255,1);    		
    	  } else if (startPersistence==1 && endPersistence==0) { //Fading out    		  
    		  interpolationAmt = easeOutExpo((1-interpolation),0,255,1);    		  
    	  }
    	  
    	  //If highlight is set to true, colour the edge as blue
    	  if (highlight){
    		  parent.strokeWeight(2);    	
        	  parent.stroke(presentColour.getRed(),presentColour.getGreen(),presentColour.getBlue(),interpolationAmt); 
    		  parent.line(n1.x,n1.y,n2.x,n2.y);
    	  }else{
    		  drawEdge(n1.x,n1.y,n2.x,n2.y,interpolationAmt,1.5f);
    	  }
      }
      /** Renders the edge between the specified coordinates
       *  @param x0,y0,x1,y1 the coordinates
       *  @param interp amount for setting the alpha value
       *  @param weight the thickness of the stroke
       * */
      void drawEdge(float x0, float y0, float x1, float y1,int interp,float weight){
    	  int alpha = (int)(interp*0.5); //Scale down the transparency    	  
    	  parent.strokeWeight(weight);     	 
    	  parent.stroke(edgeColour.getRed(), edgeColour.getGreen(), edgeColour.getBlue(),alpha); 
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
          //System.out.println(interval+" segment "+segmentLength);
    	  
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
      }
      /** Visualizes the edge persistence across all time slices to guide interaction
       *  @param nodes an array of all nodes in the graph    
       *  @param persistence an array of persistence values (if null, then set it to this object's persistence)
       *  @param view the current (drawing) view
       * */
      void drawHintPath(ArrayList <Node> nodes,ArrayList<Integer>persistence,int view){
    	  this.hintCoords.clear();
    	  
    	  if (persistence == null)
    		  persistence = this.persistence;
    	  
    	  Node n1 = nodes.get(this.node1);
    	  Node n2 = nodes.get(this.node2);   
    	  //float edgeLength = parent.dist(n1.x,n1.y,n2.x,n2.y);
    	  //float bufferSpace = -NODE_RADIUS/2-(0.03f*edgeLength);
    	  Coordinate start = pointOnLine(n2.x,n2.y,n1.x,n1.y,-NODE_RADIUS/2);
    	  Coordinate end = pointOnLine(n1.x,n1.y,n2.x,n2.y,-NODE_RADIUS/2);
    	  
    	  //Want the first time slice on the hint path to be the node thats closest to the left side of the screen
    	  //(this corresponds to the layout of the time slider)
    	  if (start.x > end.x){ //Swap them
    		  Coordinate temp = start;
    		  start = end;
    		  end = temp;
    		  
    		  Node tempNode = n1;
    		  n1 = n2;
    		  n2 = tempNode;
    	  }
    	  
    	  float interval = (float)1/(this.numTimeSlices);    	      	  
    	  float interpX,interpY,interpolation=0,prevX=start.x,prevY=start.y;     	     	  	 
    	  parent.strokeCap(parent.SQUARE);   
    	  
    	  //Calculate the hint path coordinates
    	  for (int i=0;i<this.numTimeSlices;i++){   
    		  //System.out.println(persistence.get(i)+" "+i);
    		  interpX = (end.x - start.x)*interpolation + start.x;
    		  interpY = start.y +(end.y-start.y)*((interpX-start.x)/(end.x-start.x));             
    		  this.hintCoords.add(new Coordinate(interpX,interpY)); //Save the coordinates along the hint path   		  
    		  interpolation +=interval;
    		  prevX = interpX;
    		  prevY = interpY;    		 
    	  }	    	 
    	  this.hintCoords.add(new Coordinate(end.x,end.y));
    	  //Old Design: Hint path as a highlight surrounding the edge    	  
    	 /** for (int i=1;i<this.hintCoords.size();i++){     		  
    		  if (persistence.get(i-1)==0){
    			  parent.stroke(189, 189, 189,255);
    		  }else{
    			 // parent.stroke(206,18,86,170);  
    			  parent.stroke(67,162,202,255); 
    		  }     
    		  
    		  parent.line(this.hintCoords.get(i-1).x,this.hintCoords.get(i-1).y,this.hintCoords.get(i).x,this.hintCoords.get(i).y);   		  		 
    	  }
    	  
    	  parent.strokeWeight(5);
    	  parent.stroke(25,25,25,255);
    	  parent.line(start.x, start.y, end.x, end.y);*/  
    	  
    	  Coordinate startArrow,endArrow;
    	  parent.strokeWeight(3);
		  
    	  //Draw hint path: Dotted line      	 
    	  for (int i=0;i<this.numTimeSlices;i++){ 
    		startArrow = this.hintCoords.get(i);
      	    endArrow = this.hintCoords.get(i+1);    	   		 		  		 
    		  
    		  if (persistence.get(i)==0){
    			  parent.stroke(absentColour.getRGB());    	
    			  if (i==view){
    				  parent.stroke(yearMarkColour.getRGB());
    			  }
    			  drawDottedLine(startArrow.x,startArrow.y,endArrow.x,endArrow.y);     			
    		  }else{    			 
    			  parent.stroke(presentColour.getRGB());  
    			  if (i==view){
    				  parent.stroke(yearMarkColour.getRGB());
    			  }
    			  parent.line(startArrow.x,startArrow.y,endArrow.x,endArrow.y);     			 
    		  }       		 
    	  }   
    	  
    	  //Draw arrows along the path     	 
    	  for (int i=0;i<this.numTimeSlices;i++){ 
    		  if (i==0){
    			  startArrow = new Coordinate(n1.x,n1.y); 
        		  endArrow = this.hintCoords.get(i); 
    		  }else{
    			  startArrow = this.hintCoords.get(i-1);
    	    	  endArrow = this.hintCoords.get(i);   
    		  }     				  		 
    		  
    		  if (persistence.get(i)==0){
    			  parent.stroke(absentColour.getRGB());    			     			  
    			  /**if (view==i){
    				  parent.stroke(120, 120, 120,255);
    			  }*/
    			  this.drawArrow(startArrow.x,startArrow.y,endArrow.x,endArrow.y);
    		  }else{    			 
    			  parent.stroke(presentColour.getRGB());     			  			 
    			  /**if (view==i){
    				 parent.stroke(0,118,168,255);				 
    			  }*/
    			  this.drawArrow(startArrow.x,startArrow.y,endArrow.x,endArrow.y);
    		  }       		 
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
     *  @param newX,newY the position of the mouse projected onto the hint path (according to the min distance) 
     *  @param view   to draw the time slice label 
     * */
    void animateAnchor(float newX, float newY,int view){  	   	
    	 parent.strokeCap(parent.ROUND);
   	     parent.stroke(anchorColour.getRGB());
   	     parent.strokeWeight(10);
         Coordinate endC = this.hintCoords.get(this.numTimeSlices-1);        
         ArrayList<Coordinate> coords = findPerpendicularLine(newX,newY,endC.x,endC.y,7.0f);
         
         if (coords==null){ //At the last path segment
        	 endC = this.hintCoords.get(this.numTimeSlices);        
        	 coords = findPerpendicularLine(newX,newY,endC.x,endC.y,7.0f);             	
         }
         
         parent.line(coords.get(0).x, coords.get(0).y, coords.get(1).x, coords.get(1).y);  
        
         PFont font = parent.createFont("Droid Sans",20,true);
	   	 parent.textFont(font);
	   	 parent.fill(yearLabel.getRGB());
	   	 ArrayList<Coordinate> textCoords = findPerpendicularLine(newX,newY,endC.x,endC.y,20.0f);
   	     parent.text(view, textCoords.get(0).x, textCoords.get(0).y);
    }
  
    /**Draws an arrow at the end (x2,y2) of the line defined by the given points
     * @param x1,y1  Coordinates of the first point on the line
     * @param x1,y1  Coordinates of the second point on the line (where the arrow head goes)
     * Code from Processing Help forum: http://processing.org/discourse/beta/num_1219607845.html
     * */
    void drawArrow(float x1,float y1, float x2, float y2) {   
    	  float lineLength = 6;
    	  parent.pushMatrix();
    	  parent.strokeWeight(2);    	
    	  parent.translate(x2, y2);
    	  float a = parent.atan2(x1-x2, y2-y1);
    	  parent.rotate(a);
    	  parent.line(0, 0, -lineLength, -lineLength);
    	  parent.line(0, 0, lineLength, -lineLength);    	  
    	  parent.popMatrix();
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
       
       if (dist==0) return null; //Avoid divide by zero

        dx = dx/dist;
        dy = dy/dist;
        lineCoords.add(new Coordinate(x1 + weight*dy, y1 - weight*dx));
        lineCoords.add(new Coordinate(x1 - weight*dy, y1 + weight*dx));
        return lineCoords;
   }
}


///////////////////////////////Old designs for global highlights/////////////////////////////////////////////////
/** Visualizes the overall edge persistence (how often is it displayed?) 
 *  at a certain time slice
 *  @param nodes all nodes in the graph
 *  @param view  the view to draw the edges at
 *      
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
 *        
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
	  parent.line(start.x, start.y, endPoint2.x, endPoint2.y);      	  
}*/
