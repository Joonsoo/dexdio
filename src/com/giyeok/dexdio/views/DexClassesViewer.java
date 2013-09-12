package com.giyeok.dexdio.views;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.DirectedGraph;
import org.eclipse.draw2d.graph.DirectedGraphLayout;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.EdgeList;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.NodeList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.zest.core.widgets.GraphNode;

import com.giyeok.dexdio.MainView;
import com.giyeok.dexdio.model.DexAnnotations;
import com.giyeok.dexdio.model.DexClass;
import com.giyeok.dexdio.model.DexField;
import com.giyeok.dexdio.model.DexInternalClass;
import com.giyeok.dexdio.model.DexMethod;
import com.giyeok.dexdio.model.DexProgram;
import com.giyeok.dexdio.model.DexType;
import com.giyeok.dexdio.widgets.ListWidget;
import com.giyeok.dexdio.widgets.OneSelectionListEventListener;
import com.giyeok.dexdio.widgets.TextColumnListWidget;

public class DexClassesViewer {
	private MainView mainView;
	private DexProgram program;
	
	private TabFolder tabFolder;
	private DexClassesList list;
	private DexClassesDiagram diagram;
	
	public DexClassesViewer(MainView mainView, Composite parent, DexProgram program) {
		this.mainView = mainView;
		this.program = program;
		
		tabFolder = new TabFolder(parent, SWT.NONE);
		
		TabItem item;
		
		list = new DexClassesList(tabFolder, SWT.NONE);
		item = new TabItem(tabFolder, SWT.NONE);
		item.setText("List");
		item.setControl(list);

		diagram = new DexClassesDiagram(tabFolder, SWT.NONE);
		item = new TabItem(tabFolder, SWT.NONE);
		item.setText("Diagram");
		item.setControl(diagram.getControl());
	}
	
	public TabFolder getContentControl() {
		return tabFolder;
	}

	class DexClassesList extends TextColumnListWidget {
		public DexClassesList(Composite parent, int style) {
			super(parent, style, new String[] {"id", "name"}, program.getTypesCount());
			
			addListClickedListener(new OneSelectionListEventListener() {
				
				@Override
				public void itemDoubleClicked(ListWidget widget, int index, int x, int y,
						MouseEvent e) {
					DexType type = program.getTypeByTypeId(index);
					if (type instanceof DexClass) {
						mainView.openClassDetail((DexClass) type);
					}
				}
			});
			addKeyListener(new DefaultKeyListener());
		}

		@Override
		public String[] getItem(int index) {
			DexType type = program.getTypeByTypeId(index);
			assert index == type.getTypeId();
			return new String[] {"" + index, type.getTypeName()};
		}
		
		@Override
		public void drawItem(GC g, int index, int left, int top,
				int vcenter, int width, int height) {
			if (! isHighlighted(index)) {
				DexType type = program.getTypeByTypeId(index);
				if (type instanceof DexInternalClass) {
					g.setForeground(ColorConstants.black);
				} else if (type instanceof DexClass) {
					g.setForeground(ColorConstants.lightGray);
				} else {
					g.setForeground(ColorConstants.white);
				}
			}
			super.drawItem(g, index, left, top, vcenter, width, height);
		}
	}
	
	interface Adding {
		public GraphNode add(DexType type);
	}

	class DexClassesDiagram {
		
		class InternalItemFigure extends Figure {
			InternalItemFigure() {
				ToolbarLayout layout = new ToolbarLayout();
				layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
				layout.setStretchMinorAxis(false);
				layout.setSpacing(2);
				setLayoutManager(layout);
				setBorder(new InternalBorder());
			}
			
			class InternalBorder extends AbstractBorder {

				@Override
				public Insets getInsets(IFigure figure) {
					return new Insets(1, 0, 0, 0);
				}

				@Override
				public void paint(IFigure figure, Graphics graphics, Insets insets) {
					graphics.drawLine(getPaintRectangle(figure, insets).getTopLeft(), tempRect.getTopRight());
				}
			}
		}
		
		class ClassFigure extends Figure {
			public final Color classColor = new Color(null, 255, 255, 206);
			private InternalItemFigure attributes;
			private InternalItemFigure methods;

