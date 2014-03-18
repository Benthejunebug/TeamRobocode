package com.benthejunebug.robocode.twinDuel.Bobs.targeting;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import com.benthejunebug.robocode.twinDuel.Bobs.Bob;
import com.benthejunebug.robocode.twinDuel.Bobs.Bullet;
import com.benthejunebug.robocode.twinDuel.Bobs.OtherRobot;
import com.benthejunebug.robocode.twinDuel.Bobs.Utils;







public abstract class TargetingMethod implements Serializable {

	private static final long serialVersionUID = 1L;

	//Increment and decrement values for best targeting method log
	private static final long SUCESS_INCREMENT_FOR_VIRTUAL_HIT = 1;
	private static final long SUCESS_INCREMENT_FOR_REAL_HIT = 5;
	private static final long SUCESS_DECREMENT_FOR_REAL_HIT = -2;

	//Properties of a Targeting Method
	private String nameOfTarget;
	private ArrayList<Bullet> bullets; //Virtual bullets contained within the targeting method
	private long successCount = 0; //Success log: to determine the best targeting method on runtime. The Targeting method with the best value will be selected

	/**
	 *			     _                   _                       __      _       _                                                                      _   
                    | |                 | |                     / /     | |     | |                                                                    | |  
  ___ ___  _ __  ___| |_ _ __ _   _  ___| |_ ___  _ __ ___     / /    __| | __ _| |_ __ _   _ __ ___   __ _ _ __   __ _  __ _  ___ _ __ ___   ___ _ __ | |_ 
 / __/ _ \| '_ \/ __| __| '__| | | |/ __| __/ _ \| '__/ __|   / /    / _` |/ _` | __/ _` | | '_ ` _ \ / _` | '_ \ / _` |/ _` |/ _ \ '_ ` _ \ / _ \ '_ \| __|
| (_| (_) | | | \__ \ |_| |  | |_| | (__| || (_) | |  \__ \  / /    | (_| | (_| | || (_| | | | | | | | (_| | | | | (_| | (_| |  __/ | | | | |  __/ | | | |_ 
 \___\___/|_| |_|___/\__|_|   \__,_|\___|\__\___/|_|  |___/ /_/      \__,_|\__,_|\__\__,_| |_| |_| |_|\__,_|_| |_|\__,_|\__, |\___|_| |_| |_|\___|_| |_|\__|
                                                                                                                         __/ |                              
                                                                                                                        |___/                               
	 */

	//General constructor for creating targeting methods. Initializes important values
	public TargetingMethod(String nameOfTarget) {
		this.nameOfTarget = nameOfTarget;
		this.bullets = new ArrayList<Bullet>();
	}


	//Constructor for use in copying
	public TargetingMethod(TargetingMethod method) {

		this.hasSameMethodName(method);

		this.nameOfTarget = method.getNameOfMethod();
		this.successCount = method.getSuccessCount();
		this.bullets = new ArrayList<Bullet>();

		for (Bullet bullet : method.getBullets()) {
			this.bullets.add(new Bullet(bullet));
		}

	}


	//For use with the TargetingMethod copy method. Each targeting method will return a new instance of the targeting method with itself passed through the constructor
	public abstract TargetingMethod copy();


	//Merges a secondary TargetingMethod with this one. The greatest successCount will be used and such
	public void merge(TargetingMethod method) {

		this.isSameMethod(method);

		if (getSuccessCount() < method.getSuccessCount()) {
			this.successCount = method.getSuccessCount();
		}

		additionalMerge(method);
	}

	//OPTIONAL: Additional tasks by each TargetingMethod on each merge
	protected void additionalMerge(TargetingMethod method){}

	//To be run at a new round. Removes all lingering virtual bullets. TODO: consider adding an abstract method for all additional new round tasks
	public void newRound() {
		this.bullets.removeAll(this.bullets);
	}

	//Nullifies all unshared data for the message transfer.
	public void deleteUnsharedData() {
		this.bullets = null;
		additionalDeleteUnsharedData();
	}

	//OPTIONAL: Additional self garbage collection for sending only the necessary parts in each message
	protected void additionalDeleteUnsharedData(){}
	
