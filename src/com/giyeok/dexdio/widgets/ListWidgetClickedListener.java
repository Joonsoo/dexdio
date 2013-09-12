package com.giyeok.dexdio.widgets;

import org.eclipse.swt.events.MouseEvent;

public interface ListWidgetClickedListener {
	public void itemClicked(ListWidget widget, int index, int x, int y, MouseEvent e);
	public void itemDoubleClicked(ListWidget widget, int index, int x, int y, MouseEvent e);
}
