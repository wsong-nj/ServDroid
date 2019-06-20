package tool.Analy.Analysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.test.xmldata.ProcessManifest;

import soot.Body;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
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

public class BasicAnalysis {

	public List<SootClass> classesChain; // app's all classes
	public List<SootClass> InvokerList = new ArrayList<SootClass>();
	public List<String> bindServiceNameList = new ArrayList<String>();
	public ProcessManifest processMan;
	public List<SootMethod> Classes;

	public BasicAnalysis(String apkFileLocation) {
		classesChain = resolveAllClasses(Scene.v().getClasses());
		Classes = new ArrayList<>();
		processMan = new ProcessManifest();
		processMan.loadManifestFile(apkFileLocation);
	}

	public List<SootClass> resolveAllClasses(Chain<SootClass> chain) {
		List<SootClass> allClasses = new ArrayList<SootClass>();
		for (SootClass s : chain) {
			if (s.isConcrete()) {
				if (!s.getName().startsWith("android") && !s.getName().startsWith("java")) {
					allClasses.add(s);
				}
			}
		}
		return allClasses;
	}

	private List<Unit> getAllUnitsBetweenUnit(Unit unit1, Unit unit2, Body body) {
		List<Unit> units1 = getAllPreviousUnit(unit2, body);
		List<Unit> units2 = getAllPreviousUnit(unit1, body);
		units1.removeAll(units2);
		return units1;
	}

	/**
	 * 在当前body中寻找
	 * 
	 * @param b
	 * @param value
	 * @param depth
	 * @return
	 */
	public String searchStringInOneBody(Body body, Unit unit1, String value, int depth, Set<String> set) {
		String value2 = null;
		for (Unit unit : getAllPreviousUnit(unit1, body)) {
			if (unit instanceof AssignStmt) {
				if (getLeftOP(unit).toString().equals(value)) {
					if (((AssignStmt) unit).containsInvokeExpr()) {
						SootMethod sm = ((AssignStmt) unit).getInvokeExpr().getMethod();
						if (sm.getName().equals("getPackageName")) {
							String string = processMan.getPackageName();
							for (Unit unit2 : getAllUnitsBetweenUnit(unit, unit1, body)) {
								Stmt stmt = (Stmt) unit2;
								if (stmt.containsInvokeExpr()
										&& stmt.getInvokeExpr().getMethod().getName().equals("append")) {
									if (!stmt.getInvokeExpr().getArg(0).toString().startsWith("$")) {
										String param = stmt.getInvokeExpr().getArg(0).toString();
										string = string + param.substring(1, param.length() - 1);
									}
								}

							}
							return string;
						} else {
							SootClass sc = sm.getDeclaringClass();
							if (!sc.getName().startsWith("android") && !sc.getName().startsWith("java")) {
								value2 = searchServiceName(sc, sm, set);
								if (value2 != null && judgeService(handleString(value2.toString()))) {
									set.add(handleString(value2.toString()));
									return value2;
								}
							}
						}

					}

					else {
						value2 = getRightOP(unit).toString();
						if (value2.contains(".") && value2.endsWith(">")) {
							value2 = value2.substring(value2.indexOf(".") + 1);
							return searchStringInInit(body, value2, set);
						} else if (value2.startsWith("$") && depth < 10) {
							return searchStringInOneBody(body, unit, value2, ++depth, set);
						} else {
							return value2;
						}
					}
				}
			}

			if (unit instanceof IdentityStmt) {
				if (getLeftOP(unit).toString().equals(value)) {
					return getRightOP(unit).toString();
				}
			}
		}
		return null;
	}

	public String searchStringInInit(Body body, String value, Set<String> set) {
		String string = null;
		SootClass sClass = body.getMethod().getDeclaringClass();
		for (SootMethod sm : sClass.getMethods()) {
			if (sm.getName().equals("<init>")) {
				Body body2 = sm.retrieveActiveBody();
				for (Unit unit : body2.getUnits()) {
					try {
						String leftValue = getLeftOP(unit).toString();
						if (leftValue.contains(value)) {
							return searchStringInOneBody(sm.retrieveActiveBody(), unit, getRightOP(unit).toString(), 7,
									set);
						}
					} catch (Exception e) {
					}

				}
			}
		}
		return string;
	}

