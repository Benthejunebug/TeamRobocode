package com.benthejunebug.robocode.twinDuel.Bobs;

import java.io.Serializable;
import java.util.ArrayList;














public class Message
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  private OtherRobot sendingRobot;
  private ArrayList<OtherRobot> enemyRobots;
  private long timeCreated;
  
  public Message(ArrayList<OtherRobot> enemyRobots, OtherRobot sendingRobot, long timeCreated)
  {
    this.enemyRobots = new ArrayList();
    for (OtherRobot robot : enemyRobots) {
      this.enemyRobots.add(new OtherRobot(robot));
    }
    this.sendingRobot = new OtherRobot(sendingRobot);
    this.timeCreated = timeCreated;
    deleteUnsharedData();
  }
  
  private void deleteUnsharedData() {
    for (int i = 0; i < this.enemyRobots.size(); i++) {
      ((OtherRobot)this.enemyRobots.get(i)).deleteUnsharedData();
    }
  }
  
  public String toString() {
    return null;
  }
  
  public ArrayList<OtherRobot> getEnemyRobotData() {
    return this.enemyRobots;
  }
  
  public OtherRobot getSenderRobotData() {
    return this.sendingRobot;
  }
  
  public long getTimeCreated() {
    return this.timeCreated;
  }
}