			private DexClass type;
			
			ClassFigure(final DexClass type) {
				this.type = type;
				
				ToolbarLayout layout = new ToolbarLayout();
				setLayoutManager(layout);
				setBorder(new LineBorder(ColorConstants.black, 1));
				setBackgroundColor(classColor);
				setOpaque(true);
				
				attributes = new InternalItemFigure();
				methods = new InternalItemFigure();
				
				Label label;
				for (final DexField field: type.getFields()) {
					label = new Label(field.getVisibility() + " " + field.getName() + ":" + field.getType().getTypeFullNameBeauty());
					label.setTextAlignment(Label.LEFT);
					attributes.add(label);
					fieldFigureMap.put(field, label);
					label.addMouseListener(new MouseListener() {

						@Override
						public void mousePressed(org.eclipse.draw2d.MouseEvent e) { }

						@Override
						public void mouseReleased(org.eclipse.draw2d.MouseEvent e) { }

						@Override
						public void mouseDoubleClicked(org.eclipse.draw2d.MouseEvent e) {
							mainView.openFieldDetail(field);
						}
					});
				}
				for (final DexMethod method: type.getMethods()) {
					if (method.getCodeItem() != null) {
						label = new Label(method.getName() + "(" + method.getParametersTypeFullBeauty() + "):" + method.getReturnType().getTypeFullNameBeauty() );
						label.setTextAlignment(Label.LEFT);
						methods.add(label);
						label.addMouseListener(new MouseListener() {
							
							@Override
							public void mousePressed(org.eclipse.draw2d.MouseEvent e) { }
							
							@Override
							public void mouseReleased(org.eclipse.draw2d.MouseEvent e) { }
							
							@Override
							public void mouseDoubleClicked(org.eclipse.draw2d.MouseEvent e) {
								mainView.openMethodDetail(method);
							}
						});
					}
				}
				
				MouseListener classOpener = new MouseListener() {
					
					@Override
					public void mousePressed(org.eclipse.draw2d.MouseEvent e) { }
					
					@Override
					public void mouseReleased(org.eclipse.draw2d.MouseEvent e) { }
					
					@Override
					public void mouseDoubleClicked(org.eclipse.draw2d.MouseEvent e) {
						mainView.openClassDetail(type);
					}
				};
				if (type instanceof DexInternalClass) {
					if (((DexInternalClass) type).isInterface()) {
						label = new Label("<< interface >>");
						add(label);
						label.addMouseListener(classOpener);
					}

					DexAnnotations anno = ((DexInternalClass) type).getAnnotations();
					if (anno != null) {
						label = new Label("<< annotation >>");
						add(label);
						label.addMouseListener(classOpener);
					}
				} else {
					label = new Label("<< external >>");
					add(label);
					label.addMouseListener(classOpener);
				}
				label = new Label(type.getTypeFullNameBeauty());
				add(label);
				label.addMouseListener(classOpener);
				add(attributes);
				add(methods);
			}
			
			InternalItemFigure getAttributesFigure() {
				return attributes;
			}
			
			InternalItemFigure getMethodsFigure() {
				return methods;
			}
		}
		
		private Map<DexType, Node> typeNodeMap;
		private Map<DexField, Figure> fieldFigureMap;
		
		private Node getNodeMap(NodeList nodes, DexClass type) {
			if (typeNodeMap.containsKey(type)) {
				return typeNodeMap.get(type);
			}
			
			Figure newfigure;
			
			newfigure = new ClassFigure(type);
			Node newnode = new Node(newfigure);

			typeNodeMap.put(type, newnode);
			nodes.add(newnode);

			return newnode;
		}
		
		private FigureCanvas canvas;
		
		public Control getControl() {
			return canvas;
		}
		
		private PolylineConnection generateConnection(Figure source, Figure target) {
			PolylineConnection newconnection = new PolylineConnection();
			newconnection.setSourceAnchor(new ChopboxAnchor(source));
			newconnection.setTargetAnchor(new ChopboxAnchor(target));
			
			return newconnection;
		}
		
