import acm.graphics.*;
import acm.program.*;
import acm.util.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;


/** An instance is a Brick */
public class Brick extends GRect {
    
    /** Constructor: Constructs a new brick with the 
      * specified width and height, positioned at the origin. */
    public Brick(double width, double height){
        super(width, height);
    }
    
    /** Constructor: Constructs a new rectangle 
      * with the specified bounds. */
    public Brick(double x, double y, double width, double height){
        super(x, y, width, height);
    }
}