	public String getServiceWithImplicitIntent(Body body, Value value,
			Map<String, HashSet<String>> serviceWithActionInProcessMan) {
		String serviceName = null;
		Set<String> actionList = new HashSet<>();
		for (Unit unit : body.getUnits()) {
			Stmt stmt = (Stmt) unit;
			if ((stmt instanceof InvokeStmt || stmt instanceof AssignStmt) && stmt.containsInvokeExpr()) {
				Value leftValue = getLeftOP(unit);
				if (leftValue != null && leftValue.equals(value)) {
					// System.out.println("value:" + value);
					String methodName = stmt.getInvokeExpr().getMethod().getName();
					if (methodName.equals("<init>") || methodName.equals("setAction")
							|| methodName.equals("addAction")) {
						if (stmt.getInvokeExpr().getArgCount() == 1) {
							actionList.add(stmt.getInvokeExpr().getArg(0).toString());
						}

					}
				}

			}

		}
		serviceName = getServiceNameWithAction(actionList);
		return serviceName;

	}

	private String getServiceNameWithAction(Set<String> actions) {
		String serviceName = null;
		Set<String> set = processMan.serviceWithAction.keySet();
		for (Iterator<String> iterator = set.iterator(); iterator.hasNext();) {
			String value = iterator.next();
			boolean flag = false;
			Set<String> actionSet = processMan.serviceWithAction.get(value);
			for (String action : actions) {
				if (actionSet.contains(handleString(action))) {
					flag = true;
				}
				if (!actionSet.contains(handleString(action))) {
					flag = false;
				}
			}
			if (flag) {
				serviceName = value;
			}

		}
		return serviceName;
	}

	public String searchServiceName(SootClass sc, SootMethod method, Set<String> set) {
		System.out.println("search:" + sc + " " + method);
		Classes.add(method);
		Value value = null;
		String serviceName = null;
		for (SootMethod sm : sc.getMethods()) {
			if (sm.equals(method)) {
				if (sm.isConcrete()) {
					Body body = sm.retrieveActiveBody();
					Iterator<Unit> iterator = body.getUnits().snapshotIterator();
					while (iterator.hasNext()) {
						Unit unit = iterator.next();
						if (unit instanceof ReturnStmt) {
							value = getReturnValue(unit);
							break;
						}
					}

				}
			}
		}
		if (value != null) {
			if (judgeService(handleString(value.toString()))) {
				set.add(handleString(value.toString()));
				return handleString(value.toString());
			}
			for (SootMethod sm : sc.getMethods()) {
				if (sm.equals(method)) {
					if (sm.isConcrete()) {
						Body body = sm.retrieveActiveBody();
						Iterator<Unit> iter = body.getUnits().snapshotIterator();
						while (iter.hasNext()) {
							Unit unit = iter.next();
							Stmt stmt = (Stmt) unit;
							Value result = getLeftOP(unit);
							if (result != null && result.equals(value)) {
								if (stmt.containsInvokeExpr()) {
									serviceName = analysisStmt(unit, body, value, set);
								}

								else {
									if (stmt instanceof AssignStmt) {
										Value value2 = getRightOP(unit);
										for (SootMethod sMethod : sc.getMethods()) {
											if (sMethod.isConcrete()) {
												for (Unit unit2 : sMethod.retrieveActiveBody().getUnits()) {
													Value rightValue = getRightOP(unit2);
													if (rightValue != null
															&& value2.toString().equals(rightValue.toString())) {
														Value leftvalue = getLeftOP(unit2);
														for (Unit unit4 : sMethod.retrieveActiveBody().getUnits()) {
															Stmt stmt4 = (Stmt) unit4;
															if (getLeftOP(unit4) != null
																	&& getLeftOP(unit4).equals(leftvalue)
																	&& stmt4.containsInvokeExpr()) { // InvokeStmt
																String methodName = stmt4.getInvokeExpr().getMethod()
																		.getName();
																if (methodName.equals("<init>")
																		|| methodName.contains("setClass")
																		|| methodName.contains("setClassName")) {
																	if (stmt4.getInvokeExpr().getArgCount() == 2
																			&& judgeService(
																					handleString(stmt4.getInvokeExpr()
																							.getArg(1).toString()))) {
																		serviceName = stmt4.getInvokeExpr().getArg(1)
																				.toString();
																		if (serviceName.toString().startsWith("$")) {
																			serviceName = searchStringInOneBody(
																					sMethod.retrieveActiveBody(), unit4,
																					serviceName, 7, set);
																			if (judegValueIsIdStmt(
																					sMethod.retrieveActiveBody(),
																					stmt4.getInvokeExpr().getArg(1))) {
																				for (SootMethod sootMethod : Classes) {
																					for (Unit unit3 : sootMethod
																							.retrieveActiveBody()
																							.getUnits()) {
																						Stmt stmt3 = (Stmt) unit3;
																						if (stmt3
																								.containsInvokeExpr()) {
																							if (stmt3.getInvokeExpr()
																									.getMethod()
																									.equals(sMethod)) {
																								if (stmt3
																										.getInvokeExpr()
																										.getArgCount() > 0) {
																									for (Value param : stmt3
																											.getInvokeExpr()
																											.getArgs()) {
																										if (judgeService(
																												handleString(
																														param.toString()))) {
																											serviceName = handleString(
																													param.toString());
																											set.add(handleString(
																													serviceName));
																										}
																									}
																								}
																							}
																						}

																					}
																				}

																			}
																		}
																	} else if (serviceName != null) {
																		if (judgeService(handleString(serviceName))) {
																			set.add(handleString(serviceName));
																		}

																	}
																}

															}
														}
													}
												}
											}
										}

									}
								}
							}

						}

					}
				}
			}
		}

		return serviceName;

	}

