package com.giyeok.dexdio.views.classdetailview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;

import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer;
import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer.ControlFlowAnalysis;
import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer.ControlFlowAnalysis.BasicBlock;
import com.giyeok.dexdio.augmentation.ControlFlowAnalyzer.ControlFlowAnalysis.Edge;
import com.giyeok.dexdio.model.DexCodeItem;
import com.giyeok.dexdio.model.DexType;
import com.giyeok.dexdio.model.insns.DexInstFillArrayData;
import com.giyeok.dexdio.model.insns.DexInstSwitch;
import com.giyeok.dexdio.model.insns.DexInstruction;
import com.giyeok.dexdio.util.Pair;
import com.giyeok.dexdio.widgets.TextColumnListWidget;

class DetailOfInstructionView {
	private Composite composite;
	private Label label;
	
	private Composite contents;
	private StackLayout contentSelector;
	
	private TextColumnListWidget arrayDataTable;
	private SwitchTableWidget switchTable;
	
	private Composite cfgComposite;
	private StackLayout cfgSelector;
	
	private DexClassDetailViewer controller;

	public DetailOfInstructionView(Composite parent, int style, DexClassDetailViewer controller) {
		composite = new Composite(parent, style);
		
		composite.setLayout(new Layout() {
			private Point labelSize = null;
			
			@Override
			protected void layout(Composite composite, boolean flushCache) {
				Point compositeSize = composite.getSize();
				
				if (labelSize == null || flushCache) {
					labelSize = label.computeSize(compositeSize.x, SWT.DEFAULT, true);
				}
				
				label.setLocation(0, 0);
				label.setSize(compositeSize.x, labelSize.y);

				contents.setLocation(0, labelSize.y);
				contents.setSize(compositeSize.x, compositeSize.y - labelSize.y);
			}
			
			@Override
			protected Point computeSize(Composite arg0, int arg1, int arg2, boolean arg3) {
				return null;
			}
		});

		label = new Label(composite, SWT.NONE);
		label.setText("Instruction detail");
		
		contents = new Composite(composite, SWT.NONE);
		contentSelector = new StackLayout();
		contents.setLayout(contentSelector);
		
		arrayDataTable = new TextColumnListWidget(contents, SWT.NONE, new String[] { "Value" }, 0) {
			private DexInstFillArrayData instruction;
			
			@Override
			public String[] getItem(int index) {
				return null;
			}
		};
		
		switchTable = new SwitchTableWidget(contents, SWT.NONE);
		
		cfgComposite = new Composite(contents, SWT.NONE);
		cfgSelector = new StackLayout();
		cfgComposite.setLayout(cfgSelector);
		
		this.controller = controller;
	}
	
	private class SwitchTableWidget extends TextColumnListWidget {
		private DexInstSwitch instruction;
		private ArrayList<Pair<Integer, Integer>> table;
		
		public SwitchTableWidget(Composite parent, int style) {
			super(parent, style, new String[] { "Value", "Target" }, 0);
		}
		
		public void setInstruction(DexInstSwitch instruction) {
			this.instruction = instruction;
			this.table = instruction.getSwitchTable().getTable();
			
			setListSize(table.size());
		}

		@Override
		public String[] getItem(int index) {
			return new String[] { String.valueOf(table.get(index).getKey()), 
					Integer.toHexString(instruction.getAddress() + table.get(index).getValue()) };
		}
	}

	public Control getControl() {
		return composite;
	}
	
	private Map<DexCodeItem, Graph> graphCache = new HashMap<DexCodeItem, Graph>();
	
	public void showInstruction(DexInstruction instruction) {
		label.setText("Detail on instruction " + instruction.getAddress());
		
		switch (instruction.getInstructionType()) {
		case FILL_ARRAY_DATA:
			contentSelector.topControl = arrayDataTable;
			break;
		case SWITCH:
			switchTable.setInstruction((DexInstSwitch) instruction);
			contentSelector.topControl = switchTable;
			break;
		default:
			DexCodeItem codeitem = instruction.getCodeItem();
			Graph graph = graphCache.get(codeitem);
			if (graph == null) {
				ControlFlowAnalyzer controlflow = ControlFlowAnalyzer.get(codeitem.getProgram());
				ControlFlowAnalysis cfg = controlflow.getControlFlowForMethod(codeitem);
				Map<BasicBlock, GraphNode> nodes;
				
				graph = new Graph(cfgComposite, SWT.NONE);
				graph.setNodeStyle(ZestStyles.NODES_NO_ANIMATION);
				
				nodes = new HashMap<ControlFlowAnalyzer.ControlFlowAnalysis.BasicBlock, GraphNode>();
				for (BasicBlock bb: cfg.getBlocks()) {
					GraphNode node = new GraphNode(graph, SWT.NONE, bb.getName());
					node.setTooltip(new org.eclipse.draw2d.Label(Integer.toHexString(bb.getFirstInstruction().getAddress()) + "-" + Integer.toHexString(bb.getLastInstruction().getAddress())));
					nodes.put(bb, node);
				}
				for (Edge edge: cfg.getEdges()) {
					BasicBlock src = edge.getSource();
					BasicBlock dst = edge.getDestination();
					
					if (! edge.isHandlerEdge()) {
						new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, nodes.get(src), nodes.get(dst));
					} else {
						GraphConnection connection = new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, nodes.get(src), nodes.get(dst));
						
						connection.setLineColor(ColorConstants.red);
						
						DexType exceptionType = edge.getExceptionType();
						if (exceptionType == null) {
							connection.setTooltip(new org.eclipse.draw2d.Label("<any>"));
						} else {
							connection.setTooltip(new org.eclipse.draw2d.Label(exceptionType.getTypeShortNameBeauty()));
						}
					}
				}
				
				graph.setLayoutAlgorithm(new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
				
				graphCache.put(codeitem, graph);
			}
			
			cfgSelector.topControl = graph;
			cfgComposite.layout();
			contentSelector.topControl = cfgComposite;
			break;
		}
		contents.layout();
	}
}
