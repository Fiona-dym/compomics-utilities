/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.compomics.util.math.statistics.ditributions;

import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.math.statistics.Distribution;
import java.util.ArrayList;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistributionImpl;

/**
 * This class represtents a normal distribution. A dirac if the standard deviation is null.
 *
 * @author Marc
 */
public class NormalDistribution implements Distribution {
 
    /**
     * The apache normal distribution implementation
     */
    private NormalDistributionImpl normalDistributionImpl;
    /**
     * The mean of the distribution
     */
    private double mean;
    /**
     * The standard deviation of the distribution
     */
    private double std;
    /**
     * Constructor
     * 
     * @param mean the mean
     * @param std the standard deviation
     */
    public NormalDistribution(double mean, double std) {
        this.mean = mean;
        this.std = std;
        if (std > 0) {
        this.normalDistributionImpl = new NormalDistributionImpl(mean, std);
        }
    }
    
    /**
     * Returns the normal distribution corresponding to a given list of double calibrated on mean and standard deviation.
     * 
     * @param input the input as a list of double
     * 
     * @return the normal distribution calibrated on the mean and standard deviation
     */
    public static NormalDistribution getNormalDistribution(ArrayList<Double> input) {
        return new NormalDistribution(BasicMathFunctions.mean(input), BasicMathFunctions.std(input));
    }
    
    /**
     * Returns the normal distribution corresponding to a given list of double calibrated on median and 34.1% percentile to median distance
     * 
     * @param input the input as list of double
     * 
     * @return a normal distribution calibrated on median and 34.1% percentile to median distance
     */
    public static NormalDistribution getRobustNormalDistribution(ArrayList<Double> input) {
        double std = (BasicMathFunctions.percentile(input, 0.841) - BasicMathFunctions.percentile(input, 0.159))/2;
        return new NormalDistribution(BasicMathFunctions.median(input), std);
    }

    @Override
    public Double getProbabilityAt(double x) {
        if (std == 0) {
            if (x == mean) {
                return 1.0;
            } else {
                return 0.0;
            }
        }
        return Math.pow(Math.E, -Math.pow(x-mean, 2)/(2*Math.pow(std, 2)))/(std*Math.pow(2*Math.PI, 0.5));
    }

    @Override
    public Double getMaxValueForProbability(double p) {
        if (std == 0) {
            return mean;
        }
        return mean + Math.pow(-2*Math.pow(std, 2)*Math.log(std*p*Math.pow(2*Math.PI, 0.5)), 0.5);
    }

    @Override
    public Double getMinValueForProbability(double p) {
        if (std == 0) {
            return mean;
        }
        return mean - Math.pow(-2*Math.pow(std, 2)*Math.log(std*p*Math.pow(2*Math.PI, 0.5)), 0.5);
    }

    @Override
    public Double getCumulativeProbabilityAt(double x) throws MathException {
        if (std == 0) {
            // Note: this is my personal interpretation of the cumulative distribution in this case
            if (x < mean) {
                return 0.0;
            } else if (x == mean) {
                return 0.5;
            } else {
                return 1.0;
            }
        }
        return normalDistributionImpl.cumulativeProbability(x);
    }

    @Override
    public Double getValueAtCumulativeProbability(double p) throws MathException {
        if (std == 0) {
            // Note: this is my personal interpretation of the cumulative distribution in this case
            if (p < 0.5) {
                return -Double.MAX_VALUE;
            } else if (p== 0.5) {
                return mean;
            } else {
                return Double.MAX_VALUE;
            }
        }
        return normalDistributionImpl.inverseCumulativeProbability(p);
    }

    @Override
    public Double getDescendingCumulativeProbabilityAt(double x) throws MathException {
        if (std == 0) {
            // Note: this is my personal interpretation of the cumulative distribution in this case
            if (x > mean) {
                return 0.0;
            } else if (x == mean) {
                return 0.5;
            } else {
                return 1.0;
            }
        }
        return getCumulativeProbabilityAt(x);
    }

    @Override
    public Double getValueAtDescendingCumulativeProbability(double p) throws MathException {
        if (std == 0) {
            // Note: this is my personal interpretation of the cumulative distribution in this case
            if (p < 0.5) {
                return Double.MAX_VALUE;
            } else if (p== 0.5) {
                return mean;
            } else {
                return -Double.MAX_VALUE;
            }
        }
        return getValueAtCumulativeProbability(p);
    }
    
}