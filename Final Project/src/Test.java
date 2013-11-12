import processing.core.*;

public class Test extends PApplet {
  //public Node testNode;
	public Node [] nodes;
	public Edge [] edges;
	
  public void setup() {
    size(200,200);
    background(0);
    //testNode = new Node(this,10,10,0);
    //testNode.display();
    
  }

  public void draw() {
    stroke(255);    
   // testNode.mouseClicked();
    if (mousePressed) {
      line(mouseX,mouseY,pmouseX,pmouseY);      
    }   
  
  }
  
  //Creates a test dataset of nodes and edges 
  public void generateTestNodes(){
	  Node [] nodes = new Node [10];
	 for (int i=0;i<10;i++){
		 nodes[i] = new Node (this,i);
	 }
	 this.nodes = nodes;
	 Edge [] edges = new Edge [4];
	 edges[0] = new Edge (this,0,0,1);
	 edges[1] = new Edge (this,1,0,3);
	 edges[2] = new Edge (this,2,4,5);
	 edges[3] = new Edge (this,3,6,8);
	 this.edges = edges;

  }
}

