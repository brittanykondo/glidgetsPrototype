import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;

//TODO: need fast look up each edge between two nodes, when does it dissappear (no connections between the nodes)
public class Edge {
      int node1, node2; //Id's of the connected nodes 
      String label;
      PApplet parent;
      ArrayList<Integer> persistence; //1 if the edge is drawn, 0 if it disappears
      float globalPersistence;
      int numTimeSlices;    
      
      Edge(PApplet p,String l, int n1, int n2, int numTimeSlices){
    	  this.parent = p;
    	  this.label = l;
    	  this.node1 = n1;
    	  this.node2 = n2;
    	  if (numTimeSlices>0){//Initialize the array storing persistence information
    		  this.persistence = new ArrayList <Integer>();
    		  for (int i=0;i<numTimeSlices;i++){
    			  this.persistence.add(0);
    		  }
    	  }
    	  this.numTimeSlices = numTimeSlices;    	  
      }
      /**Draws an edge in a static graph
       * @param nodes indexed list of all nodes in the graph    
       * */
      void display(ArrayList<Node> nodes){    	  
    	  Node n1 = nodes.get(this.node1);
    	  Node n2 = nodes.get(this.node2);
    	  drawEdge(n1.x,n1.y,n2.x,n2.y,255,1);    	  
      } 
      
      /**Draws an edge at a certain moment in time
       * @param nodes indexed list of all nodes in the graph
       * @param view the current time slice to draw
       * */
      void display(ArrayList<Node> nodes, int view){   	     	 
    	  Node n1 = nodes.get(this.node1);
    	  Node n2 = nodes.get(this.node2);
    	  if (n1.coords.get(view)!=null && n2.coords.get(view)!=null && this.persistence.get(view)!=0){ //Safety Check
    		  drawEdge(n1.coords.get(view).x,n1.coords.get(view).y,n2.coords.get(view).x,n2.coords.get(view).y,255,1);
    	  }
      } 
      /** Visualizes the overall edge persistence (how often is it displayed?) 
       *  at a certain time slice
       * */
      //TODO: what about edges that are not in the view?
      void displayGlobalPersistence(ArrayList<Node> nodes,int view){   
    	  Node n1 = nodes.get(this.node1);
    	  Node n2 = nodes.get(this.node2);
    	  if (n1.coords.get(view)!=null && n2.coords.get(view)!=null && this.persistence.get(view)!=0){
    		  this.globalPersistence = calculateGlobalPersistence();  
    		  drawEdge(n1.coords.get(view).x,n1.coords.get(view).y,n2.coords.get(view).x,n2.coords.get(view).y,255,(float)(this.globalPersistence*10));   
    	  }     	  
      }
      
      /**Calculates the overall persistence: 
       * (number of time slices - number of disappearances)/number of time slices
       * @return the global persistence measure (as probability)
       * */
      float calculateGlobalPersistence(){    	  
    	  int disappearanceCount = 0;
    	  for (int i=0;i<this.persistence.size();i++){
    		  if (this.persistence.get(i)==0) disappearanceCount++;
    	  }    	 
    	  return (float)(this.numTimeSlices - disappearanceCount)/this.numTimeSlices;
      }     
  
      /** Checks to see if two edges are equal.
       *  Two edges are considered equal if they connected to the same nodes
       *  @param e the edge to compare with this (calling object)
       *  @return true if they are equal, false otherwise
       * */
      boolean equalTo(Edge e){
    	  if (this.node1 == e.node1 && this.node2==e.node2){
    		  return true;
    	  }
    	  return false;
      }
      
      /**Animates an edge by re-drawing it according to the interpolated position of
       * the nodes it is attached to
       * @param nodes an array of all nodes in the graph
       * @param start the starting time slice
       * @param end the ending time slice
       * @param interpolation the amount to interpolate by
       * */
      void animate(ArrayList<Node> nodes,int start,int end, float interpolation){
    	  Node n1 = nodes.get(this.node1);
    	  Node n2 = nodes.get(this.node2);
    	  if (this.persistence.get(start)!=0 && this.persistence.get(end)!=0){ //Safety Check     		  
    		  drawEdge(n1.x,n1.y,n2.x,n2.y,255,1);
    	  }/**else if(this.persistence.get(start)!=0 && this.persistence.get(end)==0){
    		  n1 = nodes.get(start);
    		  n2 = nodes.get(start);
    		  drawEdge(n1.x,n1.y,n2.x,n2.y,(int)(interpolation*255),1);
    	  }else if(this.persistence.get(start)==0 && this.persistence.get(end)!=0){
    		  n1 = nodes.get(end);
    		  n2 = nodes.get(end);
    		  drawEdge(n1.x,n1.y,n2.x,n2.y,(int)(interpolation*255),1);
    	  }*/
      }
      /** Renders the edge between the specified coordinates
       *  @param x0,y0,x1,y1 the coordinates
       *  @param alpha the alpha amount (0 to 255) for setting transparency
       *  @param weight the thickness of the stroke
       * */
      void drawEdge(float x0, float y0, float x1, float y1,int alpha,float weight){
    	  parent.strokeWeight(weight);
    	  parent.stroke(200,200,200,alpha);  
		  parent.line(x0, y0,x1,y1);		  
      }
      /** Visualizes the edge persistence across all time slices to guide interaction
       *  @param nodes an array of all nodes in the graph    
       * */
      void drawHintPath(ArrayList <Node> nodes){
    	  Node n1 = nodes.get(this.node1);
    	  Node n2 = nodes.get(this.node2);
    	  System.out.println("drawing.."); 
    	  parent.strokeWeight(5);    	  
    	  float interval = (float)1/this.numTimeSlices;
    	  float startX = n1.x,startY = n1.y;    	  
    	  float endX,endY,interpolation=interval;    	  
    	  
    	  for (int i=0;i<this.numTimeSlices;i++){      
    		  System.out.println(interpolation);
    		  endX = (n2.x - startX)*interpolation + startX;
    		  endY = startY +(n2.y-startY)*((endX-startX)/(n2.x-startX)); 
    		  if (this.persistence.get(i)==0){
    			  parent.stroke(253, 224, 221);
    		  }else{
    			  parent.stroke(250, 159, 181);    	    	  
    		  } 
    		  parent.line(startX,startY,endX,endY);
    		  interpolation +=interval;
    		  startX = endX;
    		  startY = endY;
    	  }    	  
      }
}
