import java.util.ArrayList;

import processing.core.PApplet;

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
    	  parent.stroke(255,200);  
    	  Node n1 = nodes.get(this.node1);
    	  Node n2 = nodes.get(this.node2);
    	  parent.line(n1.x,n1.y,n2.x,n2.y);
      } 
      
      /**Draws an edge at a certain moment in time
       * @param nodes indexed list of all nodes in the graph
       * @param view the current time slice to draw
       * */
      void display(ArrayList<Node> nodes, int view){   	 
    	 
    	  Node n1 = nodes.get(this.node1);
    	  Node n2 = nodes.get(this.node2);
    	  if (n1.coords.get(view)!=null && n2.coords.get(view)!=null && this.persistence.get(view)!=0){ //Safety Check   
    		  parent.stroke(255,100);  
    		  parent.line(n1.coords.get(view).x,n1.coords.get(view).y,n2.coords.get(view).x,n2.coords.get(view).y);
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
      
}
