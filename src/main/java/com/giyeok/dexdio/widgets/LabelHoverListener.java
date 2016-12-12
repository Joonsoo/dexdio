package com.giyeok.dexdio.widgets;

import org.eclipse.swt.events.MouseEvent;

public interface LabelHoverListener {

	/**
	 * 라벨 위에 마우스가 호버되었을 때를 처리한다.
	 * 처리했으면 true, 처리되지 않았으면 false를 반환한다.
	 * @param widget
	 * @param index
	 * @param label
	 * @param x
	 * @param y
	 * @param e
	 * @return
	 */
	public boolean labelHovered(LabelListWidget widget, int index, Label label, int x, int y, MouseEvent e);
}
