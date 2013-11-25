import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import processing.core.PApplet;

/**Manages the elements of a force directed graph visualization which evolves over time
 * This does not generate the layout, only reads in a data file containing pre-computed 
 * coordinates
 * */
public class ForceDirectedGraph {	 
	 public ArrayList<Node> nodes;
     public ArrayList<Edge> edges;    
     PApplet parent;
     public int numTimeSlices;
     
     /**Creates a new graph manager which generates or parses and saves the data
      * necessary for drawing the dynamic graph
      * @param p   Processing applet for drawing the graph elements
      * @param dataFile   name of the data file with connectivity and node positions
      * @param time   the number of time slices
      * */
     public ForceDirectedGraph(PApplet p,String dataFile,int time){
    	 this.parent = p; 
    	 this.numTimeSlices = time;
         readGraphDataFile(dataFile);            
     }     
     
     /**Calls the display function for all nodes and edges, which will
      * render them onto the screen for a certain view
      * */
     public void drawGraph(int view){    	   	  
    	
		 for (int row = 0;row<this.edges.size();row++){ 			
  	          this.edges.get(row).display(this.nodes,view);    	  	    	        	   	
  	     }	 
		 
		 for (int i = 0;i<this.nodes.size();i++){
  		   this.nodes.get(i).display(view);    	
  	     }  
     }
     /**Reads the text file containing the node positions and edges for each time slice
      * */
     public void readGraphDataFile(String filename){    	 
       	  Scanner scan;
       	  int time = 0;
       	  int nodeId = 0;	 
       	  boolean nodesDone = false;
       	  Edge newEdge;
       	  int foundEdge;
       	  
       	  this.nodes = new ArrayList<Node>();
       	  this.edges = new ArrayList <Edge>	();
       	  
       	  try {
       			scan = new Scanner(new File(filename));
       			while(scan.hasNext())
       			{   				
       				String line;
       				line = scan.nextLine();
       				String[] items = line.split(" ");
       				if (items[0].equals("node")){ //Save the node
       					nodeId = Integer.parseInt(items[1]);       					
       					this.nodes.add(new Node(this.parent,nodeId,items[1]));
       				}else if (items[0].equals("time")){ //Save the time slice
       					nodesDone = true;    
       					time = Integer.parseInt(items[1]);
       					//this.edges.add(new ArrayList <Edge>());
       				}else{
       					if (nodesDone){ //Save the edge information       						
       						newEdge = new Edge (this.parent,"",Integer.parseInt(items[0]),Integer.parseInt(items[1]),this.numTimeSlices);
       						foundEdge = newEdge.find(this.edges);
       						if (foundEdge==-1){ //If edge doesn't exist, create it       							
       							newEdge.persistence.set(time, 1);
       							this.edges.add(newEdge);
       						}else{//Otherwise just update the persistence info
       							this.edges.get(foundEdge).persistence.set(time, 1);
       						}           					
           				}else{//Save the node coordinates
           					if (items[0].equals("null")){ //Node does not appear for this time slice
           						this.nodes.get(nodeId).coords.add(null);
           					}else{           						
           						this.nodes.get(nodeId).coords.add(new Coordinate(Float.parseFloat(items[0]),Float.parseFloat(items[1])));
           					}       						
           				}
       				}       				
       				
       			}	       			
       		} catch (FileNotFoundException e) {			
       			e.printStackTrace();
       		}         	 
     }
}
