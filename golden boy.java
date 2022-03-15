import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Grab Snaffles and try to throw them through the opponent's goal!
 * Move towards a Snaffle and use your team id to determine where you need to throw it.
 **/
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int myTeamId = in.nextInt(); // if 0 you need to score on the right of the map, if 1 you need to score on the left

        Entity wizard0 = null;
        Entity wizard1 = null;
        int n = 0;
        // game loop
        while (true) {
            int myScore = in.nextInt();
            int myMagic = in.nextInt();
            int opponentScore = in.nextInt();
            int opponentMagic = in.nextInt();
            int entities = in.nextInt(); // number of entities still in game

            ArrayList<Entity> snaffles = new ArrayList<Entity>();
            ArrayList<Entity> wizards = new ArrayList<Entity>();
            ArrayList<Entity> enemies = new ArrayList<Entity>();
            ArrayList<Entity> bludgers = new ArrayList<Entity>();
            for (int i = 0; i < entities; i++) {

                int entityId = in.nextInt(); // entity identifier
                String entityType = in.next(); // "WIZARD", "OPPONENT_WIZARD" or "SNAFFLE" (or "BLUDGER" after first league)
                int x = in.nextInt(); // position
                int y = in.nextInt(); // position
                int vx = in.nextInt(); // velocity
                int vy = in.nextInt(); // velocity
                int state = in.nextInt(); // 1 if the wizard is holding a Snaffle, 0 otherwise

                if(entityType.equals("WIZARD")){
                    wizards.add(new Entity(entityId, entityType, x, y, vx, vy, state));
                }else if(entityType.equals("SNAFFLE")){
                    snaffles.add(new Entity(entityId, entityType, x, y, vx, vy, state));
                }else if(entityType.equals("BLUDGER")){
                    bludgers.add(new Entity(entityId, entityType, x, y, vx, vy, state));
                }else{
                    enemies.add(new Entity(entityId, entityType, x, y, vx, vy, state));
                }
                
            }
            if(n == 0){
                wizard0 = wizards.get(0);
                wizard1 = wizards.get(1);
            }else{
                wizards.get(0).target = wizard0.target;
                wizards.get(1).target = wizard1.target;
                wizards.get(0).petTarget = wizard0.petTarget;
                wizards.get(1).petTarget = wizard1.petTarget;
                wizard0 = wizards.get(0);
                wizard1 = wizards.get(1);
            }
            
            for (int i = 0; i < 2; i++) {

                String print = "";

                Entity otherWizard = i == 0? wizard1 : wizard0;

                Entity wizard = i == 1? wizard1 : wizard0;

                print = whatDo(wizard, otherWizard, snaffles, myMagic, myTeamId);
               
                System.out.println(print);

            }
            n++;
        }   
    }

    public static String whatDo(Entity me, Entity otherWizard, ArrayList<Entity> snaffles, int myMagic, int myTeam){
        Entity closeSnaf = findClosestEnt(snaffles, me);
        Entity secondClosestSnaf = findSecondClosestEnt(snaffles, closeSnaf, me);
        Entity closeToMyGoal = closeToMyGoal(snaffles, myTeam);
        Entity otherWizardSnaf = new Entity(69,"69",69,69,69,69,69);
        boolean imCloserToCloseSnaf = calcDist(me,closeSnaf) < calcDist(otherWizard,closeSnaf) ? true:false;
        boolean betweenMeAndGoal = betweenMeAndGoal(myTeam, closeSnaf, me);
        double snafDist = calcDist(closeSnaf,me);

        System.err.println("\nMy(" + me.entityId + ")target: "  + me.target);
        System.err.println("Other(" + otherWizard.entityId + ")target: "  + otherWizard.target);

        if(otherWizard.state == 1){
            otherWizardSnaf = whatAmIHolding(snaffles, otherWizard);
        }
        
        if(me.state == 1){
            me.state = 0;
            return throwAtGoal(me, myTeam);
        }else{
            if((myMagic >= 20) && (goodFlipendoAngle(closeSnaf, me, myTeam) > 0)){
                
                return "FLIPENDO" + " " + closeSnaf.entityId;

            }else if((myMagic >= 15) && (!(betweenMeAndGoal) && (snafDist > 2000) && (snafDist < 9000))){

                return "ACCIO" + " " + closeSnaf.entityId; 

            }else if((myMagic >= 10) && (closeToMyGoal != null) && (otherWizardSnaf.entityId != closeToMyGoal.entityId) && (calcDist(otherWizard,closeToMyGoal) > 1500) && (me.petTarget != closeToMyGoal.entityId) && (otherWizard.petTarget != closeToMyGoal.entityId)){

                me.petTarget = closeToMyGoal.entityId;
                return "PETRIFICUS" + " " + closeToMyGoal.entityId;

            }else if(((imCloserToCloseSnaf) && (closeSnaf.entityId != otherWizardSnaf.entityId) && (otherWizard.target != closeSnaf.entityId || otherWizard.target == 0)) || (snaffles.size() == 1)){

                me.target = closeSnaf.entityId;
                return "MOVE" + " " + closeSnaf.x + " " + closeSnaf.y + " 150";

            }else{

                me.target = secondClosestSnaf.entityId;
                return "MOVE" + " " + secondClosestSnaf.x + " " + secondClosestSnaf.y + " 150";
            }
        } 
    }


    public static Entity findSecondClosestEnt(ArrayList<Entity> ents, Entity close, Entity me){
         ArrayList<Entity> removeClosest = new ArrayList<Entity>();

            for(int i = 0; i < ents.size(); i++){
                if(ents.get(i).entityId != close.entityId){
                    removeClosest.add(ents.get(i));
                }
            }
            if(removeClosest.size() == 0){
                return close;
            }else{
                return findClosestEnt(removeClosest,me);
            }
        
    }

    public static String throwAtGoal(Entity me, int myTeam){
       
        int targetX = myTeam == 1? 0 : 16000;
        int targetY = 3750;
        if((me.y > 2750) && (me.y < 4750)){
            targetY = me.y;
        }else if((me.y <= 2750)){
            targetY = 2750;
        }else if((me.y >= 4750)){
            targetY = 4750;
        }
        return "THROW" + " " + targetX + " " + targetY + " " + 500;
    }

    public static double goodFlipendoAngle(Entity snaf, Entity me, int myTeam){
        int targetTop = 2250;
        int targetBot = 5250;

        if(((me.y < snaf.y) && (snaf.y > targetBot)) || ((me.y > snaf.y) && (snaf.y < targetTop)) || (!(betweenMeAndGoal(myTeam, snaf, me)))){
            return 0;
        }
        double hyp = calcDist(snaf,me);
        double opp = Math.abs(snaf.y - me.y);
        double angle = Math.toDegrees(Math.asin(opp/hyp));

        double a = myTeam == 0? calcDist(snaf.x,snaf.y,16000,targetTop) : calcDist(snaf.x,snaf.y,0,targetTop);
        double b = myTeam == 0? calcDist(snaf.x,snaf.y,16000,targetBot) : calcDist(snaf.x,snaf.y,0,targetBot);
        double c = targetBot-targetTop;
        double spreadAngle = Math.toDegrees(Math.acos((a*a + b*b - c*c) / (2 * a * b)));
        double goalDistX = myTeam == 0? 16000 - snaf.x : snaf.x;
        double goalDistY1 = Math.abs(snaf.y - targetTop);
        double goalDistY2 = Math.abs(snaf.y - targetBot);
        double minAngle = 0;
        double maxAngle = 0;

        if(snaf.y > targetBot){
            minAngle = Math.toDegrees(Math.atan(goalDistY2/goalDistX));
            maxAngle = minAngle + spreadAngle;
        }else if(snaf.y < targetBot && snaf.y > targetTop){
            minAngle = 0;
            maxAngle = snaf.y > 3750 ? Math.toDegrees(Math.atan(goalDistY2/goalDistX)) : Math.toDegrees(Math.atan(goalDistY1/goalDistX));
        }else{
            minAngle = Math.toDegrees(Math.atan(goalDistY1/goalDistX));
            maxAngle = minAngle + spreadAngle;
        }


        System.err.println("Target=" + snaf.entityId + "\nMaxAngle=" + maxAngle + "\nMinAngle=" + minAngle + "\nSpreadAngle=" + spreadAngle + "\nMeasuredAngle=" + angle + "\n a=" + a + "\n b=" + b + "\n");

        if(angle >= minAngle && angle <= maxAngle){
            return spreadAngle;
        }else{
            return 0;
        }
    }

    public static Entity whatAmIHolding(ArrayList<Entity> snaffles, Entity me){

        for(int i = 0; i < snaffles.size(); i++){
            if((snaffles.get(i).x == me.x) && (snaffles.get(i).y == me.y)){
                return snaffles.get(i);
            }
        }
        return null;
    }
    
    public static Entity closeToMyGoal(ArrayList<Entity> ents, int myTeam){
        Entity ent = null;
        
        double goalX = myTeam == 0? 0 : 16000;
        double maxY = 5750;
        double minY = 1750;
        double direction = myTeam == 0? -1 : 1;

        for(int i = 0; i < ents.size(); i++){
            Entity temp = ents.get(i);
            double dist = Math.abs(goalX - temp.x);
            double v = temp.vx;

            if(((dist < 1500) && (temp.y < maxY) && (temp.y > minY) && (v * direction > 80)) || ((v * direction > 1000) && (dist < 4000))){
                System.err.println("\nSnaffle:" + temp.entityId + " Velocity(x):" + temp.vx + " Direction:" + direction);
                ent = temp;
            }

        }
        return ent;
    }

    public static boolean betweenMeAndGoal(int myTeam, Entity ent, Entity me){
        if(myTeam == 1){
            if(me.x > ent.x){
                return true;
            }else{
                return false;
            }
        }else{
            if(me.x < ent.x){
                return true;
            }else{
                return false;
            }
        }
    }

    public static Entity findClosestEnt(ArrayList<Entity> ents,Entity me){

        double min = 999999999;
        Entity closeEnt = null;
        for(int i = 0; i < ents.size(); i++){
            double dist = calcDist(me, ents.get(i));
            if(dist < min){
                min = dist;
                closeEnt = ents.get(i);   
            }
        }
        return closeEnt;
    }

    public static double calcDist(Entity A, Entity B){
        return  calcDist(A.x, A.y, B.x, B.y);
    }

    public static double calcDist(int x1, int y1, int x2, int y2){
        return  Math.sqrt(Math.pow((x1-x2),2) + Math.pow((y1-y2),2));
    }

}

class Entity{

   public int entityId;
   public String entityType;
   public int x;
   public int y;
   public int vx;
   public int vy;
   public int state;
   public int target;
   public int petTarget;
  

    public Entity(int Id, String Type, int X, int Y, int VX, int VY, int STATE){
        entityId =  Id;
        entityType = Type;
        x = X;
        y = Y;
        vx = VX;
        vy = VY;
        state = STATE;
    }
    
}


