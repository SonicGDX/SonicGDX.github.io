package com.sonicgdx.sonicswirl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * This is the class that handles player movement, player collision with the ground as well as player collision
 * with other objects.
 * It is final since it is not necessary to extend this class.
 */
public final class Player extends Entity {
    private boolean debugMode = false, isGrounded, isJumping;
    private final float ACCELERATION = 168.75F, AIR_ACCELERATION = 337.5F, SLOPE_FACTOR = 7.5F, GRAVITY_FORCE = -787.5F;
    private final int DECELERATION = 1800, MAX_SPEED = 360, JUMP_FORCE = 390;
    // An FPS of 60 was used to obtain the adjusted values
    // Original: ACCELERATION = 0.046875F, DECELERATION = 0.5F, DEBUG_SPEED = 1.5F, MAX_SPEED = 6, SLOPE_FACTOR = 0.125, AIR_ACCELERATION = 0.09375F, GRAVITY_FORCE = 0.21875F;
    // Original values were designed to occur 60 times every second so by multiplying it by 60 you get the amount of pixels moved per second.
    private float groundVelocity = 0, groundAngle = 0;
    private final Sensor sensorA, sensorB, sensorE,sensorF;
    private Vector2 velocity;
    Player(Texture image, int width, int height) {
        super(image, width, height);
        position.set(600,200); // Sets the player's starting position at (600,200). (The variable was initialised in super constructor)
        //The vector has two components for the x position and y position respectively
        velocity = new Vector2(); //Initialises to zero starting speed
        sensorA = new Sensor(position.x,position.y);
        sensorB = new Sensor(position.x + (sprite.getWidth() - 1),position.y);
        sensorE = new Sensor(position.x,position.y + (sprite.getHeight() - 1) / 2);
        sensorF = new Sensor(position.x + (sprite.getWidth() - 1),position.y + (sprite.getHeight() - 1) / 2);
    }

    //TODO Tommy Ettinger's digital extension could be used for faster operations on GWT


