package com.benthejunebug.robocode.twinDuel.Bobs.targeting;

import java.awt.geom.Point2D.Double;
import theBobs.Bullet;
import theBobs.OtherRobot;




public class HeadOnTargeting
  extends TargetingMethod
{
  private static final long serialVersionUID = 1L;
  private static final String METHOD_NAME = "Head On Targeting";
  
  public HeadOnTargeting(String nameOfTarget)
  {
    super(nameOfTarget);
  }
  
  public HeadOnTargeting(HeadOnTargeting method) {
    super(method);
  }
  


  void additionalMerge(TargetingMethod method) {}
  


  public Point2D.Double targetReturningLocation(OtherRobot observingRobot, OtherRobot targetedRobot, double power)
  {
    return new Point2D.Double(targetedRobot.getX(), targetedRobot.getY());
  }
  
  public String getNameOfMethod()
  {
    return "Head On Targeting";
  }
  
  void additionalLogForVirtualCollisions(OtherRobot targetedRobot, long currentTime, Bullet bullet, boolean hit) {}
  
  void additionalLogForRealCollisions(OtherRobot targetedRobot, long currentTime, Bullet bullet, boolean hit) {}
  
  void additionalDeleteUnsharedData() {}
}
