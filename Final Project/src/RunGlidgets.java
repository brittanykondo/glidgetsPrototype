import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import processing.core.*;
import processing.pdf.PGraphicsPDF;

import controlP5.*;

/** Main class for running the app.
 *  Accepts input data file name as a command line argument (with .txt), if no name provided, uses the default
 *  data file (vanDeBunt.txt).
 * */
public class RunGlidgets extends PApplet { 
    public Slider timeSlider;
    public ForceDirectedGraph graph;   
    public long startTime;    
    public int selectedNode, selectedEdge;
    //public int toggleLocalPersistence; 
    public Button globalViewButton;
    public Button editModeButton;   
    public Button aggregateButton;
    public Button saveButton;
    public Button newLayoutButton;
    public PGraphicsPDF pdf;
    public boolean recording;
    public Colours getColours = new Colours();
    public int screenWidth;
    public int screenHeight;
    public int borderWidth;
    public boolean clickedBorder;
    public double t; //Track animations
    public String outfile;
    
    public float scaleFactor = 1; 
    public String inputFile = "";
    public GraphGenerator g;
    
    public ControlP5 cp5;
    public Textfield attractionInput;
    public Textfield repulsionInput;
    public Textfield numIterationsInput;

/**Initialize the interface and draw the graph 
 * */
public void setup() {  
	
	this.inputFile = this.args[0];
		
	//this.screenHeight = displayHeight;
	//this.screenWidth = displayWidth;
	this.screenHeight = 700;
	this.screenWidth = 1250;	
	this.borderWidth = 30; //Width of the query de-selection area
	
	size(this.screenWidth,this.screenHeight); 
	
	 if (frame != null) { //Make sketch re-sizeable when running as a java application (not applet)
		 frame.setResizable(true);
     }	
      
	this.graph = new ForceDirectedGraph(this,inputFile);
    
	//For creating/changing the force directed layout
	this.g = new GraphGenerator(this,graph.timelineLabels.size());
	//this.g = new GraphGenerator(this,6);
    //g.process("Van_saved",this.screenWidth,this.screenHeight);
	
	this.recording = false;	
	this.clickedBorder = false;
	this.t = 0;	
    this.timeSlider = new Slider(this,graph.timelineLabels,70,650,550);  
    this.selectedNode = -1;
    this.selectedEdge = -1;   
    //this.toggleLocalPersistence = 0;
    this.globalViewButton = new Button(this,100,40,650,625,"Global View",700,650,16);
    this.editModeButton = new Button(this,100,40,890,625,"Edit Mode",940,650,16);   
    this.aggregateButton = new Button(this,100,40,770,625,"Aggregate",820,650,16); 
    this.saveButton = new Button(this,140,50,1010,620,"Save Layout",1080,652,18); 
    this.newLayoutButton = new Button(this,100,40,470,625,"New Layout",520,650,16); 
    this.aggregateButton.clickedBorderColour = getColours.MintGreen;
    this.aggregateButton.clickedColour = getColours.MintGreen;
    
    //Input parameters for changing the force directed layout
    this.cp5 = new ControlP5(this);
    PFont f = createFont("Sans-Serif",10);
   
    attractionInput = cp5.addTextfield("Attraction (default 0.75)").setPosition(100,635).setFont(f).setSize(70,20).setColor(255)
    		.setColorBackground(getColours.Ink.getRGB()).setColorForeground(getColours.Ink.getRGB())
    		.setColorActive(getColours.LightSlate2.getRGB()).setColorCaptionLabel(getColours.Ink.getRGB()).hide();
    
    repulsionInput = cp5.addTextfield("Repulsion (default 0.75)").setPosition(220,635).setFont(f).setSize(70,20).setColor(255)
    		.setColorBackground(getColours.Ink.getRGB()).setColorForeground(getColours.Ink.getRGB())
    		.setColorActive(getColours.LightSlate2.getRGB()).setColorCaptionLabel(getColours.Ink.getRGB()).hide();
    
    numIterationsInput = cp5.addTextfield("Max Iterations (default 1000)").setPosition(340,635).setFont(f).setSize(70,20).setColor(255)
    		.setColorBackground(getColours.Ink.getRGB()).setColorForeground(getColours.Ink.getRGB())
    		.setColorActive(getColours.LightSlate2.getRGB()).setColorCaptionLabel(getColours.Ink.getRGB()).hide();   
  }

