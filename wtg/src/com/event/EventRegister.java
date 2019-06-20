package com.event;

import soot.SootMethod;
import soot.Unit;

public class EventRegister {
	
	private SootMethod registerMethod; //注册方法
	private Unit registerStmt; //注册语句
	private String id; //组件id
	private EventType eventType;
	
	public EventRegister(SootMethod registerMethod, Unit registerStmt, String viewId) {
		this.registerMethod = registerMethod;
		this.registerStmt = registerStmt;
		this.id = viewId;
	}
	
	public EventRegister(SootMethod registerMethod, Unit registerStmt) {
		this(registerMethod, registerStmt, null);
	}
	
	
	public SootMethod getEventRegisterMethod() {
		return registerMethod;
	}
	
	public Unit getRegisterStmt() {
		return registerStmt;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
	public EventType getEventType() {
		return eventType;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return registerStmt == null ? registerMethod.hashCode() : registerStmt.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof EventRegister))
			return false;
		EventRegister another = (EventRegister) obj;
		if(registerStmt != null && another.registerStmt != null) {
			return registerStmt.equals(another.registerStmt);
		}else if(registerStmt == null && another.registerStmt == null) {
			return registerMethod.equals(another.registerMethod);
		}
		return false;
	}

	@Override
	public String toString() {
		
		return registerStmt + "in" + registerMethod.getSignature() + ":" + id;
	}
	
	public String toShortString() {
		return "id:" + id;
	}
	
}
