package tool.GUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.table.DefaultTableModel;

import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.button.StandardButtonShaper;
import org.jvnet.substance.painter.StandardGradientPainter;
import org.jvnet.substance.skin.BusinessBlueSteelSkin;
import org.jvnet.substance.skin.OfficeSilver2007Skin;
import org.jvnet.substance.title.FlatTitlePainter;
import org.jvnet.substance.watermark.SubstanceStripeWatermark;
import org.xmlpull.v1.XmlPullParserException;

import tool.entryForAllApks.EntryForAll;

public class userWindowForAll extends JFrame implements ActionListener {
	/*
	 * GUI
	 */
	private static final long serialVersionUID = 1L;
	private JTabbedPane tabbedPane;
	private JLabel labelStaticAnaFinished;
	private JButton buttonStaticAnalyze;
	private JButton buttonChooseFileDirectory;
	private JButton buttonShowApkList;
	private JButton buttonShowResult;
	private JTextField textFieldFileDirectory;
	private JCheckBox checkBoxChooseAll;
	private JPanel panelStaticAnaly, panelResult;
	private JPanel panelP11, panelP12, panelP121, panelP122, panelP123, panelP124, panelP31;
	private JTable tableResults; // Table for showing results
	private JScrollPane scrollPane;
	private JCheckBox[] jcbApkList; // CheckBox for choosing APKs
	private EntryForAll entryForAll; // Analyze Parts
	String FileDirectory;
	ArrayList<String> selectedAPKList; // List of selected APKs
	ArrayList<Integer> selectedAPKIndexList; //
	String Platformpath; // SDK-platform

	public static void main(String[] args) { // Main
		/*
		 * Input Params args[0]: Path of Android platform F://adt-eclipse/sdk/platforms
		 */
		Beautify();
		new userWindowForAll(args[0]);
	}

