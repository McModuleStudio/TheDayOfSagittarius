package org.mcmodule.game.util;

import javax.vecmath.GMatrix;

public class BoundingBox extends GMatrix implements java.io.Serializable {

	private static final long serialVersionUID = 7377221464643879501L;

	BoundingBox(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3) {
		super(4, 2, new double[] { x0, y0, x1, y1, x2, y2, x3, y3 });
	}

	public void add(double x, double y) {
		this.add(new BoundingBox(x, y, x, y, x, y, x, y));
	}
	

	public boolean isBoxInBoundingBox(BoundingBox box) {
		if(super.equals(box))
			return true;
		return isBoxInBoundingBox0(box) || box.isBoxInBoundingBox0(this);
	}
	
	private boolean isBoxInBoundingBox0(BoundingBox box) {
		for (int i = 0; i < 4; i++) {
			double x = box.getElement(i, 0),
				   y = box.getElement(i, 1);
			for (int j = 0; j < 2; j++) {
				if (x >= getElement(j, 0) && x <= getElement(j + 1, 0) &&
					y >= getElement(j, 1) && y <= getElement(j + 1, 1))
					return true;
			}
		}
		return false;
	}
	
	public static BoundingBox create(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3) {
		return new BoundingBox(x0, y0, x1, y1, x2, y2, x3, y3);
	}
}
