package mvc.controller;

import mvc.model.*;
import mvc.view.GamePanel;
import sounds.Sound;

import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

// ===============================================
// == This Game class is the CONTROLLER
// ===============================================

public class Game implements Runnable, KeyListener {

    // ===============================================
    // FIELDS
    // ===============================================

    public static final Dimension DIM = new Dimension(1100, 700); //the dimension of the game.
    private GamePanel gmpPanel;
    public static Random R = new Random();
    public final static int ANI_DELAY = 45; // milliseconds between screen
    // updates (animation)
    private Thread thrAnim;
    private int nLevel = 1;
    private int nTick = 0;

    private boolean bMuted = true;


    private final int PAUSE = 80, // p key
            QUIT = 81, // q key
            LEFT = 37, // rotate left; left arrow
            RIGHT = 39, // rotate right; right arrow
            UP = 38, // thrust; up arrow
            START = 83, // s key
            FIRE = 32, // space key
            MUTE = 77, // m-key mute

    // for possible future use
    GUIDEDMISSILE=71, 				//g key
            HYPER = 68, 					// d key
            SHIELD = 65, 				// a key
            BOMB = 66,					//b key
    // NUM_ENTER = 10, 				// hyp
    SPECIAL = 70; 					// fire special weapon;  F key

    private Clip clpThrust;
    private Clip clpMusicBackground;

    private static final int SPAWN_NEW_SHIP_FLOATER = 1200;
    private static final int UFOFLOATER = 800;


    // ===============================================
    // ==CONSTRUCTOR
    // ===============================================

    public Game() {

        gmpPanel = new GamePanel(DIM);
        gmpPanel.addKeyListener(this);
        clpThrust = Sound.clipForLoopFactory("whitenoise.wav");
        clpMusicBackground = Sound.clipForLoopFactory("music-background.wav");


    }

    // ===============================================
    // ==METHODS
    // ===============================================

