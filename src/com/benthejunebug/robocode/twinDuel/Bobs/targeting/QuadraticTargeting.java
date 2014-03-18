package com.benthejunebug.robocode.twinDuel.Bobs.targeting;

import java.awt.geom.Point2D.Double;
import theBobs.Bob;
import theBobs.Bullet;
import theBobs.OtherRobot;
import theBobs.Utils;





public class QuadraticTargeting
  extends TargetingMethod
{
  private static final long serialVersionUID = 1L;
  private static final String METHOD_NAME = "Quadratic Targeting";
  private static final int ADDITIONAL_TICKS = 0;
  
  public QuadraticTargeting(String nameOfTarget)
  {
    super(nameOfTarget);
  }
  
  public QuadraticTargeting(QuadraticTargeting method) {
    super(method);
  }
  



  void additionalMerge(TargetingMethod method) {}
  


  public Point2D.Double targetReturningLocation(OtherRobot observingRobot, OtherRobot targetedRobot, double power)
  {
    Point2D.Double predictedLocation = new Point2D.Double(
      targetedRobot.getX(), targetedRobot.getY());
    int elapsedTime = 0;
    
    double bulletVelocity = Bullet.getBulletVelocity(power);
    
    double robotVelocity = targetedRobot.getVelocity();
    
    while (elapsedTime * bulletVelocity < Utils.getResultant(
      observingRobot.getX(), predictedLocation.x, 
      observingRobot.getX(), predictedLocation.y)) {
      robotVelocity += targetedRobot.getAcceleration();
      if (robotVelocity > 8.0D) {
        robotVelocity = 8.0D;
      }
      
      predictedLocation = Utils.projectPosition(predictedLocation, 
        robotVelocity, targetedRobot.getHeading());
      elapsedTime++;
      if ((predictedLocation.x < 18.0D) || 
        (predictedLocation.y < 18.0D) || 
        
        (predictedLocation.x > Bob.BATTLE_FIELD_WIDTH - 18.0D) || 
        
        (predictedLocation.y > Bob.BATTLE_FIELD_HEIGHT - 18.0D)) {
        predictedLocation.x = Math.min(
          Math.max(18.0D, predictedLocation.x), 
          Bob.BATTLE_FIELD_WIDTH - 18.0D);
        predictedLocation.y = Math.min(
          Math.max(18.0D, predictedLocation.y), 
          Bob.BATTLE_FIELD_HEIGHT - 18.0D);
        break;
      }
    }
    
    return predictedLocation;
  }
  
  public String getNameOfMethod()
  {
    return "Quadratic Targeting";
  }
  
  void additionalLogForVirtualCollisions(OtherRobot targetedRobot, long currentTime, Bullet bullet, boolean hit) {}
  
  void additionalLogForRealCollisions(OtherRobot targetedRobot, long currentTime, Bullet bullet, boolean hit) {}
  
  void additionalDeleteUnsharedData() {}
}
