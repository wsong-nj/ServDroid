package com.event.wtgStructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.event.EventHandler;

public class WTG {

	private WTGNode launcher;
	private List<WTGNode> allNodes;
	private List<WTGEdge> allEdges;
	
	public WTG() {
		allNodes = new ArrayList<WTGNode>();
		allEdges = new ArrayList<WTGEdge>();
	}
	
	public WTGNode addNode(WTGNode node) {
		if(!this.allNodes.contains(node)) {
			this.allNodes.add(node);
		}
		return this.allNodes.get(this.allNodes.indexOf(node));
	}
	
	public WTGNode addLaunchNode(WTGNode node) {
		WTGNode launcherNode = addNode(node);
		if(launcher == null) {
			launcher = launcherNode;
		}else if(launcher != null && launcher != launcherNode) {
			System.out.println("try to set multiple launchers");
		}
		return launcher;
	}
	
	public WTGNode getLauncherNode() {
		return this.launcher;
	}
	
	public WTGEdge addEdge(WTGEdge newEdge) {
		if(!this.allEdges.contains(newEdge)) {
			this.allEdges.add(newEdge);
			newEdge.getSrcNode().addOutEdge(newEdge);
			newEdge.getTgtNode().addInEdge(newEdge);
			return newEdge;
		}else {
			WTGEdge edge = this.allEdges.get(this.allEdges.indexOf(newEdge));
			edge.addEventSeq(newEdge.getEventSeq());
			return edge;
		}
	}
	
	public WTGEdge getEdgeByNode(String srcNode, String tgtNode) {
		for(WTGEdge edge : allEdges) {
			if(edge.getSrcNode().getNode().equals(srcNode) && edge.getTgtNode().getNode().equals(tgtNode)) {
				return edge;
			}
		}
		return null;
	}
	
	public Collection<WTGNode> getNodes(){
		return this.allNodes;
	}
	
	public WTGNode getNodeByName(String name) {
		for(WTGNode node : allNodes) {
			if(node.getNode().equals(name))
				return node;
		}
		return null;
	}
	
	public Collection<WTGEdge> getEdges(){
		return this.allEdges;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("launcherActivity: ");
		sb.append(getLauncherNode());
		sb.append("\n");
		for(WTGNode node : allNodes) {
			sb.append("current Node: " + node + "\n");
			sb.append("Number of in edges: " + node.getInEdges().size() + "\n");
			sb.append("Number of out edges: " + node.getOutEdges().size() + "\n");
		}
		
		for(WTGEdge edge : allEdges) {                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 
			sb.append(edge.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
	
	
}
