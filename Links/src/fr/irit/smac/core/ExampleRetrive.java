package fr.irit.smac.core;

public class ExampleRetrive {

	public static void main(String[] args) {

		Links links = new Links("test");
		if (links.existsExperiment("test")) {
			links.getExperiment("test");
		} else {
			System.out.println("Experiment not found.");
		}

		System.out.println("fin");
	}
}
