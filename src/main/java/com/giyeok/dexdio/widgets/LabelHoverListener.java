package com.giyeok.dexdio.widgets;

import org.eclipse.swt.events.MouseEvent;

public interface LabelHoverListener {

	/**
	 * �� ���� ���콺�� ȣ���Ǿ��� ���� ó���Ѵ�.
	 * ó�������� true, ó������ �ʾ����� false�� ��ȯ�Ѵ�.
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
