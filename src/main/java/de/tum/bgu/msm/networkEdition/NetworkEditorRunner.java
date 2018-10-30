package de.tum.bgu.msm.networkEdition;

import org.matsim.contrib.networkEditor.run.RunNetworkEditor;

import java.awt.*;

public class NetworkEditorRunner {

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                RunNetworkEditor vis = new RunNetworkEditor();
                vis.setVisible(true);
            }
        });
    }

}
