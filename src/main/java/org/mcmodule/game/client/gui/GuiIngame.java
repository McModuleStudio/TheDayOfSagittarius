package org.mcmodule.game.client.gui;

import org.lwjgl.opengl.GL11;
import org.mcmodule.game.client.Game;
import org.mcmodule.game.client.renderer.FontRenderer;
import org.mcmodule.game.entity.Entity;
import org.mcmodule.game.util.EnumTeam;

public class GuiIngame extends Gui {

	public GuiIngame(Game game) {
		super(game);
	}

	public void render() {
		renderMap();
	}
	
	private int renderMap() {
		int mapWidth = (int) (game.width * (2/3F)),
			mapHeight = game.height - 48;
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(4, game.height - 24 - mapHeight, mapWidth, mapHeight);
		GL11.glTranslated(mapWidth / 2D, mapHeight / 2D, 0);
		GL11.glTranslated(-game.getCameraX(), -game.getCameraY(), 0);
		GL11.glPushMatrix();
		GL11.glTranslated(-Game.MAP_WIDTH / 2D, -Game.MAP_HEIGHT / 2D, 0);
		GL11.glColor4f(0, 0.5f, 0.25f, 1);
		GL11.glCallList(game.getGridList());
		GL11.glPushMatrix();
		GL11.glTranslated(Game.MAP_WIDTH / 2D, Game.MAP_HEIGHT / 2D, 0);
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
		GL11.glStencilMask(0xFF);
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
		GL11.glColor4f(1, 1, 1, 0);
		for (Entity entity : game.getEntities()) {
			EnumTeam team = entity.getTeam();
			if(team == null || team == EnumTeam.Red) {
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
		drawRect(-Game.MAP_WIDTH * 2, -Game.MAP_HEIGHT * 2, Game.MAP_WIDTH * 2, Game.MAP_HEIGHT * 2);
		GL11.glColor4f(0, 0.75f, 0.25f, 1);
		GL11.glCallList(game.getGridList());
		GL11.glPopMatrix();
		Game.checkGLError("Grid");
		for (Entity entity : game.getEntities()) {
			entity.doRender();
		}
		Game.checkGLError("Render entity");
		GL11.glDisable(GL11.GL_STENCIL_TEST);
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		GL11.glPopMatrix();
		GL11.glColor4d(0.55, 0.6, 0.75, 1.0);
		drawRect(0, 0, game.width, 24);
		drawRect(0, 0, 4, game.height);
		drawRect(mapWidth, 0, mapWidth + 4, game.height);
		drawRect(0, game.height - 24, game.width, game.height);
		FontRenderer.F16.drawString("Map", 8, 2, 0xFFFFFF);
		return mapWidth+4;
	}
}
