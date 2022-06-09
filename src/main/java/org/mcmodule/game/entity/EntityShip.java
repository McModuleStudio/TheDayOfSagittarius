package org.mcmodule.game.entity;

import org.lwjgl.opengl.GL11;

public class EntityShip extends Entity {

	public double size = 50D;
	
	public EntityShip(double posX, double posY) {
		super(posX, posY);
	}

	@Override
	protected void onRender() {
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glBegin(GL11.GL_TRIANGLES);
		GL11.glVertex2d(-size / 2, size);
		GL11.glVertex2d(size / 2, size);
		GL11.glVertex2d(0, -size);
		GL11.glEnd();
	}

	public boolean isFriend() {
		return true;
	}

	@Override
	public double getSize() {
		return size;
	}
}
