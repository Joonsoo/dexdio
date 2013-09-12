package com.giyeok.dexdio.widgets;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;

public abstract class LabelListWidget extends ListWidget {
	
	private Label[] items;
	
	private int leftMargin;

	public LabelListWidget(Composite parent, int style, String title) {
		super(parent, style);
		
		leftMargin = 5;
		setTitle(new ListTitleItem[] {
				new ListTitleItem(leftMargin, title)
		});
		
		super.addListClickedListener(new ListWidgetClickedListener() {
			
			@Override
			public void itemDoubleClicked(ListWidget widget, int index, int x, int y,
					MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void itemClicked(ListWidget widget, int index, int x, int y,
					MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	@Override
	public void drawItem(GC g, int index, int left, int top, int vcenter,
			int width, int height) {
		if (items == null) {
			items = new Label[getListSize()];
			for (int i = 0; i < getListSize(); i++) {
				items[i] = getItem(i);
			}
		}
		items[index].draw(g, left + leftMargin, top, vcenter, height);
	}
	
	public abstract Label getItem(int index);
	
	@Override
	public void setListSize(int listsize) {
		items = null;
		super.setListSize(listsize);
	}
	
	@Override
	public void mouseDown(MouseEvent e) {
		haveFocus();
		if (! mouseScrollEvent(MouseEventType.MOUSE_DOWN, e)) {
			if (e.y >= titleheight) {
				int index = (e.y - titleheight) / itemheight + scrolltop;
				if (items != null && index >= 0 && index < listitemcount) {
					if (! items[index].fireClicked(this, index, e.x, (e.y - titleheight) % itemheight, e)) {
						super.itemClicked(index, e.x, (e.y - titleheight) % itemheight, e);
						lastclicked = index;
					}
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
				if (items != null && index >= 0 && index < listitemcount) {
					if (! items[index].fireDoubleClicked(this, index, e.x - leftMargin, (e.y - titleheight) % itemheight, e)) {
						super.itemDoubleClicked(index, e.x, (e.y - titleheight) % itemheight, e);
						lastclicked = index;
					}
				}
			}
		}
	}
	
	@Override
	public void mouseMove(MouseEvent e) {
		if (! mouseScrollEvent(MouseEventType.MOUSE_MOVE, e)) {
			if (e.y >= titleheight) {
				int index = (e.y - titleheight) / itemheight + scrolltop;
				if (items != null && index >= 0 && index < listitemcount) {
					if (! items[index].fireHovered(this, index, e.x - leftMargin, (e.y - titleheight) % itemheight, e)) {
						super.itemHover(index, e.x, (e.y - titleheight) % itemheight);
					}
				}
			}
		}
	}

}
