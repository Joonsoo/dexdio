package com.giyeok.dexdio.widgets;

import org.eclipse.swt.events.MouseEvent;

public interface LabelClickListener {

	/**
	 * ���콺�� Ŭ���Ǿ��� ��츦 ó���Ѵ�.
	 * ó���� �Ϸ�Ǹ� true�� ��ȯ�ϰ�, �׷��� ������ false�� ��ȯ�Ѵ�.
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
	 * ���콺�� ���� Ŭ���Ǿ��� ��츦 ó���Ѵ�.
	 * ó���� �Ϸ�Ǹ� true�� ��ȯ�ϰ�, �׷��� ������ false�� ��ȯ�Ѵ�.
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
