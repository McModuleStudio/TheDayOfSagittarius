package org.mcmodule.game.entity;

import org.lwjgl.opengl.GL11;

public abstract class Entity {

	public double posX, posY, yaw;
	
	public Entity(double posX, double posY) {
		this.posX = posX;
		this.posY = posY;
	}
	
	public void move(double speed) {
		posX += Math.sin(Math.toRadians(yaw)) * speed;
		posY -= Math.cos(Math.toRadians(yaw)) * speed;
	}
	
	public void doRender() {
		GL11.glPushMatrix();
		GL11.glTranslated(posX, posY, 0);
		GL11.glRotated(yaw, 0, 0, 1);
		onRender();
		GL11.glPopMatrix();
	}

	protected abstract void onRender();
	
	public boolean isFriend() {
		return false;
	}
	
	public abstract double getSize();


}
