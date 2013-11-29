import java.util.ArrayList;
import processing.core.*;
//import org.gicentre.handy.*;


public class Test extends PApplet {   	
    public Slider timeSlider;
    public ForceDirectedGraph graph;  
 
    //public int toggleGlobalPersistence;
    //public HandyRenderer sketchyEffect;
    
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
    //this.sketchyEffect = new HandyRenderer(this);
  }
 
  /**Re-draw the view
  * */
  public void draw() {   
     
    if (timeSlider.dragging){
    	background(25,25,25); 
    	timeSlider.drag(mouseX);
    	timeSlider.drawSlider();  
    	this.graph.animateGraph(timeSlider.currentView, timeSlider.nextView, timeSlider.interpAmount);
    }else if (graph.dragging){ //Issue query sketch   	
    	sketch();
    }else if (graph.selectedNode !=-1 && graph.releasedNode!= -1){ //Show edge hint path between nodes  
    	background(25,25,25);
    	timeSlider.drawSlider();  
    	graph.connectNodes();
    	this.graph.drawGraph(timeSlider.drawingView);    	
    }else{    	 
    	background(25,25,25);     	
        timeSlider.drawSlider();  
    	this.graph.drawGraph(timeSlider.drawingView);
    }      
  }
  
  /**Adds a trail to the mouse movement to simulate the appearance of sketching
   * This is displayed when the mouse has dragged from one node to another
   * */
  public void sketch(){
	  stroke(255);
	  strokeWeight(1);
	  /**sketchyEffect.*/line(pmouseX,pmouseY,mouseX,mouseY);
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
  //Maybe can use key press to aggregrate queries? (e.g., select multiple nodes)
  public void keyPressed(){
	  System.out.println("key pressed");
  } 
}

