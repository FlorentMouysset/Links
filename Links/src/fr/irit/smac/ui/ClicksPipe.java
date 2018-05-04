package fr.irit.smac.ui;

import java.io.Serializable;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

import fr.irit.smac.model.Attribute.AttributeStyle;

/**
 * 
 * @author Marcillaud Guilhem
 *
 */
public class ClicksPipe extends Thread implements ViewerListener, Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6081772850762199542L;

	protected boolean loop = true;

	/**
	 * The graph.
	 */
	private Graph graph;
	
	/**
	 * The viewer.
	 */
	private  Viewer viewer;
	
	/**
	 * The LinksWindow.
	 */
	private LinksWindows links;

	/**
	 * Manage the events of the view for the graph.
	 * 
	 * @param graph
	 * 			The graph.
	 * @param viewer
	 * 			The viewer.
	 * @param links
	 * 			The LinksWindow.
	 */
	public ClicksPipe(Graph graph, Viewer viewer, LinksWindows links) {
		this.graph = graph;
		this.viewer = viewer;
		this.links = links;
		this.start();
	}


	/**
	 * Manage the events of the view.
	 */
	public void run(){

		ViewerPipe fromViewer = viewer.newViewerPipe();
		fromViewer.addViewerListener(this);
		fromViewer.addSink(graph);

		while(loop) {
			try {
				Thread.sleep(10);
				fromViewer.pump(); 

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Trigger when the view is closed, stop the thread.
	 */
	public void viewClosed(String id) {
		loop = false;
	}

	/**
	 * Set the mouse Move of the window.
	 */
	public void buttonPushed(String id) {
		this.links.setMouseMove(true);
	}

	/**
	 * Trigger when the mouse is released on a node
	 */
	public void buttonReleased(String id) {
		// If the windows is in state Drawing, draw the graph of the entity. 
		if(this.links.getDrawing()){
			this.links.constructDraw(links.getDisplayedGraph().getSnapCol().getEntity(id,links.getCurrentSnapNumber()),null,100,true);
			//this.links.isDraw();
		}
		
		// If we are not in state Moving, create an AgentVizFrame of the Entity.
		if(!this.links.getMoving()){
			AgentVizFrame f = new AgentVizFrame(links.getDisplayedGraph().getSnapCol().getEntity(id,links.getCurrentSnapNumber()),links.getSnapCol(),links);
			links.registerObserver(f);
		}
		this.links.setMouseMove(false);
	}
}