  /**Re-draw the view */
  public void draw() { 
	
	 //Capture the current frame drawn on the screen, if 'r' was pressed
	 if (recording){
		  beginRecord(PDF,"frame-####.pdf");
	  }
	  
	 scale(scaleFactor); //Zooming in and out, re-scale the mouse coordinates 
	 if (mouseX !=pmouseX && mouseY!=pmouseY){ //http://forum.processing.org/two/discussion/1015/scaling-up-1-5-the-whole-sketch-problems-with-non-working-buttons-/p1
		 mouseX = (int)(mouseX/scaleFactor);
		 mouseY = (int)(mouseY/scaleFactor);
	 }
	 
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
  /**Draws brief usage instructions on the bottom panel
   * */
  //TODO: make a help page/button and make a widget for zooming in and out
  public void drawInstructions(){
	  fill(getColours.Ink.getRGB());
	  PFont font = createFont("Droid Sans",12,true);
   	  textFont(font);
   	  textAlign(LEFT);
	  text("Press 'r' to take a screen shot",1010,630);
	  text("+/- to zoom in/out",1010,650);
	  text("0 to reset zoom",1010,670);
	  textAlign(CENTER);
  }
  
  /**Draws the background and other interface components
   * */
  public void drawBackground(){	  
	  background(255);	   	
	  fill(getColours.LightSlate.getRGB());	  
	  noStroke();
	  rect(60,600,1125,90,10);	  
	  mouseOver();
	  this.globalViewButton.draw();	
	  this.editModeButton.draw();
	  this.aggregateButton.draw();
	  
	  if (this.editModeButton.toggle ==0){ //Can't de-select elements in edit mode..
		  this.drawBorder();
		  drawInstructions();		 
		  attractionInput.hide();
		  repulsionInput.hide();
		  numIterationsInput.hide();
	  }else if (this.editModeButton.toggle ==1) { //Display some instructions for features offered in edit mode		  
		  drawEditScreen();		 
	  }
  }
  /** Draws the interface components that are displayed in edit mode (special instructions and saving function)
   * */
  public void drawEditScreen(){	 
	  //Draw some instructions regarding dragging nodes
	  fill(getColours.Ink.getRGB());
	  PFont font = createFont("Droid Sans",16,true);
   	  /**textFont(font);
   	  textAlign(LEFT);
	  text("Drag nodes to re-position them",80,620);*/
	  
	  //Draw the save button
	  this.saveButton.draw();
	  this.saveButton.hover();
	  this.newLayoutButton.draw();
	  this.newLayoutButton.hover();
	  
	  //Provide feedback that the saving completed by displaying the name of the output file
	  if (this.saveButton.toggle ==1){
		  this.t +=0.01;
		  if (this.t>=1){
			  this.t = 0;
			  this.outfile = "";
			  this.saveButton.toggle = 0;
		  }else{
			  font = createFont("Droid Sans",11,true);
		   	  textFont(font);
			  text("Saved to file: graphData_"+this.outfile+".txt",1000,685);
		  }
	  }	 
	
	  //Reveal the text fields for inputting force directed algorithm parameters
	  attractionInput.show();
	  repulsionInput.show();
	  numIterationsInput.show();
	 
	  if (this.newLayoutButton.toggle ==1){
		  this.t +=0.1;
		  if (this.t>=1){
			  this.t = 0;			  
			  this.newLayoutButton.toggle = 0;
		  }
	  }
  }   
  /**Draws the de-selection border and toggles its colour when it is clicked, using animated transitions
   * (cubic ease in and out functions)
   * 
  public void animateBorder(){
	  if (!this.onBorder()){	
		  this.clickedBorder = false;		 
		  if (this.t>0){  //Fade out border colour			 
			  this.t -=0.1;
			  stroke(220,220,220,this.easeOutExpo((float)this.t,0,200,1));			  
		  }else{ //Already faded out, do nothing
			  this.t = 0;			  
			  noStroke();
		  }		  	  
	  }else if (this.clickedBorder){		  
		  if (this.t >=1){
			  this.clickedBorder = false;
			  this.t = 0;
		  }else { //Fade in/out
			  this.t +=0.1;		
			  //stroke(214,39,40,this.easeInOutCubic((float)this.t, 0, 255, 1)); //RED	
			  stroke(100,100,100,this.easeInOutCubic((float)this.t, 0, 255, 1));				 		  
		  }		
	  }else{			  
		  if (this.t<1){ //Fade in border colour			
			  this.t +=0.1;
			  stroke(220,220,220,this.easeInExpo((float)this.t,0,200,1));		
		  }else{ //Already faded in, do nothing
			  this.t = 1;
			  stroke(220,220,220,200);
		  }
	  }
	  
	  strokeWeight(this.borderWidth);
	  noFill();
	  rect(0,0,this.screenWidth,this.screenHeight);
  }*/ //Not used
  
  /**Draws the de-selection border and toggles its colour when it is clicked, without animated transitions
   * */
  public void drawBorder(){
	  if (!this.onBorder()){
		  noFill();
		  this.clickedBorder = false;
	  }else if (this.clickedBorder){	//Make the border change colour for longer than one frame
		  if (this.t >= 1){
			  this.clickedBorder = false;
		  }
		  fill(100,100,100,255);
		  this.t +=0.1;		  		 
	  }else{		 
		 fill(220,220,220,200);
		 this.t = 0;
	  }	  
	  noStroke();
	  rect(0,0,this.screenWidth,this.borderWidth);
	  rect(this.borderWidth,this.screenHeight-this.borderWidth,this.screenWidth-(this.borderWidth*2),this.borderWidth);
	  rect(0,this.borderWidth,this.borderWidth,this.screenHeight-this.borderWidth);
	  rect(this.screenWidth-this.borderWidth,this.borderWidth,this.borderWidth,this.screenHeight-this.borderWidth);
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
	 
	  if (this.editModeButton.toggle==1) {
		  if (this.saveButton.toggle == 1){ //In edit mode, save the current layout				  
			  this.save();
		  }
		  if (this.newLayoutButton.toggle ==1){ //In edit mode, create a new force directed layout
			  this.changeLayout();
		  }
		  return;
	  }
	  
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
	  if (this.onBorder()){		  
		  graph.clearQueries();
		  this.clickedBorder = true;
		  this.t = 0;
	  }
	  
	  //Check if glyphs should be aggregated
	  if (this.aggregateButton.toggle ==1){
		 graph.aggregate = 1;
       }else{
		 graph.aggregate = 0;
	   }
  }
  /**Checks if the mouse is on the border (designated de-selection area for canceling all queries)
   * */
  public boolean onBorder(){
	  
	  if (mouseX >= this.borderWidth && mouseX <= (this.screenWidth-this.borderWidth) && mouseY >= this.borderWidth && mouseY <= 
				(this.screenHeight-this.borderWidth)){		  
		  return false;
	  }		  
	  return true;
  }
  /**Responds to a mouse dragging event (mouse pressed + mouse move ) on the canvas 
   * */
  public void mouseDragged(){	
	  
	  if (this.editModeButton.toggle==1){
		  graph.updateNodePosition(mouseX/scaleFactor, mouseY/scaleFactor);
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
	  this.saveButton.toggle();
	  this.newLayoutButton.toggle();
	  
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
	  }else if (key=='='){
		  scaleFactor +=0.01;	
		  frame.setSize((int)(this.screenWidth*scaleFactor),(int)(this.screenHeight*scaleFactor)); //Re-size the window (not working, window slightly too small)
	  }else if (key=='-'){		  
		  scaleFactor -=0.01;
		  frame.setSize((int)(this.screenWidth*scaleFactor),(int)(this.screenHeight*scaleFactor));
		  if (scaleFactor <=0) scaleFactor = 0.01f; //Minimum zoom out level
	  }else if (key =='0'){
		  scaleFactor = 1;
	  }
  }
  /** Cubic ease in animation function
   *  From: http://www.gizma.com/easing/#cub1
   *  @param t current time
   *  @param b start value
   *  @param c change in value
   *  @param d duration
   * */
  public int easeInExpo(float t, float b, float c, float d) {
	  return (int)(c * Math.pow( 2, 10 * (t/d - 1) ) + b);    	   
  }
  /** Cubic ease out animation function
   *  From: http://www.gizma.com/easing/#cub1
   *  @param see easeInExpo() above
   * */
  public int easeOutExpo(float t, float b, float c, float d) {	
	  return (int) (c * ( -Math.pow( 2, -10 * t/d ) + 1 ) + b);
	}
  /** Cubic ease out and in animation function
   *  From: http://www.gizma.com/easing/#cub1
   *  @param see easeOutExpo() above
   * */
  public float easeInOutCubic (float t, float b,float c, float d) {
		t /= d/2;
		if (t < 1) return c/2*t*t*t + b;
		t -= 2;
		return c/2*(t*t*t + 2) + b;
	}
  
  /** Saves the current node positions to a new graph data file, when save button
   *  is clicked in edit mode.  The output file name is: graphData_MM-dd-yyyy_h-mm-ss_a.txt
   * */
  public void save(){
	  //Get the current time stamp
	  Date date = new Date();
	  SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy_h-mm-ss_a");
	  this.outfile = sdf.format(date);
	 
	  ArrayList<ArrayList<Edge>> edges2D = this.convertEdgeArray(this.graph.edges);	
	  
	  //Call GraphGenerator functions to save the data in a new file
	  this.g.addElements(this.graph.nodes,edges2D);
	  this.g.saveGraphData("graphData_"+this.outfile,false,this.graph.nodes,edges2D,this.graph.timelineLabels);	  
	  this.t = 0;
  }
  /** Converts the graph's edge array into a 2D array, which is the format required by functions 
   * in GraphGenerator class.
   * @param a 1D array of edges
   * @return a 2D array of edges (as required by the GraphGenerator class)
   * */
  //TODO: change the functions in GraphGenerator so that this conversion is not necessary
  public ArrayList<ArrayList<Edge>> convertEdgeArray (ArrayList<Edge> e){  
	  ArrayList<ArrayList<Edge>> edges2D = new ArrayList<ArrayList<Edge>> ();
	  for (int i=0;i<graph.numTimeSlices;i++){
		  edges2D.add(new ArrayList<Edge>());
		  for (int j=0;j<e.size();j++){
			  if (e.get(j).persistence.get(i)==1){
				  edges2D.get(i).add(e.get(j));
			  }
		  }
	  }
	  return edges2D;
  }
  /** Changes the force directed layout according to the provided parameters entered in the text field, while in edit mode
   *  If none are entered, uses the default values (0.75 for forces, 1000 for max iterations).
   * */
  public void changeLayout(){	
	  //Get the text input and check it for validity
	  double a, r;
	  int i;
	  String a_str = attractionInput.getText();
	  if (a_str.isEmpty()){
		  a = 0.75;
	  }else{
		  a = Double.parseDouble(a_str);
		  if (a==0){
			  a = 0.75;
		  }
	  }
	  
	  String r_str = repulsionInput.getText();
	  if (r_str.isEmpty()){
		  r = 0.75;
	  }else{
		  r = Double.parseDouble(r_str);
		  if (r==0){
			  r = 0.75;
		  }
	  }
	  
	  String i_str = numIterationsInput.getText();
	  if (i_str.isEmpty()){
		  i = 1000;
	  }else{
		  i = Integer.parseInt(i_str);
	  }
	
	 ArrayList<ArrayList<Edge>> edges2D = this.convertEdgeArray(this.graph.edges);
	 this.g.addElements(this.graph.nodes, edges2D);
	 this.g.createLayout(r,a,i,this.width,this.height-90);
	 this.graph.nodes = this.g.updateNodePositions(this.graph.nodes);
	 this.t = 0;
  }
  /* The main executable class, will run full screen
   * */
  static public void main(String args[]) {		 
	  if (args.length ==0){
		  args = new String [] {"vanDeBunt.txt"}; //Default data file, if none is provided		  
	  }
	  
	   PApplet.main(new String[] {"RunGlidgets",args[0]});	  
	}
}

