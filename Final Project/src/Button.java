import java.awt.Color;

import processing.core.PApplet;
import processing.core.PFont;

/**Creates a new button drawn by processing and detects when it is clicked
 * */
public class Button {
    public boolean clicked;
    public int toggle;
    public int type; //0 - rectangle, 1 - circle
    public int width;
    public int height;
    public int x;
    public int y;
    public Color background;
    public Color textColour;
    public String label;
    public Color toggleColour;
    public PApplet parent;
    public int textX;
    public int textY;
    public int fontSize;
    /**Creates a new rectangle button
     * */
    public Button(PApplet p,int w,int h, int x, int y, Color off, Color on, Color t, String l,int tx, int ty, int fs){
    	this.parent = p;
    	this.width = w;
    	this.height = h;
    	this.x = x;
    	this.y = y;
    	this.background = off;
    	this.toggleColour = on;
    	this.textColour = t;
    	this.label = l;
    	this.type = 0;
    	this.textX = tx;
    	this.textY = ty;
    	this.fontSize = fs;
    }
    /**Creates a new circle button (radio button style)
     * */
    public Button(int r,int x,int y,Color off, Color on, Color t, String l){
    	this.type = 1;
    	this.x = x;
    	this.y = y;
    	this.background = off;
    	this.toggleColour = on;
    	this.textColour = t;
    	this.label = l;
    	this.type = 0;
    	this.width = r;
    }
    /**Draws the button using processing objects
     * */
    public void draw(){
    	if (this.type==0){ //Rectangle button
    		if (this.toggle==1){
    			  parent.fill(this.background.getRGB()); 
    			  parent.stroke(this.toggleColour.getRGB());
    		  }else{
    			  parent.fill(this.background.getRGB()); 
    			  parent.stroke(this.background.getRGB()); 
    		  }
    		  
    		parent.strokeWeight(2);
    		 parent.rect(this.x,this.y,this.width,this.height,6);
    		 PFont font = parent.createFont("Droid Serif",this.fontSize,true);
    	   	 parent.textFont(font);  	  	  
    	   	 parent.fill(this.textColour.getRGB());
    	   	 parent.text(this.label,this.textX,this.textY);  
    	}
    	//TODO: circle radio button (if needed)
    }
    public void toggle(){
    	if (this.type==0){
    		if (parent.mouseX >= this.x && parent.mouseX <= (this.x+this.width) && parent.mouseY >= this.y && parent.mouseY <= 
    				(this.y+this.height)){
      		  this.clicked = true;
      		  if (this.toggle==0){
      			  this.toggle = 1;        			  
      		  }else{
      			  this.toggle = 0;      			  
      		  }	 
      		  return;
      	  }
      	  this.clicked = false;
    	}
    	  
    }
    
}
