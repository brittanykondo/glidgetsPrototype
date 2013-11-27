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
     int currentView,nextView;
     boolean dragging;
     float dragTickX, dragTickY;
     float interpAmount;
     
     Slider (PApplet p, ArrayList<String>l,int sp){   	  
	   	  this.parent = p;
	   	  this.labels = l;   
	   	  this.numLabels = l.size();
	   	  this.spacing = sp;
	   	  
	   	  this.yPos = 600;
	   	  this.xPos = 10;
	   	  this.tickHeight = 20;
	   	  this.currentView = 0;
	   	  this.nextView = 1;
	   	  this.dragging = false;
	   	  this.interpAmount = 0;
	   	  
	   	  setTicks();
	   	  
	   	  //Set the initial position of the draggable tick
	   	  this.dragTickX = this.tickPositions.get(0)-this.dragTickWidth/2;
	   	  this.dragTickY = this.yPos-this.dragTickHeight/2;  
     }
     
     /**Sets the tick x-positions to appear on the slider
      * */
     void setTicks(){ 
    	 this.tickPositions = new ArrayList <Float>();
    	 for (int i=0;i<this.numLabels;i++){
    		 this.tickPositions.add((float) (i*this.spacing+this.spacing+this.xPos));
    	 }
     }
     /**Draws the non-interactive parts of the slider (labels, ticks and time line)
      * */
     void drawSliderBackground(){
    	//Draw the main line
    	 parent.strokeWeight(2);
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
    /**Re-draws the draggable tick along the main slider bar (responds to mouse interaction).
     * Also re-draws the slider bar
     * */
    void drawSlider(){
    	drawSliderBackground();
    	parent.strokeWeight(1);
    	parent.stroke(0);       
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
      * @param mx the x-coordinate of the mouse
      * */
     void drag(int mx){    
    	 //Only update position if it's in bounds of the slider    	
    	 if (this.dragging == true && mx >= this.tickPositions.get(0) && mx < this.tickPositions.get(this.numLabels-1)){ 
    		 float next = this.tickPositions.get(this.nextView);
    		 float current = this.tickPositions.get(this.currentView);
    		 if (mx<=current && this.currentView >0){ //Passed current view, moving backward in time
    			 this.nextView = this.currentView;
    			 this.currentView--;    			 
    		 }else if (mx>=next && this.nextView<(this.numLabels-1)){ //Passed next view, moving forward in time
    			 this.currentView = this.nextView;
    			 this.nextView++;
    		 }else{
    			 this.interpAmount = Math.abs(mx -current)/(next-current);
    		 }
    		 this.dragTickX = mx; 
    	 }        
     }
    
     /**Snaps the tick to the nearest tick on the slider
      * */
     void releaseTick(){    	 
         this.dragging = false;
         float current = this.tickPositions.get(this.currentView);
         float next = this.tickPositions.get(this.nextView);
         float nextDist = Math.abs(this.dragTickX - next);
		 float currentDist = Math.abs(this.dragTickX - current);
		 
		 if (currentDist < nextDist){ //Snap to current view
			 this.dragTickX = current;
		 }else{ //Snap to next view
			 this.dragTickX = next;
			 if (this.nextView<(this.numLabels-1)){
				 this.currentView = this.nextView;
				 this.nextView++;
			 }			
		 }
     }
     
}