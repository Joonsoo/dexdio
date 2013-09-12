package com.giyeok.dexdio.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;

public abstract class GroupedLabelListWidget extends LabelListWidget implements ListWidgetClickedListener {

	public GroupedLabelListWidget(Composite parent, int style, String title) {
		super(parent, style, title);
		
		addListClickedListener(this);
		addKeyListener(new DefaultKeyListener());
		
		itemGroups = new ArrayList<ItemGroup>();
	}
	
	public class ItemGroup {
		private int top, bottom, start, end;
		private ItemGroupSelectionListener clickListener;
		
		public ItemGroup(int single) {
			this(single, null);
		}
		
		public ItemGroup(int single, ItemGroupSelectionListener clickListener) {
			this(single, single + 1, single, single + 1, clickListener);
		}
		
		public ItemGroup(int top, int start, int end) {
			this(top, start, end, null);
		}
		
		public ItemGroup(int top, int start, int end, ItemGroupSelectionListener clickListener) {
			this(top, end, start, end, clickListener);
		}
		
		public ItemGroup(int top, int bottom, int start, int end) {
			this(top, bottom, start, end, null);
		}
		
		public ItemGroup(int top, int bottom, int start, int end, ItemGroupSelectionListener clickListener) {
			this.top = top;
			this.bottom = bottom;
			this.start = start;
			this.end = end;
			this.clickListener = clickListener;
		}
		
		protected void selected() {
			if (clickListener != null) {
				clickListener.selected();
			}
		}
		
		protected void deselected() {
			if (clickListener != null) {
				clickListener.deselected();
			}
		}
		
		protected void deselectedToMove() {
			if (clickListener != null) {
				clickListener.deselectedToMove();
			}
		}
	}
	
	public interface ItemGroupSelectionListener {
		public void selected();
		public void deselected();
		public void deselectedToMove();
	}
	
	private ArrayList<ItemGroup> itemGroups;
	private ItemGroup highlightedItemGroup = null;
	private boolean itemGroupsSorted = false;
	
	public void clearItemGroups() {
		itemGroups.clear();
		itemGroupsSorted = false;
		highlightedItemGroup = null;
		clearHighlights();
	}
	
	public void addItemGroup(ItemGroup group) {
		itemGroups.add(group);
		itemGroupsSorted = false;
	}

	@Override
	public void itemClicked(ListWidget widget, int index, int x, int y,
			MouseEvent e) {
		if (widget.isHighlighted(index)) {
			if (highlightedItemGroup != null) {
				highlightedItemGroup.deselected();
				highlightedItemGroup = null;
			}
			widget.clearHighlights();
		} else {
			for (ItemGroup itemgroup: itemGroups) {
				if (itemgroup.start <= index && index < itemgroup.end) {
					if (highlightedItemGroup == itemgroup) {
						if (highlightedItemGroup != null) {
							highlightedItemGroup.deselected();
							highlightedItemGroup = null;
							widget.clearHighlights();
						}
					} else {
						if (highlightedItemGroup != null) {
							highlightedItemGroup.deselectedToMove();
							highlightedItemGroup = null;
							widget.clearHighlights();
						}
						highlightItemGroup(itemgroup);
						highlightedItemGroup = itemgroup;
						itemgroup.selected();
					}
					break;
				}
			}
		}
	}

	@Override
	public void itemDoubleClicked(ListWidget widget, int index, int x, int y,
			MouseEvent e) {
		// nothing to do
	}
	
	protected void highlightItemGroup(ItemGroup itemgroup) {
		if (highlightedItemGroup != null) {
			highlightedItemGroup.deselected();
		}
		highlightedItemGroup = itemgroup;
		if (itemgroup != null) {
			for (int i = itemgroup.start; i < itemgroup.end; i++) {
				addHighlight(i);
			}
			boundScroll(itemgroup.bottom - 1);
			boundScroll(itemgroup.top);
		}
	}
	
	public class DefaultKeyListener implements KeyListener {
		
		@Override
		public void keyPressed(KeyEvent e) {
			if (highlightedItemGroup != null) {
				switch (e.keyCode) {
				case SWT.SPACE:
					if ((e.stateMask & SWT.SHIFT) == 0) {
						moveHighlight(1);
					} else {
						moveHighlight(-1);
					}
					break;
				case SWT.ARROW_UP:
					moveHighlight(-1);
					break;
				case SWT.ARROW_DOWN:
					moveHighlight(1);
					break;
				}
			} else {
				switch (e.keyCode) {
				case SWT.SPACE:
					if ((e.stateMask & SWT.SHIFT) == 0) {
						addScrollTop(showinglines - 1);
					} else {
						addScrollTop(-(showinglines - 1));
					}
					break;
				case SWT.HOME:
					setScrollTop(0);
					break;
				case SWT.END:
					setScrollTop(listitemcount);
					break;
				case SWT.ARROW_UP:
					addScrollTop(-1);
					break;
				case SWT.ARROW_DOWN:
					addScrollTop(1);
					break;
				case SWT.PAGE_UP:
					addScrollTop(-(showinglines - 1));
					break;
				case SWT.PAGE_DOWN:
					addScrollTop(showinglines - 1);
					break;
				}
			}
		}
		
		private void moveHighlight(int k) {
			if (! itemGroupsSorted) {
				Collections.sort(itemGroups, new Comparator<ItemGroup>() {

					@Override
					public int compare(ItemGroup o1, ItemGroup o2) {
						return o1.start - o2.start;
					}
				});
				itemGroupsSorted = true;
			}
			
			int highlightedIndex = itemGroups.indexOf(highlightedItemGroup);
			if (0 <= highlightedIndex + k && highlightedIndex + k < itemGroups.size()) {
				highlightedItemGroup.deselectedToMove();
				highlightedItemGroup = itemGroups.get(highlightedIndex + k);
				clearHighlights();
				highlightItemGroup(highlightedItemGroup);
				highlightedItemGroup.selected();
			}
		}
		
		@Override
		public void keyReleased(KeyEvent e) {
			// nothing to do
		}
	}

}
