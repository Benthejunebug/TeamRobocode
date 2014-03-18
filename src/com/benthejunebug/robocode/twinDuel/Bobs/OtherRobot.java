package com.benthejunebug.robocode.twinDuel.Bobs;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;

import com.benthejunebug.robocode.twinDuel.Bobs.Bullet;
import com.benthejunebug.robocode.twinDuel.Bobs.Utils;
import com.benthejunebug.robocode.twinDuel.Bobs.targeting.CircularLinearTargeting;
import com.benthejunebug.robocode.twinDuel.Bobs.targeting.CircularQuadraticTargeting;
import com.benthejunebug.robocode.twinDuel.Bobs.targeting.GuessFactorTargeting;
import com.benthejunebug.robocode.twinDuel.Bobs.targeting.HeadOnTargeting;
import com.benthejunebug.robocode.twinDuel.Bobs.targeting.LinearTargeting;
import com.benthejunebug.robocode.twinDuel.Bobs.targeting.QuadraticTargeting;
import com.benthejunebug.robocode.twinDuel.Bobs.targeting.TargetingMethod;



















































public class OtherRobot implements Serializable {
	
	
  private static final long serialVersionUID = 1L;
  
  
  public static final int NUM_OF_RECORDS = 50;
  public static final int MAX_VELOCITY = 8;
  public static final double MAX_ANGULAR_GUN_VELOCITY = 0.349065850398866D;
  public static final double MAX_ANGULAR_RADAR_VELOCITY = 0.7853981633974483D;
  
  
  
  private String name;
  private boolean isAlive;
  private double width;
  private double[] Xs;
  private double[] Ys;
  private long[] recordedTicks;
  private long lastTick;
  private long lastTickOfVirtualTargetingUpdate;
  private long lastTickOfBulletsUpdate;
  private double heading;
  private double velocity;
  private double acceleration;
  private double angularVelocity;
  private double angularAcceleration;
  private double energy;
  private double lastEnergy;
  private long[] previousFireTicks;
  private long lastFireTick;
  private long avgDeltaFireTick;
  private static final int BINS_FOR_WAVE_SURFING = 47;
  private double[] surfingStats;
  private ArrayList<Bullet> bullets;
  private ArrayList<TargetingMethod> targetingMethods;
  public static final int HEAD_ON_TARGETING = 0;
  public static final int LINEAR_TARGETING = 1;
  public static final int QUADRATIC_TARGETING = 2;
  public static final int CIRCULAR_LINEAR_TARGETING = 3;
  public static final int CIRCULAR_QUADRATIC_TARGETING = 4;
  public static final int GUESSFACTOR_TARGETING = 5;
  private double gunHeading;
  private double radarHeading;
  
  
  
  public OtherRobot(String name, double width, long time)
  {
    this.name = name;
    this.width = width;
    this.isAlive = true;
    this.Xs = new double[50];
    this.Ys = new double[50];
    this.recordedTicks = new long[50];
    this.previousFireTicks = new long[50];
    
    this.targetingMethods = new ArrayList();
    this.targetingMethods.add(new HeadOnTargeting(getName()));
    this.targetingMethods.add(new LinearTargeting(getName()));
    this.targetingMethods.add(new QuadraticTargeting(getName()));
    this.targetingMethods.add(new CircularLinearTargeting(getName()));
    this.targetingMethods.add(new CircularQuadraticTargeting(getName()));
    this.targetingMethods.add(new GuessFactorTargeting(getName()));
    this.lastTickOfVirtualTargetingUpdate = 0L;
    
    this.surfingStats = new double[47];
    this.bullets = new ArrayList();
    this.lastTickOfBulletsUpdate = 0L;
    
    setLastTick(time);
  }
  
  
  
  public void newRound()
  {
    this.isAlive = true;
    setLastTick(0L);
    this.lastTickOfVirtualTargetingUpdate = 0L;
    this.lastTickOfBulletsUpdate = 0L;
    for (TargetingMethod method : this.targetingMethods) {
      method.newRound();
    }
  }
  
  
  
