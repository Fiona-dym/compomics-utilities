package com.compomics.util.experiment.biology.aminoacids;

import com.compomics.util.experiment.biology.AminoAcid;

/**
 * Leucine.
 *
 * @author Marc Vaudel
 */
public class Leucine extends AminoAcid {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -656824121858821632L;

    /**
     * Constructor.
     */
    public Leucine() {
        singleLetterCode = "L";
        threeLetterCode = "Leu";
        name = "Leucine";
        averageMass = 113.1576;
        monoisotopicMass = 113.084064;
        subAminoAcidsWithoutCombination = new char[]{'L'};
        subAminoAcidsWithCombination = subAminoAcidsWithoutCombination;
        aminoAcidCombinations = new char[]{'J', 'X'};
        standardGeneticCode = new String[] {"TTA", "TTG", "CTT", "CTC", "CTA", "CTG"};
    }

    @Override
    public boolean iscombination() {
        return false;
    }
}
