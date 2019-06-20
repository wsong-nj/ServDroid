package tool.entryForAllApks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import org.xmlpull.v1.XmlPullParserException;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.config.SootConfigForAndroid;
import soot.jimple.infoflow.cfg.LibraryClassPatcher;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.options.Options;
import tool.Analy.Analysis.AndroidAnalysis;
import tool.Result.result2excel;

import com.test.xmldata.ProcessManifest;

public class EntryForAll { // Entry

	private String apkFileDirectory; // APK fileS directory
	private String androidPlatformLocation; // Android platform path
	private String apkFileLocation; // APK file path
	private ArrayList<String> AllApkFilePathList; // All APK file path list
	public long runningTime;
	public int selectedApkCount;
	int lines;
	private result2excel excel; // Excel
	private String ResultExcelLocation; // Excel path
	public static CallGraph callGraph;
	public static InfoflowCFG info;


	public EntryForAll(String[] args) { // Initialization

		apkFileDirectory = args[0];
		androidPlatformLocation = args[1];
		AllApkFilePathList = new ArrayList<String>();
		ResultExcelLocation = "Results.xls";
		excel = new result2excel();
		excel.initExcel(this.ResultExcelLocation);
	}

	public ArrayList<String> getApkFiles() { // Get APK files, return APK name
												// list

		File f = new File(this.apkFileDirectory);
		File[] list = f.listFiles();
		String filePath, fileName, fileExtension;
		ArrayList<String> apkNameList = new ArrayList<String>();
		for (int i = 0; i < list.length; i++) {
			filePath = list[i].getAbsolutePath();
			fileName = list[i].getName();
			int index1 = filePath.lastIndexOf(".");
			int index2 = filePath.length();
			fileExtension = filePath.substring(index1 + 1, index2);
			if (fileExtension.equals("apk")) { // If the file is APK
				this.AllApkFilePathList.add(filePath);
				apkNameList.add(fileName);
			}
		}
		return apkNameList;
	}

	public void AnalyzeAll(ArrayList<Integer> selectedApkIndexList) throws IOException, XmlPullParserException {
		selectedApkCount = selectedApkIndexList.size(); 
		int curIndex; // Current APK index
		String curAppName; // Current APK name
		long start = System.currentTimeMillis(); // Time when it starts
		for (int i = 0; i < selectedApkCount; i++) {
			curIndex = selectedApkIndexList.get(i);
			apkFileLocation = AllApkFilePathList.get(curIndex).toString();
			curAppName = apkFileLocation.substring(apkFileLocation.lastIndexOf("\\") + 1,
					apkFileLocation.lastIndexOf("."));
			System.out.println("App count: " + (i + 1));
			System.out.println("App path: " + apkFileLocation);
			System.out.println("App name: " + curAppName);
			String param[] = { "-android-jars", androidPlatformLocation, "-process-dir", apkFileLocation };
			initSoot(param);
			AndroidAnalysis analysis = new AndroidAnalysis(apkFileLocation, callGraph);
			analysis.Analyze();

			ProcessManifest processMan = new ProcessManifest();
			processMan.loadManifestFile(apkFileLocation);
			excel.addOneLine2Excel(curAppName, analysis, processMan, i + 1);
			G.v();
			G.reset();
		}
		excel.WriteAll(); // Write all line into excel
		long end = System.currentTimeMillis(); // Time when it ends
		runningTime = (end - start) / 1000; // Running Time(:s)
		System.out.println("It takes " + runningTime + " seconds to analyze all these " + selectedApkCount + " apps");
	}

