package org.mcmodule.game.entity;

import org.lwjgl.opengl.GL11;
import org.mcmodule.game.util.BoundingBox;
import org.mcmodule.game.util.EnumTeam;

import lombok.Getter;

public abstract class Entity implements java.io.Serializable {

	private static final long serialVersionUID = -3401174828555234742L;

	public double posX, posY, yaw;
	
	@Getter
	protected EnumTeam team;
	
	@Getter
	protected BoundingBox boundingBox;
	
	public Entity(double posX, double posY) {
		setPosition(posX, posY);
	}
	
	public void move(double speed) {
		double x = +Math.sin(Math.toRadians(yaw)) * speed,
			   y = -Math.cos(Math.toRadians(yaw)) * speed;
		boundingBox.add(x, y);
		updatePosition();
	}
	
	public void setPosition(double x, double y) {
		posX = x;
		posY = y;
		updateBoundingBox();
	}
	
	public void setRotation(double yaw) {
		this.yaw = yaw;
		updateBoundingBox();
	}
	
	protected void updateBoundingBox() {
		double x = +Math.sin(Math.toRadians(yaw)) * getSize(),
			   y = -Math.cos(Math.toRadians(yaw)) * getSize();
		boundingBox = BoundingBox.create(posX - x + y, posY - y - x, posX + x + y, posY + y - x, posX - x - y, posY - y + x, posX + x - y, posY + y + x);
	}
	
	protected void updatePosition() {
		posY = posX = 0;
		for(int i=0;i<4;i++) {
			posX += boundingBox.getElement(i, 0);
			posY += boundingBox.getElement(i, 1);
		}
		posX /= 4;
		posY /= 4;
	}
	
	public void doRender() {
		// ---- Bounding Box
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2d(boundingBox.getElement(2, 0), boundingBox.getElement(2, 1));
		GL11.glVertex2d(boundingBox.getElement(3, 0), boundingBox.getElement(3, 1));
		GL11.glVertex2d(boundingBox.getElement(1, 0), boundingBox.getElement(1, 1));
		GL11.glVertex2d(boundingBox.getElement(0, 0), boundingBox.getElement(0, 1));
		GL11.glEnd();
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		GL11.glPushMatrix();
		GL11.glTranslated(posX, posY, 0);
		GL11.glRotated(yaw, 0, 0, 1);
		onRender();
		GL11.glPopMatrix();
	}

	protected abstract void onRender();
	
	public abstract double getSize();
}
