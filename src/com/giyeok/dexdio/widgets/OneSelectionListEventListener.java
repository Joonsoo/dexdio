package com.giyeok.dexdio.widgets;

import org.eclipse.swt.events.MouseEvent;

public class OneSelectionListEventListener implements ListWidgetClickedListener {

	@Override
	public void itemClicked(ListWidget widget, int index, int x, int y, MouseEvent e) {
		if (widget.isHighlighted(index)) {
			widget.clearHighlights();
			widget.setMoveHighlights(false);
		} else {
			widget.clearHighlights();
			widget.addHighlight(index);
			widget.setMoveHighlights(true);
		}
	}

	@Override
	public void itemDoubleClicked(ListWidget widget, int index, int x, int y,
			MouseEvent e) {
		// nothing to do
	}
}
