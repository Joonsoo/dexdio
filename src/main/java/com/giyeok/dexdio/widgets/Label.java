package com.giyeok.dexdio.widgets;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

public abstract class Label {
	protected int lastLeft, lastTop;
	
	public static class ImagesLabel extends Label implements LabelDrawer {
		private Image[] images;
		
		private int prevHeight;
		private Point[] sizes;
		
		private int width;

		public ImagesLabel(Image[] images) {
			this.images = images;
			
			this.prevHeight = -1;
			this.sizes = null;
			
			setLabelDrawer(this);
		}

		@Override
		public int drawLabel(GC g, int left, int top, int vcenter, int height) {
			if (sizes == null || prevHeight != height) {
				sizes = new Point[images.length];
				for (int i = 0; i < sizes.length; i++) {
					ImageData id = images[i].getImageData();
					if (id.height < height) {
						sizes[i] = new Point(id.width, id.height);
					} else {
						sizes[i] = new Point(id.width * height / id.height, height);
					}
				}
				prevHeight = height;
			}
			width = 0;
			for (int i = 0; i < images.length; i++) {
				ImageData id = images[i].getImageData();
				g.drawImage(images[i], 0, 0, id.x, id.y, left, top, sizes[i].x, sizes[i].y);
				width = Math.max(width, sizes[i].x);
			}
			return width;
		}
	}
	
	public static class TextLabel extends Label implements LabelDrawer {
		private String text;
		private Color color;
		
		private Point extent;

		public TextLabel(String text, Color color) {
			this.text = text;
			this.color = color;
			
			this.extent = null;
			
			setLabelDrawer(this);
		}
		
		public Color getColor() {
			return color;
		}
		
		public void setColor(Color color) {
			this.color = color;
		}

		@Override
		public int drawLabel(GC g, int left, int top, int vcenter, int height) {
			if (extent == null) {
				extent = g.stringExtent(text);
			}
			g.setForeground(color);
			g.drawString(text, left, vcenter, true);
			return extent.x;
		}
	}
	
	public static class FlowContainerLabel extends Label implements LabelDrawer {
		private Label[] labels;
		
		private int[] widths;

		public FlowContainerLabel(Label[] labels) {
			this.labels = labels;
			
			this.widths = new int[labels.length];
			
			setLabelDrawer(this);
		}

		@Override
		public int drawLabel(GC g, int left, int top, int vcenter, int height) {
			int width, w;
			
			width = 0;
			for (int i = 0; i < labels.length; i++) {
				w = labels[i].draw(g, left + width, top, vcenter, height);
				width += w;
				widths[i] = width;
			}
			return width;
		}

		private Label findLabelAtPoint(int x) {
			x -= lastLeft;
			if (x < widths[0]) {
				return labels[0];
			}
			for (int i = 1; i < widths.length; i++) {
				if (x < widths[i]) {
					return labels[i];
				}
			}
			return null;
		}

		@Override
		public boolean fireClicked(LabelListWidget widget, int index,
				int x, int y, MouseEvent e) {
			Label at = findLabelAtPoint(x);
			boolean result = false;
			
			if (at != null) {
				result = at.fireClicked(widget, index, x, y, e);
			}
			if (at == null || ! result) {
				result = false;
				for (LabelClickListener l: clickListeners) {
					result |= l.labelClicked(widget, index, this, x, y, e);
				}
			}
			return result;
		}

		@Override
		public boolean fireDoubleClicked(LabelListWidget widget,
				int index, int x, int y, MouseEvent e) {
			Label at = findLabelAtPoint(x);
			boolean result = false;
			
			if (at != null) {
				result = at.fireDoubleClicked(widget, index, x, y, e);
			}
			if (at == null || ! result) {
				result = false;
				for (LabelClickListener l: clickListeners) {
					result |= l.labelDoubleClicked(widget, index, this, x, y, e);
				}
			}
			return result;
		}

