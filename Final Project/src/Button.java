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
    public int hover;   
    public String label;    
    public PApplet parent;
    public int textX;
    public int textY;
    public int fontSize;
    
    //Colours    
    public Colours getColours = new Colours();
    //public Color background = getColours.MedGrey;
    public Color background = getColours.LightSlate2;
    //public Color background = getColours.InkBrighter;
    public Color textColour = getColours.Ink;
    //public Color toggleColour = new Color(220,220,220,255);
    public Color toggleColour = getColours.LightSlate3;
    public Color toggleBorderColour = getColours.LightSlate2;
    //public Color borderColour = getColours.charcolGrey;
    public Color borderColour =  getColours.LightSlate2;
    public Color clickedColour = toggleColour;
    public Color clickedBorderColour = toggleBorderColour;
    
    /**Creates a new rectangle button
     * */
    public Button(PApplet p,int w,int h, int x, int y, String l,int tx, int ty, int fs){
    	this.parent = p;
    	this.width = w;
    	this.height = h;
    	this.x = x;
    	this.y = y;   
    	this.label = l;
    	this.type = 0;
    	this.textX = tx;
    	this.textY = ty;
    	this.fontSize = fs;
    	this.hover = 0;
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
    	      if (this.hover==1 || this.toggle==1){
    			  if (this.toggle ==1){
    				     parent.fill(this.clickedColour.getRGB());   
    	   			     parent.stroke(this.clickedBorderColour.getRGB());
    			  }else{
    				  parent.fill(this.toggleColour.getRGB());   
        			  parent.stroke(toggleBorderColour.getRGB());
    			  }    			 
    		 }else{
    			  parent.fill(this.background.getRGB()); 
    			  parent.stroke(borderColour.getRGB());
    		  }    		
    		 parent.strokeWeight(1.5f);
    		 parent.rect(this.x,this.y,this.width,this.height,6);
    		 PFont font = parent.createFont("Sans-Serif",this.fontSize);     	
    	   	 parent.textFont(font);  
    	   	 parent.textAlign(parent.CENTER);
    	   	 parent.fill(this.textColour.getRGB());
    	   	 parent.text(this.label,this.textX,this.textY);  
    	}
    	//TODO: circle radio button (if needed)
    }
    /**Checks if a mouse clicked within the area of the button 
     * */
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
      	  this.hover = 0;
      	  this.clicked = false;          
    	}    	  
    }
    /**Checks if a mouse has entered the area of the button (mouse over)
     * */
    public void hover(){
    	if (this.type==0){
    		if (parent.mouseX >= this.x && parent.mouseX <= (this.x+this.width) && parent.mouseY >= this.y && parent.mouseY <= 
    				(this.y+this.height)){
    		  this.hover = 1;
    			
    		}else{
    			this.hover = 0;
    		}
    	}    	
    	
    }
    /**Re-sets button hover and clicked colours to the defaults
     * */
    public void resetColours(){
    	 this.toggleColour = this.getColours.LightSlate3;
    	 this.toggleBorderColour = this.getColours.LightSlate2;    	  
    	 this.borderColour =  getColours.LightSlate2;
    	 this.clickedColour = toggleColour;
    	 this.clickedBorderColour = toggleBorderColour;
    }
    
}