		public DexClassesDiagram(Composite parent, int style) {
			canvas = new FigureCanvas(parent);

			final Panel contents = new Panel();
			final XYLayout layout = new XYLayout();
			
			contents.setLayoutManager(layout);
			
			final DirectedGraph dg = new DirectedGraph();
			
			dg.nodes = new NodeList();
			dg.edges = new EdgeList();
			
			int size = program.getTypesCount();
			
			typeNodeMap = new HashMap<DexType, Node>();
			fieldFigureMap = new HashMap<DexField, Figure>();
			for (int i = 0; i < size; i++) {
				DexType type = program.getTypeByTypeId(i);
				
				if (type instanceof DexInternalClass) {
					Node fig = getNodeMap(dg.nodes, (DexInternalClass) type);
					DexClass superClass = ((DexInternalClass) type).getSuperClass();
					
					// make edge with super class
					if (superClass != null && ! superClass.isJavaLangObject()) {
						Node sup = getNodeMap(dg.nodes, superClass);
						
						PolylineConnection newconnection = generateConnection((Figure) fig.data, (Figure) sup.data);
						
						// decorate with arrow
						PolylineDecoration arrow= new PolylineDecoration();
						PointList pl= new PointList();
						
						pl.addPoint(-2, 2);
						pl.addPoint(0, 0);
						pl.addPoint(-2, -2);
						arrow.setTemplate(pl);
						newconnection.setTargetDecoration(arrow);
						
						dg.edges.add(new Edge(newconnection, sup, fig));
					}
					
					// make edges with interfaces
					for (DexClass j: ((DexInternalClass) type).getImplementingInterfaces()) {
						Node imp = getNodeMap(dg.nodes, j);
						
						PolylineConnection newconnection = generateConnection((Figure) fig.data, (Figure) imp.data);
						newconnection.setLineDash(new float[] { 5.0f, 3.0f });
						newconnection.setForegroundColor(ColorConstants.gray);
						
						PolylineDecoration arrow = new PolylineDecoration();
						PointList pl = new PointList();
						pl.addPoint(-2, 2);
						pl.addPoint(0, 0);
						pl.addPoint(-2, -2);
						arrow.setTemplate(pl);
						newconnection.setTargetDecoration(arrow);

						dg.edges.add(new Edge(newconnection, imp, fig));
					}
					
					// make edges for composition
					/*
					for (DexField f: ((DexInternalClass) type).getFields()) {
						if (typeNodeMap.containsKey(f.getType())) {
							PolylineConnection newconnection = generateConnection((Figure) fig.data, (Figure) typeNodeMap.get(f.getType()).data);
							newconnection.setForegroundColor(ColorConstants.lightGray);
							
							PolygonDecoration decoration = new PolygonDecoration();
							PointList decorationPointList = new PointList();
							decorationPointList.addPoint(0,0);
							decorationPointList.addPoint(-1,1);
							decorationPointList.addPoint(-2,0);
							decorationPointList.addPoint(-1,-1);
							decoration.setTemplate(decorationPointList);
							newconnection.setTargetDecoration(decoration);
							
							contents.add(newconnection);
						}
					}
					*/
				}
			}
			
			canvas.addPaintListener(new PaintListener() {
				private boolean firstTime = true;
				
				@Override
				public void paintControl(PaintEvent e) {
					if (firstTime) {
						Map<Figure, Dimension> originalSizes = new HashMap<Figure, Dimension>();
						
						for (Object obj: dg.nodes) { 
							Node node = (Node) obj;
							Figure figure = (Figure) node.data;
	
							contents.add(figure);
							Dimension size = figure.getPreferredSize();
							originalSizes.put(figure, size);
							node.width = size.width;
							node.height = size.height;
						}
						
						for (Object obj: dg.edges) {
							Edge edge = (Edge) obj;
							
							contents.add((IFigure) edge.data);
						}
	
						DirectedGraphLayout dgl = new DirectedGraphLayout();
						dgl.visit(dg);
						
						for (Object obj: dg.nodes) {
							Node node = (Node) obj;
							Figure figure = (Figure) node.data;
							Dimension originalSize = originalSizes.get(figure);
							
							layout.setConstraint(figure, new Rectangle(node.x, node.y, originalSize.width, originalSize.height));
						}
						
						firstTime = false;
					}
				}
			});
			
			
			canvas.setContents(contents);
		}
	}
}
