import java.awt.Color;
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
     float dragTickHeight = 30, dragTickWidth=20; //Of the draggable tick
     float tickHeight, tickWidth; //Of the ticks along the main slider
     int currentView,nextView,drawingView;
     boolean dragging;
     float dragTickX, dragTickY;
     float interpAmount;
     
    //Colours    
     //public Colours getColours = new Colours();
     public Color sliderColour = new Color(115,115,115,255);    
     public Color slidingTickBorder = new Color(255,255,255,255);     
     
     /**Creates a new interactive slider widget
      * @param p reference to the processing applet (for drawing)
      * @param l an array of labels to appear along the slider
      * @param sp the spacing in between ticks along the slider
      * @param x,y the screen coordinates indicating position of the slider
      * */
     Slider (PApplet p, ArrayList<String>l,int sp,int x, int y){   	  
	   	  this.parent = p;
	   	  this.labels = l;   
	   	  this.numLabels = l.size();
	   	  this.spacing = sp;
	   	  
	   	  this.yPos = y;
	   	  this.xPos = x;
	   	  this.tickHeight = 20;
	   	  this.currentView = 0;
	   	  this.drawingView = 0;
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
	   	  parent.stroke(sliderColour.getRGB());
	   	  PFont font = parent.createFont("Century Gothic",16,true);
	   	  parent.textFont(font);		   	 
	   	  parent.textAlign(parent.CENTER);
	   	  
	   	  parent.line(this.tickPositions.get(0), this.yPos, this.tickPositions.get(this.numLabels-1), this.yPos);
	   	  
	   	  //Draw the ticks and the labels
	   	  for (int i=0;i<this.numLabels;i++){
	   		   parent.line(this.tickPositions.get(i), this.yPos + this.tickHeight/2, this.tickPositions.get(i), 
	   				   this.yPos -this.tickHeight/2);
	   		   parent.text(this.labels.get(i), this.tickPositions.get(i), this.yPos - this.tickHeight);
	   	  }
     }
    /**Re-draws the draggable tick along the main slider bar (responds to mouse interaction).
     * Also re-draws the slider bar
     * */
    void drawSlider(){
    	drawSliderBackground();
    	 parent.fill(sliderColour.getRGB());
    	if (this.dragging){
    		parent.stroke(slidingTickBorder.getRGB());
    	}else{
    		parent.noStroke();
    	}    	
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
    	 int fixAnchor = 0;
    	 if (this.dragging == true && mx >= this.tickPositions.get(0) && mx < this.tickPositions.get(this.numLabels-1)){ 
    		 float next = this.tickPositions.get(this.nextView);
    		 float current = this.tickPositions.get(this.currentView);
    		 if (mx<=current && this.currentView >0){ //Passed current view, moving backward in time
    			 this.nextView = this.currentView;
    			 this.currentView--;      			 
    			 this.interpAmount = 1;
    		 }else if (mx>=next && this.nextView<(this.numLabels-1)){ //Passed next view, moving forward in time
    			 this.currentView = this.nextView;
    			 this.nextView++;    			
    			 this.interpAmount = 0;
    		 }else{
    			 this.interpAmount = Math.abs(mx -current)/(next-current);
    		 }
    		 this.dragTickX = mx; 
    	 }else if (mx <= this.tickPositions.get(0)){
    		 fixAnchor = 1;
    	 }else if (mx > this.tickPositions.get(this.numLabels-1)){
    		 fixAnchor = 1;
    	 }
    	 
    	 if (fixAnchor==1){
    		 this.drawingView = this.nextView;
    	 }else{
    		 this.drawingView = this.currentView;
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
			 this.dragTickX = current- this.dragTickWidth/2; 
			 this.drawingView = this.currentView;
		 }else{ //Snap to next view
			 this.dragTickX = next- this.dragTickWidth/2;
			 if (this.nextView<(this.numLabels-1)){
				 this.currentView = this.nextView;
				 this.nextView++;
				 this.drawingView = this.currentView;
			 }else{
				 this.drawingView = this.nextView;
			 }
		 }
     }
     /**Moves the tick across the slider based on the specified views and interpolation amount
      * @param interpolation amount to move the slider by
      * @param start, end views to draw in between
      * */
     void animateTick(float interpolation,int start,int end){
    	 float x1 = this.tickPositions.get(start);
    	 float x2 = this.tickPositions.get(end);
    	 this.dragTickX = (x2-x1)*interpolation + x1;
     }
     /**Sets the view variables as the provided parameters
      * @param current the current view
      * @param next  the next view
      * @param drawing the view to draw the graph at
      * */
    public void updateView(int current,int next,int drawing){
    	this.currentView = current;
    	this.nextView = next;
    	this.drawingView = drawing;
    	this.dragTickX = this.tickPositions.get(drawing) - this.dragTickWidth/2;
    }
     
}
