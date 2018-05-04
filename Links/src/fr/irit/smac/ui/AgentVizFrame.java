package fr.irit.smac.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.graphstream.graph.Graph;
import org.graphstream.ui.view.Viewer;

import fr.irit.smac.attributes.AVTAttribute;
import fr.irit.smac.attributes.DrawableAttribute;
import fr.irit.smac.lxplot.LxPlot;
import fr.irit.smac.lxplot.commons.ChartType;
import fr.irit.smac.model.Attribute;
import fr.irit.smac.model.Entity;
import fr.irit.smac.model.Relation;
import fr.irit.smac.model.SnapshotsCollection;
import fr.irit.smac.model.Attribute.AttributeStyle;

public class AgentVizFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1736315532751908362L;

	private JPanel contentPane;

	private JTree attributeTree;

	private JPanel attributeViewerPanel;

	private JLabel lblBotTxt;

	/**
	 * The Entity to visualize.
	 */
	private Entity entity;

	/**
	 * The snapshot collection.
	 */
	private SnapshotsCollection snapCol;

	/**
	 * The reference to the LinksWindows.
	 */
	private LinksWindows links;

	private JSplitPane splitPane;

	/**
	 * Boolean of the synchronization.
	 */
	private boolean isSynch = true;

	/**
	 * The number of the snapshot.
	 */
	private long snapNum;
	private JButton btnNewButton;

	/**
	 * The boolean of the neighbouring.
	 */
	private boolean neigh = false;

	/**
	 * The size long of a chart.
	 */
	private long drawSizeLong = 50;

	private JButton btnNewButton_1;
	private JButton btnDraw;

	private JTextPane txtpnLook;

	private AgentVizFrame me;

	/**
	 * The list of Drawable Attribute to look for.
	 */
	private ArrayList<DrawableAttribute> toLook = new ArrayList<DrawableAttribute>();

	private String aname;
	private JButton btnSynch;

	private boolean isDrawing = false;
	private JFormattedTextField drawSize;
	private JLabel lblDrawSize;

	private long currentFrameNum;

	private ArrayList<Relation> relations = new ArrayList<Relation>();

	private long lastSnapNumDrawn = 0;
	private JPanel panel;
	private JLabel lblNewLabel;



	/**
	 * Create the frame.
	 * @param a The entity to look at.
	 * @param snapCol The reference to the snapshot collection.
	 * @param links The reference to the links windows.
	 */
	public AgentVizFrame(Entity a, SnapshotsCollection snapCol, LinksWindows links) {
		me = this;
		aname = a.getName();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				links.unregisterObserver(me);
				entity.setTargeted(false);
			}
		});
		this.entity = a;
		this.snapCol = snapCol;
		this.links = links;
		snapNum = links.getCurrentSnapNumber();

		setTitle(a.getName() + " Vizualization tool"+ "   Type : "+ a.getType());

		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 592, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JToolBar toolBar = new JToolBar();
		contentPane.add(toolBar, BorderLayout.NORTH);

		splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		contentPane.add(splitPane, BorderLayout.CENTER);

		attributeTree = new JTree();
		attributeTree.setRootVisible(false);
		attributeTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
		attributeTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent arg0) {
				if (arg0.getSource().equals(attributeTree)) {
					updateLookAndDraw(attributeTree.getSelectionPaths());
				}
			}
		});

		splitPane.setLeftComponent(new JScrollPane(attributeTree));

		attributeViewerPanel = new JPanel();
		splitPane.setRightComponent(new JScrollPane(attributeViewerPanel));
		attributeViewerPanel.setLayout(new BorderLayout(0, 0));

		txtpnLook = new JTextPane();
		attributeViewerPanel.add(txtpnLook, BorderLayout.CENTER);

		btnNewButton = new JButton("Neighbouring:OFF");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (btnNewButton.getText().equals("Neighbouring:OFF")) {
					isTargeted(true);
					updateTreeList();
					btnNewButton.setText("Neighbouring:ON");
				} else {
					isTargeted(false);
					updateTreeList();
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
						update();
					}
				}
			}
		});

		lblNewLabel = new JLabel("");
		lblNewLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				updateTreeList();
			}
		});
		lblNewLabel.setIcon(new ImageIcon(AgentVizFrame.class.getResource("/icons/refresh.png")));
		toolBar.add(lblNewLabel);
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
						draw();
						isDrawing = !isDrawing;
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

		panel = new JPanel();
		contentPane.add(panel, BorderLayout.WEST);

		lblBotTxt = new JLabel("Agent name : Toto ");
		lblBotTxt.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(lblBotTxt, BorderLayout.SOUTH);

		initFrame();
		relations = snapCol.getRelations(aname, snapNum);
		updateTreeList();
		for (int i = 0; i < attributeTree.getRowCount(); i++) {
			attributeTree.expandRow(i);
		}
	}

	private void updateLookAndDraw(TreePath[] path) {
		this.toLook = new ArrayList<DrawableAttribute>();
		relations = snapCol.getRelations(aname, currentFrameNum);
		if (path != null && entity != null) {
			for (int i = 0; i < path.length; i++) {
				switch (path[i].getPath().length) {
				case 1:
					// System.out.println(path[i].getPath()[0].toString());
					break;
				case 2:
					/* Whole agent or relations selected */
					if (path[i].getPath()[1].toString().contains("Relations")) {
						for (Relation r : this.relations) {
							if(r.getAttributes() != null)
								for (String s : r.getAttributes().keySet()) {
									for (Attribute t : r.getAttributes().get(s)) {
										this.toLook.add(
												new DrawableAttribute(DrawableAttribute.Type.RELATION, r.getName(), s, t));
									}
								}
						}
					}
					if (path[i].getPath()[1].toString().contains("Entity")) {
						if(entity.getAttributes() != null)
							for (String s : entity.getAttributes().keySet()) {
								for (Attribute t : entity.getAttributes().get(s)) {
									this.toLook.add(
											new DrawableAttribute(DrawableAttribute.Type.ENTITY, entity.getName(), s, t));
								}
							}
					}
					break;
				case 3:
					/* Set of characteristics selected */

					if (path[i].getPath()[1].toString().contains("Relations")) {
						Relation r = snapCol.getRelation(path[i].getPath()[2].toString(), this.currentFrameNum);
						if(r != null && this.toLook != null){
							if(r.getAttributes() != null)
								for (String s : r.getAttributes().keySet()) {
									for (Attribute t : r.getAttributes().get(s)) {
										this.toLook
										.add(new DrawableAttribute(DrawableAttribute.Type.RELATION, r.getName(), s, t));
									}
								}
						}
					}

					if (path[i].getPath()[1].toString().contains("Entity")) {

						String s = path[i].getPath()[2].toString();
						for (Attribute t : entity.getAttributes().get(s)) {
							this.toLook
							.add(new DrawableAttribute(DrawableAttribute.Type.ENTITY, entity.getName(), s, t));
						}
					}
					break;
				case 4:
					/* One characteristic selected */

					/* Set of characteristics selected */

					if (path[i].getPath()[1].toString().contains("Relations")) {
						Relation r = snapCol.getRelation(path[i].getPath()[2].toString(), this.currentFrameNum);
						String s = path[i].getPath()[3].toString();
						if(r.getAttributes() != null)
							for (Attribute t : r.getAttributes().get(s)) {
								this.toLook.add(new DrawableAttribute(DrawableAttribute.Type.RELATION, r.getName() , s, t));
							}

					}

					if (path[i].getPath()[1].toString().contains("Entity")) {
						String s = path[i].getPath()[2].toString();
						String tmp = path[i].getPath()[3].toString();
						tmp = tmp.substring(tmp.indexOf("[") + 1, tmp.indexOf("]"));
						if (entity != null) {
							Attribute t = entity.getAttributesWithName(tmp);
							this.toLook
							.add(new DrawableAttribute(DrawableAttribute.Type.ENTITY, entity.getName() + entity.getType(), s, t));
						}

					}
					break;
				}
			}
		}
	}

	protected void isTargeted(boolean b) {
		entity.setTargeted(b);
		links.getDisplayedGraph().refreshNeighbouring(entity.getName(), entity.getType());
	}

	private void initFrame() {
		setlblBotTxt("Entity name : " + this.entity.getName() + " on snapshot number : " + links.getCurrentSnapNumber(),
				links.getCurrentSnapNumber());

		updateTreeList();

		setVisible(true);
	}

	/**
	 * Method uses by LinksWindows 
	 * Check if the synchronization is on
	 * 	If it's on uses the method updateTreeList
	 */
	public void update(){
		if(isSynch)
			updateTreeList();
	}

	/**
	 * Update treeList.
	 */
	private void updateTreeList() {
		if(entity != null){
			setTitle(entity.getName() + " Vizualization tool"+ "   Type : "+ entity.getType());
			TreePath[] path = attributeTree.getSelectionPaths();

			//We use reload if we have to create entity or relation
			boolean needReload = false;
			DefaultTreeModel model = (DefaultTreeModel) this.attributeTree.getModel();
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
			if(root.isLeaf()){
				DefaultMutableTreeNode entityNode = new DefaultMutableTreeNode("Entity : "+entity.getName());
				model.insertNodeInto(entityNode, root, 0);
				needReload = true;
			}
			DefaultMutableTreeNode entityNode = (DefaultMutableTreeNode) root.getFirstChild();

			this.redrawTree(entityNode, this.entity.getAttributes(), model);
			//Verify if the tree already knows the attributes

			//DefaultMutableTreeNode relNode = new DefaultMutableTreeNode("Relations");
			if(root.getChildCount()<2){
				DefaultMutableTreeNode relNode = new DefaultMutableTreeNode("Relations");
				//root.add(relNode);
				model.insertNodeInto(relNode, root, 1);
				needReload = true;
			}
			DefaultMutableTreeNode relNode = (DefaultMutableTreeNode) root.getChildAt(1);


			/*for(int i =relNode.getChildCount()-1; i>= 1;i--){
				boolean alive = false;
				for(Relation r : this.relations){
					if(relNode.getChildAt(i).toString().equals(r.getName())){
						alive = true;
					}
				}
				if(!alive)
					model.removeNodeFromParent((MutableTreeNode) relNode.getChildAt(i));
			}*/

			for (Relation r : this.relations) {
				boolean exist = false;
				int pos = -1;
				for(int i =0; i < relNode.getChildCount(); i++){
					if(relNode.getChildAt(i).toString().equals(r.getName())){
						exist = true;
						pos = i;
					}
				}
				if(exist){
					DefaultMutableTreeNode relationNode = (DefaultMutableTreeNode) relNode.getChildAt(pos);
					this.redrawTree(relationNode, r.getAttributes(), model);
				}
				else{
					DefaultMutableTreeNode relationNode = new DefaultMutableTreeNode(r.getName());
					model.insertNodeInto(relationNode, relNode, relNode.getChildCount());
					this.redrawTree(relationNode, r.getAttributes(), model);
				}
			}

			attributeTree.getSelectionModel().addSelectionPaths(path);


			if(needReload)
				model.reload();
			else{
				model.nodeChanged(root);
				model.nodeChanged(relNode);
				model.nodeChanged(entityNode);
			}
		}
		//attributeTree.setModel(tree);
		//attributeTree.setRootVisible(false);
	}
	
	public void redrawTree(DefaultMutableTreeNode node, HashMap<String,ArrayList<Attribute>> h,DefaultTreeModel model){
		//Verify if the tree already knows the attributes
		for (String carac : h.keySet()) {
			boolean exist = false;
			int pos =0;
			for(int i = 0; i < node.getChildCount(); i++){
				if(node.getChildAt(i).toString().equals(carac)){
					exist = true;
					pos = i;
				}
			}
			//If no we create it
			if(!(exist)){
				DefaultMutableTreeNode newCarac = new DefaultMutableTreeNode(carac);
				model.insertNodeInto(newCarac, node, node.getChildCount());

				for (Attribute t : h.get(carac)) {
					model.insertNodeInto(new DefaultMutableTreeNode(t.toString()), newCarac, newCarac.getChildCount());
				}
				model.nodeChanged(node);
			}
			//If yes we check if the values are the same
			else{
				boolean valExist = true;
				ArrayList<Attribute> addList = new ArrayList<Attribute>();
				for(Attribute att : h.get(carac)){
					boolean find = false;
					for(int i = 0; i < node.getChildAt(pos).getChildCount();i++){
						if(node.getChildAt(pos).getChildAt(i).toString().equals(att.toString()))
							find = true;
					}
					if(!(find)){
						valExist = false;
						addList.add(att);
					}
				}
				//If no we replace the old value with the new
				if(!(valExist)){
					for(Attribute att : addList){
						boolean creat = false;
						for(int i =0; i < node.getChildAt(pos).getChildCount(); i++){
							if(node.getChildAt(pos).getChildAt(i).toString().contains(att.getName())){
								TreeNode tmp = node.getChildAt(pos).getChildAt(i);
								model.insertNodeInto(new DefaultMutableTreeNode(att), (MutableTreeNode) node.getChildAt(pos), 0);
								model.removeNodeFromParent((MutableTreeNode) tmp);
								creat = true;
							}
						}
						if(!creat)
							model.insertNodeInto(new DefaultMutableTreeNode(att), (MutableTreeNode) node.getChildAt(pos), node.getChildAt(pos).getChildCount());

					}
					//TreeNode tmp = entityNode.getChildAt(pos).getChildAt(0);

				}
				model.nodeChanged(node);
			}

		}
	}

	/**
	 * Draw a chart thanks to LxPlot.
	 */
	public synchronized void draw() {
		Entity a;
		long max = this.currentFrameNum;
		if (drawSizeLong == 0) {
			max = this.links.getMaxSnapNumber();
		}
		long u;
		if (links.getFrameSpeed() > 0) {
			u = Math.max(this.lastSnapNumDrawn, Math.max(1, this.currentFrameNum - drawSizeLong));
		} else {
			u = Math.min(this.lastSnapNumDrawn, Math.max(1, this.currentFrameNum - drawSizeLong));
		}

		for (long i = u; i <= max; i++) {
			long timei = i;
			if (drawSizeLong != 0) {
				timei = i % drawSizeLong;
			}
			a = snapCol.getEntity(this.aname, i);
			if (a != null) {
				for (DrawableAttribute t : this.toLook) {
					String s = t.getAttribute().getName();
					Attribute theAttribute = t.getAttribute();
					if (theAttribute.getTypeToDraw().equals(AttributeStyle.LINEAR)) {
						LxPlot.getChart(t.getType() + ">" + t.getName() + ":" + t.getCaracList() + ":" + " linear",
								ChartType.LINE).add(s, timei, (Double) theAttribute.getValue());
					}
					if (theAttribute.getTypeToDraw().equals(AttributeStyle.BAR)) {
						LxPlot.getChart(t.getType() + ">" + t.getName() + ":" + t.getCaracList() + ":" + " bar",
								ChartType.BAR).add(s+1, 0, (Double) theAttribute.getValue());
						LxPlot.getChart(t.getType() + ">" + t.getName() + ":" + t.getCaracList() + ":" + " bar",
								ChartType.BAR).add(s+2, 1, (Double) theAttribute.getValue());
					}
					if (theAttribute.getTypeToDraw().equals(AttributeStyle.AVRT)) {
						Double tab[] = (Double[]) theAttribute.getValue();
						for (Double val : tab) {
							LxPlot.getChart(
									t.getType() + ">" + t.getName() + ":" + t.getCaracList() + ":" + " AVRT : " + s,
									ChartType.LINE).add(s + "LOWER", timei, tab[0]);
							LxPlot.getChart(
									t.getType() + ">" + t.getName() + ":" + t.getCaracList() + ":" + " AVRT : " + s,
									ChartType.LINE).add(s + "AVTDownLower", timei, tab[1] - tab[2]);
							LxPlot.getChart(
									t.getType() + ">" + t.getName() + ":" + t.getCaracList() + ":" + " AVRT : " + s,
									ChartType.LINE).add(s + "AVTDownValue", timei, tab[1]);
							LxPlot.getChart(
									t.getType() + ">" + t.getName() + ":" + t.getCaracList() + ":" + " AVRT : " + s,
									ChartType.LINE).add(s + "AVTDownUpper", timei, tab[1] + tab[2]);
							LxPlot.getChart(
									t.getType() + ">" + t.getName() + ":" + t.getCaracList() + ":" + " AVRT : " + s,
									ChartType.LINE).add(s + "AVTUpLower", timei, tab[3] - tab[4]);
							LxPlot.getChart(
									t.getType() + ">" + t.getName() + ":" + t.getCaracList() + ":" + " AVRT : " + s,
									ChartType.LINE).add(s + "AVTUpValue", timei, tab[3]);
							LxPlot.getChart(
									t.getType() + ">" + t.getName() + ":" + t.getCaracList() + ":" + " AVRT : " + s,
									ChartType.LINE).add(s + "AVTUpUpper", timei, tab[3] + tab[4]);
							LxPlot.getChart(
									t.getType() + ">" + t.getName() + ":" + t.getCaracList() + ":" + " AVRT : " + s,
									ChartType.LINE).add(s + "UPPER", timei, tab[5]);
						}
					}
					if (theAttribute.getTypeToDraw().equals(AttributeStyle.AVT)) {
						LxPlot.getChart(t.getType() + ">" + t.getName() + ":" + t.getCaracList() + ":" + " AVT",
								ChartType.LINE).add(s + "Value", timei, (Double) theAttribute.getValue());
						LxPlot.getChart(t.getType() + ">" + t.getName() + ":" + t.getCaracList() + ":" + " AVT",
								ChartType.LINE).add(s + "Delta", timei, ((AVTAttribute) theAttribute).getDelta());
					}
				}
			}
		}

		lastSnapNumDrawn = this.currentFrameNum;
	}

	/**
	 * Write the information on the text pane.
	 */
	public void drawLook() {
		String s = "";
		Lock l = new ReentrantLock();
		l.lock();
		try{
			for (DrawableAttribute t : this.toLook) {
				if (t.getType().equals(DrawableAttribute.Type.ENTITY)) {
					s = s + "{" + t.getCaracList() + "} " + t.getAttribute().toString() + "\n";
				} else {
					s = s + " " + t.getType() + ":" + t.getName() + " : {" + t.getCaracList() + "} "
							+ t.getAttribute().toString() + "\n";
				}
			}
			if (s == "") {
				if(toLook.isEmpty()){
					s = "Nothing is selected";
				}else{
					s = "Entity is dead or not alive yet";
				}
			}
			txtpnLook.setText(s);
		}
		finally{
			l.unlock();
		}
	}

	/**
	 * Update the vizFrame.
	 * 
	 * @param num
	 * 			The number of the snapshot.
	 */
	public void notifyJump(long num) {
		if (isSynch) {
			entity = snapCol.getEntity(aname, num);
			relations = snapCol.getRelations(aname, num);
			updateLookAndDraw(attributeTree.getSelectionPaths());
			drawLook();
			if (isDrawing) {
				draw();
			}
			setlblBotTxt("Entity name : " + aname + " on snapshot number : " + num, num);
			update();
		}
	}

	/**
	 * Set lblBoxTxt
	 * @param txt
	 * 			The text.
	 * @param num
	 * 			The number.
	 */
	public void setlblBotTxt(String txt, long num) {
		currentFrameNum = num;
		lblBotTxt.setText(txt);
	}

	/**
	 * Return the name.
	 */
	public String getName(){
		return this.aname;
	}
}