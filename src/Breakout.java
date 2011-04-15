import acm.graphics.*;
import acm.program.*;
import acm.util.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;

/** An instance is the game breakout. Start it by executing
    Breakout.main(null);
    */
public class Breakout extends GraphicsProgram implements MouseMotionListener, KeyListener {
    /** Width of the game display (all coordinates are in pixels) */
    private static final int WIDTH= 460;
    /** Height of the game display */
    private static final int HEIGHT= 610;
    
    /** Width of the paddle */
    private static final int WIDTH_OF_PADDLE= 58;
    /** Height of the paddle */
    private static final int HEIGHT_OF_PADDLE= 11;
    /**Distance of the (bottom of the) paddle up from the bottom */
    private static final int PADDLE_OFFSET= 30;
    
    /** Horizontal separation between bricks */
    private static final int BRICK_SEP_H= 5;
    /** Vertical separation between bricks */
    private static final int BRICK_SEP_V= 4;
    /** Height of a brick */
    private static final int BRICK_HEIGHT= 8;
    /** Offset of the top brick row from the top */
    private static final int BRICK_Y_BOTTOM_OFFSET= 71;
    
    /** Number of bricks per row */
    private static  int BRICKS_IN_ROW= 10;
    /** Number of rows of bricks, in range 1..10. */
    private static  int BRICK_ROWS= 10;
    /** Width of a brick */
    private static int BRICK_WIDTH= WIDTH / BRICKS_IN_ROW - BRICK_SEP_H;
    
    /** Radius of the ball in pixels */
    private static final int BALL_RADIUS= 10;
    
    /** Number of turns */
    private static final int NTURNS= 3;
    
    /** Marker variable to keep track of brick color */
    private static int color= 0;
    
    /** Paddle*/
    private GRect paddle= new GRect(WIDTH/2 - WIDTH_OF_PADDLE/2, HEIGHT - (PADDLE_OFFSET + HEIGHT_OF_PADDLE), WIDTH_OF_PADDLE, HEIGHT_OF_PADDLE); 
    /** Ball */
    private GOval ball= new GOval(2*BALL_RADIUS, 2*BALL_RADIUS);
    /** Ball 2 */
    private GOval ball2= new GOval(2*BALL_RADIUS, 2*BALL_RADIUS);
    
    /** Random number generator */
    private RandomGenerator rgen= new RandomGenerator();
    
    /** Velocity components of the balls */
    private double vx, vy, vx2, vy2;
    
    /** Number of bricks on the board */
    private int numOfBricks;
    
    /** Lives */
    private int lives;
    
    /** If the two ball game mode has been initiated */
    boolean twoballmode= false;
    
    /** Boing sound */
    AudioClip bounceClip= MediaTools.loadAudioClip("bounce.au");
   
    
    /** Run the program as an application. If args contains 2 elements that are positive
        integers, then use the first for the number of bricks per row and the second for
        the number of rows of bricks.
        A hint on how main works. The main program creates an instance of
        the class, giving the constructor the width and height of the graphics
        panel. The system then calls method run() to start the computation.
      */
    public static void main(String[] args) {
        fixBricks(args);
        String[] sizeArgs= {"width=" + WIDTH, "height=" + HEIGHT};
        new Breakout().start(sizeArgs);
    }
    
    
    /** If b is non-null, has exactly two elements, and they are positive
        integers with no blanks surrounding them, then:
        Store the first int in BRICKS_IN_ROW, store the second int in BRICK_ROWS,
        and recompute BRICK_WIDTH using the formula given in its declaration. */
    public static void fixBricks(String[] b) {

        try{
            
            if (b.length == 2){
                int zero= Integer.valueOf(b[0]);
                int one= Integer.valueOf(b[0]);
                
                //Negative
                if ( zero <= 0 || one <= 0)
                    throw new IllegalArgumentException(); 
                
                BRICKS_IN_ROW= Integer.valueOf(b[0]);
                BRICK_ROWS= Integer.valueOf(b[1]);
                BRICK_WIDTH= WIDTH / BRICKS_IN_ROW - BRICK_SEP_H;
            }
        }
        
        //Null or blanks surrounding
        catch(NumberFormatException e){
        }
        
        //Negative
        catch(IllegalArgumentException e){
        }
    }
    
