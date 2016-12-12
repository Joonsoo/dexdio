package com.giyeok.dexdio.widgets;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;

public abstract class TextColumnListWidget extends ListWidget {
	private static final long serialVersionUID = 1L;
	
	private ListTitleItem[] title;
	private String[][] items;
	
	public TextColumnListWidget(Composite parent, int style, String[] columns, int listsize) {
		super(parent, style);
		
		title = new ListTitleItem[columns.length];
		
		for (int i = 0; i < columns.length; i++) {
			title[i] = new ListTitleItem(5, columns[i]);
		}
		
		setTitle(title);
		
		setListSize(listsize);
	}
	
	public abstract String[] getItem(int index);
	
	@Override
	protected void calculateFontSize(GC g) {
		super.calculateFontSize(g);
		
		if (title.length > 0) {
			int maxwidth[] = new int[title.length];
			
			items = new String[getListSize()][];
			for (int i = 0; i < title.length; i++) {
				maxwidth[i] = 0;
			}
			for (int i = 0; i < getListSize(); i++) {
				String values[] = getItem(i);
				assert values.length == title.length;
				items[i] = values;
				for (int j = 0; j < title.length - 1; j++) {
					maxwidth[j] = Math.max(maxwidth[j], g.stringExtent(values[j]).x);
				}
			}
			title[0].x = 5;
			for (int i = 1; i < title.length; i++) {
				title[i].x = title[i - 1].x + 15 + Math.max(g.stringExtent(title[i - 1].title).x, maxwidth[i - 1]);
			}
		}
	}

	@Override
	public void drawItem(GC g, int index, int left, int top,
			int vcenter, int width, int height) {
		if (items == null) {
			calculateFontSize(g);
		}
		for (int i = 0; i < title.length; i++) {
			g.drawString(items[index][i], left + title[i].x, vcenter, true);
		}
	}
	
	@Override
	public void setListSize(int listsize) {
		super.setListSize(listsize);
		items = null;
		redraw();
	}
}
