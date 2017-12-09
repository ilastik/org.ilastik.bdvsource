/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ilastik.bdvsource;

import com.google.gson.Gson;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import net.imagej.DatasetService;
import net.imagej.ImageJ;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.options.OptionsService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author chaubold
 */
@Plugin(type = Command.class, headless = false, menuPath = "Plugins>ilastik>Stream to BDV")
public class IlastikToBdvStreamer implements Command, ActionListener {

    // needed services:
    @Parameter
    LogService log;

    @Parameter
    DatasetService datasetService;
    
    @Parameter
    OptionsService optionsService;

    // plugin parameters
    @Parameter(label = "base URL of the ilastik server",
               description="e.g. http://localhost:5000 where the ilastik server is running")
    private String ilastikServerURL = "http://localhost:5000";
    
    // private variables
    private String selectedDataset;
    private JComboBox dataSetBox;
    private JFrame frameSelectDataset;
    
    private final Lock lock = new ReentrantLock();
    private final Condition finishedCondition = lock.newCondition();
    private boolean isFinished = false;
    
    @Override
    public void run() {
        // retrieve dataset list from server
        HttpRequest projectListRequest = new HttpRequest(ilastikServerURL + "/api/project/project-list");
        String jsonProjectList = projectListRequest.get();
        Gson gson = new Gson();
        ProjectList projectList = gson.fromJson(jsonProjectList, ProjectList.class);
        
        for(String s : projectList.projects){
            log.info(s);
        }
        
        showDatasetSelectionDialog(projectList.projects);
        
        // wait for isFinished to become true
        lock.lock();
        try {
            while(!isFinished)
                finishedCondition.await();
        } catch (InterruptedException ex) {
            log.warn("ilastik Dataset selection got interrupted");
        } finally {
            lock.unlock();
        }
        
        log.info("Selected ilastik dataset, starting BigDataViewer...");
        
        // then start BdvIlastikFrontend
        new BdvIlastikFrontend(ilastikServerURL, selectedDataset).run();
    }
    
    private void showDatasetSelectionDialog(String[] projectList) {
        frameSelectDataset = new JFrame();
        frameSelectDataset.setTitle("Select dataset on ilastik server");
        JButton b1 = new JButton("Select");
        b1.setActionCommand("selectDataset");
        b1.addActionListener(this);
        JButton b2 = new JButton("Cancel");
        b2.setActionCommand("cancelDatasetSelection");
        b2.addActionListener(this);

        dataSetBox = new JComboBox(projectList);
        dataSetBox.addActionListener(this);

        frameSelectDataset.getContentPane().add(dataSetBox, BorderLayout.PAGE_START);
        frameSelectDataset.getContentPane().add(b1, BorderLayout.LINE_START);
        frameSelectDataset.getContentPane().add(b2, BorderLayout.LINE_END);
        frameSelectDataset.setResizable(false);
        frameSelectDataset.setLocationRelativeTo(null);
        frameSelectDataset.pack();
        frameSelectDataset.setVisible(true);
    }
    
    private void signalFinished(){
        lock.lock();
        try{
            isFinished = true;
            finishedCondition.signal();
        } finally
        {
            lock.unlock();
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("selectDataset")) {
            selectedDataset = (String)dataSetBox.getSelectedItem();
            frameSelectDataset.dispose();
            signalFinished();
        } else if (event.getActionCommand().equals("cancelDatasetSelection")) {
            frameSelectDataset.dispose();
            signalFinished();
        }
    }
    
    // ------------------------------------------------------------------------------------
    // Main method to test this plugin
    // ------------------------------------------------------------------------------------
    public static void main(String[] args) {
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();
        ij.command().run(IlastikToBdvStreamer.class, true);
    }
    
}
