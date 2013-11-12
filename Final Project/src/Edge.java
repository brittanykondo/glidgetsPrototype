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
      
}
