/*******************************************************************************
 * Copyright (c) 2012 Secure Software Engineering Group at EC SPRIDE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors: Christian Fritz, Steven Arzt, Siegfried Rasthofer, Eric
 * Bodden, and others.
 ******************************************************************************/
package com.event;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import org.xmlpull.v1.XmlPullParserException;

import soot.Body;
import soot.PackManager;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.config.SootConfigForAndroid;
import soot.jimple.infoflow.handlers.ResultsAvailableHandler;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.results.ResultSinkInfo;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.results.xml.InfoflowResultsSerializer;
import soot.jimple.infoflow.solver.cfg.BackwardsInfoflowCFG;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import soot.jimple.infoflow.taintWrappers.EasyTaintWrapper;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;
import soot.options.Options;

public class ViewToListeners {
	
	private static final class MyResultsAvailableHandler implements
			ResultsAvailableHandler {
		private final BufferedWriter wr;

		private MyResultsAvailableHandler() {
			this.wr = null;
		}

		private MyResultsAvailableHandler(BufferedWriter wr) {
			this.wr = wr;
		}

		@Override
		public void onResultsAvailable(
				IInfoflowCFG cfg, InfoflowResults results) {
			// Dump the results
			if (results == null) {
				print("No results found.");
			}
			else {
				// Report the results
				for (ResultSinkInfo sink : results.getResults().keySet()) {
					
						print("Found a flow to sink " + sink + ", from the following sources:");
					
					
					for (ResultSourceInfo source : results.getResults().get(sink)) {
						
						print("\t- " + source.getSource() + " (in "
								+ cfg.getMethodOf(source.getSource()).getSignature()  + ")");
						
						if (source.getPath() != null)
							print("\t\ton Path " + Arrays.toString(source.getPath()));
					}
				}
				
				// Serialize the results if requested
				// Write the results into a file if requested
				if (resultFilePath != null && !resultFilePath.isEmpty()) {
					InfoflowResultsSerializer serializer = new InfoflowResultsSerializer(cfg);
					try {
						serializer.serialize(results, resultFilePath);
					} catch (FileNotFoundException ex) {
						System.err.println("Could not write data flow results to file: " + ex.getMessage());
						ex.printStackTrace();
						throw new RuntimeException(ex);
					} catch (XMLStreamException ex) {
						System.err.println("Could not write data flow results to file: " + ex.getMessage());
						ex.printStackTrace();
						throw new RuntimeException(ex);
					}
				}
			}
			
		}

		private void print(String string) {
			try {
				System.out.println(string);
				if (wr != null)
					wr.write(string + "\n");
			}
			catch (IOException ex) {
				// ignore
			}
		}
	}
	
	private static InfoflowAndroidConfiguration config = new InfoflowAndroidConfiguration();
	
	private static boolean aggressiveTaintWrapper = false;
	private static String resultFilePath = "sootOutput/HandlerAnalysis.xml";

	
	public static final String androidPlatformLocation = "E:\\android\\sdk\\platforms";
	public static final String apkName = "tumblr";
	public static final String apkFileLocation = "E:\\workspace-zch\\Extractocol_public-master\\Extractocol_public-master\\SerializationFiles\\" + apkName + ".apk";

	public static void main(final String[] args) throws IOException, InterruptedException {
		
		
		runAnalysis(apkFileLocation, androidPlatformLocation);
		
	}

	private static InfoflowResults runAnalysis(final String fileName, final String androidJar) {
		try {
			final long beforeRun = System.nanoTime();

			final SetupApplication app;
			app = new SetupApplication(androidJar, fileName);
			// Set configuration object
			app.getConfig().setEnableStaticFieldTracking(false);
			app.getConfig().setStopAfterFirstFlow(false);
			app.getConfig().setFlowSensitiveAliasing(false);
			app.getConfig().setIgnoreFlowsInSystemPackages(true);
			
			SootConfigForAndroid sootConfig = new SootConfigForAndroid() {

				@Override
				public void setSootOptions(Options options) {
					// TODO Auto-generated method stub
					options.set_whole_program(true);
					options.set_process_multiple_dex(true);
					super.setSootOptions(options);
				}

				
				
			};
			app.setSootConfig(sootConfig);
			
			final ITaintPropagationWrapper taintWrapper;
			final EasyTaintWrapper easyTaintWrapper;
			File twSourceFile = new File("../soot-infoflow/EasyTaintWrapperSource.txt");
			if (twSourceFile.exists())
				easyTaintWrapper = new EasyTaintWrapper(twSourceFile);
			else {
				twSourceFile = new File("EasyTaintWrapperSource.txt");
				if (twSourceFile.exists())
					easyTaintWrapper = new EasyTaintWrapper(twSourceFile);
				else {
					System.err.println("Taint wrapper definition file not found at "
							+ twSourceFile.getAbsolutePath());
					return null;
				}
			}
			easyTaintWrapper.setAggressiveMode(aggressiveTaintWrapper);
			taintWrapper = easyTaintWrapper;
			
			app.setTaintWrapper(taintWrapper);
			
			app.calculateSourcesSinksEntrypoints("SourcesAndSinks.txt");
			System.out.println("Running data flow analysis...");
			//app.addResultsAvailableHandler();
			
			//final InfoflowResults res = app.runInfoflow("SourcesAndSinks.txt");
			final InfoflowResults res = app.runInfoflow(new MyResultsAvailableHandler());
			//final InfoflowResults res = app.runInfoflow(new NullSourceSinkDefinitionProvider());
			System.out.println(app.getCallbackClasses());
			
			System.out.println(Scene.v().getCallGraph().size());
			InfoflowCFG icfg = new InfoflowCFG();
			BackwardsInfoflowCFG bcfg = new BackwardsInfoflowCFG(icfg);
			for(SootClass sc : Scene.v().getClasses()) {
				if(sc.isConcrete()) {
					for(SootMethod sm : sc.getMethods()) {
						if(!sm.isConcrete())
							continue;
						Body body = sm.retrieveActiveBody();
						if(body == null) {
							continue;
						}
						PatchingChain<Unit> units = body.getUnits();
						for(Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
							final Stmt st = (Stmt) iter.next();
							if(st.containsInvokeExpr()) {
								InvokeExpr invokeExpr = (InvokeExpr) st.getInvokeExpr();
								SootMethod method = invokeExpr.getMethod();
								String subsig = method.getName();
								if("startActivity".equals(subsig)) {
									System.out.println(bcfg.getMethodOf(st));
								}
							}
				}
						}
					}
			}
			System.out.println("Analysis has run for " + (System.nanoTime() - beforeRun) / 1E9 + " seconds");
			
			if (config.getLogSourcesAndSinks()) { 
				if (!app.getCollectedSources().isEmpty()) {
					System.out.println("Collected sources:");
					for (Stmt s : app.getCollectedSources())
						System.out.println("\t" + s);
				}
				if (!app.getCollectedSinks().isEmpty()) {
					System.out.println("Collected sinks:");
					for (Stmt s : app.getCollectedSinks())
						System.out.println("\t" + s);
				}
			}
			
			return res;
		} catch (IOException ex) {
			System.err.println("Could not read file: " + ex.getMessage());
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} catch (XmlPullParserException ex) {
			System.err.println("Could not read Android manifest file: " + ex.getMessage());
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}
	
}
