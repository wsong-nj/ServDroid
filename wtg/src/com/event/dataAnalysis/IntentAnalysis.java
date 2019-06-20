package com.event.dataAnalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.event.CGExporter;
import com.event.EventHandler;
import com.event.Util.basicAnalysis;
import com.event.com.test.xmldata.ProcessManifest;
import com.event.wtgStructure.WTG;
import com.event.wtgStructure.WTGEdge;
import com.event.wtgStructure.WTGNode;

import soot.Body;
import soot.MethodOrMethodContext;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Sources;

public class IntentAnalysis extends basicAnalysis {
	public ProcessManifest processMan;
	public List<SootMethod> Classes;
	public WTG wtg = null;
	public CallGraph callGraph = null;
	public static Map<String, Set<MethodWithUnit>> serviceMap = new HashMap<>();

	public IntentAnalysis(String apkFileLocation, CallGraph callGraph) {
		super();
		serviceMap.clear();
		wtg = new WTG();
		this.callGraph = callGraph;
		processMan = new ProcessManifest();
		processMan.loadManifestFile(apkFileLocation);
		WTGNode launcherNode = new WTGNode(processMan.getMainActivity());
		wtg.addLaunchNode(launcherNode);
	}
	
	public void doAnalysis() {
		String[] serviceStr = {"startService", "bindService"};
		getService(Arrays.asList(serviceStr), processMan.serviceWithAction);
		String[] activityStr = {"startActivity", "startActivityForResult"};
		getActivity(Arrays.asList(activityStr), processMan.activityWithAction);
	}
	
	public void getService(List<String> str, Map<String, HashSet<String>> serviceWithActionInProcessMan) {
		Map<MethodWithUnit, Set<String>> startServiceMethodToTargetServiceMap = parseIntent(str, serviceWithActionInProcessMan);
		for(MethodWithUnit mUnit : startServiceMethodToTargetServiceMap.keySet()) {
			System.out.println(mUnit.getMethod() + ": \n\t" + startServiceMethodToTargetServiceMap.get(mUnit));
			for(String service : startServiceMethodToTargetServiceMap.get(mUnit)) {
				service = HandleString(service);
				Set<MethodWithUnit> startServiceMethodSet = serviceMap.get(service);
				if(startServiceMethodSet == null) {
					Set<MethodWithUnit> methodSet = new HashSet<>();
					methodSet.add(mUnit);
					serviceMap.put(service, methodSet);
				}else {
					startServiceMethodSet.add(mUnit);
				}
			}
		}
	}
	
	public void getActivity(List<String> startActivityStr, Map<String, HashSet<String>> activityWithActionInProcessMan) {
		Map<MethodWithUnit, Set<String>> startActivityMethodToTargetActivityMap = parseIntent(startActivityStr, activityWithActionInProcessMan);
		for(MethodWithUnit mUnit : startActivityMethodToTargetActivityMap.keySet()) {
			List<SootMethod> methods = new ArrayList<>();
			List<EventHandler> eventList = new ArrayList<>();
			Map<String, Set<List<EventHandler>>> activities = new HashMap<>();
			InterAnalysis.itemId = null;
			InterAnalysis.dfs(mUnit.getMethod(), mUnit.getU(), callGraph, methods, eventList, activities);
			System.out.println(mUnit.getMethod() + ": \n\t" + activities + "\n\t" + startActivityMethodToTargetActivityMap.get(mUnit)+"\n");
			wtgBuild(activities, startActivityMethodToTargetActivityMap.get(mUnit));
		}
	}

