package com.event.wtgStructure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.event.EventHandler;

public class WTGEdge {
	
	private WTGNode srcNode;
	private WTGNode tgtNode;
	private Set<List<EventHandler>> eventSeq;
	
	public WTGEdge(WTGNode srcNode, WTGNode tgtNode, Set<List<EventHandler>> eventSeq) {
		this.srcNode = srcNode;
		this.tgtNode = tgtNode;
		this.eventSeq = new HashSet<>();
		for(List<EventHandler> list : eventSeq) {
			List<EventHandler> tmpList = new ArrayList<>();
			tmpList.addAll(list);
			this.eventSeq.add(tmpList);
		}
	}

	public WTGNode getSrcNode() {
		return srcNode;
	}

	public void setSrcNode(WTGNode srcNode) {
		this.srcNode = srcNode;
	}

	public WTGNode getTgtNode() {
		return tgtNode;
	}

	public void setTgtNode(WTGNode tgtNode) {
		this.tgtNode = tgtNode;
	}

	public Set<List<EventHandler>> getEventSeq() {
		return eventSeq;
	}
	public String getEventSeqToString() {
		StringBuffer sb=new StringBuffer();
		for(List<EventHandler> list:eventSeq) {
			sb.append(list);
		}
		return sb.toString();
	}

	public void setEventSeq(Set<List<EventHandler>> eventSeq) {
		this.eventSeq = eventSeq;
	}
	
	public void addEventSeq(Set<List<EventHandler>> eventSeq) {
		this.eventSeq.addAll(eventSeq);
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return srcNode.hashCode() + tgtNode.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof WTGEdge)) {
			return false;
		}
		WTGEdge another = (WTGEdge) obj;
		return srcNode.equals(another.getSrcNode())
				&& tgtNode.equals(another.getTgtNode());
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("src: ");
		sb.append(srcNode);
		sb.append("\n");
		sb.append("tgt: ");
		sb.append(tgtNode);
		sb.append("\n");
		sb.append("eventSeq: ");
		sb.append("\n");
		for(List<EventHandler> seq : getEventSeq()) {
			sb.append("\t");
			for(EventHandler handler : seq) {
				sb.append(handler.toString());
			}
			sb.append("\n");
		}
		sb.append("\n");
		return sb.toString();
	}

	

	
}
