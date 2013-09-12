package com.giyeok.dexdio.widgets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;


abstract public class ListWidget extends Canvas 
		implements MouseListener, MouseMoveListener, MouseWheelListener, DisposeListener, PaintListener {

	private static String DefaultFontFace = "Consolas";
	private static int DefaultFontSize = 10;
	private static int TextVerticalAlignment = 0;
	
	private Color itemBackgroundColors[];
	private Color fontcolor;
	private Font font;
	
	private ListTitleItem[] listtitle;
	protected int titleheight;
	private Color titlecolor, titlebackground;
	
	protected int listitemcount;
	protected int itemheight;
	protected int charwidth;
	
	protected int showinglines;
	protected int scrolltop;
	private Set<Integer> highlighted;
	private Color highlightcolor, highlightborder, highlightfontcolor;
	
	protected int scrollbarwidth = 10;
	protected Color scrollbarback, scrollbarhint;
	protected boolean scrollbarenabled = true, scrollbarshown = false;
	
	protected boolean movehighlights = false;
	protected int lastclicked;
	
	public ListWidget(Composite parent, int style) {
		super(parent, style | SWT.DOUBLE_BUFFERED);
		itemBackgroundColors = new Color[] { new Color(null, 234, 234, 234), new Color(null, 230, 230, 230) };
		fontcolor = new Color(null, 0, 0, 0);
		titlecolor = new Color(null, 0, 0, 0);
		titlebackground = new Color(null, 150, 150, 150);
		highlightcolor = new Color(null, 255, 255, 255);
		highlightborder = new Color(null, 255, 0, 0);
		highlightfontcolor = new Color(null, 0, 0, 255);
		scrollbarback = new Color(null, 255, 255, 255);
		scrollbarhint = new Color(null, 180, 180, 180);
		
		this.font = new Font(null, ListWidget.DefaultFontFace, ListWidget.DefaultFontSize, SWT.NONE);
		setFont(font);
		
		itemheight = -1;		// marks to recalculate the font size
		
		highlighted = new HashSet<Integer>();
		scrolltop = 0;
		listtitle = new ListTitleItem[0];
		listitemcount = 0;
		
		addPaintListener(this);
		
		addMouseListener(this);
		addMouseMoveListener(this);
		addMouseWheelListener(this);
		
		clicklisteners = new ArrayList<ListWidgetClickedListener>();
		selectlisteners = new ArrayList<ListWidgetSelectionListener>();
		hoverlisteners = new ArrayList<ListWidgetHoverListener>();
		
		addDisposeListener(this);
	}
	
	@Override
	public void widgetDisposed(DisposeEvent event) {
		for (int i = 0; i < itemBackgroundColors.length; i++) {
			itemBackgroundColors[i].dispose();
		}
		fontcolor.dispose();
		titlecolor.dispose();
		titlebackground.dispose();
		highlightcolor.dispose();
		highlightborder.dispose();
		highlightfontcolor.dispose();
		scrollbarback.dispose();
		scrollbarhint.dispose();
		
		font.dispose();
	}
	
	public void setMoveHighlights(boolean newmovehighlights) {
		movehighlights = newmovehighlights;
	}
	
	protected void moveHighlights(int amount) {
		Set<Integer> newhighlighted = new HashSet<Integer>();
		
		for (int k: highlighted) {
			int newk = k + amount;
			
			if (newk < 0) {
				newk = 0;
			} else if (newk >= listitemcount) {
				newk = listitemcount - 1;
			}
			newhighlighted.add(newk);
		}
		highlighted = newhighlighted;
		
		for (int k: highlighted) {
			itemSelected(k);
		}
		
		lastclicked += amount;
		if (lastclicked < 0)
			lastclicked = 0;
		if (lastclicked >= listitemcount)
			lastclicked = listitemcount - 1;
		boundScroll(lastclicked);
	}
	
	public int getLastClicked() {
		return lastclicked;
	}
	
	public class DefaultKeyListener implements KeyListener {
		
		@Override
		public void keyPressed(KeyEvent e) {
			if (movehighlights) {
				switch (e.keyCode) {
				case SWT.SPACE:
					if ((e.stateMask & SWT.SHIFT) == 0) {
						moveHighlights(ListWidget.this.showinglines - 1);
					} else {
						moveHighlights(-(ListWidget.this.showinglines - 1));
					}
					break;
				case SWT.HOME:
					moveHighlights(-lastclicked);
					break;
				case SWT.END:
					moveHighlights(ListWidget.this.listitemcount - lastclicked - 1);
					break;
				case SWT.ARROW_UP:
					moveHighlights(-1);
					break;
				case SWT.ARROW_DOWN:
					moveHighlights(1);
					break;
				case SWT.PAGE_UP:
					moveHighlights(-(ListWidget.this.showinglines - 1));
					break;
				case SWT.PAGE_DOWN:
					moveHighlights(ListWidget.this.showinglines - 1);
					break;
				}
			} else {
				switch (e.keyCode) {
				case SWT.SPACE:
					if ((e.stateMask & SWT.SHIFT) == 0) {
						ListWidget.this.addScrollTop(ListWidget.this.showinglines - 1);
					} else {
						ListWidget.this.addScrollTop(-(ListWidget.this.showinglines - 1));
					}
					break;
				case SWT.HOME:
					ListWidget.this.setScrollTop(0);
					break;
				case SWT.END:
					ListWidget.this.setScrollTop(ListWidget.this.listitemcount);
					break;
				case SWT.ARROW_UP:
					ListWidget.this.addScrollTop(-1);
					break;
				case SWT.ARROW_DOWN:
					ListWidget.this.addScrollTop(1);
					break;
				case SWT.PAGE_UP:
					ListWidget.this.addScrollTop(-(ListWidget.this.showinglines - 1));
					break;
				case SWT.PAGE_DOWN:
					ListWidget.this.addScrollTop(ListWidget.this.showinglines - 1);
					break;
				}
			}
		}
		
		@Override
		public void keyReleased(KeyEvent e) { }
	}

	@Override
	public void mouseDown(MouseEvent e) {
		haveFocus();
		if (! mouseScrollEvent(MouseEventType.MOUSE_DOWN, e)) {
			if (e.y >= titleheight) {
				int index = (e.y - titleheight) / itemheight + scrolltop;
				if (index >= 0 && index < listitemcount) {
					ListWidget.this.itemClicked(index, e.x, (e.y - titleheight) % itemheight, e);
					lastclicked = index;
				}
			}
		}
	}
	
	@Override
	public void mouseUp(MouseEvent e) {
		mouseScrollEvent(MouseEventType.MOUSE_UP, e);
	}
	
	@Override
	public void mouseDoubleClick(MouseEvent e) {
		haveFocus();
		if (! mouseScrollEvent(MouseEventType.MOUSE_DOWN, e)) {
			if (e.y >= titleheight) {
				int index = (e.y - titleheight) / itemheight + scrolltop;
				if (index >= 0 && index < listitemcount) {
					ListWidget.this.itemDoubleClicked(index, e.x, (e.y - titleheight) % itemheight, e);
					lastclicked = index;
				}
			}
		}
	}
	
	@Override
	public void mouseMove(MouseEvent e) {
		if (! mouseScrollEvent(MouseEventType.MOUSE_MOVE, e)) {
			if (e.y >= titleheight) {
				int index = (e.y - titleheight) / itemheight + scrolltop;
				if (index >= 0 && index < listitemcount) {
					ListWidget.this.itemHover(index, e.x, (e.y - titleheight) % itemheight);
				}
			}
		}
	}

	@Override
	public void mouseScrolled(MouseEvent e) {
		haveFocus();
		ListWidget.this.addScrollTop(- e.count * 2);
	}
	
	protected void haveFocus() {
		forceFocus();
	}
	
	protected void calculateFontSize(GC g) {
		String standard = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890.~`!\\|!@#$%^&*()_+-=,./<>?";
		
		Point charsize;
		
		itemheight = 0;
		charwidth = 0;
		for (int i = 0; i < standard.length(); i++) {
			charsize = g.stringExtent("" + standard.charAt(i));
			if (charsize.x > charwidth) {
				charwidth = charsize.x;
			}
			itemheight = Math.max(itemheight, charsize.y);
		}
		itemheight += 1;
		titleheight = itemheight;
	}
	
	protected void setTitle(ListTitleItem[] listtitle) {
		this.listtitle = listtitle;
	}
	
	protected ListTitleItem[] getTitleItems() {
		return listtitle;
	}
	
	public void setListSize(int listitemcount) {
		this.listitemcount = listitemcount;
		redraw();
	}
	
	public int getListSize() {
		return listitemcount;
	}
	
	protected int getScrollTop() {
		return scrolltop;
	}
	
	protected void setScrollTop(int newscrolltop) {
		int oldscrolltop = scrolltop;
		scrolltop = newscrolltop;
		boundScroll();
		if (oldscrolltop != scrolltop) {
			redraw();
		}
	}
	
	protected void addScrollTop(int scrollamount) {
		int oldscrolltop = scrolltop;
		scrolltop += scrollamount;
		boundScroll();
		if (oldscrolltop != scrolltop) {
			redraw();
		}
	}
	
	protected void boundScroll() {
		if (scrolltop + showinglines > listitemcount)
			scrolltop = listitemcount - showinglines;
		if (scrolltop < 0)
			scrolltop = 0;
	}
	
	public void boundScroll(int index) {
		// index가 화면에 나타나도록 스크롤을 바운드
		if (index < scrolltop) {
			scrolltop = index;
		} else if (index >= scrolltop + showinglines) {
			scrolltop = index - showinglines + 1;
		}
		boundScroll();
		redraw();
	}
	
	@Override
	public void paintControl(PaintEvent e) {
		GC g = e.gc;
		Rectangle area = getBounds();
		
		if (itemheight <= 0) {
			calculateFontSize(g);
		}
		showinglines = (area.height - titleheight) / itemheight;
		boundScroll();
		if (scrolltop + showinglines >= listitemcount) {
			showinglines = listitemcount - scrolltop;
		}
		drawTitle(g, 0, 0, area.width, titleheight);
		if (showinglines < listitemcount && scrollbarenabled) {
			drawList(g, 0, 0 + titleheight, area.width - scrollbarwidth, area.height - titleheight);
			drawScrollBar(g, area.width - scrollbarwidth, 0 + titleheight, scrollbarwidth, area.height - titleheight);
			scrollbarshown = true;
		} else {
			drawList(g, 0, 0 + titleheight, area.width, area.height - titleheight);
			scrollbarshown = false;
		}
	}
	
	private void drawTitle(GC g, int left, int top, int width, int height) {
		g.setBackground(titlebackground);
		g.fillRectangle(left, top, width, height);
		g.setForeground(titlecolor);
		for (ListTitleItem title: listtitle) {
			g.drawString(title.title, title.x, top + TextVerticalAlignment, true);
		}
	}
	
	private void drawList(GC g, int left, int top, int width, int height) {
		int index, y;
		
		for (int i = 0; i < showinglines; i++) {
			index = i + scrolltop;
			y = 1 + top + i * itemheight;
			g.setBackground(itemBackgroundColors[index % itemBackgroundColors.length]);
			g.fillRectangle(left, y, width, itemheight);
			if (highlighted.contains(index)) {
				g.setForeground(highlightborder);
				if (index == 0 || ! highlighted.contains(index-1)) {
					g.drawLine(left, y - 1, left + width, y - 1);
				}
				g.setBackground(highlightcolor);
				g.fillRectangle(left, y, width, itemheight);
				g.setForeground(highlightborder);
				if (index == listitemcount - 1 || ! highlighted.contains(index+1)) {
					g.drawLine(left, y + itemheight - 1, left + width, y + itemheight - 1);
				}
				g.drawLine(left, y - 1, left, y + itemheight - 1);
				g.drawLine(left + width - 1, y - 1, left + width - 1, y + itemheight - 1);
			}
		}
		for (int i = 0; i < showinglines; i++) {
			index = i + scrolltop;
			y = 1 + top + i * itemheight;
			assert 0 <= index && index < listitemcount;
			if (highlighted.contains(index)) {
				g.setForeground(highlightfontcolor);
			} else {
				g.setForeground(fontcolor);
			}
			drawItem(g, index, left, y, y + TextVerticalAlignment, width, itemheight);
		}
		y = 1 + showinglines * itemheight;
		g.setBackground(itemBackgroundColors[(showinglines + scrolltop) % itemBackgroundColors.length]);
		g.fillRectangle(left, top + y, width, height - y);
	}
	
	private boolean scrollbarmoving = false;
	private int scrollbarheight = 0, scrollbarhintheight = 0;
	
	private int fromYtoScrolltop(int y) {
		if (scrollbarheight <= 0)
			return 0;
		
		return ((y - titleheight - scrollbarhintheight / 2) * (listitemcount)) / (scrollbarheight);
	}
	
	enum MouseEventType {
		MOUSE_DOWN,
		MOUSE_UP,
		MOUSE_MOVE
	}
	
	protected boolean mouseScrollEvent(MouseEventType type, MouseEvent e) {
		if (scrollbarshown) {
			int width = getSize().x;
			boolean inscrollbar = ((e.y >= titleheight) && (width - scrollbarwidth <= e.x && e.x < width));
			
			if (type == MouseEventType.MOUSE_MOVE) {
				if (scrollbarmoving) {
					setScrollTop(fromYtoScrolltop(e.y));
					return true;
				} else {
					return inscrollbar;
				}
			}
			if (type == MouseEventType.MOUSE_UP && scrollbarmoving) {
				scrollbarmoving = false;
				return true;
			}
			if (inscrollbar) {
				if (type == MouseEventType.MOUSE_DOWN) {
					scrollbarmoving = true;
					setScrollTop(fromYtoScrolltop(e.y));
				}
				return true;
			}
		}
		return false;
	}
	
	private void drawScrollBar(GC g, int left, int top, int width, int height) {
		assert (scrolltop + showinglines) <= listitemcount;
		
		g.setBackground(scrollbarback);
		scrollbarheight = height;
		g.fillRectangle(left, top, width, scrollbarheight);
		
		g.setBackground(scrollbarhint);
		scrollbarhintheight = (showinglines * height) / listitemcount + 1;
		g.fillRectangle(left, top + (scrolltop * height) / listitemcount, width, scrollbarhintheight);
	}
	
	public abstract void drawItem(GC g, int index, int left, int top, int vcenter, int width, int height);
	
	private ArrayList<ListWidgetClickedListener> clicklisteners;
	private ArrayList<ListWidgetSelectionListener> selectlisteners;
	private ArrayList<ListWidgetHoverListener> hoverlisteners;
	
	public void addListClickedListener(ListWidgetClickedListener listener) {
		clicklisteners.add(listener);
	}
	
	public void addListSelectionListener(ListWidgetSelectionListener listener) {
		selectlisteners.add(listener);
	}
	
	public void addListHoverListener(ListWidgetHoverListener listener) {
		hoverlisteners.add(listener);
	}
	
	protected void itemClicked(int index, int x, int y, MouseEvent e) {
		for(ListWidgetClickedListener l : clicklisteners) {
			l.itemClicked(this, index, x, y, e);
		}
		itemSelected(index);
	}
	
	protected void itemDoubleClicked(int index, int x, int y, MouseEvent e) {
		for(ListWidgetClickedListener l : clicklisteners) {
			l.itemDoubleClicked(this, index, x, y, e);
		}
	}
	
	protected void itemSelected(int index) {
		for(ListWidgetSelectionListener l : selectlisteners) {
			l.itemSelected(this, index);
		}
	}
	
	protected void itemHover(int index, int x, int y) {
		for(ListWidgetHoverListener l : hoverlisteners) {
			l.itemHover(this, index, x, y);
		}
	}
	
	public void clearHighlights() {
		if (! highlighted.isEmpty()) {
			highlighted.clear();
			redraw();
		}
	}
	
	public int getHighlightsCount() {
		return highlighted.size();
	}
	
	public boolean isHighlighted(int index) {
		return highlighted.contains(index);
	}
	
	public Integer[] getHighlightedList() {
		return highlighted.toArray(new Integer[0]);
	}
	
	public void addHighlight(int index) {
		if (! highlighted.contains(index)) {
			highlighted.add(index);
			redraw();
		}
	}
	
	public void delHighlight(int index) {
		if (highlighted.contains(index)) {
			highlighted.remove(index);
			redraw();
		}
	}
	
	public void toggleHighlight(int index) {
		if (! highlighted.contains(index)) {
			addHighlight(index);
		} else {
			delHighlight(index);
		}
	}
	
	public static int getDigitLength(int number) {
		int length = 1;
		
		number /= 10;
		while (number > 0) {
			length++;
			number /= 10;
		}
		return length;
	}
	
	// Returns max(GetDigitLength(number), minlen)
	public static int getDigitLength(int number, int minlen) {
		int result;
		
		result = getDigitLength(number);
		return (result < minlen)? minlen:result;
	}
	
	public interface Lambda1 {
		public int calc(int index);
	}
	
	public int getMaxAmong(Lambda1 f) {
		return getMaxAmong(f, Integer.MIN_VALUE);
	}
	
	public int getMaxAmong(Lambda1 f, int minvalue) {
		int max = minvalue;
		
		for (int i = 0; i < getListSize(); i++) {
			max = Math.max(max, f.calc(i));
		}
		return max;
	}
}