	public static void initSoot(String[] param) throws IOException, XmlPullParserException {
		/*
		 * Params args[0,1]: APK file location args[2,3]: Path of Android platform
		 */
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_output_format(Options.output_format_jimple);
		Options.v().set_process_multiple_dex(true);
		Options.v().set_output_dir("JimpleOutput");
		Options.v().set_keep_line_number(true);
		Options.v().set_prepend_classpath(true);
	    Options.v().set_allow_phantom_refs(true);
    	Options.v().set_android_jars(param[1]);
	    Options.v().set_process_dir(Collections.singletonList(param[3]));
	    Options.v().set_whole_program(true);
		Options.v().set_force_overwrite(true); 
		Scene.v().loadNecessaryClasses();	// Load necessary classes
        CHATransformer.v().transform(); //Call graph
        callGraph=Scene.v().getCallGraph();
        JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG();
		info = new InfoflowCFG(icfg);
		
        Scene.v().addBasicClass("java.io.BufferedReader",SootClass.HIERARCHY);
		Scene.v().addBasicClass("java.lang.StringBuilder",SootClass.BODIES);
		Scene.v().addBasicClass("java.util.HashSet",SootClass.BODIES);
		Scene.v().addBasicClass("android.content.Intent",SootClass.BODIES);
		Scene.v().addBasicClass("java.io.PrintStream",SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.System",SootClass.SIGNATURES); 
        Scene.v().addBasicClass("com.app.test.CallBack",SootClass.BODIES);		
        Scene.v().addBasicClass("java.io.Serializable",SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.io.Serializable",SootClass.BODIES);
        Scene.v().addBasicClass("android.graphics.PointF",SootClass.SIGNATURES);
        Scene.v().addBasicClass("android.graphics.PointF",SootClass.BODIES);
        Scene.v().addBasicClass("org.reflections.Reflections",SootClass.HIERARCHY);
        Scene.v().addBasicClass("org.reflections.scanners.Scanner",SootClass.HIERARCHY);
        Scene.v().addBasicClass("org.reflections.scanners.SubTypesScanner",SootClass.HIERARCHY);
        Scene.v().addBasicClass("java.lang.ThreadGroup",SootClass.SIGNATURES);
        Scene.v().addBasicClass("com.ironsource.mobilcore.OfferwallManager",SootClass.HIERARCHY);
        Scene.v().addBasicClass("bolts.WebViewAppLinkResolver$2",SootClass.HIERARCHY);
        Scene.v().addBasicClass("com.ironsource.mobilcore.BaseFlowBasedAdUnit",SootClass.HIERARCHY);
        Scene.v().addBasicClass("android.annotation.TargetApi",SootClass.SIGNATURES);
        Scene.v().addBasicClass("com.outfit7.engine.Recorder$VideoGenerator$CacheMgr",SootClass.HIERARCHY);
        Scene.v().addBasicClass("com.alibaba.motu.crashreporter.handler.CrashThreadMsg$",SootClass.HIERARCHY);
        Scene.v().addBasicClass("java.lang.Cloneable",SootClass.HIERARCHY);
        Scene.v().addBasicClass("org.apache.http.util.EncodingUtils",SootClass.SIGNATURES);
        Scene.v().addBasicClass("org.apache.http.protocol.HttpRequestHandlerRegistry",SootClass.SIGNATURES);
        Scene.v().addBasicClass("org.apache.commons.logging.Log",SootClass.SIGNATURES);
        Scene.v().addBasicClass("org.apache.http.params.HttpProtocolParamBean",SootClass.SIGNATURES);
        Scene.v().addBasicClass("org.apache.http.protocol.RequestExpectContinue",SootClass.SIGNATURES);
        Scene.v().loadClassAndSupport("Constants");	
        /*
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
		 app = new SetupApplication(param[1], param[3]);
		 app.setSootConfig(sootConf);
		 app.runInfoflow("./SourcesAndSinks3.txt");
		
		 Options.v().set_allow_phantom_refs(true);
		 Options.v().set_prepend_classpath(true);
		 Options.v().set_whole_program(true);
		 Options.v().set_process_dir(Collections.singletonList(param[3]));
		 Options.v().set_keep_line_number(true);
		 Options.v().set_src_prec(Options.src_prec_apk);
		 Options.v().set_output_format(Options.output_format_dex);
		 Options.v().process_multiple_dex();
		 Options.v().set_process_multiple_dex(true);
		 Options.v().set_android_jars(param[1]);
		
		 Scene.v().loadNecessaryClasses();
		 Scene.v().loadBasicClasses();
		 new LibraryClassPatcher().patchLibraries();
		 // Create the entry point
		 SootMethod entryPoint = app.getDummyMainMethod();
		 entryPoint.getDeclaringClass().setLibraryClass();
		 Options.v().set_main_class(entryPoint.getDeclaringClass().getName());
		 Scene.v().setEntryPoints(Collections.singletonList(entryPoint));
		
		 PackManager.v().runPacks();
		 app.setCallbackFile("./AndroidCallbacks.txt");
		 app.constructCallgraph();
		 callGraph = Scene.v().getCallGraph();
		 System.out.println("-----end");
		 */
	}

	public Object[] getResult(int i) {// Get results
		Workbook book;
		try {
			book = Workbook.getWorkbook(new File(ResultExcelLocation));
			Sheet sheet = book.getSheet(result2excel.SheetName);
			Object[] rowData = new Object[10];
			rowData[0] = sheet.getCell(0, i).getContents();
			rowData[1] = sheet.getCell(1, i).getContents();
			rowData[2] = sheet.getCell(2, i).getContents();
			rowData[3] = sheet.getCell(3, i).getContents();
			rowData[4] = sheet.getCell(4, i).getContents();
			rowData[5] = sheet.getCell(5, i).getContents();
			rowData[6] = sheet.getCell(6, i).getContents();
			//rowData[7] = sheet.getCell(7, i).getContents();
			//rowData[8] = sheet.getCell(8, i).getContents();
			//rowData[9] = sheet.getCell(9, i).getContents();


			book.close();
			return rowData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
