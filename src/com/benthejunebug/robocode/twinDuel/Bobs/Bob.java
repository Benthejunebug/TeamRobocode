package com.benthejunebug.robocode.twinDuel.Bobs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.benthejunebug.robocode.twinDuel.Bobs.targeting.TargetingMethod;

import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.MessageEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;















public class Bob
extends TeamRobot
{
	private static final double RADAR_LOCK_CONSTANT = 1.9D;
	private static final int SPIN_RADAR_INTERVAL = 150;
	private static final int LOST_ROBOT_INTERVAL = 16;
	private static final double RADAR_SKIP_ANGLE = 0.008726646259971648D;
	private static final double EXPONENTIAL_BASE_FOR_BULLET_POWER_EVAL = 1.2D;
	private static final double DISTANCE_TO_USE_MAX_POWER = 200.0D;
	private static final double FIREING_INTERVAL = 10.0D;
	private static boolean init = false;

	private long lastScannedTick = 0L;

	public static double ROBOT_WIDTH;

	public static double BATTLE_FIELD_WIDTH;

	public static double BATTLE_FIELD_HEIGHT;

	public static final double WALL_WIDTH = 18.0D;

	private static final double WALL_STICK = 300.0D;

	private static final double WALL_SMOOTHING_CONST = 0.05D;

	private static final int NUMBER_OF_RANDOM_POINT_SOURCES = 2;

	private static final int RANDOM_POINT_SOURCE_GENERATION_INTERVAL = 50;
	private static final double POINT_SOURCE_DEFAULT_CHARGE = 0.001D;
	private static Random rnd;
	private static ArrayList<OtherRobot> enemyRobots;
	private static OtherRobot thisRobot;
	private static OtherRobot partnerRobot;

	public void run()
	{
		if (!init) {
			init();
			init = true;
		} else {
			newRound();
		}

		setAdjustRadarForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		for (;;)
		{
			updateSelfOtherRobotData();
			scan();
			transmitData();
			rotateRadar();

			if ((enemyRobots.size() != 0) && (partnerRobot != null)) {
				electricFieldMovement();
				updateTargeting();
				paintRobot();
				fire();
			}
		}
	}













	private void init()
	{
		ROBOT_WIDTH = getWidth();
		BATTLE_FIELD_WIDTH = getBattleFieldWidth();
		BATTLE_FIELD_HEIGHT = getBattleFieldHeight();


		rnd = new Random();
		enemyRobots = new ArrayList();
		thisRobot = new OtherRobot(getName(), ROBOT_WIDTH, getTime());
		partnerRobot = null;

		System.out.println("looks like you have a meeting with the Bobs.");
	}

	private void newRound()
	{
		System.out.println(enemyRobots.size());
		for (int i = 0; i < enemyRobots.size(); i++) {
			((OtherRobot)enemyRobots.get(i)).newRound();
		}
		partnerRobot.newRound();
		thisRobot.newRound();
	}














	public void onScannedRobot(ScannedRobotEvent e)
	{
		if (isTeammate(e.getName())) {
			if (partnerRobot == null) {
				partnerRobot = new OtherRobot(e.getName(), ROBOT_WIDTH, e.getTime());
			}
			return;
		}

		this.lastScannedTick = getTime();

		Integer enemyIndex = null;
		for (int i = 0; i < enemyRobots.size(); i++) {
			if (((OtherRobot)enemyRobots.get(i)).getName().equals(e.getName())) {
				enemyIndex = Integer.valueOf(i);
				break;
			}
		}

		if (enemyIndex == null) {
			enemyIndex = Integer.valueOf(enemyRobots.size());
			enemyRobots.add(new OtherRobot(e.getName(), ROBOT_WIDTH, e.getTime()));
		}


		updateSelfOtherRobotData();

		((OtherRobot)enemyRobots.get(enemyIndex.intValue())).updateScanedProperties(thisRobot, e.getHeadingRadians(), e.getDistance(), e.getBearingRadians(), e.getVelocity(), e.getEnergy(), e.getTime());
	}

	private void updateSelfOtherRobotData() {
		thisRobot.updatePropertiesFromSelf(getX(), getY(), getHeadingRadians(), getGunHeadingRadians(), getRadarHeadingRadians(), getVelocity(), getEnergy(), getTime());
	}



























	private OtherRobot getClosestAssignedBot()
	{
		OtherRobot closestAssignedBot = new OtherRobot("FarBot", ROBOT_WIDTH, getTime());
		closestAssignedBot.setX(BATTLE_FIELD_WIDTH * Math.sqrt(2.0D) + 100.0D);
		closestAssignedBot.setY(BATTLE_FIELD_HEIGHT * Math.sqrt(2.0D) + 100.0D);
		OtherRobot closestBotToPartner = closestAssignedBot;

		if (getNumberOfAliveBots() == 1) {
			return getRemainingAliveBot();
		}

		if (partnerRobot.isAlive())
		{
			for (int i = 0; i < enemyRobots.size(); i++) {
				double distanceToEnemyBotFromPartner = ((OtherRobot)enemyRobots.get(i)).getDistance(partnerRobot.getX(), partnerRobot.getY());

				if (distanceToEnemyBotFromPartner < closestBotToPartner.getDistance(partnerRobot.getX(), partnerRobot.getY())) {
					closestBotToPartner = (OtherRobot)enemyRobots.get(i);
				}
			}
		}



		for (int i = 0; i < enemyRobots.size(); i++) {
			double distanceToEnemyBot = ((OtherRobot)enemyRobots.get(i)).getDistance(thisRobot.getX(), thisRobot.getY());

			if (distanceToEnemyBot < closestAssignedBot.getDistance(thisRobot.getX(), thisRobot.getY())) {
				if (partnerRobot.isAlive()) {
					boolean sameBot = ((OtherRobot)enemyRobots.get(i)).getName().equals(closestBotToPartner.getName());

					if ((!sameBot) || ((sameBot) && (distanceToEnemyBot < closestBotToPartner.getDistance(partnerRobot.getX(), partnerRobot.getY())))) {
						closestAssignedBot = (OtherRobot)enemyRobots.get(i);
					}
				} else {
					closestAssignedBot = (OtherRobot)enemyRobots.get(i);
				}
			}
		}


		return closestAssignedBot;
	}

	private OtherRobot getBotToTrack()
	{
		OtherRobot closestAssignedBot = getClosestAssignedBot();
		OtherRobot botToTrack = null;

		if (getNumberOfAliveBots() == 1) {
			botToTrack = getRemainingAliveBot();
		}
		else {
			for (int i = 0; i < enemyRobots.size(); i++) {
				if (!((OtherRobot)enemyRobots.get(i)).getName().equals(closestAssignedBot.getName())) {
					botToTrack = (OtherRobot)enemyRobots.get(i);
				}
			}
		}

		return botToTrack;
	}


	private int getNumberOfAliveBots()
	{
		int numberOfAliveBots = 0;

		for (int i = 0; i < enemyRobots.size(); i++) {
			if (((OtherRobot)enemyRobots.get(i)).isAlive()) {
				numberOfAliveBots++;
			}
		}
		return numberOfAliveBots;
	}

	private OtherRobot getRemainingAliveBot()
	{
		for (int i = 0; i < enemyRobots.size(); i++) {
			if (((OtherRobot)enemyRobots.get(i)).isAlive()) {
				return (OtherRobot)enemyRobots.get(i);
			}
		}
		return null;
	}






































	private void lockRadar(OtherRobot robot)
	{
		setTurnRadarRightRadians(1.9D * robocode.util.Utils.normalRelativeAngle(robot.getAbsBearing(thisRobot.getX(), thisRobot.getY()) - getRadarHeadingRadians()));
	}

	private void antiLockRadar(OtherRobot robot)
	{
		setTurnRadarRightRadians(1.9D * robocode.util.Utils.normalRelativeAngle(robot.getAbsBearing(thisRobot.getX(), thisRobot.getY()) - getRadarHeadingRadians() + 3.141592653589793D));
	}

	private void spinRadar()
	{
		setTurnRadarRightRadians((1.0D / 0.0D));
	}

	private boolean getSpinning() {
		return (enemyRobots.size() == 0) || (getTime() - this.spinning <= 16L) || ((!partnerRobot.isAlive()) && (getNumberOfAliveBots() != 1));
	}

	private long spinning = 0L;
	double[] randXPointSources;
	double[] randYPointSources;

	private void rotateRadar() {
		if ((getTime() % 150L == 0L) || (getTime() - this.lastScannedTick >= 16L)) {
			this.spinning = getTime();
			System.out.println("I beg your pardon?");
			System.out.println("Eight Bosses");
			System.out.println("Eight?");
			System.out.println("Eight, Bob. So that means that when I make a mistake, I have eight different people coming by to tell me about it. That's my only real motivation is not to be hassled, that and the fear of losing my job. But you know, Bob, that will only make someone work just hard enough not to get fired.");
		}

		if (getSpinning()) {
			spinRadar();
		}
		else {
			lockRadar(getBotToTrack());
		}
	}


























	private void electricFieldMovement()
	{
		double horizontalForce = 0.0D;
		double verticalForce = 0.0D;


		for (int i = 0; i < enemyRobots.size(); i++) {
			if (((OtherRobot)enemyRobots.get(i)).isAlive()) {
				double distance = ((OtherRobot)enemyRobots.get(i)).getDistance(thisRobot.getX(), thisRobot.getY());

				double absBearing = ((OtherRobot)enemyRobots.get(i)).getAbsBearing(thisRobot.getX(), thisRobot.getY());









				horizontalForce -= Math.sin(absBearing) / Math.pow(distance, 2.0D);
				verticalForce -= Math.cos(absBearing) / Math.pow(distance, 2.0D);
			}
		}


		if (partnerRobot.isAlive()) {
			double distanceToPartner = partnerRobot.getDistance(thisRobot.getX(), thisRobot.getY());
			double absBearingToPartner = partnerRobot.getAbsBearing(thisRobot.getX(), thisRobot.getY());

			horizontalForce -= 2.0D * Math.sin(absBearingToPartner) / Math.pow(distanceToPartner, 2.0D);
			verticalForce -= 2.0D * Math.cos(absBearingToPartner) / Math.pow(distanceToPartner, 2.0D);
		}


		double distanceToCenter = thisRobot.getDistance(BATTLE_FIELD_WIDTH / 2.0D, BATTLE_FIELD_HEIGHT / 2.0D);
		double sine = (BATTLE_FIELD_WIDTH / 2.0D - thisRobot.getX()) / distanceToCenter;
		double cosine = (BATTLE_FIELD_HEIGHT / 2.0D - thisRobot.getY()) / distanceToCenter;

		horizontalForce -= sine * 0.001D / Math.pow(distanceToCenter, 2.0D);
		verticalForce -= cosine * 0.001D / Math.pow(distanceToCenter, 2.0D);

		if (this.randXPointSources == null) {
			this.randXPointSources = new double[2];
			this.randYPointSources = new double[2];
		}


		if (getTime() % 50L == 0L) {
			for (int i = 0; i < this.randXPointSources.length; i++) {
				this.randXPointSources[i] = (rnd.nextDouble() * (BATTLE_FIELD_WIDTH - 36.0D) + 18.0D);
				this.randYPointSources[i] = (rnd.nextDouble() * (BATTLE_FIELD_HEIGHT - 36.0D) + 18.0D);
			}
		}














		OtherRobot closestBot = getClosestAssignedBot();
		Bullet surfBullet = closestBot.getClosestSurfableBullet(thisRobot, getTime());


		double angleToSource = robocode.util.Utils.normalRelativeAngle(Math.atan2(thisRobot.getX() - surfBullet.getSourceX(), thisRobot.getY() - surfBullet.getSourceY()) + 1.570796326794897D);
		double lateralAngle = 0.0D;









		if (surfBullet.getLateralDirectionOfTarget() > 0) {
			lateralAngle = angleToSource + 1.570796326794897D;
		} else {
			lateralAngle = angleToSource - 1.570796326794897D;
		}


		horizontalForce += Math.sin(lateralAngle) / Math.pow(75.0D, 2.0D);
		verticalForce += Math.cos(lateralAngle) / Math.pow(75.0D, 2.0D);


		double angle = Math.atan2(horizontalForce, verticalForce);

		Rectangle2D.Double battleField = new Rectangle2D.Double(18.0D, 18.0D, BATTLE_FIELD_WIDTH - 18.0D, BATTLE_FIELD_HEIGHT - 18.0D);


		while (!battleField.contains(Utils.projectPosition(thisRobot.getX(), thisRobot.getY(), 300.0D, angle))) {
			angle += Utils.sign(Math.abs(robocode.util.Utils.normalRelativeAngle(angle - getHeadingRadians()))) * 0.05D;
		}

		if ((horizontalForce != 0.0D) || (verticalForce != 0.0D))
		{
			if (Math.abs(angle - getHeadingRadians()) < 1.570796326794897D) {
				setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(angle - getHeadingRadians()));
				setAhead((1.0D / 0.0D));
			} else {
				setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(angle + 3.141592653589793D - getHeadingRadians()));
				setBack((1.0D / 0.0D));
			}
		}
	}

	private void move()
	{
		Point2D.Double requestedLocation = evaluateMovements();

		double requestedHeading = Math.atan2(thisRobot.getY() - requestedLocation.y, thisRobot.getX() - requestedLocation.x);
		double angle = robocode.util.Utils.normalRelativeAngle(requestedHeading - getHeadingRadians());
		double distance = requestedLocation.distance(thisRobot.getX(), thisRobot.getY());

		if (Math.abs(angle) > 1.570796326794897D) {
			if (angle < 0.0D) {
				setTurnRightRadians(3.141592653589793D + angle);
			} else {
				setTurnLeftRadians(3.141592653589793D - angle);
			}
			setBack(distance);
		} else {
			if (angle < 0.0D) {
				setTurnLeftRadians(-1.0D * angle);
			} else {
				setTurnRightRadians(angle);
			}
			setAhead(distance);
		}
	}

	private Point2D.Double evaluateMovements()
	{
		Point2D.Double requestedLocation = null;
		return requestedLocation;
	}

	private Point2D.Double evaluateMovementPattern()
	{
		return new Point2D.Double(thisRobot.getX() + Math.sin(getHeadingRadians()) * 100.0D, thisRobot.getY() + Math.cos(getHeadingRadians()) * 100.0D);
	}

	private Point2D.Double wallSmoothing(Point2D.Double currentRequestedLocation)
	{
		Rectangle2D.Double battleField = new Rectangle2D.Double(18.0D, 18.0D, BATTLE_FIELD_WIDTH - 18.0D, BATTLE_FIELD_HEIGHT - 18.0D);

		double currentRequestedHeading = Math.atan2(thisRobot.getY() - currentRequestedLocation.y, thisRobot.getX() - currentRequestedLocation.x);
		double currentRequestedDistance = currentRequestedLocation.distance(thisRobot.getX(), thisRobot.getY());

		while (!battleField.contains(Utils.projectPosition(thisRobot.getX(), thisRobot.getY(), currentRequestedDistance + 300.0D, currentRequestedHeading))) {
			currentRequestedHeading += Utils.sign(Math.abs(robocode.util.Utils.normalRelativeAngle(currentRequestedHeading - getHeadingRadians())) - 1.570796326794897D) * 0.05D;
		}

		return new Point2D.Double(thisRobot.getX() + Math.sin(currentRequestedHeading) * currentRequestedDistance, thisRobot.getY() + Math.cos(currentRequestedHeading) * currentRequestedDistance);
	}

















	private double evaluateBulletPowerToUse(OtherRobot robot)
	{
		double bulletPower = 2.0D * Math.pow(1.2D, -1.0D * robot.getDistance(thisRobot.getX(), thisRobot.getY()) + 200.0D) + 1.0D;
		if (bulletPower > 3.0D) {
			bulletPower = 3.0D;
		}
		return bulletPower;
	}

	private void updateTargeting()
	{
		OtherRobot targetedRobot = getClosestAssignedBot();

		for (int i = 0; i < enemyRobots.size(); i++) {
			double power = evaluateBulletPowerToUse((OtherRobot)enemyRobots.get(i));
			((OtherRobot)enemyRobots.get(i)).updateVirtualTargeting(thisRobot, power, getTime());
		}

		double power = evaluateBulletPowerToUse(targetedRobot);

		if (getTime() % 2L == 0L)
		{
			if ((!partnerRobot.isAlive()) && (getBotToTrack().isAlive())) {
				getBotToTrack().fireVirtualBullets(thisRobot, power, getTime());
			}

			targetedRobot.fireVirtualBullets(thisRobot, power, getTime());
			setTurnGunRightRadians(targetedRobot.getBestTargetingMethod().targetReturningRelativeTurningAngle(thisRobot, targetedRobot, power));
		}
	}

	long lastFireTime = 0L;

	private void fire()
	{
		OtherRobot targetedRobot = getClosestAssignedBot();


		if ((getGunHeat() == 0.0D) && (Math.abs(getGunTurnRemainingRadians()) < 0.008726646259971648D)) {
			TargetingMethod bestMethod = targetedRobot.getBestTargetingMethod();
			double power = evaluateBulletPowerToUse(targetedRobot);
			double angleToPartner = 0.0D;


			for (int i = 0; i < partnerRobot.getTargetingMethods().size(); i++) {
				if (bestMethod.getNameOfMethod().equals(((TargetingMethod)partnerRobot.getTargetingMethods().get(i)).getNameOfMethod())) {
					angleToPartner = ((TargetingMethod)partnerRobot.getTargetingMethods().get(i)).targetReturningRelativeTurningAngle(thisRobot, partnerRobot, power);
				}
			}

			Point2D.Double predictedTargetLocation = bestMethod.targetReturningLocation(thisRobot, targetedRobot, power);

			double distanceToPartner = partnerRobot.getDistance(thisRobot.getX(), thisRobot.getY());
			double angleAcrossHalfPartner = Math.atan2(Math.sqrt(2.0D) * partnerRobot.getWidth() / 2.0D, distanceToPartner);

			if ((thisRobot.getDistance(predictedTargetLocation.x, predictedTargetLocation.y) < distanceToPartner) || (Math.abs(angleToPartner) > angleAcrossHalfPartner)) {
				System.out.println(targetedRobot.getBestTargetingMethod().getNameOfMethod());
				this.lastFireTime = getTime();
				targetedRobot.getBestTargetingMethod().fireReal(thisRobot, targetedRobot, power, getTime() + 1L, getGunHeadingRadians());
				setFire(power);
			}
		}
	}





















	private void transmitData()
	{
		try
		{
			broadcastMessage(new Message(enemyRobots, thisRobot, getTime()));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void onMessageReceived(MessageEvent e)
	{
		Message message = (Message)e.getMessage();
		if (partnerRobot == null) {
			partnerRobot = new OtherRobot(e.getSender(), ROBOT_WIDTH, e.getTime());
		}

		if (partnerRobot.getLastTick() < message.getSenderRobotData().getLastTick()) {
			partnerRobot.merge(message.getSenderRobotData());
		}

		ArrayList<OtherRobot> recievedEnemyRobotData = message.getEnemyRobotData();




		if (recievedEnemyRobotData.size() > enemyRobots.size()) {
			for (int i = 0; i < recievedEnemyRobotData.size(); i++) {
				boolean matchExists = false;
				for (int j = 0; j < enemyRobots.size(); j++) {
					if (((OtherRobot)recievedEnemyRobotData.get(i)).getName().equals(((OtherRobot)enemyRobots.get(j)).getName())) {
						matchExists = true;
					}
				}
				if (!matchExists) {
					enemyRobots.add(i, new OtherRobot(((OtherRobot)recievedEnemyRobotData.get(i)).getName(), ROBOT_WIDTH, e.getTime()));
				}
			}
		} else if (recievedEnemyRobotData.size() < enemyRobots.size()) {
			return;
		}


		for (int i = 0; i < enemyRobots.size(); i++) {
			((OtherRobot)enemyRobots.get(i)).merge((OtherRobot)recievedEnemyRobotData.get(i));
		}
	}
















	public void onBulletHit(BulletHitEvent e)
	{
		System.out.println("BOOM");
		System.out.println(e.getRobotName());

		if (e.getRobotName().equals(partnerRobot.getName())) {
			System.out.println("PC load letter? What the f*ck does that mean?");
			throw new IllegalArgumentException();
		}

		OtherRobot hitRobot = null;

		System.out.println("What if - and believe me this is a hypothetical - but what if you were offered some kind of a stock option equity sharing program. Would that do anything for you?");

		for (int i = 0; i < enemyRobots.size(); i++) {
			if (e.getRobotName().equals(((OtherRobot)enemyRobots.get(i)).getName())) {
				hitRobot = (OtherRobot)enemyRobots.get(i);
			}
		}

		if (hitRobot == null) {
			return;
		}
		for (int i = 0; i < hitRobot.getTargetingMethods().size(); i++) {
			((TargetingMethod)hitRobot.getTargetingMethods().get(i)).checkForRealCollisions(hitRobot, e.getTime());
		}
	}



	public void onHitByBullet(HitByBulletEvent e)
	{
		if (e.getName().equals(partnerRobot.getName()))
		{
			return;
		}

		for (int i = 0; i < enemyRobots.size(); i++) {
			if (e.getName().equals(((OtherRobot)enemyRobots.get(i)).getName())) {
				for (int j = 0; j < ((OtherRobot)enemyRobots.get(i)).getBullets().size(); j++) {
					((OtherRobot)enemyRobots.get(i)).logBulletHit(thisRobot, e.getTime());
				}
			}
		}
	}



	public void onRobotDeath(RobotDeathEvent e)
	{
		if (e.getRobotName().equals(partnerRobot.getName())) {
			partnerRobot.dies();
		}

		for (int i = 0; i < enemyRobots.size(); i++) {
			if (e.getRobotName().equals(((OtherRobot)enemyRobots.get(i)).getName())) {
				((OtherRobot)enemyRobots.get(i)).dies();
				System.out.println("We're gonna be getting rid of these people here... First, Mr. Samir Naga... Naga... Naga... Not gonna work here anymore, anyway.");
			}
		}
	}

























	public void paintRobot()
	{
		Color color = null;
		ArrayList<TargetingMethod> methods = getClosestAssignedBot().getTargetingMethods();
		TargetingMethod bestMethod = getClosestAssignedBot().getBestTargetingMethod();
		for (int i = 0; i < methods.size(); i++) {
			if (bestMethod.getNameOfMethod().equals(((TargetingMethod)methods.get(i)).getNameOfMethod())) {
				if (i == 0) {
					color = Color.BLACK;
				} else if (i == 1) {
					color = Color.RED;
				} else if (i == 2) {
					color = Color.GREEN;
				} else if (i == 3) {
					color = Color.CYAN;
				} else if (i == 4) {
					color = Color.MAGENTA;
				} else if (i == 5) {
					color = Color.BLUE;
				}
			}
		}
		setColors(color, color, Color.GRAY);
	}


	public void onPaint(Graphics2D g)
	{
		int n;
		for (int i = 0; i < enemyRobots.size(); i++) {
			for (int j = 0; j < ((OtherRobot)enemyRobots.get(i)).getTargetingMethods().size(); j++) {
				ArrayList<Bullet> bullets = ((TargetingMethod)((OtherRobot)enemyRobots.get(i)).getTargetingMethods().get(j)).getBullets();
				if (j == 0) {
					g.setColor(Color.BLACK);
				} else if (j == 1) {
					g.setColor(Color.RED);
				} else if (j == 2) {
					g.setColor(Color.GREEN);
				} else if (j == 3) {
					g.setColor(Color.CYAN);
				} else if (j == 4) {
					g.setColor(Color.MAGENTA);
				} else if (j == 5) {
					g.setColor(Color.BLUE);
				}

				for (n = 0; n < bullets.size(); n++) {
					Point2D.Double location = ((Bullet)bullets.get(n)).getLocation(getTime());
					g.fillOval((int)location.x, (int)location.y, 10, 10);
				}
			}
		}


		g.setColor(Color.pink);
		for (int i = 0; i < enemyRobots.size(); i++) {
			for (int j = 0; j < ((OtherRobot)enemyRobots.get(i)).getBullets().size(); j++) {
				Bullet bullet = (Bullet)((OtherRobot)enemyRobots.get(i)).getBullets().get(j);
				//TODO: figure out what the heck happened here
				//n = bullet.getDistanceTraveled(getTime());
			}
		}




		g.setColor(Color.ORANGE);
		for (int i = 0; i < 2; i++) {}
	}
}
