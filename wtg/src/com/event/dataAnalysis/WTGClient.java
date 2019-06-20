package com.event.dataAnalysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xmlpull.v1.XmlPullParserException;

import com.beust.jcommander.internal.Lists;
import com.event.AndroidEventInfo;
import com.event.CGExporter;
import com.event.EventHandler;
import com.event.GetActivityWithFragment;
import com.event.Path;
import com.event.Util.basicAnalysis;
import com.event.wtgStructure.WTG;
import com.event.wtgStructure.WTGEdge;
import com.event.wtgStructure.WTGNode;

import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.config.SootConfigForAndroid;
import soot.jimple.infoflow.cfg.LibraryClassPatcher;
import soot.jimple.infoflow.source.data.NullSourceSinkDefinitionProvider;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

public class WTGClient {

	public static String androidPlatformLocation;
	public static String apkName;
	public static String apkDir;
	public static String apkFileLocation;
	public static String saveDir = "F:\\zj\\testcase\\";

	public static CallGraph cg;
	public static WTG wtg;

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String androidPlatformLocation = "D:\\adt-eclipse\\sdk\\platforms";
		String apkFileLocation = "F:\\apk下载\\";
		String apkName = "Instagram Lite_v19.0.0.9.101_apkpure.com";
		WTGClient client = new WTGClient(androidPlatformLocation, apkName, apkFileLocation);
		client.getWTG();
		client.getAllPaths();

