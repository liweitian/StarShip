package mvc.model;


import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import mvc.controller.Game;


/**
 * Created by lwt on 16/11/26.
 */

public class Debris extends Sprite{
    private int nSpin;

    //radius of a large asteroid
    private final int RAD = 20;
    public Debris(Asteroid astExploded){

        //call Sprite constructor
        super();
        setTeam(Team.DEBRIS);
        ArrayList<Point> pntCs = new ArrayList<Point>();

        pntCs.add(new Point(1,1)); //top point
        pntCs.add(new Point(1,-1));
        pntCs.add(new Point(-1,-1));
        pntCs.add(new Point(-1,1));
        assignPolarPoints(pntCs);
        //the spin will be
        int nSpin = astExploded.getSpin()/2;
        if(nSpin %2 ==0)
            nSpin = -nSpin;
        setSpin(nSpin);
        //The following code indicate which direction the asteroid will move
        //random delta-x
        int nDX = Game.R.nextInt(10);
        if(nDX %2 ==0)
            nDX = -nDX;
        setDeltaX(nDX);

        //random delta-y
        int nDY = Game.R.nextInt(10);
        if(nDY %2 ==0)
            nDY = -nDY;
        setDeltaY(nDY);
        setRadius(RAD);
        setExpire(20);
        setCenter(astExploded.getCenter());
        setColor(Color.GREEN);

    }
    public int getSpin() {
        return this.nSpin;
    }


    public void setSpin(int nSpin) {
        this.nSpin = nSpin;
    }

    @Override
    public void move()
    {
        super.move();

        if (getExpire() == 0)
            CommandCenter.getInstance().getOpsList().enqueue(this, CollisionOp.Operation.REMOVE);
        else
            setExpire(getExpire() - 1);

    }


}