    /** Run the Breakout program. */
    public void run() {
        
        /** Setup phase */
        
        //Adding listeners
        addMouseListeners();
        addKeyListeners(); 
        
        //Lives reset
        lives= 3;
        
        //Opening splash
        if (!twoballmode) openingsplash();
        
        //Initializes the bricks
        numOfBricks= 0;
        setUpBricks(BRICK_ROWS);   
            
        //Initializes the paddle
        setUpPaddle();
        
        
        /** Play phase */
        if (!twoballmode){
            play();
        }
        else{
            //Notification that the two ball mode is beginning
            superawesomedisplaymessage();
            play2();
        }
    }
    
    /** Loop for play */
    private void play(){
        
        //Initializes the ball
        setUpBall();
        
        int k= 0;
        int hits= 0;
        double x= ball.getX();
        double y= ball.getY(); 
        vy= 3;
        vx= rgen.nextDouble(1.0, 3.0);
        if (!rgen.nextBoolean(0.5)) vx= -vx;
        
        while(k != 1){
            
            //Testing if the two ball mode has been activated
            if (twoballmode){
                removeAll();
                x= 0;
                y= 0;
                run();
            }
            
            x= x + vx;
            y= y + vy;
            
            //Move Ball    
            ball.setBounds(x, y, 2*BALL_RADIUS, 2*BALL_RADIUS);
            
            //Testing for collisions with other objects
            if (getCollidingObject(ball) != null){
                
                double paddlex= paddle.getX();
                double paddley= paddle.getY();
                
                //The object the ball has collided with is the paddle
                if (!(getCollidingObject(ball) instanceof Brick)){
                    hits= hits + 1;
                
                    //Kicker: Every 7th paddle hit the speed increases by a magnitude of 1.5
                    if (hits%7 == 0){
                        vy= 1.5*vy;
                        vx= 1.5*vx;
                    }
                    
                    //Ball is going down and hits (prevents ball from getting stuck inside the paddle
                    if (vy > 0){
                        vy= -vy;
                    }
                    
                    //Improved user control over bounces with paddle
                if (x + 2*BALL_RADIUS <= paddlex + WIDTH_OF_PADDLE/4 && x + 2*BALL_RADIUS >= paddlex){
                    vx= -vx;
                }
                if (x >= paddlex + (WIDTH_OF_PADDLE - WIDTH_OF_PADDLE/4) && x <= paddlex + WIDTH_OF_PADDLE){
                    vx= -vx;
                }
                }

                //Ball hits a brick
                if (getCollidingObject(ball) instanceof Brick){
                    vy= -vy;
                    remove(getCollidingObject(ball));
                    numOfBricks= numOfBricks - 1;
                }
            }
            
            //Hitting the side walls
            if (y <=0) vy= -vy;
            else if (x <= 0 || x + 2*BALL_RADIUS >= WIDTH) vx= -vx;
            
            //Hitting the bottom
            if (y + 2*BALL_RADIUS >= HEIGHT){
                remove(ball);
                lives= lives - 1;

                //Still has lives left
                if (lives > 0){
                    GLabel warning= new GLabel("Another ball is coming in 3 seconds. You have " + lives + ((lives == 1) ? " life left." : " lives left."), 13, 60);
                    warning.setFont(new Font("Arial", Font.BOLD, 16));
                    add(warning);
                    pause(3000);
                    remove(warning);
                    k= k+1; //Exit loop
                }
                
                //Lose the game
                else if (lives <= 0){
                    remove(ball);
                    GLabel lose= new GLabel("YOU JUST LOST THE GAME", 30, 60);
                    lose.setFont(new Font("Arial", Font.BOLD, 30));
                    lose.setColor(Color.red);
                    add(lose);
                    waitForClick();
                    remove(lose);
                    k= k+1; //Exit loop
                }
            }
            
            //Win the game
            if(numOfBricks == 0){
                remove(ball);
                GLabel won= new GLabel("YOU JUST WON THE GAME", 30, 80);
                won.setFont(new Font("Arial", Font.BOLD, 30));
                won.setColor(Color.blue);
                add(won);
                lives= 0;
                waitForClick();
                remove(won);
                k= k+1; //Exit loop
            }
            
            pause(10);
  
        }
        if (lives > 0){
            play();
        }
        
        //Restart
        removeAll();
        run();
    }
    
