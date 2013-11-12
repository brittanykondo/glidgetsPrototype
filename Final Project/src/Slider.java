import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PFont;


public class Slider {
	 
     ArrayList <String> labels;
     int numLabels;
     PApplet parent;
     ArrayList <Float> tickPositions;
     int spacing; //Between ticks
     float yPos; //Of the slider
     float xPos; //Spacing from edge
     float tickHeight;
     
     Slider (PApplet p, ArrayList<String>l,int sp){   	  
	   	  this.parent = p;
	   	  this.labels = l;   
	   	  this.numLabels = l.size();
	   	  this.spacing = sp;
	   	  
	   	  this.yPos = 700;
	   	  this.xPos = 10;
	   	  this.tickHeight = 20;
	   	  
	   	  setTicks();
     }
     
     /**Sets the tick x-positions to appear on the slider
      * */
     void setTicks(){ 
    	 this.tickPositions = new ArrayList <Float>();
    	 for (int i=0;i<this.numLabels;i++){
    		 this.tickPositions.add((float) (i*this.spacing+this.spacing+this.xPos));
    	 }
     }
     /**Draws the entire slider (doesn't need to be redrawn during mouse interaction)
      * */
     void drawSlider(){
    	 //Draw the main line
	   	  parent.stroke(255);
	   	  PFont font = parent.createFont("Arial",16,true);
	   	  parent.textFont(font);	   	  
	   	  parent.fill(255);
	   	  
	   	  parent.line(this.tickPositions.get(0), this.yPos, this.tickPositions.get(this.numLabels-1), this.yPos);
	   	  
	   	  //Draw the ticks and the labels
	   	  for (int i=0;i<this.numLabels;i++){
	   		   parent.line(this.tickPositions.get(i), this.yPos + this.tickHeight/2, this.tickPositions.get(i), 
	   				   this.yPos -this.tickHeight/2);
	   		   parent.text(this.labels.get(i), this.tickPositions.get(i), this.yPos - this.tickHeight/2);
	   	  }
	   	  
     } 
    /**Re-draws the draggable tick along the main slider bar (responds to mouse interaction)
     * */
     void redrawTick(){
    	 parent.fill(100);
    	 parent.rect(10,10,10,20,5);
     }
    
     /**void mouseClicked(){
	   	  if (parent.mousePressed && parent.dist(this.x,this.y,parent.mouseX,parent.mouseY)<=RADIUS){
	   		  System.out.println("clicked");
	   	  }
     }*/
     
}
