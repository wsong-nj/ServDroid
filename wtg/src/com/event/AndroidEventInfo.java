package com.event;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import com.event.dataAnalysis.WTGClient;

import soot.Body;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.infoflow.util.SootMethodRepresentationParser;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLiveLocals;
import soot.toolkits.scalar.SmartLocalDefs;

public class AndroidEventInfo {
	
	private static final boolean DEBUG = true;
	public static HashMap<String, Set<EventRegister>> _handlerToRegister = new HashMap<>();
	public static HashMap<String, Set<String>> _handlerLayoutMap = new HashMap<>();
	public static HashMap<String, Set<String>> _runnableToAsync = new HashMap<>();
	
	public static List<String> _handlerInLayout = new ArrayList<>();
	private static Map<String, String> _idLayoutMap = new HashMap<>();
	public static Map<Integer, String> idNameMap = new HashMap<>();
	public static CallGraph callGraph = null;
	
	public AndroidEventInfo(CallGraph callGraph) throws Exception {
		_handlerToRegister.clear();
		_handlerLayoutMap.clear();
		_runnableToAsync.clear();
		_handlerInLayout.clear();
		_idLayoutMap.clear();
		idNameMap.clear();
		AndroidEventInfo.callGraph = callGraph;
		findHandlersFromLayout();
		findHandlersFromCode();
	}
	/**
	 * 通过遍历每个类和方法查找所有的在CallBack中定义的set***Listener语句，找到所有的事件
	 * virtualinvoke $r3.<com.ted.android.core.view.widget.RemoteImageView: void setOnClickListener(android.view.View$OnClickListener)>($r22);
	 * 通过参数类型可知事件处理器为那个类：$22.getType:com.ted.android.view.widget.ImageWatchView$ImageClickListener，
	 * 再在该类中找到事件处理器，将至与事件监听者（set***Listener所在的方法）对应起来
	 * 结果存放在_handlerToRegister中
	 */
	private static void findHandlersFromCode() {
		for(SootClass sootClass : Scene.v().getClasses()) {
			
			if(isClassInSystemPackage(sootClass.getName()))
				continue;
			for(SootMethod sootMethod : sootClass.getMethods()) {
				if(!sootMethod.isConcrete())
					continue;
				
				Body body = sootMethod.retrieveActiveBody();
				if(body == null)
					continue;
				
				//=======================================================================
				//find handler in layout
				if(!_handlerLayoutMap.isEmpty()) {
					if(_handlerLayoutMap.containsKey(sootMethod.getSubSignature())) {
						_handlerInLayout.add(sootMethod.getSignature());
					}
				}
				//=======================================================================
				
				ExceptionalUnitGraph graph = new ExceptionalUnitGraph(body);
				SmartLocalDefs smd = new SmartLocalDefs(graph, new SimpleLiveLocals(graph));
				
				// Iterate over all statement and find callback registration methods
				for (Unit u : body.getUnits()) {
					Stmt stmt = (Stmt) u;
					// Callback registrations are always instance invoke expressions
					if (stmt.containsInvokeExpr()) {
						InvokeExpr iinv = stmt.getInvokeExpr();
						//stmt is register stmt or asyn stmt?
						boolean isAsync = false;
						List<String> callBackList = CallBack.getCallBackByRegister(iinv.getMethodRef().getSubSignature().getString());
						if(callBackList == null) { // stmt is not register stmt
							callBackList = CallBack.getRunnableByAsync(iinv.getMethodRef().getSubSignature().getString());
							if(callBackList == null) // stmt is not asyn stmt
								continue;
							else
								isAsync = !isAsync;
						}
						
						String[] parameters = SootMethodRepresentationParser.v().getParameterTypesFromSubSignature(
								iinv.getMethodRef().getSubSignature().getString());
						for (int i = 0; i < parameters.length; i++) {
							String param = parameters[i];
							
							if (CallBack.callBackListener.contains(param) || CallBack.RUNNABLE.equals(param)) {
								Value arg = iinv.getArg(i);
								
								// We have a formal parameter type that corresponds to one of the Android
								// callback interfaces. Look for definitions of the parameter to estimate
								// the actual type.
								if (arg.getType() instanceof RefType && arg instanceof Local) {
									Set<SootClass> callbackClass = getClassForMethodLocal(sootClass, sootMethod, (Local)arg, u, new ArrayList<SootMethod>());
									if(callbackClass != null) {
										for(SootClass sclass : callbackClass)
											analyzeClassCallbacks(sclass, sootMethod, callBackList, u, isAsync);										
									}else{
										System.out.println("[WARNING-CALLBACK]: " + u + " in " + sootMethod + "can not find handler");
									}
								}
							}
						}
					}
				}
				
			}
		}
	}
	