    /** Loop for two balls */
    private void play2(){
        
        //Initializes the balls
        setUpBall();
        setUpBall2();
        
        //The balls are currently on the playing field
        boolean ball1b= true;
        boolean ball2b= true;
        boolean messageshown= false;
        
        int ballsinplay= 2;
        
        int k=0;
        
        int hits= 0;
        int hits2= 0;
        
        double x1= ball.getX();
        double y1= ball.getY();
        double x2= ball2.getX();
        double y2= ball2.getY();
        
        vy= 1.5;
        vx= rgen.nextDouble(1.0, 1.5);
        if (!rgen.nextBoolean(0.5)) vx= -vx;
        
        vy2= 1.5;
        vx2= rgen.nextDouble(1.0, 1.5);
        if (!rgen.nextBoolean(0.5)) vx2= -vx2;
        
        while(k != 1){
            
            x1= x1 + vx;
            y1= y1 + vy;
            
            x2= x2 + vx2;
            y2= y2 + vy2;
            
            //Move balls
            ball.setBounds(x1, y1, 2*BALL_RADIUS, 2*BALL_RADIUS);
            ball2.setBounds(x2, y2, 2*BALL_RADIUS, 2*BALL_RADIUS);
            
            //Testing for ball1's collisions with other objects 
            if (getCollidingObject(ball) != null && ball1b){
                
                double paddlex= paddle.getX();
                double paddley= paddle.getY();
                
                //The object the ball has collided with is the paddle
                if (!(getCollidingObject(ball) instanceof Brick) && !(getCollidingObject(ball) instanceof GOval)){
                    hits= hits + 1;
                
                    //Kicker: Every 7th paddle hit the speed increases by a magnitude of 1.5
                    if (hits%7 == 0){
                        vy= 1.5*vy;
                        vx= 1.5*vx;
                    }
                    
                    //Ball is going down and hits (prevents ball from getting stuck inside the paddle
                    if (vy > 0){
                        vy= -vy;
                    }
                    
                    //Improved user control over bounces with paddle
                    if (x1 + 2*BALL_RADIUS <= paddlex + WIDTH_OF_PADDLE/4 && x1 + 2*BALL_RADIUS >= paddlex){
                        vx= -vx;
                    }
                    if (x1 >= paddlex + (WIDTH_OF_PADDLE - WIDTH_OF_PADDLE/4) && x1 <= paddlex + WIDTH_OF_PADDLE){
                        vx= -vx;
                    }
                }
                
                //Ball hits a brick
                if (getCollidingObject(ball) instanceof Brick){
                    vy= -vy;
                    remove(getCollidingObject(ball));
                    numOfBricks= numOfBricks - 1;
                }
                
                //Ball hits ball2
                if (getCollidingObject(ball) instanceof GOval){
                    bounceClip.play();
                    vy= -vy;
                }
            }
            
            //Testing for ball2's collisions with other objects 
            if (getCollidingObject(ball2) != null && ball2b){
                      
                double paddlex= paddle.getX();
                double paddley= paddle.getY();
                
                
                //The object the ball has collided with is the paddle
                if (!(getCollidingObject(ball2) instanceof Brick) && !(getCollidingObject(ball2) instanceof GOval)){
                    hits2= hits2 + 1;
                    
                    //Kicker: Every 7th paddle hit the speed increases by a magnitude of 1.5
                    if (hits2%7 == 0){
                        vy2= 1.5*vy2;
                        vx2= 1.5*vx2;
                    }
                    
                    //Ball is going down and hits (prevents ball from getting stuck inside the paddle
                    if (vy2 > 0){
                        vy2= -vy2;
                    }
                    
                    //Improved user control over bounces with paddle
                    if (x2 + 2*BALL_RADIUS <= paddlex + WIDTH_OF_PADDLE/4 && x2 + 2*BALL_RADIUS >= paddlex){
                        vx2= -vx2;
                    }
                    if (x2 >= paddlex + (WIDTH_OF_PADDLE - WIDTH_OF_PADDLE/4) && x2 <= paddlex + WIDTH_OF_PADDLE){
                        vx2= -vx2;
                    }
                }

                
                //Ball2 hits a brick
                if (getCollidingObject(ball2) instanceof Brick){
                    vy2= -vy2;
                    remove(getCollidingObject(ball2));
                    numOfBricks= numOfBricks - 1;
                }
                
                //Ball2 hits ball
                if (getCollidingObject(ball2) instanceof GOval){
                    bounceClip.play();
                    vy2= -vy2;
                }
            }
            
            //Hitting the side walls
            if (y1 <=0) vy= -vy;
            else if (x1 <= 0 || x1 + 2*BALL_RADIUS >= WIDTH) vx= -vx;
            
            if (y2 <=0) vy2= -vy2;
            else if (x2 <= 0 || x2 + 2*BALL_RADIUS >= WIDTH) vx2= -vx2;
            
            
            //Hitting the bottom
            if (y1 + 2*BALL_RADIUS >= HEIGHT && ball1b){
                ballsinplay= ballsinplay-1;
                remove(ball);
                ball1b= false;
            }
            if (y2 + 2*BALL_RADIUS >= HEIGHT && ball2b){
                ballsinplay= ballsinplay-1;
                remove(ball2);
                ball2b= false;
            }
            
            //One ball in play warning message
            if (ballsinplay == 1 && !messageshown){
                GLabel warning= new GLabel("You have one ball remaining.", 120, 60);
                warning.setFont(new Font("Arial", Font.BOLD, 16));
                add(warning);
                pause(2000);
                remove(warning);
                messageshown= true;      
            }
            
            //Both balls are gone
            if (ballsinplay == 0){
                lives= lives - 1;
                
                //Still has lives left
                if (lives > 0){
                    GLabel warning= new GLabel("Another two balls are coming in 3 seconds. You have " + lives + ((lives == 1) ? " life left." : " lives left."), 13, 60);
                    warning.setFont(new Font("Arial", Font.BOLD, 13));
                    add(warning);
                    pause(3000);
                    remove(warning);
                    k= k+1; //Exit loop
                }
            
                //Lose the game
                else if (lives <= 0){
                    remove(ball);
                    remove(ball2);
                    GLabel lose= new GLabel("YOU JUST LOST THE GAME", 30, 60);
                    lose.setFont(new Font("Arial", Font.BOLD, 30));
                    lose.setColor(Color.red);
                    add(lose);
                    waitForClick();
                    remove(lose);
                    k= k+1; //Exit loop
                }
            }
            
            //Win the game
            if(numOfBricks == 0){
                remove(ball);
                remove(ball2);
                GLabel won= new GLabel("YOU JUST WON THE GAME", 30, 80);
                won.setFont(new Font("Arial", Font.BOLD, 30));
                won.setColor(Color.blue);
                add(won);
                lives= 0;
                waitForClick();
                remove(won);
                k= k+1; //Exit loop
            }
            
            pause(10);
        }
        if (lives > 0){
            play2();
        }
        
        //Restart
        twoballmode= false;
        removeAll();
        run();
    }
    