	/**
	 * 
_________ _______  _______  _______  _______ __________________ _        _______ 
\__   __/(  ___  )(  ____ )(  ____ \(  ____ \\__   __/\__   __/( (    /|(  ____ \
   ) (   | (   ) || (    )|| (    \/| (    \/   ) (      ) (   |  \  ( || (    \/
   | |   | (___) || (____)|| |      | (__       | |      | |   |   \ | || |      
   | |   |  ___  ||     __)| | ____ |  __)      | |      | |   | (\ \) || | ____ 
   | |   | (   ) || (\ (   | | \_  )| (         | |      | |   | | \   || | \_  )
   | |   | )   ( || ) \ \__| (___) || (____/\   | |   ___) (___| )  \  || (___) |
   )_(   |/     \||/   \__/(_______)(_______/   )_(   \_______/|/    )_)(_______)
	 *
	 */

	//abstract function to return the predicted location of the targeted robot based on the method of the targeting method itself.
	//The targeting method itself is to be stored HERE.
	public abstract Point2D.Double targetReturningLocation(OtherRobot observingRobot, OtherRobot targetedRobot, double power);

	//Returns an angle relative to the heading of the observing (targeting) robot to the predicted location of the targeted robot
	public double target(OtherRobot observingRobot, OtherRobot targetedRobot, double power) {
		isSameTarget(targetedRobot);
		Point2D.Double targetLocation = targetReturningLocation(observingRobot, targetedRobot, power);
		return robocode.util.Utils.normalRelativeAngle(Math.atan2(targetLocation.x - observingRobot.getX(), targetLocation.y - observingRobot.getY()));
	}

	//Returns an angle relative to the current gun heading of the observing robot. Will be most commonly used for all aiming.
	public double targetReturningRelativeTurningAngle(OtherRobot observingRobot, OtherRobot targetedRobot, double power) {
		return robocode.util.Utils.normalRelativeAngle(target(observingRobot, targetedRobot, power) - observingRobot.getGunHeading());
	}

	//A commonly used loop for predicting the location of the targetedRobot when the bullet will hit. This method is optional for use.
	protected Point2D.Double targetingLoop(OtherRobot observingRobot, OtherRobot targetedRobot, double power){
		Point2D.Double predictedLocation = new Point2D.Double(targetedRobot.getX(), targetedRobot.getY());
		int elapsedTime = 0;

		double bulletVelocity = Bullet.getBulletVelocity(power);

		while (elapsedTime * bulletVelocity < Utils.getResultant(observingRobot.getX(), predictedLocation.x, observingRobot.getX(), predictedLocation.y)) {
			predictedLocation = Utils.projectPosition(predictedLocation, getTargetedRobotVelocityForTargetingLoop(targetedRobot, elapsedTime), getTargetedRobotHeadingForTargetingLoop(targetedRobot, elapsedTime));
			elapsedTime++;
			if ((predictedLocation.x < Bob.WALL_WIDTH) || (predictedLocation.y < Bob.WALL_WIDTH) || (predictedLocation.x > Bob.BATTLE_FIELD_WIDTH - Bob.WALL_WIDTH) || (predictedLocation.y > Bob.BATTLE_FIELD_HEIGHT - Bob.WALL_WIDTH)) {
				predictedLocation.x = Math.min(Math.max(Bob.WALL_WIDTH, predictedLocation.x), Bob.BATTLE_FIELD_WIDTH - Bob.WALL_WIDTH);
				predictedLocation.y = Math.min(Math.max(Bob.WALL_WIDTH, predictedLocation.y), Bob.BATTLE_FIELD_HEIGHT - Bob.WALL_WIDTH);
				break;
			}
		}
		
		return predictedLocation;
	}

	//OPTIONAL: Returns the velocity for use in the targeting loop. Only necessary when the targetingLoop is used
	protected double getTargetedRobotVelocityForTargetingLoop(OtherRobot targetedRobot, double elapsedTime){
		this.throwNonImplementedException();
		return 0;
	}

	//OPTIONAL: Returns the velocity for use in the targeting loop. Only necessary when the targetingLoop is used
	protected double getTargetedRobotHeadingForTargetingLoop(OtherRobot targetedRobot, double elapsedTime){
		this.throwNonImplementedException();
		return 0;
	}

	//Fires a virtual bullet in predicted direction of the targeted robot.
	public void fireVirtual(OtherRobot observingRobot, OtherRobot targetedRobot, double power, long timeOfFire) {
		this.bullets.add(new Bullet(observingRobot.getName(), targetedRobot.getName(), targetedRobot.getAbsBearing(observingRobot.getX(), observingRobot.getY()), targetedRobot.getLateralDirection(observingRobot.getX(), observingRobot.getY()), true, observingRobot.getX(), observingRobot.getY(), power, timeOfFire, target(observingRobot, targetedRobot, power)));
	}

