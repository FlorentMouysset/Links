package fr.irit.smac.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import fr.irit.smac.core.Links;
import fr.irit.smac.core.LinksUI;
import fr.irit.smac.core.MongoDBDelegate;
import fr.irit.smac.model.Attribute.AttributeStyle;
import fr.irit.smac.model.Entity;
import fr.irit.smac.model.Relation;
import fr.irit.smac.model.Snapshot;

/**
 * 
 * @author Marcillaud Guilhem
 *
 */
public class XpChooser extends JFrame {

	private JPanel contentPane;
	private JList<String> list;
	private LinksUI linksRef;
	private NewXpWindows xpWindows;
	private JTextArea textField;

	/**
	 * Create the frame.
	 * 
	 * @param links
	 *            The reference to the links window.
	 */
	public XpChooser(LinksUI links) {
		setTitle("Links: Xp Chooser");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent arg0) {
				Links.mongoClient.close();
			}
		});
		this.linksRef = links;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(400, 400, 400, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(new JScrollPane(contentPane));

		JToolBar toolBar = new JToolBar();
		contentPane.add(toolBar, BorderLayout.NORTH);

		JLabel lblAdd = new JLabel("");
		XpChooser ref = this;
		lblAdd.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				xpWindows = new NewXpWindows(ref);
			}
		});
		lblAdd.setIcon(new ImageIcon(XpChooser.class.getResource("/icons/plus.png")));
		toolBar.add(lblAdd);
		toolBar.addSeparator(new Dimension(5, 25));

		JLabel lblRemove = new JLabel("");
		lblRemove.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				if (arg0.getSource().equals(lblRemove)) {
					int dialogButton = JOptionPane.YES_NO_OPTION;

					List<String> xps = list.getSelectedValuesList();
					String choice = "Would You Like to Completly Delete the Experiment Entitled : \n";
					for (String xpName : xps) {
						choice += xpName + "\n";
					}
					choice += " ?";
					int dialogResult = JOptionPane.showConfirmDialog(null, choice, "Warning", dialogButton);
					if (dialogResult == JOptionPane.YES_OPTION) {
						for (String xpName : xps)
							destroyExperiment(xpName);
					}
				}
			}
		});
		lblRemove.setIcon(new ImageIcon(XpChooser.class.getResource("/icons/minus.png")));
		toolBar.add(lblRemove);
		toolBar.addSeparator(new Dimension(5, 25));

		JLabel lblPlay = new JLabel("");
		lblPlay.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				if (list.getSelectedValue() != null) {
					String xpName = (String) list.getSelectedValue();
					if (xpName != null) {
						String linkToCss = Links.getCssFilePathFromXpName(xpName);
						links.createNewLinksWindows(xpName, linkToCss, true);
					}
				}
			}

		});

		JLabel lblEdit = new JLabel("");
		lblEdit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getSource().equals(lblEdit)) {
					if (list.getSelectedValue() != null) {
						String xpName = (String) list.getSelectedValue();
						if (xpName != null) {
							xpWindows = new NewXpWindows(XpChooser.this, xpName);
						}
					}
				}
			}
		});

		JLabel lblErase = new JLabel("");
		lblErase.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				int dialogButton = JOptionPane.YES_NO_OPTION;
				List<String> xps = list.getSelectedValuesList();
				String choice = "Would You Like to Drop the Experiment Entitled : \n";
				for (String xpName : xps) {
					choice += xpName + "\n";
				}
				choice += " ?";
				int dialogResult = JOptionPane.showConfirmDialog(null, choice, "Warning", dialogButton);

				if (dialogResult == JOptionPane.YES_OPTION) {
					// String xpName = (String) list.getSelectedValue();
					linksRef.deleteWindow();
					for (String xpName : xps)
						drop(xpName);
				}

			}
		});
		lblErase.setIcon(new ImageIcon(XpChooser.class.getResource("/icons/eraser.png")));
		toolBar.add(lblErase);
		toolBar.addSeparator(new Dimension(5, 25));
		lblEdit.setIcon(new ImageIcon(XpChooser.class.getResource("/icons/edit.png")));
		toolBar.add(lblEdit);
		toolBar.addSeparator(new Dimension(5, 25));

		ImageIcon iErase = new ImageIcon(LinksWindows.class.getResource("/icons/eraser.png"));
		;

		JLabel lblSave = new JLabel();
		lblSave.setIcon(new ImageIcon(new ImageIcon(LinksWindows.class.getResource("/icons/save.png")).getImage()
				.getScaledInstance(iErase.getIconWidth(), iErase.getIconHeight(), Image.SCALE_DEFAULT)));
		lblSave.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				// not available in this version
				// save((String) list.getSelectedValue());
			}
		});
		toolBar.add(lblSave);
		toolBar.addSeparator(new Dimension(5, 25));

		JLabel lblLoad = new JLabel();
		lblLoad.setIcon(new ImageIcon(new ImageIcon(LinksWindows.class.getResource("/icons/file.png")).getImage()
				.getScaledInstance(iErase.getIconWidth(), iErase.getIconHeight(), Image.SCALE_DEFAULT)));
		lblLoad.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				loadExperience();
			}
		});
		toolBar.add(lblLoad);
		toolBar.addSeparator(new Dimension(5, 25));
		lblPlay.setIcon(new ImageIcon(XpChooser.class.getResource("/icons/play.png")));
		toolBar.add(lblPlay);
		toolBar.addSeparator(new Dimension(5, 25));

		JLabel lblSaveDesc = new JLabel("Save desc");
		lblSaveDesc.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				saveDesc();
			}
		});
		toolBar.add(lblSaveDesc);
		toolBar.addSeparator(new Dimension(5, 25));

		list = new JList();
		init();
		this.setVisible(true);
	}

	/**
	 * Save the decription in mongoDB.
	 */
	protected void saveDesc() {
		String descriptionToAdd = textField.getText();
		String collectionNameToAddDescription = list.getSelectedValue();
		MongoDBDelegate.updateCollectionDescription(collectionNameToAddDescription, descriptionToAdd);
	}

	/**
	 * Delete completely an experience.
	 * 
	 * @param xpName
	 *            The name of the experience.
	 */
	public void delete(String xpName) {
		linksRef.deleteExperiment(xpName);
	}

	/**
	 * Create a new experience.
	 * 
	 * @param xpName
	 *            The name of the experience.
	 */
	public void create(String xpName) {
		linksRef.createExperiment(xpName);
		this.redrawList();
	}

	/**
	 * Create an experience with the cssPath.
	 * 
	 * @param xpName
	 *            The name of the experience.
	 * @param cssPath
	 *            The path to the css.
	 */
	public void create(String xpName, String cssPath) {
		linksRef.createExperiment(xpName, cssPath);
		this.redrawList();
	}

	/**
	 * Drop an experience.
	 * 
	 * @param xpName
	 *            The name of the experience.
	 */
	public void drop(String xpName) {
		linksRef.dropExperiment(xpName);
	}

	/**
	 * Delete an experience and redraw the list.
	 * 
	 * @param xpName
	 *            The name of the experience.
	 */
	protected void destroyExperiment(String xpName) {
		delete(xpName);
		this.redrawList();
	}

	/**
	 * Initialize the frame.
	 */
	private void init() {
		Vector<String> v = MongoDBDelegate.getExperiencesList();

		list = new JList<String>(v);
		list.setFont(new Font("Tahoma", Font.PLAIN, 14));
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.addListSelectionListener(new ListSelectionListener() {
			// TODO
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (list.getSelectedValue() != null) {
					String description = MongoDBDelegate.getDescription(list.getSelectedValue());
					textField.setText(description);
				}
			}
		});

		JLabel lblTxt = new JLabel("Select or create your experiment");
		lblTxt.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(lblTxt, BorderLayout.SOUTH);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		contentPane.add(splitPane, BorderLayout.CENTER);

		splitPane.setLeftComponent(list);

		textField = new JTextArea();
		textField.setLineWrap(true);
		splitPane.setRightComponent(textField);

		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					if (list.getSelectedValue() != null) {
						String xpName = (String) list.getSelectedValue();
						if (xpName != null) {
							String linkToCss = Links.getCssFilePathFromXpName(xpName);
							linksRef.createNewLinksWindows(xpName, linkToCss, true);
						}
					}
				}
			}
		});
	}

	/**
	 * Update the list of experience.
	 */
	public void redrawList() {
		xpWindows = null;

		DefaultListModel<String> v = new DefaultListModel<String>();
		MongoDBDelegate.getExperiencesList().forEach(xpName -> v.addElement(xpName));

		list.setModel(v);
	}

	/**
	 * Load an experience and put it in mongoDB
	 */
	public void loadExperience() {
		JFileChooser chooser = new JFileChooser();
		chooser.showOpenDialog(null);
		String loadPath = null;
		if (chooser.getSelectedFile() != null) {
			loadPath = chooser.getSelectedFile().toString();

			String[] loadSplit = loadPath.split("\\\\");
			String xpName = loadSplit[loadSplit.length - 1];
			xpName = xpName.split("\\.")[0];

			Vector<String> allExperiments = MongoDBDelegate.getExperiencesList();
			int iter = 1;
			while (allExperiments.contains(xpName)) {
				xpName = xpName + "(" + iter + ")";
				iter++;
			}

			this.create(xpName);
			String linkToCss = Links.getCssFilePathFromXpName(xpName);
			linksRef.createNewLinksWindows(xpName, linkToCss, true);
			try {
				BufferedReader sourceFile = new BufferedReader(new FileReader(loadPath));
				String line;
				int i = 0;
				Map<String, String> things = new HashMap<String, String>();
				// For each line
				while ((line = sourceFile.readLine()) != null) {
					if (line.contains("DescriptionOfXP")) {
						String[] lineSplit = line.split(",|=|\\}");
						ArrayList<String> tmp = this.eraseEmpty(lineSplit);
						String descriptionToAdd = tmp.get(tmp.size() - 1);

						MongoDBDelegate.updateCollectionDescription(xpName, descriptionToAdd);

					} else {
						// We split by a quote
						String[] lineSplit = line.split(",");
						if (i > 1) {
							// Creation of the new Snapshot
							Snapshot s = new Snapshot();
							// for each part splitted
							for (int j = 2; j < lineSplit.length - 1; j++) {
								if (!lineSplit[j].equals("")) {
									String base = lineSplit[j];
									// We get the type
									String[] baseSplit = base.split(":|\"|\\{");
									String type = baseSplit[baseSplit.length - 1];
									if (type.equals("Entity")) {
										j = constructEntity(lineSplit, j, s, type, things);
									} else {
										j = constructRelation(lineSplit, j, s);
									}
								}
							}
							this.linksRef.addSnapshot(s);
						}
						i++;
					}
				}
				sourceFile.close();

			} catch (FileNotFoundException e) {
				System.out.println("Le fichier est introuvable !");
			} catch (IOException e) {
				e.printStackTrace();
			}
			redrawList();
		}
	}

	/**
	 * Method use to construct an entity
	 * 
	 * @param lineSplit
	 *            The tab of String
	 * @param j
	 *            The index
	 * @param s
	 *            The Snapshot
	 * @param type
	 *            The type
	 * @param things
	 *            The map with the entities
	 * @return j The index
	 */
	private int constructEntity(String[] lineSplit, int j, Snapshot s, String type, Map<String, String> things) {

		int nbAcc = 1;
		j++;
		// We get the name
		ArrayList<String> spl = eraseEmpty(lineSplit[j].split(":|\"|\\[|\\]|\\{|\\}|="));
		String name = spl.get(spl.size() - 1);

		nbAcc += matchBraces(lineSplit[j]);

		j++;
		// We get the class
		spl = eraseEmpty(lineSplit[j].split(":|\"|\\[|\\]|\\{|\\}|="));
		String eclass = spl.get(spl.size() - 1);

		j++;
		// We get the coorX
		spl = eraseEmpty(lineSplit[j].split(":|\"|\\[|\\]|\\{|\\}|="));
		double coorx = Double.parseDouble(spl.get(spl.size() - 1));

		j++;
		// We get the coorY
		spl = eraseEmpty(lineSplit[j].split(":|\"|\\[|\\]|\\{|\\}|="));
		double coory = Double.parseDouble(spl.get(spl.size() - 1));

		// If the entity does not exist we add it
		if (things.get(name) == null) {
			things.put(name, name);
		}
		nbAcc += matchBraces(lineSplit[j]);
		Entity entity;
		if (coorx == -10000 && coory == -10000)
			entity = s.addEntity(name, eclass);
		else
			entity = s.addEntity(name, eclass, coorx, coory);

		// we search for all the attribute
		if (!lineSplit[j].contains("}")) {
			boolean eend = false;
			j++;
			while (!eend) {

				// We get the attribute
				spl = eraseEmpty(lineSplit[j].split(":|\"|\\[|\\]|\\{|\\}|="));
				String att = spl.get(0);

				boolean aend = false;
				boolean fir = true;
				while (!aend) {
					nbAcc += matchBraces(lineSplit[j]);
					spl = eraseEmpty(lineSplit[j].split(":|\"|\\[|\\]|\\{|\\}|="));
					// We get the name
					String aname = null;
					if (fir)
						aname = spl.get(1);
					else
						aname = spl.get(0);

					// We get the attributeStyle
					String style = null;
					if (fir)
						style = spl.get(3);
					else
						style = spl.get(2);
					AttributeStyle astyle = null;
					switch (style.trim()) {
					case "BAR":
						astyle = AttributeStyle.BAR;
						break;
					case "AVRT":
						astyle = AttributeStyle.AVRT;
						break;
					case "AVT":
						astyle = AttributeStyle.AVT;
						break;
					case "STRING":
						astyle = AttributeStyle.STRING;
					default:
						astyle = AttributeStyle.LINEAR;
					}

					j++;

					// We get the type
					spl = eraseEmpty(lineSplit[j].split(":|\"|\\[|\\]|\\{|\\}|="));
					String atype = spl.get(2);
					nbAcc += matchBraces(lineSplit[j]);

					// We get the value
					String avalue = spl.get(spl.size() - 1);

					switch (atype.trim()) {
					case "Double":
						entity.addOneAttribute(att, aname, Double.parseDouble(avalue), astyle);
						break;
					case "String":
						entity.addOneAttribute(att, aname, avalue);
						break;
					default:
						break;
					}
					aend = lineSplit[j].contains("}}");
					if (nbAcc != 0)
						j++;
					fir = false;
				}
				eend = (nbAcc == 0);

			}
		}
		return j;
	}

	/**
	 * Erase the empty from a tab and put the result in a list.
	 * 
	 * @param split
	 *            The tab.
	 * @return ret An ArrayList.
	 */
	private ArrayList<String> eraseEmpty(String[] split) {
		ArrayList<String> ret = new ArrayList<String>();
		for (int i = 0; i < split.length; i++) {
			if (!split[i].equals(""))
				ret.add(split[i]);
		}
		return ret;
	}

	/**
	 * Method use to construct the relations
	 * 
	 * @param lineSplit
	 *            The tab with the fields
	 * @param j
	 *            the index
	 * @param s
	 *            The snapshot
	 * @return j the index
	 */
	private int constructRelation(String[] lineSplit, int j, Snapshot s) {
		int nbAcc = 1;
		j++;
		// We get the name
		ArrayList<String> spl = eraseEmpty(lineSplit[j].split(":|\"|\\[|\\]|\\{|\\}|="));
		String name = spl.get(1);

		j++;
		// We get the A
		spl = eraseEmpty(lineSplit[j].split(":|\"|\\[|\\]|\\{|\\}|="));
		String a = spl.get(1);

		j++;
		// We get the B
		spl = eraseEmpty(lineSplit[j].split(":|\"|\\[|\\]|\\{|\\}|="));
		String b = spl.get(1);

		j++;
		// We get the direction
		spl = eraseEmpty(lineSplit[j].split(":|\"|\\[|\\]|\\{|\\}|="));
		String dir = spl.get(1);
		boolean bdir = dir.equals("true");

		j++;
		// We get the class
		spl = eraseEmpty(lineSplit[j].split(":|\"|\\[|\\]|\\{|\\}|="));
		String rclass = spl.get(1);

		Relation r = s.addRelation(a, b, name, bdir, rclass);

		// we search for all the attribute
		if (!lineSplit[j].contains("}")) {

			boolean eend = false;
			j++;
			while (!eend) {

				// We get the attribute
				spl = eraseEmpty(lineSplit[j].split(":|\"|\\[|\\]|\\{|\\}|="));
				String att = spl.get(0);

				boolean aend = false;
				boolean fir = true;
				while (!aend) {
					nbAcc += matchBraces(lineSplit[j]);
					spl = eraseEmpty(lineSplit[j].split(":|\"|\\[|\\]|\\{|\\}|="));
					// We get the name
					String aname = null;
					if (fir)
						aname = spl.get(1);
					else
						aname = spl.get(0);

					// We get the attributeStyle
					String style = null;
					if (fir)
						style = spl.get(3);
					else
						style = spl.get(2);
					AttributeStyle astyle = null;
					switch (style.trim()) {
					case "BAR":
						astyle = AttributeStyle.BAR;
						break;
					case "AVRT":
						astyle = AttributeStyle.AVRT;
						break;
					case "AVT":
						astyle = AttributeStyle.AVT;
						break;
					case "STRING":
						astyle = AttributeStyle.STRING;
					default:
						astyle = AttributeStyle.LINEAR;
					}

					j++;

					// We get the type
					spl = eraseEmpty(lineSplit[j].split(":|\"|\\[|\\]|\\{|\\}|="));
					String atype = spl.get(2);
					nbAcc += matchBraces(lineSplit[j]);

					// We get the value
					String avalue = spl.get(spl.size() - 1);

					switch (atype.trim()) {
					case "Double":
						r.addOneAttribute(att, aname, Double.parseDouble(avalue), astyle);
						break;
					case "String":
						r.addOneAttribute(att, aname, avalue);
						break;
					default:
						break;
					}
					aend = lineSplit[j].contains("}}");
					if (nbAcc != 0)
						j++;
					fir = false;
				}
				eend = (nbAcc == 0);

			}
		}

		return j;
	}

	/**
	 * Count the number of braces
	 * 
	 * @param s
	 *            The string
	 * @return nbAcc Then umber of braces
	 */
	private int matchBraces(String s) {
		String tmpF = s;
		int nbAcc = 0;
		int lengF = 0;
		while ((lengF = tmpF.indexOf("{", lengF)) > 0) {
			nbAcc++;
			lengF++;
		}
		String tmpB = s;
		int lengB = 0;
		while ((lengB = tmpB.indexOf("}", lengB)) > 0) {
			nbAcc--;
			lengB++;
		}
		return nbAcc;
	}
}
