package org.mcmodule.game.client.renderer;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.lwjgl.opengl.GL11;

public class FontRenderer {
	public static final FontRenderer F16;
	
	private static final int[] colorCode = new int[32];
	
	private final Font font;
	private final float size;
	private final byte[][] charwidth = new byte[256][];
	private final int[] textures = new int[256];
	private final FontRenderContext context = new FontRenderContext(new AffineTransform(), true, true);
	private final int fontWidth, fontHeight;
	private final int textureWidth, textureHeight;

	public int getFontHeight(){
		return fontHeight / 2;
	}

	public FontRenderer(Font font) {
		this.font = font;
		size = font.getSize2D();
		Arrays.fill(textures, -1);
		Rectangle2D maxBounds = font.getMaxCharBounds(context);
		this.fontWidth = (int) Math.ceil(maxBounds.getWidth());
		this.fontHeight = (int) Math.ceil(maxBounds.getHeight());
		if(fontWidth > 127 || fontHeight > 127)
			throw new IllegalArgumentException("Font size to large!");
		this.textureWidth = resizeToOpenGLSupportResolution(fontWidth * 16);
		this.textureHeight = resizeToOpenGLSupportResolution(fontHeight * 16);
	}
	
	protected int drawChar(char chr, int x, int y) {
		int region = chr >> 8;
		int id = chr & 0xFF;
		int xTexCoord = (id & 0xF) * fontWidth,
			yTexCoord = (id >> 4) * fontHeight;
		int width = getOrGenerateCharWidthMap(region)[id];
		glBindTexture(GL_TEXTURE_2D, getOrGenerateCharTexture(region));
		glEnable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glBegin(GL_QUADS);
		glTexCoord2d(wrapTextureCoord(xTexCoord, textureWidth), wrapTextureCoord(yTexCoord, textureHeight));
		glVertex2f(x, y);
		glTexCoord2d(wrapTextureCoord(xTexCoord, textureWidth), wrapTextureCoord(yTexCoord + fontHeight, textureHeight));
		glVertex2f(x, y + fontHeight);
		glTexCoord2d(wrapTextureCoord(xTexCoord + width, textureWidth), wrapTextureCoord(yTexCoord + fontHeight, textureHeight));
		glVertex2f(x + width, y + fontHeight);
		glTexCoord2d(wrapTextureCoord(xTexCoord + width, textureWidth), wrapTextureCoord(yTexCoord, textureHeight));
		glVertex2f(x + width, y);
		glEnd();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		return width;
	}
	
	public int drawString(String str, int x, int y, int color) {
		return drawString(str, x, y, color, false);
	}
	
	public int drawString(String str, int x, int y, int color, boolean darken) {
		int offset = 0;
		if(darken) {
			color = (color & 0xFCFCFC) >> 2 | color & 0xFF000000;
		}
		float r,g,b,a;
		r = (color >> 16 & 0xFF) / 255F;
		g = (color >>  8 & 0xFF) / 255F;
		b = (color >>  0 & 0xFF) / 255F;
		a = (color >> 24 & 0xFF) / 255F;
		if(a == 0)
			a = 1;
		glColor4f(r, g, b, a);
		glPushMatrix();
		char[] chars = str.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char chr = chars[i];
			if(chr == '\u00A7' && i != chars.length-1) {
				i++;
				color = "0123456789abcdef".indexOf(chars[i]);
				if(color != -1) {
					if(darken) color |= 0x10;
					color = colorCode[color];
					r = (color >> 16 & 0xFF) / 255f;
					g = (color >>  8 & 0xFF) / 255f;
					b = (color >>  0 & 0xFF) / 255f;
					glColor4f(r, g, b, a);
				}
				continue;
			}
			offset += drawChar(chr, x + offset, y);
		}
		glPopMatrix();
		return offset;
	}
	
	public int getStringWidth(String str) {
		int width = 0;
		char[] chars = str.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char chr = chars[i];
			if(chr == '\u00A7' && i != chars.length-1)
				continue;
			width += getOrGenerateCharWidthMap(chr >> 8)[chr & 0xFF];
		}
		return width / 2;
	}
	
	public float getSize() {
		return size;
	}
	
	private int generateCharTexture(int id) {
		int textureId = glGenTextures();
		int offset = id << 8;
		BufferedImage img = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
//		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g.setFont(font);
		g.setColor(Color.WHITE);
		FontMetrics fontMetrics = g.getFontMetrics();
		for(int y=0;y<16;y++) for(int x=0;x<16;x++) {
			String chr = String.valueOf((char) ((y << 4 | x) | offset));
			g.drawString(chr, x * fontWidth, y * fontHeight + fontMetrics.getAscent());
		}
		glBindTexture(GL_TEXTURE_2D, textureId);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textureWidth, textureHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, imageToBuffer(img));
		return textureId;
	}
	
	private int getOrGenerateCharTexture(int id) {
		if(textures[id] == -1)
			return textures[id] = generateCharTexture(id);
		return textures[id];
	}
	
	/**
	 * 由于某些显卡不能很好的支持分辨率不是2的n次方的纹理，此处用于缩放到支持的范围内
	 */
	private int resizeToOpenGLSupportResolution(int size) {
		int power = 0;
		while(size > 1 << power) power++;
		return 1 << power;
	}

	private void generateCharWidthMap(int id) {
		int offset = id << 8;
		byte[] widthmap = new byte[256];
		for (int i = 0; i < widthmap.length; i++) {
			widthmap[i] = (byte) Math.ceil(font.getStringBounds(String.valueOf((char) (i | offset)), context).getWidth());
		}
		charwidth[id] = widthmap;
	}
	
	private byte[] getOrGenerateCharWidthMap(int id) {
		if(charwidth[id] == null)
			generateCharWidthMap(id);
		return charwidth[id];
	}
	
	private double wrapTextureCoord(int coord, int size) {
		return coord / (double) size;
	}
	
	private ByteBuffer imageToBuffer(BufferedImage img) {
		int[] arr = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
		ByteBuffer buf = ByteBuffer.allocateDirect(4 * arr.length);

		for (int i : arr) {
			buf.putInt(i << 8 | i >> 24 & 0xFF);
		}

		buf.flip();
		return buf;
	}
	
	protected void finalize() throws Throwable {
		for (int textureId : textures) {
			if(textureId != -1)
				glDeleteTextures(textureId);
		}
	}

	static {
		Font font = Font.decode("宋体");
		F16 = new FontRenderer(font.deriveFont(16F));
		for (int i = 0; i < 32; ++i) {
			int base = (i >> 3 & 1) * 85;
			int r = (i >> 2 & 1) * 170 + base;
			int g = (i >> 1 & 1) * 170 + base;
			int b = (i >> 0 & 1) * 170 + base;
			if (i == 6) {
				r += 85;
			}

			if (i >= 16) {
				r /= 4;
				g /= 4;
				b /= 4;
			}

			colorCode[i] = (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
        }
	}
}