	//Fires a real bullet such that one can evaluate the success of the special case. (It is one thing if it works virtually. Another entirely if we get a robot hit event.)
	public void fireReal(OtherRobot observingRobot, OtherRobot targetedRobot, double power, long timeOfFire, double angle) {
		this.bullets.add(new Bullet(observingRobot.getName(), targetedRobot.getName(), targetedRobot.getAbsBearing(observingRobot.getX(), observingRobot.getY()), targetedRobot.getLateralDirection(observingRobot.getX(), observingRobot.getY()), false, observingRobot.getX(), observingRobot.getY(), power, timeOfFire, angle));
	}

	/**
	 * 
 				   ,,    ,,    ,,            ,,                                ,,                                         ,,                                                    ,,  
                 `7MM  `7MM    db            db                              `7MM           mm                     mm     db                                                  `7MM  
                   MM    MM                                                    MM           MM                     MM                                                           MM  
 ,p6"bo   ,pW"Wq.  MM    MM  `7MM  ,pP"Ybd `7MM  ,pW"Wq.`7MMpMMMb.        ,M""bMM  .gP"Ya mmMMmm .gP"Ya   ,p6"bo mmMMmm `7MM  ,pW"Wq.`7MMpMMMb.       ,6"Yb.  `7MMpMMMb.   ,M""bMM  
6M'  OO  6W'   `Wb MM    MM    MM  8I   `"   MM 6W'   `Wb MM    MM      ,AP    MM ,M'   Yb  MM  ,M'   Yb 6M'  OO   MM     MM 6W'   `Wb MM    MM      8)   MM    MM    MM ,AP    MM  
8M       8M     M8 MM    MM    MM  `YMMMa.   MM 8M     M8 MM    MM      8MI    MM 8M""""""  MM  8M"""""" 8M        MM     MM 8M     M8 MM    MM       ,pm9MM    MM    MM 8MI    MM  
YM.    , YA.   ,A9 MM    MM    MM  L.   I8   MM YA.   ,A9 MM    MM      `Mb    MM YM.    ,  MM  YM.    , YM.    ,  MM     MM YA.   ,A9 MM    MM      8M   MM    MM    MM `Mb    MM  
 YMbmd'   `Ybmd9'.JMML..JMML..JMML.M9mmmP' .JMML.`Ybmd9'.JMML  JMML.     `Wbmd"MML.`Mbmmd'  `Mbmo`Mbmmd'  YMbmd'   `Mbmo.JMML.`Ybmd9'.JMML  JMML.    `Moo9^Yo..JMML  JMML.`Wbmd"MML.



  ,,                                 ,,                                                                                                                                             
`7MM                                 db                                                                                                                                             
  MM                                                                                                                                                                                
  MM  ,pW"Wq.   .P"Ybmmm  .P"Ybmmm `7MM  `7MMpMMMb.  .P"Ybmmm                                                                                                                       
  MM 6W'   `Wb :MI  I8   :MI  I8     MM    MM    MM :MI  I8                                                                                                                         
  MM 8M     M8  WmmmP"    WmmmP"     MM    MM    MM  WmmmP"                                                                                                                         
  MM YA.   ,A9 8M        8M          MM    MM    MM 8M                                                                                                                              
.JMML.`Ybmd9'   YMMMMMb   YMMMMMb  .JMML..JMML  JMML.YMMMMMb                                                                                                                        
               6'     dP 6'     dP                  6'     dP                                                                                                                       
               Ybmmmd'   Ybmmmd'                    Ybmmmd'                               
	 * 
	 */

	//Combs through the fired bullets and evaluates, with simple collision detection, whether or not the virtual bullet hit the robot.
	//Also decrements successCount for a failed real bullet
	public void checkForVirtualCollisions(OtherRobot targetedRobot, long currentTime) {

		isSameTarget(targetedRobot);

		for (int i = 0; i<this.bullets.size(); i++) {
			Bullet bullet = this.bullets.get(i);
			double distanceTraveled = bullet.getDistanceTraveled(currentTime);
			double distanceOfTargetedRobotFromSource = targetedRobot.getDistance(bullet.getSourceX(), bullet.getSourceY());

			//checks if bullet has either passed the targeted robot or if it has traveled farther than the longest distance possible on the battlefield
			if ((distanceTraveled > distanceOfTargetedRobotFromSource + Math.sqrt(2) * targetedRobot.getWidth()/2) || (distanceTraveled > Utils.getResultant(Bob.BATTLE_FIELD_WIDTH, Bob.BATTLE_FIELD_HEIGHT))) {

				if (bullet.isVirtual()) {
					additionalLogForVirtualCollisions(targetedRobot, currentTime, bullet, false);
				} else {
					additionalLogForRealCollisions(targetedRobot, currentTime, bullet, false);
					this.successCount += SUCESS_DECREMENT_FOR_REAL_HIT;
				}

				this.bullets.remove(i);
			} else if (bullet.isVirtual()) { //TODO: consider a more elegant else if
				Point2D.Double bulletLocation = bullet.getLocation(currentTime);
				double distanceBetweenBulletAndTarget = targetedRobot.getDistance(bulletLocation.x, bulletLocation.y);

				//collision detection for virtual bullets. increments if hit
				if (distanceBetweenBulletAndTarget < Math.sqrt(2) * targetedRobot.getWidth() / 2) {
					additionalLogForVirtualCollisions(targetedRobot, currentTime, bullet, true);
					this.successCount += SUCESS_INCREMENT_FOR_VIRTUAL_HIT;
					this.bullets.remove(i);
				}
			}
		}
	}

