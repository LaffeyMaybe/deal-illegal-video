package com.yizhuoyan.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.yizhuoyan.model.TaskWorkCallback;
import com.yizhuoyan.model.TaskWorker;
import com.yizhuoyan.model.TaskWorker.TaskProcessInfo;

public class AppWindow extends JFrame implements ActionListener,TaskWorkCallback {

	final private JFileChooser chooser;
	final private Set<File> selectedFiles = new HashSet<File>(10);

	private JList<Object> fileListView;
	private JProgressBar progressBar;
	private JButton beginBtn;

	public AppWindow() {

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("��Ƶ�ļ�md5ǩ���޸�");
		this.setLocation(100, 100);
		this.setSize(500, 400);
		chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileHidingEnabled(true);
		

		JButton selectFileBtn = new JButton("ѡ����Ƶ�ļ�");
		selectFileBtn.setActionCommand("selectFile");
		selectFileBtn.addActionListener(this);
		this.add(selectFileBtn, BorderLayout.NORTH);

		JPanel fileListPanel = new JPanel();
		fileListPanel.setLayout(new BorderLayout());
		JLabel title = new JLabel("��ѡ���ļ�:");
		fileListPanel.add(title, BorderLayout.NORTH);
		this.fileListView = new JList<Object>();
		this.fileListView.setAutoscrolls(true);
		this.fileListView.setFocusable(false);
		this.fileListView.setRequestFocusEnabled(false);

		fileListPanel.add(fileListView, BorderLayout.CENTER);
		this.add(fileListPanel, BorderLayout.CENTER);

		JPanel actionBar = new JPanel();
		actionBar.setLayout(new BorderLayout());
		this.progressBar = new JProgressBar();
		this.progressBar.setString("����ѡ����Ƶ�ļ�");
		this.progressBar.setStringPainted(true);
		actionBar.add(progressBar);
		beginBtn = new JButton("��ʼ����");
		beginBtn.setActionCommand("begin");
		beginBtn.addActionListener(this);
		actionBar.add(beginBtn, BorderLayout.EAST);

		this.add(actionBar, BorderLayout.SOUTH);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case "selectFile":
			this.selectFile();
			
			break;
		case "begin":
				this.handleFiles();
		default:
			break;
		}
	}

	private void handleFiles() {
		if(this.selectedFiles.size()!=0){
			new TaskWorker(this).execute();
		}else{
			this.progressBar.setString("����ѡ����Ƶ�ļ�");
			this.beginBtn.setText("��ʼ����");
		}
	}

	private void selectFile() {
		int result = chooser.showOpenDialog(this);
		switch (result) {
		case JFileChooser.APPROVE_OPTION:
		case JFileChooser.CANCEL_OPTION:
			this.putFile(chooser.getSelectedFiles());
			break;
		}

	}

	private void putFile(File... files) {
		for (File file : files) {
			this.selectedFiles.add(file);
		}
		this.fileListView.setListData(this.selectedFiles.toArray());
		this.beginBtn.setEnabled(true);
		this.beginBtn.setText("��ʼ����");
		int total=this.selectedFiles.size();
		this.progressBar.setString(total+ "/"+total);
	}

	@Override
	public void begin(TaskProcessInfo info) {
		this.beginBtn.setEnabled(false);
		this.beginBtn.setText("������..");

	}

	@Override
	public void process(List<TaskProcessInfo> infos) {
		for (TaskProcessInfo info : infos) {
			if (info.current == 0) {
				this.progressBar.setMaximum(info.total);
				this.progressBar.setString(info.fileIndex+1 + "/"
						+ info.taskFiles.length);
				
			}else if(info.current==info.total){
				this.selectedFiles.remove(info.taskFiles[info.fileIndex]);
				this.fileListView.setListData(this.selectedFiles.toArray());
			}
			this.progressBar.setValue(info.current);
		}
	}

	@Override
	public void done(TaskProcessInfo info) {
		this.beginBtn.setEnabled(true);
		this.beginBtn.setText("�������!");
	}

	@Override
	public boolean processError(Exception e, TaskProcessInfo info) {
		JDialog dialog=new JDialog(this);
		dialog.setTitle("������");
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setLocationRelativeTo(this);
		JLabel messageUI=new JLabel(e.getMessage());
		dialog.add(messageUI);
		dialog.setVisible(true);
		return true;
	}

	@Override
	public File[] getTaskFiles() {
		final File[] files = this.selectedFiles
				.toArray(new File[this.selectedFiles.size()]);
		return files;
	}
}
