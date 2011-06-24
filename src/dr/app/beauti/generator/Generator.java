package dr.app.beauti.generator;

import dr.app.beauti.components.ComponentFactory;
import dr.app.beauti.options.*;
import dr.app.beauti.util.XMLWriter;
import dr.evoxml.MergePatternsParser;
import dr.evoxml.SitePatternsParser;
import dr.inference.model.ParameterParser;
import dr.util.Attribute;
import dr.xml.XMLParser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexei Drummond
 * @author Andrew Rambaut
 * @author Walter Xie
 */
public abstract class Generator {

    protected static final String COALESCENT = "coalescent";
    public static final String SP_TREE = "sptree";
    protected static final String SPECIATION_LIKE = "speciation.likelihood";
    public static final String SPLIT_POPS = "splitPopSize";
    protected static final String PDIST = "pdist";
    //	protected static final String STP = "stp";
    protected static final String SPOPS = TraitData.TRAIT_SPECIES + "." + "popSizesLikelihood";

    protected final BeautiOptions options;

    //    protected PartitionSubstitutionModel model;
    protected String modelPrefix = ""; // model prefix, could be PSM, PCM, PTM, PTP

    protected Generator(BeautiOptions options) {
        this.options = options;
    }

    public Generator(BeautiOptions options, ComponentFactory[] components) {
        this.options = options;
        if (components != null) {
            for (ComponentFactory component : components) {
                this.components.add(component.getGenerator(options));
            }
        }
    }

    public String getModelPrefix() {
        return modelPrefix;
    }

    public void setModelPrefix(String modelPrefix) {
        this.modelPrefix = modelPrefix;
    }

    /**
     * fix a parameter
     *
     * @param id    the id
     * @param value the value
     */
//    public void fixParameter(Parameter parameter, double value) {
////        dr.app.beauti.options.Parameter parameter = options.getParameter(id);
//        if (parameter == null) {
//            throw new IllegalArgumentException("parameter with name, " + parameter.getName() + ", is unknown");
//        }
//        parameter.isFixed = true;
//        parameter.initial = value;
//    }


    /**
     * write a parameter
     *
     * @param wrapperName wrapperName
     * @param id          the id
     * @param writer      the writer
     */
    public void writeParameterRef(String wrapperName, String id, XMLWriter writer) {
        writer.writeOpenTag(wrapperName);
        writer.writeIDref(ParameterParser.PARAMETER, id);
        writer.writeCloseTag(wrapperName);
    }

    public void writeParameterRef(String id, XMLWriter writer) {
        writer.writeIDref(ParameterParser.PARAMETER, id);
    }

    /**
     * write a parameter
     *
     * @param id      the id
     * @param options PartitionOptions
     * @param writer  the writer
     */
    public void writeParameter(String id, PartitionOptions options, XMLWriter writer) {
        Parameter parameter = options.getParameter(id);
        String prefix = options.getPrefix();

        if (parameter == null) {
            throw new IllegalArgumentException("parameter with name, " + id + ", is unknown; and its prefix is " + options.getPrefix());
        }
        writeParameter(prefix + id, parameter, writer);
    }


    public void writeParameter(int num, String id, PartitionSubstitutionModel model, XMLWriter writer) {
        Parameter parameter = model.getParameter(model.getPrefixCodon(num) + id);
        String prefix = model.getPrefix(num);

        if (parameter == null) {
            throw new IllegalArgumentException("parameter with name, " + id + ", is unknown; and its prefix is " + model.getPrefix());
        }
        writeParameter(prefix + id, parameter, writer);
    }

    /**
     * write a parameter
     *
     * @param wrapperName wrapperName
     * @param id          the id
     * @param dimension   dimension
     * @param writer      the writer
     */
    public void writeParameter(String wrapperName, String id, int dimension, XMLWriter writer) {
        Parameter parameter = options.getParameter(id);
        writer.writeOpenTag(wrapperName);
        writeParameter(parameter, dimension, writer);
        writer.writeCloseTag(wrapperName);
    }

    /**
     * write a parameter
     *
     * @param num         num
     * @param wrapperName wrapperName
     * @param id          the id
     * @param model       PartitionSubstitutionModel
     * @param writer      the writer
     */
    public void writeParameter(int num, String wrapperName, String id, PartitionSubstitutionModel model, XMLWriter writer) {
        writer.writeOpenTag(wrapperName);
        writeParameter(num, id, model, writer);
        writer.writeCloseTag(wrapperName);
    }

    public void writeParameter(String wrapperName, String id, PartitionOptions options, XMLWriter writer) {
        writer.writeOpenTag(wrapperName);
        writeParameter(id, options, writer);
        writer.writeCloseTag(wrapperName);
    }