	public String searchCompontentName(Value value, Body body) {
		String resultString = null;
		for (Unit unit : body.getUnits()) {
			Stmt stmt = (Stmt) unit;
			if (stmt instanceof InvokeStmt || (stmt instanceof AssignStmt && stmt.containsInvokeExpr())) {
				Value leftValue = getLeftOP(unit);
				if (leftValue != null && leftValue.equals(value)) {
					if (stmt.getInvokeExpr().getMethod().getName().contains("<init>")) {
						if (stmt.getInvokeExpr().getArgCount() == 2)
							resultString = stmt.getInvokeExpr().getArg(1).toString();
					}
				}
			}
		}
		return resultString;
	}

	public Value getReturnValue(Unit unit) {
		Value returnValue = null;
		Stmt stmt = (Stmt) unit;
		if (stmt instanceof ReturnStmt) {
			returnValue = ((ReturnStmt) stmt).getOp();
		}
		return returnValue;
	}

	public Value getLeftOP(Unit unit) {
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

	public Value getFinalAssignValue(Value value, Body body) {
		Value invoker = value;
		for (Unit unit1 : body.getUnits()) {
			if (unit1 instanceof AssignStmt) {
				Value leftValue = ((AssignStmt) unit1).getLeftOp();
				if (leftValue.equals(value)) {
					List<ValueBox> ValueBoxList = unit1.getUseAndDefBoxes();
					if (ValueBoxList.size() > 2) {
						invoker = ValueBoxList.get(ValueBoxList.size() - 1).getValue();
					}
					if (judgeValueExistAssignvalue(body, invoker))
						invoker = getFinalAssignValue(invoker, body);
					else
						break;

				}
			}
		}
		return invoker;

	}

	public boolean judgeValueExistAssignvalue(Body body, Value value) {
		boolean flag = false;
		for (Unit unit : body.getUnits()) {
			if (unit instanceof AssignStmt) {
				Value leftValue = ((AssignStmt) unit).getLeftOp();
				if (leftValue.equals(value))
					flag = true;

			}
		}
		return flag;
	}

	public Value getRightOP(Unit unit) {
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

	public Value getSecondRightOP(Unit unit) {
		Value secondRightValue = null;
		Stmt stmt = (Stmt) unit;
		if (stmt instanceof InvokeStmt) {
			if (stmt.containsInvokeExpr()) {
				InvokeExpr expr = stmt.getInvokeExpr();
				List<Value> argsList = expr.getArgs();
				if (argsList.size() >= 2) {
					secondRightValue = argsList.get(1);
				}
			}
		}
		return secondRightValue;
	}

	public Value getLastRightOP(Unit unit) {
		Value v = null;
		Stmt stmt = (Stmt) unit;
		if (stmt instanceof InvokeStmt) {
			if (stmt.containsInvokeExpr()) {
				InvokeExpr expr = stmt.getInvokeExpr();
				List<Value> argsList = expr.getArgs();
				for (int i = 0; i < argsList.size(); i++) {
					v = argsList.get(i);
				}
			}
		} else if (stmt instanceof AssignStmt) {
			ValueBox vbBox = ((AssignStmt) stmt).getRightOpBox();
			v = vbBox.getValue();
		}
		return v;
	}

	public List<SootClass> getSuperClass(String sootclass) {
		List<SootClass> getSuperClasses = new ArrayList<SootClass>();
		if (Scene.v().getSootClass(sootclass).hasSuperclass()) {
			getSuperClasses.add(Scene.v().getSootClass(sootclass).getSuperclass());
			getSuperClasses.addAll(getSuperClass(Scene.v().getSootClass(sootclass).getSuperclass().toString()));
		}

		return getSuperClasses;
	}

	public boolean judgeIntentService(String sc) {
		List<SootClass> getsuperClasses = getSuperClass(sc);
		for (int i = 0; i < getsuperClasses.size(); i++) {
			if (getsuperClasses.get(i).toString().equals("android.app.IntentService"))
				return true;
		}
		return false;
	}

	public boolean judgeService(String sc) {
		List<SootClass> getsuperClasses = getSuperClass(sc);
		for (int i = 0; i < getsuperClasses.size(); i++) {
			if (getsuperClasses.get(i).toString().equals("android.app.Service"))
				return true;
		}

		return false;
	}

	public boolean judgeActivity(String sc) {
		List<SootClass> getsuperClasses = getSuperClass(sc);
		for (int i = 0; i < getsuperClasses.size(); i++) {
			if (getsuperClasses.get(i).toString().equals("android.app.ActionBarActivity")
					|| getsuperClasses.get(i).toString().equals("android.app.Activity")
					|| getsuperClasses.get(i).toString().equals("android.support.v7.app.ActionBarActivity"))
				return true;
		}

		return false;
	}

	public boolean judgeBroadcast(String sc) {
		List<SootClass> getsuperClasses = getSuperClass(sc);
		for (int i = 0; i < getsuperClasses.size(); i++) {
			if (getsuperClasses.get(i).toString().equals("android.content.BroadcastReceiver"))
				return true;
		}
		return false;
	}

	public boolean judgeServiceOverrideStart(String value) {
		boolean flag = judgeOneServiceOverrideStart(value);
		if (!flag) {
			List<SootClass> superClasses = getSuperClass(handleString(value));
			for (SootClass sc : superClasses) {
				flag = judgeOneServiceOverrideStart(sc.toString());
				if (flag)
					return flag;
			}

		}
		return flag;

	}

	public boolean judgeServiceOverrideonBind(String value) {
		boolean flag = judgeOneServiceOnbindReturnValue(value);
		if (!flag) {
			List<SootClass> superClasses = getSuperClass(handleString(value));
			for (SootClass sc : superClasses) {
				flag = judgeOneServiceOnbindReturnValue(sc.toString());
				if (flag)
					return flag;
			}

		}
		return flag;

	}

	public boolean judgeOneServiceOnbindReturnValue(String value) {
		List<Value> list = new ArrayList<>();
		if (!Scene.v().getSootClass(value).getName().startsWith("android")
				&& !Scene.v().getSootClass(value).getName().startsWith("java")) {
			for (SootMethod sm : Scene.v().getSootClass(value).getMethods()) {
				if (sm.isConcrete()) {
					if (sm.toString().contains("onBind")) {
						for (Unit unit : sm.retrieveActiveBody().getUnits()) {
							Stmt stmt = (Stmt) unit;
							if (stmt instanceof ReturnStmt) {
								list.add(((ReturnStmt) stmt).getOp());

							}
						}

					}
				}

			}
		}
		if (list.isEmpty())
			return false;
		else
			return true;
	}

	public boolean judegValueIsIdStmt(Body body, Value value) {
		for (Unit unit : body.getUnits()) {
			Stmt stmt = (Stmt) unit;
			if (stmt instanceof IdentityStmt) {
				Value leftValue = getLeftOP(unit);
				if (leftValue.equals(value)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean judgeOneServiceOverrideStart(String value) {
		if (!Scene.v().getSootClass(value).getName().startsWith("android")
				&& !Scene.v().getSootClass(value).getName().startsWith("java")) {

			for (SootMethod sm : Scene.v().getSootClass(value).getMethods()) {
				if (sm.isConcrete()) {
					if (sm.toString().contains("onStart") || sm.toString().contains("onStartCommand"))
						return true;
				}

			}
		}
		return false;

	}

	/**
	 * 根据intent匹配service name
	 */
	public List<String> getServiceWithIntent(Body body, Value value, Unit unit, CallGraph callgraph) {
		List<String> list = new ArrayList<>();
		list = getServiceWithExplicitIntent(body, value, unit, callgraph);
		return list;
	}

	private List<String> getServiceWithExplicitIntent(Body body, Value value, Unit unit1, CallGraph callgraph) {

		// System.out.println();
		// 是賦值語句,被其他方法調用

		List<Unit> units = getAllPreviousUnit(unit1, body);
		List<String> list = new ArrayList<>();
		Set<String> set = new HashSet<>();
		Value value1 = value;
		for (Unit unit : units) {
			Stmt stmt = (Stmt) unit;
			if (stmt instanceof AssignStmt && (!stmt.containsInvokeExpr())) {
				if (getLeftOP(unit) != null && getLeftOP(unit).toString().equals(value1.toString())) {
					value1 = getRightOP(unit);
					analysisStmt(unit, body, value1, set);
				}
			}
			analysisStmt(unit, body, value, set);
		}
		if (!set.isEmpty()) {
			list.addAll(set);
		}
		if (judegValueIsIdStmt(body, value)) {// as param
			list.addAll(getPreviousMethodWithService(body.getMethod(), callgraph));
		}
		return list;
	}

	private Set<String> getPreviousMethodWithService(SootMethod sm, CallGraph callGraph) {
		String name = null;
		Set<String> set = new HashSet<>();
		List<SootMethod> preMethods = new ArrayList<SootMethod>();
		if (!preMethods.contains(sm)) {
			preMethods.add(sm);
		}
		for (int i = 0; i < preMethods.size(); i++) {
			Iterator<MethodOrMethodContext> sources = new Sources(callGraph.edgesInto(preMethods.get(i)));
			while (sources.hasNext()) {
				SootMethod sourceMethod = (SootMethod) sources.next();
				if (sourceMethod.getSignature().startsWith("<android"))
					continue;
				if (sourceMethod.getName().equals("main")) {
					break;
				}
				if (!preMethods.contains(sourceMethod) && !sourceMethod.getName().equals("main")) {
					preMethods.add(sourceMethod);
					for (Unit unit : sourceMethod.retrieveActiveBody().getUnits()) {
						Stmt stmt = (Stmt) unit;
						if (stmt.containsInvokeExpr()) {
							SootMethod sMethod = stmt.getInvokeExpr().getMethod();
							if (sMethod.equals(preMethods.get(i))) {
								for (Value value : stmt.getInvokeExpr().getArgs()) {
									if (value.getType().toString().equals("android.content.Intent")) {
										System.out.println("sourceMethod:" + sourceMethod);
										Value intent = value;
										for (Unit unit2 : getAllPreviousUnit(unit, sourceMethod.retrieveActiveBody())) {
											name = analysisStmt(unit2, sourceMethod.retrieveActiveBody(), intent, set);
											if (name != null && judgeService(handleString(name))) {
												set.add(handleString(name));
											}
										}

									}
								}
							}
						}
					}
				}
			}
		}
		return set;
	}

	private String analysisStmt(Unit unit, Body body, Value value, Set<String> set) {
		String serviceName = null;
		Stmt stmt = (Stmt) unit;
		Set<String> actionList = null;
		if ((stmt instanceof InvokeStmt || stmt instanceof AssignStmt) && stmt.containsInvokeExpr()) {

			Value leftValue = getLeftOP(unit);
			if (leftValue != null && leftValue.equals(value)) {
				// System.out.println("value:" + value);
				String methodName = stmt.getInvokeExpr().getMethod().getName();
				// 在該body直接中定義
				if (methodName.equals("<init>") || methodName.equals("setClassName") || methodName.equals("setClass")) {
					for (Value value2 : stmt.getInvokeExpr().getArgs()) {
						if (handleString(value2.toString()).contains("Service")) {
							serviceName = handleString(value2.toString());
							set.add(handleString(value2.toString()));

						}
					}
					if (stmt.getInvokeExpr().getArgCount() == 2) {
						serviceName = stmt.getInvokeExpr().getArg(1).toString();
						// 如果该serviceName以$开头，则在该body中继续寻找
						if (serviceName.startsWith("$")) {
							// System.out.println("该serviceName以$开头，在该body中继续寻找...");
							serviceName = searchStringInOneBody(body, unit, serviceName, 10, set);

						}
						if (serviceName == null) {
							actionList = new HashSet<>();
							actionList.add(stmt.getInvokeExpr().getArg(0).toString());
							serviceName = getServiceNameWithAction(actionList);

						}

					}
					if (stmt.getInvokeExpr().getArgCount() == 1 && methodName.equals("<init>")) {
						actionList = new HashSet<>();
						if (stmt.getInvokeExpr().getArgCount() == 1) {
							String string = stmt.getInvokeExpr().getArg(0).toString();
							if (string.startsWith("$")) {
								string = searchStringInOneBody(body, unit, string, 10, set);

							}
							if (string != null) {
								actionList.add(string);
							}
						}
						System.out.println("action:" + actionList);
						serviceName = getServiceNameWithAction(actionList);

					}
				}

				else if (methodName.equals("setComponent")) {
					Value value2 = stmt.getInvokeExpr().getArg(0);
					for (Unit unit2 : getAllPreviousUnit(unit, body)) {
						analysisStmt(unit2, body, value2, set);
					}
				}

				else if (methodName.equals("setAction") || methodName.equals("addAction")) {
					actionList = new HashSet<>();
					if (stmt.getInvokeExpr().getArgCount() == 1) {
						actionList.add(stmt.getInvokeExpr().getArg(0).toString());
					}
					// System.out.println("action:"+actionList);
					System.out.println("action:" + actionList);
					serviceName = getServiceNameWithAction(actionList);
				}
				// 調用其他方法的返回值
				else {
					SootMethod sm = stmt.getInvokeExpr().getMethod();
					SootClass sc = sm.getDeclaringClass();
					if (!sc.getName().startsWith("android") && !sc.getName().startsWith("java")) {
						serviceName = searchServiceName(sc, sm, set);
					}

				}
				if (serviceName != null && (!serviceName.startsWith("$")) && judgeService(handleString(serviceName))) {
					set.add(handleString(serviceName));
					return serviceName;
				}
			}
		}
		return serviceName;
	}

	private List<Unit> getAllPreviousUnit(Unit unit1, Body body) {
		List<Unit> units = new ArrayList<>();
		for (Unit unit : body.getUnits()) {
			units.add(unit);
			if (unit.equals(unit1)) {
				break;
			}
		}
		List<Unit> list = new ArrayList<>();
		for (int i = units.size() - 1; i >= 0; i--) {
			list.add(units.get(i));
		}
		return list;

	}

	public boolean serarchStopSelfInClass(String serviceName) {
		for (SootMethod sm : Scene.v().getSootClass(handleString(serviceName)).getMethods()) {
			try {
				if (sm.isConcrete()) {
					if(sm.getName().equals("onStartCommand")||sm.getName().equals("onStart")) {
						Body body=sm.retrieveActiveBody();
						for(Unit unit:body.getUnits()) {
							Stmt stmt=(Stmt) unit;
							if((stmt instanceof AssignStmt||stmt instanceof InvokeStmt)&&stmt.containsInvokeExpr()) {
								SootMethod sMethod=stmt.getInvokeExpr().getMethod();
								if(sMethod.getName().equals("stopSelf"))
									return true;
								else {
									if(searchStopSelfInMethod(sMethod))
										return true;
								}
							}
						}
					}
				}
			} catch (Exception e) {
			}
		}

		return false;
	}

	private boolean searchStopSelfInMethod(SootMethod sm) {
		if (sm.isConcrete()) {
			Body body = sm.retrieveActiveBody();
			for (Unit unit : body.getUnits()) {
				Stmt stmt = (Stmt) unit;
				if ((stmt instanceof AssignStmt || stmt instanceof InvokeStmt) && stmt.containsInvokeExpr()) {
					SootMethod sMethod = stmt.getInvokeExpr().getMethod();
					if (sMethod.getName().equals("stopSelf"))
						return true;
				}
			}
		}
		return false;
	}

	public List<String> getBoundServiceMethods(String service) {
		List<String> methodsList = new ArrayList<>();
		List<SootClass> superClasses = getSuperClass(handleString(service));
		superClasses.add(0, Scene.v().getSootClass(service));
		for (SootClass sc : superClasses) {
			if (!sc.getName().startsWith("android") && !sc.getName().startsWith("java")) {
				String sootclass = null;
				for (SootMethod sm : sc.getMethods()) {
					if (sm.toString().contains("onBind")) {
						if (sm.isConcrete()) {
							Body body = sm.retrieveActiveBody();
							Iterator<Unit> iterator = body.getUnits().snapshotIterator();
							while (iterator.hasNext()) {
								Unit unit = iterator.next();
								if (unit instanceof ReturnStmt) {
									Value ReturnPara = ((ReturnStmt) unit).getOp();
									sootclass = ReturnPara.getType().toString();
									if (sootclass != null) {
										methodsList = getMethodsAccordingonBindReturn(sootclass);
										break;
									}
								}
							}
						}
					}
				}
			}

		}
		return methodsList;
	}

	public List<String> getMethodsAccordingonBindReturn(String sootclass) {
		List<String> methodsList = new ArrayList<>();
		List<SootClass> superClasses = getSuperClass(handleString(sootclass));
		superClasses.add(0, Scene.v().getSootClass(sootclass));
		for (SootClass sc : superClasses) {
			if (!sc.getName().startsWith("android") && !sc.getName().startsWith("java")) {
				for (SootMethod sm : sc.getMethods())
					if (sm.isConcrete() && !sm.isConstructor())
						methodsList.add(sm.getSignature());
			}
		}

		return methodsList;
	}

	public List<String> handleServiceList(List<String> serviceList) {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < serviceList.size(); i++) {
			if (serviceList.get(i) != null && (!serviceList.get(i).toString().startsWith("$")))
				list.add(serviceList.get(i));
		}

		return list;

	}

	public String handleString(String value) {
		String string = value.replaceAll("/", ".");
		try {
			if (string.contains(" ")) {
				string = string.substring(string.indexOf(" ") + 2, string.length() - 1);
				if (string.startsWith("L"))
					string = string.substring(1, string.length() - 1);
			}
			if (string.contains("\"")) {
				string = string.replaceAll("\"", "");
			}
		} catch (Exception e) {

		}

		return string;

	}

	public List<String> removeDuplicates(List<String> slist) {
		Set<String> set = new HashSet<>();
		List<String> list2 = new ArrayList<>();
		set.addAll(slist);
		list2.addAll(set);
		return list2;
	}

}
