/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.fapra.labelpropagation;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;


/**
 *
 * @author arm
 */
    
@ServiceProvider(service = StatisticsBuilder.class)
public class LabelPropagationBuilder implements StatisticsBuilder {

    @Override
    public String getName() {
        return "LabelPropagation"; 
    }

    @Override
    public Statistics getStatistics() {
        return new LabelPropagation();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return LabelPropagation.class;
    }

}