  public OtherRobot(OtherRobot robot)
  {
    this.name = robot.getName();
    this.isAlive = robot.isAlive();
    this.width = robot.getWidth();
    this.Xs = new double[50];
    this.Ys = new double[50];
    this.recordedTicks = new long[50];
    this.previousFireTicks = new long[50];
    
    for (int i = 0; i < 50; i++) {
      this.Xs[i] = robot.getXs()[i];
      this.Ys[i] = robot.getYs()[i];
      this.recordedTicks[i] = robot.getRecordedTicks()[i];
      this.previousFireTicks[i] = robot.getPreviousFireTicks()[i];
    }
    
    this.lastTick = robot.getLastTick();
    this.lastTickOfVirtualTargetingUpdate = robot.getLastTickOfVirtualTargetingUpdate();
    this.lastTickOfBulletsUpdate = robot.getLastTickOfBulletsUpdate();
    this.heading = robot.getHeading();
    this.velocity = robot.getVelocity();
    this.acceleration = robot.getAcceleration();
    this.angularVelocity = robot.getAnglularVelociy();
    this.angularAcceleration = robot.getAngluarAcceleration();
    this.energy = robot.getEnergy();
    this.lastEnergy = robot.getLastEnergy();
    this.lastFireTick = robot.getLastFireTick();
    this.avgDeltaFireTick = robot.getAvgDeltaFireTick();
    
    this.surfingStats = new double[47];
    for (int i = 0; i < 47; i++) {
      this.surfingStats[i] = robot.getSurfingStats()[i];
    }
    
    this.bullets = new ArrayList();
    for (Bullet bullet : robot.getBullets()) {
      this.bullets.add(new Bullet(bullet));
    }
    
    this.targetingMethods = new ArrayList();
    for (int i = 0; i < robot.getTargetingMethods().size(); i++) {
      TargetingMethod method = (TargetingMethod)robot.getTargetingMethods().get(i);
      switch (i) {
      case 0: 
        this.targetingMethods.add(new HeadOnTargeting((HeadOnTargeting)method));
        break;
      case 1: 
        this.targetingMethods.add(new LinearTargeting((LinearTargeting)method));
        break;
      case 2: 
        this.targetingMethods.add(new QuadraticTargeting((QuadraticTargeting)method));
        break;
      case 3: 
        this.targetingMethods.add(new CircularLinearTargeting((CircularLinearTargeting)method));
        break;
      case 4: 
        this.targetingMethods.add(new CircularQuadraticTargeting((CircularQuadraticTargeting)method));
        break;
      case 5: 
        this.targetingMethods.add(new GuessFactorTargeting((GuessFactorTargeting)method));
      }
      
    }
  }
  
  public void merge(OtherRobot robot)
  {
    if (!this.name.equals(robot.getName())) {
      System.out.println("robots are not the same!");
      throw new IllegalArgumentException();
    }
    
    if (robot.getLastTick() > getLastTick()) {
      this.heading = robot.getHeading();
      this.velocity = robot.getVelocity();
      this.acceleration = robot.getAcceleration();
      this.angularVelocity = robot.getAnglularVelociy();
      this.angularAcceleration = robot.getAngluarAcceleration();
      this.isAlive = robot.isAlive();
      setEnergy(robot.getEnergy());
      if (robot.getRecordedTicks()[1] > getRecordedTicks()[1]) {
        this.lastEnergy = robot.getLastEnergy();
      }
    }
    
    if (robot.getLastTickOfVirtualTargetingUpdate() > getLastTickOfVirtualTargetingUpdate()) {
      for (int i = 0; i < getTargetingMethods().size(); i++) {
        ((TargetingMethod)this.targetingMethods.get(i)).merge((TargetingMethod)robot.getTargetingMethods().get(i));
      }
      this.lastTickOfVirtualTargetingUpdate = robot.getLastTickOfVirtualTargetingUpdate();
    }
    
    for (int i = 0; i < 47; i++) {
      if (this.surfingStats[i] < robot.getSurfingStats()[i]) {
        this.surfingStats[i] = robot.getSurfingStats()[i];
      }
    }
    




















    for (int i = 0; i < 50; i++) {
      if (robot.getRecordedTicks()[i] >= getRecordedTicks()[i]) {
        this.recordedTicks = Utils.insertToArray(robot.getRecordedTicks()[i], this.recordedTicks, i);
        this.Xs = Utils.insertToArray(robot.getXs()[i], getXs(), i);
        this.Ys = Utils.insertToArray(robot.getYs()[i], getYs(), i);
        this.previousFireTicks = Utils.insertToArray(robot.getPreviousFireTicks()[i], getPreviousFireTicks(), i);
      }
    }
  }
  

  public void deleteUnsharedData()
  {
    this.bullets = null;
    for (TargetingMethod method : getTargetingMethods()) {
      method.deleteUnsharedData();
    }
  }
  











  public void updateProperties(double x, double y, double heading, double velocity, double energy, long lastTick)
  {
    setHeading(heading);
    setVelocity(velocity);
    setEnergy(energy);
    setLastTick(lastTick);
    setX(x);
    setY(y);
  }
  
