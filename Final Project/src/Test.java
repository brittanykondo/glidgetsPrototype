import java.util.ArrayList;

import processing.core.*;

public class Test extends PApplet {   	
    public Slider timeSlider;
    public ForceDirectedGraph graph;   
    public int toggleGlobalPersistence;  
    
/**Initialize the view, draw the visualization
   * */
public void setup() {
    size(800,800);    
    this.graph = new ForceDirectedGraph(this,"savedGraphData2.txt",6);    
    //GraphManager g = new GraphManager(this,true);  //Creates the graph layout using JUNG library
	ArrayList <String> testLabels = new ArrayList <String>();
	for (int i=0;i<this.graph.numTimeSlices;i++){
		testLabels.add(""+i);
	}    
    this.timeSlider = new Slider(this,testLabels,70,10,650);     
  }

  /**Re-draw the view */
  public void draw() {   
   if (timeSlider.dragging){ //Dragging the slider
    	background(25,25,25);
    	drawGlobalButton();    	
    	timeSlider.drawSlider();  
    	timeSlider.drag(mouseX);
    	this.graph.animateGraph(timeSlider.currentView, timeSlider.nextView, timeSlider.interpAmount,-1,-1);
    }else if (graph.draggingNode != -1){ //Dragging around a node
    	background(25,25,25);
    	drawGlobalButton();    	
    	timeSlider.drawSlider();
    	timeSlider.animateTick(graph.interpAmount, graph.currentView, graph.nextView);
    	this.graph.dragAroundNode();    	
    }else if (graph.dragging){ //Issue query sketching   	
    	sketch();
    }else{ //Draw the graph (with hint paths if anything is selected)
    	background(25,25,25); 
    	drawGlobalButton();    	
        timeSlider.drawSlider();  
    	this.graph.drawGraph(this.graph.drawingView);
    }          
  }
  
  /**Adds a trail to the mouse movement to simulate the appearance of sketching
   * This is displayed when the mouse has dragged from one node to another
   * */
  public void sketch(){
	  stroke(255);
	  strokeWeight(1);
	  /**sketchyEffect.*/line(pmouseX,pmouseY,mouseX,mouseY);
  }

  /**Responds to a mouse down event on the canvas */
  public void mousePressed(){
	  timeSlider.selectTick(mouseX,mouseY);
	  if (!timeSlider.dragging){ //Avoid interference between selecting node and slider tick
		  graph.selectNodes(); 
	  }	 
	  toggleGlobalButton();
  }
  
  /**Responds to a mouse up event on the canvas */
  public void mouseReleased(){
	  if (timeSlider.dragging){ //Snap to view based on the slider		 
		  timeSlider.releaseTick();		  
		  this.graph.updateView(timeSlider.currentView, timeSlider.nextView, timeSlider.drawingView);		  
	  } else { //Snap to view based on the node anchor		 
		  graph.releaseNodes(); 
		  timeSlider.updateView(graph.currentView,graph.nextView,graph.drawingView);
	  }     	 
  }     
 /** When a key is pressed, queries can be aggregated.  This means that either node or edge
  *  persistence is combined into a single hint path (over time).  This shows when a set of
  *  nodes appear together.
  * */
  public void keyPressed(){	 
	  if (key=='n' || key=='N'){ //Aggregate nodes
		  graph.selectMultipleNodes();	
	  }	else if (key=='e'||key=='E'){ //Aggregate edges
		  graph.selectMultipleEdges();
	  }    	 
  } 
  /**Cancels the aggregated query by clearing the hint paths 
   * */
  public void keyReleased(){	 
	  if (key=='n' || key=='N'){ //Aggregate nodes
		  graph.releaseMultipleNodes();
	  }	else if (key=='e'||key=='E'){ //Aggregate edges
		  graph.releaseMultipleEdges();
	  }
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
	  ellipse(10,690,10,10);
	  PFont font = createFont("Arial",12,true);
   	  textFont(font);	   	  
   	  fill(255);   	  
   	  textAlign(LEFT);
   	  text("Toggle Global Persistence", 20,695);	
  }
  /**Checks if the mouse was pressed on this button
   * */
  public void toggleGlobalButton(){
	  if (dist(10,690,mouseX,mouseY)<=10 && this.toggleGlobalPersistence==0){    			    		 
		  this.toggleGlobalPersistence = 1;
		  return;
	  }  
	  this.toggleGlobalPersistence = 0;
	  return;	  
  }
}

