package org.mcmodule.game.client.gui;

import org.lwjgl.opengl.GL11;
import org.mcmodule.game.client.Game;

public abstract class Gui {
	
	protected final Game game;
	
	public Gui(Game game) {
		this.game = game;
	}

	public abstract void render();

	public void onTick() {}
	
	public static void drawRect(int x0, int y0, int x1, int y1) {
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2d(x0, y1);
		GL11.glVertex2d(x1, y1);
		GL11.glVertex2d(x1, y0);
		GL11.glVertex2d(x0, y0);
		GL11.glEnd();
	}
}
