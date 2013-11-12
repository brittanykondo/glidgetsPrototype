import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import processing.core.*;

public class Test extends PApplet {
  //public Node testNode;
	public ArrayList<Node> nodes;
	public ArrayList<Edge> edges;
  
/**Initialize the view, draw the visualization
   * */
  public void setup() {
    size(800,800);
    background(0);
    //generateTestNodes();
    readLesMis();
    ForceDirectedLayout layout = new ForceDirectedLayout(this.nodes,this.edges);
    layout.makeLayout();
    for (int i = 0;i<layout.numNodes;i++){
    	layout.nodes.get(i).display();    	
    }
    
    for (int i = 0;i<layout.numEdges;i++){
    	layout.edges.get(i).display(layout.nodes);    	
    }
    //testNode = new Node(this,10,10,0);
    //testNode.display();
    
  }
 
  /**Re-draw the screen in response to a mouse event
  * */
  public void draw() {
    stroke(255);    
   // testNode.mouseClicked();
    if (mousePressed) {
      line(mouseX,mouseY,pmouseX,pmouseY);      
    }   
  
  }
  
  /**Creates a test data set of nodes and edges 
   * Saves the nodes and edges into global variables
   */
  public void generateTestNodes(){
	 this.nodes = new ArrayList<Node>();
	 this.edges = new ArrayList <Edge>();
	 
	 for (int i=0;i<10;i++){
		 this.nodes.add(new Node (this,i,""));
	 }
	
	 
	 this.edges.add(new Edge (this,0,0,1));
	 this.edges.add(new Edge (this,1,0,3));
	 this.edges.add(new Edge (this,2,4,5));
	 this.edges.add(new Edge (this,3,6,8));	 

  }
  /**Reads the text file containing the data of the co-occurrence network in Les Miserables
   * Saves the nodes and edges into global variables
   * */
  public void readLesMis(){
	  String filename = "lesmisShort.txt";
	  Scanner scan;
	  int type = 0;
	  int edgeCounter = 0;
	  this.nodes = new ArrayList<Node>();
	  this.edges = new ArrayList <Edge>();
	  
	  try {
			scan = new Scanner(new File(filename));
			while(scan.hasNext())
			{
				// Get the credit hours and quality points and
	            // determine if the student is on warning. If so,
	            // write the student data to the output file.
				String line;
				line = scan.nextLine();
				String[] items = line.split(" ");
				if (items[0].equals("node")){
					type = 0;
				}else if (items[0].equals("edge")){
					type = 1;
				}else{
					if (type==0){ //Create a node
						this.nodes.add(new Node(this,Integer.parseInt(items[0]),items[1]));
					}else{ //Create an edge
						this.edges.add(new Edge (this,edgeCounter,Integer.parseInt(items[0]),Integer.parseInt(items[1])));
						edgeCounter++;
					}
				}				
			}			
			
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		}
  }
}