    /**
     * @param delta time since last frame. Used to make physics similar to how they would be at 60FPS
     * at different frame rates.
     * @see GameScreen#render(float)
     */
    public void move(float delta)
    {
        //TODO Would be better to implement an InputProcessor. This makes more sense as an interrupt rather than constant polling.
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q))
        {
            //Toggle debug mode
            debugMode = !debugMode;

            //Reset movement variables when entering or exiting debug mode to prevent oddities in physics.
            groundVelocity = 0;
            velocity.setZero();
            groundAngle = 0;

            //TODO acceleration in debug mode
        }
        if (debugMode) {
            debugMove(delta);
        }
        else {
            //FIXME Right now, right movement is prioritised if both directions are pressed at the same time. Consider cancelling them out.

            if (!isGrounded) {
                airMove(delta);
                setAirSensors();
            }

            else {
                groundMove(delta);

                sensorA.setActive(true);
                sensorB.setActive(true);

                //Updates player position
                position.x += velocity.x * delta;
                position.y += velocity.y * delta;

            }

            if (sensorA.getActive() && sensorB.getActive()) {

                Sensor winningSensor = floorSensors();

                if (isGrounded) {
                    if (winningSensor != null && Math.max(-Math.abs(velocity.x) - 4, -14) < winningSensor.getDistance() && winningSensor.getDistance() < 14) groundCollision(winningSensor); //TODO comment out this line first if there are physics bugs.
                    else isGrounded = false;
                }
                else{
                    if (Math.abs(velocity.x) >= Math.abs(velocity.y)) {
                        if (velocity.x > 0) { //going mostly right
                            if (winningSensor != null && winningSensor.getDistance() >= 0 && velocity.y <= 0) groundCollision(winningSensor);
                        }
                        else { //going mostly left
                            if (winningSensor != null && winningSensor.getDistance() >= 0 && velocity.y <= 0) groundCollision(winningSensor);
                        }
                    }
                    else {
                        if (velocity.y > 0) { //going mostly up

                        }
                        else { //going mostly down
                            if (winningSensor != null && winningSensor.getDistance() >= 0 && (sensorA.getDistance() <= -(velocity.y + 8) || sensorB.getDistance() >= -(velocity.y + 8))) groundCollision(winningSensor);
                        }
                    }
                }

            }

            sensorE.wallProcess();
            sensorF.wallProcess();



            //TODO perhaps add a check if the player is stationary before calculating collision

        }

        enforceBoundaries();
        calculateSensorPositions();

        sprite.setPosition(position.x, position.y);
        sprite.setRotation(groundAngle);

    }

    private void groundMove(float delta) {
        if (groundVelocity != 0) groundVelocity -= delta * SLOPE_FACTOR * MathUtils.sinDeg(groundAngle); //TODO this only happens when the player is not in ceiling mode.

        if (Gdx.input.isKeyPressed(Input.Keys.D) || (Gdx.input.isKeyPressed(Input.Keys.RIGHT))) // if moving right
        {
            if (groundVelocity < 0) groundVelocity += (DECELERATION * delta); // Deceleration acts in the opposite direction to the one in which the player is currently moving.
            else if (groundVelocity < MAX_SPEED) groundVelocity += (ACCELERATION * delta); //Takes 128 frames to accelerate from 0 to 6 - exactly 2 seconds
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.A) || (Gdx.input.isKeyPressed(Input.Keys.LEFT))) // if moving left
        {
            if (groundVelocity > 0) groundVelocity -= (DECELERATION * delta);
            else if (groundVelocity > -MAX_SPEED) groundVelocity -= ACCELERATION * delta;
        }
        else groundVelocity -= Math.min(Math.abs(groundVelocity), ACCELERATION * delta) * Math.signum(groundVelocity); // friction if not pressing any directions
        // Decelerates until the absolute value of groundSpeed is lower than the ACCELERATION value (which doubles as the friction value) and then stops

        velocity.set(groundVelocity * MathUtils.cosDeg(groundAngle), groundVelocity * MathUtils.sinDeg(groundAngle));

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) jump(delta); //TODO placement different from original, may cause bugs.


        //FIXME player momentum functions oddly when landing after jumping downwards from a steep slope
    }

    /**
     * Collides with the nearest floor within a certain limit by adjusting the player's y position appropriately.
     * The positive limit is always 14, but the negative limit only becomes more lenient as the player's speed increases.
     * Limits of -16<=x<=16 are not used as those distances are likely too far away from the player to matter.
     * Uses angle for rotation and speed of the player and for player slope physics. TODO
     * Applies unique calculation to find minimum value, from Sonic 2 depending on the player's speed.
     * @return NULLABLE "Winning Distance" sensor.
     * Returns null in the condition that the sensor distances are equal but their respective returnTiles are different -
     * this prevents the groundAngle being changed and the player rotating haphazardly.
     */
    public Sensor floorSensors()
    {
        //Checks below and potentially above the positions of both sensors to find the nearest tile if one is present.
        sensorA.floorProcess();
        sensorB.floorProcess();

        /*
        Returns the sensor that had found the greater distance.
        Note that even if it returns a sensor it may not have a valid distance.
        The validation happens outside this method.
        */
        if(sensorA.getDistance() > sensorB.getDistance()) return sensorA;
        else if (sensorA.getDistance() < sensorB.getDistance()) return sensorB;
        //If sensorB could be returned in this case it would not make a difference - the sensors are essentially the same.
        else if (sensorA.getTile() == sensorB.getTile()) return sensorA; //FIXME comment out this line first if there are physics bugs.
        else return null;

    }

    public void groundCollision(Sensor sensor)
    {
        /*
        Corrects the player's Y position according to the distance found by the sensor.
        This should place them at the same height as the found tile after the frame is drawn, responding to the collision.
        Note that the player might not be on top of a surface even after this - this line may run multiple times if they
        are inside the floor to push them upwards and out of it.
        */
        position.y += sensor.getDistance();

        /*
        In the special case that the Tile has an angle of 360 (the 'flagged' angle)
        the player's current angle is rounded to the nearest 90 degrees.
        An example of a use case is the peak of a ramp which sends the player directly upwards when they run off it.
        */
        if (groundAngle == 360) {
            groundAngle = snapToNearest(groundAngle,90);
        }

        //Otherwise, sets the player's ground angle to that of the tile found by the sensor.
        else groundAngle = sensor.getTile().angle; //TODO possibly apply this to enemies?

        /*
        This block is run when the player lands onto the ground from the air (e.g. after jumping).
        Its purpose is to apply the same momentum that the player had whilst in the air, but adjusted
        depending on the angle of the ground.
        */
        if (!isGrounded) {
            if (0 <= groundAngle && groundAngle <= 23) groundVelocity = velocity.x;
                //TODO when mirrored... https://info.sonicretro.org/SPG:Slope_Physics#When_Falling_Downward
            else if (24 <= groundAngle && groundAngle <= 45) {
                if (Math.abs(velocity.x) >= Math.abs(velocity.y)) {
                    groundVelocity = velocity.x;
                } else {
                    groundVelocity = velocity.y * 0.5F * -MathUtils.sinDeg(groundAngle);
                }
            }
            else if (46 <= groundAngle && groundAngle <= 90) {
                if (Math.abs(velocity.x) >= Math.abs(velocity.y)) {
                    groundVelocity = velocity.x;
                } else {
                    groundVelocity = velocity.y * -MathUtils.sinDeg(groundAngle);
                }
            }
            isGrounded = true;
            if (isJumping) isJumping = false;
        }
    }

    /**
     * @param delta time since last frame. Used to make physics similar to how they would be at 60FPS
     * at different frame rates.
     */
    public void jump(float delta) {
        //FIXME bug when jumping while moving downhill on a slope
        velocity.x -= JUMP_FORCE * MathUtils.sinDeg(groundAngle);
        velocity.y += JUMP_FORCE * MathUtils.cosDeg(groundAngle);
        isGrounded = false; isJumping = true;
        //TODO if time is available, jump buffering and coyote time
    }

    public void airMove(float delta) {
        //Reduce the height jumped by capping the Y Speed if player releases the jump button (Space) early.
        if (!Gdx.input.isKeyPressed(Input.Keys.SPACE) && velocity.y > 4 && isJumping) velocity.y = 4;

        //Air acceleration
        if (Gdx.input.isKeyPressed(Input.Keys.D) || (Gdx.input.isKeyPressed(Input.Keys.RIGHT))) // if moving right
        {
            if (velocity.x < MAX_SPEED) velocity.x += AIR_ACCELERATION * delta; // accelerates right at twice the speed compared to on ground
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.A) || (Gdx.input.isKeyPressed(Input.Keys.LEFT))) // if moving left
        {
            if (velocity.x > -MAX_SPEED) velocity.x -= AIR_ACCELERATION * delta; // accelerates left at twice the speed compared to on ground
        }
        //Air drag
        if (0 < velocity.y && velocity.y < 4)
        {
            velocity.x -= (MathUtils.floor(velocity.x / 0.125F) / 256F * 60F * delta); //TODO Maybe use 60 * delta in all calculations instead of applying it to variable. For readability
        }

        //Updates player position
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        //Gravity - a force pushing the player down when they are in the air
        velocity.y += GRAVITY_FORCE * delta;
    }

    /**
     * && operator uses short-circuit evaluation (as opposed to &) so will only evaluate the left hand side of the boolean expression is true. This means that
     * if the returned value is null it won't check its distance so won't throw a NullPointerException.
     */
    public void setAirSensors(){
        //TODO insert sensorC and sensorD
        if (Math.abs(velocity.x) >= Math.abs(velocity.y)) {
            //In both cases the ground sensors will be checked
            sensorA.setActive(true);
            sensorB.setActive(true);
            if (velocity.x > 0) { //going mostly right

                sensorE.setActive(false);
                sensorF.setActive(true);
            }
            else { //going mostly left

                sensorE.setActive(true);
                sensorF.setActive(false);
            }
        }
        else {
            //In both cases the wall sensors will be checked
            sensorE.setActive(true);
            sensorF.setActive(true);
            if (velocity.y > 0) { //going mostly up
                sensorA.setActive(false);
                sensorB.setActive(false);
            }
            else { //going mostly down
                sensorA.setActive(true);
                sensorB.setActive(true);
            }
        }
    }

    /**
     * Sets the sensor positions relative to the player's position.
     * sensorA is positioned in the bottom left corner and sensorB in the bottom right corner.
     * sensorE is positioned at the centre left and sensorF is positioned at the centre right.
     */
    @Override
    public void calculateSensorPositions() {
        super.calculateSensorPositions();
        sensorA.setPosition(lSensorX,position.y); //TODO possibly remove these variables
        sensorB.setPosition(rSensorX,position.y);
        sensorE.setPosition(lSensorX,centreY);
        sensorF.setPosition(rSensorX,centreY);
    }

    private void debugMove(float delta) {
        final int DEBUG_SPEED = 90;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) position.x += (DEBUG_SPEED * delta);
        if (Gdx.input.isKeyPressed(Input.Keys.A)) position.x -= (DEBUG_SPEED * delta);
        if (Gdx.input.isKeyPressed(Input.Keys.W)) position.y += (DEBUG_SPEED * delta);
        if (Gdx.input.isKeyPressed(Input.Keys.S)) position.y -= (DEBUG_SPEED * delta);
        //Gdx.app.debug("deltaTime",String.valueOf(delta));
    }

    public float getXPosition() {
        return position.x;
    }
    public float getYPosition() {
        return position.y;
    }
}
