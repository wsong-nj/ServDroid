package com.event.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.Body;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Sources;
import soot.util.Chain;

public class basicAnalysis {
	public static List<SootClass> classesChain; // app's all classes

	public basicAnalysis() {
		classesChain = resolveAllClasses(Scene.v().getClasses());
	}

	public static List<SootClass> resolveAllClasses(Chain<SootClass> chain) { // Traverse
		List<SootClass> allClasses = new ArrayList<SootClass>();
		for (SootClass s : chain) {
			if (s.isConcrete()) {
				if (!s.getName().startsWith("android") && !s.getName().startsWith("java")
						&& !s.getName().startsWith("org")) {
					allClasses.add(s);
				}
			}
		}
		return allClasses;
	}

	public static Value GetLeftOP(Unit unit) {
		Value leftop = null;
		Stmt stmt = (Stmt) unit;
		if (stmt instanceof AssignStmt) {
			leftop = ((AssignStmt) stmt).getLeftOp();
		} else if (stmt instanceof IdentityStmt) {
			leftop = ((IdentityStmt) stmt).getLeftOp();
		} else if (stmt instanceof InvokeStmt) {
			List<ValueBox> ValueBoxList = unit.getUseAndDefBoxes();
			if (ValueBoxList.size() > 2) {
				leftop = ValueBoxList.get(ValueBoxList.size() - 2).getValue();
			}
		}
		return leftop;
	}

	/**
	 * 获得unit的右值
	 */
	public Value GetRightOP(Unit unit) {
		Value rightop = null;
		Stmt stmt = (Stmt) unit;
		if (stmt instanceof InvokeStmt) {
			if (stmt.containsInvokeExpr()) {
				if (stmt.containsInvokeExpr()) {
					InvokeExpr expr = stmt.getInvokeExpr();
					List<Value> argsList = expr.getArgs();
					if (argsList.size() != 0) {
						rightop = argsList.get(0);
					}
				}
			}
		} else if (stmt instanceof AssignStmt) {
			rightop = ((AssignStmt) stmt).getRightOp();
		} else if (stmt instanceof IdentityStmt) {
			rightop = ((IdentityStmt) stmt).getRightOp();
		}
		return rightop;
	}

	public Value GetReturnValue(Unit unit) {
		Value returnValue = null;
		Stmt stmt = (Stmt) unit;
		if (stmt instanceof ReturnStmt) {
			// returnValue=stmt.
			returnValue = ((ReturnStmt) stmt).getOp();
		}
		return returnValue;
	}

	public boolean judegValueIsIdStmt(Body body, Value value) {
		for (Unit unit : body.getUnits()) {
			Stmt stmt = (Stmt) unit;
			if (stmt instanceof IdentityStmt) {
				Value leftValue = GetLeftOP(unit);
				if (leftValue.equals(value)) {
					return true;
				}
			}
		}
		return false;
	}

	private static ArrayList<SootClass> getSuperClass(String sootclass) {
		ArrayList<SootClass> getSuperClasses = new ArrayList<SootClass>();
		for (SootClass sc : Scene.v().getClasses()) {
			if (sc.toString().equals(sootclass)) {
				if (sc.hasSuperclass()) {
					// System.out.println(sc.getSuperclass());
					getSuperClasses.add(sc.getSuperclass());
					getSuperClasses.addAll(getSuperClass(sc.getSuperclass().toString()));
				}
			}
		}

		return getSuperClasses;
	}

	protected static boolean judgeActivity(String sc) {
		List<SootClass> getsuperClasses = getSuperClass(sc);
		for (int i = 0; i < getsuperClasses.size(); i++) {
			if (getsuperClasses.get(i).toString().equals("android.app.Activity")
					|| getsuperClasses.get(i).toString().equals("android.app.Service"))
				return true;
		}

		return false;
	}
	
	public static List<SootMethod> getSourceMethods(SootMethod sootMethod) {
		// System.out.println("source"+sootMethod);
		List<SootMethod> sMethods = new ArrayList<SootMethod>();
		CallGraph cg = Scene.v().getCallGraph();
		Iterator<MethodOrMethodContext> targets = new Sources(cg.edgesInto(sootMethod));
		while (targets.hasNext()) {
			
			SootMethod m = (SootMethod) targets.next();
			sMethods.add(m);

		}
		//System.out.println("source method:" + sMethods);
		return sMethods;
	}
	
	public static int judgeParam(Body body, Value value) {
		for (Unit unit : body.getUnits()) {
			Stmt stmt = (Stmt) unit;
			if (stmt instanceof IdentityStmt) {
				Value value1 = ((IdentityStmt) stmt).getLeftOp();
				if (value1.equals(value)) {
					if (judgeInit(body, value1)) {
						List<Type> types = body.getMethod().getParameterTypes();
						for (int i = 0; i < types.size(); i++) {
							if (types.get(i).equals(value.getType()))
								return i;
						}
					}
				}
			}
		}
		return -1;

	}

	public static boolean judgeInit(Body body, Value value) {
		for (Unit unit : body.getUnits()) {
			Stmt stmt = (Stmt) unit;
			if (stmt instanceof AssignStmt) {
				Value value1 = ((AssignStmt) stmt).getLeftOp();
				if (value1.equals(value))
					return false;
			}
		}
		return true;
	}
	
	public static boolean judgeFragment(String sc) {
		List<SootClass> superClasses = getSuperClass(sc);
		if (sc.equals("android.support.v4.app.Fragment"))
			return true;
		else {
			for (int i = 0; i < superClasses.size(); i++) {
				if (superClasses.get(i).toString().equals("android.support.v4.app.Fragment"))
					return true;
			}
		}
		return false;
	}
	
	public static boolean judgePagerAdpter(String sc) {
		List<SootClass> superClasses = getSuperClass(sc);
		if (sc.equals("android.support.v4.view.PagerAdapter"))
			return true;
		else {
			for (int i = 0; i < superClasses.size(); i++) {
				if (superClasses.get(i).toString().equals("android.support.v4.view.PagerAdapter"))
					return true;
			}
		}
		return false;
	}
	
	public String HandleString(String value) {
		String string = value.replaceAll("/", ".");
		try {
			if (string.contains(" ")) {
				string = string.substring(string.indexOf(" ") + 2, string.length() - 1);
				if (string.startsWith("L"))
					string = string.substring(1, string.length() - 1);
			}
			if(string.contains("\"")) {
				string=string.replaceAll("\"","");
			}
		} catch (Exception e) {

		}

		return string;

	}
}
