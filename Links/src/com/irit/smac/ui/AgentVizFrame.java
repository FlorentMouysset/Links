package com.irit.smac.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.Viewer;

import com.irit.smac.core.DisplayedGraph;
import com.irit.smac.model.Agent;
import com.irit.smac.model.Attribute;
import com.irit.smac.model.Relation;
import com.irit.smac.model.Snapshot;
import com.irit.smac.model.SnapshotsCollection;

import fr.irit.smac.lxplot.LxPlot;
import fr.irit.smac.lxplot.commons.ChartType;
import javax.swing.JFormattedTextField;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class AgentVizFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1736315532751908362L;

	private JPanel contentPane;

	private JTree attributeTree;

	private JPanel attributeViewerPanel;

	private JLabel lblBotTxt;

	private Agent agent;

	private SnapshotsCollection snapCol;

	private LinksWindows links;

	private JSplitPane splitPane;

	private boolean isSynch = true;

	private long snapNum;
	private JButton btnNewButton;

	private ClicksPipe clicksPipe;

	private boolean neigh = false;

	private long drawSizeLong = 100;

	private Graph g;

	private Viewer viewer;
	private JButton btnNewButton_1;
	private JButton btnDraw;

	private String treeListSlected;
	private JTextPane txtpnLook;

	private AgentVizFrame me;

	private ArrayList<String> toLook = new ArrayList<String>();
	private ArrayList<String> toDrawGraphic = new ArrayList<String>();

	private String aname;
	private JButton btnSynch;

	private boolean isDrawing = false;
	private JFormattedTextField drawSize;
	private JLabel lblDrawSize;

	private long currentFrameNum;

	private long lastSnapNumDrawn = 0;

	/**
	 * Create the frame.
	 */
	public AgentVizFrame(Agent a, SnapshotsCollection snapCol, LinksWindows links) {
		me = this;
		aname = a.getName();
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent arg0) {
				links.unregisterObserver(me);
				agent.isTargeted(false);
			}
		});
		this.agent = a;
		this.snapCol = snapCol;
		this.links = links;
		snapNum = links.getCurrentSnapNumber();

		setTitle(a.getName() + " Vizualization tool");

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 592, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JToolBar toolBar = new JToolBar();
		contentPane.add(toolBar, BorderLayout.NORTH);

		splitPane = new JSplitPane();
		contentPane.add(splitPane, BorderLayout.CENTER);

		attributeTree = new JTree();
		attributeTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent arg0) {
				if (arg0.getSource().equals(attributeTree)) {
					toLook = new ArrayList<String>();
					for (int i : attributeTree.getSelectionRows()) {
						toLook.add(attributeTree.getPathForRow(i).toString());
					}
				}
			}
		});

		attributeViewerPanel = new JPanel();
		splitPane.setRightComponent(attributeViewerPanel);
		attributeViewerPanel.setLayout(new BorderLayout(0, 0));

		txtpnLook = new JTextPane();
		attributeViewerPanel.add(txtpnLook, BorderLayout.CENTER);

		btnNewButton = new JButton("Neighbouring:OFF");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (btnNewButton.getText().equals("Neighbouring:OFF")) {
					isTargeted(true);
					btnNewButton.setText("Neighbouring:ON");
				} else {
					isTargeted(false);
					btnNewButton.setText("Neighbouring:OFF");
				}
			}
		});

		btnSynch = new JButton("Synch: ON");
		btnSynch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (arg0.getSource().equals(btnSynch)) {
					isSynch = !isSynch;
					if (!isSynch) {
						btnSynch.setText("Synch: OFF");
					} else {
						btnSynch.setText("Synch: ON");
					}
				}
			}
		});
		btnSynch.setIcon(new ImageIcon(AgentVizFrame.class.getResource("/icons/synchronization.png")));
		toolBar.add(btnSynch);
		btnNewButton.setIcon(new ImageIcon(AgentVizFrame.class.getResource("/icons/neighb.png")));
		toolBar.add(btnNewButton);

		btnNewButton_1 = new JButton("Look");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (arg0.getSource().equals(btnNewButton_1)) {
					drawLook();
				}
			}
		});
		btnNewButton_1.setIcon(new ImageIcon(AgentVizFrame.class.getResource("/icons/look.png")));
		toolBar.add(btnNewButton_1);

		btnDraw = new JButton("Draw:OFF");
		btnDraw.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource().equals(btnDraw)) {
					if (isDrawing) {
						isDrawing = !isDrawing;
						btnDraw.setText("Draw:OFF");
					} else {
						isDrawing = !isDrawing;
						draw();
						btnDraw.setText("Draw:ON");
					}
				}
			}
		});
		btnDraw.setIcon(new ImageIcon(AgentVizFrame.class.getResource("/icons/draw.png")));
		toolBar.add(btnDraw);

		lblDrawSize = new JLabel("Draw Size: ");
		toolBar.add(lblDrawSize);

		drawSize = new JFormattedTextField();
		drawSize.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent arg0) {
				if (arg0.getSource().equals(drawSize)) {
					drawSizeLong = Long.valueOf(drawSize.getText());
				}
			}
		});
		drawSize.setText("100");
		toolBar.add(drawSize);

		lblBotTxt = new JLabel("Agent name : Toto ");
		lblBotTxt.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(lblBotTxt, BorderLayout.SOUTH);

		initFrame();
	}

	protected void isTargeted(boolean b) {
		agent.isTargeted(b);
		links.getDisplayedGraph().refresh(agent.getName(), agent.getType());
	}

	protected void switchNeighAtt() {
		if (neigh) {
			viewer.close();
			g = null;
			updateAttributeTreeList();
		} else {
			updateRelationsTreeList();
			switchToNeighGraph();
		}
	}

	protected void switchToNeighGraph() {
		updateRelationsTreeList();
	}

	private void updateRelationsTreeList() {
		attributeTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Relations") {
			{
				DefaultMutableTreeNode node;
				for (Relation r : snapCol.getSnaptshot(snapNum).getAgentsRelations()) {
					if (r.getA().getName().equals(agent.getName()) || r.getB().getName().equals(agent.getName())) {
						node = new DefaultMutableTreeNode(r.getName());
						add(node);
					}
				}
			}
		}));
		splitPane.setLeftComponent(attributeTree);
	}

	private void initFrame() {
		setlblBotTxt("Agent name : " + this.agent.getName() + " on snapshot number : " + links.getCurrentSnapNumber(),
				links.getCurrentSnapNumber());

		updateAttributeTreeList();

		setVisible(true);
	}

	private void updateAttributeTreeList() {
		attributeTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("> Attributes") {
			{
				DefaultMutableTreeNode node;
				for (String s : agent.getAttributes().keySet()) {
					node = new DefaultMutableTreeNode("@ " + s);
					for (Attribute a : agent.getAttributes().get(s)) {
						node.add(new DefaultMutableTreeNode(a.getName() + " := " + a.type()));
					}
					add(node);
				}
			}
		}));
		splitPane.setLeftComponent(attributeTree);
	}

	public void draw() {
		Agent a;
		long max = this.currentFrameNum;
		if (drawSizeLong == 0) {
			max = this.links.getMaxSnapNumber();
		}
		for (long i = Math.max(this.lastSnapNumDrawn, Math.max(1, this.currentFrameNum - drawSizeLong)); i < max; i++) {
			System.out.println("Yipeee");
			long timei = i;
			if (drawSizeLong != 0) {
				timei = i % drawSizeLong;
			}
			a = snapCol.getAgent(this.aname, i);
			if (a != null) {
				for (String s : toDrawGraphic) {
					if (a.getAttributesWithName(s).getTypeToDraw().equals("linear")) {
						LxPlot.getChart(aname + " linear", ChartType.LINE).add(s, timei,
								(Double) a.getAttributesWithName(s).getValue());
					}
					if (a.getAttributesWithName(s).getTypeToDraw().equals("bar")) {
						LxPlot.getChart(aname + " bar", ChartType.BAR).add(s, timei,
								(Double) a.getAttributesWithName(s).getValue());
					}
					if (a.getAttributesWithName(s).getTypeToDraw().equals("AVRT")) {
						Double tab[] = (Double[]) a.getAttributesWithName(s).getValue();
						for (Double val : tab) {
							LxPlot.getChart(aname + " AVRT : " + s, ChartType.LINE).add(s + "LOWER", timei, tab[0]);
							LxPlot.getChart(aname + " AVRT : " + s, ChartType.LINE).add(s + "AVTDownLower", timei,
									tab[1] - tab[2]);
							LxPlot.getChart(aname + " AVRT : " + s, ChartType.LINE).add(s + "AVTDownValue", timei,
									tab[1]);
							LxPlot.getChart(aname + " AVRT : " + s, ChartType.LINE).add(s + "AVTDownUpper", timei,
									tab[1] + tab[2]);
							LxPlot.getChart(aname + " AVRT : " + s, ChartType.LINE).add(s + "AVTUpLower", timei,
									tab[3] - tab[4]);
							LxPlot.getChart(aname + " AVRT : " + s, ChartType.LINE).add(s + "AVTUpValue", timei,
									tab[3]);
							LxPlot.getChart(aname + " AVRT : " + s, ChartType.LINE).add(s + "AVTUpUpper", timei,
									tab[3] + tab[4]);
							LxPlot.getChart(aname + " AVRT : " + s, ChartType.LINE).add(s + "UPPER", timei, tab[5]);
						}
					}
				}
			}
		}

		lastSnapNumDrawn = this.currentFrameNum;
	}

	public void drawLook() {
		toDrawGraphic = new ArrayList<String>();
		if (agent == null) {
			txtpnLook.setText("Warning : Agent do not exists in this snapshot");
		} else {
			String toDraw = "";
			String toL = "";
			for (String name : toLook) {
				Scanner sc = new Scanner(name);
				sc.useDelimiter(",");
				while (sc.hasNext()) {
					toL = sc.next();
				}
				String s = "";
				if (toL.contains(">")) {
					for (String key : agent.getAttributes().keySet()) {
						for (Attribute a : agent.getAttributes().get(key)) {

							toDraw += a.toString() + "\n";

							if (a.type().equals("double") || a.type().equals("AVRT")) {
								String nameToDraw = a.toString().substring(a.toString().indexOf("[") + 1,
										a.toString().indexOf("]"));
								toDrawGraphic.add(nameToDraw);
							}
						}
					}
				}
				if (toL.contains("@")) {
					s = toL.substring(toL.indexOf("@") + 2, toL.length() - 1);
					ArrayList<Attribute> list = agent.getAttributes().get(s);
					for (Attribute a : list) {
						toDrawGraphic.add(a.getName());
						toDraw += a.toString() + "\n";
					}
				}
				if (toL.contains(":=")) {
					s = toL.substring(1, toL.indexOf(":") - 1);
					toDrawGraphic.add(s);
					toDraw += agent.getAttributesWithName(s) + "\n";
				}
			}
			txtpnLook.setText(toDraw);
		}
	}

	public void notifyJump(long num) {
		if (isSynch) {
			agent = snapCol.getAgent(aname, num);
			drawLook();
			if (isDrawing) {
				draw();
			}
			setlblBotTxt("Agent name : " + aname + " on snapshot number : " + num, num);
		}
	}

	public void setlblBotTxt(String txt, long num) {
		currentFrameNum = num;
		lblBotTxt.setText(txt);
	}

}
