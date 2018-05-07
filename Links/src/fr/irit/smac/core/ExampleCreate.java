package fr.irit.smac.core;

import fr.irit.smac.model.Attribute.AttributeStyle;
import fr.irit.smac.model.Entity;
import fr.irit.smac.model.Experiment;
import fr.irit.smac.model.Snapshot;

public class ExampleCreate {

	public static void main(String[] args) {

		Links links = new Links("test");
		Snapshot s = new Snapshot();
		Entity a = s.addEntity("Toto", "Humain");

		a.addOneAttribute("Charct", "Age", 24);
		a.addOneAttribute("Charct", "Bonbons", 2., AttributeStyle.BAR);
		a.addOneAttribute("Charct", "Nom", "Toto");

		s.addEntity("Rufus", "Dog");
		s.addRelation("Toto", "Rufus", "TotoPossedeRufus", false, "possede");

		s.addEntity("Toto", "Humain");

		Experiment exp = links.createExperiment("test");
		exp.addSnapshotToExperiment(s);

		System.out.println("fin");
	}
}