	/**
	 * 根据给定的带有参数为Intent的stmt语句得到Intent的第二个具体参数值，主要用于寻找通过intent启动的activity或service
	 * @param str ： 带有参数为Intent的语句
	 * @return Map<语句所在的方法和语句，启动的activity或service>
	 */
	public Map<MethodWithUnit, Set<String>> parseIntent(List<String> strList, Map<String, HashSet<String>> activityWithActionInProcessMan) {
		Map<MethodWithUnit, Set<String>> methodToTargetClassMap = new HashMap<>();
		for (SootClass sc : classesChain) {
			for (SootMethod sm : sc.getMethods()) {
				if (sm.isConcrete()) {
					Value intent = null;
					Body body = sm.retrieveActiveBody();
					for (Unit unit : body.getUnits()) {
						Stmt stmt = (Stmt) unit;
						if ((stmt instanceof AssignStmt || stmt instanceof InvokeStmt) && stmt.containsInvokeExpr()) {
							String methodName = stmt.getInvokeExpr().getMethod().getName();
							if (strList.contains(methodName)) {
								Classes = new ArrayList<SootMethod>();
								for (Value value : stmt.getInvokeExpr().getArgs()) {
									if (value.getType().toString().equals("android.content.Intent")) {
										intent = value;
										Map<SootMethod, Set<String>> name = getActivityWithIntent(body, intent, unit, activityWithActionInProcessMan);
										
										for(SootMethod method : name.keySet()) {
											
											MethodWithUnit methodWithUnit = new MethodWithUnit(method, unit);
											Set<String> targets = methodToTargetClassMap.get(methodWithUnit);
											if(targets == null) {
												Set<String> targs = new HashSet<>(name.get(method));
												methodToTargetClassMap.put(methodWithUnit, targs);
											}else {
												targets.addAll(name.get(method));
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
		return methodToTargetClassMap;
	}
	
	private void wtgBuild(Map<String, Set<List<EventHandler>>> srcInfo, Set<String> tgtInfo) {
		for(String act : srcInfo.keySet()) {
			WTGNode srcNode = new WTGNode(act);
			srcNode = wtg.addNode(srcNode);
			for(String tgt : tgtInfo) {
				WTGNode tgtNode = new WTGNode(HandleString(tgt));
				tgtNode = wtg.addNode(tgtNode);
				WTGEdge edge = new WTGEdge(srcNode, tgtNode, srcInfo.get(act));
				wtg.addEdge(edge);
				}
		}
	}
	
	public WTG getWTG() {
		return wtg;
	}
	
	/**
	 * 根据intent匹配activity name
	 */
	private Map<SootMethod, Set<String>> getActivityWithIntent(Body body, Value value, Unit unit, Map<String, HashSet<String>> activityWithActionInProcessMan) {
		Map<SootMethod, Set<String>> map = new HashMap<>();
		map = getActivityWithExplicitIntent(body, value, unit, activityWithActionInProcessMan);
		// if (activityName == null) {
		// activityName = getActivityWithImplicitIntent(body, value);
		// }
		// System.out.println("activityName:" + HandleString(activityName));

		return map;
	}

	private Map<SootMethod, Set<String>> getActivityWithExplicitIntent(Body body, Value value,
			Unit unit1, Map<String, HashSet<String>> activityWithActionInProcessMan) {

		//System.out.println();
		// 是賦值語句,被其他方法調用

		List<Unit> units = getAllPreviousUnit(unit1, body);
		Map<SootMethod, Set<String>> map = new HashMap<>();
		Set<String> set = new HashSet<>();
		for (Unit unit : units) {
			Stmt stmt = (Stmt) unit;
			if (stmt instanceof AssignStmt && (!stmt.containsInvokeExpr())) {
				if (GetLeftOP(unit) != null && GetLeftOP(unit).equals(value)) {
					value = GetRightOP(unit);
				}
			}
			analysisStmt(unit, body, value, set, activityWithActionInProcessMan);
		}
		if (!set.isEmpty()) {
			if (map.isEmpty() || !map.containsKey(body.getMethod()))
				map.put(body.getMethod(), set);
			else {
				for (String str : map.get(body.getMethod())) {
					set.add(str);
				}
				map.put(body.getMethod(), set);
			}
		}
		//
		if (judegValueIsIdStmt(body, value)) {
			map.putAll(getPreviousMethodWithActivity(body.getMethod(), activityWithActionInProcessMan));
		}
		return map;
	}

	private Map<SootMethod, Set<String>> getPreviousMethodWithActivity(SootMethod sm, Map<String, HashSet<String>> activityWithActionInProcessMan) {
		String name = null;
		Map<SootMethod, Set<String>> map = new HashMap<>();
		List<SootMethod> preMethods = new ArrayList<SootMethod>();
		if (!preMethods.contains(sm)) {
			preMethods.add(sm);
		}
		for (int i = 0; i < preMethods.size(); i++) {
			Iterator<MethodOrMethodContext> sources = new Sources(callGraph.edgesInto(preMethods.get(i)));
			// Iterator it = callGraph.edgesInto(m);
			while (sources.hasNext()) {
				SootMethod sourceMethod = (SootMethod) sources.next();
				if (sourceMethod.getSignature().startsWith("<android"))
					continue;
				if (sourceMethod.getName().equals("main")) {
					break;
				}
				if (!preMethods.contains(sourceMethod) && !sourceMethod.getName().equals("main")) {
					preMethods.add(sourceMethod);
					Set<String> set = new HashSet<>();
					for (Unit unit : sourceMethod.retrieveActiveBody().getUnits()) {
						Stmt stmt = (Stmt) unit;
						if (stmt.containsInvokeExpr()) {
							SootMethod sMethod = stmt.getInvokeExpr().getMethod();
							if (sMethod.equals(preMethods.get(i))) {
								for (Value value : stmt.getInvokeExpr().getArgs()) {
									if (value.getType().toString().equals("android.content.Intent")) {
										Value intent = value;
										for (Unit unit2 : getAllPreviousUnit(unit, sourceMethod.retrieveActiveBody())) {
											// System.out.println("value:"+intent);
											name = analysisStmt(unit2, sourceMethod.retrieveActiveBody(), intent, set, activityWithActionInProcessMan);
											if (name != null) {
												set.add(name);
											}
										}

									}
								}
							}
						}
					}
					if (!set.isEmpty()) {
						if (map.isEmpty() || !map.containsKey(sourceMethod))
							map.put(sourceMethod, set);
						else {
							for (String str : map.get(sourceMethod)) {
								set.add(str);
							}
							map.put(sourceMethod, set);
						}
					}
				}
			}
		}
		return map;
	}

	private String analysisStmt(Unit unit, Body body, Value value, Set<String> set, Map<String, HashSet<String>> activityWithActionInProcessMan) {
		String activityName = null;
		Stmt stmt = (Stmt) unit;
		if ((stmt instanceof InvokeStmt || stmt instanceof AssignStmt) && stmt.containsInvokeExpr()) {

			Value leftValue = GetLeftOP(unit);
			if (leftValue != null && leftValue.equals(value)) {
				// System.out.println("value:" + value);
				String methodName = stmt.getInvokeExpr().getMethod().getName();
				// 在該body直接中定義
				if (methodName.equals("<init>") || methodName.equals("setClassName") || methodName.equals("setClass")) {
					if (stmt.getInvokeExpr().getArgCount() == 2) {
						activityName = stmt.getInvokeExpr().getArg(1).toString();
						// 如果该activityName以$开头，则在该body中继续寻找
						if (activityName.startsWith("$")) {
							// System.out.println("该activityName以$开头，在该body中继续寻找...");
							activityName = SearchStringInOneBody(body, unit, activityName, 10, set, activityWithActionInProcessMan);

						}
						if (activityName == null) {
							Set<String> actionList = new HashSet<>();
							actionList.add(stmt.getInvokeExpr().getArg(0).toString());
							activityName = getActivityNameWithAction(actionList, activityWithActionInProcessMan);

						}

					}
					if (stmt.getInvokeExpr().getArgCount() == 1 && methodName.equals("<init>")) {
						Set<String> actionList = new HashSet<>();
						if (stmt.getInvokeExpr().getArgCount() == 1) {
							String string = stmt.getInvokeExpr().getArg(0).toString();
							if (string.startsWith("$")) {
								string = SearchStringInOneBody(body, unit, string, 10, set, activityWithActionInProcessMan);

							}
						if(string!=null) {
							actionList.add(string);
						}
						}
							activityName = getActivityNameWithAction(actionList, activityWithActionInProcessMan);

					}
				}

				else if (methodName.equals("setComponent")) {
					Value value2 = stmt.getInvokeExpr().getArg(0);
					for (Unit unit2 : getAllPreviousUnit(unit, body)) {
						analysisStmt(unit2, body, value2, set, activityWithActionInProcessMan);
					}
				}

				else if (methodName.equals("setAction") || methodName.equals("addAction")) {
					Set<String> actionList = new HashSet<>();
					if (stmt.getInvokeExpr().getArgCount() == 1) {
						actionList.add(stmt.getInvokeExpr().getArg(0).toString());
					}
					// System.out.println("action:"+actionList);
					activityName = getActivityNameWithAction(actionList, activityWithActionInProcessMan);
				}
				// 調用其他方法的返回值
				else {
					SootMethod sm = stmt.getInvokeExpr().getMethod();
					SootClass sc = sm.getDeclaringClass();
					if (!sc.getName().startsWith("android") && !sc.getName().startsWith("java")
							&& !sc.getName().startsWith("org")) {
						activityName = SearchActivityName(sc, sm, set, activityWithActionInProcessMan);
					}

				}
				if (activityName != null && (!activityName.startsWith("$"))) {
					set.add(activityName);
					return activityName;
				}
			}
		}
		return activityName;
	}

	public String getActivityWithImplicitIntent(Body body, Value value, Map<String, HashSet<String>> activityWithActionInProcessMan) {
		String activityName = null;
		Set<String> actionList = new HashSet<>();
		for (Unit unit : body.getUnits()) {
			Stmt stmt = (Stmt) unit;
			if ((stmt instanceof InvokeStmt || stmt instanceof AssignStmt) && stmt.containsInvokeExpr()) {
				Value leftValue = GetLeftOP(unit);
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
		// System.out.println("action:"+actionList);
		activityName = getActivityNameWithAction(actionList, activityWithActionInProcessMan);
		return activityName;

	}

	private String getActivityNameWithAction(Set<String> actions, Map<String, HashSet<String>> activityWithActionInProcessMan) {
		String activityName = null;
		Set<String> set = activityWithActionInProcessMan.keySet();
		for (Iterator<String> iterator = set.iterator(); iterator.hasNext();) {
			String value = iterator.next();
			boolean flag = false;
			Set<String> actionSet = activityWithActionInProcessMan.get(value);
			for (String action : actions) {
				if (actionSet.contains(HandleString(action))) {
					flag = true;
				}
				if (!actionSet.contains(HandleString(action))) {
					flag = false;
				}
			}
			if (flag) {
				activityName = value;
			}

		}
		return activityName;
	}

	public String SearchActivityName(SootClass sc, SootMethod method, Set<String> set, Map<String, HashSet<String>> activityWithActionInProcessMan) {
		System.out.println();
		Classes.add(method);
		Value value = null;
		String activityName = null;
		for (SootMethod sm : sc.getMethods()) {
			if (sm.equals(method)) {
				if (sm.isConcrete()) {
					Body body = sm.retrieveActiveBody();
					Iterator<Unit> iterator = body.getUnits().snapshotIterator();
					while (iterator.hasNext()) {
						Unit unit = iterator.next();
						if (unit instanceof ReturnStmt) {
							value = GetReturnValue(unit);
							break;
						}
					}

				}
			}
		}
		if (value != null) {
			for (SootMethod sm : sc.getMethods()) {
				if (sm.equals(method)) {
					if (sm.isConcrete()) {
						Body body = sm.retrieveActiveBody();
						Iterator<Unit> iter = body.getUnits().snapshotIterator();
						while (iter.hasNext()) {
							Unit unit = iter.next();
							Stmt stmt = (Stmt) unit;
							Value result = GetLeftOP(unit);
							if (result != null && result.equals(value)) {
								if (stmt.containsInvokeExpr()) {
									activityName = analysisStmt(unit, body, value, set, activityWithActionInProcessMan);
								}

								else {
									if (stmt instanceof AssignStmt) {
										Value value2 = GetRightOP(unit);
										for (SootMethod sMethod : sc.getMethods()) {
											if (sMethod.isConcrete()) {
												for (Unit unit2 : sMethod.retrieveActiveBody().getUnits()) {
													Value rightValue = GetRightOP(unit2);
													if (rightValue != null
															&& value2.toString().equals(rightValue.toString())) {
														Value leftvalue = GetLeftOP(unit2);
														for (Unit unit4 : sMethod.retrieveActiveBody().getUnits()) {
															Stmt stmt4 = (Stmt) unit4;
															if (GetLeftOP(unit4) != null
																	&& GetLeftOP(unit4).equals(leftvalue)
																	&& stmt4.containsInvokeExpr()) { // InvokeStmt
																String methodName = stmt4.getInvokeExpr().getMethod()
																		.getName();
																if (methodName.equals("<init>")
																		|| methodName.contains("setClass")
																		|| methodName.contains("setClassName")) {
																	if (stmt4.getInvokeExpr().getArgCount() == 2) {
																		activityName = stmt4.getInvokeExpr().getArg(1)
																				.toString();
																		if (activityName.toString().startsWith("$")) {
																			activityName = SearchStringInOneBody(
																					sMethod.retrieveActiveBody(), unit4,
																					activityName, 7, set, activityWithActionInProcessMan);
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
																										if (judgeActivity(
																												HandleString(
																														param.toString()))) {
																											activityName = HandleString(
																													param.toString());
																											set.add(activityName);
																										}
																									}
																								}
																							}
																						}

																					}
																				}

																			}
																		}
																	} else if (activityName != null) {
																		if (judgeActivity(HandleString(activityName))) {
																			set.add(activityName);
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

		return activityName;

	}

	public String SearchStringInOneBody(Body body, Unit unit1, String value, int depth, Set<String> set, Map<String, HashSet<String>> activityWithActionInProcessMan) {
		String value2 = null;
		for (Unit unit : getAllPreviousUnit(unit1, body)) {
			if (unit instanceof AssignStmt) {
				if (GetLeftOP(unit).toString().equals(value)) {
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
							if (!sc.getName().startsWith("android") && !sc.getName().startsWith("java")
									&& !sc.getName().startsWith("org")) {
								value2 = SearchActivityName(sc, sm, set, activityWithActionInProcessMan);
								if(value2 != null) {
									set.add(value2);
									return value2;									
								}
							}
						}

					}

					else {
						value2 = GetRightOP(unit).toString();
						if (value2.contains(".")) {
							value2 = value2.substring(value2.indexOf("."));
							return searchStringInInit(body, value2, set, activityWithActionInProcessMan);
						} else if (value2.startsWith("$") && depth < 10) {
							return SearchStringInOneBody(body, unit, value2, ++depth, set, activityWithActionInProcessMan);
						} else {
							return value2;
						}
					}
				}
			}

			if (unit instanceof IdentityStmt) {
				if (GetLeftOP(unit).toString().equals(value)) {
					return GetRightOP(unit).toString();
				}
			}
		}
		return null;
	}

	public String searchStringInInit(Body body, String value, Set<String> set, Map<String, HashSet<String>> activityWithActionInProcessMan) {
		String string = null;
		SootClass sClass = body.getMethod().getDeclaringClass();
		for (SootMethod sm : sClass.getMethods()) {
			if (sm.getName().equals("<init>")) {
				for (Unit unit : sm.retrieveActiveBody().getUnits()) {
					try {
						String leftValue = GetLeftOP(unit).toString();
						if (leftValue.contains(value)) {
							return SearchStringInOneBody(sm.retrieveActiveBody(), unit, GetRightOP(unit).toString(), 7,
									set, activityWithActionInProcessMan);
						}
					} catch (Exception e) {
					}

				}
			}
		}
		return string;
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

	private List<Unit> getAllUnitsBetweenUnit(Unit unit1, Unit unit2, Body body) {
		List<Unit> units1 = getAllPreviousUnit(unit2, body);
		List<Unit> units2 = getAllPreviousUnit(unit1, body);
		units1.removeAll(units2);
		return units1;
	}

}

