package fr.irit.smac.core;

import fr.irit.smac.model.Experiment;
import fr.irit.smac.model.Snapshot;

public class ExampleRetrive {

	public static void main(String[] args) {

		Links links = new Links("test");
		if (links.existsExperiment("test")) {
			Experiment exp1 = links.getExperiment("test");
			System.out.println(exp1.getExperimentSize());
			Snapshot snp = exp1.getSnaptshot(0);
			snp.getEntityList().forEach(entity -> {
				System.out.println(entity.getName() + " " + entity.getType());
				System.out.println(entity.getAttributes());
			});
		} else {
			System.out.println("Experiment not found.");
		}

		System.out.println("fin");
	}
}
