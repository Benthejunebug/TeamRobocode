package com.benthejunebug.robocode.twinDuel.Bobs.targeting;

import java.awt.geom.Point2D.Double;
import robocode.util.Utils;
import theBobs.Bullet;
import theBobs.OtherRobot;






public class GuessFactorTargeting
  extends TargetingMethod
{
  private static final long serialVersionUID = 1L;
  private static final String METHOD_NAME = "GuessFactor Targeting";
  private static final int NUMBER_OF_GUESSFACTORS = 31;
  private static final int VIRTUAL_GUESSFACTOR_INCREMENT = 1;
  private static final int REAL_GUESSFACTOR_INCREMENT = 3;
  private int[] stats;
  
  public GuessFactorTargeting(String nameOfTarget)
  {
    super(nameOfTarget);
    this.stats = new int[31];
  }
  
  public GuessFactorTargeting(GuessFactorTargeting method) {
    super(method);
    this.stats = new int[31];
    for (int i = 0; i < 31; i++) {
      this.stats[i] = method.getStats()[i];
    }
  }
  
  void additionalMerge(TargetingMethod method)
  {
    for (int i = 0; i < 31; i++) {
      if (this.stats[i] < ((GuessFactorTargeting)method).getStats()[i]) {
        this.stats[i] = ((GuessFactorTargeting)method).getStats()[i];
      }
    }
  }
  

  public Point2D.Double targetReturningLocation(OtherRobot observingRobot, OtherRobot targetedRobot, double power)
  {
    int bestGuessFactorIndex = 15;
    
    for (int i = 0; i < 31; i++) {
      if (this.stats[bestGuessFactorIndex] < this.stats[i]) {
        bestGuessFactorIndex = i;
      }
    }
    
    double guessFactor = (bestGuessFactorIndex - (this.stats.length - 1) / 2) / (
      (this.stats.length - 1) / 2);
    double angleOffset = targetedRobot.getLateralDirection(
      observingRobot.getX(), observingRobot.getY()) * 
      guessFactor * Bullet.getMaxEscapeAngle(power);
    
    double absRelativeAngle = targetedRobot.getAbsBearing(
      observingRobot.getX(), observingRobot.getY()) + 
      angleOffset;
    double robotDistance = targetedRobot.getDistance(observingRobot.getX(), 
      observingRobot.getY());
    
    return new Point2D.Double(observingRobot.getX() + robotDistance * 
      Math.sin(absRelativeAngle), observingRobot.getY() + 
      robotDistance * Math.cos(absRelativeAngle));
  }
  

  private void log(OtherRobot targetedRobot, long currentTime, Bullet bullet, int increment)
  {
    double desiredAngle = Math.atan2(
      targetedRobot.getX() - bullet.getSourceX(), 
      targetedRobot.getY() - bullet.getSourceY());
    double angleOffset = 
      Utils.normalRelativeAngle(desiredAngle - 
      bullet.getAbsBearingOfTarget());
    double guessFactor = Math.max(
      -1.0D, 
      Math.min(
      1.0D, 
      angleOffset / 
      Bullet.getMaxEscapeAngle(bullet.getPower()))) * 
      bullet.getLateralDirectionOfTarget();
    int index = 
      (int)Math.round((this.stats.length - 1) / 2 * (guessFactor + 1.0D));
    
    this.stats[index] += increment;
  }
  
  public int[] getStats()
  {
    return this.stats;
  }
  

  void additionalLogForVirtualCollisions(OtherRobot targetedRobot, long currentTime, Bullet bullet, boolean hit)
  {
    log(targetedRobot, currentTime, bullet, 1);
  }
  

  void additionalLogForRealCollisions(OtherRobot targetedRobot, long currentTime, Bullet bullet, boolean hit)
  {
    log(targetedRobot, currentTime, bullet, 3);
  }
  
  public String getNameOfMethod()
  {
    return "GuessFactor Targeting";
  }
  
  void additionalDeleteUnsharedData() {}
}
