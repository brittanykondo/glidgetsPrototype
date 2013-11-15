import java.util.ArrayList;

import processing.core.PApplet;


public class Edge {
      int node1, node2; //Id's of the connected nodes 
      int id;
      PApplet parent;
      
      Edge(PApplet p, int id,int n1, int n2){
    	  this.parent = p;
    	  this.id = id;
    	  this.node1 = n1;
    	  this.node2 = n2;
      }
      
      void display(ArrayList<Node> nodes){
    	  parent.stroke(255,200);  
    	  Node n1 = nodes.get(this.node1);
    	  Node n2 = nodes.get(this.node2);
    	  parent.line(n1.x,n1.y,n2.x,n2.y);
      } 
      
      /**Draws an edge at a certain moment in time
       * */
      void display(ArrayList<Node> nodes, int view){
    	  parent.stroke(255);  
    	  Node n1 = nodes.get(this.node1);
    	  Node n2 = nodes.get(this.node2);
    	  parent.line(n1.coords.get(view).x,n1.coords.get(view).y,n2.coords.get(view).x,n2.coords.get(view).y);
      } 
      
     /** void mouseClicked(){
    	  if (parent.mousePressed && parent.dist(this.x,this.y,parent.mouseX,parent.mouseY)<=RADIUS){
    		  System.out.println("clicked");
    	  }
      }*/
      
}