    public static void main(String args[]) {
        EventQueue.invokeLater(new Runnable() { // uses the Event dispatch thread from Java 5 (refactored)
            public void run() {
                try {
                    Game game = new Game(); // construct itself
                    game.fireUpAnimThread();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
    }

    private void fireUpAnimThread() { // called initially
        if (thrAnim == null) {
            thrAnim = new Thread(this); // pass the thread a runnable object (this)
            thrAnim.start();//"this" refer to a Game object, the start() will call the run() in Game
        }
    }

    // implements runnable - must have run method
    public void run() {

        // lower this thread's priority; let the "main" aka 'Event Dispatch'
        // thread do what it needs to do first
        thrAnim.setPriority(Thread.MIN_PRIORITY);

        // and get the current time
        long lStartTime = System.currentTimeMillis();

        // this thread animates the scene
        while (Thread.currentThread() == thrAnim) {

            tick();
            spawnNewShipFloater();
            UFOAppears();
            UFOFire();
            gmpPanel.update(gmpPanel.getGraphics()); // update takes the graphics context we must
            // surround the sleep() in a try/catch block
            // this simply controls delay time between
            // the frames of the animation

            //this might be a good place to check for collisions
            checkCollisions();
            //this might be a god place to check if the level is clear (no more foes)
            //if the level is clear then spawn some big asteroids -- the number of asteroids
            //should increase with the level.
            checkNewLevel();

            try {
                // The total amount of time is guaranteed to be at least ANI_DELAY long.  If processing (update)
                // between frames takes longer than ANI_DELAY, then the difference between lStartTime -
                // System.currentTimeMillis() will be negative, then zero will be the sleep time
                lStartTime += ANI_DELAY;
                Thread.sleep(Math.max(0,
                        lStartTime - System.currentTimeMillis()));
            } catch (InterruptedException e) {
                // just skip this frame -- no big deal
                continue;
            }
        } // end while
    } // end run

    private void checkCollisions() {



        Point pntFriendCenter, pntFoeCenter;
        int nFriendRadiux, nFoeRadiux;

        for (Movable movFriend : CommandCenter.getInstance().getMovFriends()) {
            for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {

                pntFriendCenter = movFriend.getCenter();
                pntFoeCenter = movFoe.getCenter();
                nFriendRadiux = movFriend.getRadius();
                nFoeRadiux = movFoe.getRadius();

                //detect collision
                if (pntFriendCenter.distance(pntFoeCenter) < (nFriendRadiux + nFoeRadiux)) {

                    //falcon
                    if(movFoe instanceof Asteroid) {
                        if ((movFriend instanceof Falcon)) {
                            if (!CommandCenter.getInstance().getFalcon().getProtected() && !CommandCenter.getInstance().getFalcon().getShield()) {
                                CommandCenter.getInstance().getOpsList().enqueue(movFriend, CollisionOp.Operation.REMOVE);
                                CommandCenter.getInstance().spawnFalcon(false);
                            }
                        }
                        //not the falcon
                        else if ((movFriend instanceof Bullet)) {
                            CommandCenter.getInstance().getOpsList().enqueue(movFriend, CollisionOp.Operation.REMOVE);
                        } else if (movFriend instanceof Bomb) {
                            CommandCenter.getInstance().getOpsList().enqueue(movFriend, CollisionOp.Operation.REMOVE);
                            clusterBomb((Bomb) movFriend, (Asteroid) movFoe);

                        } else {
                            CommandCenter.getInstance().getOpsList().enqueue(movFriend, CollisionOp.Operation.REMOVE);
                        }//end else
                        //kill the foe and if asteroid, then spawn new asteroids
                        killFoe(movFoe);
                        Sound.playSound("kapow.wav");
                    }
                    if(movFoe instanceof UFO){
                        if ((movFriend instanceof Falcon)) {
                            if (!CommandCenter.getInstance().getFalcon().getProtected() && !CommandCenter.getInstance().getFalcon().getShield()) {
                                CommandCenter.getInstance().getOpsList().enqueue(movFriend, CollisionOp.Operation.REMOVE);
                                CommandCenter.getInstance().spawnFalcon(false);
                            }
                        }
                        //not the falcon
                        else {
                            CommandCenter.getInstance().getOpsList().enqueue(movFriend, CollisionOp.Operation.REMOVE);
                        }//end else

                        killUFO((UFO)movFoe,movFriend);
                    }
                }//end if
            }//end inner for
        }//end outer for


        //check for collisions between falcon and floaters
        if (CommandCenter.getInstance().getFalcon() != null){
            Point pntFalCenter = CommandCenter.getInstance().getFalcon().getCenter();
            int nFalRadiux = CommandCenter.getInstance().getFalcon().getRadius();
            Point pntFloaterCenter;
            int nFloaterRadiux;

            for (Movable movFloater : CommandCenter.getInstance().getMovFloaters()) {
                pntFloaterCenter = movFloater.getCenter();
                nFloaterRadiux = movFloater.getRadius();

                //detect collision
                if (pntFalCenter.distance(pntFloaterCenter) < (nFalRadiux + nFloaterRadiux)) {
                    CommandCenter.getInstance().setNumFalcons(CommandCenter.getInstance().getNumFalcons()+1);
                    CommandCenter.getInstance().getOpsList().enqueue(movFloater, CollisionOp.Operation.REMOVE);
                    Sound.playSound("pacman_eatghost.wav");

                }//end if
            }//end inner for
        }//end if not null

        //check for collisions between falcon and debris

        if (CommandCenter.getInstance().getFalcon() != null){
            Point pntFalCenter = CommandCenter.getInstance().getFalcon().getCenter();
            int nFalRadiux = CommandCenter.getInstance().getFalcon().getRadius();
            Point pntFloaterCenter;
            int nFloaterRadiux;

            for (Movable movDebris : CommandCenter.getInstance().getMovDebris()) {
                pntFloaterCenter = movDebris.getCenter();
                nFloaterRadiux = movDebris.getRadius();

                //detect collision
                if (pntFalCenter.distance(pntFloaterCenter) < (nFalRadiux + nFloaterRadiux)) {
                    CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore()+20);
                    CommandCenter.getInstance().getOpsList().enqueue(movDebris, CollisionOp.Operation.REMOVE);
                    Sound.playSound("pacman_eatghost.wav");
                }//end if
            }//end inner for
        }//end if not null


        //we are dequeuing the opsList and performing operations in serial to avoid mutating the movable arraylists while iterating them above
        while(!CommandCenter.getInstance().getOpsList().isEmpty()){
            CollisionOp cop =  CommandCenter.getInstance().getOpsList().dequeue();
            Movable mov = cop.getMovable();
            CollisionOp.Operation operation = cop.getOperation();

            switch (mov.getTeam()){
                case FOE:
                    if (operation == CollisionOp.Operation.ADD){
                        CommandCenter.getInstance().getMovFoes().add(mov);
                    } else {
                        CommandCenter.getInstance().getMovFoes().remove(mov);
                    }

                    break;
                case FRIEND:
                    if (operation == CollisionOp.Operation.ADD){
                        CommandCenter.getInstance().getMovFriends().add(mov);
                    } else {
                        CommandCenter.getInstance().getMovFriends().remove(mov);
                    }
                    break;

                case FLOATER:
                    if (operation == CollisionOp.Operation.ADD){
                        CommandCenter.getInstance().getMovFloaters().add(mov);
                    } else {
                        CommandCenter.getInstance().getMovFloaters().remove(mov);
                    }
                    break;

                case DEBRIS:
                    if (operation == CollisionOp.Operation.ADD){
                        CommandCenter.getInstance().getMovDebris().add(mov);
                    } else {
                        CommandCenter.getInstance().getMovDebris().remove(mov);
                    }
                    break;


            }

        }
        //a request to the JVM is made every frame to garbage collect, however, the JVM will choose when and how to do this
        System.gc();

    }//end meth
    private void killUFO(UFO movFoe,Movable movFriend){
        if(movFriend instanceof Bomb){
            if(movFoe.getLife()-((Bomb) movFriend).getFIRE_POWER()<0){
                CommandCenter.getInstance().getOpsList().enqueue(movFoe, CollisionOp.Operation.REMOVE);
                CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore()+200);
            }
            else{
                movFoe.setLife(movFoe.getLife()-((Bomb) movFriend).getFIRE_POWER());
            }
        }
        else if(movFriend instanceof Cruise){
            if(movFoe.getLife()-((Cruise) movFriend).getFIRE_POWER()<0){
                CommandCenter.getInstance().getOpsList().enqueue(movFoe, CollisionOp.Operation.REMOVE);
                CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore()+200);
            }
            else{
                movFoe.setLife(movFoe.getLife()-((Cruise)movFriend).getFIRE_POWER());
            }
        }
        else if(movFriend instanceof Bullet){
            if(movFoe.getLife()-((Bullet) movFriend).getFIRE_POWER()<0){
                CommandCenter.getInstance().getOpsList().enqueue(movFoe, CollisionOp.Operation.REMOVE);
                CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore()+200);
            }
            else{
                movFoe.setLife(movFoe.getLife()-((Bullet)movFriend).getFIRE_POWER());
            }
        }
        else if(movFriend instanceof GuidedMissile){
            if(movFoe.getLife()-((GuidedMissile) movFriend).getFIRE_POWER()<0){
                CommandCenter.getInstance().getOpsList().enqueue(movFoe, CollisionOp.Operation.REMOVE);
                CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore()+200);
            }
            else{
                movFoe.setLife(movFoe.getLife()-((GuidedMissile)movFriend).getFIRE_POWER());
            }
        }
        //System.out.println(movFoe.getLife()+"life");
    }
    private void killFoe(Movable movFoe) {

        if (movFoe instanceof Asteroid){

            //we know this is an Asteroid, so we can cast without threat of ClassCastException
            Asteroid astExploded = (Asteroid)movFoe;
            //big asteroid
            if(astExploded.getSize() == 0){
                //spawn two medium Asteroids
                CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore()+astExploded.getScore());
                CommandCenter.getInstance().getOpsList().enqueue(new Asteroid(astExploded), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Asteroid(astExploded), CollisionOp.Operation.ADD);

            }
            //medium size aseroid exploded
            else if(astExploded.getSize() == 1){
                //spawn three small Asteroids
                CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore()+astExploded.getScore()/(astExploded.getSize()*2));
                CommandCenter.getInstance().getOpsList().enqueue(new Asteroid(astExploded), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Asteroid(astExploded), CollisionOp.Operation.ADD);
                CommandCenter.getInstance().getOpsList().enqueue(new Asteroid(astExploded), CollisionOp.Operation.ADD);
            }
            //small size aseroid exploded
            else{
                CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore()+astExploded.getScore()/(astExploded.getSize()*2));
                CommandCenter.getInstance().getOpsList().enqueue(new Debris(astExploded), CollisionOp.Operation.ADD);
            }
        }

        //remove the original Foe
        CommandCenter.getInstance().getOpsList().enqueue(movFoe, CollisionOp.Operation.REMOVE);

    }
    public void clusterBomb(Bomb bomb,Asteroid asteroid){
        if(bomb.getSize()==1){
            CommandCenter.getInstance().getOpsList().enqueue(new Bomb(bomb,asteroid), CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(new Bomb(bomb,asteroid), CollisionOp.Operation.ADD);
            CommandCenter.getInstance().getOpsList().enqueue(new Bomb(bomb,asteroid), CollisionOp.Operation.ADD);
        }
    }
    //some methods for timing events in the game,
    //such as the appearance of UFOs, floaters (power-ups), etc.
    public void tick() {
        if (nTick == Integer.MAX_VALUE)
            nTick = 0;
        else
            nTick++;
    }

    public int getTick() {
        return nTick;
    }

    private void spawnNewShipFloater() {
        //make the appearance of power-up dependent upon ticks and levels
        //the higher the level the more frequent the appearance
        if (nTick % (SPAWN_NEW_SHIP_FLOATER - nLevel * 7) == 0) {
            //CommandCenter.getInstance().getMovFloaters().enqueue(new NewShipFloater());
            CommandCenter.getInstance().getOpsList().enqueue(new NewShipFloater(), CollisionOp.Operation.ADD);
        }
    }
    private void UFOAppears(){
        //if (nTick % (UFOFLOATER - nLevel * 7) == 0) {
        if(nTick%200==0){
            CommandCenter.getInstance().getOpsList().enqueue(new UFO(), CollisionOp.Operation.ADD);
        }
    }
    private void UFOFire(){
        if(nTick%50==0) {
            for (Movable movable : CommandCenter.getInstance().getMovFoes()) {
                if (movable instanceof UFO) {
                    CommandCenter.getInstance().getOpsList().enqueue(new Asteroid((UFO)movable), CollisionOp.Operation.ADD);
                }
            }
        }
    }
    // Called when user presses 's'
    private void startGame() {
        CommandCenter.getInstance().clearAll();
        CommandCenter.getInstance().initGame();
        CommandCenter.getInstance().setLevel(0);
        CommandCenter.getInstance().setPlaying(true);
        CommandCenter.getInstance().setPaused(false);
        //if (!bMuted)
        // clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);
    }

    //this method spawns new asteroids
    private void spawnAsteroids(int nNum) {
        for (int nC = 0; nC < nNum; nC++) {
            //Asteroids with size of zero are big
            CommandCenter.getInstance().getOpsList().enqueue(new Asteroid(0), CollisionOp.Operation.ADD);

        }
    }


    private boolean isLevelClear(){
        //if there are no more Asteroids on the screen
        boolean bFoeFree = true;
        for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
            if ((movFoe instanceof Asteroid)||(movFoe instanceof UFO)){
                bFoeFree = false;
                break;
            }
        }

        return bFoeFree;


    }

    private void checkNewLevel(){

        if (isLevelClear() ){
            if (CommandCenter.getInstance().getFalcon() !=null)
                CommandCenter.getInstance().getFalcon().setProtected(true);

            spawnAsteroids(CommandCenter.getInstance().getLevel() + 2);
            CommandCenter.getInstance().setLevel(CommandCenter.getInstance().getLevel() + 1);

        }
    }




    // Varargs for stopping looping-music-clips
    private static void stopLoopingSounds(Clip... clpClips) {
        for (Clip clp : clpClips) {
            clp.stop();
        }
    }

    // ===============================================
    // KEYLISTENER METHODS
    // ===============================================

    @Override
    public void keyPressed(KeyEvent e) {
        Falcon fal = CommandCenter.getInstance().getFalcon();
        int nKey = e.getKeyCode();
        // System.out.println(nKey);

        if (nKey == START && !CommandCenter.getInstance().isPlaying())
            startGame();

        if (fal != null) {

            switch (nKey) {
                case PAUSE:
                    CommandCenter.getInstance().setPaused(!CommandCenter.getInstance().isPaused());
                    if (CommandCenter.getInstance().isPaused())
                        stopLoopingSounds(clpMusicBackground, clpThrust);
                    else
                        clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);
                    break;
                case QUIT:
                    System.exit(0);
                    break;
                case UP:
                    fal.thrustOn();
                    if (!CommandCenter.getInstance().isPaused())
                        clpThrust.loop(Clip.LOOP_CONTINUOUSLY);
                    break;
                case LEFT:
                    fal.rotateLeft();
                    break;
                case RIGHT:
                    fal.rotateRight();
                    break;
                case SHIELD:
                    fal.setShield(!fal.getShield());
                    break;
                case HYPER:
                    fal.setCenter(new Point((int)(Game.DIM.width*Game.R.nextDouble()), (int)(Game.DIM.width*Game.R.nextDouble())));
                    break;

                // possible future use
                // case KILL:
                // case SHIELD:
                // case NUM_ENTER:

                default:
                    break;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        Falcon fal = CommandCenter.getInstance().getFalcon();
        int nKey = e.getKeyCode();
        System.out.println(nKey);

        if (fal != null) {
            switch (nKey) {
                case FIRE:
                    CommandCenter.getInstance().getOpsList().enqueue(new Bullet(fal), CollisionOp.Operation.ADD);
                    Sound.playSound("laser.wav");
                    break;

                //special is a special weapon, current it just fires the cruise missile.
                case SPECIAL:
                    CommandCenter.getInstance().getOpsList().enqueue(new Cruise(fal), CollisionOp.Operation.ADD);
                    break;
                case BOMB:
                    CommandCenter.getInstance().getOpsList().enqueue(new Bomb(fal), CollisionOp.Operation.ADD);
                    break;
                case GUIDEDMISSILE:
                    CommandCenter.getInstance().getOpsList().enqueue(new GuidedMissile(fal), CollisionOp.Operation.ADD);
                    break;
                case LEFT:
                    fal.stopRotating();
                    break;
                case RIGHT:
                    fal.stopRotating();
                    break;
                case UP:
                    fal.thrustOff();
                    clpThrust.stop();
                    break;
                case MUTE:
                    if (!bMuted){
                        stopLoopingSounds(clpMusicBackground);
                        bMuted = !bMuted;
                    }
                    else {
                        clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);
                        bMuted = !bMuted;
                    }
                    break;


                default:
                    break;
            }
        }
    }

    @Override
    // Just need it b/c of KeyListener implementation
    public void keyTyped(KeyEvent e) {
    }

}


