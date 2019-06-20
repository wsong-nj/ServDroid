package com.event;

import java.util.List;

import com.event.wtgStructure.WTGEdge;

public class Path {
	
	private List<WTGEdge> edges;
	private String targetActivity;
	private String targetMethod;
	private List<EventHandler> actToMethodEvents;
	
	public Path(List<WTGEdge> edges, String act, String method, List<EventHandler> events) {
		this.edges = edges;
		this.targetActivity = act;
		this.targetMethod = method;
		this.actToMethodEvents = events;
	}
	
	public List<WTGEdge> getEdges(){
		return edges;
	}

	public String getTargetActivity() {
		return targetActivity;
	}

	public void setEdges(List<WTGEdge> edges) {
		this.edges = edges;
	}
	
	public void setTargetActivity(String targetActivity) {
		this.targetActivity = targetActivity;
	}

	public String getTargetMethod() {
		return targetMethod;
	}

	public void setTargetMethod(String targetMethod) {
		this.targetMethod = targetMethod;
	}

	public List<EventHandler> getActToMethodEvents() {
		return actToMethodEvents;
	}

	public void setActToMethodEvents(List<EventHandler> actToMethodEvents) {
		this.actToMethodEvents = actToMethodEvents;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("targetActivity: " + targetActivity + "\n");
		sb.append("targetMethod: " + targetMethod + "\n");
		for(WTGEdge edge : edges) {
			sb.append(edge.toString());
		}
		sb.append("eventHandlers: \n");
		for(EventHandler handler : actToMethodEvents) {
			sb.append("\t" + handler.toString() + "\n");
		}
		sb.append("=================================\n");
		
		return sb.toString();
	}

	
	
}