	private static Set<SootClass> getClassForMethodLocal(SootClass sootClass, SootMethod method, Local local, Unit u, ArrayList<SootMethod> visitedMethodList) {
		
		if(visitedMethodList.contains(method))
			return null;
		else
			visitedMethodList.add(method);
		
		ExceptionalUnitGraph graph = new ExceptionalUnitGraph(method.retrieveActiveBody());
		SmartLocalDefs smd = new SmartLocalDefs(graph, new SimpleLiveLocals(graph));
		
		for (Unit def : smd.getDefsOfAt(local, u)) {
			assert def instanceof DefinitionStmt; 
			DefinitionStmt defs = (DefinitionStmt)def;
			Type tp = defs.getRightOp().getType();
			if (tp instanceof RefType) {
				SootClass callbackClass = ((RefType) tp).getSootClass();
				if (callbackClass.isInterface()) { //setonclicklistener($r2)中$r2为view.onclicklistener接口类型
					if(defs.getRightOp().toString().contains("@parameter")) { //listener $r2为方法的参数
						Set<SootClass> argClass = new HashSet<>();
						int position = getMethodArgPosition(defs);
						if(position != -1) {
							getClassForMethodArg(method, position, callGraph, argClass, visitedMethodList);
							return argClass;
						}
						
					}else if(defs.containsInvokeExpr()) { //lisntener $r2为一个方法的返回值
						InvokeExpr invoke = defs.getInvokeExpr();
						SootClass invokedClass = invoke.getMethod().getDeclaringClass();
						List<SootClass> invokedClassList = new ArrayList<>();
						SootMethod invokedMethod = null;
						if(invokedClass.isInterface()) { //方法为一个接口的方法 ，需要找所有实现该接口的实现类
							//System.out.println(defs.getRightOp());
							List<ValueBox> valueBox = defs.getRightOp().getUseBoxes();
							Value val = valueBox.get(valueBox.size()-1).getValue();
							for(Unit def2 : smd.getDefsOfAt((Local)val, defs)) {
								assert def2 instanceof DefinitionStmt; 
								DefinitionStmt defs2 = (DefinitionStmt)def2;
								if(defs2.getRightOp().toString().contains("@parameter")) {
									Set<SootClass> argClass = new HashSet<>();
									int position = getMethodArgPosition(defs2);
									if(position != -1) {
										getClassForMethodArg(method, position, callGraph, argClass, visitedMethodList);
										if(!argClass.isEmpty()) {
											invokedClassList.addAll(argClass);
										}
									}
								}
							}
							
						}else {
							invokedClassList.add(invokedClass);
						}
						for(SootClass sc : invokedClassList) {
							invokedMethod = sc.getMethod(invoke.getMethod().getSubSignature());
							if(invokedMethod.isConcrete())
								for(Unit unit : invokedMethod.retrieveActiveBody().getUnits()) {
									Stmt st = (Stmt)unit;
									if(st instanceof ReturnStmt) {
										ReturnStmt returnStmt = (ReturnStmt)st;
										if(returnStmt.getOp().toString().equals("null"))
											continue;
										
										return getClassForMethodLocal(sc, invokedMethod, (Local)returnStmt.getOp(), unit, visitedMethodList);
									}
								}
						}
						
					}
					else { //Listener $r2为本类中的一个变量的值
						//$r2 = $r0.<in.shick.diode.threads.ThreadsListActivity: in.shick.diode.threads.ThreadClickDialogOnClickListenerFactory mThreadClickDialogOnClickListenerFactory>
						String rightOp = defs.getRightOp().toString();
						int beginIndex = rightOp.indexOf("<");
						int endIndex = rightOp.indexOf(">");
						if(beginIndex == -1 || endIndex == -1) {
							continue;
						}
						String listener = rightOp.substring(beginIndex, endIndex);
						for (Unit unit : method.retrieveActiveBody().getUnits()) { //优先在当前方法中找
							Stmt stmt = (Stmt) unit;
							if(stmt instanceof AssignStmt) {
								AssignStmt assignStmt = (AssignStmt)stmt;
								if(assignStmt.getLeftOp().toString().contains(listener)
										&& !(assignStmt.getRightOp() instanceof NullConstant)
										&& !assignStmt.getRightOp().equals(defs.getLeftOp())) {
									visitedMethodList.remove(method);
									return getClassForMethodLocal(method.getDeclaringClass(), method, (Local)assignStmt.getRightOp(), u, visitedMethodList);
								}
							}
						}
						return analyzeIntefaceCallbacks(sootClass, listener, visitedMethodList);
					}
				}
				else {
					HashSet<SootClass> callbackSet = new HashSet<>();
					callbackSet.add(callbackClass);
					return callbackSet;
				}
			}
		}
		return null;
	}
	