	//OPTIONAL: Additional log by the targetingMethod when a virtual bullet hits another robot.
	protected void additionalLogForVirtualCollisions(OtherRobot targetedRobot, long currentTime, Bullet bullet, boolean hit){}

	//Runs upon a BulletHit event to check if a bullet fired from a given targeting method has hit the robot.
	public void checkForRealCollisions(OtherRobot hitRobot, long hitTime) {
		isSameTarget(hitRobot);
		for (int i = 0; i<this.bullets.size(); i++) {
			Bullet bullet = this.bullets.get(i);
			
			if (!bullet.isVirtual()) {
				Point2D.Double bulletLocation = bullet.getLocation(hitTime);
				double distanceBetweenRobotAndBullet = hitRobot.getDistance(bulletLocation.x, bulletLocation.y);

				if (distanceBetweenRobotAndBullet < Math.sqrt(2) * hitRobot.getWidth() / 2) {
					additionalLogForRealCollisions(hitRobot, hitTime, bullet, true);
					this.successCount += SUCESS_INCREMENT_FOR_REAL_HIT;
					this.bullets.remove(i);
					break;
				}
			}
		}
	}

	//OPTIONAL: Additional log by the targetingMethod when a real bullet hits another robot.	
	protected void additionalLogForRealCollisions(OtherRobot targetedRobot, long hitTime, Bullet bullet, boolean hit){}

	//Checks if the method name is the same and throws an exception otherwise
	public void hasSameMethodName(TargetingMethod method){
		if(!this.getNameOfMethod().equals(method.getNameOfMethod())){
			System.out.println("The two objects do not have the same Method.");
			throw new IllegalArgumentException();			
		}
	}

	/**
	 *
         _   _         _          _            _       _    _            _                                                                                   
       /\_\/\_\ _    /\ \       /\ \         / /\    / /\ /\ \         /\ \                                                                                 
      / / / / //\_\ /  \ \      \_\ \       / / /   / / //  \ \       /  \ \____                                                                            
     /\ \/ \ \/ / // /\ \ \     /\__ \     / /_/   / / // /\ \ \     / /\ \_____\                                                                           
    /  \____\__/ // / /\ \_\   / /_ \ \   / /\ \__/ / // / /\ \ \   / / /\/___  /                                                                           
   / /\/________// /_/_ \/_/  / / /\ \ \ / /\ \___\/ // / /  \ \_\ / / /   / / /                                                                            
  / / /\/_// / // /____/\    / / /  \/_// / /\/___/ // / /   / / // / /   / / /                                                                             
 / / /    / / // /\____\/   / / /      / / /   / / // / /   / / // / /   / / /                                                                              
/ / /    / / // / /______  / / /      / / /   / / // / /___/ / / \ \ \__/ / /                                                                               
\/_/    / / // / /_______\/_/ /      / / /   / / // / /____\/ /   \ \___\/ /                                                                                
        \/_/ \/__________/\_\/       \/_/    \/_/ \/_________/     \/_____/                                                                                 

 _          _       _            _            _          _        _           _             _                 _          _          _            _          
/\ \    _ / /\     /\ \         /\ \         /\ \       /\ \     /\ \       /\ \           / /\              /\ \       /\ \       /\ \         /\ \     _  
\ \ \  /_/ / /    /  \ \       /  \ \        \ \ \     /  \ \    \ \ \     /  \ \         / /  \             \_\ \      \ \ \     /  \ \       /  \ \   /\_\
 \ \ \ \___\/    / /\ \ \     / /\ \ \       /\ \_\   / /\ \ \   /\ \_\   / /\ \ \       / / /\ \            /\__ \     /\ \_\   / /\ \ \     / /\ \ \_/ / /
 / / /  \ \ \   / / /\ \_\   / / /\ \_\     / /\/_/  / / /\ \_\ / /\/_/  / / /\ \ \     / / /\ \ \          / /_ \ \   / /\/_/  / / /\ \ \   / / /\ \___/ / 
 \ \ \   \_\ \ / /_/_ \/_/  / / /_/ / /    / / /    / /_/_ \/_// / /    / / /  \ \_\   / / /  \ \ \        / / /\ \ \ / / /    / / /  \ \_\ / / /  \/____/  
  \ \ \  / / // /____/\    / / /__\/ /    / / /    / /____/\  / / /    / / /    \/_/  / / /___/ /\ \      / / /  \/_// / /    / / /   / / // / /    / / /   
   \ \ \/ / // /\____\/   / / /_____/    / / /    / /\____\/ / / /    / / /          / / /_____/ /\ \    / / /      / / /    / / /   / / // / /    / / /    
    \ \ \/ // / /______  / / /\ \ \  ___/ / /__  / / /   ___/ / /__  / / /________  / /_________/\ \ \  / / /   ___/ / /__  / / /___/ / // / /    / / /     
     \ \  // / /_______\/ / /  \ \ \/\__\/_/___\/ / /   /\__\/_/___\/ / /_________\/ / /_       __\ \_\/_/ /   /\__\/_/___\/ / /____\/ // / /    / / /      
      \_\/ \/__________/\/_/    \_\/\/_________/\/_/    \/_________/\/____________/\_\___\     /____/_/\_\/    \/_________/\/_________/ \/_/     \/_/      
	 * 
	 */

