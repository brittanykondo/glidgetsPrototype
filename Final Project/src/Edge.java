import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;

//TODO: need fast look up each edge between two nodes, when does it dissappear (no connections between the nodes)
public class Edge {
      int node1, node2; //Id's of the connected nodes 
      String label;
      PApplet parent;
      ArrayList<Integer> persistence; //1 if the edge is drawn, 0 if it disappears
      
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
      }
      /**Draws an edge in a static graph
       * @param nodes indexed list of all nodes in the graph    
       * */
      void display(ArrayList<Node> nodes){    	  
    	  Node n1 = nodes.get(this.node1);
    	  Node n2 = nodes.get(this.node2);
    	  drawEdge(n1.x,n1.y,n2.x,n2.y,255);    	  
      } 
      
      /**Draws an edge at a certain moment in time
       * @param nodes indexed list of all nodes in the graph
       * @param view the current time slice to draw
       * */
      void display(ArrayList<Node> nodes, int view){   	     	 
    	  Node n1 = nodes.get(this.node1);
    	  Node n2 = nodes.get(this.node2);
    	  if (n1.coords.get(view)!=null && n2.coords.get(view)!=null && this.persistence.get(view)!=0){ //Safety Check
    		  drawEdge(n1.coords.get(view).x,n1.coords.get(view).y,n2.coords.get(view).x,n2.coords.get(view).y,255);
    	  }
      } 
      /** Finds an edge in an ArrayList of edges
       *  @param Arraylist of edges to search within
       *  @return index of the edge that was found, -1 otherwise
       * */
      int find(ArrayList<Edge> edges){
    	  for (int i=0;i<edges.size();i++){
    		  if (equalTo(edges.get(i),this)){
    			  return i;
    		  }
    	  }
    	  return -1;
      }
      /** Checks to see if two edges are equal.
       *  Two edges are considered equal if they connected to the same nodes
       *  @param e1, e2 the edges to compare
       *  @return true if they are equal, false otherwise
       * */
      boolean equalTo(Edge e1, Edge e2){
    	  if (e1.node1 == e2.node1 && e1.node2==e2.node2){
    		  return true;
    	  }
    	  return false;
      }
     /** void mouseClicked(){
    	  if (parent.mousePressed && parent.dist(this.x,this.y,parent.mouseX,parent.mouseY)<=RADIUS){
    		  System.out.println("clicked");
    	  }
      }*/
      
      /**Animates an edge by re-drawing it according to the interpolated position of
       * the nodes it is attached to
       * @param start the starting time slice
       * @param end the ending time slice
       * @interpolation the amount to interpolate by
       * */
      void animate(ArrayList<Node> nodes,int start,int end, float interpolation){
    	  Node n1 = nodes.get(this.node1);
    	  Node n2 = nodes.get(this.node2);
    	  if (this.persistence.get(start)!=0 && this.persistence.get(end)!=0){ //Safety Check     		  
    		  drawEdge(n1.x,n1.y,n2.x,n2.y,255);
    	  }else if(this.persistence.get(start)!=0 && this.persistence.get(end)==0){
    		  n1 = nodes.get(start);
    		  n2 = nodes.get(start);
    		  drawEdge(n1.x,n1.y,n2.x,n2.y,(int)(interpolation*255));
    	  }else if(this.persistence.get(start)==0 && this.persistence.get(end)!=0){
    		  n1 = nodes.get(end);
    		  n2 = nodes.get(end);
    		  drawEdge(n1.x,n1.y,n2.x,n2.y,(int)(interpolation*255));
    	  }
      }
      /** Renders the edge between the specified coordinates
       *  @param x0,y0,x1,y1 the coordinates
       *  @param alpha the alpha amount (0 to 255) for setting transparency
       * */
      void drawEdge(float x0, float y0, float x1, float y1,int alpha){
    	  parent.stroke(200,200,200,alpha);  
		  parent.line(x0, y0,x1,y1);
      }
}
