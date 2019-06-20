package com.event;

import soot.SootMethod;

public class EventHandler {
	
	private SootMethod handler;
	private EventRegister register;
	
	public EventHandler(SootMethod handler, EventRegister register) {
		this.handler = handler;
		this.register = register;
	}
	
	public SootMethod getHandler() {
		return handler;
	}
	public void setHandler(SootMethod handler) {
		this.handler = handler;
	}
	public EventRegister getRegister() {
		return register;
	}
	public void setRegister(EventRegister register) {
		this.register = register;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return handler.hashCode() + register.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof EventHandler))
			return false;
		EventHandler another = (EventHandler) obj;
		return handler.getSignature().equals(another.getHandler().getSignature())
				&& register.equals(another.getRegister());
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return handler.getSignature() + "->" + register.toString();
	}
	
	public String toShortString() {
		return handler.getSignature() + "->" + register.toShortString();
	}

}
