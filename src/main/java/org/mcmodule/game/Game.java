package org.mcmodule.game;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;
import org.mcmodule.game.entity.Entity;
import org.mcmodule.game.entity.EntityShip;

public class Game implements Runnable {

	private static final int MAP_WIDTH = 16384, MAP_HEIGHT = 65536;
	
	private boolean running = true;

	private int gridList;
	
	private int width, height;
	
	private Entity currentEntity = new EntityShip(0, 0);
	
	private List<Entity> entities = new ArrayList<>();
	
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
		Display.create(new PixelFormat(), new ContextAttribs().withDebug(true));
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
		entities.add(currentEntity);
//		entities.add(new EntityShip(0, 0));
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
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glStencilMask(0x00);
		checkGLError("Pre Render");
		GL11.glTranslated(width / 2D, height / 2D, 0);
		GL11.glTranslated(-cameraX, -cameraY, 0);
		GL11.glPushMatrix();
		GL11.glTranslated(-MAP_WIDTH / 2D, -MAP_HEIGHT / 2D, 0);
		GL11.glColor4f(0, 0.5f, 0.25f, 1);
		GL11.glCallList(gridList);
		GL11.glPushMatrix();
		GL11.glTranslated(MAP_WIDTH / 2D, MAP_HEIGHT / 2D, 0);
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
		GL11.glStencilMask(0xFF);
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
		GL11.glColor4f(1, 1, 1, 0);
		for (Entity entity : entities) {
			if(entity.isFriend()) {
				double x = entity.posX,
					   y = entity.posY;
				double size = entity.getSize() * 1.5;
				GL11.glBegin(GL11.GL_POLYGON);
				for(int i=0;i<90;i++) {
					GL11.glVertex2d(x + size * Math.cos(2 * Math.PI * i / 90d), y + size * Math.sin(2 * Math.PI * i / 90d));
				}
				GL11.glEnd();
			}
		}
		GL11.glPopMatrix();
		GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
		GL11.glStencilMask(0x00);
		GL11.glColor4f(0, 0.05f, 0.125f, 1);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2d(-MAP_WIDTH * 2D,  MAP_HEIGHT * 2D);
		GL11.glVertex2d( MAP_WIDTH * 2D,  MAP_HEIGHT * 2D);
		GL11.glVertex2d( MAP_WIDTH * 2D, -MAP_HEIGHT * 2D);
		GL11.glVertex2d(-MAP_WIDTH * 2D, -MAP_HEIGHT * 2D);
		GL11.glEnd();
		GL11.glColor4f(0, 0.75f, 0.25f, 1);
		GL11.glCallList(gridList);
		GL11.glPopMatrix();
		checkGLError("Grid");
		for (Entity entity : entities) {
			entity.doRender();
		}
		checkGLError("Render entity");
		GL11.glDisable(GL11.GL_STENCIL_TEST);
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
				currentEntity.move(4D);
			}
			if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
				currentEntity.move(-4D);
			}
			if(Keyboard.isKeyDown(Keyboard.KEY_A)) {
				currentEntity.yaw--;
			}
			if(Keyboard.isKeyDown(Keyboard.KEY_D)) {
				currentEntity.yaw++;
			}
//			cameraX = currentEntity.posX; cameraY = currentEntity.posY;
		}
	}

	private void destroy() {
		Display.destroy();
	}
	
	public static void checkGLError(String location) {
		int code = GL11.glGetError();
		if(code != GL11.GL_NO_ERROR) {
			System.err.printf("====================\nGLError: %d(%s)\nLocation: %s\n====================\n", code, GLU.gluErrorString(code), location);
		}
	}

	public static void main(String[] args) {
		new Thread(new Game()).start();
	}
}
