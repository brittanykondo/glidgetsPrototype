import java.util.ArrayList;
import processing.core.*;
/**A node on a graph, for displaying it and saving important properties
 * */
public class Node {
      float x, y;      
      int id;
      String label;
      double value;     
      public static final float RADIUS = 20;
      PApplet parent;
      ArrayList<Coordinate> coords; //Ordered by time
      
      Node(PApplet p,int id,String l){
    	  this.x = 0;
    	  this.y = 0;    	  
    	  
    	  this.label = l;
    	  this.parent = p;
    	  this.id = id;    
    	  
    	  this.coords = new ArrayList<Coordinate>();
      }  
      
      void display(){
    	  parent.fill(255,100);
    	  parent.noStroke();
    	  parent.ellipse(this.x,this.y,RADIUS,RADIUS);  
    	 
    	  PFont font = parent.createFont("Arial",12,true);
	   	  parent.textFont(font);	   	  
	   	  parent.fill(255);
	   	  parent.text(this.label, this.x, this.y);
      } 
      
      /**Draw the node at a certain moment in time
       * */
      void display(int view){
    	  parent.fill(255,100);
    	  parent.noStroke();
    	  parent.ellipse(this.coords.get(view).x,this.coords.get(view).y,RADIUS,RADIUS);    	  
      }
      
      void mouseClicked(){
    	  if (parent.mousePressed && parent.dist(this.x,this.y,parent.mouseX,parent.mouseY)<=RADIUS){
    		  System.out.println("clicked");
    	  }
      }
      
}