	private static void getClassForMethodArg(SootMethod method, int position, CallGraph callGraph, Set<SootClass> argClass, ArrayList<SootMethod> methods) {
		if(!methods.contains(method)) {
			methods.add(method);
		}
		for(Iterator<Edge> iterator=callGraph.edgesInto(method); iterator.hasNext();) {
			SootMethod srcMethod = (SootMethod)iterator.next().getSrc();
			if(methods.contains(srcMethod)
					|| srcMethod.getName().equals("main")) {
				continue;
			}
			if(isClassInSystemPackage(srcMethod.getDeclaringClass().getName()))
				continue;
			if(srcMethod.isConcrete()) {
				for(Unit u : srcMethod.retrieveActiveBody().getUnits()) {
					Stmt stmt = (Stmt)u;
					if(stmt.containsInvokeExpr() && stmt.getInvokeExpr().getMethod().equals(method)) {
						InvokeExpr inv = stmt.getInvokeExpr();
						Value arg = inv.getArg(position);
						if((arg instanceof NullConstant) || (arg instanceof IntConstant))
							continue;
						Set<SootClass> sc = getClassForMethodLocal(srcMethod.getDeclaringClass(), srcMethod, (Local)arg, u, methods);
						if(sc != null)
							argClass.addAll(sc);
					}
				}
			}
		}
		methods.remove(method);
	}
	
	private static Set<SootClass> analyzeIntefaceCallbacks(SootClass baseClass, String listener, ArrayList<SootMethod> visitedMethodList) {
		for(SootMethod method : baseClass.getMethods()) {
			if (!method.isConcrete())
				continue;
			
			for (Unit u : method.retrieveActiveBody().getUnits()) {
				Stmt stmt = (Stmt) u;
				if(stmt instanceof AssignStmt) {
					AssignStmt assignStmt = (AssignStmt)stmt;
					if(assignStmt.getLeftOp().toString().contains(listener)
							&& !(assignStmt.getRightOp() instanceof NullConstant)) {
						return getClassForMethodLocal(baseClass, method, (Local)assignStmt.getRightOp(), u, visitedMethodList);
					}
				}
			}
		}
		return null;
	}
	
	
	private static void analyzeClassCallbacks(SootClass sootClass, SootMethod registMethod, List<String> callBackList, Unit u, boolean isAsync) {
		if (sootClass.getName().startsWith("android."))
			return;
		if(sootClass.hasSuperclass()) {
			analyzeClassCallbacks(sootClass.getSuperclass(), registMethod, callBackList, u, isAsync);
		}
		
		for(String callBack : callBackList) {
			if(sootClass.declaresMethod(callBack)) {
				SootMethod callBackMethod = sootClass.getMethodUnsafe(callBack);
				if(callBackMethod != null) {
					checkAndAndMethod(registMethod, callBackMethod, u, isAsync);
				}
			}
		}
	}
	
	private static void checkAndAndMethod(SootMethod regist, SootMethod callBack, Unit u, boolean isAsync) {
		// Skip empty methods
		if (callBack.isConcrete() && isEmpty(callBack.retrieveActiveBody()))
			return;
		if(!isAsync) {
			InvokeExpr inv = ((Stmt)u).getInvokeExpr();
			Value view = inv.getUseBoxes().get(inv.getUseBoxes().size()-1).getValue();
			String id = findViewId(regist, (Local)view, u);
			if(id == null || !(id.matches("^[0-9]*$"))) {
				System.out.println("[WARNING-ID]: " + u + " in " + regist + "can not find id");
			}
			EventRegister handler = new EventRegister(regist, u, id);
			if (_handlerToRegister.containsKey(callBack.getSignature())) {
				_handlerToRegister.get(callBack.getSignature()).add(handler);
			}
			else {
				Set<EventRegister> regists = new HashSet<EventRegister>();
				regists.add(handler);
				_handlerToRegister.put(callBack.getSignature(), regists);
			}
		}else {
			if (_runnableToAsync.containsKey(callBack.getSignature()))
				_runnableToAsync.get(callBack.getSignature()).add(regist.getSignature());
			else {
				Set<String> regists = new HashSet<String>();
				regists.add(regist.getSignature());
				_runnableToAsync.put(callBack.getSignature(), regists);
			}
		}
	}
	
