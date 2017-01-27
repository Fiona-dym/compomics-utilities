package com.compomics.util.experiment.identification.identification_parameters;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.AndromedaParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.CometParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.DirecTagParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.MsAmandaParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.MsgfParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.MyriMatchParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.NovorParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.OmssaParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.PNovoParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.PepnovoParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.TideParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.XtandemParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.io.SerializationUtils;
import com.compomics.util.io.json.marshallers.IdentificationParametersMarshaller;
import com.compomics.util.preferences.DigestionPreferences;
import com.compomics.util.preferences.DummyParameters;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.preferences.MarshallableParameter;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import no.uib.jsparklines.data.XYDataPoint;

/**
 * This class groups the parameters used for identification.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class SearchParameters implements Serializable, MarshallableParameter {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -2773993307168773763L;
    /**
     * Name of the type of marshalled parameter.
     */
    private String marshallableParameterType = null;

    /**
     * Possible mass accuracy types.
     */
    public enum MassAccuracyType {

        PPM, DA;

        @Override
        public String toString() {
            switch (this) {
                case PPM:
                    return "ppm";
                case DA:
                    return "Da";
                default:
                    throw new UnsupportedOperationException("Name of tolerance type " + this.name() + " not implemented.");
            }
        }
    };
    /**
     * Convenience array for forward ion type selection.
     */
    public static final String[] implementedForwardIons = {"a", "b", "c"};
    /**
     * Convenience array for rewind ion type selection.
     */
    public static final String[] implementedRewindIons = {"x", "y", "z"};
    /**
     * The precursor accuracy type. Default is ppm.
     */
    private MassAccuracyType precursorAccuracyType = MassAccuracyType.PPM;
    /**
     * The fragment accuracy type. Default is Da.
     */
    private MassAccuracyType fragmentAccuracyType = MassAccuracyType.DA;
    /**
     * The precursor mass tolerance.
     */
    private Double precursorTolerance = 10.0;
    /**
     * The precursor mass tolerance in Dalton.
     */
    private Double precursorToleranceDalton = 0.5;
    /**
     * The MS2 ion tolerance.
     */
    private Double fragmentIonMZTolerance = 0.5;
    /**
     * The expected modifications. Modified peptides will be grouped and
     * displayed according to this classification.
     */
    private PtmSettings ptmSettings = new PtmSettings();
    /**
     * The enzyme used for digestion.
     *
     * @deprecated use the Digestion preferences instead.
     */
    private Enzyme enzyme;
    /**
     * The allowed number of missed cleavages.
     *
     * @deprecated use the Digestion preferences instead.
     */
    private Integer nMissedCleavages = 2;
    /**
     * The digestion preferences.
     */
    private DigestionPreferences digestionPreferences;
    /**
     * The sequence database file used for identification.
     */
    private File fastaFile;
    /**
     * The corresponding file.
     *
     * @deprecated the file is now handled outside the parameters.
     */
    private File parametersFile;
    /**
     * The list of fraction molecular weights. The key is the fraction file
     * path.
     *
     * @deprecated moved to the FractionSettings
     */
    private HashMap<String, XYDataPoint> fractionMolecularWeightRanges;
    /**
     * The forward ions to consider (a, b or c).
     *
     * @deprecated use the list allowing multiple ions instead.
     */
    private Integer forwardIon;
    /**
     * The forward ions to consider (a, b or c).
     */
    private ArrayList<Integer> forwardIons;
    /**
     * The rewind ions to consider (x, y or z).
     *
     * @deprecated use the list allowing multiple ions instead.
     */
    private Integer rewindIon;
    /**
     * The rewind ions to consider (x, y or z).
     */
    private ArrayList<Integer> rewindIons;
    /**
     * The minimal charge searched (in absolute value).
     */
    private Charge minChargeSearched = new Charge(Charge.PLUS, 2);
    /**
     * The minimal charge searched (in absolute value).
     */
    private Charge maxChargeSearched = new Charge(Charge.PLUS, 4);
    /**
     * The minimal isotope correction.
     */
    private Integer minIsotopicCorrection = 0;
    /**
     * The maximal isotope correction.
     */
    private Integer maxIsotopicCorrection = 1;
    /**
     * Reference mass for the conversion of the fragment ion tolerance from ppm
     * to Dalton.
     */
    private Double refMass = 2000.0;
    /**
     * The algorithm specific parameters.
     */
    private HashMap<Integer, IdentificationAlgorithmParameter> algorithmParameters;

    /**
     * Constructor.
     */
    public SearchParameters() {

        // Set ions to be searched by default
        forwardIons = new ArrayList<Integer>(1);
        forwardIons.add(PeptideFragmentIon.B_ION);
        rewindIons = new ArrayList<Integer>(1);
        rewindIons.add(PeptideFragmentIon.Y_ION);

        // Set advanced parameters
        setDefaultAdvancedSettings();
    }

    /**
     * Constructor.
     *
     * @param searchParameters the search parameter to base the search
     * parameters on.
     */
    public SearchParameters(SearchParameters searchParameters) {

        // Set values from the given parameters
        this.precursorAccuracyType = searchParameters.getPrecursorAccuracyType();
        this.fragmentAccuracyType = searchParameters.getFragmentAccuracyType();
        this.precursorTolerance = searchParameters.getPrecursorAccuracy();
        this.precursorToleranceDalton = searchParameters.getPrecursorAccuracyDalton();
        this.fragmentIonMZTolerance = searchParameters.getFragmentIonAccuracy();
        this.ptmSettings = new PtmSettings(searchParameters.getPtmSettings());
        this.digestionPreferences = searchParameters.getDigestionPreferences();
        this.fastaFile = searchParameters.getFastaFile();
        this.forwardIons = new ArrayList<Integer>(searchParameters.getForwardIons());
        this.rewindIons = new ArrayList<Integer>(searchParameters.getRewindIons());
        this.minChargeSearched = searchParameters.getMinChargeSearched();
        this.maxChargeSearched = searchParameters.getMaxChargeSearched();
        this.minIsotopicCorrection = searchParameters.getMinIsotopicCorrection();
        this.maxIsotopicCorrection = searchParameters.getMaxIsotopicCorrection();
        this.refMass = searchParameters.getRefMass();

        // Set advanced parameters
        setDefaultAdvancedSettings(searchParameters);
    }

    /**
     * Set the advanced settings to the default values.
     */
    public void setDefaultAdvancedSettings() {
        setDefaultAdvancedSettings(null);
    }

    /**
     * Set the advanced settings to the values in the given search parameters
     * object or to the default values of the advanced settings are not set for
     * a given advocate.
     *
     * @param searchParameters the search parameter to extract the advanced
     * settings from
     */
    public void setDefaultAdvancedSettings(SearchParameters searchParameters) {

        if (searchParameters == null || searchParameters.getIdentificationAlgorithmParameter(Advocate.omssa.getIndex()) == null) {
            setIdentificationAlgorithmParameter(Advocate.omssa.getIndex(), new OmssaParameters());
        } else {
            setIdentificationAlgorithmParameter(Advocate.omssa.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.omssa.getIndex()));
        }

        if (searchParameters == null || searchParameters.getIdentificationAlgorithmParameter(Advocate.xtandem.getIndex()) == null) {
            setIdentificationAlgorithmParameter(Advocate.xtandem.getIndex(), new XtandemParameters());
        } else {
            setIdentificationAlgorithmParameter(Advocate.xtandem.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.xtandem.getIndex()));
        }

        if (searchParameters == null || searchParameters.getIdentificationAlgorithmParameter(Advocate.msgf.getIndex()) == null) {
            setIdentificationAlgorithmParameter(Advocate.msgf.getIndex(), new MsgfParameters());
        } else {
            setIdentificationAlgorithmParameter(Advocate.msgf.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.msgf.getIndex()));
        }

        if (searchParameters == null || searchParameters.getIdentificationAlgorithmParameter(Advocate.msAmanda.getIndex()) == null) {
            setIdentificationAlgorithmParameter(Advocate.msAmanda.getIndex(), new MsAmandaParameters());
        } else {
            setIdentificationAlgorithmParameter(Advocate.msAmanda.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.msAmanda.getIndex()));
        }

        if (searchParameters == null || searchParameters.getIdentificationAlgorithmParameter(Advocate.myriMatch.getIndex()) == null) {
            setIdentificationAlgorithmParameter(Advocate.myriMatch.getIndex(), new MyriMatchParameters());
        } else {
            setIdentificationAlgorithmParameter(Advocate.myriMatch.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.myriMatch.getIndex()));
        }

        if (searchParameters == null || searchParameters.getIdentificationAlgorithmParameter(Advocate.comet.getIndex()) == null) {
            setIdentificationAlgorithmParameter(Advocate.comet.getIndex(), new CometParameters());
        } else {
            setIdentificationAlgorithmParameter(Advocate.comet.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.comet.getIndex()));
        }

        if (searchParameters == null || searchParameters.getIdentificationAlgorithmParameter(Advocate.tide.getIndex()) == null) {
            setIdentificationAlgorithmParameter(Advocate.tide.getIndex(), new TideParameters());
        } else {
            setIdentificationAlgorithmParameter(Advocate.tide.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.tide.getIndex()));
        }
        if (searchParameters == null || searchParameters.getIdentificationAlgorithmParameter(Advocate.andromeda.getIndex()) == null) {
            setIdentificationAlgorithmParameter(Advocate.andromeda.getIndex(), new AndromedaParameters());
        } else {
            setIdentificationAlgorithmParameter(Advocate.andromeda.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.andromeda.getIndex()));
        }

        if (searchParameters == null || searchParameters.getIdentificationAlgorithmParameter(Advocate.pepnovo.getIndex()) == null) {
            setIdentificationAlgorithmParameter(Advocate.pepnovo.getIndex(), new PepnovoParameters());
        } else {
            setIdentificationAlgorithmParameter(Advocate.pepnovo.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.pepnovo.getIndex()));
        }

        if (searchParameters == null || searchParameters.getIdentificationAlgorithmParameter(Advocate.direcTag.getIndex()) == null) {
            setIdentificationAlgorithmParameter(Advocate.direcTag.getIndex(), new DirecTagParameters());
        } else {
            setIdentificationAlgorithmParameter(Advocate.direcTag.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.direcTag.getIndex()));
        }

        if (searchParameters == null || searchParameters.getIdentificationAlgorithmParameter(Advocate.pNovo.getIndex()) == null) {
            setIdentificationAlgorithmParameter(Advocate.pNovo.getIndex(), new PNovoParameters());
        } else {
            setIdentificationAlgorithmParameter(Advocate.pNovo.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.pNovo.getIndex()));
        }

        if (searchParameters == null || searchParameters.getIdentificationAlgorithmParameter(Advocate.novor.getIndex()) == null) {
            setIdentificationAlgorithmParameter(Advocate.novor.getIndex(), new NovorParameters());
        } else {
            setIdentificationAlgorithmParameter(Advocate.novor.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.novor.getIndex()));
        }
    }

    /**
     * Returns the reference mass used to convert ppm to Da.
     * 
     * @return the reference mass used to convert ppm to Da
     */
    public Double getRefMass() {
        if (refMass == null) { // Backward compatibility
            refMass = 2000.0;
        }
        return refMass; 
    }

    /**
     * Sets the reference mass used to convert ppm to Da.
     * 
     * @param refMass the reference mass used to convert ppm to Da
     */
    public void setRefMass(Double refMass) {
        this.refMass = refMass;
    }

    /**
     * Returns the PTM settings.
     *
     * @return the PTM settings
     */
    public PtmSettings getPtmSettings() {
        return ptmSettings;
    }

    /**
     * Sets the PTM settings.
     *
     * @param ptmSettings the PTM settings
     */
    public void setPtmSettings(PtmSettings ptmSettings) {
        this.ptmSettings = ptmSettings;
    }

    /**
     * Returns the MS2 ion m/z tolerance.
     *
     * @return the MS2 ion m/z tolerance
     */
    public Double getFragmentIonAccuracy() {
        return fragmentIonMZTolerance;
    }

    /**
     * Returns the absolute fragment ion tolerance in Dalton. If the tolerance
     * is in ppm, the internal reference mass is used.
     *
     * @return the absolute fragment ion tolerance in Dalton
     */
    public Double getFragmentIonAccuracyInDaltons() {
        return getFragmentIonAccuracyInDaltons(refMass);
    }

    /**
     * Returns the absolute fragment ion tolerance in Dalton. If the tolerance
     * is in ppm, the given reference mass is used.
     *
     * @param refMass the reference mass to use for the conversion of tolerances
     * in ppm.
     *
     * @return the absolute fragment ion tolerance in Dalton
     */
    public Double getFragmentIonAccuracyInDaltons(Double refMass) {
        switch (fragmentAccuracyType) {
            case DA:
                return fragmentIonMZTolerance;
            case PPM:
                return fragmentIonMZTolerance * refMass / 1000000;
            default:
                throw new UnsupportedOperationException("Tolerance in " + fragmentAccuracyType + " not implemented.");
        }
    }

    /**
     * Sets the fragment ion m/z tolerance.
     *
     * @param fragmentIonMZTolerance the fragment ion m/z tolerance
     */
    public void setFragmentIonAccuracy(Double fragmentIonMZTolerance) {
        this.fragmentIonMZTolerance = fragmentIonMZTolerance;
    }

    /**
     * Returns the enzyme used for digestion.
     *
     * @return the enzyme used for digestion
     *
     * @deprecated use the PTM Digestion preferences instead.
     */
    public Enzyme getEnzyme() {
        return enzyme;
    }

    /**
     * Returns the digestion preferences.
     *
     * @return the digestion preferences
     */
    public DigestionPreferences getDigestionPreferences() {
        if (digestionPreferences == null && enzyme != null) { // Backward compatibility check
            enzyme.backwardCompatibilityFix();
            digestionPreferences = new DigestionPreferences();
            if (enzyme.isWholeProtein()) {
                digestionPreferences.setCleavagePreference(DigestionPreferences.CleavagePreference.wholeProtein);
            } else if (enzyme.isUnspecific()) {
                digestionPreferences.setCleavagePreference(DigestionPreferences.CleavagePreference.unSpecific);
            } else {
                digestionPreferences.setCleavagePreference(DigestionPreferences.CleavagePreference.enzyme);
                digestionPreferences.addEnzyme(enzyme);
                String enzymeName = enzyme.getName();
                if (enzyme.isSemiSpecific()) {
                    digestionPreferences.setSpecificity(enzymeName, DigestionPreferences.Specificity.semiSpecific);
                } else {
                    digestionPreferences.setSpecificity(enzymeName, DigestionPreferences.Specificity.specific);
                }
                digestionPreferences.setnMissedCleavages(enzymeName, nMissedCleavages);
            }
        }
        return digestionPreferences;
    }

    /**
     * Sets the digestion preferences.
     *
     * @param digestionPreferences the digestion preferences
     */
    public void setDigestionPreferences(DigestionPreferences digestionPreferences) {
        this.digestionPreferences = digestionPreferences;
    }

    /**
     * Returns the sequence database file used for identification.
     *
     * @return the sequence database file used for identification
     */
    public File getFastaFile() {
        return fastaFile;
    }

    /**
     * Sets the sequence database file used for identification.
     *
     * @param fastaFile the sequence database file used for identification
     */
    public void setFastaFile(File fastaFile) {
        this.fastaFile = fastaFile;
    }

    /**
     * Returns the allowed number of missed cleavages.
     *
     * @deprecated use the Digestion preferences instead.
     *
     * @return the allowed number of missed cleavages
     */
    public Integer getnMissedCleavages() {
        return nMissedCleavages;
    }

    /**
     * Returns the forward ions searched as list of integers as indexed in the
     * FragmentIon class.
     *
     * @return the forward ions searched
     */
    public ArrayList<Integer> getForwardIons() {
        if (forwardIons == null) { // Backward compatibility
            forwardIons = new ArrayList<Integer>(1);
            forwardIons.add(forwardIon);
        }
        return forwardIons;
    }

    /**
     * Sets the forward ions searched as list of integers as indexed in the
     * FragmentIon class.
     *
     * @param forwardIons the forward ions searched
     */
    public void setForwardIons(ArrayList<Integer> forwardIons) {
        this.forwardIons = forwardIons;
    }

    /**
     * Returns the rewind ions searched as list of integers as indexed in the
     * FragmentIon class.
     *
     * @return the rewind ions searched
     */
    public ArrayList<Integer> getRewindIons() {
        if (rewindIons == null) { // Backward compatibility
            rewindIons = new ArrayList<Integer>(1);
            rewindIons.add(rewindIon);
        }
        return rewindIons;
    }

    /**
     * Sets the rewind ions searched as list of integers as indexed in the
     * FragmentIon class.
     *
     * @param rewindIons the rewind ions searched
     */
    public void setRewindIons(ArrayList<Integer> rewindIons) {
        this.rewindIons = rewindIons;
    }

    /**
     * Getter for the list of ion symbols used.
     *
     * @return the list of ion symbols used
     */
    public static String[] getIons() {
        String[] ions = new String[implementedForwardIons.length + implementedRewindIons.length];
        for (String forwardIon1 : implementedForwardIons) {
            ions[ions.length] = forwardIon1;
        }
        for (String rewindIon1 : implementedRewindIons) {
            ions[ions.length] = rewindIon1;
        }
        return ions;
    }

    /**
     * Returns the precursor tolerance.
     *
     * @return the precursor tolerance
     */
    public Double getPrecursorAccuracy() {
        return precursorTolerance;
    }

    /**
     * Sets the precursor tolerance.
     *
     * @param precursorTolerance the precursor tolerance
     */
    public void setPrecursorAccuracy(Double precursorTolerance) {
        this.precursorTolerance = precursorTolerance;
    }

    /**
     * Returns the precursor tolerance in Dalton (for de novo searches).
     *
     * @return the precursor tolerance
     */
    public Double getPrecursorAccuracyDalton() {
        return precursorToleranceDalton;
    }

    /**
     * Sets the precursor tolerance in Dalton (for de novo searches).
     *
     * @param precursorToleranceDalton the precursor tolerance
     */
    public void setPrecursorAccuracyDalton(Double precursorToleranceDalton) {
        this.precursorToleranceDalton = precursorToleranceDalton;
    }

    /**
     * Returns the precursor accuracy type.
     *
     * @return the precursor accuracy type
     */
    public MassAccuracyType getPrecursorAccuracyType() {
        return precursorAccuracyType;
    }

    /**
     * Sets the precursor accuracy type.
     *
     * @param precursorAccuracyType the precursor accuracy type
     */
    public void setPrecursorAccuracyType(MassAccuracyType precursorAccuracyType) {
        this.precursorAccuracyType = precursorAccuracyType;
    }

    /**
     * Returns the fragment accuracy type.
     *
     * @return the fragment accuracy type
     */
    public MassAccuracyType getFragmentAccuracyType() {
        return fragmentAccuracyType;
    }

    /**
     * Sets the fragment accuracy type.
     *
     * @param fragmentAccuracyType the fragment accuracy type
     */
    public void setFragmentAccuracyType(MassAccuracyType fragmentAccuracyType) {
        this.fragmentAccuracyType = fragmentAccuracyType;
    }

    /**
     * Returns true if the current precursor accuracy type is ppm.
     *
     * @return true if the current precursor accuracy type is ppm
     */
    public Boolean isPrecursorAccuracyTypePpm() {
        return getPrecursorAccuracyType() == MassAccuracyType.PPM;
    }

    /**
     * Returns the maximal charge searched.
     *
     * @return the maximal charge searched
     */
    public Charge getMaxChargeSearched() {
        return maxChargeSearched;
    }

    /**
     * Sets the maximal charge searched.
     *
     * @param maxChargeSearched the maximal charge searched
     */
    public void setMaxChargeSearched(Charge maxChargeSearched) {
        this.maxChargeSearched = maxChargeSearched;
    }

    /**
     * Returns the minimal charge searched.
     *
     * @return the minimal charge searched
     */
    public Charge getMinChargeSearched() {
        return minChargeSearched;
    }

    /**
     * Sets the minimal charge searched.
     *
     * @param minChargeSearched the minimal charge searched
     */
    public void setMinChargeSearched(Charge minChargeSearched) {
        this.minChargeSearched = minChargeSearched;
    }

    /**
     * Returns the algorithm specific parameters in a map: algorithm as indexed
     * in the Advocate class &gt; parameters. null if not set.
     *
     * @return the algorithm specific parameters in a map
     */
    public HashMap<Integer, IdentificationAlgorithmParameter> getAlgorithmSpecificParameters() {
        return algorithmParameters;
    }

    /**
     * Returns the algorithm specific parameters, null if not found.
     *
     * @param algorithmID the index of the search engine as indexed in the
     * Advocate class
     *
     * @return the algorithm specific parameters
     */
    public IdentificationAlgorithmParameter getIdentificationAlgorithmParameter(int algorithmID) {
        if (algorithmParameters == null) {
            return null;
        }
        return algorithmParameters.get(algorithmID);
    }

    /**
     * Adds identification algorithm specific parameters.
     *
     * @param algorithmID the algorithm id as indexed in the Advocate class
     *
     * @param identificationAlgorithmParameter the specific parameters
     */
    public void setIdentificationAlgorithmParameter(int algorithmID, IdentificationAlgorithmParameter identificationAlgorithmParameter) {
        if (algorithmParameters == null) {
            algorithmParameters = new HashMap<Integer, IdentificationAlgorithmParameter>();
        }
        algorithmParameters.put(algorithmID, identificationAlgorithmParameter);
    }

    /**
     * Returns the algorithms for which specific parameters are stored. Warning:
     * this does not mean that the algorithm was actually used.
     *
     * @return the algorithms for which specific parameters are stored in a set
     * of indexes as listed in the Advocate class
     */
    public Set<Integer> getAlgorithms() {
        if (algorithmParameters == null) {
            return new HashSet<Integer>();
        }
        return algorithmParameters.keySet();
    }

    /**
     * Returns the minimal isotopic correction.
     *
     * @return the minimal isotopic correction
     */
    public Integer getMinIsotopicCorrection() {
        if (minIsotopicCorrection == null) {
            minIsotopicCorrection = 0;
        }
        return minIsotopicCorrection;
    }

    /**
     * Sets the minimal isotopic correction.
     *
     * @param minIsotopicCorrection the minimal isotopic correction
     */
    public void setMinIsotopicCorrection(Integer minIsotopicCorrection) {
        this.minIsotopicCorrection = minIsotopicCorrection;
    }

    /**
     * Returns the maximal isotopic correction.
     *
     * @return the maximal isotopic correction
     */
    public Integer getMaxIsotopicCorrection() {
        if (maxIsotopicCorrection == null) {
            maxIsotopicCorrection = 1;
        }
        return maxIsotopicCorrection;
    }

    /**
     * Sets the maximal isotopic correction.
     *
     * @param maxIsotopicCorrection the maximal isotopic correction
     */
    public void setMaxIsotopicCorrection(Integer maxIsotopicCorrection) {
        this.maxIsotopicCorrection = maxIsotopicCorrection;
    }

    /**
     * Loads the identification parameters from a file. If the file is an
     * identification parameters file, the search parameters are extracted.
     *
     * @param searchParametersFile the search parameter file
     *
     * @return the search parameters
     *
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     */
    public static SearchParameters getIdentificationParameters(File searchParametersFile) throws IOException, ClassNotFoundException {

        Object savedObject;

        try {

            // Try as json file
            IdentificationParametersMarshaller jsonMarshaller = new IdentificationParametersMarshaller();
            Class expectedObjectType = DummyParameters.class;
            Object object = jsonMarshaller.fromJson(expectedObjectType, searchParametersFile);
            DummyParameters dummyParameters = (DummyParameters) object;
            if (dummyParameters.getType() == MarshallableParameter.Type.search_parameters) {
                expectedObjectType = SearchParameters.class;
                savedObject = jsonMarshaller.fromJson(expectedObjectType, searchParametersFile);
            } else if (dummyParameters.getType() == MarshallableParameter.Type.identification_parameters) {
                expectedObjectType = IdentificationParameters.class;
                savedObject = jsonMarshaller.fromJson(expectedObjectType, searchParametersFile);
            } else {
                throw new IllegalArgumentException("Parameters file " + searchParametersFile + " not recognized.");
            }

        } catch (Exception e1) {

            try {
                // Try serialized java object
                savedObject = SerializationUtils.readObject(searchParametersFile);

            } catch (Exception e2) {
                e1.printStackTrace();
                e2.printStackTrace();
                throw new IllegalArgumentException("Parameters file " + searchParametersFile + " not recognized.");
            }
        }

        SearchParameters searchParameters;
        if (savedObject instanceof SearchParameters) {
            searchParameters = (SearchParameters) savedObject;
        } else if (savedObject instanceof IdentificationParameters) {
            IdentificationParameters identificationParameters = (IdentificationParameters) savedObject;
            searchParameters = identificationParameters.getSearchParameters();
        } else {
            throw new UnsupportedOperationException("Parameters of type " + savedObject.getClass() + " not supported.");
        }

        return searchParameters;
    }

    /**
     * Saves the identification parameters to a serialized file.
     *
     * @param searchParameters the identification parameters
     * @param searchParametersFile the file
     *
     * @throws IOException if an IOException occurs
     */
    public static void saveIdentificationParameters(SearchParameters searchParameters, File searchParametersFile) throws IOException {
        IdentificationParametersMarshaller jsonMarshaller = new IdentificationParametersMarshaller();
        searchParameters.setType();
        jsonMarshaller.saveObjectToJson(searchParameters, searchParametersFile);
    }

    /**
     * Saves the identification parameters as a human readable text file.
     *
     * @param file the file
     *
     * @throws IOException if an IOException occurs
     */
    public void saveIdentificationParametersAsTextFile(File file) throws IOException {
        FileWriter fw = new FileWriter(file);
        try {
            BufferedWriter bw = new BufferedWriter(fw);
            try {
                bw.write(toString());
            } finally {
                bw.close();
            }
        } finally {
            fw.close();
        }
    }

    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * Returns a short description of the parameters.
     *
     * @return a short description of the parameters
     */
    public String getShortDescription() {

        SearchParameters defaultParameters = new SearchParameters();
        String newLine = System.getProperty("line.separator");
        StringBuilder output = new StringBuilder();

        if (digestionPreferences != null && !DigestionPreferences.getDefaultPreferences().equals(digestionPreferences)) {
            output.append(digestionPreferences.getShortDescription());
        }

        if (ptmSettings != null) {
            ArrayList<String> ptms = ptmSettings.getFixedModifications();
            if (!ptms.isEmpty()) {
                output.append("Fixed: ");
                boolean first = true;
                for (String ptm : ptms) {
                    if (first) {
                        output.append(ptm);
                        first = false;
                    } else {
                        output.append(", ").append(ptm);
                    }
                }
                output.append(".").append(newLine);
            }
        }

        if (ptmSettings != null) {
            ArrayList<String> ptms = ptmSettings.getVariableModifications();
            if (!ptms.isEmpty()) {
                output.append("Variable: ");
                boolean first = true;
                for (String ptm : ptms) {
                    if (first) {
                        output.append(ptm);
                        first = false;
                    } else {
                        output.append(", ").append(ptm);
                    }
                }
                output.append(".").append(newLine);
            }
        }

        if (!precursorTolerance.equals(defaultParameters.getPrecursorAccuracy())
                || !getPrecursorAccuracyType().equals(defaultParameters.getPrecursorAccuracyType())) {
            output.append("Precursor Tolerance: ").append(precursorTolerance).append(" ").append(precursorAccuracyType).append(".").append(newLine);
        }

        if (!fragmentIonMZTolerance.equals(defaultParameters.getFragmentIonAccuracy())
                || !getFragmentAccuracyType().equals(defaultParameters.getFragmentAccuracyType())) {
            output.append("Fragment Tolerance: ").append(fragmentIonMZTolerance).append(" ").append(fragmentAccuracyType).append(".").append(newLine);
        }

        if (!Util.sameLists(forwardIons, defaultParameters.getForwardIons())
                || !Util.sameLists(rewindIons, defaultParameters.getRewindIons())) {
            StringBuilder ions1 = new StringBuilder();
            Collections.sort(forwardIons);
            for (Integer ion : forwardIons) {
                if (ions1.length() > 0) {
                    ions1.append(", ");
                }
                ions1.append(PeptideFragmentIon.getSubTypeAsString(ion));
            }
            StringBuilder ions2 = new StringBuilder();
            Collections.sort(rewindIons);
            for (Integer ion : rewindIons) {
                if (ions2.length() > 0) {
                    ions2.append(", ");
                }
                ions2.append(PeptideFragmentIon.getSubTypeAsString(ion));
            }
            output.append("Ion Types: ").append(ions1).append(" and ").append(ions2).append(".").append(newLine);
        }

        if (!minChargeSearched.equals(defaultParameters.getMinChargeSearched())
                || !maxChargeSearched.equals(defaultParameters.getMaxChargeSearched())) {
            output.append("Charge: ").append(minChargeSearched.value).append("-").append(maxChargeSearched.value).append(".").append(newLine);
        }

        if (!getMinIsotopicCorrection().equals(defaultParameters.getMinIsotopicCorrection())
                || !getMaxIsotopicCorrection().equals(defaultParameters.getMaxIsotopicCorrection())) {
            output.append("Isotopic Correction: ").append(minIsotopicCorrection).append("-").append(maxIsotopicCorrection).append(".").append(newLine);
        }

        output.append("DB: ");
        if (fastaFile != null) {
            output.append(fastaFile.getName());
        } else {
            output.append("not set");
        }
        output.append(".").append(newLine);

        return output.toString();
    }

    /**
     * Returns the search parameters as a string.
     *
     * @param html use HTML formatting
     * @return the search parameters as a string
     */
    public String toString(boolean html) {

        String newLine;
        if (html) {
            newLine = "<br>";
        } else {
            newLine = System.getProperty("line.separator");
        }

        StringBuilder output = new StringBuilder();

        output.append("# ------------------------------------------------------------------");
        output.append(newLine);
        output.append("# General Search Parameters");
        output.append(newLine);
        output.append("# ------------------------------------------------------------------");
        output.append(newLine);
        output.append(newLine);

        output.append("DATABASE_FILE=");
        if (fastaFile != null) {
            output.append(fastaFile.getAbsolutePath());
        }
        output.append(newLine);

        if (digestionPreferences.getCleavagePreference() == DigestionPreferences.CleavagePreference.enzyme) {
            ArrayList<Enzyme> enzymes = digestionPreferences.getEnzymes();
            for (int i = 0; i < enzymes.size(); i++) {
                Enzyme enzyme = enzymes.get(i);
                String enzymeName = enzyme.getName();
                output.append("ENZYME").append(i).append("=");
                output.append(enzymeName).append(", ").append(digestionPreferences.getSpecificity(enzymeName));
                Integer nmc = digestionPreferences.getnMissedCleavages(enzymeName);
                if (nmc != null) {
                    output.append(", ").append(nmc).append(" missed cleavages");
                }
                output.append(newLine);
            }
        } else {
            output.append("ENZYME").append("=").append(digestionPreferences.getCleavagePreference().name);
        }

        output.append("FIXED_MODIFICATIONS=");
        if (ptmSettings != null) {
            ArrayList<String> fixedPtms = ptmSettings.getFixedModifications();
            boolean first = true;
            for (String ptm : fixedPtms) {
                if (first) {
                    output.append(ptm);
                    first = false;
                } else {
                    output.append("//").append(ptm);
                }
            }
        }
        output.append(newLine);

        output.append("VARIABLE_MODIFICATIONS=");
        if (ptmSettings != null) {
            ArrayList<String> fixedPtms = ptmSettings.getVariableModifications();
            boolean first = true;
            for (String ptm : fixedPtms) {
                if (first) {
                    output.append(ptm);
                    first = false;
                } else {
                    output.append("//").append(ptm);
                }
            }
        }
        output.append(newLine);

        output.append("REFINEMENT_FIXED_MODIFICATIONS=");
        if (ptmSettings != null && ptmSettings.getRefinementFixedModifications() != null) {
            ArrayList<String> fixedPtms = ptmSettings.getRefinementFixedModifications();
            boolean first = true;
            for (String ptm : fixedPtms) {
                if (first) {
                    output.append(ptm);
                    first = false;
                } else {
                    output.append("//").append(ptm);
                }
            }
        }
        output.append(newLine);

        output.append("REFINEMENT_VARIABLE_MODIFICATIONS=");
        if (ptmSettings != null && ptmSettings.getRefinementVariableModifications() != null) {
            ArrayList<String> fixedPtms = ptmSettings.getRefinementVariableModifications();
            boolean first = true;
            for (String ptm : fixedPtms) {
                if (first) {
                    output.append(ptm);
                    first = false;
                } else {
                    output.append("//").append(ptm);
                }
            }
        }
        output.append(newLine);

        output.append("PRECURSOR_MASS_TOLERANCE=");
        output.append(precursorTolerance);
        output.append(newLine);

        output.append("PRECURSOR_MASS_TOLERANCE_UNIT=");
        if (getPrecursorAccuracyType() == MassAccuracyType.PPM) {
            output.append("ppm");
        } else {
            output.append("Da");
        }
        output.append(newLine);

        output.append("FRAGMENT_MASS_TOLERANCE=");
        output.append(fragmentIonMZTolerance);
        output.append(newLine);

        output.append("FRAGMENT_MASS_TOLERANCE_UNIT=");
        if (getFragmentAccuracyType()== MassAccuracyType.PPM) {
            output.append("ppm");
        } else {
            output.append("Da");
        }
        output.append(newLine);

        output.append("PPM_TO_DA_CONVERSION_REF_MASS=");
        output.append(getRefMass());
        output.append(newLine);

        output.append("FORWARD_FRAGMENT_ION_TYPE=");
        StringBuilder ions1 = new StringBuilder();
        Collections.sort(forwardIons);
        for (Integer ion : forwardIons) {
            if (ions1.length() > 0) {
                ions1.append(", ");
            }
            ions1.append(PeptideFragmentIon.getSubTypeAsString(ion));
        }
        output.append(ions1);
        output.append(newLine);

        output.append("FRAGMENT_ION_TYPE_2=");
        StringBuilder ions2 = new StringBuilder();
        Collections.sort(rewindIons);
        for (Integer ion : rewindIons) {
            if (ions2.length() > 0) {
                ions2.append(", ");
            }
            ions2.append(PeptideFragmentIon.getSubTypeAsString(ion));
        }
        output.append(ions2);
        output.append(newLine);

        output.append("PRECURSOR_CHARGE_LOWER_BOUND=");
        output.append(minChargeSearched);
        output.append(newLine);

        output.append("PRECURSOR_CHARGE_UPPER_BOUND=");
        output.append(maxChargeSearched);
        output.append(newLine);

        output.append("ISOTOPIC_CORRECTION_LOWER_BOUND=");
        output.append(getMinIsotopicCorrection());
        output.append(newLine);

        output.append("ISOTOPIC_CORRECTION_UPPER_BOUND=");
        output.append(getMaxIsotopicCorrection());
        output.append(newLine);

        for (int index : algorithmParameters.keySet()) {
            output.append(newLine);
            output.append(newLine);
            output.append(algorithmParameters.get(index).toString(html));
        }

        return output.toString();
    }

    /**
     * Returns true if the search parameter objects have identical settings.
     *
     * @param otherSearchParameters the parameters to compare to
     *
     * @return true if the search parameter objects have identical settings
     */
    public boolean equals(SearchParameters otherSearchParameters) {

        if (otherSearchParameters == null) {
            return false;
        }
        if (this.getPrecursorAccuracyType() != otherSearchParameters.getPrecursorAccuracyType()) {
            return false;
        }
        if (!this.getPrecursorAccuracy().equals(otherSearchParameters.getPrecursorAccuracy())) {
            return false;
        }
        if (!this.getFragmentAccuracyType().equals(otherSearchParameters.getFragmentAccuracyType())) {
            return false;
        }
        if (!this.getFragmentIonAccuracy().equals(otherSearchParameters.getFragmentIonAccuracy())) {
            return false;
        }
        if ((this.getFastaFile() == null && otherSearchParameters.getFastaFile() != null)
                || (this.getFastaFile() != null && otherSearchParameters.getFastaFile() == null)) {
            return false;
        }
        if (this.getFastaFile() != null && otherSearchParameters.getFastaFile() != null) {
            if (!this.getFastaFile().getAbsolutePath().equalsIgnoreCase(otherSearchParameters.getFastaFile().getAbsolutePath())) {
                return false;
            }
        }
        if ((this.getDigestionPreferences() != null && otherSearchParameters.getDigestionPreferences() == null)
                || this.getDigestionPreferences() == null && otherSearchParameters.getDigestionPreferences() != null) {
            return false;
        }
        if (this.getDigestionPreferences() != null && otherSearchParameters.getDigestionPreferences() != null
                && !this.getDigestionPreferences().isSameAs(otherSearchParameters.getDigestionPreferences())) {
            return false;
        }
        if (!Util.sameLists(forwardIons, otherSearchParameters.getForwardIons())) {
            return false;
        }
        if (!Util.sameLists(rewindIons, otherSearchParameters.getRewindIons())) {
            return false;
        }
        if (!this.getMinChargeSearched().equals(otherSearchParameters.getMinChargeSearched())) {
            return false;
        }
        if (!this.getMaxChargeSearched().equals(otherSearchParameters.getMaxChargeSearched())) {
            return false;
        }
        if (!this.getMinIsotopicCorrection().equals(otherSearchParameters.getMinIsotopicCorrection())) {
            return false;
        }
        if (!this.getMaxIsotopicCorrection().equals(otherSearchParameters.getMaxIsotopicCorrection())) {
            return false;
        }
        if (!this.getPtmSettings().equals(otherSearchParameters.getPtmSettings())) {
            return false;
        }

        if (this.getAlgorithms().size() != otherSearchParameters.getAlgorithms().size()) {
            return false;
        }

        for (int se : getAlgorithms()) {
            IdentificationAlgorithmParameter otherParameter = otherSearchParameters.getIdentificationAlgorithmParameter(se);
            if (otherParameter == null) {
                return false;
            }
            IdentificationAlgorithmParameter thisParameter = getIdentificationAlgorithmParameter(se);
            if (!otherParameter.equals(thisParameter)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void setType() {
        marshallableParameterType = Type.search_parameters.name();
    }

    @Override
    public Type getType() {
        if (marshallableParameterType == null) {
            return null;
        }
        return Type.valueOf(marshallableParameterType);
    }
}
