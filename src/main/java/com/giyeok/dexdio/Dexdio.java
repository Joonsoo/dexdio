package com.giyeok.dexdio;

import java.io.IOException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.giyeok.dexdio.dexreader.DalvikExecutable;

public class Dexdio {
	public static void main(String args[]) {
		DalvikExecutable dex[];
		
		try {
			dex = new DalvikExecutable[] {
				DalvikExecutable.load("./samples/mysample.dex")
			};
		} catch (IOException e) {
			e.printStackTrace();
			dex = null;
		}

		Display display;
		MainView mainViews[];
		
		display = new Display();
		
		if (dex == null) {
			MessageBox msg = new MessageBox(null);
			msg.setMessage("Error while reading dex");
			msg.open();
			return;
		} else {
			mainViews = new MainView[dex.length];
			for (int i = 0; i < dex.length; i++) {
				mainViews[i] = new MainView(dex[i], new Shell(display));
			}
		}
		
		while (! allViewsDisposed(mainViews)) {
			if (! display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
		
		// is it really needed?
		System.exit(0);
	}
	
	private static boolean allViewsDisposed(MainView[] views) {
		for (int i = 0; i < views.length; i++) {
			if (! views[i].getShell().isDisposed()) {
				return false;
			}
		}
		return true;
	}
}