  public void updateScanedProperties(OtherRobot observingRobot, double heading, double distance, double bearing, double velocity, double energy, long currentTime)
  {
    double absBearing = calculateAbsBearing(bearing, observingRobot.getHeading());
    updateProperties(observingRobot.getX() + distance * Math.sin(absBearing), observingRobot.getY() + distance * Math.cos(absBearing), heading, velocity, energy, currentTime);
    updateWaves(observingRobot, currentTime);
  }
  
  public void updatePropertiesFromSelf(double x, double y, double heading, double gunHeading, double radarHeading, double velocity, double energy, long currentTime)
  {
    updateProperties(x, y, heading, velocity, energy, currentTime);
    this.gunHeading = gunHeading;
    this.radarHeading = radarHeading;
  }
  











  public void updateVirtualTargeting(OtherRobot observingRobot, double power, long currentTime)
  {
    this.lastTickOfVirtualTargetingUpdate = currentTime;
    
    for (int i = 0; i < getTargetingMethods().size(); i++)
    {
      ((TargetingMethod)getTargetingMethods().get(i)).checkForVirtualCollisions(this, currentTime);
    }
  }
  

  public void fireVirtualBullets(OtherRobot observingRobot, double power, long currentTime)
  {
    for (TargetingMethod method : getTargetingMethods()) {
      method.fireVirtual(observingRobot, this, power, currentTime);
    }
  }
  
  public TargetingMethod getBestTargetingMethod() {
    TargetingMethod bestMethod = (TargetingMethod)getTargetingMethods().get(0);
    
    for (TargetingMethod method : getTargetingMethods()) {
      if (method.getSucessCount() > bestMethod.getSucessCount()) {
        bestMethod = method;
      }
    }
    
    return bestMethod;
  }
  










  public void updateWaves(OtherRobot observingRobot, long currentTime)
  {
    this.lastTickOfBulletsUpdate = currentTime;
    
    double power = getLastEnergy() - getEnergy();
    
    if ((power < 3.01D) && (power > 0.09D))
    {
      this.bullets.add(new Bullet(getName(), getX(), getY(), getLateralDirection(observingRobot.getX(), observingRobot.getY()), power, currentTime - 1L));
      setLastFireTick(currentTime - 1L);
      
      observingRobot.fireVirtualBullets(this, power, currentTime - 1L);
    }
    
    for (int i = 0; i < getBullets().size(); i++) {
      if (((Bullet)this.bullets.get(i)).getDistanceTraveled(currentTime) > observingRobot.getDistance(((Bullet)this.bullets.get(i)).getSourceX(), ((Bullet)this.bullets.get(i)).getSourceY()) + Math.sqrt(2.0D) * observingRobot.getWidth()) {
        this.bullets.remove(i);
      }
    }
  }
  













  public void logBulletHit(OtherRobot observingHitRobot, long timeOfHit)
  {
    for (int i = 0; i < this.bullets.size(); i++) {
      Point2D.Double bulletLocation = ((Bullet)this.bullets.get(i)).getLocation(timeOfHit);
      double distanceBetweenRobotAndBullet = observingHitRobot.getDistance(bulletLocation.getX(), bulletLocation.getY());
      

      if (distanceBetweenRobotAndBullet < Math.sqrt(2.0D) * observingHitRobot.getWidth() / 2.0D) {
        double factorIndex = getFactorIndex((Bullet)this.bullets.get(i), observingHitRobot, new Point2D.Double(observingHitRobot.getX(), observingHitRobot.getY()), timeOfHit);
        

        for (int j = 0; j < 47; j++) {
          this.surfingStats[j] += 1.0D / (Math.pow(factorIndex - j, 2.0D) + 1.0D);
        }
        










        this.bullets.remove(i);
        break;
      }
    }
  }
  
  public Bullet getClosestSurfableBullet(OtherRobot observingRobot, long currentTime)
  {
    Bullet closestBullet = new Bullet("FarBot", Bob.BATTLE_FIELD_WIDTH * Math.sqrt(2.0D) + 100.0D, Bob.BATTLE_FIELD_HEIGHT * Math.sqrt(2.0D) + 100.0D, 1, 3.0D, currentTime);
    
    for (int i = 0; i < this.bullets.size(); i++) {
      double distanceBetweenRobotAndCurrentClosestBullet = closestBullet.getDistance(observingRobot.getX(), observingRobot.getY(), currentTime);
      double distanceBetweenRobotAndBullet = ((Bullet)this.bullets.get(i)).getDistance(observingRobot.getX(), observingRobot.getY(), currentTime);
      
      if ((distanceBetweenRobotAndBullet < distanceBetweenRobotAndCurrentClosestBullet) && (distanceBetweenRobotAndBullet > ((Bullet)this.bullets.get(i)).getVelocity())) {
        closestBullet = (Bullet)this.bullets.get(i);
      }
    }
    
    return closestBullet;
  }
  