		// initializeSoot();
		// new AndroidEventInfo(cg).printEventHandlers().printAsyns();
		// new GetActivityWithFragment().printActivityWithFragments();
		// String apkFileLocation = WTGClient.apkFileLocation;
		// IntentAnalysis analysis=new IntentAnalysis(apkFileLocation, cg);
		// analysis.doAnalysis();
		// wtg = analysis.getWTG();
		// System.out.println(wtg.toString());
		// saveResult(wtg);
		// System.out.println("---save wtg finished!---");
		// generateGraph();
		// System.out.println("---generate wtg graph finished!---");
		// SootMethod sm = Scene.v().getMethod("<com.nephoapp.anarxiv.PaperListWnd: void
		// onScroll(android.widget.AbsListView,int,int,int)>");
		// generatePathsToTargetMethod(sm);
		// System.out.println("----generate paths finished!---");
	}

	public WTGClient(String androidPlatformLocation, String apkName, String apkDir) throws Exception {
		setConfig(androidPlatformLocation, apkName, apkDir);
		initializeSoot();
		String newPath = saveDir + apkName + "\\";// 指定新路径
		File file = new File(newPath);// 定义一个文件流
		file.mkdir();// 创建文件夹
	}

	public void analyse() throws Exception {
		getWTG();
		getAllPaths();
	}

	public WTG getWTG() throws Exception {
		new AndroidEventInfo(cg).printEventHandlersAndAsyns();
		new GetActivityWithFragment().printActivityWithFragments();
		IntentAnalysis analysis = new IntentAnalysis(apkFileLocation, cg);
		analysis.doAnalysis();
		wtg = analysis.getWTG();
		System.out.println(wtg.toString());
		saveResult(wtg);
		System.out.println("---save wtg finished!---");
		generateGraph();
		System.out.println("---generate wtg graph finished!---");
		System.out.println("mainActivity:" + analysis.processMan.mainActivity);
		return wtg;
	}

	public void setConfig(String androidPlatformLocation, String apkName, String apkDir) {
		WTGClient.androidPlatformLocation = androidPlatformLocation;
		WTGClient.apkName = apkName;
		WTGClient.apkDir = apkDir;
		WTGClient.apkFileLocation = apkDir + apkName + ".apk";
	}

	public void setSavePath(String dir) {
		saveDir = dir;
	}

	public static void initializeSoot() throws IOException, XmlPullParserException {
		SootConfigForAndroid sootConf = new SootConfigForAndroid() {
			@Override
			public void setSootOptions(Options options) {
				options.set_allow_phantom_refs(true);
				options.set_process_multiple_dex(true);
				options.set_whole_program(true);
				options.set_keep_line_number(true);
				options.set_no_bodies_for_excluded(true);
				super.setSootOptions(options);
			}
		};
		SetupApplication app = null;
		app = new SetupApplication(androidPlatformLocation, apkFileLocation);
		app.setSootConfig(sootConf);
		app.getConfig().setEnableImplicitFlows(true);
		app.setCallbackFile("./AndroidCallbacks.txt");
		app.calculateSourcesSinksEntrypoints(new NullSourceSinkDefinitionProvider());

		Options.v().set_allow_phantom_refs(true);
		Options.v().set_prepend_classpath(true);
		Options.v().set_whole_program(true);
		Options.v().set_process_dir(Collections.singletonList(apkFileLocation));
		Options.v().set_keep_line_number(true);
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_output_format(Options.output_format_dex);
		Options.v().process_multiple_dex();
		Options.v().set_process_multiple_dex(true);
		Options.v().set_android_jars(androidPlatformLocation);
		Options.v().set_no_writeout_body_releasing(true);

		// We need a callgraph
		//Options.v().setPhaseOption("cg", "all-reachable:true");
		Options.v().setPhaseOption("cg", "safe-forname:true");
		Options.v().setPhaseOption("cg", "safe-newinstance:true");
		Options.v().setPhaseOption("cg.spark", "enabled:true");
		//Options.v().setPhaseOption("cg.spark", "vta:true");

		Scene.v().loadNecessaryClasses();
		Scene.v().loadBasicClasses();
		new LibraryClassPatcher().patchLibraries();
		// Create the entry point
		app.getEntryPointCreator().setDummyMethodName("main");
		SootMethod entryPoint = app.getEntryPointCreator().createDummyMain();
		entryPoint.getDeclaringClass().setLibraryClass();
		Options.v().set_main_class(entryPoint.getDeclaringClass().getName());
		Scene.v().setEntryPoints(Collections.singletonList(entryPoint));
		//Scene.v().setEntryPoints(Scene.v().getEntryPoints());
		PackManager.v().runPacks();
		cg = Scene.v().getCallGraph();
		System.out.println("size:" + cg.size());
		handlerCallGraph();
		System.out.println("new size:" + cg.size());
		saveCallGraph(cg);

		System.out.println("-----end");
	}

	private static void handlerCallGraph() {
		List<Edge> list = new ArrayList<>();
		for (Iterator<Edge> iterator = cg.iterator(); iterator.hasNext();) {
			Edge edge = iterator.next();
			String className = edge.getSrc().method().getDeclaringClass().getName();
			if (AndroidEventInfo.isClassInSystemPackage(className)) {
				list.add(edge);
			}
		}
		for (Edge edge : list) {
			cg.removeEdge(edge);
		}
	}

	public void getAllPaths() {
		int j = 0;
		for (SootClass sc : basicAnalysis.classesChain) {
			for (SootMethod sm : sc.getMethods()) {
				if (sm.isConcrete()) {
					for (Unit unit : sm.retrieveActiveBody().getUnits()) {
						Stmt stmt = (Stmt) unit;
						if (stmt.containsInvokeExpr()) {
							SootMethod sootMethod = stmt.getInvokeExpr().getMethod();
							if (sootMethod.getName().equals("startService")||sootMethod.getName().equals("stopService")||
									sootMethod.getName().equals("bindService")||sootMethod.getName().equals("unbindService")) {
								generatePathsToTargetMethod(sm, j++);
							}
						}
					}
				}
			}

		}
	}

	public static List<Path> generatePathsToTargetMethod(SootMethod method, int i) {
		if (wtg == null) {
			System.out.println("wtg is null, please invoke getWTG() before this!");
			return null;
		}
		List<SootMethod> methods = new ArrayList<>();
		List<EventHandler> eventList = new ArrayList<>();
		Map<String, Set<List<EventHandler>>> activities = new HashMap<>();
		InterAnalysis.dfs(method, null, cg, methods, eventList, activities);
		// System.out.println(method + ": \n\t" + activities);
		List<Path> pathList = Lists.newArrayList();
		for (String act : activities.keySet()) {
			List<List<WTGEdge>> paths = generatePathsToTargetActivity(act);
			for (List<WTGEdge> path : paths) {
				for (List<EventHandler> handler : activities.get(act)) {
					Path p = new Path(path, act, method.getSignature(), handler);
					pathList.add(p);
				}
			}
		}
		savePaths(method, pathList, i);
		return pathList;
	}

	public static List<List<WTGEdge>> generatePathsToTargetActivity(String act) {
		if (wtg == null) {
			System.out.println("wtg is null, please invoke getWTG() before this!");
			return null;
		}
		WTGNode node = wtg.getNodeByName(act);
		if (node == null) {
			System.out.println("there is not a node " + act + "in wtg");
			return null;
		}
		List<List<WTGEdge>> paths = Lists.newArrayList();
		List<WTGEdge> path = Lists.newArrayList();
		List<WTGNode> nodeList = Lists.newArrayList();
		explorePath(node, paths, path, nodeList);
		return paths;
	}

	private static void explorePath(WTGNode node, List<List<WTGEdge>> paths, List<WTGEdge> path,
			List<WTGNode> nodeList) {
		if (!nodeList.contains(node))
			nodeList.add(node);

		for (WTGEdge inEdge : node.getInEdges()) {
			WTGNode srcNode = inEdge.getSrcNode();
			path.add(inEdge);
			if (srcNode.countInEdges() != 0 && !nodeList.contains(srcNode) && !srcNode.equals(wtg.getLauncherNode())) {
				explorePath(srcNode, paths, path, nodeList);
			} else if (!nodeList.contains(srcNode)) {
				List<WTGEdge> list = Lists.newArrayList();
				list.addAll(path);
				Collections.reverse(list);
				paths.add(list);
			}
			path.remove(inEdge);
		}
		nodeList.remove(node);
	}

	public static void generateGraph() {
		CGExporter cge = new CGExporter();
		for (WTGEdge edge : wtg.getEdges()) {
			String srcNode = edge.getSrcNode().toString();
			String tarNode = edge.getTgtNode().toString();
			cge.createNode(srcNode);
			cge.createNode(tarNode);
			cge.linkNodeByID(edge);

		}
		String newPath = saveDir + apkName + "\\";// 指定新路径
		cge.exportMIG(apkName + "_WTGgraph", newPath);

	}

	public CallGraph getCallGraph() {
		return cg;
	}

	private static void saveCallGraph(CallGraph cg) {
		try {
			String newPath = saveDir + apkName + "\\";// 指定新路径
			FileWriter file = new FileWriter(newPath + apkName + "_callgraph.txt");
			file.write(cg.toString());
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void saveResult(WTG wtg) {
		try {
			String newPath = saveDir + apkName + "\\";// 指定新路径
			FileWriter file = new FileWriter(newPath + apkName + "_wtg.txt");
			file.write(wtg.toString());
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void savePaths(SootMethod method, List<Path> paths, int j) {
		try {
			String newPath = saveDir + apkName + "\\";// 指定新路径
			FileWriter file = new FileWriter(newPath + apkName + "_" + j + "_paths.txt");
			int i = 0;
			file.write(method.getSignature() + "\n");
			for (Path p : paths) {
				file.write("path" + (++i) + "\n");
				file.write(p.toString());
			}
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