	private static boolean isEmpty(Body activeBody) {
		for (Unit u : activeBody.getUnits())
			if (!(u instanceof IdentityStmt || u instanceof ReturnVoidStmt))
				return false;
		return true;
	}
	
	public static String findViewId(SootMethod method, Local local,  Unit u) {
		ExceptionalUnitGraph graph = new ExceptionalUnitGraph(method.retrieveActiveBody());
		SmartLocalDefs smd = new SmartLocalDefs(graph, new SimpleLiveLocals(graph));
		String viewId = null;
		for (Unit def : smd.getDefsOfAt(local, u)) {
			assert def instanceof DefinitionStmt; 
			DefinitionStmt defs = (DefinitionStmt)def;
			Value rightOp = defs.getRightOp();
			if(defs.containsInvokeExpr()) {
				//组件为方法的返回值，从调用的方法中去寻找id，若方法恰好为findViewById，则可直接获取id
				InvokeExpr invokeExpr = defs.getInvokeExpr();
				SootMethod invokedMethod = invokeExpr.getMethod();
				if("findViewById".equals(invokedMethod.getName()) || "inflate".equals(invokedMethod.getName())
						|| "setId".equals(invokedMethod.getName())) {
					//System.out.println(u + ": " + invokeExpr.getArg(0));
					if(invokeExpr.getArgCount() != 0)
						viewId = invokeExpr.getArg(0).toString();
				}else {
					if(invokedMethod.isConcrete())
						for(Unit unit : invokedMethod.retrieveActiveBody().getUnits()) {
							Stmt st = (Stmt)unit;
							if(st instanceof ReturnStmt) {
								ReturnStmt returnStmt = (ReturnStmt)st;
								if(returnStmt.getOp().toString().equals("null") || (returnStmt.getOp() instanceof IntConstant))
									continue;
								//System.out.println(returnStmt.getOp());
								String id = findViewId(invokedMethod, (Local)returnStmt.getOp(), unit);
								if(id != null)
									return id;
							}
						}
					
				}
			}else if(rightOp.toString().contains("@parameter")){
				//组件为参数，需通过callgraph从调用该方法的方法中寻找
				int position = getMethodArgPosition(defs);
				viewId = getViewIdByMethodArg(method, position, callGraph, new ArrayList<SootMethod>());
			}else {
				if(rightOp.toString().contains("<")) {
					//引用的其他变量值
					//$z0 = $r0.<in.shick.diode.user.ProfileActivity$4$2: boolean val$fUseExternalBrowser>;
					String rightOpStr = rightOp.toString();
					List<ValueBox> valueBox = rightOp.getUseBoxes();
					SootClass sootClass = null;
					if(valueBox.isEmpty()) {
						int index1 = rightOpStr.indexOf("<");
						int index2 = rightOpStr.indexOf(":");
						sootClass = Scene.v().getSootClass(rightOpStr.substring(index1+1, index2));
					}else {
						Type tp = valueBox.get(valueBox.size()-1).getValue().getType();
						if(tp instanceof RefType) {
							sootClass = ((RefType) tp).getSootClass();
						}
					}
					if(sootClass != null) {
						int beginIndex = rightOpStr.indexOf("<");
						int endIndex = rightOpStr.indexOf(">");
						if(beginIndex == -1 || endIndex == -1) {
							continue;
						}
						String variable = rightOp.toString().substring(beginIndex, endIndex + 1);
						//优先在当前方法中寻找该变量的值
						for (Unit unit : method.retrieveActiveBody().getUnits()) {
							Stmt stmt = (Stmt) unit;
							if(stmt instanceof AssignStmt) {
								AssignStmt assignStmt = (AssignStmt)stmt;
								if(assignStmt.getLeftOp().toString().contains(variable)) {
									if((assignStmt.getRightOp() instanceof NullConstant) || assignStmt.getRightOp() instanceof IntConstant)
										continue;
									String id = findViewId(method, (Local)assignStmt.getRightOp(), unit);
									if(id != null)
										return id;
								}
							}
						}
						
						for(SootMethod sm : sootClass.getMethods()) {
							if (!sm.isConcrete())
								continue;
							
							for (Unit unit : sm.retrieveActiveBody().getUnits()) {
								Stmt stmt = (Stmt) unit;
								if(stmt instanceof AssignStmt) {
									AssignStmt assignStmt = (AssignStmt)stmt;
									if(assignStmt.getLeftOp().toString().contains(variable)) {
										if((assignStmt.getRightOp() instanceof NullConstant) || assignStmt.getRightOp() instanceof IntConstant)
											continue;
										String id = findViewId(sm, (Local)assignStmt.getRightOp(), unit);
										if(id != null)
											return id;
									}
								}
							}
						}
					}
				}else {
					//$r10 = (android.widget.ListView) $r7;
					//System.out.println(rightOp);
					if(!rightOp.getUseBoxes().isEmpty()) {
						Value value = rightOp.getUseBoxes().get(0).getValue();
						if(!(value instanceof NullConstant) && !(value instanceof IntConstant))
							viewId = findViewId(method, (Local)value, def);
					}
				}
				
				if(viewId != null)
					return viewId;
			}
			
		}
		return viewId;
	}
	
