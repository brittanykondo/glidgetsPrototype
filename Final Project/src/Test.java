import java.util.ArrayList;

import processing.core.*;
//import org.gicentre.handy.*;


public class Test extends PApplet {   	
    public Slider timeSlider;
    public ForceDirectedGraph graph;   
    public int toggleGlobalPersistence;
    //public HandyRenderer sketchyEffect;
    
/**Initialize the view, draw the visualization
   * */
public void setup() {
    size(800,800);       
    this.graph = new ForceDirectedGraph(this,"savedGraphData.txt",6);   
	ArrayList <String> testLabels = new ArrayList <String>();
	for (int i=0;i<this.graph.numTimeSlices;i++){
		testLabels.add(""+i);
	}    
    this.timeSlider = new Slider(this,testLabels,70,10,650);    
    //this.sketchyEffect = new HandyRenderer(this);
  }

  /**Re-draw the view */
  public void draw() {   
     
    if (timeSlider.dragging){
    	background(25,25,25);
    	drawGlobalButton();    	
    	timeSlider.drawSlider();  
    	timeSlider.drag(mouseX);
    	this.graph.animateGraph(timeSlider.currentView, timeSlider.nextView, timeSlider.interpAmount);
    }else if (graph.draggingNode){
    	background(25,25,25);
    	drawGlobalButton();    	
    	timeSlider.drawSlider();
    	this.graph.dragAroundNode();    	
    }else if (graph.dragging){ //Issue query sketching   	
    	sketch();
    }/**else if (graph.selectedNode!=-1 && graph.releasedNode==-1){ //Mouse is released, one node is selected
    	background(25,25,25);     	
        timeSlider.drawSlider();  
    	this.graph.drawGraph(timeSlider.drawingView);    	 
    */else{
    	background(25,25,25); 
    	drawGlobalButton();    	
        timeSlider.drawSlider();  
    	this.graph.drawGraph(timeSlider.drawingView);
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

  /**Responds to a mouse down event on the canvas
   * */
  public void mousePressed(){	  
	  timeSlider.selectTick(mouseX,mouseY);
	  if (!timeSlider.dragging){
		  graph.selectNodes(); //Avoid interference between selecting node and slider tick
	  }	 
	  toggleGlobalButton();
  }
  
  /**Responds to a mouse up event on the canvas
   * */
  public void mouseReleased(){	 	  
      timeSlider.releaseTick();	 
      graph.releaseNodes();      
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
   	  text("Global Persistence", 20,695);	
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

