package com.benthejunebug.robocode.twinDuel.Bobs.targeting;

import java.awt.Color;
import java.awt.geom.Point2D;

import com.benthejunebug.robocode.twinDuel.Bobs.Bullet;
import com.benthejunebug.robocode.twinDuel.Bobs.OtherRobot;




public class GuessFactorTargeting extends TargetingMethod {

	private static final long serialVersionUID = 1L;

	private static final String METHOD_NAME = "GuessFactor Targeting";
	private static final Color COLOR = Color.BLUE;

	
	//GuessFactor constants
	private static final int NUMBER_OF_GUESSFACTORS = 31;
	private static final int VIRTUAL_GUESSFACTOR_INCREMENT = 1;
	private static final int REAL_GUESSFACTOR_INCREMENT = 3;
	
	private int[] stats;

	//Main constructor
	public GuessFactorTargeting(String nameOfTarget) {
		super(nameOfTarget);
		this.stats = new int[NUMBER_OF_GUESSFACTORS];
	}

	//Copy constructor
	public GuessFactorTargeting(GuessFactorTargeting method) {
		super(method);
		this.stats = new int[NUMBER_OF_GUESSFACTORS];
		
		for (int i = 0; i < NUMBER_OF_GUESSFACTORS; i++) {
			this.stats[i] = method.getStats()[i];
		}
		
	}

	public TargetingMethod copy(){
		return new GuessFactorTargeting(this);
	}
	
	@Override //Merges stats
	protected void additionalMerge(TargetingMethod method) {
		for (int i = 0; i < NUMBER_OF_GUESSFACTORS; i++) {

			if (this.stats[i] < ((GuessFactorTargeting)method).getStats()[i]) {
				this.stats[i] = ((GuessFactorTargeting)method).getStats()[i];
			}
			
		}
	}


	public Point2D.Double targetReturningLocation(OtherRobot observingRobot, OtherRobot targetedRobot, double power) {
		int bestGuessFactorIndex = 15;

		for (int i = 0; i < NUMBER_OF_GUESSFACTORS; i++) {
			if (this.stats[bestGuessFactorIndex] < this.stats[i]) {
				bestGuessFactorIndex = i;
			}
		}

		double guessFactor = (bestGuessFactorIndex - (this.stats.length - 1) / 2) / ((this.stats.length - 1) / 2);
		double angleOffset = targetedRobot.getLateralDirection(observingRobot.getX(), observingRobot.getY()) * guessFactor * Bullet.getMaxEscapeAngle(power);
		double absRelativeAngle = targetedRobot.getAbsBearing(observingRobot.getX(), observingRobot.getY()) + angleOffset;
		double robotDistance = targetedRobot.getDistance(observingRobot.getX(), observingRobot.getY());

		return new Point2D.Double(observingRobot.getX() + robotDistance * Math.sin(absRelativeAngle), observingRobot.getY() + robotDistance * Math.cos(absRelativeAngle));
	}


	//Logs each hit and updates the stats
	private void log(OtherRobot targetedRobot, long currentTime, Bullet bullet, int increment) {
		double desiredAngle = Math.atan2(targetedRobot.getX() - bullet.getSourceX(), targetedRobot.getY() - bullet.getSourceY());
		double angleOffset = robocode.util.Utils.normalRelativeAngle(desiredAngle - bullet.getAbsBearingOfTarget());
		double guessFactor = Math.max(-1, Math.min(1, angleOffset / Bullet.getMaxEscapeAngle(bullet.getPower()))) * bullet.getLateralDirectionOfTarget();
		int index = (int)Math.round((this.stats.length - 1) / 2 * (guessFactor + 1));

		this.stats[index] += increment;
	}

	protected void additionalLogForVirtualCollisions(OtherRobot targetedRobot, long currentTime, Bullet bullet, boolean hit) {
		log(targetedRobot, currentTime, bullet, VIRTUAL_GUESSFACTOR_INCREMENT);
	}


	protected void additionalLogForRealCollisions(OtherRobot targetedRobot, long currentTime, Bullet bullet, boolean hit) {
		log(targetedRobot, currentTime, bullet, REAL_GUESSFACTOR_INCREMENT);
	}

	public String getNameOfMethod() {
		return METHOD_NAME;
	}

	public Color getColor() {
		return COLOR;
	}
	
	public int[] getStats() {
		return this.stats;
	}

}