		@Override
		public boolean fireHovered(LabelListWidget widget, int index,
				int x, int y, MouseEvent e) {
			Label at = findLabelAtPoint(x);
			boolean result = false;
			
			if (at != null) {
				result = at.fireHovered(widget, index, x, y, e);
			}
			if (at == null || ! result) {
				result = false;
				for (LabelHoverListener l: hoverListeners) {
					result |= l.labelHovered(widget, index, this, x, y, e);
				}
			}
			return result;
		}
	}
	
	/**
	 * draw the label using g, and returns its drawn width;
	 * @param g
	 * @return
	 */
	public final int draw(GC g, int left, int top, int vcenter, int height) {
		if (labelDrawer != null) {
			lastLeft = left;
			lastTop = top;
			return labelDrawer.drawLabel(g, left, top, vcenter, height);
		} else {
			return 0;
		}
	}
	
	private LabelDrawer labelDrawer;
	
	public void setLabelDrawer(LabelDrawer labelDrawer) {
		this.labelDrawer = labelDrawer;
	}
	
	public LabelDrawer getLabelDrawer() {
		return labelDrawer;
	}
	
	Set<LabelClickListener> clickListeners;
	Set<LabelHoverListener> hoverListeners;
	
	public void addClickListener(LabelClickListener listener) {
		clickListeners.add(listener);
	}
	
	public void addHoverListener(LabelHoverListener listener) {
		hoverListeners.add(listener);
	}
	
	protected boolean fireClicked(LabelListWidget widget, int index, int x, int y, MouseEvent e) {
		boolean result = false;
		for (LabelClickListener l: clickListeners) {
			result |= l.labelClicked(widget, index, this, x, y, e);
		}
		return result;
	}
	
	protected boolean fireDoubleClicked(LabelListWidget widget, int index, int x, int y, MouseEvent e) {
		boolean result = false;
		for (LabelClickListener l: clickListeners) {
			result |= l.labelDoubleClicked(widget, index, this, x, y, e);
		}
		return result;
	}
	
	protected boolean fireHovered(LabelListWidget widget, int index, int x, int y, MouseEvent e) {
		boolean result = false;
		for (LabelHoverListener l: hoverListeners) {
			result |= l.labelHovered(widget, index, this, x, y, e);
		}
		return result;
	}
	
	private Label() {
		clickListeners = new HashSet<LabelClickListener>();
		hoverListeners = new HashSet<LabelHoverListener>();
		
		this.labelDrawer = null;
	}
	
	/**
	 * �ϳ��� �̹����� ǥ���ϴ� ��
	 * �̹����� ���� ���� �������� ���ĵǰ�, ũ�Ⱑ ���� ũ�⺸�� ũ�� ���δ�.
	 * @param image
	 * @return
	 */
	public static Label newLabel(Image image) {
		return new ImagesLabel(new Image[] { image });
	}
	
	/**
	 * ���� ���� �̹����� ���ļ� ǥ���ϴ� ��.
	 * �̹����� ���� ���� �������� ���ĵǰ�, ũ�Ⱑ ���� ũ�⺸�� ũ�� ���δ�.
	 * @param images
	 * @return
	 */
	public static Label newLabel(Image images[]) {
		return new ImagesLabel(images);
	}
	
	/**
	 * ������ �� �ؽ�Ʈ�� ǥ���ϴ� ��.
	 * @param text
	 * @param color
	 * @return
	 */
	public static Label newLabel(String text, Color color) {
		return new TextLabel(text, color);
	}
	
	/**
	 * ������ �ؽ�Ʈ ��
	 * @param text
	 * @return
	 */
	public static Label newLabel(String text) {
		return Label.newLabel(text, ColorConstants.black);
	}
	
	/**
	 * �� ��
	 * @return
	 */
	public static Label newEmptyLabel() {
		return Label.newLabel("");
	}
	
	/**
	 * ���� ���� ���� �÷ο�� ��ġ�ϴ� ��.
	 * @param labels
	 * @return
	 */
	public static Label newLabel(Label labels[]) {
		return new FlowContainerLabel(labels);
	}
}
