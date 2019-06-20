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
package com.event.com.test.xmldata;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.xmlpull.v1.XmlPullParser;

import com.event.com.content.res.xmlprinter.AXmlResourceParser;

public class ProcessManifest {

	boolean ActivityExported = false;
	private String applicationName = "";
	private int versionCode = -1;
	private String versionName = "";
	private String packageName = "";
	private int minSdkVersion = -1;
	private int targetSdkVersion = -1;
	public String mainActivity = null;
	public final Set<String> permissions = new HashSet<String>();
	public final Set<String> servicesList = new HashSet<String>();
	public final Set<String> activityList = new HashSet<String>();
	public final Map<String, HashSet<String>> serviceWithAction = new HashMap<String, HashSet<String>>();
	public final Map<String, HashSet<String>> activityWithAction = new HashMap<String, HashSet<String>>();
	public final Map<String, HashSet<String>> receiverWithAction = new HashMap<String, HashSet<String>>();

	private void handleAndroidManifestFile(String apk, IManifestHandler handler) {
		File apkF = new File(apk);
		if (!apkF.exists())
			throw new RuntimeException("file '" + apk + "' does not exist!");

		boolean found = false;
		try {
			ZipFile archive = null;
			try {
				archive = new ZipFile(apkF);
				Enumeration<?> entries = archive.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = (ZipEntry) entries.nextElement();
					String entryName = entry.getName();
					if (entryName.equals("AndroidManifest.xml")) {
						found = true;
						handler.handleManifest(archive.getInputStream(entry));
						break;
					}
				}
			} finally {
				if (archive != null)
					archive.close();
			}
		} catch (Exception e) {
			throw new RuntimeException("Error when looking for manifest in apk: " + e);
		}
		if (!found)
			throw new RuntimeException("No manifest file found in apk");
	}

	public void loadManifestFile(String apk) {
		handleAndroidManifestFile(apk, new IManifestHandler() {
			public void handleManifest(InputStream stream) {
				loadClassesFromBinaryManifest(stream);
			}
		});
	}

	protected void loadClassesFromBinaryManifest(InputStream manifestIS) {
		try {
			AXmlResourceParser parser = new AXmlResourceParser();
			parser.open(manifestIS);
			int type = -1;
			HashSet<String> Action = null;
			String activityName = null;
			String serviceName = null;
			String aliasActivityName = null;
			String receiverName = null;
			while ((type = parser.next()) != XmlPullParser.END_DOCUMENT) {
				switch (type) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					String tagName = parser.getName();
					if ("manifest".equals(tagName)) {
						this.packageName = getAttributeValue(parser, "package");
						String versionCode = getAttributeValue(parser, "versionCode");
						if (versionCode != null && versionCode.length() > 0)
							this.versionCode = Integer.valueOf(versionCode);
						this.versionName = getAttributeValue(parser, "versionName");
					} else if ("activity".equals(tagName)) {
						String flag = getAttributeValue(parser, "enable");
						if (flag != null && flag.equals("false"))
							continue;
						String name = getAttributeValue(parser, "name");
						activityName = expandClassName(name);
						activityList.add(activityName);
						activityWithAction.put(activityName, null);
						String exported = getAttributeValue(parser, "exported");
						if (exported == null || exported == "true") {
							ActivityExported = true;
						} else {
						}
					} else if ("service".equals(tagName)) {
						String flag = getAttributeValue(parser, "enable");
						if (flag != null && flag.equals("false"))
							continue;
						String name = getAttributeValue(parser, "name");
						serviceName = expandClassName(name);
						servicesList.add(serviceName);
						serviceWithAction.put(serviceName, null);

					} else if ("activity-alias".equals(tagName)) {
						String name = getAttributeValue(parser, "targetActivity");
						aliasActivityName = expandClassName(name);
					} else if (tagName.equals("receiver")) {
						String attrValue = getAttributeValue(parser, "enabled");
						if (attrValue != null && attrValue.equals("false"))
							continue;
						attrValue = getAttributeValue(parser, "name");
						receiverName = expandClassName(attrValue);
						receiverWithAction.put(receiverName, null);
					} else if ("intent-filter".equals(tagName)) {
						Action = new HashSet<>();

					} else if ("action".equals(tagName)) {
						String name = getAttributeValue(parser, "name");
						if ("android.intent.action.MAIN".equals(name)) {
							if (mainActivity == null) {
								mainActivity = activityName == null ? aliasActivityName : activityName;
							}
						}
						Action.add(name);
					}
					break;
				case XmlPullParser.END_TAG:
					String endName = parser.getName();
					if ("activity".equals(endName)) { // set ActivityExported false for next activity
						if (activityWithAction.get(activityName) == null)
							activityWithAction.remove(activityName);
						activityName = null;
					}
					else if ("service".equals(endName)) { // set ActivityExported false for next activity
						if (serviceWithAction.get(serviceName) == null)
							serviceWithAction.remove(serviceName);
						serviceName = null;
					}

					else if ("receiver".equals(endName)) {
						if (receiverWithAction.get(receiverName) == null)
							receiverWithAction.remove(receiverName);
						receiverName = null;
					} else if ("intent-filter".equals(endName)) {
						if (receiverName != null) {
							if (receiverWithAction.get(receiverName) == null)
								receiverWithAction.put(receiverName, Action);
							else
								receiverWithAction.get(receiverName).addAll(Action);
						}
						if (activityName != null) {
							if (activityWithAction.get(activityName) == null)
								activityWithAction.put(activityName, Action);
							else
								activityWithAction.get(activityName).addAll(Action);
						}
						if (serviceName != null) {
							if (serviceWithAction.get(serviceName) == null)
								serviceWithAction.put(serviceName, Action);
							else
								serviceWithAction.get(serviceName).addAll(Action);
						}
					}

					break;
				case XmlPullParser.TEXT:
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generates a full class name from a short class name by appending the
	 * globally-defined package when necessary
	 * 
	 * @param className
	 *            The class name to expand
	 * @return The expanded class name for the given short name
	 */
	private String expandClassName(String className) {
		if (className.startsWith("."))
			return this.packageName + className;
		else if (className.substring(0, 1).equals(className.substring(0, 1).toUpperCase()))
			return this.packageName + "." + className;
		else
			return className;
	}

	private String getAttributeValue(AXmlResourceParser parser, String attributeName) {// get attribute value
		for (int i = 0; i < parser.getAttributeCount(); i++)
			if (parser.getAttributeName(i).equals(attributeName))
				return parser.getAttributeValue(i);
		return null;
	}

	public void setApplicationName(String name) {
		this.applicationName = name;
	}

	public void setPackageName(String name) {
		this.packageName = name;
	}

	public String getApplicationName() {
		return this.applicationName;
	}

	public Set<String> getPermissions() {
		return this.permissions;
	}

	public int getVersionCode() {
		return this.versionCode;
	}

	public String getVersionName() {
		return this.versionName;
	}

	public String getPackageName() {
		return this.packageName;
	}

	public int getMinSdkVersion() {
		return this.minSdkVersion;
	}

	public int targetSdkVersion() {
		return this.targetSdkVersion;
	}

	public String getMainActivity() {
		return mainActivity;
	}

}