	//Checks if the name of a targeted robot matches the dedicated targetedRobot for the given method and throws an exception otherwise.
	public void isSameTarget(String targetedRobotName) {
		if (!getNameOfTarget().equals(targetedRobotName)) {
			System.out.println("TargetedRobot is not the same");
			throw new IllegalArgumentException();
		}
	}

	//Checks if the OtherRobot targetedRobot matches the dedicated targetedRobot for the given method and throws an exception otherwise
	public void isSameTarget(OtherRobot targetedRobot){
		this.isSameTarget(targetedRobot.getName());
	}

	//Checks if the method is identical to this one (same target and same method name) and throws an exception otherwise
	public void isSameMethod(TargetingMethod method){
		this.hasSameMethodName(method);
		this.isSameTarget(method.getNameOfTarget());
	}

	//throws an exception. For use with optional methods that have not yet been overridden. (Such that one can avoid a bunch of empty methods in the TargetingMethods classes)
	private void throwNonImplementedException() {
		try {
			throw new NoSuchMethodException();
		} catch (NoSuchMethodException e) {
			System.out.println("There was an attempt at calling a non-implemented method. FIX YO CODE!");
			e.printStackTrace();
			System.exit(1);
		}
	}

	
	/**
	 *
   ______          _     _                                               __    ______          _     _                        
 .' ___  |        / |_  / |_                                            |  ] .' ____ \        / |_  / |_                      
/ .'   \_|  .---.`| |-'`| |-'.---.  _ .--.  .--.    ,--.   _ .--.   .--.| |  | (___ \_| .---.`| |-'`| |-'.---.  _ .--.  .--.  
| |   ____ / /__\\| |   | | / /__\\[ `/'`\]( (`\]  `'_\ : [ `.-. |/ /'`\' |   _.____`. / /__\\| |   | | / /__\\[ `/'`\]( (`\] 
\ `.___]  || \__.,| |,  | |,| \__., | |     `'.'.  // | |, | | | || \__/  |  | \____) || \__.,| |,  | |,| \__., | |     `'.'. 
 `._____.'  '.__.'\__/  \__/ '.__.'[___]   [\__) ) \'-;__/[___||__]'.__.;__]  \______.' '.__.'\__/  \__/ '.__.'[___]   [\__) )
	 *
	 */

	//Returns the name of the given method. It is suggested the name is placed at the top of the Targeting Method as a static final field.
	public abstract String getNameOfMethod();

	//Returns the assigned color of a given method. It is suggested the color is placed at the top of the Targeting Method as a static final field.
	public abstract Color getColor();

	public String getNameOfTarget() {
		return this.nameOfTarget;
	}

	public ArrayList<Bullet> getBullets() {
		return this.bullets;
	}

	public long getSuccessCount() {
		return this.successCount;
	}

}
