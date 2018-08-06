package fr.irit.smac.ui;

import fr.irit.smac.model.Snapshot;

/**
 * It is just a dummy class for architecture transition ! DO NOT USE (expect in
 * test AbsRunConf and Links Client) OR MODIFY ! MUST BE REMOVE SAP ! DOESN'T
 * HAVE ANY SENS IN THIS BRANCH !
 */
public class DummySnapshotsCollection {

	private Snapshot dummySnaptshot;

	public DummySnapshotsCollection(Snapshot snaptshot) {
		this.dummySnaptshot = snaptshot;
	}

	public Snapshot getSnaptshot(int i) {
		return dummySnaptshot;
	}

}
