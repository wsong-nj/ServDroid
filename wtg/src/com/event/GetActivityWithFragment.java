package com.event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.event.Util.basicAnalysis;
import com.event.dataAnalysis.WTGClient;

import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.Stmt;

public class GetActivityWithFragment extends basicAnalysis {
	public static Map<String, Set<String>> fragmentToActsMap = new HashMap<>();
	public GetActivityWithFragment() {
		fragmentToActsMap.clear();
		Map<String, Set<String>> actToFragmentMap = getFragment();
		for(String act : actToFragmentMap.keySet()) {
			for(String frag : actToFragmentMap.get(act)) {
				addFragment(frag, act);
			}
		}
		if(WTGClient.apkName.equals("ted")) {
			addFragment("com.ted.android.view.fragment.ArchivesFragment", "com.ted.android.view.activity.MainActivity");
			addFragment("com.ted.android.view.fragment.FeaturedFragment", "com.ted.android.view.activity.MainActivity");
			addFragment("com.ted.android.view.fragment.TalkDetailFragment", "com.ted.android.view.activity.TalkDetailActivity");
			addFragment("com.ted.android.view.fragment.MyTalksFragment", "com.ted.android.view.activity.MainActivity");
			addFragment("com.ted.android.view.fragment.InspireMeFragment", "com.ted.android.view.activity.MainActivity");
			
		}
	}
	
	public static void addFragment(String fragment, String act) {
		Set<String> acts = fragmentToActsMap.get(fragment);
		if(acts == null) {
			Set<String> actSet = new HashSet<>();
			actSet.add(act);
			fragmentToActsMap.put(fragment, actSet);
		}else {
			acts.add(act);
		}
	}

	public static Map<String, Set<String>> getFragment() { // activity,set of fragment
		Map<String, Set<String>> mapInfo = new HashMap<>();
		for (SootClass sc : resolveAllClasses(Scene.v().getClasses())) {
			List<SootMethod> scMethods = sc.getMethods();
			for (int i=0;i<scMethods.size();i++) {
				SootMethod sm = scMethods.get(i);
				Set<String> set = new HashSet<>();
				if (sm.isConcrete()) {
					Body body = sm.retrieveActiveBody();
					for (Unit u : body.getUnits()) {
						Stmt stmt = (Stmt)u;
						if (stmt.containsInvokeExpr()) {
							SootMethod ts = stmt.getInvokeExpr().getMethod();
							// System.out.println(ts.getSignature());
							String tmp = ts.getSignature();
							if (tmp.equals(
									"<android.support.v4.app.FragmentTransaction: android.support.v4.app.FragmentTransaction replace(int,android.support.v4.app.Fragment)>")
									|| tmp.equals(
											"<android.support.v4.app.FragmentTransaction: android.support.v4.app.FragmentTransaction replace(int,android.support.v4.app.Fragment,java.lang.String)>")
									|| tmp.equals(
											"<android.support.v4.app.FragmentTransaction: android.support.v4.app.FragmentTransaction add(int,android.support.v4.app.Fragment,java.lang.String)>")
									|| tmp.equals(
											"<android.support.v4.app.FragmentTransaction: android.support.v4.app.FragmentTransaction add(int,android.support.v4.app.Fragment)>")) {
								//System.out.println("method: "+sm);
								//System.out.println("stmt: "+stmt);
								List<Value> args = stmt.getInvokeExpr().getArgs();
								if (judgeParam(body, args.get(1)) != -1) {
									int position = judgeParam(body, args.get(1));
									mapInfo = mapMerge(mapInfo,dealParam(sm,position));
									//System.out.println(dealParam(sm,position));
									break;
								}
								if (args.get(1).getType().toString() != "android.support.v4.app.Fragment")
									set.add(args.get(1).getType().toString());
								else {
									for (Local l : body.getLocals())
										if (judgeFragment(l.getType().toString()))
											set.add(l.getType().toString());
								}
								if (set.contains("android.support.v4.app.Fragment") && set.size() > 1)
									set.remove("android.support.v4.app.Fragment");
							}
							if (tmp.equals(
									"<android.support.v4.app.FragmentTransaction: android.support.v4.app.FragmentTransaction add(android.support.v4.app.Fragment,java.lang.String)>")) {
								//System.out.println("method: "+sm);
								//System.out.println("stmt: "+u);
								List<Value> args = stmt.getInvokeExpr().getArgs();
								if (judgeParam(body, args.get(0)) != -1) {
									//mapInfo.putAll(dealParam(sm));
									int position = judgeParam(body, args.get(0));
									mapInfo = mapMerge(mapInfo,dealParam(sm,position));
									break;
								}
								if (args.get(0).getType().toString() != "android.support.v4.app.Fragment")
									set.add(args.get(0).getType().toString());
								else {
									for (Local l : body.getLocals())
										if (judgeFragment(l.getType().toString()))
											set.add(l.getType().toString());
								}
								if (set.contains("android.support.v4.app.Fragment") && set.size() > 1)
									set.remove("android.support.v4.app.Fragment");
							}
						}
					}
				}
				if (!set.isEmpty()&&judgeActivity(sc.toString())) {
					if(!mapInfo.containsKey(sc.toString()))
						mapInfo.put(sc.toString(), set);
					else {
						set.addAll(mapInfo.get(sc.toString()));
						mapInfo.put(sc.toString(), set);
					}
				}					
			}				
			if (judgePagerAdpter(sc.toString())) {
				Set<String> set1 = new HashSet<>();
				for (SootMethod sm : sc.getMethods()) {
					if (sm.isConcrete() && sm.getName().equals("<init>")) {
						Body body = sm.retrieveActiveBody();
						List<Type> tps = sm.getParameterTypes();
						if (tps.size() == 2) {
							if (judgeActivity(tps.get(0).toString())
									&& tps.get(1).toString().equals("android.support.v4.app.FragmentManager")) {
								for (Local v : body.getLocals()) {
									if (judgeFragment(v.getType().toString())
											&& !v.getType().toString().startsWith("android.support"))
										set1.add(v.getType().toString());
								}
							}
							if (!set1.isEmpty()){
								if(!mapInfo.containsKey(tps.get(0).toString()))
									mapInfo.put(tps.get(0).toString(), set1);
								else {
									set1.addAll(mapInfo.get(sc.toString()));
									mapInfo.put(tps.get(0).toString(), set1);
								}
							}
						}
					}
				}
			}
		}
		return mapInfo;
	}
	
