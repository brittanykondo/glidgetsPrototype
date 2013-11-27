import java.util.ArrayList;
import processing.core.*;


public class Test extends PApplet {   	
    public Slider timeSlider;
    public ForceDirectedGraph graph;
    public float startX,startY; //Saved mouse coordinates for sketching the edge between nodes
    //public int toggleGlobalPersistence;
    
/**Initialize the view, draw the visualization
   * */
public void setup() {
    size(800,800);    
    this.graph = new ForceDirectedGraph(this,"savedGraphData.txt",6);   
	ArrayList <String> testLabels = new ArrayList <String>();
	for (int i=0;i<this.graph.numTimeSlices;i++){
		testLabels.add(""+i);
	}    
    this.timeSlider = new Slider(this,testLabels,70);     
  }
 
  /**Re-draw the view
  * */
  public void draw() {
	background(25,25,25); 
    stroke(255);  
    timeSlider.drag(mouseX);
    timeSlider.drawSlider();   
    if (timeSlider.dragging){
    	this.graph.animateGraph(timeSlider.currentView, timeSlider.nextView, timeSlider.interpAmount);
    }else if (graph.dragging){ //Draw a line used to sketch    	
    	this.graph.drawGraph(timeSlider.currentView);
    	sketch();
    }else if (graph.selectedNode !=-1 && graph.releasedNode!= -1){ //Show edge hint path between nodes    	
    	graph.connectNodes();
    	this.graph.drawGraph(timeSlider.currentView);    	
    }else{    	 
    	this.startX = mouseX;
    	this.startY = mouseY;
    	this.graph.drawGraph(timeSlider.currentView);
    } 
  }
  
  /**Adds a trail to the mouse movement to simulate the appearance of sketching
   * This is displayed when the mouse has dragged from one node to another
   * */
  public void sketch(){
	  stroke(255);
	  line(this.startX,this.startY,mouseX,mouseY);
  }

  /**Responds to a mouse down event on the canvas
   * */
  public void mousePressed(){	  
	  timeSlider.selectTick(mouseX,mouseY);
	  graph.selectNodes();
  }
  
  /**Responds to a mouse up event on the canvas
   * */
  public void mouseReleased(){	 	  
      timeSlider.releaseTick();	 
      graph.releaseNodes();
  }     

}

