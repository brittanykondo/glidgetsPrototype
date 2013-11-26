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
      public static final float RADIUS = 50;
      PApplet parent;
      ArrayList<Coordinate> coords; //Ordered by time
      float globalPersistence; //Percentage of time the node does not disappear
      int numTimeSlices;
      
      Node(PApplet p,int id,String l,int t){
    	  this.x = 0;
    	  this.y = 0;       	  
    	  this.label = l;
    	  this.parent = p;
    	  this.id = id;    
    	  this.numTimeSlices = t;
    	  
    	  this.coords = new ArrayList<Coordinate>();
      }  
      /**Renders a node and adds a text label
       * */
      void display(){
    	  parent.fill(255,100);
    	  parent.noStroke();
    	  parent.ellipse(this.x,this.y,RADIUS,RADIUS);  
    	 
    	  PFont font = parent.createFont("Arial",12,true);
	   	  parent.textFont(font);	   	  
	   	  parent.fill(255);
	   	  parent.text(this.label, this.x, this.y);
      } 
      
      /**Renders the node at its position at a certain moment in time
       * */
      void display(int view){       	
    	  if (this.coords.get(view)!=null){
    		  drawNode(this.coords.get(view).x, this.coords.get(view).y,255);    		  
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
    		  drawNode(this.coords.get(view).x, this.coords.get(view).y,255);   
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
       * */
      void mouseClicked(){
    	  if (parent.mousePressed && parent.dist(this.x,this.y,parent.mouseX,parent.mouseY)<=RADIUS){
    		  System.out.println("clicked");
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
      }       
}
