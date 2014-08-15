import java.awt.Color;
import java.io.PrintWriter;
import java.util.ArrayList;

import processing.core.*;
import processing.event.KeyEvent;
import processing.pdf.PGraphicsPDF;

public class RunApp extends PApplet { 
    public Slider timeSlider;
    public ForceDirectedGraph graph;   
    public long startTime;    
    public int selectedNode, selectedEdge;
    //public int toggleLocalPersistence; 
    public Button globalViewButton;
    public Button editModeButton;   
    public Button aggregateButton;
    public PGraphicsPDF pdf;
    public boolean recording;
    public Colours getColours = new Colours();
    public int screenWidth;
    public int screenHeight;
    public int borderSize;
   // public float scaleFactor = 1;
    
/**Initialize the view, draw the visualization */
public void setup() {    
	this.screenHeight = displayHeight;
	this.screenWidth = displayWidth;
	//this.screenHeight = 700;
	//this.screenWidth = 1250;	
	this.borderSize = 40; //Width of the query de-selection area
	
	size(this.screenWidth,this.screenHeight); 
   
	//GraphGenerator g = new GraphGenerator(this,4);
    //g.process("infectious_saved",this.screenWidth,this.screenHeight);
	
    this.graph = new ForceDirectedGraph(this,"vanDeBunt_saved.txt",6);    
    this.recording = false;
	
	ArrayList <String> testLabels = new ArrayList <String>();
	for (int i=0;i<this.graph.numTimeSlices;i++){
		testLabels.add(""+i);
	}    
    this.timeSlider = new Slider(this,testLabels,70,10,650);  
    this.selectedNode = -1;
    this.selectedEdge = -1;   
    //this.toggleLocalPersistence = 0;
    this.globalViewButton = new Button(this,130,40,495,625,"Global View",560,650,18);
    this.editModeButton = new Button(this,130,40,650,625,"Edit Mode",715,650,18);   
    this.aggregateButton = new Button(this,130,40,805,625,"Aggregate",870,650,18);      
    this.aggregateButton.clickedBorderColour = getColours.MintGreen;
    this.aggregateButton.clickedColour = getColours.MintGreen;
  }

  /**Re-draw the view */
  public void draw() { 
	 //Capture the current frame drawn on the screen, if 'r' was pressed
	 if (recording){
		  beginRecord(PDF,"frame-####.pdf");
	  }
	 // scale(scaleFactor);	 
	 if (this.editModeButton.toggle==1){ //Reposition nodes by dragging
		  drawBackground();    	    
		  this.graph.clearQueries();
		  this.graph.drawAllElements();
	  }else if (timeSlider.dragging){ //Dragging the slider
    	drawBackground();    	    	
    	timeSlider.drawSlider();  
    	timeSlider.drag(mouseX);
    	this.graph.updateView(timeSlider.currentView, timeSlider.nextView, timeSlider.drawingView);	
    	this.graph.animateGraph(timeSlider.currentView, timeSlider.nextView, timeSlider.interpAmount);       	
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
    }else if (this.globalViewButton.toggle ==1){ //Global view mode
    	drawBackground(); 
    	this.graph.showGlobalPersistence(); 
    }else{ //Draw the graph (with hint paths if anything is selected)
    	drawBackground();    	
        timeSlider.drawSlider();          
    	this.graph.drawGraph(graph.drawingView);    	
    } 
	  
	if (recording){
		endRecord();
		recording=false;
	}
  }
  /** Checks where a mouse is hovering
   * */
  public void mouseOver(){
	  this.globalViewButton.hover();
	  this.editModeButton.hover();
	  this.aggregateButton.hover();
  }
  /**Draws the background and other interface components
   * */
  public void drawBackground(){	  
	  background(255);	   	  
	 // fill(getColours.LightGrey.getRGB()); //Panel surrounding the slider and toggle options
	  fill(getColours.LightSlate.getRGB());
	  noStroke();
	  rect(60,600,890,100,10); 
	  mouseOver();
	  this.globalViewButton.draw();	
	  this.editModeButton.draw();
	  this.aggregateButton.draw();
	  this.drawBorder();	  
  }
  /**Draws the de-selection border and toggles its colour on mouse over
   * */
  public void drawBorder(){
	  if (mouseX >= this.borderSize && mouseX <= (this.screenWidth-this.borderSize) && mouseY >= this.borderSize && mouseY <= 
				(this.screenHeight-this.borderSize)){
		  stroke(255);		  
	  }else{
		  stroke(220,220,220,200);
	  }
	  
	  strokeWeight(this.borderSize);
	  noFill();
	  rect(0,0,this.screenWidth,this.screenHeight);
  }
  /**Adds a trail to the mouse movement to simulate the appearance of sketching
   * This is displayed when the mouse has dragged from one node to another
   * */
  public void sketch(){	 
	  stroke(0);
	  strokeWeight(1);
	  line(pmouseX,pmouseY,mouseX,mouseY);
  }