	private static String getViewIdByMethodArg(SootMethod method, int position, CallGraph callGraph, List<SootMethod> methods) {
		if(!methods.contains(method)) {
			methods.add(method);
		}
		for(Iterator<Edge> iterator=callGraph.edgesInto(method); iterator.hasNext();) {
			SootMethod srcMethod = (SootMethod)iterator.next().getSrc();
			if(methods.contains(srcMethod)
					|| srcMethod.getName().equals("main")) {
				continue;
			}
			if(isClassInSystemPackage(srcMethod.getDeclaringClass().getName()))
				continue;
			if(srcMethod.isConcrete()) {
				for(Unit u : srcMethod.retrieveActiveBody().getUnits()) {
					Stmt stmt = (Stmt)u;
					if(stmt.containsInvokeExpr() && stmt.getInvokeExpr().getMethod().equals(method)) {
						InvokeExpr inv = stmt.getInvokeExpr();
						Value arg = inv.getArg(position);
						if((arg instanceof NullConstant) || (arg instanceof IntConstant))
							continue;
						String id = findViewId(srcMethod, (Local)arg, u);
						if(id != null)
							return id;
					}
				}
			}
		}
		methods.remove(method);
		return null;
	}
	
	public static String getMenuItemIdByStmt(SootMethod onOptionsItemSelectedMethod, Unit u) {
		BriefUnitGraph graph = new BriefUnitGraph(onOptionsItemSelectedMethod.retrieveActiveBody());
		List<Unit> stList = backwardDFS(graph, u);
		if(stList == null) {
			System.out.println("can not find case stmt in " + onOptionsItemSelectedMethod + " for " + u);
			return null;
		}
		Unit caseStmt = stList.get(1);
		Unit predStmt = stList.get(0);		
		LookupSwitchStmt switchStmt = (LookupSwitchStmt)caseStmt;
		List<Unit> targetList = switchStmt.getTargets();
		int index = targetList.indexOf(predStmt);
		if(index != -1) {
			String id = switchStmt.getLookupValues().get(index).toString();
			//System.out.println(onOptionsItemSelectedMethod + ":" + id);
			return id;
		}
		return null;
	}
	
	private static List<Unit> backwardDFS(UnitGraph graph, Unit targetStmt) {
		for(Unit u : graph.getPredsOf(targetStmt)) {
			if(u.toString().contains("case")) {
				List<Unit> list = new ArrayList<>();
				list.add(targetStmt);
				list.add(u);
				return list;
			}
			return backwardDFS(graph, u);
		}
		return null;
	}
	
