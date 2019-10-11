package tool.Analy.MethodAnalysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.xmlpull.v1.XmlPullParserException;

import android.R.integer;

import heros.InterproceduralCFG;
import soot.Body;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.Sources;
import soot.jimple.toolkits.callgraph.Targets;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.options.Options;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.MHGDominatorsFinder;
import soot.toolkits.graph.MHGPostDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;
import tool.Analy.tags.MethodTag;
import tool.Analy.util.LocalAnalysis;

public class InterMethodAnalysis {

	//public final static String jarPath = "E:\\360安全浏览器下载\\android-sdk-windows\\platforms";
	static CallGraph callGraph;
	//SootMethod m;
	static List<SootMethod> allMethods = new ArrayList<SootMethod>();
	//public static List<SootClass> classesChain;
	public InfoflowCFG info;

	public InterMethodAnalysis(InfoflowCFG info,CallGraph cg) {
		this.info = info;
		callGraph = cg;
	}
	
	/**
	 * get all post-dominators of an unit in the InfoflowCFG
	 * @param sm  SootMethod of the unit
	 * @param u  unit
	 * @return
	 */
	public List<Unit> getAllPostDominatorsOfUnit(SootMethod sm,Unit u){
		List<Unit> pds = new ArrayList<Unit>();
		Body b = sm.retrieveActiveBody();
		UnitGraph unitGraph = new BriefUnitGraph(b);  //don't consider exception edges
		MHGPostDominatorsFinder<Unit> pdFinder = new MHGPostDominatorsFinder<Unit>(unitGraph);
		List<Unit> tmp = pdFinder.getDominators(u);
		pds.addAll(tmp);
		for(int i=0;i<tmp.size();i++) {
			Unit unit = tmp.get(i);
			if(!u.equals(unit)) {
				Stmt stmt = (Stmt)unit;
				if((stmt instanceof InvokeStmt || stmt instanceof AssignStmt) && stmt.containsInvokeExpr()) {					
					SootMethod s = stmt.getInvokeExpr().getMethod();
					Set<String> ignoredMethods = new HashSet<String>();
					ignoredMethods.add(s.getSignature());
					List<Unit> appendpds = getMBEUnit(s,ignoredMethods);
					if(appendpds!=null)
						pds.addAll(appendpds);
				}
			}
		}
		
		return pds;
	}
	
	/**
	 * get all dominators of an unit in the InfoflowCFG
	 * @param sm  SootMethod of the unit
	 * @param u  unit
	 * @return
	 */
	public List<Unit> getAllDominatorsOfUnit(SootMethod sm,Unit u){
		List<Unit> doms = new ArrayList<Unit>();
		Body b = sm.retrieveActiveBody();
		UnitGraph unitGraph = new BriefUnitGraph(b);  //don't consider exception edges
		MHGDominatorsFinder<Unit> domFinder = new MHGDominatorsFinder<Unit>(unitGraph);
		List<Unit> tmp = domFinder.getDominators(u);
		doms.addAll(tmp);
		for(int i=0;i<tmp.size();i++) {
			Unit unit = tmp.get(i);
			if(!u.equals(unit)) {
				Stmt stmt = (Stmt)unit;
				if((stmt instanceof InvokeStmt || stmt instanceof AssignStmt) && stmt.containsInvokeExpr()) {					
					SootMethod s = stmt.getInvokeExpr().getMethod();
					Set<String> ignoredMethods = new HashSet<String>();
					ignoredMethods.add(s.getSignature());
					List<Unit> appendpds = getMBEUnit(s,ignoredMethods);
					if(appendpds!=null)
						doms.addAll(appendpds);
				}
			}
		}
		
		return doms;
	}
	
	/**
	 * Recursively get units that must be executed through getting post-dominators of the first unit in the sm
	 * @param sm SootMethod 
	 * @param ignoredMethods the signatures of methods that don't need to search.
	 * @return
	 */
	public List<Unit> getMBEUnit(SootMethod sm,Set<String> ignoredMethods){
		if(sm == null || !sm.isConcrete())
			return null;
		if(sm.getDeclaringClass().getName().startsWith("java")   //usually don't need units in these classes
				|| sm.getDeclaringClass().getName().startsWith("android"))
			return null;

		List<Unit> pds = new ArrayList<Unit>();
		List<Unit> tmp = null;
		Body b = sm.retrieveActiveBody();
		UnitGraph unitGraph = new BriefUnitGraph(b);  //don't consider exception edges
		MHGPostDominatorsFinder<Unit> pdFinder = new MHGPostDominatorsFinder<Unit>(unitGraph);
		unitGraph = null;
		Unit firstUnit = b.getUnits().getFirst();
		tmp = pdFinder.getDominators(firstUnit);
		pds.addAll(tmp);
		for(int i=0;i<tmp.size();i++) {
			Unit unit = tmp.get(i);
			Stmt stmt = (Stmt)unit;
			if((stmt instanceof InvokeStmt || stmt instanceof AssignStmt) && stmt.containsInvokeExpr()) {
				SootMethod s = stmt.getInvokeExpr().getMethod();
				if(ignoredMethods.contains(s.getSignature()))
					return null;
				ignoredMethods.add(s.getSignature());
				List<Unit> appendpds = getMBEUnit(s,ignoredMethods);
				if(appendpds!=null)
					pds.addAll(appendpds);
				
			}
		}
		return pds;
	}

