/*package com.event;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.Intent;
import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.PackManager;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Transform;
import soot.JastAddJ.AssignExpr;
import soot.jimple.AssignStmt;
import soot.jimple.Jimple;
import soot.options.Options;

public class MainTest {
	
	public static class Configuration{
		public static String androidPlatformLocation = "E:\\android\\sdk\\platforms";
		public static String apkName = "music";
		public static String apkDir = "E:\\workspace-zch\\Extractocol_public-master\\Extractocol_public-master\\SerializationFiles\\";
		public static String apkFileLocation = apkDir + apkName + ".apk";
		
	}
	
	public static boolean flag = false;
	
	public static void main(String[] args) throws Exception {  
    	
    	G.reset();
		Options.v().set_soot_classpath("./lib/rt.jar;"+"E:\\Java\\jre1.8\\lib\\jce.jar;" +
				"./lib/tools.jar;" +
				"./lib/android.jar;"+
				"./lib/android-support-v4.jar;"+
				"./bin;"
				);
		
    	Options.v().set_allow_phantom_refs(true);
        //prefer Android APK files// -src-prec apk  
        Options.v().set_src_prec(Options.src_prec_apk); 
        Options.v().set_android_jars(Configuration.androidPlatformLocation);
        Options.v().set_process_dir(Collections.singletonList(Configuration.apkFileLocation));
        
        Options.v().set_prepend_classpath(true);
        Options.v().set_whole_program(true);
        //Options.v().set_soot_classpath("./bin");
        Options.v().set_process_multiple_dex(true);
        Options.v().set_force_overwrite(true);
        Options.v().set_keep_line_number(true);
        Options.v().set_include_all(true);
        Options.v().set_validate(true);
          
        //output as APK, too//-f J  
        Options.v().set_output_format(Options.output_format_jimple);
          
        Scene.v().addBasicClass("android.app.Dialog",SootClass.BODIES);  
        Scene.v().addBasicClass("android.view.View",SootClass.BODIES);
        Scene.v().addBasicClass("android.view.MenuItem",SootClass.BODIES);
        Scene.v().addBasicClass("android.app.Activity", SootClass.SIGNATURES);
        Scene.v().loadNecessaryClasses();
        Scene.v().loadBasicClasses();

        //test findRegisterForView
//        SootMethod sm = Scene.v().getMethod("<com.ted.android.view.activity.VideoActivity$5: void onItemClick(android.widget.AdapterView,android.view.View,int,long)>");
//        SootClass sootClass = Scene.v().getSootClass("com.ted.android.view.activity.VideoActivity");
//        System.out.println("method: " + sm);
//        System.out.println("class: " + sootClass);
//        System.out.println(sootClass.getMethods());
//        EventHandler handler = new EventHandler(sm, sootClass);
//        handler.findRegisterForView();
//        System.out.println("eventType:" + handler.getEventType());
//        System.out.println("register:" + handler.getEventRegisterMethod());
        (new AndroidEventInfo(Scene.v().getCallGraph())).printEventHandlers();
		//AndroidEventInfo.findHandlersFromLayout();
		//AndroidEventInfo.printHandlersInLayout();
        //test findRegisterForMenu
        SootMethod sm = Scene.v().getMethod("<com.ted.android.view.activity.TalkDetailActivity: boolean onOptionsItemSelected(android.view.MenuItem)>");
        SootMethod target = Scene.v().getMethod("<com.ted.android.utility.GoogleAnalyticsHelper: void trackNextTalkEvent(java.lang.String)>");
        System.out.println("method: " + sm);
        System.out.println("target: " + target);
        EventHandler handler = new EventHandler();
        handler.findRegisterForMenu(sm, target);
        
        //soot.Main.main(args);  
        //PackManager.v().runPacks();
	    //PackManager.v().writeOutput();
    } 

}
*/