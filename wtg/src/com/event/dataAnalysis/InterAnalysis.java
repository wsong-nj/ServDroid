package com.event.dataAnalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.event.AndroidEventInfo;
import com.event.EventHandler;
import com.event.EventRegister;
import com.event.GetActivityWithFragment;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class InterAnalysis {
	
	public static String itemId = null;
	
	public static void dfs(SootMethod sm, Unit callUnit, CallGraph callGraph, List<SootMethod> methods, List<EventHandler> eventList, Map<String, Set<List<EventHandler>>> activities) {
		if(!methods.contains(sm)) {
			methods.add(sm);
		}
		if(!callGraph.edgesInto(sm).hasNext()) { //callgraph中没有调用sm方法的边
			if(isActivity(sm.getDeclaringClass()) || isFragment(sm.getDeclaringClass())) { //判断sm方法是否为activity、fragment、service，若是直接记录下
				Set<String> acts = null;
				if(isFragment(sm.getDeclaringClass())) {
					acts = GetActivityWithFragment.fragmentToActsMap.get(sm.getDeclaringClass().getName());
				}
				if(acts == null) {
					acts = new HashSet<>();
					acts.add(sm.getDeclaringClass().getName());
				}
				for(String act : acts) {
					Set<List<EventHandler>> eventSeq = activities.get(act); 
					List<EventHandler> events = new ArrayList<>();
					if(eventList.isEmpty()) { //跳转事件序列为空
						String viewId = null;
						if(sm.getName().equals("onOptionsItemSelected")
								|| sm.getName().equals("onContextItemSelected")) { //菜单选项
							viewId = AndroidEventInfo.getMenuItemIdByStmt(sm, callUnit);
							if((viewId == null || viewId.equals("default")) && itemId != null) {
								//特殊情况
								viewId = itemId;
								itemId = null;
							}
						}
						EventRegister register = new EventRegister(sm, callUnit, viewId);
						EventHandler handler = new EventHandler(sm, register);
						events.add(handler);
					}else {
						events.addAll(eventList);
					}
					Collections.reverse(events);
					if(eventSeq == null) {
						Set<List<EventHandler>> eventSet = new HashSet<>();
						eventSet.add(events);
						activities.put(act, eventSet);
					}else {
						eventSeq.add(events);
					}
				}
				
			}else { 
				Set<EventRegister> registers = AndroidEventInfo._handlerToRegister.get(sm.getSignature());
				if(registers != null) { //判断sm方法是否为事件，若是，继续处理，若不是，则callgraph缺少该方法的边，不进行处理
					
					for(Iterator<EventRegister> iterator = registers.iterator(); iterator.hasNext();) {
						EventRegister register = iterator.next();
						EventHandler eventHandler = new EventHandler(sm, register);
						eventList.add(eventHandler);
						SootMethod reg = register.getEventRegisterMethod();
						dfs(reg, register.getRegisterStmt(), callGraph, methods, eventList, activities);
						eventList.remove(eventHandler);
					}
				}else {
					Set<MethodWithUnit> services = IntentAnalysis.serviceMap.get(sm.getDeclaringClass().getName());//判断是否是service
					if(services != null) {
						for(MethodWithUnit mUnit : services) {
							EventRegister reg = new EventRegister(mUnit.getMethod(), mUnit.getU());
							EventHandler eventHandler = new EventHandler(sm, reg);
							eventList.add(eventHandler);
							dfs(mUnit.getMethod(), mUnit.getU(), callGraph, methods, eventList, activities);
							eventList.remove(eventHandler);
						}
					}else {
						//判断是否是异步回调
						Set<String> asyns = AndroidEventInfo._runnableToAsync.get(sm.getSignature());
						if(asyns != null) {
							for(Iterator<String> iterator = asyns.iterator(); iterator.hasNext();) {
								String asyn = iterator.next();
								dfs(Scene.v().getMethod(asyn), null, callGraph, methods, eventList, activities);
							}
						}else if("onPostExecute".equals(sm.getName())) {
							List<SootMethod> methodList = sm.getDeclaringClass().getMethods();
							SootMethod doInBackgroundMethod = null;
							for(SootMethod sootMethod : methodList) {
								if(sootMethod.getName().equals("doInBackground") && !sootMethod.getReturnType().toString().equals("java.lang.Object")) {
									doInBackgroundMethod = sootMethod;
									break;
								}
							}
							
							if(doInBackgroundMethod != null)
								dfs(doInBackgroundMethod, null, callGraph, methods, eventList, activities);
						}
						
					}
				}
			}
		}
		Iterator<Edge> callEdges = callGraph.edgesInto(sm);
		ArrayList<Edge> callList = new ArrayList<>();
		while(callEdges.hasNext()) {
			callList.add(callEdges.next());
		}
		if(callList.size() < 50) {
			//System.out.println(callList.size() + "->" + sm);
			for(Edge edge : callList) {
				SootMethod callSrc = (SootMethod) edge.getSrc();
				
				
				//System.out.println("callSrc" + callSrc + "->" + sm);
				
				if(AndroidEventInfo.isClassInSystemPackage(callSrc.getDeclaringClass().getName())) {
					continue;
				}
				//特殊处理：当sm为onOptionsItemSelected且callSrc也为该方法时，先在sm方法里寻找menu item id，若找到了，则为组件id，此时将其记录下来；
				//若找不到， 则去callSrc中找
				if((sm.getName().equals("onOptionsItemSelected") && callSrc.getName().equals("onOptionsItemSelected"))
						|| sm.getName().equals("onContextItemSelected") && callSrc.getName().equals("onContextItemSelected")) {
					itemId = AndroidEventInfo.getMenuItemIdByStmt(sm, callUnit);
				}
				
				if(!methods.contains(callSrc)
						&& !callSrc.getName().equals("main")) {
					dfs(callSrc, edge.srcStmt(), callGraph, methods, eventList, activities);
				}else if(!methods.contains(callSrc) && !sm.getName().equals("<init>")) { //callsrc is in dummyClass
					Set<EventRegister> registers = AndroidEventInfo._handlerToRegister.get(sm.getSignature());
					if(registers != null) { //判断是否是事件
						for(Iterator<EventRegister> iterator = registers.iterator(); iterator.hasNext();) {
							EventRegister register = iterator.next();
							EventHandler eventHandler = new EventHandler(sm, register);
							eventList.add(eventHandler);
							SootMethod reg = register.getEventRegisterMethod();
							dfs(reg, register.getRegisterStmt(), callGraph, methods, eventList, activities);
							eventList.remove(eventHandler);
						}
					}else {
						Set<MethodWithUnit> services = IntentAnalysis.serviceMap.get(sm.getDeclaringClass().getName());//判断是否是service
						if(services != null) { //判断是否是service
							for(MethodWithUnit mUnit : services) {
								EventRegister reg = new EventRegister(mUnit.getMethod(), mUnit.getU());
								EventHandler eventHandler = new EventHandler(sm, reg);
								eventList.add(eventHandler);
								dfs(mUnit.getMethod(), mUnit.getU(), callGraph, methods, eventList, activities);
								eventList.remove(eventHandler);
							}
						}else {
							//判断是否是异步回调
							Set<String> asyns = AndroidEventInfo._runnableToAsync.get(sm.getSignature());
							if(asyns != null) {
								for(Iterator<String> iterator = asyns.iterator(); iterator.hasNext();) {
									String asyn = iterator.next();
									dfs(Scene.v().getMethod(asyn), null, callGraph, methods, eventList, activities);
								}
							}else {
								//针对callgraph不全导致回调没找到的情况，需要判断找到的是否是activity或fragment
								if(isActivity(sm.getDeclaringClass()) || isFragment(sm.getDeclaringClass())) { 
									Set<String> acts = null;
									if(isFragment(sm.getDeclaringClass())) {
										acts = GetActivityWithFragment.fragmentToActsMap.get(sm.getDeclaringClass().getName());
									}
									if(acts == null) {
										acts = new HashSet<>();
										acts.add(sm.getDeclaringClass().getName());
									}
									for(String act : acts) {
										Set<List<EventHandler>> eventSeq = activities.get(act);
										List<EventHandler> events = new ArrayList<>();
										if(eventList.isEmpty()) {
											String viewId = null;
											if(sm.getName().equals("onOptionsItemSelected")) { //菜单选项
												viewId = AndroidEventInfo.getMenuItemIdByStmt(sm, callUnit);
												if((viewId == null || viewId.equals("default")) && itemId != null) {
													//特殊情况
													viewId = itemId;
													itemId = null;
												}
											}
											EventRegister register = new EventRegister(sm, callUnit, viewId);
											EventHandler handler = new EventHandler(sm, register);
											events.add(handler);
										}else {
											events.addAll(eventList);
										}
										Collections.reverse(events);
										if(eventSeq == null) {
											Set<List<EventHandler>> eventSet = new HashSet<>();
											eventSet.add(events);
											activities.put(act, eventSet);
										}else {
											eventSeq.add(events);
										}
									}
									
								}
							}
							
						}
					}
				}
			}
			
		}
		methods.remove(sm);
	}
	private static boolean isActivity(SootClass sootClass) {
		SootClass subClass = sootClass;
        while(subClass.hasSuperclass()) {
        	subClass = subClass.getSuperclass();
        	String subClassName = subClass.getName();
        	if(subClassName.equals("android.app.Activity")) {
        		return true;
        	}
        	
        }
        return false;
	}
	private static boolean isFragment(SootClass sootClass) {
		SootClass subClass = sootClass;
        while(subClass.hasSuperclass()) {
        	subClass = subClass.getSuperclass();
        	String subClassName = subClass.getName();
        	if(subClassName.equals("android.support.v4.app.Fragment")) {
        		return true;
        	}
        	
        }
        return false;
	}
}
