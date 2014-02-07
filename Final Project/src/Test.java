import java.util.ArrayList;

import processing.core.*;

public class Test extends PApplet {   	
    public Slider timeSlider;
    public ForceDirectedGraph graph;   
    public int toggleGlobalPersistence;  
    public boolean slowDown;
    public float t;
    public int start, end;
    
/**Initialize the view, draw the visualization */
public void setup() {
    size(1100,650);    
    //The screen size: size(displayWidth,displayHeight);
    this.graph = new ForceDirectedGraph(this,"savedGraphData4.txt",6);    
    //GraphManager g = new GraphManager(this,true);  //Creates the graph layout using JUNG library
	ArrayList <String> testLabels = new ArrayList <String>();
	for (int i=0;i<this.graph.numTimeSlices;i++){
		testLabels.add(""+i);
	}    
    this.timeSlider = new Slider(this,testLabels,70,10,550);
    this.slowDown = false;
    this.t = 0;
    this.start = -1;
    this.end = -1;
  }

  /**Re-draw the view */
  public void draw() {   
   if (timeSlider.dragging){ //Dragging the slider
    	drawBackground();    	    	
    	timeSlider.drawSlider();  
    	timeSlider.drag(mouseX);
    	this.graph.animateGraph(timeSlider.currentView, timeSlider.nextView, timeSlider.interpAmount,new int[] {-1,-1},-1);
    }else if (graph.draggingNode){ //Dragging around a node
    	drawBackground();    	
    	timeSlider.drawSlider();    	
    	this.graph.dragAroundNode();  
    	timeSlider.animateTick(graph.interpAmount, graph.currentView, graph.nextView);
    }else if (graph.onDraggingEdge){
    	drawBackground();    	
    	timeSlider.drawSlider();
    	timeSlider.animateTick(graph.interpAmount, graph.currentView, graph.nextView);
    	this.graph.dragAlongEdge();    		
    }else if (graph.dragging){ //Issue query sketching   
    	sketch();
    }/**else if (slowDown){ //Released a node or edge anchor, slowly animate the graph
    	drawBackground();    		
    	timeSlider.drawSlider(); 
    	timeSlider.animateTick(this.t, this.start, this.end);
    	this.graph.animateGraph(this.start, this.end, this.t, new int [] {-1, -1},-1);
    	slowDownAnimation();
    }*/else{ //Draw the graph (with hint paths if anything is selected)
    	drawBackground();    	
        timeSlider.drawSlider();  
    	this.graph.drawGraph(this.graph.drawingView);
    }          
  }
  /**Updates the parameter for a slow down animation which occurs after a dragged element is released
   * */
  public void slowDownAnimation(){
	  if (this.start == graph.currentView && this.end == graph.nextView){ //Snapped to current view, animate backwards
		  this.t -=0.01;
	      if (this.t<0){
	    	  this.slowDown = false;
	       }		  
	  }else{ //Otherwise animate forward
		  this.t +=0.01;
	      if (this.t>1){
	    	  this.slowDown = false;
	      }
	  }
  }
  /**Draws the background and other interface components
   * */
  public void drawBackground(){
	  background(25,25,25);
	  fill(115,115,115,50); //Panel surrounding the slider and toggle options
	  noStroke();
	  rect(60,500,390,140);
	  drawGlobalButton();
	  //drawControlInstructions();
  }
  /**Adds a trail to the mouse movement to simulate the appearance of sketching
   * This is displayed when the mouse has dragged from one node to another
   * */
  public void sketch(){
	  //stroke(67,162,202,150);
	  stroke(255);
	  strokeWeight(1);
	  line(pmouseX,pmouseY,mouseX,mouseY);
  }

  /**Responds to a mouse down event on the canvas */
  public void mousePressed(){
	  timeSlider.selectTick(mouseX,mouseY);
	  if (!timeSlider.dragging){ //Avoid interference between selecting node and slider tick		  
		if (graph.draggingEdge!=null){
			graph.selectEdge();
		}else{
			graph.selectNodes();			
		}				    
	  }	 
	  toggleGlobalButton();
  }
  
  /**Responds to a mouse up event on the canvas */
  public void mouseReleased(){
	  if (timeSlider.dragging){ //Snap to view based on the slider		 
		  timeSlider.releaseTick();		  
		  this.graph.updateView(timeSlider.currentView, timeSlider.nextView, timeSlider.drawingView);		  
	  } else if (graph.onDraggingEdge){ //Snap to view based on edge anchor
		  this.slowDown = true;
		  this.t = graph.interpAmount;
		  this.start = graph.currentView;
		  this.end = graph.nextView;
		  graph.releaseEdge();
		  timeSlider.updateView(graph.currentView,graph.nextView,graph.drawingView);		  
	  }else { //Snap to view based on the node anchor	
	      //Animation doesn't work..
		 /** this.slowDown = true;
		  this.t = graph.interpAmount;
		  this.start = graph.currentView;
		  this.end = graph.nextView;*/
		  graph.releaseNodes(); 		  
		  timeSlider.updateView(graph.currentView,graph.nextView,graph.drawingView);
	  }     	 
  }   
  /**Displays the instructions for issuing aggregated queries
   * */
  public void drawControlInstructions(){
	  PFont font = createFont("Droid Serif",12,true);
   	  textFont(font);	   	  
   	  fill(255);   	  
   	  textAlign(LEFT);
   	  text("Hold 'n' to aggregate node query", 90,610);	   	 
  	  text("Hold 'e' to aggregate edge query", 90,625);	
  }
  /**Draws a button used for toggling the global highlights on and off 
   * (for now, can only toggle when the graph is at a view (not during
   * interaction)
   * */
  public void drawGlobalButton(){
	  if (this.toggleGlobalPersistence==1){
		  this.graph.drawGlobalPersistence(timeSlider.drawingView); //Draw the global highlights on the graph
		  fill(255);
		  stroke(255,255,255,100);
	  }else{
		  fill(100);
		  noStroke();
	  }
	  ellipse(80,590,10,10);
	  PFont font = createFont("Droid Serif",12,true);
   	  textFont(font);	   	  
   	  fill(255);   	  
   	  textAlign(LEFT);
   	  text("Toggle Global Persistence", 90,595);	
  }
  /**Checks if the mouse was pressed on this button
   * */
  public void toggleGlobalButton(){
	  if (dist(80,590,mouseX,mouseY)<=10 && this.toggleGlobalPersistence==0){    			    		 
		  this.toggleGlobalPersistence = 1;
		  return;
	  }  
	  this.toggleGlobalPersistence = 0;
	  return;	  
  }
  
  static public void main(String args[]) {
	    PApplet.main("Test");
	}
}

