package org.mcmodule.game.entity;

import org.lwjgl.opengl.GL11;
import org.mcmodule.game.util.EnumTeam;

public class EntityShip extends Entity {

	private static final long serialVersionUID = 7581191034260251498L;
	public double size = 50D;
	
	public EntityShip(double posX, double posY, EnumTeam team) {
		super(posX, posY);
		this.updateBoundingBox();
		this.team = team;
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

	@Override
	public double getSize() {
		return size;
	}
}
