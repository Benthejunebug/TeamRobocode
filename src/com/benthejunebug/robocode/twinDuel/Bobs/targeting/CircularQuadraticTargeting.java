package com.benthejunebug.robocode.twinDuel.Bobs.targeting;

import java.awt.geom.Point2D;

import com.benthejunebug.robocode.twinDuel.Bobs.Bob;
import com.benthejunebug.robocode.twinDuel.Bobs.Bullet;
import com.benthejunebug.robocode.twinDuel.Bobs.OtherRobot;
import com.benthejunebug.robocode.twinDuel.Bobs.Utils;






public class CircularQuadraticTargeting extends TargetingMethod {
	
  private static final long serialVersionUID = 1L;
  
  private static final String METHOD_NAME = "Circular Quadratic Targeting";
  private static final int ADDITIONAL_TICKS = 0;
  
  public CircularQuadraticTargeting(String nameOfTarget)
  {
    super(nameOfTarget);
  }
  
  public CircularQuadraticTargeting(CircularQuadraticTargeting method) {
    super(method);
  }
  


  public Point2D.Double targetReturningLocation(OtherRobot observingRobot, OtherRobot targetedRobot, double power) {
    Point2D.Double predictedLocation = new Point2D.Double(
      targetedRobot.getX(), targetedRobot.getY());
    int elapsedTime = 0;
    
    double bulletVelocity = Bullet.getBulletVelocity(power);
    
    double robotVelocity = targetedRobot.getHeading();
    double robotHeading = targetedRobot.getHeading();
    double robotAngularVelocity = targetedRobot.getAnglularVelociy();
    
    while (elapsedTime * bulletVelocity < Utils.getResultant(
      observingRobot.getX(), predictedLocation.x, 
      observingRobot.getX(), predictedLocation.y)) {
      robotVelocity += targetedRobot.getAcceleration();
      if (robotVelocity > 8.0D) {
        robotVelocity = 8.0D;
      }
      
      robotAngularVelocity += targetedRobot.getAngluarAcceleration();
      
      if (robotAngularVelocity > targetedRobot.getMaxAngularVelocity(robotVelocity)) {
        robotAngularVelocity = 
          targetedRobot.getMaxAngularVelocity(robotVelocity);
      }
      
      robotHeading += robotAngularVelocity;
      predictedLocation = Utils.projectPosition(predictedLocation, 
        robotVelocity, robotHeading);
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
  


  void additionalMerge(TargetingMethod method) {}
  


  public String getNameOfMethod() {
    return METHOD_NAME;
  }
  
  void additionalLogForVirtualCollisions(OtherRobot targetedRobot, long currentTime, Bullet bullet, boolean hit) {}
  
  void additionalLogForRealCollisions(OtherRobot targetedRobot, long currentTime, Bullet bullet, boolean hit) {}
  
  void additionalDeleteUnsharedData() {}
}
