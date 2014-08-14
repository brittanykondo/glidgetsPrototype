import java.awt.Color;
import java.util.ArrayList;

import processing.core.*;
/**A node on a graph, for displaying it and saving important properties
 * */

public class Node {
      float x, y;      
      int id;
      String label;
      double value;     
    
      PApplet parent;
      ArrayList<Integer> persistence; //Ordered by time
      ArrayList<Integer> degrees; //Ordered by time, degree of the node     
      float globalPersistence; //Percentage of time the node does not disappear
      int numTimeSlices; 
      float hintSegmentAngle; //The angle of each segment along the hint path
      boolean dragging;
      int maxDegree; //For scaling visualizations of relative degree amount (only w.r.t to this node's degree changes)
      ArrayList <Coordinate> hintAngles; //View angles along the hint path
      ArrayList <Integer> incidentEdges; //Edges that the node forms with other nodes over time
            
      //Class Constants
      static int MIN_WEIGHT = 6;
      static int MAX_WEIGHT = 15;
      static final float RADIUS = 32;
      
      //Display colours     
      static Colours getColours = new Colours();
      static Color presentColour = getColours.BlueGreen;         
      static Color absentColour = getColours.LightGrey;
      static Color presentColour_darker = getColours.DarkBlue;
      static Color absentColour_darker = getColours.DarkGrey;
      static Color yearMarkColour = getColours.BlueSugarOrangeDarker; 
      static Color anchorColour = getColours.DarkOrange;  
      static Color yearLabel = getColours.Ink;    
      static Color nodeColour = getColours.SearchLightPink;
      static Color nodeLabelColour = getColours.Ink;
      static Color aggregationColour = getColours.ArcTreesAlgaeGreen;
     
      /** Constructor for creating a Node
       * @param p Reference to processing applet for drawing with processing commands
       * @param id unique id to discern nodes
       * @param l label to be drawn inside the node
       * @param t  the number of time slices in the dataset
       * */
      Node(PApplet p,int id,String l,int t){
    	   
    	  //Save the parameters
    	  this.label = l;
    	  this.parent = p;
    	  this.id = id;    
    	  this.numTimeSlices = t; 
    	  this.hintSegmentAngle = 0.0f;
    	  
    	  //Initialize other class variables
    	  this.x = 0;
    	  this.y = 0;  
    	  this.dragging = false;    	  
    	  this.persistence = new ArrayList<Integer>();
    	  this.degrees = new ArrayList<Integer>();
    	  this.maxDegree = -1;
    	  setHintAngles();   	  
      }        
      
