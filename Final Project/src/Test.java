import java.util.ArrayList;
import processing.core.*;


public class Test extends PApplet {   	
    public Slider timeSlider;
    public GraphManager graph;
    
/**Initialize the view, draw the visualization
   * */
public void setup() {
    size(900,900);
    background(0);   
    
    this.graph = new GraphManager(this,null);
    this.graph.drawGraph();    
    
	ArrayList <String> testLabels = new ArrayList <String>();
    testLabels.add("0");
    testLabels.add("1");
    testLabels.add("2");
    testLabels.add("3");
    testLabels.add("4");
    
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