	public userWindowForAll(String platform) {

		super("SerDroid-A tool for detecting patterns of using service  in Android apps");
		// Platformpath="F://adt-eclipse/sdk/platforms"; // SDK-platform��·��
		Platformpath = platform;
		setBounds(100, 100, 600, 480);
		setFont(new Font("����", Font.BOLD, 40));
		Container container = getContentPane();
		// create tabbedPanel
		tabbedPane = new JTabbedPane();
		// create panel-TabbedPanel 1-Static analysis
		panelStaticAnaly = new JPanel();
		panelResult = new JPanel();
		buttonChooseFileDirectory = new JButton("Choose file directory");
		textFieldFileDirectory = new JTextField(20);
		buttonShowApkList = new JButton("Show the APK list");
		buttonStaticAnalyze = new JButton("Start detection");
		labelStaticAnaFinished = new JLabel("");

		panelP121 = new JPanel();
		panelP121.setLayout(new FlowLayout());
		panelP121.add(buttonChooseFileDirectory);
		panelP121.add(textFieldFileDirectory);
		panelP121.add(buttonShowApkList);

		// show apks
		panelP122 = new JPanel();
		panelP122.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
		panelP122.setLayout(new GridLayout(10, 2));
		jcbApkList = new JCheckBox[500];

		panelP123 = new JPanel();
		panelP123.setLayout(new FlowLayout());
		checkBoxChooseAll = new JCheckBox("Choose all");
		panelP123.add(checkBoxChooseAll);
		panelP123.add(buttonStaticAnalyze);

		panelP124 = new JPanel();
		panelP124.add(labelStaticAnaFinished);

		panelP11 = new JPanel();// 1 center
		panelP12 = new JPanel();// 1 bottom
		panelP11.setLayout(new BorderLayout());
		panelP11.add(panelP121, "North");
		panelP11.add(panelP122, "Center");
		panelP11.add(panelP123, "South");
		panelP12.setLayout(new GridLayout(2, 1));
		panelP12.add(panelP124);

		panelStaticAnaly.setLayout(new BorderLayout());
		panelStaticAnaly.add(panelP11, "Center");
		panelStaticAnaly.add(panelP12, "South");

		// TabbedPanel 3-Show result
		buttonShowResult = new JButton("Show results");
		String[][] row = new String[0][10];
		String[] columnNames = { "App name", "Use Service", "PCBs", "LDBs", "PDBs", "SLBs", "Total" };
		DefaultTableModel tmd = new DefaultTableModel(row, columnNames);
		tableResults = new JTable(tmd);

		scrollPane = new JScrollPane();
		scrollPane.setViewportView(tableResults);
		tableResults.setBackground(Color.white);
		tableResults.setGridColor(Color.DARK_GRAY);
		tableResults.setSelectionBackground(Color.RED);
		tableResults.setSelectionForeground(Color.WHITE);
		panelP31 = new JPanel();
		panelP31.add(buttonShowResult);
		panelResult.setLayout(new BorderLayout());
		panelResult.add(panelP31, "North");
		panelResult.add(scrollPane, "Center");

		// register listener for buttons
		buttonChooseFileDirectory.addActionListener(this);
		buttonShowApkList.addActionListener(this);
		buttonStaticAnalyze.addActionListener(this);
		buttonShowResult.addActionListener(this);
		checkBoxChooseAll.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if (((JCheckBox) e.getSource()).isSelected()) {// choose all
					for (int i = 0; i < jcbApkList.length; i++) {
						if (jcbApkList[i] != null) {// checkbox is not null
							jcbApkList[i].setSelected(true);
						}
					}
				}
				if (!((JCheckBox) e.getSource()).isSelected()) {// undo choose all
					for (int i = 0; i < jcbApkList.length; i++) {
						if (jcbApkList[i] != null) {// checkbox is not null
							jcbApkList[i].setSelected(false);
						}
					}
				}
				getContentPane().validate();
			}
		});
		// add JPanel to JTabbedPanel
		tabbedPane.addTab("Detection", null, panelStaticAnaly, "Open this panel to do detection");
		tabbedPane.addTab("Results", null, panelResult, "Open this panel to show results");
		container.add(tabbedPane);
		setVisible(true);
		// response to exit
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		selectedAPKList = new ArrayList<String>();
		selectedAPKIndexList = new ArrayList<Integer>();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == buttonChooseFileDirectory) {// choose file directory

			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setCurrentDirectory(new File("."));
			int flag = chooser.showDialog(new JLabel(), "Choose");
			if (flag == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				if (file.isDirectory()) {
					FileDirectory = file.getAbsolutePath();
					textFieldFileDirectory.setText(FileDirectory);
				}
			}
		}
		if (e.getSource() == buttonShowApkList) {// Open all APK files under the directory
			if (FileDirectory == null) {
				JOptionPane.showMessageDialog(null, "You do not select any file directory. Please select again.",
						"Promption", JOptionPane.ERROR_MESSAGE);
			} else {
				// String Platformpath="D://adt-eclipse/sdk/platforms";
				String[] args = { FileDirectory, Platformpath };
				entryForAll = new EntryForAll(args);
				ArrayList<String> files = entryForAll.getApkFiles();
				int size = files.size();
				if (size == 0) {
					JOptionPane.showMessageDialog(null, "There is no APK file in this directory. Please select again.",
							"Promption", JOptionPane.ERROR_MESSAGE);
					textFieldFileDirectory.setText("");
				} else {
					String nameString;
					for (int i = 0; i < size; i++) {
						int index = files.get(i).lastIndexOf(".");
						nameString = files.get(i).substring(0, index);
						jcbApkList[i] = new JCheckBox(nameString);
						panelP122.add(jcbApkList[i]);
					}
				}
				getContentPane().validate();
			}
		}

		if (e.getSource() == buttonStaticAnalyze) {// Static analyze
			boolean chooseFlag = false;
			for (int i = 0; i < jcbApkList.length; i++) {
				if (jcbApkList[i] != null && jcbApkList[i].isSelected()) {
					selectedAPKList.add(jcbApkList[i].getText());
					selectedAPKIndexList.add(i);
					chooseFlag = true;
				}
			}
			if (chooseFlag == true) {
				try {
					entryForAll.AnalyzeAll(selectedAPKIndexList);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (XmlPullParserException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} // Analyze all
				labelStaticAnaFinished.setText(
						"It takes " + entryForAll.runningTime + " Seconds to complete detections of Services in "
								+ entryForAll.selectedApkCount + " Android apps");
			} else {
				JOptionPane.showMessageDialog(null, "Please choose APK files.", "Promption", JOptionPane.ERROR_MESSAGE);
			}
		}
		if (e.getSource() == buttonShowResult) {// Show results
			for (int i = 0; i <= selectedAPKList.size(); i++) {//
				if (entryForAll.getResult(i) != null) {
					Object[] rowData = entryForAll.getResult(i);
					((DefaultTableModel) tableResults.getModel()).addRow(rowData);
				}
			}
		}
	}

	public static void Beautify() {

		try {
			UIManager.setLookAndFeel(new SubstanceLookAndFeel());
			JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);
			SubstanceLookAndFeel.setSkin(new OfficeSilver2007Skin());
			SubstanceLookAndFeel.setSkin(new BusinessBlueSteelSkin());

			SubstanceLookAndFeel.setCurrentButtonShaper(new StandardButtonShaper());

			SubstanceLookAndFeel.setCurrentWatermark(new SubstanceStripeWatermark());

			SubstanceLookAndFeel.setCurrentGradientPainter(new StandardGradientPainter());

			SubstanceLookAndFeel.setCurrentTitlePainter(new FlatTitlePainter());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
}