    /** Returns the GObject that object has collided with */
    private GObject getCollidingObject(GOval object){
        double x= object.getX();
        double y= object.getY();
        
        GObject ul= getElementAt(x, y);
        GObject ur= getElementAt(x +  2*BALL_RADIUS, y);
        GObject ll= getElementAt(x, y +  2*BALL_RADIUS);
        GObject lr= getElementAt(x +  2*BALL_RADIUS, y +  2*BALL_RADIUS);
        
        if (ul != null) return ul;
        else if (ur != null) return ur;
        else if (ll != null) return ll;
        else if (lr != null)return lr;
        else return null; 
    }
        
    /** Move the horizontal middle of the paddle to the x-coordinate of the mouse position -- but
      * keep the paddle completley on the board.*/
    public void mouseMoved(MouseEvent e){
        GPoint p= new GPoint(e.getPoint());
        
        double x= p.getX();
        if(x <= WIDTH/2) paddle.setBounds(Math.max(x - WIDTH_OF_PADDLE/2, 0), paddle.getY(), WIDTH_OF_PADDLE, HEIGHT_OF_PADDLE);
        else paddle.setBounds(Math.min(x - WIDTH_OF_PADDLE/2, WIDTH - WIDTH_OF_PADDLE), paddle.getY(), WIDTH_OF_PADDLE, HEIGHT_OF_PADDLE);
    }
  
