package mvc.model;


import java.awt.*;
import java.util.ArrayList;

public class GuidedMissile extends Sprite {
    private final int FIRE_POWER = 25;
    public GuidedMissile(Falcon fal) {

        super();
        setTeam(Team.FRIEND);
        //defined the points on a cartesean grid
        ArrayList<Point> pntCs = new ArrayList<Point>();


        pntCs.add(new Point(0, 5));
        pntCs.add(new Point(1, 3));
        pntCs.add(new Point(1, 0));
        pntCs.add(new Point(6, 0));
        pntCs.add(new Point(6, -1));
        pntCs.add(new Point(1, -1));
        pntCs.add(new Point(1, -2));

        pntCs.add(new Point(-1, -2));
        pntCs.add(new Point(-1, -1));
        pntCs.add(new Point(-6, -1));
        pntCs.add(new Point(-6, 0));
        pntCs.add(new Point(-1, 0));
        pntCs.add(new Point(-1, 3));
        assignPolarPoints(pntCs);

        //a cruis missile expires after MAX_EXPIRE frames
        setExpire(30);
        setRadius(20);
        setCenter(fal.getCenter());
        //everything is relative to the falcon ship that fired the bullet
        setOrientation(fal.getOrientation());
        setColor(Color.CYAN);
    }
    public void move() {

        super.move();
        if(CommandCenter.getInstance().getMovFoes().size()>0) {
            double missleX = getCenter().getX();
            double missleY = getCenter().getY();
            double targetX = CommandCenter.getInstance().getMovFoes().get(0).getCenter().getX();
            double targetY = CommandCenter.getInstance().getMovFoes().get(0).getCenter().getY();
            double tan = (double) ((targetY-missleY)/(targetX-missleX));
            double deltadegree=(int)(Math.atan(tan)*360/2/Math.PI);
            if(missleX > targetX){
                deltadegree+=180;
            }
            //System.out.print(deltadegree);

            double rotateRate = 0.8;
            double degree=getOrientation()*rotateRate+deltadegree*(1-rotateRate);
            setOrientation((int)degree);
            setDeltaX((FIRE_POWER*Math.cos(Math.toRadians(getOrientation()))));
            setDeltaY((FIRE_POWER*Math.sin(Math.toRadians(getOrientation()))));

        }
        else{
            CommandCenter.getInstance().getOpsList().enqueue(this, CollisionOp.Operation.REMOVE);
        }
        if (getExpire() == 0)
            CommandCenter.getInstance().getOpsList().enqueue(this, CollisionOp.Operation.REMOVE);
        else
            setExpire(getExpire() - 1);

    }
    public void draw(Graphics g){
        super.draw(g);
    }

    public int getFIRE_POWER(){
        return FIRE_POWER;
    }
}
