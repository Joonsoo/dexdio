package com.giyeok.dexdio.augmentation;

import java.util.ArrayList;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.events.MouseEvent;

import com.giyeok.dexdio.MainView;
import com.giyeok.dexdio.model0.DexClass;
import com.giyeok.dexdio.model0.DexField;
import com.giyeok.dexdio.model0.DexMethod;
import com.giyeok.dexdio.model0.DexProgram;
import com.giyeok.dexdio.model0.DexType;
import com.giyeok.dexdio.widgets.Label;
import com.giyeok.dexdio.widgets.LabelClickListener;
import com.giyeok.dexdio.widgets.LabelListWidget;

public class Logger extends Augmentation {

	public static Logger get(DexProgram program) {
		return (Logger) Augmentation.getAugmentation(Logger.class, program);
	}

	@Override
	protected String getAugmentationName() {
		return "Augmentation messages";
	}
	
	private ArrayList<Log> messages;
	private long version;

	public Logger(DexProgram program) {
		super(program);
		
		messages = new ArrayList<Log>();
		version = 0;
	}
	
	public static void addMessage(Augmentation augmentation, Log message) {
		message.messageFrom = augmentation;
		Logger.get(augmentation.getProgram()).addMessage(message);
	}
	
	public static void addMessage(DexProgram program, Log message) {
		message.messageFrom = null;
		Logger.get(program).addMessage(message);
	}
	
	public void addMessage(Log message) {
		messages.add(message);
		version++;
	}
	
	public long getVersion() {
		return version;
	}
	
	public Log[] getMessages() {
		return messages.toArray(new Log[0]);
	}
	
	public ArrayList<Label> getLabels(MainView mainView) {
		ArrayList<Label> labels = new ArrayList<Label>();
		
		for (int i = 0; i < messages.size(); i++) {
			if (i == 0 || messages.get(i - 1).messageFrom != messages.get(i).messageFrom) {
				if (messages.get(i).messageFrom != null) {
					labels.add(Label.newLabel("== Message from " + messages.get(i).messageFrom.getAugmentationName()));
				}
			}
			labels.add(messages.get(i).getLabel(mainView));
		}
		return labels;
	}
	
	public static abstract class Log {
		public static Log log(DexClass dexclass) {
			return new MsgClass(dexclass);
		}
		
		public static Log log(DexType type) {
			if (type instanceof DexClass) {
				return new MsgClass((DexClass) type);
			} else {
				return new MsgText(type.getTypeFullNameBeauty());
			}
		}
		
		public static Log log(DexField field) {
			return new MsgField(field);
		}

		public static Log log(DexMethod method) {
			return new MsgMethod(method);
		}
		
		public static Log log(String text) {
			return new MsgText(text);
		}
		
		public static Log line(Log items[]) {
			return new MsgLine(items);
		}
		
		private static class ClassOpenListener implements LabelClickListener {
			private MainView mainView;
			private DexClass dexclass;
			
			public ClassOpenListener(MainView mainView, DexClass dexclass) {
				this.mainView = mainView;
				this.dexclass = dexclass;
			}
			
			@Override
			public boolean labelDoubleClicked(LabelListWidget widget, int index,
					Label label, int x, int y, MouseEvent e) {
				mainView.openClassDetail(dexclass);
				return false;
			}
			
			@Override
			public boolean labelClicked(LabelListWidget widget, int index, Label label,
					int x, int y, MouseEvent e) {
				// nothing to do
				return false;
			}
		}
		
		private static class MsgClass extends Log {
			private DexClass dexclass;
			
			public MsgClass(DexClass dexclass) {
				this.dexclass = dexclass;
			}

			@Override
			public Label getLabel(MainView mainView) {
				Label label = Label.newLabel(dexclass.getTypeFullNameBeauty());
				label.addClickListener(new ClassOpenListener(mainView, dexclass));
				return label;
			}
		}
				
		private static class MsgField extends Log {
			private DexField field;
			
			public MsgField(DexField field) {
				this.field = field;
			}

			@Override
			public Label getLabel(final MainView mainView) {
				Label classlabel, fieldlabel;
				
				classlabel = Label.newLabel(field.getBelongedClass().getTypeFullNameBeauty());
				classlabel.addClickListener(new ClassOpenListener(mainView, field.getBelongedClass()));
				fieldlabel = Label.newLabel(field.getName(), ColorConstants.darkBlue);
				fieldlabel.addClickListener(new LabelClickListener() {
					
					@Override
					public boolean labelDoubleClicked(LabelListWidget widget, int index,
							Label label, int x, int y, MouseEvent e) {
						mainView.openFieldDetail(field);
						return false;
					}
					
					@Override
					public boolean labelClicked(LabelListWidget widget, int index, Label label,
							int x, int y, MouseEvent e) {
						// nothing to do
						return false;
					}
				});
				return Label.newLabel(new Label[] { classlabel, Label.newLabel("."), fieldlabel });
			}
		}
		
		private static class MsgMethod extends Log {
			private DexMethod method;
			
			public MsgMethod(DexMethod method) {
				this.method = method;
			}

			@Override
			public Label getLabel(final MainView mainView) {
				Label classlabel, methodlabel;
				
				classlabel = Label.newLabel(method.getBelongedType().getTypeFullNameBeauty());
				if (method.getBelongedType() instanceof DexClass) {
					classlabel.addClickListener(new ClassOpenListener(mainView, (DexClass) method.getBelongedType()));
				}
				methodlabel = Label.newLabel(method.getName(), ColorConstants.darkGreen);
				methodlabel.addClickListener(new LabelClickListener() {
					
					@Override
					public boolean labelDoubleClicked(LabelListWidget widget, int index,
							Label label, int x, int y, MouseEvent e) {
						mainView.openMethodDetail(method);
						return false;
					}
					
					@Override
					public boolean labelClicked(LabelListWidget widget, int index, Label label,
							int x, int y, MouseEvent e) {
						// nothing to do
						return false;
					}
				});
				return Label.newLabel(new Label[] { classlabel, Label.newLabel("."), methodlabel });
			}
		}
		
		private static class MsgText extends Log {
			private String text;
			
			public MsgText(String text) {
				this.text = text;
			}

			@Override
			public Label getLabel(MainView mainView) {
				return Label.newLabel(text);
			}
		}
		
		private static class MsgLine extends Log {
			private Log items[];
			
			public MsgLine(Log items[]) {
				this.items = items;
			}

			@Override
			public Label getLabel(MainView mainView) {
				Label labels[] = new Label[items.length];
				for (int i = 0; i < items.length; i++) {
					labels[i] = items[i].getLabel(mainView);
				}
				return Label.newLabel(labels);
			}
		}
		
		public abstract Label getLabel(MainView mainView);
		
		private Augmentation messageFrom;
	}
}
