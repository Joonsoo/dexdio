package com.giyeok.dexdio.widgets;

import org.eclipse.swt.events.MouseEvent;

public interface LabelClickListener {

	/**
	 * 마우스가 클릭되었을 경우를 처리한다.
	 * 처리가 완료되면 true를 반환하고, 그렇지 않으면 false를 반환한다.
	 * @param widget
	 * @param index
	 * @param label
	 * @param x
	 * @param y
	 * @param e
	 * @return
	 */
	public boolean labelClicked(LabelListWidget widget, int index, Label label, int x, int y, MouseEvent e);

	/**
	 * 마우스가 더블 클릭되었을 경우를 처리한다.
	 * 처리가 완료되면 true를 반환하고, 그렇지 않으면 false를 반환한다.
	 * @param widget
	 * @param index
	 * @param label
	 * @param x
	 * @param y
	 * @param e
	 * @return
	 */
	public boolean labelDoubleClicked(LabelListWidget widget, int index, Label label, int x, int y, MouseEvent e);
}