      /**Renders the node at its position at a certain moment in time
       * @param view  the current time point
       * @param aggNodes  a list of selected nodes or nodes attached to selected edges
       * @param fade    the amount of transparency
       * @param  outlineOnly  true if only the outline of the node should be displayed (when attached to an edge)
       * */
      void display(int view,ArrayList<Integer> aggNodes,float fade, boolean outlineOnly){    
    	  if (view==-1){ //In global view
    		  drawNode((int)(255*fade),false);
    	  }else if (aggNodes.contains(this.id) && this.persistence.get(view)==0){ //Disappearing node case
    		  if (outlineOnly){ //Node attached to an edge
    			  drawNode((int)(255*fade),outlineOnly);
    		  }else{
    			  drawNodeLabel((int)(255*fade));
    		  }    		     	
    	  }else if (this.persistence.get(view)==1){
    		  drawNode((int)(255*fade),false);
    	  }
      }
      /**Finds and saved the degree (number of edges incident on the node) of the node
       * for each time slice
       * @param an array of all edges present in the graph (for each time slice) 
       * */
      void setNodeDegree(ArrayList<Edge> edges){
    	  this.incidentEdges = new ArrayList <Integer>();
    	  //Initialize the array for saving degree over time
    	  for (int i=0;i<this.numTimeSlices;i++){
    		  this.degrees.add(0);
    	  }
    	  //For each time slice, count the number of edges containing the node
    	  Edge currentEdge;
    	  int degreeCount;    	  
    	  
    	  for (int i=0;i<edges.size();i++){
    		  currentEdge = edges.get(i);
    		  if (currentEdge.node1==this.id || currentEdge.node2 == this.id){    
    			  this.incidentEdges.add(i);    			  
    			  for (int t=0;t<this.numTimeSlices;t++){
    				  if (currentEdge.persistence.get(t)==1){
    					  degreeCount = this.degrees.get(t)+1;
    					  if (degreeCount>this.maxDegree) this.maxDegree = degreeCount;
    					  this.degrees.set(t, degreeCount);
    				  }
    			  }
    		  }
    	  }    	 
      }
      /**Saves the hint path angles which separate different views along the hint path
       * (to check when the mouse is dragging along it)
       * */
      void setHintAngles(){
    	  this.hintAngles = new ArrayList<Coordinate>();
    	  float interval = parent.TWO_PI/this.numTimeSlices;       	 
    	  float startAngle, endAngle;    	 
    	  for (int i=0;i<this.numTimeSlices;i++){    		  
    		  //First, calculate the angles for the current time interval
    		  startAngle = (i*interval);    		  
    		  endAngle = (startAngle + interval);       		 
    		  
    		  //Add the start and end angles for the current time slice (i), off-setting it by half pi
    		  //So that the beginning of time is at the top of the node (like a clock)
    		  
    		  this.hintAngles.add(new Coordinate(startAngle, endAngle));
    		  //System.out.println(i+" "+startAngle*180/Math.PI+" "+endAngle*180/Math.PI);
    	  }
    	  this.hintSegmentAngle = interval;
      }

      /** Renders the node at the specified position and saves the positions in class variables
       *  @param x,y the position coordinates
       *  @param alpha the amount of transparency (0 to 255)
       * */
      void drawNode(int alpha,boolean outlineOnly){ 
    	  if (outlineOnly){
    		  parent.stroke(nodeColour.getRed(),nodeColour.getGreen(),nodeColour.getBlue(),alpha);    	  
        	  parent.strokeWeight(2) ;
        	  parent.noFill();
        	  parent.ellipse(this.x,this.y,RADIUS,RADIUS); 
    	  }else{
    		  parent.fill(nodeColour.getRed(),nodeColour.getGreen(),nodeColour.getBlue(),alpha);    	  
        	  parent.noStroke();    	 
        	  parent.ellipse(this.x,this.y,RADIUS,RADIUS);         	  	   
    	  }
    	  drawNodeLabel(alpha); 
      }    
      /**Draws only the label, centered on top of the node
       * */
      void drawNodeLabel(int alpha){    	  
    	  PFont font = parent.createFont("Century Gothic",12,true);
	   	  parent.textFont(font);	   	  
	   	  parent.fill(nodeLabelColour.getRed(),nodeLabelColour.getGreen(),nodeLabelColour.getBlue(),alpha);
	   	  parent.textAlign(parent.CENTER);
	   	  parent.text(this.label, this.x,this.y+4); 
      }
      /** Adds highlights to the nodes to show global persistence
       *     
      void drawGlobalPersistenceHighlights(){
    	  //Piechart Glyph:
    	  //parent.stroke(206,18,86,170);   
    	  parent.stroke(67,162,202,255);   
    	  parent.strokeWeight(MIN_WEIGHT);
    	  parent.strokeCap(parent.SQUARE);
    	  parent.noFill();   
    	  float startAngle = 0;
    	  float endAngle = parent.TWO_PI*this.globalPersistence;
    	  parent.arc(this.x, this.y, RADIUS+MIN_WEIGHT, RADIUS+MIN_WEIGHT, startAngle-parent.HALF_PI, endAngle-parent.HALF_PI);        	  
      }*/
      /**Calculates the overall persistence: 
       * (number of time slices - number of disappearances)/number of time slices
       * @return the global persistence measure (as probability)
       * 
      float calculateGlobalPersistence(){    	  
    	  int disappearanceCount = 0;
    	  for (int i=0;i<this.coords.size();i++){
    		  if (this.coords.get(i)==null) disappearanceCount++;
    	  }    	 
    	  return (float)(this.numTimeSlices - disappearanceCount)/this.numTimeSlices;
      }*/
     