	private static Map<String, Set<String>> dealParam(SootMethod sm,int position){
		Map<String, Set<String>> mapInfo = new HashMap<String,Set<String>>();		
		List<SootMethod> methodlist = getSourceMethods(sm);	
		HashSet<SootMethod> h = new HashSet<>(methodlist);
		methodlist.clear();
		methodlist.addAll(h);
		for(SootMethod tmp:methodlist) {
			Set<String> set = new HashSet<>();
			if(tmp.isConcrete()) {
				Body body = tmp.getActiveBody();				
				for(Unit u:body.getUnits()) {
					Stmt stmt = (Stmt)u;
					if (stmt.containsInvokeExpr()) {
						SootMethod ts = stmt.getInvokeExpr().getMethod();
						if(ts.equals(sm)) {							
							List<Value> args = stmt.getInvokeExpr().getArgs();
							//System.out.println(args.get(position).getType());
							if(judgeFragment(args.get(position).getType().toString()))
								set.add(args.get(position).getType().toString());
						}
					}
				}
			}
			if (judgeActivity(tmp.getDeclaringClass().toString())) {
				if(!mapInfo.containsKey(tmp.getDeclaringClass().toString()))
					mapInfo.put(tmp.getDeclaringClass().toString(), set);
				else {
					set.addAll(mapInfo.get(tmp.getDeclaringClass().toString()));
					mapInfo.put(tmp.getDeclaringClass().toString(), set);
				}
				//System.out.println(mapInfo);
			}
		}
		return mapInfo;
	}
	
	private static Map<String, Set<String>> mapMerge(Map<String, Set<String>> hm1,Map<String, Set<String>> hm2) {
		Map<String, Set<String>> result = new HashMap<>();
		result.putAll(hm1);
		for(String k:hm2.keySet()) {
			if(result.containsKey(k)) {
				Set<String> t = result.remove(k);
				t.addAll(hm2.get(k));
				result.put(k, t);
			}else {
				result.put(k, hm2.get(k));
			}
		}
		return result;
	}

	/*private String getTypeOfLocal(Value local, Body body) {
		String type = null;
		for (Unit unit : body.getUnits()) {
			Stmt stmt = (Stmt) unit;
			Value leftValue = GetLeftOP(unit);
			if (leftValue != null && leftValue.equals(local)) {
				if (stmt instanceof AssignStmt && (!stmt.containsInvokeExpr())) {
					try {
						type = unit.toString().substring(unit.toString().indexOf("(") + 1,
								unit.toString().lastIndexOf(")"));
					} catch (Exception e) {
					}
				}
			}
		}
		return type;
	}*/
	
	public void printActivityWithFragments() {
		System.out.println("------Fragments-------");
		for(String act : fragmentToActsMap.keySet()) {
			System.out.println(act + "->" + fragmentToActsMap.get(act));
		}
	}
}
