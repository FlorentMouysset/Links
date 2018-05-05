package fr.irit.smac.core;

import java.util.HashMap;
import java.util.Map;

import javax.swing.UIManager;

import org.apache.commons.lang3.StringUtils;
import org.graphstream.graph.Graph;
import org.graphstream.ui.view.Viewer;

import com.mongodb.ServerAddress;

import fr.irit.smac.model.Snapshot;
import fr.irit.smac.ui.LinksWindows;
import fr.irit.smac.ui.XpChooser;

public class LinksUI extends Links implements ILinks {

	private static final long serialVersionUID = -8072212895377938335L;
	/**
	 * The main UI windows.
	 */
	private LinksWindows linksWindow;

	private XpChooser xpChooser;

	private Map<String, LinksWindows> windows = new HashMap<String, LinksWindows>();

	/**
	 * Creates a new Links instance connection to the localhost and default port
	 * of MongoDB. This constructor enables to start the application with the
	 * selection of the experiment name.
	 */
	public LinksUI() {
		this(null, null, null, true);
	}

	/**
	 * Creates a new Links instance connection to the localhost and default port
	 * of MongoDB. This constructor intializes the experiment to the name passed
	 * in parameter.
	 * 
	 * @param xpName
	 *            The name of the experiment to use. If an experiment with this
	 *            name already exists, the application restore the previously
	 *            loaded data.
	 * 
	 */
	public LinksUI(String xpName) {
		this(null, xpName, null, true);
	}

	/**
	 * Creates a new Links instance connection to the specified address of
	 * MongoDB. This constructor intialise the experiment to the name passed in
	 * parameter.
	 * 
	 * @param addr
	 *            The ServerAddress of the MongoDB database.
	 * @param xpName
	 *            The name of the experiment to use. If an experiment with this
	 *            name already exists, the application restore the previously
	 *            loaded data.
	 * @param visible
	 *            The visibility of the experience's frame.
	 * 
	 */
	public LinksUI(ServerAddress addr, String xpName, boolean visible) {
		this(addr, xpName, null, visible);
	}

	/**
	 * Creates a new Links instance connection to the localhost and default port
	 * of MongoDB. This constructor intializes the experiment to the name passed
	 * in parameter.
	 * 
	 * @param xpName
	 *            The name of the experiment to use. If an experiment with this
	 *            name already exists, the application restore the previously
	 *            loaded data.
	 * @param visible
	 *            The visibility of the experience's frame.
	 * 
	 * @Param pathCss The path to the css file
	 */
	public LinksUI(String xpName, boolean visible, String pathCss) {
		this(null, xpName, pathCss, visible);
	}

	/**
	 * Creates a new Links instance connection to the localhost and default port
	 * of MongoDB. This constructor intializes the experiment to the name passed
	 * in parameter.
	 * 
	 * @param xpName
	 *            The name of the experiment to use. If an experiment with this
	 *            name already exists, the application restore the previously
	 *            loaded data.
	 * @param visible
	 *            The visibility of the experience's frame.
	 * 
	 */
	public LinksUI(String xpName, boolean visible) {
		this(null, xpName, null, visible);
	}

	/**
	 * Creates a new Links instance connection to the specified address of
	 * MongoDB. This constructor intialise the experiment to the name passed in
	 * parameter.
	 * 
	 * @param addr
	 *            The ServerAddress of the MongoDB database.
	 * @param xpName
	 *            The name of the experiment to use. If an experiment with this
	 *            name already exists, the application restore the previously
	 *            loaded data.
	 * 
	 * @Param pathCss The path to the css file
	 */
	public LinksUI(ServerAddress addr, String xpName, String pathCss) {
		this(addr, xpName, pathCss, true);
	}

	/**
	 * Creates a new Links instance connection to the localhost and default port
	 * of MongoDB. This constructor intializes the experiment to the name passed
	 * in parameter.
	 * 
	 * @param xpName
	 *            The name of the experiment to use. If an experiment with this
	 *            name already exists, the application restore the previously
	 *            loaded data.
	 * 
	 */
	public LinksUI(String xpName, String pathCss) {
		this(null, xpName, pathCss, true);
	}