      /** Visualizes the overall node persistence (how often is it displayed?) 
       *  at a certain time slice
       *  @param the current view to draw at
       *  @param showAllHighlights true, if all node persistence highlights should be drawn
       * */ 
      void drawGlobalPersistenceHighlights(int view,boolean showAllHighlights,float fade){
    	  
    	  if (showAllHighlights || this.persistence.get(view)==1){
    		  parent.strokeCap(parent.SQUARE);        	 
        	  parent.noFill();
        	  parent.stroke(presentColour.getRed(),presentColour.getGreen(),presentColour.getBlue(),(255*fade));
    		  parent.strokeWeight(MIN_WEIGHT);
        	  for (int i=0;i<this.numTimeSlices;i++){     		   		 
        		  if (this.persistence.get(i)==1){    			  
                	  parent.arc(this.x, this.y, RADIUS+MIN_WEIGHT, RADIUS+MIN_WEIGHT,this.hintAngles.get(i).x-parent.HALF_PI, 
                			  (this.hintAngles.get(i).y-parent.HALF_PI-0.06f));       			      			  
        		  }       	  	
        	  }
        	  drawNode((int)(255*fade),false);
    	  }
    	        	  
      }     
      /**Checks to see if the mouse event is in the node's circular area
       * @param view  the current view       
       * @return [index of the selected, type of selection (2 is drawing around, 1 is clicking)], -1 otherwise
       * */
      int [] selectNode(int view,boolean selectedDisappeared){
    	  Integer p = this.persistence.get(view);
    	  //TODO:Quick fix for allowing nodes that have disappeared but are part of a query can be selected, refactor this later
    	  if (p==1 && parent.mousePressed && parent.dist(this.x,this.y,parent.mouseX,parent.mouseY)<=(RADIUS/2)){  //Clicking directly on a node  			    		 
    		  return new int [] {this.id,1};
    	  } else if (selectedDisappeared  && parent.mousePressed && parent.dist(this.x,this.y,parent.mouseX,parent.mouseY)<=(RADIUS/2)){
    		  return new int [] {this.id,1};
    	  }    	  
    	  /**else if (coord !=null && parent.mousePressed && parent.dist(coord.x,coord.y,parent.mouseX,parent.mouseY)<=(RADIUS/2)+MAX_WEIGHT){  //Drawing around a node or clicking near it  			    		 
    		  return new int [] {this.id,2};
    	  } */  	 
    	  return new int [] {-1,-1};
      }
      
