package org.mcmodule.game;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.mcmodule.game.entity.Entity;
import org.mcmodule.game.entity.EntityShip;

public class Game implements Runnable {

	private static final int MAP_WIDTH = 8192, MAP_HEIGHT = 2048;
	
	private boolean running = true;

	private int gridList;
	
	private int width, height;
	
	private Entity currentEntity = new EntityShip(0, 0);
	
	private double cameraX, cameraY;
	
	long lastUpdate;
	
	public Game() {
		
	}

	public void run() {
		try {
			init();
		} catch (LWJGLException e) {
			e.printStackTrace();
			return;
		}
		lastUpdate = System.currentTimeMillis();
		while(running)
			runLoop();
		destroy();
	}

	private void init() throws LWJGLException {
		Display.setDisplayMode(new DisplayMode(1280, 720));
		Display.create();
		Display.setSwapInterval(1);
		gridList = GL11.glGenLists(1);
		GL11.glNewList(gridList, GL11.GL_COMPILE);
		GL11.glBegin(GL11.GL_LINES);
		for(int y = 0; y <= MAP_HEIGHT; y += 64) {
			GL11.glVertex2d(0, y);
			GL11.glVertex2d(MAP_WIDTH, y);
		}
		for(int x = 0; x <= MAP_WIDTH; x += 64) {
			GL11.glVertex2d(x, 0);
			GL11.glVertex2d(x, MAP_HEIGHT);
		}
		GL11.glEnd();
		GL11.glEndList();
		checkGLError("Pre startup");
		Keyboard.create();
	}
	
	private void runLoop() {
		width = Display.getWidth();
		height = Display.getHeight();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, width, height, 0, 1000, 3000);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslated(0, 0, -1000);
		GL11.glViewport(0, 0, width, height);
		GL11.glClearColor(0, 0.05f, 0.10f, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		checkGLError("Pre Render");
		GL11.glPushMatrix();
		GL11.glTranslated(-MAP_WIDTH / 2D, -MAP_HEIGHT / 2D, 0);
		GL11.glColor4f(0, 0.5f, 0.25f, 1);
		GL11.glCallList(gridList);
		GL11.glColor4f(0, 0.75f, 0.25f, 1);
		GL11.glCallList(gridList);
		GL11.glPopMatrix();
		GL11.glTranslated(width / 2D, height / 2D, 0);
		checkGLError("Grid");
		GL11.glTranslated(-cameraX, -cameraY, 0);
		if(currentEntity != null) {
			currentEntity.doRender();
		}
		checkGLError("Post Render");
		while(System.currentTimeMillis() - lastUpdate >= 50L) {
			onTick();
			lastUpdate += 50L;
		}
		running &= !Display.isCloseRequested();
		Display.update();
	}
	
	private void onTick() {
		while(Keyboard.next()) {
			
		}
		if(currentEntity != null) {
			if(Keyboard.isKeyDown(Keyboard.KEY_W)) {
				currentEntity.move(1D);
			}
			if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
				currentEntity.move(-1D);
			}
			if(Keyboard.isKeyDown(Keyboard.KEY_A)) {
				currentEntity.yaw--;
			}
			if(Keyboard.isKeyDown(Keyboard.KEY_D)) {
				currentEntity.yaw++;
			}
		}
	}

	private void destroy() {
		Display.destroy();
	}
	
	public static void checkGLError(String location) {
		int code = GL11.glGetError();
		if(code != GL11.GL_NO_ERROR) {
			System.err.printf("====================\nGLError: %d(%s)\nLocation: %s\n====================\n", code, GL11.glGetString(code), location);
		}
	}

	public static void main(String[] args) {
		new Thread(new Game()).start();
	}
}