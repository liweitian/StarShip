package mvc.model;

import mvc.controller.Game;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by lwt on 16/11/27.
 */
public class UFO extends Sprite {
    private int life;
    public UFO(){
        super();
        setLife(100);
        setTeam(Team.FOE);
        ArrayList<Point> pntCs = new ArrayList<Point>();
        pntCs.add(new Point(0,9));
        pntCs.add(new Point(-1, 6));
        pntCs.add(new Point(-1,3));
        pntCs.add(new Point(-4, 1));
        pntCs.add(new Point(4,1));
        pntCs.add(new Point(-4,1));
        pntCs.add(new Point(-4, -2));
        pntCs.add(new Point(-1, -2));
        pntCs.add(new Point(-1, -9));
        pntCs.add(new Point(-1, -2));
        pntCs.add(new Point(-4, -2));
        pntCs.add(new Point(-10, -8));
        pntCs.add(new Point(-5, -9));
        pntCs.add(new Point(-7, -11));
        pntCs.add(new Point(-4, -11));
        pntCs.add(new Point(-2, -9));
        pntCs.add(new Point(-2, -10));
        pntCs.add(new Point(-1, -10));
        pntCs.add(new Point(-1, -9));
        pntCs.add(new Point(1, -9));
        pntCs.add(new Point(1, -10));
        pntCs.add(new Point(2, -10));
        pntCs.add(new Point(2, -9));
        pntCs.add(new Point(4, -11));
        pntCs.add(new Point(7, -11));
        pntCs.add(new Point(5, -9));
        pntCs.add(new Point(10, -8));
        pntCs.add(new Point(4, -2));
        pntCs.add(new Point(1, -2));
        pntCs.add(new Point(1, -9));
        pntCs.add(new Point(1, -2));
        pntCs.add(new Point(4,-2));
        pntCs.add(new Point(4, 1));
        pntCs.add(new Point(1, 3));
        pntCs.add(new Point(1,6));
        pntCs.add(new Point(0,9));
        assignPolarPoints(pntCs);
        setCenter(new Point(Game.DIM.width / 2, 100));
        setColor(Color.MAGENTA);
        //with random orientation
        setOrientation(90);

        //this is the size of the falcon
        setRadius(50);

    }
    public void setLife(int tmp){
        life=tmp;
    }
    public int getLife(){
        return life;
    }

    private int adjustColor(int nCol, int nAdj) {
        if (nCol - nAdj <= 0) {
            return 0;
        } else {
            return nCol - nAdj;
        }
    }
    public void draw(Graphics g){
        Color colShip;

        if(getLife()>20) {
            colShip = new Color(Color.MAGENTA.getRed() * getLife() / 100, Color.MAGENTA.getGreen() * getLife() / 100, Color.MAGENTA.getBlue() * getLife() / 100);
            setColor(colShip);
        }
        else{
            colShip=getColor();
        }
        double UFOx = getCenter().getX();
        double UFOy = getCenter().getY();
        double falx = CommandCenter.getInstance().getFalcon().getCenter().getX();
        double faly = CommandCenter.getInstance().getFalcon().getCenter().getY();
        double tan = (double) (faly-UFOy)/(falx-UFOx);

        double deltadegree=(int)(Math.atan(tan)*360/2/Math.PI);
        if(falx<UFOx){
            deltadegree+=180;
        }
        double rotateRate = 0.9;
        double degree=getOrientation()*rotateRate+deltadegree*(1-rotateRate);

        setOrientation((int)degree);
        drawShipWithColor(g, colShip);
    }
    public void drawShipWithColor(Graphics g, Color colShip) {
        super.draw(g);
        g.setColor(colShip);
        g.drawPolygon(getXcoords(), getYcoords(), dDegrees.length);
    }


}