      /**Checks to see if the mouse up event is in the node's circular area
       * @param view  the current view
       * @param selectedNode the currently selected node (if any)
       * @return index of the released if it is a different node than what is selected
       *  -1 otherwise 
       * */
      int releaseNode(int view, int selectedNode,boolean selectDisappeared){    	
    	  Integer p = this.persistence.get(view);
    	  if ((p ==1||selectDisappeared) && parent.dist(this.x,this.y,parent.mouseX,parent.mouseY)<=RADIUS && this.id!=selectedNode){    		 
    		  return this.id;   		  
    	  }    	 
    	  return -1;
      }      
      /**Draws the anchor as an elastic attached to the mouse when it is dragging around a node
       * @param mouseAngle the angle of the mouse w.r.t to the center of the node
       * @param fixAnchor, if 1, anchor should only extend vertically (staying at a fixed angle)
       *                   if 0, anchor should move with the mouse angle
       * @param view      the time slice (to draw next to the anchor)
       **/
      void animateAnchor(float mouseAngle, int fixAnchor,int view){
    	 
    	  parent.strokeCap(parent.ROUND);
    	  parent.stroke(anchorColour.getRGB());
          parent.strokeWeight(4);          
          float x1 = (float) (this.x + RADIUS/2*Math.cos(mouseAngle));
          float y1 = (float) (this.y + RADIUS/2*Math.sin(mouseAngle));     
          
          PFont font = parent.createFont("Droid Sans",20,true);
	   	  parent.textFont(font);
	   	  parent.fill(yearLabel.getRGB());
          
	   	  if (fixAnchor==1){
        	  float dist = parent.dist(x1, y1, parent.mouseX, parent.mouseY);
        	  float x2 = (float) (this.x + dist*Math.cos(mouseAngle));
              float y2 = (float) (this.y + dist*Math.sin(mouseAngle));              
        	  parent.line(x1, y1, x2, y2);
        	  parent.text(view, x2, y2);
          }else{
        	  parent.line(x1, y1, parent.mouseX, parent.mouseY);
        	  parent.text(view, parent.mouseX, parent.mouseY);
          }   
          
      }
      /**Animates the time slice indicator along the hint path when the time slider is dragged
       * @param mouseAngle the angle of the mouse w.r.t to the center of the node
       * @param fixAnchor, if 1, anchor should only extend vertically (staying at a fixed angle)
       *                   if 0, anchor should move with the mouse angle
       **/
      void animateIndicator(float interpolation,int view){
    	  //parent.stroke(67,162,202,255); 
    	  parent.strokeCap(parent.ROUND);
    	  parent.stroke(206,18,86,255);
          parent.strokeWeight(4);            
            					 
		  int weight = MIN_WEIGHT+(int)(((float)this.degrees.get(view)/this.maxDegree)*MAX_WEIGHT);			  
		  float mouseAngle =  ((parent.TWO_PI/this.numTimeSlices)*interpolation + this.hintAngles.get(view).x)-parent.HALF_PI;
		  
          float x1 = (float) (this.x + RADIUS/2*Math.cos(mouseAngle));
          float y1 = (float) (this.y + RADIUS/2*Math.sin(mouseAngle));     
          float x2 = (float) (this.x + (RADIUS/2+weight)*Math.cos(mouseAngle));
          float y2 = (float) (this.y + (RADIUS/2+weight)*Math.sin(mouseAngle));
          parent.line(x1, y1, x2, y2);              	  
      }
     
