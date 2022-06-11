package org.mcmodule.game.client;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;
import org.mcmodule.game.client.gui.Gui;
import org.mcmodule.game.client.gui.GuiIngame;
import org.mcmodule.game.client.gui.Overlay;
import org.mcmodule.game.entity.Entity;
import org.mcmodule.game.entity.EntityShip;
import org.mcmodule.game.util.EnumTeam;

import lombok.Getter;

public class Game implements Runnable {

	public static final int MAP_WIDTH = 16384,
							MAP_HEIGHT = 65536;
	
	private boolean running = true;

	@Getter
	private int gridList;
	
	public int width, height;
	
	private Entity currentEntity = new EntityShip(0, 0, EnumTeam.Red);
	
	@Getter
	private List<Entity> entities = new ArrayList<>();
	
	@Getter 
	private double cameraX, cameraY;
	
	public Gui currentGui = new GuiIngame(this);
	public Overlay currentOverlay = null;
	
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
		entities.add(new EntityShip(0, 0, EnumTeam.Blue));
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
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glStencilMask(0x00);
		checkGLError("Pre Render");
		currentGui.render();
		checkGLError("Post Render");
		while(System.currentTimeMillis() - lastUpdate >= 50L) {
			onTick();
			lastUpdate += 50L;
		}
		running &= !Display.isCloseRequested();
		Display.update();
	}
	
	private void onTick() {
		while(Keyboard.next());
		while(Mouse.next());
		if(currentEntity != null) {
			if(Keyboard.isKeyDown(Keyboard.KEY_W)) {
				currentEntity.move(4D);
			}
			if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
				currentEntity.move(-4D);
			}
			if(Keyboard.isKeyDown(Keyboard.KEY_A)) {
				currentEntity.setRotation(currentEntity.yaw-1);
			}
			if(Keyboard.isKeyDown(Keyboard.KEY_D)) {
				currentEntity.setRotation(currentEntity.yaw+1);
			}
//			cameraX = currentEntity.posX; cameraY = currentEntity.posY;
		}
		currentGui.onTick();
//		System.out.println(entities.get(1).getBoundingBox().isBoxInBoundingBox(currentEntity.getBoundingBox()));
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
