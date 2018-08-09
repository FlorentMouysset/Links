package fr.irit.smac.core;

import fr.irit.smac.model.Snapshot;
import fr.irit.smac.ui.LinksWindows;

/**
 * It is just a dummy class for architecture transition ! DO NOT USE (expect in
 * test AbsRunConf and Links Client) OR MODIFY ! MUST BE REMOVE SAP ! DOESN'T
 * HAVE ANY SENS IN THIS BRANCH !
 */
public class LinksUI extends Links {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8954685432659766888L;

	public void createExperiment(String expName, String links_style_sheet_css) {
		super.createExperiment(expName);
	}

	public void addSnapshot(Snapshot snapshot, String expName) {
		super.createExperiment(expName).addSnapshotToExperiment(snapshot);
	}

	public void deleteWindow() {
		// nothing to do...
	}

	public LinksWindows createNewLinksWindows(String collectionName, String cssLocation, boolean b) {
		return new LinksWindows(this.getExperiment(collectionName));
	}

	public void duplicateExperiments(String collectionNameSource, String collectionNameTarget) {
		super.duplicateExperiments(collectionNameSource, collectionNameTarget);

	}

}
