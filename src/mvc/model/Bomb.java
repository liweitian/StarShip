package mvc.model;


import java.awt.*;
import java.util.ArrayList;
import mvc.controller.Game;

public class Bomb extends Sprite{



    private final double MAX = 100.0;
    private final int FIRE_POWER = 10;
    private int size;
    public Bomb(Falcon fal){

        super();
        setTeam(Team.FRIEND);
        //defined the points on a cartesean grid
        ArrayList<Point> pntCs = new ArrayList<Point>();

        pntCs.add(new Point(0,2)); //top point

        pntCs.add(new Point(1,0));
        pntCs.add(new Point(0,-2));
        pntCs.add(new Point(-1,0));

        assignPolarPoints(pntCs);
        setSize(1);
        setRadius(20);
        setFadeValue(100);
        setColor(Color.LIGHT_GRAY);
        //everything is relative to the falcon ship that fired the bullet

        setDeltaX(( -fal.getDeltaX() -
                Math.cos( Math.toRadians( fal.getOrientation() ))));
        System.out.println(getDeltaX());
        setDeltaY(( -fal.getDeltaY() -
                Math.sin( Math.toRadians( fal.getOrientation() ) )));
        setCenter( fal.getCenter() );

        //set the bullet orientation to the falcon (ship) orientation
        setOrientation(fal.getOrientation());
    }
    public Bomb(Bomb bomb,Asteroid asteroid){

        super();
        setTeam(Team.FRIEND);
        //defined the points on a cartesean grid
        ArrayList<Point> pntCs = new ArrayList<Point>();
        pntCs.add(new Point(0,2)); //top point
        pntCs.add(new Point(1,0));
        pntCs.add(new Point(0,-2));
        pntCs.add(new Point(-1,0));
        assignPolarPoints(pntCs);

        //a bullet expires after 20 frames
        setSize(bomb.getSize()+1);
        setRadius(20/getSize());
        setFadeValue(100);
        setColor(Color.LIGHT_GRAY);
        //everything is relative to the falcon ship that fired the bullet
        setDeltaX(( asteroid.getDeltaX() +
                Math.cos( Math.toRadians( asteroid.getOrientation()*Game.R.nextInt()))));
        setDeltaY(( asteroid.getDeltaY()+
                Math.sin( Math.toRadians( asteroid.getOrientation()*Game.R.nextInt()))));
        setCenter(bomb.getCenter() );
        setOrientation(asteroid.getOrientation());
    }
    @Override
    public void move(){
        super.move();
        if(getFadeValue()>0){
            setFadeValue(getFadeValue()-1);
        }
        setDeltaX((getDeltaX() -
                Math.cos( Math.toRadians(getOrientation() )))*getFadeValue()/MAX);
        setDeltaY((getDeltaY() -
                Math.sin( Math.toRadians(getOrientation() ) ))*getFadeValue()/MAX);
    }
    public void setSize(int tmp){
        size = tmp;
    }
    public int getSize(){
        return size;
    }

    public int getFIRE_POWER() {
        return FIRE_POWER;
    }
}