	/**
	 * Creates a new Links instance connection to the specified address of
	 * MongoDB. This constructor intialise the experiment to the name passed in
	 * parameter.
	 * 
	 * @param addr
	 *            The ServerAddress of the MongoDB database.
	 * @param xpName
	 *            The name of the experiment to use. If an experiment with this
	 *            name already exists, the application restore the previously
	 *            loaded data.
	 * 
	 * @Param pathCss The path to the css file
	 */
	public LinksUI(ServerAddress addr, String xpName, String pathCss, boolean visible) {
		super(addr, xpName);
		setLookAndFeel();
		xpChooser = new XpChooser(this); // TODO CSS
		if (StringUtils.isNoneBlank(xpName)) {
			createNewLinksWindows(xpName, Links.getCssFilePathFromXpName(xpName), visible);
		}
		xpChooser.redrawList();

	}

	/**
	 * Get the displayed graph (to access advanced graphstream options).
	 * 
	 * @return The currently displayed graph.
	 */
	public Graph getGraph() {
		if (linksWindow != null) {
			// return linksWindow.getDisplayedGraph().getGraph();
			return this.windows.get(super.getCurrentXP()).getDisplayedGraph().getGraph();
		} else {
			return null;
		}
	}

	/**
	 * Get the graph view (to access advanced graphstream options).
	 * 
	 * @return The currently displayed graph.
	 */
	public Viewer getGraphView() {
		if (linksWindow != null) {
			return linksWindow.getViewer();
		} else {
			return null;
		}
	}

	/**
	 * Update the current graph to visualize the snapshot.
	 * 
	 * @param s
	 *            The snapshot to view.
	 */
	public void viewSnapshot(Snapshot s) {
		if (linksWindow != null) {
			// linksWindow.getDisplayedGraph().viewSnapshot(s);
			this.windows.get(super.getCurrentXP()).getDisplayedGraph().viewSnapshot(s);
		}
	}

	public void deleteWindow() {
		if (this.linksWindow != null)
			this.linksWindow.close();
	}

	/**
	 * Initialize the visualization windows on the specificied experiment using
	 * the specified CSS file.
	 * 
	 * @param xpName
	 *            The name of the experiment to visualize.
	 * @param linkToCss
	 *            The path to the CSS file.
	 * @return
	 */
	public LinksWindows createNewLinksWindows(String xpName, String linkToCss, boolean visible) {
		this.currentXP = xpName; // FIXME
		final LinksWindows newLinksWindows = new LinksWindows(xpName, linkToCss, this, visible);
		this.windows.put(xpName, newLinksWindows);
		return newLinksWindows;
	}

	/**
	 * Set look and feel according to the OS.
	 */
	private void setLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Release memory when a vizualisation windows is closed.
	 */
	@Override
	public void informClose() {
		super.informClose();
		this.windows.remove(super.getCurrentXP());
		this.linksWindow = null;
	}

	/**
	 * Drop the experiment with the given name and reset the current snapNumber
	 * at 0.
	 * 
	 * @param xpName
	 *            The name of the experiment.
	 */
	@Override
	public void dropExperiment(String xpName) {
		super.dropExperiment(xpName);

		if (this.windows.get(xpName) != null)
			this.windows.get(xpName).getDisplayedGraph().resetSnapNumber();
	}

	/**
	 * Add a new Snapshot to the model. The number of this snapshot is
	 * automatically choose.
	 * 
	 * @param s
	 *            The snapshot to add.
	 */
	@Override
	public void addSnapshot(Snapshot s, String xpName) {
		super.addSnapshot(s, xpName);
		if (windows.get(xpName) == null) {
			this.createNewLinksWindows(xpName, Links.getCssFilePathFromXpName(xpName), true);
		}
		linksWindow = windows.get(xpName);
		if (linksWindow != null && s != null) {
			try {
				linksWindow.addSnapshot(s);
			} catch (Exception e) {
				linksWindow.addSnapshot(s);
			}
		}

		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Add a new Snapshot to the model. The number of this snapshot is
	 * automatically choose.
	 * 
	 * @param s
	 *            The snapshot to add.
	 */
	@Override
	public void addSnapshot(Snapshot s) {
		linksWindow = this.windows.get(super.getCurrentXP());
		if (linksWindow != null && s != null) {
			try {
				linksWindow.addSnapshot(s);
				// this.windows.get(currentXP).addSnapshot(s);
			} catch (Exception e) {
				e.printStackTrace();
				linksWindow.addSnapshot(s);
				// this.windows.get(currentXP).addSnapshot(s);
			}
		}
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
		}
	}

}
