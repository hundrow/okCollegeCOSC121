package assignment5;

import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Author: Andrew Johnson
 * THIS CODE IS A HEAVILY MODIFIED VERSION OF THE CODE THAT www.codingame.com/ide/puzzle/fantastic-bits
 * GIVES YOU TO START.
 * I AM THE AUTHOR OF THE MODIFICATIONS BUT I DO NOT CLAIM TO HAVE AUTHORED THE ORIGINAL CODE.
 * 
 * Grab Snaffles and try to throw them through the opponent's goal!
 * Move towards a Snaffle and use your team id to determine where you need to throw it.
 **/
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int myTeamId = in.nextInt(); // if 0 you need to score on the right of the map, if 1 you need to score on the left
        ArrayList<Wizard> wizardList = new ArrayList<Wizard>();
        ArrayList<Enemy> enemyList = new ArrayList<Enemy>();
        ArrayList<Snaffle> snaffleList = new ArrayList<Snaffle>();
        ArrayList<Bludger> bludgerList = new ArrayList<Bludger>();
        int spellCounter = 1;
        boolean wiz0Out = false;
        boolean wiz1Out = false;
        // game loop
        while (true) {
            int myScore = in.nextInt();
            int myMagic = in.nextInt();
            int opponentScore = in.nextInt();
            int opponentMagic = in.nextInt();
            int entities = in.nextInt(); // number of entities still in game
            for (int i = 0; i < entities; i++) {
                int entityId = in.nextInt(); // entity identifier
                String entityType = in.next(); // "WIZARD", "OPPONENT_WIZARD" or "SNAFFLE" (or "BLUDGER" after first league)
                int x = in.nextInt(); // position
                int y = in.nextInt(); // position
                int vx = in.nextInt(); // velocity
                int vy = in.nextInt(); // velocity
                int state = in.nextInt(); // 1 if the wizard is holding a Snaffle, 0 otherwise

                if(entityType.equals("WIZARD")){
                    wizardList.add(new Wizard(entityId, entityType, x, y, vx, vy, state));
                }else if(entityType.equals("SNAFFLE")){
                    snaffleList.add(new Snaffle(entityId, entityType, x, y, vx, vy, state));
                }else if(entityType.equals("BLUDGER")){
                    bludgerList.add(new Bludger(entityId, entityType, x, y, vx, vy, state));
                }else if(entityType.equals("OPPONENT_WIZARD")){
                    enemyList.add(new Enemy(entityId, entityType, x, y, vx, vy, state));
                }
            }
            
//control wizard 0
            if(wizardList.get(0).getState() == 1 && myTeamId == 0){
                System.out.println("THROW 16000 3750 500");
                wiz0Out = true;

            }else if(wizardList.get(0).getState() == 1 && myTeamId == 1){
                System.out.println("THROW 0 3750 500");
                wiz0Out = true;

            }else{
            	
                //where is wizard 0?
                int wizardX = wizardList.get(0).getX();
                int wizardY = wizardList.get(0).getY();

                //find the closest snaffle to wizard 0
                int snaffleRefIndex = getClosestSnaffle(snaffleList, wizardX, wizardY);
                int snaffleX = snaffleList.get(snaffleRefIndex).getX();
                int snaffleY = snaffleList.get(snaffleRefIndex).getY();
                int snaffleVX = snaffleList.get(snaffleRefIndex).getVX();
                int snaffleVY = snaffleList.get(snaffleRefIndex).getVY();
                int closestSnaffle = ((snaffleX + snaffleVX) - wizardX) * ((snaffleX + snaffleVX) - wizardX) + ((snaffleY + snaffleVY) - wizardY) * ((snaffleY + snaffleVY) - wizardY);

                // Cast a Spell!
                for(int i = 0; i < snaffleList.size(); i++){
                    double angleForFlipendo = 0.1;
                    double angleToTopGoalPost = 0;
                    double angleToBotGoalPost = 0;
                    if(myTeamId == 0 && snaffleList.get(i).getX() - wizardX != 0){
                        angleForFlipendo = (snaffleList.get(i).getY() - wizardY) / (snaffleList.get(i).getX() - wizardX);
                        angleToTopGoalPost = 1.0 * (1800 - snaffleList.get(i).getY()) / 1.0 * (16000 - snaffleList.get(i).getX());
                        angleToBotGoalPost = 1.0 * (5700 - snaffleList.get(i).getY()) / 1.0 * (16000 - snaffleList.get(i).getX());
                    }
                    
                    if(myMagic >= 20 && spellCounter > 2 && myTeamId == 0 && wizardX + 1000 < snaffleList.get(i).getX() && angleToTopGoalPost < angleForFlipendo && angleForFlipendo < angleToBotGoalPost){
                        System.out.println("FLIPENDO " + snaffleList.get(i).getID());
                        wiz0Out = true;
                        myMagic -= 20;
                        spellCounter = 0;
                        continue;
                    }else if(myMagic >= 20 && spellCounter > 2 && myTeamId == 1 && wizardX - 1000 > snaffleList.get(i).getX() && angleToTopGoalPost > angleForFlipendo && angleForFlipendo > angleToBotGoalPost){
                        System.out.println("FLIPENDO " + snaffleList.get(i).getID());
                        wiz0Out = true;
                        myMagic -= 20;
                        spellCounter = 0;
                        continue;
                    }else if(myMagic >= 20 && spellCounter > 6  && myTeamId == 0 && 1750 < wizardY && wizardY < 5750 && snaffleList.get(i).getX() < wizardX && wizardX - snaffleList.get(i).getX() < 4000 && wizardX - snaffleList.get(i).getX() > 600){
                        System.out.println("ACCIO " + snaffleList.get(i).getID());
                        wiz0Out = true;
                        myMagic -= 15;
                        spellCounter = 0;
                        continue;
                    }else if(myMagic >= 20 && spellCounter > 6  && myTeamId == 1 && 1750 < wizardY && wizardY < 5750 && snaffleList.get(i).getX() > wizardX && snaffleList.get(i).getX() - wizardX < 4000 && snaffleList.get(i).getX() - wizardX > 600){
                        System.out.println("ACCIO " + snaffleList.get(i).getID());
                        wiz0Out = true;
                        myMagic -= 15;
                        spellCounter = 0;
                        continue;
                    }
                }// If you didnt cast a spell then i guess you should move.
                if(!wiz0Out && (snaffleVX * snaffleVX) + (snaffleVY * snaffleVY) >= 45000 && closestSnaffle > 45000){
                    System.out.println("MOVE " + (snaffleX + 3 * snaffleVX) + " " + (snaffleY + 3 * snaffleVY) + " " + 150);
                }else if(!wiz0Out && closestSnaffle > 22500){
                    System.out.println("MOVE " + (snaffleX + 2 * snaffleVX) + " " + (snaffleY + 2 * snaffleVY) + " " + 150);
                }else if(!wiz0Out && closestSnaffle > 1000){
                    System.out.println("MOVE " + (snaffleX + snaffleVX) + " " + (snaffleY + snaffleVY) + " " + 100);
                }else if(!wiz0Out){
                    System.out.println("MOVE " + (snaffleX + snaffleVX) + " " + (snaffleY + snaffleVY) + " " + 80);
                }
            }
            
//control wizard 1
            if(wizardList.get(1).getState() == 1 && myTeamId == 0){
                System.out.println("THROW 16000 3750 500");
                wiz1Out = true;
            }else if(wizardList.get(1).getState() == 1 && myTeamId == 1){
                System.out.println("THROW 0 3750 500");
                wiz1Out = true;
            }else{
                //where is wizard 1?
                int wizardX = wizardList.get(1).getX();
                int wizardY = wizardList.get(1).getY();

                //find the closest snaffle to wizard 1
                int snaffleRefIndex = getClosestSnaffle(snaffleList, wizardX, wizardY);
                int snaffleX = snaffleList.get(snaffleRefIndex).getX();
                int snaffleY = snaffleList.get(snaffleRefIndex).getY();
                int snaffleVX = snaffleList.get(snaffleRefIndex).getVX();
                int snaffleVY = snaffleList.get(snaffleRefIndex).getVY();
                int closestSnaffle = ((snaffleX + snaffleVX) - wizardX) * ((snaffleX + snaffleVX) - wizardX) + ((snaffleY + snaffleVY) - wizardY) * ((snaffleY + snaffleVY) - wizardY);

                // Cast a Spell!
                for(int i = 0; i < snaffleList.size(); i++){
                    double angleForFlipendo = 0.1;
                    double angleToTopGoalPost = 0;
                    double angleToBotGoalPost = 0;
                    if(snaffleList.get(i).getX() - wizardX != 0){
                        angleForFlipendo = (snaffleList.get(i).getY() - wizardY) / (snaffleList.get(i).getX() - wizardX);
                        angleToTopGoalPost = 1.0 * (1800 - snaffleList.get(i).getY()) / 1.0 * (16000 - snaffleList.get(i).getX());
                        angleToBotGoalPost = 1.0 * (5700 - snaffleList.get(i).getY()) / 1.0 * (16000 - snaffleList.get(i).getX());
                    }
                    
                    if(myMagic >= 20 && spellCounter > 3 && myTeamId == 0 && wizardX + 1000 < snaffleList.get(i).getX() && angleToTopGoalPost < angleForFlipendo && angleForFlipendo < angleToBotGoalPost){
                        System.out.println("FLIPENDO " + snaffleList.get(i).getID());
                        wiz1Out = true;
                        myMagic -= 20;
                        spellCounter = 0;
                        continue;
                    }else if(myMagic >= 20 && spellCounter > 3 && myTeamId == 1 && wizardX - 1000 > snaffleList.get(i).getX() && angleToTopGoalPost > angleForFlipendo && angleForFlipendo > angleToBotGoalPost){
                        System.out.println("FLIPENDO " + snaffleList.get(i).getID());
                        wiz1Out = true;
                        myMagic -= 20;
                        spellCounter = 0;
                        continue;
                    }else if(myMagic >= 20 && spellCounter > 7  && myTeamId == 0 && 1750 < wizardY && wizardY < 5750 && snaffleList.get(i).getX() < wizardX && wizardX - snaffleList.get(i).getX() < 4000 && wizardX - snaffleList.get(i).getX() > 600){
                        System.out.println("ACCIO " + snaffleList.get(i).getID());
                        wiz1Out = true;
                        myMagic -= 15;
                        spellCounter = 0;
                        continue;
                    }else if(myMagic >= 20 && spellCounter > 7  && myTeamId == 1 && 1750 < wizardY && wizardY < 5750 && snaffleList.get(i).getX() > wizardX && snaffleList.get(i).getX() - wizardX < 4000 && snaffleList.get(i).getX() - wizardX > 600){
                        System.out.println("ACCIO " + snaffleList.get(i).getID());
                        wiz1Out = true;
                        myMagic -= 15;
                        spellCounter = 0;
                        continue;
                    }
                }// If you didnt cast a spell then i guess you should move.
                if(!wiz1Out && (snaffleVX * snaffleVX) + (snaffleVY * snaffleVY) >= 45000 && closestSnaffle > 45000){
                    System.out.println("MOVE " + (snaffleX + 3 * snaffleVX) + " " + (snaffleY + 3 * snaffleVY) + " " + 150);
                }else if(!wiz1Out && closestSnaffle > 22500){
                    System.out.println("MOVE " + (snaffleX + 2 * snaffleVX) + " " + (snaffleY + 2 * snaffleVY) + " " + 150);
                }else if(!wiz1Out && closestSnaffle > 1000){
                    System.out.println("MOVE " + (snaffleX + snaffleVX) + " " + (snaffleY + snaffleVY) + " " + 100);
                }else if(!wiz1Out){
                    System.out.println("MOVE " + (snaffleX + snaffleVX) + " " + (snaffleY + snaffleVY) + " " + 80);
                }
                
            }
            //clear the list so that we can make the list again at the top.
            spellCounter++;
            wiz0Out = false;
            wiz1Out = false;
            wizardList.clear();
            snaffleList.clear();
        }
    }

    public static int getClosestSnaffle(ArrayList<Snaffle> snaffleList, int wizardX, int wizardY){
        int closestSnaffle = 2000000000;
        int snaffleRefIndex = 0;
        int snaffleX;
        int snaffleY;
        int snaffleVX;
        int snaffleVY;
        
        for(int j = 0; j<snaffleList.size(); j++){
            snaffleX = snaffleList.get(j).getX();
            snaffleY = snaffleList.get(j).getY();
            snaffleVX = snaffleList.get(j).getVX();
            snaffleVY = snaffleList.get(j).getVY();
            int distanceToSnaffle = ((snaffleX + snaffleVX) - wizardX) * ((snaffleX + snaffleVX) - wizardX) + ((snaffleY + snaffleVY) - wizardY) * ((snaffleY + snaffleVY) - wizardY);
            if(closestSnaffle > distanceToSnaffle){
                closestSnaffle = distanceToSnaffle;
                snaffleRefIndex = j;
            }
        }
        return snaffleRefIndex;
    }
}

class Entity{
    protected int entityId;
    protected String entityType;
    protected int x;
    protected int y;
    protected int vx;
    protected int vy;
    protected int state;
    Entity(int entityId, String entityType, int x, int y, int vx, int vy, int state){
        this.entityId = entityId;
        this.entityType = entityType;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.state = state;
    }
    public int getID(){
        return entityId;
    }
    public int getState(){
        return state;
    }
    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
    public int getVX(){
        return vx;
    }
    public int getVY(){
        return vy;
    }
}
class Enemy extends Entity{
    Enemy(int entityId, String entityType, int x, int y, int vx, int vy, int state){
        super(entityId, entityType, x, y, vx, vy, state);
    }
}
class Wizard extends Entity{
    Wizard(int entityId, String entityType, int x, int y, int vx, int vy, int state){
        super(entityId, entityType, x, y, vx, vy, state);
    }
}
class Snaffle extends Entity{
    Snaffle(int entityId, String entityType, int x, int y, int vx, int vy, int state){
        super(entityId, entityType, x, y, vx, vy, state);
    }
}
class Bludger extends Entity{
    Bludger(int entityId, String entityType, int x, int y, int vx, int vy, int state){
        super(entityId, entityType, x, y, vx, vy, state);
    }
}