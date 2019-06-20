package com.event.wtgStructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WTGNode {
	
	private String node;
	private List<WTGEdge> incomingEdges;
	private List<WTGEdge> outgoingEdges;
	
	public WTGNode(String node) {
		this.node = node;
		this.incomingEdges = new ArrayList<WTGEdge>();
		this.outgoingEdges = new ArrayList<WTGEdge>();
	}
	
	public void addOutEdge(WTGEdge out) {
		if(!outgoingEdges.contains(out)) {
			outgoingEdges.add(out);
		}
	}

	public void addInEdge(WTGEdge in) {
		if(!incomingEdges.contains(in)) {
			incomingEdges.add(in);
		}
	}
	
	public Collection<WTGEdge> getOutEdges() {
	    return this.outgoingEdges;
	}

	public Collection<WTGEdge> getInEdges() {
	    return this.incomingEdges;
	}
	
	public String getNode() {
		return node;
	}
	
	public int countInEdges() {
	    return incomingEdges.size();
	}

	public int countOutEdges() {
	    return outgoingEdges.size();
	}
	public String toString() {
	    return node;
	}

	@Override
	public int hashCode() {
		return node.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof WTGNode)) {
			return false;
		}
		WTGNode another = (WTGNode) obj;
		return node.equals(another.getNode());
	}
	
	
}
