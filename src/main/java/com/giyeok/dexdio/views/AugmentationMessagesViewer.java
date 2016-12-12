package com.giyeok.dexdio.views;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.giyeok.dexdio.MainView;
import com.giyeok.dexdio.augmentation.Logger;
import com.giyeok.dexdio.model.DexProgram;
import com.giyeok.dexdio.widgets.GroupedLabelListWidget;
import com.giyeok.dexdio.widgets.Label;

public class AugmentationMessagesViewer extends GroupedLabelListWidget {
	
	private MainView mainView;
	private Logger messages;
	private ArrayList<Label> labels;
	private long lastVersion;
	
	public AugmentationMessagesViewer(MainView mainView, Composite parent, DexProgram program) {
		super(parent, SWT.NONE, "Augmentation Messages");
		
		this.mainView = mainView;
		messages = Logger.get(program);
		lastVersion = -1;
		update();
	}
	
	@Override
	public Label getItem(int index) {
		return labels.get(index);
	}
	
	public void update() {
		if (messages.getVersion() != lastVersion) {
			lastVersion = messages.getVersion();
			labels = messages.getLabels(mainView);
			setListSize(labels.size());
		}
	}
}
