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
     float dragTickHeight = 20, dragTickWidth=10; //Of the draggable tick
     float tickHeight, tickWidth; //Of the ticks along the main slider
     int currentView;
     boolean dragging;
     float dragTickX, dragTickY;
     
     Slider (PApplet p, ArrayList<String>l,int sp){   	  
	   	  this.parent = p;
	   	  this.labels = l;   
	   	  this.numLabels = l.size();
	   	  this.spacing = sp;
	   	  
	   	  this.yPos = 80;
	   	  this.xPos = 10;
	   	  this.tickHeight = 20;
	   	  this.currentView = 0;
	   	  this.dragging = false;
	   	  
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
	   	  
	   	  parent.line(this.tickPositions.get(this.currentView), this.yPos, this.tickPositions.get(this.numLabels-1), this.yPos);
	   	  
	   	  //Draw the ticks and the labels
	   	  for (int i=0;i<this.numLabels;i++){
	   		   parent.line(this.tickPositions.get(i), this.yPos + this.tickHeight/2, this.tickPositions.get(i), 
	   				   this.yPos -this.tickHeight/2);
	   		   parent.text(this.labels.get(i), this.tickPositions.get(i), this.yPos - this.tickHeight/2);
	   	  }
	   	  
	   	  //Draw the draggable tick
	   	  parent.fill(100);
	   	  this.dragTickX = this.tickPositions.get(this.currentView)-this.dragTickWidth/2;
	   	  this.dragTickY = this.yPos-this.dragTickHeight/2;   	        
     } 
    /**Re-draws the draggable tick along the main slider bar (responds to mouse interaction)
     * */
    void redrawTick(){
    	parent.stroke(0);
        if (dragging) parent.fill(50);        
        else parent.fill(175,200);
    	parent.rect(this.dragTickX,this.dragTickY,this.dragTickWidth,this.dragTickHeight,4);  
     } 
     
     /**Checks if a mouse down event is occurring on the draggable tick
      * */
     void selectTick(int mx, int my){    	 
    	 if (mx >= this.dragTickX && mx < this.dragTickX+this.dragTickWidth && 
    			 my >= this.dragTickY && my<this.dragTickY+this.dragTickHeight){    		 
    		this.dragging = true;
    	 }
     }
     /**Re-draws the tick's position in correspondence with the mouse position if the tick
      * has been selected
      * */
     void drag(int mx){    
    	 //Only update position if it's in bounds of the slider    	
    	 if (this.dragging == true && mx >= this.tickPositions.get(0) && mx < this.tickPositions.get(this.numLabels-1)){
    		 this.dragTickX = mx; 
    	 }    	    	 
     }
    
     /**Snaps the tick to the nearest tick on the slider
      * */
     void releaseTick(){    	 
         this.dragging = false;    	     	 
     }
     
}