      /** Draws a hint path (following a persistence array)          
       *  @param persistence Array of persistence information of the node
       *  @param view the current view of the visualization 
       * */      
      void drawHintPath(int view){      	
    	  float x1,y1,x2,y2,weight; //For drawing the ticks in middle of segments    	 
    	  float nodeStrokeWeight=0;    	 
    	  
    	  for (int i=0;i<this.numTimeSlices;i++){ 
    		  //For now, just drawing the original degree amount when node is present, not aggregating it    		 
    		  if (persistence.get(i)==0){
    			  parent.stroke(absentColour.getRGB()); 
    			  if (i==view){
    				  parent.stroke(yearMarkColour.getRGB());
    			  }
    			  weight = 3;
    			  parent.strokeWeight(weight);
    		  }else{    			  
    			  parent.stroke(presentColour.getRGB()); 
    			  if (i==view){
    				  parent.stroke(yearMarkColour.getRGB());
    			  }
    			  weight = MIN_WEIGHT+(int)(((float)this.degrees.get(i)/this.maxDegree)*MAX_WEIGHT);
    			  parent.strokeWeight(weight);
    		  }      		  
        	  
        	  parent.strokeCap(parent.SQUARE);        	 
        	  parent.noFill();
        	  //Offset by half pi so that beginning of time lies on top of the node (like a clock layout)
        	  parent.arc(this.x, this.y, RADIUS+weight+nodeStrokeWeight, RADIUS+weight+nodeStrokeWeight,
        			  this.hintAngles.get(i).x-parent.HALF_PI, this.hintAngles.get(i).y-parent.HALF_PI);  
        	  
        	  //Draw the ticks along the segments
        	  x1 = (float) (this.x + (RADIUS/2)*Math.cos(this.hintAngles.get(i).x-parent.HALF_PI));
              y1 = (float) (this.y + (RADIUS/2)*Math.sin(this.hintAngles.get(i).x-parent.HALF_PI));    
              
              x2 = (float) (this.x + (RADIUS/2+weight)*Math.cos(this.hintAngles.get(i).x-parent.HALF_PI));
              y2 = (float) (this.y + (RADIUS/2+weight)*Math.sin(this.hintAngles.get(i).x-parent.HALF_PI));
              
              if (persistence.get(i)==0){
    			  parent.stroke(absentColour_darker.getRGB());
    			  parent.strokeWeight(2.5f);
    			  //parent.strokeWeight(1.0f);
    		  }else{    			  
    			  parent.stroke(presentColour_darker.getRGB());
    			  parent.strokeWeight(2.5f);
    			  //parent.strokeWeight(1.0f);
    		  }               
        	  parent.line(x1, y1, x2, y2);
    	  }  
      }
      /** Draws an aggregated hint path          
       *  @param persistence Array of persistence information for the aggregated nodes 
       *  @param view the current view of the visualization 
       * */ 
      //TODO: copied code for drawhintPath() function, refactor this
      void drawAggregatedHintPath(ArrayList<Integer> persistence,int view){ 
    	//float angleOffset = parent.HALF_PI + this.hintSegmentAngle/2;
    	  float x1,y1,x2,y2,weight; //For drawing the ticks in middle of segments    	 
    	  float nodeStrokeWeight=0; 
    	  float borderWeight = 3;    	
    	  
    	  for (int i=0;i<this.numTimeSlices;i++){     		  
    		  parent.strokeCap(parent.SQUARE);        	 
        	  parent.noFill();
        	  
    		  //For now, just drawing the original degree amount when node is present, not aggregating it    		 
    		  if (persistence.get(i)==0){
    			  weight = 3;
    			  parent.stroke(aggregationColour.getRGB(), 255);
    			  parent.strokeWeight(weight+borderWeight);
    			  parent.arc(this.x, this.y, RADIUS+weight+nodeStrokeWeight+borderWeight, RADIUS+weight+nodeStrokeWeight+borderWeight,
            			  this.hintAngles.get(i).x-parent.HALF_PI, this.hintAngles.get(i).y-parent.HALF_PI); 
    			  
    			  parent.stroke(absentColour.getRGB()); 
    			  if (i==view){
    				  parent.stroke(yearMarkColour.getRGB());
    			  }    			  
    			  parent.strokeWeight(weight);
    			  parent.arc(this.x, this.y, RADIUS+weight+nodeStrokeWeight, RADIUS+weight+nodeStrokeWeight,
            			  this.hintAngles.get(i).x-parent.HALF_PI, this.hintAngles.get(i).y-parent.HALF_PI); 
    		  }else{   
    			  weight = MIN_WEIGHT+(int)(((float)this.degrees.get(i)/this.maxDegree)*MAX_WEIGHT);
    			  parent.stroke(aggregationColour.getRGB(),255);
    			  parent.strokeWeight(weight+borderWeight);
    			  parent.arc(this.x, this.y, RADIUS+weight+nodeStrokeWeight+borderWeight, RADIUS+weight+nodeStrokeWeight+borderWeight,
            			  this.hintAngles.get(i).x-parent.HALF_PI, this.hintAngles.get(i).y-parent.HALF_PI); 
    			 
    			  parent.stroke(presentColour.getRGB()); 
    			  if (i==view){
    				  parent.stroke(yearMarkColour.getRGB());
    			  }    			 
    			  parent.strokeWeight(weight);
    			  parent.arc(this.x, this.y, RADIUS+weight+nodeStrokeWeight, RADIUS+weight+nodeStrokeWeight,
            			  this.hintAngles.get(i).x-parent.HALF_PI, this.hintAngles.get(i).y-parent.HALF_PI); 
    		  }      		  
        	  
        	 //Draw the ticks along the segments
        	  x1 = (float) (this.x + (RADIUS/2)*Math.cos(this.hintAngles.get(i).x-parent.HALF_PI));
              y1 = (float) (this.y + (RADIUS/2)*Math.sin(this.hintAngles.get(i).x-parent.HALF_PI));    
              
              x2 = (float) (this.x + (RADIUS/2+weight)*Math.cos(this.hintAngles.get(i).x-parent.HALF_PI));
              y2 = (float) (this.y + (RADIUS/2+weight)*Math.sin(this.hintAngles.get(i).x-parent.HALF_PI));
              
              if (persistence.get(i)==0){
    			  parent.stroke(absentColour_darker.getRGB());
    			  parent.strokeWeight(2.5f);
    			  //parent.strokeWeight(1.0f);
    		  }else{    			  
    			  parent.stroke(presentColour_darker.getRGB());
    			  parent.strokeWeight(2.5f);
    			  //parent.strokeWeight(1.0f);
    		  }               
        	  parent.line(x1, y1, x2, y2);
    	  }    	  
    	  
      }
      /**Animates a node by interpolating its position between two time slices
       * @param start the starting time slice
       * @param end the ending time slice
       * @param interpolation the amount to interpolate by
       * @param aggNodes  list of selected nodes or nodes attached to selected edges
       * @param outlineOnly  show only the outline of a disappearing node (when attached to an edge)
       * */
      void animate(int start,int end, float interpolation,ArrayList<Integer> aggNodes,boolean outlineOnly){
    	  Integer startPosition = this.persistence.get(start);
    	  Integer endPosition = this.persistence.get(end);     	 
    	 
    	  int interpolationAmt=0;  	
    	  
    	  if (startPosition ==1 && endPosition ==1){   
    		  interpolationAmt = 255;
    	  }else if (startPosition==0 && endPosition==1){ //Node is fading in 
    		  interpolationAmt = easeInExpo(interpolation,0,255,1);    		    		  
    	  }else if (startPosition==1 && endPosition==0){ //Node is fading out    		  
    		  interpolationAmt = easeOutExpo((1-interpolation),0,255,1);   		  		
    	  }    
    	  
    	  if (aggNodes.contains(this.id) && interpolationAmt!=255){
    		  if (outlineOnly){
    			  drawNode(255,outlineOnly); //Kind of redundant...    			  
    		  }else{
    			  drawNode(interpolationAmt,false);
    			  drawNodeLabel(255);
    		  }   	    		 
    	  }else{
    		  drawNode(interpolationAmt,false);
    	  }    	 
      }  
      /** Function to compute the transparency of a node fading in
       *  From: http://www.gizma.com/easing/#cub1
       *  @param t current time
       *  @param b start value
       *  @param c change in value
       *  @param d duration
       * */
      int easeInExpo(float t, float b, float c, float d) {
    	  return (int)(c * Math.pow( 2, 10 * (t/d - 1) ) + b);    	   
      }
      /** Function to compute the transparency of a node fading out
       *  From: http://www.gizma.com/easing/#cub1
       *  @param see easeInExpo();
       * */
      int easeOutExpo(float t, float b, float c, float d) {
    	 // t /= d;
    		//return (int)(-c * t*(t-2) + b);
    	  return (int) (c * ( -Math.pow( 2, -10 * t/d ) + 1 ) + b);
    	}
      
      /**Linearly interpolates the position of a node 
       * @param c0 first coordinate of a point on the line
       * @param c1 second coordinate of a point on the line
       * @return Coordinate: new coordinates using the lerp formula
       * */
      Coordinate interpolatePosition(Coordinate c0, Coordinate c1, float interpAmount){
    	  float x0 = c0.x;
    	  float y0 = c0.y;
    	  float x1 = c1.x;
    	  float y1 = c1.y;
    	  float x = (x1 - x0)*interpAmount + x0;
		  float y = y0 +(y1-y0)*((x-x0)/(x1-x0)); 
		  return new Coordinate(x,y);
      }
}