  private int getFactorIndex(Bullet bullet, OtherRobot targetedRobot, Point2D.Double targetedLocation, long timeOfHit)
  {
    double offsetAngle = Math.atan2(targetedLocation.getX() - bullet.getSourceX(), targetedLocation.getY() - 
      bullet.getSourceY()) - Math.atan2(targetedRobot.getXs()[((int)(timeOfHit - bullet.getTimeOfFire()))] - bullet.getSourceX(), targetedRobot.getYs()[((int)(timeOfHit - bullet.getTimeOfFire()))] - bullet.getSourceY());
    
    double factor = robocode.util.Utils.normalRelativeAngle(offsetAngle) / bullet.getMaxEscapeAngle() * bullet.getLateralDirectionOfTarget();
    
    return (int)Utils.limit(0.0D, factor * 23.0D + 23.0D, 46.0D);
  }
  




  public double checkDanger(OtherRobot observingRobot, Bullet bullet, int direction, long currentTime)
  {
    Point2D.Double predictedPosition = new Point2D.Double(observingRobot.getX(), observingRobot.getY());
    double predictedVelocity = observingRobot.getVelocity();
    double predictedHeading = observingRobot.getHeading();
    

    int counter = 0;
    boolean intercepted = false;
    do
    {
      double moveAngle = Utils.wallSmoothing(new Rectangle2D.Double(18.0D, 18.0D, Bob.BATTLE_FIELD_WIDTH, Bob.BATTLE_FIELD_HEIGHT), predictedPosition, Math.atan2(predictedPosition.getX() - bullet.getSourceX(), predictedPosition.getY() - bullet.getSourceY()) + direction * 1.570796326794897D, direction, 150) - 
        predictedHeading;
      double moveDir = 1.0D;
      
      if (Math.cos(moveAngle) < 0.0D) {
        moveAngle += 3.141592653589793D;
        moveDir = -1.0D;
      }
      
      moveAngle = robocode.util.Utils.normalRelativeAngle(moveAngle);
      

      double maxTurning = 0.004363323129985824D * (40.0D - 3.0D * Math.abs(predictedVelocity));
      predictedHeading = robocode.util.Utils.normalRelativeAngle(predictedHeading + 
        Utils.limit(-maxTurning, moveAngle, maxTurning));
      




      predictedVelocity = predictedVelocity + (predictedVelocity * moveDir < 0.0D ? 2.0D * moveDir : moveDir);
      predictedVelocity = Utils.limit(-8.0D, predictedVelocity, 8.0D);
      

      predictedPosition = Utils.projectPosition(predictedPosition, predictedHeading, predictedVelocity);
      
      counter++;
      

      if (predictedPosition.distance(bullet.getSourceX(), bullet.getSourceY()) < bullet.getDistanceTraveled(currentTime) + counter * bullet.getVelocity() + 
        bullet.getVelocity()) {
        intercepted = true;
      }
    } while ((!intercepted) && (counter < 500));
    
    int index = getFactorIndex(bullet, observingRobot, predictedPosition, currentTime);
    
    return this.surfingStats[index];
  }
  





















  public String getName()
  {
    return this.name;
  }
  
  public boolean isAlive() {
    return this.isAlive;
  }
  
  public void dies() {
    this.isAlive = false;
  }
  
  public double getWidth() {
    return this.width;
  }
  
  public double getX() {
    return this.Xs[0];
  }
  
  public double getY() {
    return this.Ys[0];
  }
  
  public void setX(double x) {
    this.Xs = Utils.insertToArray(x, this.Xs, 0);
  }
  
  public void setY(double y) {
    this.Ys = Utils.insertToArray(y, this.Ys, 0);
  }
  
  public double[] getXs() {
    return this.Xs;
  }
  
  public double[] getYs() {
    return this.Ys;
  }
  
  public Point2D.Double predictPostion(int ticks)
  {
    return Utils.projectPosition(getX(), getY(), getVelocity() * ticks, getHeading());
  }
  
  public Point2D.Double predictPositionFromBulletVelocity(double observingX, double observingY, double bulletVelocity, int additionalTicks)
  {
    Point2D.Double predictedLocation = new Point2D.Double(getX(), getY());
    int elapsedTime = -1 * additionalTicks;
    
    while (elapsedTime * bulletVelocity < Utils.getResultant(observingX, 
      predictedLocation.x, observingY, predictedLocation.y)) {
      predictedLocation = Utils.projectPosition(predictedLocation, 
        getVelocity(), getHeading());
      elapsedTime++;
    }
    
    return predictedLocation;
  }
  
