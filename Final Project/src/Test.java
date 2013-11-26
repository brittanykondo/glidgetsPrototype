import java.util.ArrayList;
import processing.core.*;


public class Test extends PApplet {   	
    public Slider timeSlider;
    public ForceDirectedGraph graph;
    
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
    }else{
    	this.graph.drawGraph(timeSlider.currentView); //View 0 has no graph, should remove this time slice    
        // this.graph.drawGlobalPersistence(4);
    }    
  }
  
  /**Responds to a mouse down event on the canvas
   * */
  public void mousePressed(){	  
	  timeSlider.selectTick(mouseX,mouseY);
  }
  
  /**Responds to a mouse up event on the canvas
   * */
  public void mouseReleased(){	 	  
      timeSlider.releaseTick();	
      
  }     

}