  /**Responds to a mouse down event on the canvas */
  public void mousePressed(){	  
	  toggleButtons();
	  //System.out.println(this.editModeButton.toggle);
	  if (this.editModeButton.toggle==1) return;
	  
	  timeSlider.selectTick(mouseX,mouseY);
	  if (!timeSlider.dragging){ //Avoid interference between selecting graph elements and slider tick
		 graph.selectElement(); //Select elements on the graph	     		    
	  } 
	  if (!graph.inGlobalView){
		  this.globalViewButton.toggle = 0;
	  }	  
	  
	 /** if (!this.globalViewButton.clicked){
		  if (graph.selectedNode==-1 && graph.selectedEdge==-1){			  
			  graph.clearQueries();				
		  }			 	 
	  }  */
	 //Check if queries should be cleared
	  if (this.clickedBorder()){
		  graph.clearQueries();
	  }
	  
	  //Check if glyphs should be aggregated
	  if (this.aggregateButton.toggle ==1){
		 graph.aggregate = 1;
       }else{
		 graph.aggregate = 0;
	   }
  }
  /**Checks if the mouse clicked the border (designated de-selection area for canceling all queries)*/
  public boolean clickedBorder(){
	  if (mouseX >= this.borderSize && mouseX <= (this.screenWidth-this.borderSize) && mouseY >= this.borderSize && mouseY <= 
				(this.screenHeight-this.borderSize)){
		  return false;
	  }
		  
	  return true;
  }
  /**Responds to a mouse dragging event (mouse pressed + mouse move ) on the canvas */
  public void mouseDragged(){	
	  
	  if (this.editModeButton.toggle==1){
		  graph.updateNodePosition(mouseX, mouseY);
		  return;
	  }	  
	  graph.isNodeDragged();//Just check if the node should be dragged around or de-selected
	  if (!graph.inGlobalView){
		  this.globalViewButton.toggle = 0;
	  }
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
  /**Detects clicks on all buttons by calling their toggle function
   * */
  public void toggleButtons(){
	  this.globalViewButton.toggle();
	  
	  if (this.globalViewButton.toggle==1){		  
		  graph.inGlobalView = true;		 
	  }else {
		  graph.inGlobalView = false;		 
	  }	 
	  
	  this.editModeButton.toggle();
	  this.aggregateButton.toggle();
	  
	  //Both global view and edit mode can't be selected at the same time
	  if (this.globalViewButton.clicked){
		  this.editModeButton.toggle = 0;
		  this.aggregateButton.toggle = 0;
	  }else if (this.editModeButton.clicked){
		  this.globalViewButton.toggle= 0;
		  this.aggregateButton.toggle = 0;
	  }else if (this.aggregateButton.clicked){
		  this.globalViewButton.toggle= 0;
		  this.editModeButton.toggle = 0;
	  }
	  
  }
  /**Toggles the recording (capturing the screen's graphics and writing to a pdf)
   * */
  public void keyPressed(){
	  if (key=='r'){
		  recording = true;
	  }/**else if (key=='i'){
		  scaleFactor +=0.01;
	  }else if (key=='o'){		  
		  scaleFactor -=0.01;
		  if (scaleFactor <=1) scaleFactor = 1;
	  }*/
  }
  static public void main(String args[]) {
	   PApplet.main(new String[] { "--present", "RunApp" });	  
	}
}