  public long getLastTick() {
    return this.lastTick;
  }
  
  public void setLastTick(long lastTick) {
    Utils.insertToArray(lastTick, this.recordedTicks, 0);
    this.lastTick = lastTick;
  }
  
  public long getLastTickOfVirtualTargetingUpdate() {
    return this.lastTickOfVirtualTargetingUpdate;
  }
  
  public long getLastTickOfBulletsUpdate() {
    return this.lastTickOfBulletsUpdate;
  }
  
  public long[] getRecordedTicks() {
    return this.recordedTicks;
  }
  
  public void setHeading(double heading) {
    setAngularVelocity((heading - this.heading) / 2.0D);
    this.heading = heading;
  }
  
  public double getHeading() {
    return this.heading;
  }
  
  public double getGunHeading() {
    return this.gunHeading;
  }
  
  public double getRadarHeading() {
    return this.radarHeading;
  }
  
  public double calculateAbsBearing(double bearing, double headingOfObserver) {
    return robocode.util.Utils.normalRelativeAngle(bearing + headingOfObserver);
  }
  
  public double getAbsBearing(double observingX, double observingY) {
    return Math.atan2(observingX - getX(), observingY - getY()) + 3.141592653589793D;
  }
  
  public double getBearing(double observingX, double observingY, double observingHeading) {
    return robocode.util.Utils.normalRelativeAngle(getAbsBearing(observingX, observingY) - observingHeading);
  }
  
  public double getDistance(double observingX, double observingY) {
    return Utils.getResultant(observingX, getX(), observingY, 
      getY());
  }
  
  private void setVelocity(double velocity) {
    double acceleration = this.acceleration;
    this.acceleration = ((velocity - this.velocity) / 2.0D);
    
    this.velocity = velocity;
  }
  
  public double getVelocity() {
    return this.velocity;
  }
  
  public double getAcceleration() {
    return this.acceleration;
  }
  
  public void setAngularVelocity(double angularVelocity) {
    setAngularAcceleration((angularVelocity - this.angularVelocity) / 2.0D);
    this.angularVelocity = angularVelocity;
  }
  
  public double getAnglularVelociy() {
    return this.angularVelocity;
  }
  
  public double getMaxAngularVelocity(double velocity) {
    return (10.0D - 0.75D * Math.abs(velocity)) * 0.0174532925199433D;
  }
  
  public double getMaxAngularVelocity() {
    return getMaxAngularVelocity(this.velocity);
  }
  
  public void setAngularAcceleration(double angularAcceleration) {
    this.angularAcceleration = angularAcceleration;
  }
  
  public double getAngluarAcceleration() {
    return this.angularAcceleration;
  }
  
  public double getLateralVelocity(double observingX, double observingY) {
    return this.velocity * Math.sin(getHeading() - getAbsBearing(observingX, observingY));
  }
  

  public int getLateralDirection(double observingX, double observingY)
  {
    if (getLateralVelocity(observingX, observingY) < 0.0D) {
      return -1;
    }
    return 1;
  }
  
  public double getEnergy()
  {
    return this.energy;
  }
  
  public void setEnergy(double energy) {
    this.lastEnergy = this.energy;
    this.energy = energy;
  }
  
  public double getLastEnergy() {
    return this.lastEnergy;
  }
  
  public long getLastFireTick() {
    return this.lastFireTick;
  }
  
  public void setLastFireTick(long lastFireTick) {
    this.previousFireTicks = Utils.insertToArray(lastFireTick, 
      this.previousFireTicks, 0);
    this.avgDeltaFireTick = 0L;
    
    for (int i = 0; i < this.previousFireTicks.length; i++) {
      this.avgDeltaFireTick += this.previousFireTicks[i];
    }
    
    this.avgDeltaFireTick /= this.previousFireTicks.length;
    this.lastFireTick = lastFireTick;
  }
  
  public long[] getPreviousFireTicks() {
    return this.previousFireTicks;
  }
  
  public long getAvgDeltaFireTick() {
    return this.avgDeltaFireTick;
  }
  
  public double[] getSurfingStats() {
    return this.surfingStats;
  }
  
  public ArrayList<Bullet> getBullets() {
    return this.bullets;
  }
  
  public ArrayList<TargetingMethod> getTargetingMethods() {
    return this.targetingMethods;
  }
}
