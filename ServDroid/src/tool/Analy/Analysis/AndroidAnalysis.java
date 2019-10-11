package tool.Analy.Analysis;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.event.EventHandler;
import com.event.dataAnalysis.InterAnalysis;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.toolkits.graph.BriefUnitGraph;
import soot.util.Chain;
import tool.Analy.MethodAnalysis.InterMethodAnalysis;
import tool.Analy.MethodAnalysis.IntraMethodAnalysis;
import tool.entryForAllApks.EntryForAll;

public class AndroidAnalysis extends BasicAnalysis {

	public boolean flagEnableService;// app enables Service or not
	public String AppName;// app name
	public int LinesofCode;// the count of code lines
	public List<String> startServicelist;
	public Map<String, SootMethod> startServiceAndDirectMap;
	public Map<String, SootMethod> stopServiceAndDirectMap;
	public List<String> bindServiceList;
	public List<String> stopServicelist;
	public List<String> unboundServiceList;
	public List<String> serviceAfterAppClosed;
	public List<SootClass> bindServiceCaller;
	public Map<String, SootClass> bindServiceAndDirectedCaller;
	public List<String> hyList;
	public CallGraph callGraph = null;
	public static boolean flag;
	public static int PCBs;
	public static int LDBs;
	public static int PDBs;
	public static int SLBs;
	public static int total;
	public int startServiceCount;
	public int bindServiceCount;
	public int hybridServiceCount;
	public String apkFileLocation;
	int SServiceLeakCount;
	int SPreDestroyCount;
	int SLateDestroyCount;
	int BServiceLeakCount;
	int BLateDestroyCount;
	int BPreCreateCount;
	int HServiceLeakCount;
	int PrehybridService;
	int HPreCreateCount;
	int LatehybridService;
	StringBuffer output;

	public AndroidAnalysis(String apkFileLocation, CallGraph callGraph) {

		super(apkFileLocation);
		this.callGraph = callGraph;
		LinesofCode = 0;
		flag = false;
		flagEnableService = false;
		HPreCreateCount = 0;
		LDBs = 0;
		PDBs = 0;
		SLBs = 0;
		total = 0;
		BServiceLeakCount = 0;
		SServiceLeakCount = 0;
		HServiceLeakCount = 0;
		SPreDestroyCount = 0;
		SLateDestroyCount = 0;
		BLateDestroyCount = 0;
		PrehybridService = 0;
		LatehybridService = 0;
		startServiceCount = 0;
		bindServiceCount = 0;
		hybridServiceCount = 0;
		BPreCreateCount = 0;
		startServicelist = new ArrayList<String>();
		bindServiceList = new ArrayList<String>();
		stopServicelist = new ArrayList<String>();
		unboundServiceList = new ArrayList<String>();
		bindServiceCaller = new ArrayList<SootClass>();
		serviceAfterAppClosed = new ArrayList<String>();
		bindServiceAndDirectedCaller = new HashMap<String, SootClass>();
		startServiceAndDirectMap = new HashMap<String, SootMethod>();
		stopServiceAndDirectMap = new HashMap<String, SootMethod>();
		hyList = new ArrayList<String>();
		output = new StringBuffer();
		this.apkFileLocation = apkFileLocation;

	}

	public void Analyze() {// Start analysis

		System.out.println("---------------------------Analysis Begin---------------------------");
		analyse();

		System.out.println("---------------------------Analysis End---------------------------");
	}

