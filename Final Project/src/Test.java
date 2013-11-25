import java.util.ArrayList;
import processing.core.*;


public class Test extends PApplet {   	
    public Slider timeSlider;
    public ForceDirectedGraph graph;
    
/**Initialize the view, draw the visualization
   * */
public void setup() {
    size(900,900);
    background(25,25,25);   
    
    this.graph = new ForceDirectedGraph(this,"savedGraphData.txt",6);
    this.graph.drawGraph(1); //View 0 has no graph, should remove this time slice
    
	ArrayList <String> testLabels = new ArrayList <String>();
	for (int i=0;i<this.graph.numTimeSlices;i++){
		testLabels.add(""+i);
	}    
    this.timeSlider = new Slider(this,testLabels,50);
    this.timeSlider.drawSlider();
  }
 
  /**Re-draw the view
  * */
  public void draw() {
    stroke(255);  
    timeSlider.drag(mouseX);
    timeSlider.redrawTick();
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