	/**
	 * 用apktool反编译得到布局文件，反编译后的文件存放在新建文件夹apkName_apktool中
	 */
	public static void decompileApk() {
		String dir = "./preprocess\\" + "preprocessApk.bat";
		String[] command = {dir, WTGClient.apkDir, WTGClient.apkName};
		try {
			File decompileFile = new File(WTGClient.apkDir + WTGClient.apkName + "_apktool");
			if(!decompileFile.exists()) {
				Runtime.getRuntime().exec(command);
				System.out.println("decompile Apk successed!");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("decompile Apk failed!");
		}
	}
	/**
	 * 先使用apktool反编译apk得到布局文件，在读取布局文件中的id文件，记录下layout和id的ID，以备后用
	 * 最后解析布局文件，寻找onclick属性的组件，记录下方法名和组件id
	 * @throws Exception
	 */
	public static void findHandlersFromLayout() throws Exception {
		decompileApk();
		getLayoutIDs();
		getLayoutHandlers();
	}
	/**
	 * 解析布局文件中public.xml文件，得到类型为layout和id的ID
	 */
	public static void getLayoutIDs() {
        try {
            File resourceFile = new File(WTGClient.apkDir + WTGClient.apkName + "_apktool/res/values/public.xml");
            if (!resourceFile.exists()) {
                return ;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document resourceXML = dBuilder.parse(resourceFile);
            resourceXML.getDocumentElement().normalize();

            //NodeList publicNodes = resourceXML.getElementsByTagName("public");
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("/resources//public[@type=\"layout\"]");
            NodeList layoutNodes = (NodeList)expr.evaluate(resourceXML, XPathConstants.NODESET);
            XPathExpression expr_id = xpath.compile("/resources//public[@type=\"id\"]");
            NodeList idNodes = (NodeList)expr_id.evaluate(resourceXML, XPathConstants.NODESET);

            if (layoutNodes != null) {
                for (int i = 0; i < layoutNodes.getLength(); i++) {
                    Node layoutNode = layoutNodes.item(i);
                    if (layoutNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    Element layoutElement = (Element)layoutNode;
                    Integer layoutID = Integer.decode(layoutElement.getAttribute("id"));
                    String layoutName = layoutElement.getAttribute("name");

                    if (DEBUG) {
                        System.out.println("Layout: " + layoutName + "; id: " + layoutID);
                    }

                    _idLayoutMap.put(layoutName, layoutID+"");
                }
            }
            
            if(idNodes != null) {
            	for (int i = 0; i < idNodes.getLength(); i++) {
                    Node idNode = idNodes.item(i);
                    if (idNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    Element idElement = (Element)idNode;
                    Integer idID = Integer.decode(idElement.getAttribute("id"));
                    String idName = idElement.getAttribute("name");

                    if (DEBUG) {
                        System.out.println("ID: " + idName + "; id: " + idID);
                    }

                    idNameMap.put(idID, idName);
                }
            }

        } catch (Exception e) {
            System.err.println("Exception: " + e.toString());
            e.printStackTrace();
        }

    }

	/**
	 * 解析所有的布局文件，找到在布局文件中绑定的事件监听
	 * @throws Exception
	 */
    public static void getLayoutHandlers() throws Exception {
        File resourceDir = new File(WTGClient.apkDir + WTGClient.apkName + "_apktool/res/");
        if (!resourceDir.exists() || !resourceDir.isDirectory()) {
            return;
        }

        File[] layoutDirs = resourceDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.startsWith("layout")) {
                    return true;
                }

                return false;
            }
        });

        for (File layoutDir : layoutDirs) {
            File[] xmlFiles = layoutDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    if (name.endsWith(".xml")) {
                        return true;
                    }

                    return false;
                }
            });

            for (File layoutXmlFile : xmlFiles) {
                processLayoutXml(layoutXmlFile);
            }
        }
    }

    /**
     * 解析布局文件，寻找有属性onclick的组件，记录下对应的方法名和组件id
     * _handlerLayoutMap<String, Set<String>>- onclicmethod subsig -> set(id)
     * @param xmlFile
     * @throws Exception
     */
    private static void processLayoutXml(File xmlFile) throws Exception {
        String layoutName = FilenameUtils.removeExtension(xmlFile.getName());

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        try {
            Document resourceXML = dBuilder.parse(xmlFile);
        
            resourceXML.getDocumentElement().normalize();

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("//@*[local-name()='id']/..");
            NodeList uiElementNodes = (NodeList)expr.evaluate(resourceXML, XPathConstants.NODESET);

            for (int i = 0; i < uiElementNodes.getLength(); i++) {
                Node uiElementNode = uiElementNodes.item(i);
                if (uiElementNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                Element uiElement = (Element)uiElementNode;

                String elementName = uiElement.getTagName();

                if (uiElement.hasAttribute("android:onClick")) {
                    String onClickMethodName = uiElement.getAttribute("android:onClick");
                    //onClickMethod subsig
                    String onClickMethodSubsig = "void " + onClickMethodName + "(android.view.View)";
                    //view id
                    String id = uiElement.getAttribute("android:id");
                    //System.out.println(id);
                    //System.out.println("Layout: " + layoutName);
                    //System.out.println("    onclick: " + onClickMethodName);

                    if (!_handlerLayoutMap .containsKey(onClickMethodSubsig)) {
                        _handlerLayoutMap.put(onClickMethodSubsig, new HashSet<String>());
                    }
                    
                    _handlerLayoutMap.get(onClickMethodSubsig).add(id);
                    
                    if(DEBUG) {
                    	System.out.println("find handler " + id + " " + onClickMethodSubsig + " in layout " + xmlFile.getName());
                    }

                }
            }
        } catch (SAXParseException e) {
            // ignore invalid characters in the XML file
        } 
    }
    
    
	public AndroidEventInfo printEventHandlersAndAsyns() {
		if(DEBUG) {
			try {
				FileWriter file = new FileWriter(WTGClient.saveDir + WTGClient.apkName + "_EventHandlersAndAsyns.txt");
				file.write("-------EventHandlers----------\n");
				for(String handler : _handlerToRegister.keySet()) {
					file.write(handler + " -> " + _handlerToRegister.get(handler) + "\n");
				}
				file.write("-------Asyns----------\n");
				for(String async : _runnableToAsync.keySet()) {
					file.write(async + " -> " + _runnableToAsync.get(async) + "\n");
				}
				file.close();
				System.out.println("[AndroidEventInfo]: save eventHandlersAndAsyns to " + WTGClient.saveDir + WTGClient.apkName + "_EventHandlersAndAsyns.txt");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return this;
	}
	
	public void printEventHandlersInLayout() {
		if(DEBUG) {
			for(String handler : _handlerInLayout) {
				System.out.println("handler in layout: " + handler);
			}
		}
	}
	/**
	 * 根据给定的stmt（包含@parameter），得到对应的方法参数位置
	 * $r2 := @parameter2: java.lang.String; -> 参数位置为2
	 * @return
	 */
	private static int getMethodArgPosition(Stmt stmt) {
		int position = -1;
		if(stmt != null && stmt instanceof DefinitionStmt && stmt.toString().contains("@parameter")) {
			String rightOp = ((DefinitionStmt) stmt).getRightOp().toString();
			int beginIndex = rightOp.indexOf("@parameter") + 10;
			int endIndex = rightOp.indexOf(":");
			if(beginIndex != -1 && endIndex != -1 && beginIndex < endIndex) {
				position = Integer.parseInt(rightOp.substring(beginIndex, endIndex));
			}
		}
		return position;
	}
	
	public static boolean isActivity(SootClass sootClass) {
		SootClass subClass = sootClass;
        while(subClass.hasSuperclass()) {
        	subClass = subClass.getSuperclass();
        	if(subClass.getName().equals("android.app.Activity")) {
        		return true;
        	}
        	
        }
        return false;
	}

	public static boolean isClassInSystemPackage(String className) {
		return 
				className.startsWith("android.")
				|| className.startsWith("java.")
				|| className.startsWith("javax.")
				|| className.startsWith("sun.")
				|| className.startsWith("org.codehaus.jackson.")
				|| className.startsWith("org.jsoup.")
				//|| className.startsWith("io.branch.")
				//|| className.startsWith("rx.")
				|| className.startsWith("io.fabric.")
				|| className.startsWith("com.facebook.")
				|| className.startsWith("com.twitter.sdk.")
				|| className.startsWith("com.comscore.")
				|| className.startsWith("com.google.android.gms.")
				|| className.startsWith("org.apache.");
	}
	
	public static boolean isEvent(SootMethod method) {
		String subSigOfMethod = method.getSubSignature();
		for(String register : CallBack.getViewRegistars()) {
			List<String> callBack = CallBack.viewToCallBacks.get(register);
			if(callBack.contains(subSigOfMethod)) {
				return true;
			}
		}
		
		for(String register : CallBack.getDialogRegistars()) {
			List<String> callBack = CallBack.dialogToCallBacks.get(register);
			if(callBack.contains(subSigOfMethod)) {
				return true;
			}
		}
		return false;
	}
}
