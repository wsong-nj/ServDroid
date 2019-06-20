package tool.GUI;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import com.event.dataAnalysis.WTGClient;

import soot.Body;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.options.Options;


public class EntryForAll { //Entry
	
	private String apkFileDirectory; //APK fileS directory
	private String androidPlatformLocation; //Android platform path
	private String apkFileLocation; //APK file path
	private ArrayList<String> AllApkFilePathList; //All APK file path list
	public long runningTime;
	public int selectedApkCount;
    int lines;

    public EntryForAll(String[] args){ //Initialization
    	
		apkFileDirectory=args[0];
		androidPlatformLocation=args[1];
		AllApkFilePathList=new ArrayList<String>();


		}
    
	public ArrayList<String> getApkFiles(){ //Get APK files, return APK name list
	     
		 File f=new File(this.apkFileDirectory);
		 File[] list=f.listFiles();
		 String filePath,fileName,fileExtension; 
		 ArrayList<String> apkNameList=new ArrayList<String>();
		 for(int i=0;i<list.length;i++)
		 {
			 filePath=list[i].getAbsolutePath(); 
			 fileName=list[i].getName();  
			 int index1=filePath.lastIndexOf(".");
			 int index2=filePath.length();
			 fileExtension=filePath.substring(index1+1,index2);
			 if(fileExtension.equals("apk")){ //If the file is APK
				 this.AllApkFilePathList.add(filePath);
				 apkNameList.add(fileName);
			 }
		 }
	     return apkNameList;
	}
	
	public void AnalyzeAll(ArrayList<Integer> selectedApkIndexList) throws Exception{//Analyze all APKs
        /*
         * Param: Index list of selected APKs  
         */
		selectedApkCount=selectedApkIndexList.size(); //Number of selected APKs
		int curIndex; //Current APK index
		String curAppName; //Current APK name
		String apkDir=apkFileDirectory+"\\";
		long start=System.currentTimeMillis(); // Time when it starts 
		for(int i=0;i<selectedApkCount;i++){
			try {
				curIndex=selectedApkIndexList.get(i);
				apkFileLocation=AllApkFilePathList.get(curIndex).toString();
				curAppName = apkFileLocation.substring(apkFileLocation.lastIndexOf("\\") + 1, apkFileLocation.lastIndexOf("."));
				System.out.println("App count: "+(i+1));
				System.out.println("App path: "+apkFileLocation);
				System.out.println("App name: "+curAppName);
				//String androidPlatformLocation = "D:\\adt-eclipse\\sdk\\platforms";
				WTGClient analysis=new WTGClient(androidPlatformLocation,curAppName,apkDir);
				analysis.analyse();						
				G.v();
				G.reset();
			}catch (Exception e) {
				continue;
				// TODO: handle exception
			}
			
		}
		long end=System.currentTimeMillis(); //Time when it ends
	    runningTime=(end-start)/1000; // Running Time(:s)
		System.out.println("It takes "+runningTime+" seconds to analyze all these "+selectedApkCount+" apps");
	}
}