	/**
	 * ʹ��callgraph���sootMethod���������з���
	 */
	public static List<SootMethod> getTargetsMethods(SootMethod sootMethod) {
		List<SootMethod> sMethods = new ArrayList<SootMethod>();
		Iterator<MethodOrMethodContext> targets = new Targets(callGraph.edgesOutOf(sootMethod));
		while (targets.hasNext()) {
			SootMethod m = (SootMethod) targets.next();
			sMethods.add(m);
		}
		return sMethods;
	}

	
	public static List<SootMethod> getSourcesMethods(SootMethod sootMethod) {
		List<SootMethod> sMethods = new ArrayList<SootMethod>();
		Iterator<MethodOrMethodContext> sources = new Targets(callGraph.edgesInto(sootMethod));
		while (sources.hasNext()) {
			SootMethod m = (SootMethod) sources.next();
			sMethods.add(m);

		}

		return sMethods;
	}

	public static void getTargetEdges(SootMethod sootMethod) {
		Iterator<Edge> iterator = callGraph.edgesOutOf(sootMethod);
		while (iterator.hasNext()) {
			Edge edge = iterator.next();
			System.out.println("edge:" + edge);
		}
	}

	public static Iterator<Edge> getSourceEdges(SootMethod sootMethod) {
		return callGraph.edgesInto(sootMethod);
	}

	public static ArrayList<SootMethod> getAllPreviousMethods(SootMethod sm) {
		ArrayList<SootMethod> preMethods = new ArrayList<SootMethod>();
		Iterator<MethodOrMethodContext> iterator = callGraph.sourceMethods();
		List<SootMethod> temp = new ArrayList<SootMethod>();
		while (iterator.hasNext()) {
			SootMethod smMethod = (SootMethod) iterator.next();
			if (!smMethod.getSignature().startsWith("<android") && !smMethod.getSignature().startsWith("<java")
					&& !smMethod.getSignature().contains("<init>"))
				temp.add(smMethod);
		}
		getAllMethos(temp);
		for (int j = 0; j < allMethods.size(); j++) {
			SootMethod smMethod = allMethods.get(j);
			if (smMethod.toString().contains(sm.toString())) {

				if (!preMethods.contains(smMethod)) {
					preMethods.add(smMethod);
				}

				for (int i = 0; i < preMethods.size(); i++) {
					Iterator<MethodOrMethodContext> sources = new Sources(callGraph.edgesInto(preMethods.get(i)));
					// System.out.println("source:"+sources);//Iterator it = callGraph.edgesInto(m);
					while (sources.hasNext()) {
						SootMethod sourceMethod = (SootMethod) sources.next();
						System.out.println("sootMethod:" + sourceMethod);
						if (sourceMethod.getSignature().startsWith("<android"))
							continue;
						if (!preMethods.contains(sourceMethod) && !sourceMethod.getName().equals("dummyMainMethod")) {
							preMethods.add(sourceMethod);
						}
					}
				}

				break;
			}
		}

		return preMethods;
	}

	// �����local�йص����о䡣
	public static List<Stmt> getStmtsWithLocal(Local l, UnitGraph ug) {
		List<Stmt> stmts = new ArrayList<Stmt>();
		SootMethod sm = ug.getBody().getMethod();
		MethodTag mt = new MethodTag(sm);
		Iterator<Unit> bUnits = ug.iterator();
		while (bUnits.hasNext()) {
			Unit u = bUnits.next();
			List<ValueBox> vbsBoxs = u.getUseAndDefBoxes();
			for (ValueBox vb : vbsBoxs) {
				if (vb.getValue() instanceof Local) {
					if (vb.getValue().equals(l)) {
						stmts.add((Stmt) u);
						u.addTag(mt);
						break;
					}
				}
			}
		}
		return stmts;
	}

	public static Set<SootClass> searchClassesContainsType(String name) {
		Set<SootClass> classes = new HashSet<SootClass>();
		Chain<SootClass> classes2 = Scene.v().getClasses();
		SootClass exceptClass = Scene.v().getSootClass(name);
		List<SootClass> subClasses = new ArrayList<SootClass>();
		if (exceptClass.isAbstract()) {
			// List<SootClass> subClasses =
			// Scene.v().getActiveHierarchy().getSubclassesOf(exceptClass);
			subClasses = Scene.v().getActiveHierarchy().getSubclassesOfIncluding(exceptClass);
		}
		classes2.removeAll(subClasses);
		for (SootClass ex : subClasses) {
			for (SootClass sc : classes2)
				if (sc.isConcrete())
					for (SootMethod sm : sc.getMethods())
						if (sm.isConcrete() && !classes.contains(sm) && LocalAnalysis.containLocal(sm, ex.getName())) {
							classes.add(sc);
							break;
						}
		}
		classes2.addAll(subClasses);
		return classes;
	}

	public static void getAllMethos(List<SootMethod> list1) {
		allMethods.addAll(list1);
		List<SootMethod> temp = new ArrayList<SootMethod>();
		for (int i = 0; i < list1.size(); i++) {
			Iterator<MethodOrMethodContext> iterator = new Targets(callGraph.edgesOutOf(list1.get(i)));
			while (iterator.hasNext()) {
				SootMethod sMethod = (SootMethod) iterator.next();
				if (!sMethod.getSignature().startsWith("<android") && !sMethod.getSignature().startsWith("<java")
						&& !sMethod.getSignature().contains("<init>"))
					temp.add(sMethod);

			}
		}
		try {
			if (!temp.isEmpty()) {
				getAllMethos(temp);
			}
		} catch (Exception e) {
		}

	}

}