	public void analyse() {
		output.append("the apk:" + apkFileLocation + "\n");
		useService(classesChain);
		if (flagEnableService == true) {
			System.out.println("The app uses Service");
			searchStartServiceList();
			List<String> startList = handleServiceList(startServicelist);
			searchStopService(startList);
			searchBindServiceList();
			List<String> boundList = handleServiceList(bindServiceList);
			hyList.addAll(startList);
			hyList.retainAll(boundList);
			judgeFirstPattern(hyList, startList);
			System.out.println();
			System.out.println("Analyse service leak:");
			List<String> boundServiceLeakList = new ArrayList<String>();
			for (int i = 0; i < bindServiceList.size(); i++) {
				String value = bindServiceList.get(i);
				if (value != null && !hyList.contains(value)) {
					if (!searchunBindService(bindServiceCaller.get(i))) {
						BServiceLeakCount++;
						boundServiceLeakList.add(value);
						output.append(handleString(value.toString()) + "is bound service and exist leak" + "\n");
					}
				}
			}

			startList.removeAll(stopServicelist);
			startList.removeAll(hyList);
			for (int i = 0; i < startList.size(); i++) {
				SServiceLeakCount++;
				output.append(handleString(startList.get(i).toString()) + "is started service and exist leak" + "\n");
			}
			List<String> hycopyList = new ArrayList<String>();
			List<String> hycopyList1 = new ArrayList<String>();
			hycopyList.addAll(hyList);
			hycopyList1.addAll(hyList);
			hycopyList.removeAll(stopServicelist);
			for (int i = 0; i < hycopyList.size(); i++) {
				HServiceLeakCount++;
				startServicelist.remove(hycopyList.get(i));
				bindServiceList.remove(hycopyList.get(i));
				output.append(handleString(hycopyList.get(i).toString()) + " is hybrid service and exist leak" + "\n");
			}
			hycopyList1.removeAll(hycopyList);
			Iterator<String> iterator = hycopyList1.iterator();
			while (iterator.hasNext()) {
				String value = iterator.next();
				if (value != null) {
					if (!searchunBindService(bindServiceAndDirectedCaller.get(handleString(value)))) {
						startServicelist.remove(value);
						bindServiceList.remove(value);
						HServiceLeakCount++;
						output.append(handleString(value.toString()) + " is hybrid service and exist leak" + "\n");
					}
				}

			}
			System.out.println("Analyse Third Pattern");
			startServicelist.removeAll(startList);
			System.out.println("Analyse started services:");
			judgeStartService(startServicelist);
			System.out.println("Analyse bound services:");
			bindServiceList.removeAll(boundServiceLeakList);
			judgeBindService(bindServiceList);
			System.out.println("Analyse hybrid services:");
			judgeHybridService(startServicelist, bindServiceList);
			LDBs = SLateDestroyCount + BLateDestroyCount + LatehybridService;
			PDBs = SPreDestroyCount + PrehybridService;
			SLBs = BServiceLeakCount + SServiceLeakCount + HServiceLeakCount;
			PCBs = BPreCreateCount + HPreCreateCount;
			total = LDBs + PDBs + SLBs + PCBs;
			output.append("PCBs:" + PCBs + "\n");
			output.append("PDBs:" + PDBs + "\n");
			output.append("LDBs:" + LDBs + "\n");
			output.append("SLBs:" + SLBs + "\n");
			output.append("total:" + total + "\n");
			output.append("startService PDBs,LDBs,SLBs:" + SPreDestroyCount + "," + SLateDestroyCount + ","
					+ SServiceLeakCount + "\n");
			output.append("bindService PCBs,LDBs,SLBs:" + BPreCreateCount + "," + BLateDestroyCount + ","
					+ BServiceLeakCount + "\n");
			output.append("hybridService PCBs,PDBs,LDBs,SLBs:" + HPreCreateCount + "," + PrehybridService + ","
					+ LatehybridService + "," + HServiceLeakCount + "\n");

			System.out.println(PCBs + " " + PDBs + " " + LDBs + " " + SLBs + " " + total);

		} else {
			output.append("The app does not use Service" + "\n");
			System.out.println("The app does not use Service");
		}

		try {
			FileWriter fw = new FileWriter("./output.txt", true);
			fw.write(output.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param classesChain
	 */
	public void useService(List<SootClass> classesChain) { // Analyze whether
		for (SootClass sc : classesChain) {
			try {
				for (SootMethod sm : sc.getMethods()) {
					if (sm.isConcrete() && !sm.isConstructor()) {
						Body body = sm.retrieveActiveBody();
						for (Unit unit : body.getUnits()) {
							LinesofCode++;
							Stmt stmt = (Stmt) unit;
							if (stmt instanceof InvokeStmt && stmt.containsInvokeExpr()) {
								if (stmt.getInvokeExpr().getArgCount() > 0) {
									String methodName = stmt.getInvokeExpr().getMethod().getName();
									if (methodName.contains("startService") || methodName.contains("bindService")) {
										flagEnableService = true;
									}
								}

							}
						}

					}

				}
			} catch (Exception e) {
			}
		}

	}

	/**
	 * get started Service list
	 * 
	 * @return
	 */
	public void searchStartServiceList() {
		for (SootClass sc : classesChain) {
			try {
				for (SootMethod sm : sc.getMethods()) {
					if (sm.isConcrete()) {
						Body body = sm.retrieveActiveBody();
						Iterator<Unit> iter = body.getUnits().snapshotIterator();
						while (iter.hasNext()) {
							Unit unit = iter.next();
							Stmt stmt = (Stmt) unit;
							if ((stmt instanceof InvokeStmt || stmt instanceof AssignStmt)
									&& stmt.containsInvokeExpr()) {
								if (stmt.getInvokeExpr().getArgCount() > 0) {
									String methodName = stmt.getInvokeExpr().getMethod().getName();
									if (methodName.contains("startService")) {
										Value value = null;
										for (Value param : stmt.getInvokeExpr().getArgs()) {
											if (param.getType().toString().equals("android.content.Intent"))
												value = param;
										}
										if (value != null) {
											List<String> list = getServiceWithIntent(body, value, unit, callGraph);

											if (!list.isEmpty()) {
												System.out.println(sc.toString());
												System.out.println(sm.toString());
												System.out.println(unit.toString());
												System.out.println("search value is:" + value.toString());
												System.out.println("the serviceName is:" + list);
												System.out.println();												
												for (String serviceName : list) {
													startServicelist.add(handleString(serviceName));
													startServiceAndDirectMap.put(handleString(serviceName), sm);
													Set<String> set = new HashSet<>();
													List<SootMethod> methods = new ArrayList<>();
													List<EventHandler> eventList = new ArrayList<>();
													Map<String, Set<List<EventHandler>>> activities = new HashMap<>();
													SootMethod method = Scene.v().getMethod("<android.content.Context: android.content.ComponentName startService(android.content.Intent)>");
													try {
														InterAnalysis.dfs(method, null, callGraph, methods, eventList,
																activities);
													} catch (Exception e) {
														continue;
													}
													if (!activities.isEmpty()) {
														set = activities.keySet();
													} else
														set.add(sc.toString());
													System.out.println("the caller is : " + set);

												}
												
												InterMethodAnalysis IA = new InterMethodAnalysis(EntryForAll.info,EntryForAll.callGraph);
												List<Unit> pds = IA.getAllPostDominatorsOfUnit(sm, unit);
												if(pds != null) {
													for(int k=0;k<pds.size();k++) {
														Unit tu = pds.get(k);
														Stmt tstmt = (Stmt)tu;
														if(tstmt.containsInvokeExpr()) {
															if(tstmt.getInvokeExpr().getMethod().getName().equals("stopService"))
																stopServicelist.addAll(list);
														}
													}
												}
											}
											System.out.println();
										}
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
			}

		}

	}

	public void searchStopService(List<String> startServiceList) {
		for (int i = 0; i < startServiceList.size(); i++) {
			outer: {
				String value = startServiceList.get(i);
				if (!(value == null)) {
					if (serarchStopSelfInClass(handleString(value.toString()))) {
						System.out.println(handleString(value.toString()) + " exist stopSelf method.");
						stopServicelist.add(value);
						break outer;
					}
					if (!serarchStopSelfInClass(value.toString())) {
						try {
							for (SootClass sc : classesChain) {
								if (sc.toString().equals(handleString(value.toString()))) {
									if (judgeIntentService(sc.toString())) {
										System.out.println("the service extends IntentService.");
										stopServicelist.add(value);
										break;
									}
								} else {
									for (SootMethod sm : sc.getMethods()) {
										Value valueArg = null;
										try {
											if (sm.isConcrete()) {
												Body body = sm.retrieveActiveBody();
												Iterator<Unit> iterator = body.getUnits().snapshotIterator();
												while (iterator.hasNext()) {
													Unit unit = iterator.next();
													Stmt stmt = (Stmt) unit;

													if ((stmt instanceof InvokeStmt || stmt instanceof AssignStmt)
															&& stmt.containsInvokeExpr()) {
														Iterator<Value> iter = stmt.getInvokeExpr().getArgs()
																.iterator();
														while (iter.hasNext()) {
															if (handleString(iter.next().toString())
																	.contains(handleString(value.toString()))) {
																valueArg = getLeftOP(unit);
															}
														}
														String methodName = stmt.getInvokeExpr().getMethod().getName();
														if (methodName.contains("stopService")) {
															if (stmt.getInvokeExpr().getArgCount() > 0) {
																Value result = stmt.getInvokeExpr().getArg(0);
																if (result.equals(valueArg)) {
																	stopServicelist.add(value);
																	stopServiceAndDirectMap.put(value, sm);
																}
															}
														}
													}

												}

											}
										} catch (Exception e) {

										}
									}
								}
							}
						} catch (ConcurrentModificationException e) {
							// break outer;

						}

					}
				}

			}
		}

	}

	public boolean searchunBindService(SootClass value) {
		outer: try {
			for (SootClass sc : classesChain) {
				if (sc.equals(value)) {
					for (SootMethod sm : sc.getMethods()) {
						if (sm.isConcrete()) {
							Body body = sm.retrieveActiveBody();
							Iterator<Unit> iterator = body.getUnits().snapshotIterator();
							while (iterator.hasNext()) {
								Unit unit = iterator.next();
								Stmt stmt = (Stmt) unit;
								if ((stmt instanceof InvokeStmt || stmt instanceof AssignStmt)
										&& stmt.containsInvokeExpr()) {
									String methodName = stmt.getInvokeExpr().getMethod().getName();
									if (methodName.contains("unbindService")) {
										return true;

									}
								}
							}
						}
					}
				}
			}

		} catch (ConcurrentModificationException e) {
			break outer;

		}
		return false;

	}

	public Map<String, SootClass> searchBindServiceList() {
		outer: try {
			for (SootClass sc : classesChain)
				for (SootMethod sm : sc.getMethods()) {
					if (sm.isConcrete()) {
						try {
							Body body = sm.retrieveActiveBody();
							Iterator<Unit> iterator = body.getUnits().snapshotIterator();
							while (iterator.hasNext()) {
								Unit unit = iterator.next();
								Stmt stmt = (Stmt) unit;
								if ((stmt instanceof InvokeStmt || stmt instanceof AssignStmt)
										&& stmt.containsInvokeExpr()) {
									SootMethod sootMethod = stmt.getInvokeExpr().getMethod();
									if (sootMethod.getName().equals("bindService")) {
										if (stmt.getInvokeExpr().getArgCount() > 0) {
											Value value = null;
											for (Value param : stmt.getInvokeExpr().getArgs()) {
												if (param.getType().toString().equals("android.content.Intent"))
													value = param;
											}

											List<String> list = getServiceWithIntent(body, value, unit, callGraph);
											if (!list.isEmpty()) {

												System.out.println("sootClass:" + sc.toString());
												System.out.println("sootMethod:" + sm.toString());
												System.out.println("unit:" + unit.toString());
												System.out.println("search value is" + value.toString());
												System.out.println();
												
												for (String bindServiceName : list) {
													bindServiceList.add(handleString(bindServiceName));
													bindServiceCaller.add(sc);
													bindServiceAndDirectedCaller.put(handleString(bindServiceName), sc);
													Set<String> set = new HashSet<>();
													List<SootMethod> methods = new ArrayList<>();
													List<EventHandler> eventList = new ArrayList<>();
													Map<String, Set<List<EventHandler>>> activities = new HashMap<>();
													SootMethod method = Scene.v().getMethod("<android.content.Context: boolean bindService(android.content.Intent,android.content.ServiceConnection,int)>");
													try {
														InterAnalysis.dfs(method, null, callGraph, methods, eventList,
																activities);
													} catch (Exception e) {
														continue;
													}
													if (!activities.isEmpty()) {
														set = activities.keySet();
													} else
														set.add(sc.toString());
													System.out.println("the caller is : " + set);

												}

												InterMethodAnalysis IA = new InterMethodAnalysis(EntryForAll.info,EntryForAll.callGraph);
												List<Unit> pds = IA.getAllPostDominatorsOfUnit(sm, unit);
												if(pds != null) {
													for(int k=0;k<pds.size();k++) {
														Unit tu = pds.get(k);
														Stmt tstmt = (Stmt)tu;
														if(tstmt.containsInvokeExpr()) {
															if(tstmt.getInvokeExpr().getMethod().getName().equals("unbindService"))
																unboundServiceList.addAll(list);
														}
													}
												}
											}
										}
									}
								}

							}
						} catch (Exception e) {
						}
					}
				}
		} catch (ConcurrentModificationException e) {
			break outer;

		}
		// bindServiceList = handleServiceList(bindServiceList);
		return bindServiceAndDirectedCaller;

	}

	public void judgeStartService(List<String> startServicelist) {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < startServicelist.size(); i++) {
			String serviceNameValue = startServicelist.get(i);
			if (!hyList.contains(serviceNameValue)) {
				if (!list.contains(serviceNameValue)) {
					list.add(serviceNameValue);
					startServicelist.remove(i);
					if (serviceNameValue != null) {
						System.out.println("The current service is:" + handleString(serviceNameValue.toString()));

						boolean flag = startServicelist.contains(serviceNameValue);

						int returnCount = judgeOneStartService(startServicelist, serviceNameValue, flag);
						if (returnCount == 0) {
							SLateDestroyCount++;
							System.out.println(handleString(serviceNameValue) + "is started service and late destroy");
							output.append(
									handleString(serviceNameValue) + "is started service and late destroy" + "\n");
						}
						if (returnCount == 2) {
							SPreDestroyCount++;
							System.out.println(
									handleString(serviceNameValue) + "is started service and premature destroy");
							output.append(
									handleString(serviceNameValue) + "is started service and premature destroy" + "\n");
						}

					}
					startServicelist.add(i, serviceNameValue);
				}
			}
		}
		// startServicelist.addAll(list);
		// System.out.println(startServicelist);
		System.out.println();

	}

	public int judgeOneStartService(List<String> startlist, String serviceName, boolean flag) {
		int returnCount = 0;
		if (!flag) {
			System.out.println(handleString(serviceName.toString()) + " is started services and will not be shared");
			returnCount = analyseStartServiceClosedTime(serviceName.toString(), flag);
			if (returnCount == 0)
				returnCount = analyseSuperService(startlist, serviceName, flag);

		} else {
			System.out.println(handleString(serviceName.toString()) + " is started services and will may be shared");
			returnCount = analyseStartServiceClosedTime(serviceName.toString(), flag);
			if (returnCount == 0)
				returnCount = analyseSuperService(startlist, serviceName, flag);
			if (returnCount == 2) {
				System.out.println(handleString(serviceName.toString()) + " is started services and premature destroy");
			}

		}
		return returnCount;
	}

	public int analyseSuperService(List<String> list, String serviceName, boolean flag) {
		List<SootClass> superClasses = getSuperClass(handleString(serviceName.toString()));
		int returnCount = 0;
		for (SootClass sc : superClasses) {
			if (classesChain.contains(sc)) {

				returnCount = analyseStartServiceClosedTime(sc.toString(), flag);
				if (returnCount == 1)// 正常使用，有stopSelf()方法且使用正确
					return returnCount;
				else if (returnCount == 2) {// 过早销毁，有stopSelf()方法
					System.out.println(
							handleString(serviceName.toString()) + " is started services and premature destroy");
					return returnCount;
				}
			}
		}
		return returnCount;
	}

	public int analyseStartServiceClosedTime(String serviceName, boolean flag) {
		SootClass sc = Scene.v().getSootClass(serviceName);
		if (judgeIntentService(handleString(serviceName))) {
			System.out.println("The service extends IntentService, do not need to consider!");

			return 1;
		}

		else {
			for (SootMethod sm : sc.getMethods()) {
				if (sm.getName().contains("onStartCommand") || sm.getName().contains("onStart")) {
					Body body = sm.retrieveActiveBody();
					Iterator<Unit> iterator = body.getUnits().snapshotIterator();
					while (iterator.hasNext()) {
						Unit unit = iterator.next();
						Stmt stmt = (Stmt) unit;
						if (stmt instanceof InvokeStmt && stmt.containsInvokeExpr()) {
							String methodName = stmt.getInvokeExpr().getMethod().getName();

							if (methodName.contains("stopSelf") || methodName.contains("stopService")) {

								if (!flag) {// 不共享
									System.out.println(" is started services and start normal use");
									// output.append(" is started services and start normal use"+"\n");
									return 1;
								} else {// 有参数且参数类型为int，正常使用
									if (stmt.getInvokeExpr().getArgCount() > 0
											&& stmt.getInvokeExpr().getArg(0).getType().toString().equals("int")) {
										System.out.println(" is started services and start normal use ");
										// output.append(" is started services and start normal use"+"\n");
										return 1;

									} else
										return 2;

								}

							}

						}
					}
				}

			}
		}

		return 0;

	}

	public void judgeBindService(List<String> bindServiceList) {

		for (int i = 0; i < bindServiceList.size(); i++) {
			String serviceName = bindServiceList.get(i);
			if (!(serviceName == null) && (!hyList.contains(serviceName))) {
				SootClass sClass = bindServiceAndDirectedCaller.get(handleString(serviceName));
				int returnCount = judgeOneBindService(serviceName, sClass);
				if (returnCount == 0) {
					BLateDestroyCount++;
					System.out.println(handleString(serviceName.toString()) + " is bound service and Late destroy");
					output.append(handleString(serviceName) + "is bind service and late destroy" + "\n");
				}
				if (returnCount == 1) {
					System.out.println(handleString(serviceName.toString()) + " is bind service and premature create");
					output.append(handleString(serviceName) + "is bind service and pre create" + "\n");
					BPreCreateCount++;
				}

			}
		}
	}

	private int judgeOneBindService(String serviceName, SootClass sc) {// 0过晚 1过早 2正常
		boolean preCreate = judegBindService(serviceName, sc, "bindService");
		boolean lateDestroy = judegBindService(serviceName, sc, "unbindService");
		if (preCreate) {
			return 1;
		}
		if (lateDestroy) {
			return 0;
		}
		return 2;

	}

	private boolean judegBindService(String serviceName, SootClass sc, String method) {
		List<String> methods = getBoundServiceMethods(serviceName);
		for (SootMethod sm : sc.getMethods()) {
			Body body = sm.retrieveActiveBody();
			for (Unit unit : body.getUnits()) {
				Stmt stmt = (Stmt) unit;
				if (stmt.containsInvokeExpr()) {
					String methodName = stmt.getInvokeExpr().getMethod().getName();
					if (methodName.equals(method)) {
						List<String> bindlist = getPreOrSuccInvokeOfUnit(body, unit, method);
						if (methods != null && bindlist != null) {
							for (String str : bindlist) {
								if (methods.contains(str))
									return false;
								else
									return true;
							}

						}
					}
				}

			}
		}
		return false;
	}

	private List<String> getPreOrSuccInvokeOfUnit(Body body, Unit unit, String methodName) {
		BriefUnitGraph graph = new BriefUnitGraph(body);
		List<Unit> units = new ArrayList<>();
		if (methodName.equals("bindService")) {
			units = graph.getSuccsOf(unit);
		}
		if (methodName.equals("unbindService")) {
			units = graph.getPredsOf(unit);
		}

		for (Unit u : units) {
			Stmt stmt = (Stmt) u;
			if (stmt.containsInvokeExpr()) {
				List<String> list = new ArrayList<>();
				list.add(stmt.getInvokeExpr().getMethod().getSignature());
				return list;
			}
			return getPreOrSuccInvokeOfUnit(body, u, methodName);
		}
		return null;

	}

	public void judgeHybridService(List<String> startServiceList, List<String> bindserviceList) {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < startServiceList.size(); i++) {
			String serviceNameValue = startServiceList.get(i);
			if (!list.contains(serviceNameValue)) {
				list.add(serviceNameValue);
				startServiceList.remove(i);

				if (serviceNameValue != null) {
					if (bindserviceList.contains(serviceNameValue)) {
						System.out.println("the current service is:" + serviceNameValue);
						boolean count = startServiceList.contains(serviceNameValue);
						int returnCount = judgeOneStartService(startServiceList, serviceNameValue, count);
						if (returnCount == 2) {
							PrehybridService++;
							System.out.println(
									handleString(serviceNameValue) + "is hybrid service and premature destroy");
							output.append(
									handleString(serviceNameValue) + "is hybrid service and premature destroy" + "\n");
						} else {// 判断绑定服务是否过晚
							for (int k = 0; k < bindserviceList.size(); k++) {
								if (bindserviceList.get(k) != null && bindserviceList.get(k).equals(serviceNameValue)) {
									SootClass sClass = bindServiceAndDirectedCaller.get(serviceNameValue);
									if (judgeOneBindService(serviceNameValue, sClass) == 0) {
										LatehybridService++;
										System.out.println(
												handleString(serviceNameValue) + "is hybrid service and late destroy");
										output.append(handleString(serviceNameValue)
												+ "is hybrid service and late destroy" + "\n");

									} else if (judgeOneBindService(serviceNameValue, sClass) == 1) {
										output.append(handleString(serviceNameValue)
												+ "is hybrid service and pre create" + "\n");
										System.out.println(
												handleString(serviceNameValue) + "is hybrid service and pre create");
										HPreCreateCount++;
									} else {// 绑定服务使用正常,判断启动服务是否过晚
										if (returnCount == 0) {
											LatehybridService++;
											System.out.println(handleString(serviceNameValue)
													+ "is hybrid service and late destroy");
											output.append(handleString(serviceNameValue)
													+ "is hybrid service and late destroy" + "\n");
										}

									}

								}

							}
						}
					}
				}
				startServiceList.add(i, serviceNameValue);
			}
		}
		startServicelist.addAll(list);

	}

	public void judgeFirstPattern(List<String> hybridService, List<String> startServiceList) {
		hybridService = handleServiceList(hybridService);
		for (int i = 0; i < hybridService.size(); i++) {
			String serviceName = hybridService.get(i);
			if (serviceName != null) {
				// 每一次使用
				for (int j = 0; j < startServiceList.size(); j++) {
					if (startServiceList.get(j).equals(serviceName)) {
						if (!judgeServiceOverrideStart(serviceName)) {
							SootMethod startMethod = startServiceAndDirectMap.get(serviceName);
							SootClass startClass = startMethod.getDeclaringClass();
							SootClass bindclass = bindServiceAndDirectedCaller.get(serviceName);
							if (!startClass.equals(bindclass)) {
								hyList.remove(serviceName);
								startServicelist.remove(serviceName);
								bindServiceList.remove(serviceName);
								HPreCreateCount++;
								System.out.println(handleString(serviceName.toString()) + " premature create");
							}
						}

					}
				}
			}
		}

	}

	public boolean judgeServiceLeak(List<SootMethod> ServicePreviousMethods1,
			List<SootMethod> ServicePreviousMethods2) {

		for (SootMethod sootMethod : ServicePreviousMethods1) {
			SootClass sClass = sootMethod.getDeclaringClass();
			String sclass1 = sClass.toString();
			if (sClass.toString().contains("$"))
				sclass1 = sClass.toString().substring(0, sClass.toString().length() - 2);
			if (judgeActivity(sclass1.toString()) || judgeService(sclass1.toString())) {
				if (sootMethod.toString().contains("onCreate") || sootMethod.toString().contains("onStart")
						|| sootMethod.toString().contains("onResume")
						|| sootMethod.toString().contains("onStartCommand")) {
					System.out.println("prevous2:" + ServicePreviousMethods2);
					for (SootMethod sootMethod2 : ServicePreviousMethods2) {
						SootClass sClass2 = sootMethod2.getDeclaringClass();
						String sclass = sClass2.toString();
						if (sclass.contains("$"))
							sclass = sclass.substring(0, sclass.indexOf("$"));
						System.out.println(sclass);
						if (sClass.toString().equals(sclass)) {// �������bindService�����Ҳ������unbindService���ж�����֮���Ƿ�ɴ�
							List<Unit> PostDominators = new ArrayList<Unit>();
							if (sootMethod2.equals(sootMethod)) {// �����ͬһ������
								for (Unit unit : sootMethod.getActiveBody().getUnits()) {
									Stmt stmt = (Stmt) unit;
									if ((stmt instanceof AssignStmt || stmt instanceof InvokeStmt)
											&& stmt.containsInvokeExpr()) {
										SootMethod method = stmt.getInvokeExpr().getMethod();
										if (ServicePreviousMethods1.contains(method)) {
											new IntraMethodAnalysis(sootMethod);
											PostDominators = IntraMethodAnalysis.getPostDominatorsByUnit(unit);
										}
										if (ServicePreviousMethods2.contains(method)) {
											if (PostDominators.contains(unit)) {
												System.out.println(
														"startService(bindService)��stopServivce(unbindService)��ͬһ�����Ҳ��ᵼ��й¶");
												return true;

											} else {
												System.out.println(
														"startService(bindService)��stopServivce(unbindService)��ͬһ�����������ܻᵼ��й¶");
												return false;
											}
										}

									}
								}

							} else {// ͬһ������ǲ���ͬһ������
								if (sootMethod2.toString().equals("onDestroy")) {
									{
										if (ServicePreviousMethods2.size() == 1) {// stop����unbind����ֱ����onDestroy����
											Chain<Unit> unitChains = sootMethod2.getActiveBody().getUnits();
											Unit FirstUnit = unitChains.getFirst();
											PostDominators = IntraMethodAnalysis.getPostDominatorsByUnit(FirstUnit);
											for (Unit unit : PostDominators) {
												Stmt stmt = (Stmt) unit;
												if ((stmt instanceof AssignStmt || stmt instanceof InvokeStmt)
														&& stmt.containsInvokeExpr()) {
													SootMethod sMethod = stmt.getInvokeExpr().getMethod();
													if (ServicePreviousMethods2.contains(sMethod)) {
														System.out.println("���ᵼ�·���й¶");
														return true;
													} else {
														System.out.println("���ܻᵼ�·���й¶");
														return false;
													}
												}
											}

										} else {// stop����unbind���������onDestroy�е���
											if (judgeAlwaysExistPathBetweenTwoMethods(sootMethod2,
													ServicePreviousMethods2)) {
												System.out.println(
														"��һ��һ���ᵽ��stopService����unbindService��·��,������ڷ���й¶");
												return true;
											}

											else {
												System.out.println(
														"������һ���ᵽ��stopService����unbindService��·��,����ڷ���й¶");
												return false;
											}

										}
									}
								} else {
									System.out.println("���ڷ���й¶");
									return false;
								}

							}

						}
					}

				}
			}
		}
		return false;
	}

	public boolean judgeAlwaysExistPathBetweenTwoMethods(SootMethod sm, List<SootMethod> ServicePreviousMethods) {
		if (sm.isConcrete()) {
			Body body = sm.getActiveBody();
			Chain<Unit> unitChains = body.getUnits();
			Unit FirstUnit = unitChains.getFirst();
			List<Unit> PostDominators = IntraMethodAnalysis.getPostDominatorsByUnit(FirstUnit);
			Iterator<Unit> iterator = PostDominators.iterator();
			while (iterator.hasNext()) {
				Stmt stmt = (Stmt) iterator.next();
				if ((stmt instanceof AssignStmt || stmt instanceof InvokeStmt) && stmt.containsInvokeExpr()) {
					SootMethod sootMethod = stmt.getInvokeExpr().getMethod();
					List<SootMethod> TargetMethods = InterMethodAnalysis.getTargetsMethods(sootMethod);
					if (TargetMethods.retainAll(ServicePreviousMethods)) {
						for (SootMethod targetMethod : TargetMethods) {
							for (Unit unit : targetMethod.retrieveActiveBody().getUnits()) {
								Stmt stmt2 = (Stmt) unit;
								if ((stmt2 instanceof AssignStmt || stmt2 instanceof InvokeStmt)
										&& stmt2.containsInvokeExpr()) {
									if (stmt2.getInvokeExpr().getMethod().getName().equals("stopService")
											|| stmt2.getInvokeExpr().getMethod().getName().equals("unbindServie"))
										return true;
								}
							}
							judgeAlwaysExistPathBetweenTwoMethods(targetMethod, ServicePreviousMethods);
						}

					}
				}
			}
		}
		return false;
	}

	public void judegServiceAfterAppClosed() {
		List<String> Servicelist = handleServiceList(startServicelist);
		for (int i = 0; i < Servicelist.size(); i++) {
			outer: for (SootClass sc : classesChain) {
				if (sc.toString().equals(handleString(Servicelist.get(i).toString()))) {
					for (SootMethod sm : sc.getMethods()) {
						if (sm.getName().equals("onStartCommand") || sm.equals("onStart")) {
							Body body = sm.retrieveActiveBody();
							Iterator<Unit> iterator = body.getUnits().snapshotIterator();
							while (iterator.hasNext()) {
								Unit unit = iterator.next();
								Stmt stmt = (Stmt) unit;
								if (stmt instanceof ReturnStmt) {
									Value value = ((ReturnStmt) stmt).getOp();
									try {
										if (Integer.valueOf(value.toString()) == 1) {
											serviceAfterAppClosed.add(Servicelist.get(i));
											System.out.println(handleString(Servicelist.get(i).toString()));
											break outer;
										}
									} catch (Exception e) {
									}
								}
							}

						}
					}

				}

			}
		}
	}

	public static void setTotal(int PCBs, int LDBs, int PDBs, int SLBs, int total) {
		AndroidAnalysis.PCBs = PCBs;
		AndroidAnalysis.LDBs = LDBs;
		AndroidAnalysis.PDBs = PDBs;
		AndroidAnalysis.SLBs = SLBs;
		AndroidAnalysis.total = total;
	}

	public static int getNumber(List<String> list, String value) {
		int count = 0;
		for (String value2 : list) {
			if (value2.equals(value))
				count++;
		}
		return count;

	}

}