    /** Key listener: when the user types 'b', the game changes into the two ball mode */
    public void keyTyped (KeyEvent e){
        if (e.getKeyChar() == 'b'){
            twoballmode= true;
        }
    }
    
    
    /** Opening splash screen that shows the title of the game and instructions */
    private void openingsplash(){
        GRect bg= new GRect(WIDTH, HEIGHT);
        
        GLabel breakout= new GLabel("BREAKOUT", 15, 70);
        breakout.setFont(new Font("Arial", Font.BOLD, 70));
        breakout.setColor(Color.red);
        
        GLabel author= new GLabel("By Anthony Chen", 18, 95);
        author.setFont(new Font("Arial", Font.BOLD, 20));
        author.setColor(Color.black);
        
        GLabel insttitle= new GLabel("Instructions:", 18, 180);
        insttitle.setFont(new Font("Arial", Font.BOLD, 17));
        insttitle.setColor(Color.black);
        
        GLabel inst= new GLabel("Move the mouse horizontally to move the paddle.", 18, 200);
        inst.setFont(new Font("Arial", Font.PLAIN, 15));
        inst.setColor(Color.black);
        
        GLabel inst2= new GLabel("Bonus fun mode! Press b for two ball mode!", 18, 220);
        inst2.setFont(new Font("Arial", Font.PLAIN, 15));
        inst2.setColor(Color.black);
        
        GLabel inst3= new GLabel("Click to continue!", 18, 240);
        inst3.setFont(new Font("Arial", Font.PLAIN, 15));
        inst3.setColor(Color.black);
        
        add(breakout);
        add(author);
        add(insttitle);
        add(inst);
        add(inst2);
        add(inst3);
        
        waitForClick();
        
        remove(breakout);
        remove(author);
        remove(insttitle);
        remove(inst);
        remove(inst2);
        remove(inst3);
    }
   
    
    /** Makes a flashing message appear on the board. The message says that that the two ball mode was activated */
    private void superawesomedisplaymessage(){
        GLabel message= new GLabel("TWO BALL MODE ACTIVATED", 15, 60);
        message.setFont(new Font("Arial", Font.BOLD, 30));
        message.setColor(Color.red);
        add(message);
        for (int k= 1; k < 10; k= k + 1){
            message.setColor(Color.blue);
            pause(100);
            message.setColor(Color.red);
            pause(100);     
        }
        remove(message);
    }
    
    /** Initializes the ball */
    private void setUpBall(){
        ball.setBounds(WIDTH/2 - BALL_RADIUS, WIDTH/2 - BALL_RADIUS, 2*BALL_RADIUS, 2*BALL_RADIUS);
        ball.setFilled(true);
        ball.setColor(Color.black);
        add(ball);
    }
    
    /** Initializes the second ball */
    private void setUpBall2(){
        ball2.setBounds(WIDTH/2 - BALL_RADIUS + 50, WIDTH/2 - BALL_RADIUS + 50, 2*BALL_RADIUS, 2*BALL_RADIUS);
        ball2.setFilled(true);
        ball2.setColor(Color.black);
        add(ball2);
    }
    
    /** Initializes the paddle */
    private void setUpPaddle(){
        paddle.setFilled(true);
        paddle.setColor(Color.black);
        add(paddle);
    }
    
