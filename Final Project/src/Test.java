import java.util.ArrayList;

import processing.core.*;

public class Test extends PApplet {   	
    public Slider timeSlider;
    public ForceDirectedGraph graph;   
    public int toggleLocalPersistence;  
    public boolean slowDown;
    public float t;
    public int start, end;
    
/**Initialize the view, draw the visualization */
public void setup() {
    //size(1100,650);
	size(displayWidth,displayHeight);
    //The screen size: size(displayWidth,displayHeight);   
    //GraphManager g = new GraphManager(this,false);  //Creates the graph layout using JUNG library
    this.graph = new ForceDirectedGraph(this,"savedGraphData7.txt",6);    
    
	ArrayList <String> testLabels = new ArrayList <String>();
	for (int i=0;i<this.graph.numTimeSlices;i++){
		testLabels.add(""+i);
	}    
    this.timeSlider = new Slider(this,testLabels,70,10,650);
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
    	this.graph.clearQueries();
    	this.graph.animateGraph(timeSlider.currentView, timeSlider.nextView, timeSlider.interpAmount,new int[] {-1,-1},-1);
    }else if (graph.draggingNode){ //Dragging around a node
    	drawBackground();    	
    	timeSlider.drawSlider();    	
    	this.graph.dragAroundNode();  
    	timeSlider.animateTick(graph.interpAmount, graph.currentView, graph.nextView);
    }else if (graph.onDraggingEdge){
    	drawBackground();    	
    	timeSlider.drawSlider();
    	System.out.println(graph.interpAmount);
    	timeSlider.animateTick(graph.interpAmount, graph.currentView, graph.nextView);
    	this.graph.dragAlongEdge();    		
    }else if (graph.dragging){ //Issue query sketching   
    	sketch();
    }else{ //Draw the graph (with hint paths if anything is selected)
    	drawBackground();    	
        timeSlider.drawSlider();  
    	this.graph.drawGraph(this.graph.drawingView);
    }          
  }
  /**Draws the background and other interface components
   * */
  public void drawBackground(){
	  background(25,25,25);
	  fill(115,115,115,50); //Panel surrounding the slider and toggle options
	  noStroke();
	  rect(60,600,800,150);
	  drawLocalButton();
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
	  if (!timeSlider.dragging){ //Avoid interference between selecting graph elements and slider tick
		 graph.selectElement(); //Select elements on the graph	     		    
	  }	 
	  toggleLocalButton();
  }
  /**Responds to a mouse dragging event (mouse pressed + mouse move ) on the canvas */
  public void mouseDragged(){	  
	  graph.isNodeDragged();//Just check if the node should be dragged around or de-selected	 
  }
  
  /**Responds to a mouse up event on the canvas */
  public void mouseReleased(){
	  if (timeSlider.dragging){ //Snap to view based on the slider		 
		  timeSlider.releaseTick();		  
		  this.graph.updateView(timeSlider.currentView, timeSlider.nextView, timeSlider.drawingView);		  
	  } else {		 
		  graph.releaseElements();
		  timeSlider.updateView(graph.currentView,graph.nextView,graph.drawingView);		  
	  }
		 	 
  }   
  /**Displays the instructions for issuing aggregated queries
   
  public void drawControlInstructions(){
	  PFont font = createFont("Droid Serif",12,true);
   	  textFont(font);	   	  
   	  fill(255);   	  
   	  textAlign(LEFT);
   	  text("Hold 'n' to aggregate node query", 90,610);	   	 
  	  text("Hold 'e' to aggregate edge query", 90,625);	
  }*/
  
  /**Draws a button used for toggling the global highlights on and off 
   * (for now, can only toggle when the graph is at a view (not during
   * interaction)
   * */
  public void drawLocalButton(){
	  if (this.toggleLocalPersistence==1){
		  this.graph.drawLocalPersistence(timeSlider.drawingView); //Draw the local highlights for all elements at the current time slice
		  fill(255);
		  stroke(255,255,255,100);
	  }else{
		  fill(100);
		  noStroke();
	  }
	  ellipse(80,690,10,10);
	  PFont font = createFont("Droid Serif",12,true);
   	  textFont(font);	   	  
   	  fill(255);   	  
   	  textAlign(LEFT);
   	  text("Toggle Local Persistence", 90,695);	
  }
  /**Checks if the mouse was pressed on this button
   * */
  public void toggleLocalButton(){
	  if (dist(80,690,mouseX,mouseY)<=10 && this.toggleLocalPersistence==0){    			    		 
		  this.toggleLocalPersistence = 1;
		  return;
	  }  
	  this.toggleLocalPersistence = 0;
	  return;	  
  }
  
  static public void main(String args[]) {
	    PApplet.main(new String[] { "--present", "Test" });
	}
}

