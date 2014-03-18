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

	//General constructor for creating targeting methods. Initializes important values
	public TargetingMethod(String nameOfTarget) {
		this.nameOfTarget = nameOfTarget;
		this.bullets = new ArrayList<Bullet>();
	}


	//Constructor for use in copying
	public TargetingMethod(TargetingMethod method) {

		this.nameOfTarget = method.getNameOfMethod();
		this.successCount = method.getSuccessCount();
		this.bullets = new ArrayList<Bullet>();

		for (Bullet bullet : method.getBullets()) {
			this.bullets.add(new Bullet(bullet));
		}

	}


	//For use with the TargetingMethod copy method. Each targeting method will return a new instance of the targeting method with itself passed through the constructor
	abstract TargetingMethod copy();

	//Additional tasks by each TargetingMethod on each merge
	abstract void additionalMerge(TargetingMethod method);

	//Additional self garbage collection for sending only the necessary parts in each message
	abstract void additionalDeleteUnsharedData();

	//Merges a secondary TargetingMethod with this one. The greatest successCount will be used and such
	public void merge(TargetingMethod method) {

		if ((!getNameOfMethod().equals(method.getNameOfMethod())) || (!getNameOfTarget().equals(method.getNameOfTarget()))) {
			System.out.println("Methods are not the same!");
			throw new IllegalArgumentException();
		}

		if (getSuccessCount() < method.getSuccessCount()) {
			this.successCount = method.getSuccessCount();
		}

		additionalMerge(method);
	}

	//To be run at a new round. Removes all lingering virtual bullets. TODO: consider adding an abstract method for all additional new round tasks
	public void newRound() {
		this.bullets.removeAll(this.bullets);
	}

	//Nullifies all unshared data for the message transfer.
	public void deleteUnsharedData() {
		this.bullets = null;
		additionalDeleteUnsharedData();
	}

	//abstract function to return the predicted location of the targeted robot based on the method of the targeting method itself.
	//The targeting method itself is to be stored HERE.
	public abstract Point2D.Double targetReturningLocation(OtherRobot observingRobot, OtherRobot targetedRobot, double power);

	//Returns an angle relative to the heading og the observing (targeting) robot to the predicted location of the targeted robot
	public double target(OtherRobot observingRobot, OtherRobot targetedRobot, double power) {
		checkSameTarget(targetedRobot);
		Point2D.Double targetLocation = targetReturningLocation(observingRobot, targetedRobot, power);
		return robocode.util.Utils.normalRelativeAngle(Math.atan2(targetLocation.x - observingRobot.getX(), targetLocation.y - observingRobot.getY()));
	}

	//Returns an angle relative to the current gun heading of the observing robot. Will be most commonly used for all aiming.
	public double targetReturningRelativeTurningAngle(OtherRobot observingRobot, OtherRobot targetedRobot, double power) {
		return robocode.util.Utils.normalRelativeAngle(target(observingRobot, targetedRobot, power) - observingRobot.getGunHeading());
	}

	//Fires a virtual bullet in predicted direction of the targeted robot.
	public void fireVirtual(OtherRobot observingRobot, OtherRobot targetedRobot, double power, long timeOfFire) {
		this.bullets.add(new Bullet(observingRobot.getName(), targetedRobot.getName(), targetedRobot.getAbsBearing(observingRobot.getX(), observingRobot.getY()), targetedRobot.getLateralDirection(observingRobot.getX(), observingRobot.getY()), true, observingRobot.getX(), observingRobot.getY(), power, timeOfFire, target(observingRobot, targetedRobot, power)));
	}

	//Fires a real bullet such that one can evaluate the success of the special case. (It is one thing if it works virtually. Another entirely if we get a robot hit event.)
	public void fireReal(OtherRobot observingRobot, OtherRobot targetedRobot, double power, long timeOfFire, double angle) {
		this.bullets.add(new Bullet(observingRobot.getName(), targetedRobot.getName(), targetedRobot.getAbsBearing(observingRobot.getX(), observingRobot.getY()), targetedRobot.getLateralDirection(observingRobot.getX(), observingRobot.getY()), false, observingRobot.getX(), observingRobot.getY(), power, timeOfFire, angle));
	}

	//Combs through the fired bullets and evaluates, with simple collision detection, whether or not the virtual bullet hit the robot.
	//Also decrements successCount for a failed real bullet
	public void checkForVirtualCollisions(OtherRobot targetedRobot, long currentTime) {
		checkSameTarget(targetedRobot);

		for (Bullet bullet: this.bullets) {
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
				
				this.bullets.remove(bullet);
			} else if (bullet.isVirtual()) { //TODO: consider a more elegant else if
				Point2D.Double bulletLocation = bullet.getLocation(currentTime);
				double distanceBetweenBulletAndTarget = targetedRobot.getDistance(bulletLocation.x, bulletLocation.y);
				
				//collision detection for virtual bullets. increments if hit
				if (distanceBetweenBulletAndTarget < Math.sqrt(2) * targetedRobot.getWidth() / 2) {
					additionalLogForVirtualCollisions(targetedRobot, currentTime, bullet, true);
					this.successCount += SUCESS_INCREMENT_FOR_VIRTUAL_HIT;
					this.bullets.remove(bullet);
				}
			}
		}
	}

	//Runs upon a BulletHit event to check if a bullet fired from a given targeting method has hit the robot.
	public void checkForRealCollisions(OtherRobot hitRobot, long hitTime) {
		checkSameTarget(hitRobot);
		for (Bullet bullet: this.bullets) {
			
			if (!bullet.isVirtual()) {
				Point2D.Double bulletLocation = bullet.getLocation(hitTime);
				double distanceBetweenRobotAndBullet = hitRobot.getDistance(bulletLocation.x, bulletLocation.y);
				
				if (distanceBetweenRobotAndBullet < Math.sqrt(2) * hitRobot.getWidth() / 2) {
					additionalLogForRealCollisions(hitRobot, hitTime, bullet, true);
					this.successCount += SUCESS_INCREMENT_FOR_REAL_HIT;
					this.bullets.remove(bullet);
					break;
				}
			}
		}
	}

	//Checks if the targeted robot is the correct robot and throws an exception otherwise.
	public void checkSameTarget(OtherRobot targetedRobot) {
		if (!getNameOfTarget().equals(targetedRobot.getName())) {
			System.out.println("TargetedRobot is not the same");
			throw new IllegalArgumentException();
		}
	}

	//Additional log by the targetingMethod when a virtual bullet hits another robot.
	abstract void additionalLogForVirtualCollisions(OtherRobot paramOtherRobot, long paramLong, Bullet paramBullet, boolean paramBoolean);

	//Additional log by the targetingMethod when a real bullet hits another robot.	
	abstract void additionalLogForRealCollisions(OtherRobot paramOtherRobot, long paramLong, Bullet paramBullet, boolean paramBoolean);


	
	public String getNameOfTarget() {
		return this.nameOfTarget;
	}

	public ArrayList<Bullet> getBullets() {
		return this.bullets;
	}

	public long getSuccessCount() {
		return this.successCount;
	}

	//Returns the name of the given method. It is suggested the name is placed at the top of the Targeting Method as a static final field.
	public abstract String getNameOfMethod();

	//Returns the assigned color of a given method. It is suggested the color is placed at the top of the Targeting Method as a static final field.
	public abstract Color getColor();
}
