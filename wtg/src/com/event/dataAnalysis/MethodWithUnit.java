package com.event.dataAnalysis;

import soot.SootMethod;
import soot.Unit;

public class MethodWithUnit {
	SootMethod method;
	Unit u;
	public MethodWithUnit(SootMethod method, Unit u) {
		this.method = method;
		this.u = u;
	}
	public SootMethod getMethod() {
		return method;
	}
	public void setMethod(SootMethod method) {
		this.method = method;
	}
	public Unit getU() {
		return u;
	}
	public void setU(Unit u) {
		this.u = u;
	}
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return u.hashCode() + method.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof MethodWithUnit))
			return false;
		MethodWithUnit another = (MethodWithUnit) obj;
		return u == another.getU() && method.equals(another.getMethod());
	}
	
}