    /**
     * write a parameter
     *
     * @param parameter the parameter
     * @param dimension the dimension
     * @param writer    the writer
     */
    public void writeParameter(Parameter parameter, int dimension, XMLWriter writer) {
//        Parameter parameter = options.getParameter(id);
        if (parameter == null) {
            throw new IllegalArgumentException("parameter (== null) is unknown");
        }
        if (parameter.isFixed) { // with prefix
            writeParameter(parameter.getName(), dimension, parameter.initial, Double.NaN, Double.NaN, writer);
        } else {
            double lower = Double.NaN;
            double upper = Double.NaN;
            if (parameter.isNonNegative) {
                lower = 0.0;
            }
            if (parameter.isZeroOne) {
                lower = 0.0;
                upper = 1.0;
            }
            writeParameter(parameter.getName(), dimension, parameter.initial, lower, upper, writer);
        }
    }

    public void writeParameter(String id, Parameter parameter, XMLWriter writer) {
        if (parameter.isFixed) {
            writeParameter(id, 1, parameter.initial, Double.NaN, Double.NaN, writer);
        } else {
            double lower = Double.NaN;
            double upper = Double.NaN;
            if (parameter.isNonNegative) {
                lower = 0.0;
            }
            if (parameter.isZeroOne) {
                lower = 0.0;
                upper = 1.0;
            }
            writeParameter(id, 1, parameter.initial, lower, upper, writer);
        }
    }


    /**
     * write a parameter
     *
     * @param id        the id
     * @param dimension the dimension
     * @param value     the value
     * @param lower     the lower bound
     * @param upper     the upper bound
     * @param writer    the writer
     */
    public void writeParameter(String id, int dimension, double value, double lower, double upper, XMLWriter writer) {
        ArrayList<Attribute.Default> attributes = new ArrayList<Attribute.Default>();
        attributes.add(new Attribute.Default<String>(XMLParser.ID, id));
        if (dimension > 1) {
            attributes.add(new Attribute.Default<String>(ParameterParser.DIMENSION, dimension + ""));
        }
        if (!Double.isNaN(value)) {
            attributes.add(new Attribute.Default<String>(ParameterParser.VALUE, multiDimensionValue(dimension, value)));
        }
        if (!Double.isNaN(lower)) {
            attributes.add(new Attribute.Default<String>(ParameterParser.LOWER, multiDimensionValue(dimension, lower)));
        }
        if (!Double.isNaN(upper)) {
            attributes.add(new Attribute.Default<String>(ParameterParser.UPPER, multiDimensionValue(dimension, upper)));
        }

        Attribute[] attrArray = new Attribute[attributes.size()];
        for (int i = 0; i < attrArray.length; i++) {
            attrArray[i] = attributes.get(i);
        }

        writer.writeTag(ParameterParser.PARAMETER, attrArray, true);
    }

    /**
     * write a parameter
     *
     * @param wrapperName wrapperName
     * @param id          the id
     * @param dimension   the dimension
     * @param value       the value
     * @param lower       the lower bound
     * @param upper       the upper bound
     * @param writer      the writer
     */
    public void writeParameter(String wrapperName, String id, int dimension, double value, double lower, double upper, XMLWriter writer) {
        writer.writeOpenTag(wrapperName);
        writeParameter(id, dimension, value, lower, upper, writer);
        writer.writeCloseTag(wrapperName);
    }

    protected void writeCodonPatternsRef(String prefix, int num, int CodonPartitionCount, XMLWriter writer) {
        if (CodonPartitionCount == 2 && num == 1) { // "11" of "112", num start from 1
            writer.writeIDref(MergePatternsParser.MERGE_PATTERNS, prefix + SitePatternsParser.PATTERNS);
        } else { // "2" of "112" and "123"
            writer.writeIDref(SitePatternsParser.PATTERNS, prefix + SitePatternsParser.PATTERNS);
        }
    }

    private String multiDimensionValue(int dimension, double value) {
        String multi = "";

        multi += value + "";

        // AR: A multidimensional parameter only needs to give initial values for every dimension
        // if they are actually different. A single value will automatically be expanded to every
        // dimension and make for a cleaner looking XML (and more robust to changes in the number
        // of groups/taxa etc.

//        for (int i = 2; i <= dimension; i++)
//            multi += " " + value;

        return multi;
    }

    public void writeReferenceComment(String[] lines, XMLWriter writer) {
        for (String line : lines)
            writer.writeComment(line);
    }

    protected void generateInsertionPoint(final ComponentGenerator.InsertionPoint ip, final XMLWriter writer) {
        generateInsertionPoint(ip, null, writer);
    }

    protected void generateInsertionPoint(final ComponentGenerator.InsertionPoint ip, final Object item, final XMLWriter writer) {
        for (ComponentGenerator component : components) {
            if (component.usesInsertionPoint(ip)) {
                component.generateAtInsertionPoint(ip, item, writer);
            }
        }
    }

    private final List<ComponentGenerator> components = new ArrayList<ComponentGenerator>();

    public class GeneratorException extends Exception {
        public GeneratorException(String message) {
            super(message);
        }
    }
}
