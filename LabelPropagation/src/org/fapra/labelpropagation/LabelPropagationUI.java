/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fapra.labelpropagation;

import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author arm
 */
@ServiceProvider(service = StatisticsUI.class)
public class LabelPropagationUI implements StatisticsUI{

    @Override
    public JPanel getSettingsPanel() {
        return null;
    }

    @Override
    public void setup(Statistics ststcs) {
    }

    @Override
    public void unsetup() {
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return LabelPropagation.class;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Label Propagation";
    }

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    @Override
    public int getPosition() {
        return 5000;
    }

    @Override
    public String getShortDescription() {
        return null;
    }
    
}