    /** Initializes the brick setup based on r, the number of rows that should be created */
    private void setUpBricks(int r){
        int c= 1;
        int k= BRICK_Y_BOTTOM_OFFSET;
        
        //Invariant: Rows 1..c have been set up
        while (c <= r){
            setUpRow(k);
            k= k + (BRICK_HEIGHT + BRICK_SEP_V);
            c= c+1;
        }
    }
    
    /** Sets up a row of BRICKS_IN_ROW bricks with color RED, ORANGE, YELLOW, GREEN, or CYAN. The color is based off a pattern. */
    private void setUpRow(int k){
        
        if (color == 0 || color == 1){
            for(int i= BRICK_SEP_H/2; i < BRICK_SEP_H + (BRICKS_IN_ROW - 1)*(BRICK_WIDTH + BRICK_SEP_H); i= i + (BRICK_WIDTH + BRICK_SEP_H)){                          
                Brick x= new Brick(i, k, BRICK_WIDTH, BRICK_HEIGHT);
                x.setFilled(true);
                x.setColor(Color.red);
                add(x);
                numOfBricks= numOfBricks + 1;
            }
            color= color + 1;
        }
        
        else if (color == 2 || color == 3){
            for(int i= BRICK_SEP_H/2; i < BRICK_SEP_H + (BRICKS_IN_ROW - 1)*(BRICK_WIDTH + BRICK_SEP_H); i= i + (BRICK_WIDTH + BRICK_SEP_H)){                          
                Brick x= new Brick(i, k, BRICK_WIDTH, BRICK_HEIGHT);
                x.setFilled(true);
                x.setColor(Color.orange);
                add(x);
                numOfBricks= numOfBricks + 1;
            }
            color= color + 1;
        }
        
        else if (color == 4 || color == 5){
            for(int i= BRICK_SEP_H/2; i < BRICK_SEP_H + (BRICKS_IN_ROW - 1)*(BRICK_WIDTH + BRICK_SEP_H); i= i + (BRICK_WIDTH + BRICK_SEP_H)){                          
                Brick x= new Brick(i, k, BRICK_WIDTH, BRICK_HEIGHT);
                x.setFilled(true);
                x.setColor(Color.yellow);
                add(x);
                numOfBricks= numOfBricks + 1;
            }
            color= color + 1;
        }
        
        else if (color == 6 || color == 7){
            for(int i= BRICK_SEP_H/2; i < BRICK_SEP_H + (BRICKS_IN_ROW - 1)*(BRICK_WIDTH + BRICK_SEP_H); i= i + (BRICK_WIDTH + BRICK_SEP_H)){                          
                Brick x= new Brick(i, k, BRICK_WIDTH, BRICK_HEIGHT);
                x.setFilled(true);
                x.setColor(Color.green);
                add(x);
                numOfBricks= numOfBricks + 1;
            }
            color= color + 1;
        }
        
        else if (color == 8){
            for(int i= BRICK_SEP_H/2; i < BRICK_SEP_H + (BRICKS_IN_ROW - 1)*(BRICK_WIDTH + BRICK_SEP_H); i= i + (BRICK_WIDTH + BRICK_SEP_H)){                          
                Brick x= new Brick(i, k, BRICK_WIDTH, BRICK_HEIGHT);
                x.setFilled(true);
                x.setColor(Color.cyan);
                add(x);
                numOfBricks= numOfBricks + 1;
            }
            color= color + 1;
        }
        
        else if (color == 9){
            for(int i= BRICK_SEP_H/2; i < BRICK_SEP_H + (BRICKS_IN_ROW - 1)*(BRICK_WIDTH + BRICK_SEP_H); i= i + (BRICK_WIDTH + BRICK_SEP_H)){                          
                Brick x= new Brick(i, k, BRICK_WIDTH, BRICK_HEIGHT);
                x.setFilled(true);
                x.setColor(Color.cyan);
                add(x);
                numOfBricks= numOfBricks + 1;
            }
            color= 0;
        }
    }    
}