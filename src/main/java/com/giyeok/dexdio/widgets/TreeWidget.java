package com.giyeok.dexdio.widgets;

import java.util.ArrayList;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;

public class TreeWidget extends ListWidget {
	private static final long serialVersionUID = -3616609797453174209L;
	
	protected ArrayList<TreeItem> treeitems;
	private ArrayList<TreeItem> flatteneditems;
	
	public TreeWidget(Composite parent, int style) {
		super(parent, style);
		
		treeitems = new ArrayList<TreeWidget.TreeItem>();
		flatteneditems = new ArrayList<TreeWidget.TreeItem>();
	}

	@Override
	public void drawItem(GC g, int index, int left, int top,
			int vcenter, int width, int height) {
		TreeItem item = findFlattenedItem(index);
		
		item.draw(g, index - item.listoffset, left, top, vcenter, width, height);
	}

	public abstract class TreeItem {
		public TreeItem() {
			collapsed = true;
		}
		
		public abstract void draw(GC g, int index, int left, int top, int vcenter, int width, int height);
		public abstract void clicked(int index, int x, int y, MouseEvent event);
		public abstract void collapsed();
		public abstract void expanded();
		
		public abstract int GetLength();
		/**
		 * GetChildren이 실행되기 전에 최소 한 번 이상은 Expanded가 호출된다.
		 * because: GetChildren은 expanded item에 대해서만 호출되는데, 트리 아이템은 처음엔 반드시 collapsed로 시작하기 때문
		 * @return
		 */
		public abstract TreeItem[] GetChildren();
		
		public void collapse() {
			collapsed = true;
			collapsed();
			updateList();
		}
		
		public void expand() {
			collapsed = false;
			expanded();
			updateList();
		}
		
		public void toggle() {
			if (collapsed)
				expand();
			else
				collapse();
		}
		
		public boolean isCollapsed() {
			return collapsed;
		}
		
		public boolean isExpanded() {
			return ! collapsed;
		}
		
		private boolean collapsed;
		private int listoffset;
	}
	
	private int updateListRecursively(TreeItem treeitem, int offset) {
		treeitem.listoffset = offset;
		flatteneditems.add(treeitem);
		offset += treeitem.GetLength();
		if (treeitem.isExpanded()) {
			TreeItem[] children;
			
			children = treeitem.GetChildren();
			for (int i = 0; i < children.length; i++) {
				offset = updateListRecursively(children[i], offset);
			}
		}
		return offset;
	}
	
	public void updateList() {
		int offset;
		
		flatteneditems.clear();
		offset = 0;
		for (int i = 0; i < treeitems.size(); i++) {
			offset = updateListRecursively(treeitems.get(i), offset);
		}
		setListSize(offset);
		redraw();
	}
	
	private int findFlattenedItemIndex(int index) {
		int l = 0, r = flatteneditems.size() - 1, mid = 0, offset, length;

		while (l <= r) {
			mid = (l + r) / 2;
			if (mid == r)
				break;
			
			offset = flatteneditems.get(mid).listoffset;
			length = flatteneditems.get(mid).GetLength();
			if (offset <= index && index < offset + length)
				break;
				
			if (index < offset)
				r = mid - 1;
			else if (offset < index)
				l = mid + 1;
		}
		
		if (mid < flatteneditems.size()) {
			return mid;
		}
		return -1;
	}
	
	public TreeItem findFlattenedItem(int index) {
		int itemindex = findFlattenedItemIndex(index);
		
		if (itemindex >= 0 && itemindex < flatteneditems.size())
			return flatteneditems.get(itemindex);
		return null;
	}
	
	@Override
	public void itemClicked(int index, int x, int y, MouseEvent e) {
		super.itemClicked(index, x, y, e);
		
		TreeItem item = findFlattenedItem(index);
		assert item != null;

		item.clicked(index - item.listoffset, x, y, e);
	}

}
