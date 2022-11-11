package com.sonicgdx.sonicswirl;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Arrays;

//import java.awt.*;

//import com.badlogic.gdx.math.Rectangle;
//import com.badlogic.gdx.Game; -- replaces ApplicationAdapter
public class SonicGDX extends Game {
	SpriteBatch batch; Sprite sprite1; Texture img, img2; ShapeRenderer dr;
	OrthographicCamera camera;
	// capital F can be used to cast from double to float (e.g. 50.55F)
	float speedX = 0, speedY = 0, groundSpeed = 0;
	float x = 600, y = 200; //https://colourtann.github.io/HelloLibgdx/
	Vector2 cameraOffset = Vector2.Zero;

	//https://info.sonicretro.org/SPG:Solid_Tiles#Sensors
	float leftFootSensor, rightFootSensor;

	static final float accel = 0.046875F, decel = 0.5F;


	FPSLogger frameLog;

	// solid blocks
	int[] block = {16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16};
	int[] empty = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	int[] staircase = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};

	int[][][] steepChunk = {{staircase,empty,empty,empty,empty,empty,empty,empty},{block,staircase,empty,empty,empty,empty,empty,empty},{block,block,staircase,empty,empty,empty,empty,empty},{block,block,block,staircase,empty,empty,empty,empty},{block,block,block,block,staircase,empty,empty,empty},{block,block,block,block,block,staircase,empty,empty},{block,block,block,block,block,block,staircase,empty},{block,block,block,block,block,block,block,staircase}};

	int[][][] blockChunk = {{block,block,block,block,block,block,block,block},{block,block,block,block,block,block,block,block},{block,block,block,block,block,block,block,block},{block,block,block,block,block,block,block,block},{block,block,block,block,block,block,block,block},{block,block,block,block,block,block,block,block},{block,block,block,block,block,block,block,block},{block,block,block,block,block,block,block,block}};

	int[][][] emptyChunk = {{empty,empty,empty,empty,empty,empty,empty,empty},{empty,empty,empty,empty,empty,empty,empty,empty},{empty,empty,empty,empty,empty,empty,empty,empty},{empty,empty,empty,empty,empty,empty,empty,empty},{empty,empty,empty,empty,empty,empty,empty,empty},{empty,empty,empty,empty,empty,empty,empty,empty},{empty,empty,empty,empty,empty,empty,empty,empty},{empty,empty,empty,empty,empty,empty,empty,empty}};


	int[][][][][] steepMap = {{steepChunk,emptyChunk,emptyChunk,emptyChunk},{blockChunk,steepChunk,emptyChunk,emptyChunk},{blockChunk,blockChunk,steepChunk,emptyChunk},{blockChunk,blockChunk,blockChunk,steepChunk}};


	// 128x128 chunk - one dimension for x, one dimension for y and the data is a height array
	// one height array makes up a 16x16 block


	@Override
	public void create () { // equivalent to start in unity

		// implement class with reference to https://gamedev.stackexchange.com/a/133593

		//System.out.println(tile[1][3][15]);

		camera = new OrthographicCamera(); // 3D camera which projects into 2D.
   		camera.setToOrtho(false, 1280, 720); // Even if the device has a scaled resolution, the in game view will still be 1280x720
		// So for example, one screen won't be in the bottom left corner in 1080p
		// but would take up the entire view

		// https://web.archive.org/web/20200427232345/https://www.badlogicgames.com/wordpress/?p=1550


		dr = new ShapeRenderer();
		batch = new SpriteBatch(); //sprite batch provides multiple sprites to draw to the GPU to improve openGl performance https://gamedev.stackexchange.com/questions/32910/what-is-the-technical-definition-of-sprite-batching
		img = new Texture("1x1-ffffffff.png"); img2 = new Texture("1x1-000000ff.png");

		sprite1 = new Sprite(img2,25,50);
		sprite1.setPosition(x,y);

		cameraOffset.x = camera.position.x - sprite1.getX();
		cameraOffset.y = camera.position.y - sprite1.getY();

		frameLog = new FPSLogger();
	}

	@Override
	public void render () { // equivalent to update in unity

		frameLog.log();

		ScreenUtils.clear(Color.GRAY); // clears the screen and sets the background to a certain colour

		camera.update(); // recompute matrix for orthographical projection - this is necessary if it needs to move.

		dr.setProjectionMatrix(camera.combined);
		dr.begin(ShapeRenderer.ShapeType.Filled);
		for (int i=0;i<4;i++)
		{
			for (int j =0; j<4;j++)
			{
				drawChunk(i,j);
			}
		}

		dr.end();

		//if (250-y >= 150) y += 10;
		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			//ternary operator
			groundSpeed = (groundSpeed + accel <= 6) ? (groundSpeed + accel) : 6;
			//Takes 128 frames to accelerate from 0 to 6 - exactly 2 seconds

			if (x <= 1280) {
				x += groundSpeed;
				if (x > 1280) x = 20;
			}

		} else {
			groundSpeed = (groundSpeed - decel >= 0) ? (groundSpeed - decel) : 0;
			x += groundSpeed;
		}
		sprite1.setPosition(x, y); camera.position.set(sprite1.getX() + cameraOffset.x,sprite1.getY() + cameraOffset.y,camera.position.z);

		leftFootSensor = sprite1.getX();
		rightFootSensor = sprite1.getX() + (sprite1.getWidth() - 1); // x pos + (srcWidth - 1) - using srcWidth places it one pixel right of the square

		// tell the SpriteBatch to render in the coordinate system specified by the camera - https://libgdx.com/wiki/start/a-simple-game
		batch.setProjectionMatrix(camera.combined);
		batch.begin();



		sprite1.draw(batch);

		// DEBUG
		batch.draw(img,leftFootSensor,y); batch.draw(img,rightFootSensor,y);


		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
		dr.dispose();
	}

	public void drawChunk(int chunkX, int chunkY) {

		for (int blockX = 0; blockX < 8; blockX++)
		{
			for (int blockY = 0; blockY < 8; blockY++)
			{
				for (int grid = 0; grid < 16; grid++)
				{
					//batch.draw(img, blockX*16+grid+(128*chunkX),blockY*16+(128*chunkY),1, steepMap[chunkX][chunkY][blockX][blockY][grid]);
					Color newColour = new Color(grid/16F,blockY/8F,blockX/8F,1);
					dr.rect(blockX*16+grid+(128*chunkX),blockY*16+(128*chunkY),1,steepMap[chunkX][chunkY][blockX][blockY][grid],newColour,newColour, newColour, newColour);
				}
			}
		}
	}

}