package fr.irit.smac.ui;

import fr.irit.smac.model.Experiment;

/**
 * It is just a dummy class for architecture transition ! DO NOT USE (expect in
 * test AbsRunConf and Links Client) OR MODIFY ! MUST BE REMOVE SAP ! DOESN'T
 * HAVE ANY SENS IN THIS BRANCH !
 */
public class LinksWindows {

	private Experiment experiment;

	public LinksWindows(Experiment experiment) {
		this.experiment = experiment;
	}

	public DummySnapshotsCollection getSnapshotsCollection() {
		return new DummySnapshotsCollection(experiment.getSnaptshot(experiment.getCurrentMaxSnapshotNum() - 1));
	}

	public int getMaxSnapNumber() {
		return experiment.getCurrentMaxSnapshotNum();
	}

}
