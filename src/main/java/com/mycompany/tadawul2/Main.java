/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.tadawul2;

import static com.mycompany.tadawul2.Index.keys;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;
import org.tartarus.snowball.SnowballStemmer;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.core.FastVector;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.Remove;

/**
 *
 * @author shahdalotaibi
 */
public class Main {

    private static void convertCSVtoArff(String filename) throws Exception {

        // load CSV
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(filename));

        // CSV uses no header
        String[] options = new String[3];

        options[0] = "-H";
        options[1] = "-N";
        options[2] = "first-last";
        loader.setOptions(options);

        Instances data = loader.getDataSet();

        // save ARFF
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);

        filename = filename.replace(".csv", ".arff");

        saver.setFile(new File(filename));
        saver.writeBatch();
        System.out.println("CSV file is converted to ARFF file");

    }

    public static BufferedReader readDataFile(String filename) {
        BufferedReader inputReader = null;
        try {
            inputReader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException ex) {
            System.err.println("File not found: " + filename);
        }
        return inputReader;
    }

    public static Evaluation classify(Classifier model,
            Instances trainingSet, Instances testingSet) throws Exception {
        Evaluation evaluation = new Evaluation(trainingSet);
        model.buildClassifier(trainingSet);
        System.out.println("trainingSet: " + trainingSet.size());
        System.out.println("testingSet: " + testingSet.size());
        evaluation.evaluateModel(model, testingSet);
        System.out.println("evaluation: " + evaluation.correct());
        return evaluation;
    }

    public static double calculateAccuracy(FastVector predictions) {
        double correct = 0;
        for (int i = 0; i < predictions.size(); i++) {
            NominalPrediction np = (NominalPrediction) predictions.elementAt(i);
            if (np.predicted() == np.actual()) {
                correct++;
            }
        }
        System.out.println("correct: " + correct);
        return 100 * correct / predictions.size();
    }

    public static Instances[][] crossValidationSplit(Instances data, int numberOfFolds) {
        Instances[][] split = new Instances[2][numberOfFolds];
        for (int i = 0; i < numberOfFolds; i++) {
            split[0][i] = data.trainCV(numberOfFolds, i);
            split[1][i] = data.testCV(numberOfFolds, i);
        }
        return split;
    }

    public static Instances selectAttributeIG(Instances instances, int num) throws Exception {
//        instances.setClassIndex(0);
        InfoGainAttributeEval eval = new InfoGainAttributeEval();
        Ranker search = new Ranker();
        String sNum = num + "";
        search.setOptions(new String[]{"-N", sNum});	// information gain threshold
        AttributeSelection attSelect = new AttributeSelection();
        attSelect.setEvaluator(eval);
        attSelect.setSearch(search);

        // apply attribute selection
        attSelect.SelectAttributes(instances);

        // remove the attributes not selected in the last run
        Instances reduced = attSelect.reduceDimensionality(instances);

        System.out.println("reduced classIndex: " + reduced.classIndex());
        System.out.println("reduced numAttributes: " + reduced.numAttributes());
//        System.out.println("reduced classAttribute: "+reduced.classAttribute().toString());
        return reduced;
    }

    public static Instances selectAttributeIDF(Instances instances, int num) throws Exception {
        int[] indices = new int[num + 1];
        for (int i = 0; i < num; i++) {
            indices[i] = instances.attribute(i).index();
//                instances30.add(instances.get(i));
        }
        indices[num] = instances.attribute(instances.classIndex()).index();
        Remove removeFilter = new Remove();
        removeFilter.setAttributeIndicesArray(indices);
        removeFilter.setInvertSelection(true);
        removeFilter.setInputFormat(instances);
        Instances newData = Filter.useFilter(instances, removeFilter);
        return newData;
    }

    public static void trainModels(Instances instances) throws Exception {
        // Do 260-split cross validation
//        instances.setClassIndex(0);
        Instances[][] split = crossValidationSplit(instances, 6);

        // Separate split into training and testing arrays
        Instances[] trainingSplits = split[0];
        Instances[] testingSplits = split[1];
        System.out.println("trainingSplits: " + trainingSplits.length);
        // Use a set of classifiers
        Classifier[] models = {
            new J48(),
            //            new NaiveBayes(),
            new SMO()
//            new PART(), 
//            new DecisionTable(),//decision table majority classifier
//            new DecisionStump() //one-level decision tree
        };
        // Run for each model
        for (int j = 0; j < models.length; j++) {

            // Collect every group of predictions for current model in a FastVector
            FastVector predictions = new FastVector();
            Evaluation eval = new Evaluation(instances);
            eval.crossValidateModel(models[j], instances, 6, new Random(1));
            System.out.println(eval.toSummaryString("\nResults of " + models[j].getClass().getSimpleName() + "\n======\n", false));

//            //trueNegative
            System.out.println("MatrixString of " + models[j].getClass().getSimpleName() + ": "
                    + eval.toMatrixString()
                    + "\n----------------");
//            //Accuracy
            System.out.println("----------------\nAccuracy of " + models[j].getClass().getSimpleName() + ": "
                    + String.format("%.2f%%", (eval.correct() / trainingSplits.length) * 100)
                    + "\n----------------");
//            //Recall
            System.out.println("Recall of " + models[j].getClass().getSimpleName().toString() + ": "
                    + String.format("%.2f%%", eval.recall(1) * 100)
                    + "\n----------------");
//            //Precision
            System.out.println("Precision of " + models[j].getClass().getSimpleName() + ": "
                    + String.format("%.2f%%", eval.precision(1) * 100)
                    + "\n----------------");
//            //fMeasure
            System.out.println("fMeasure of " + models[j].getClass().getSimpleName() + ": "
                    + String.format("%.2f%%", eval.fMeasure(1) * 100)
                    + "\n----------------");
//            
////            //trueNegative
//            System.out.println("TN of " + models[j].getClass().getSimpleName() + ": "
//                    + eval.trueNegativeRate(1)
//                    + "\n----------------");
////            //trueNegative
//            System.out.println("TP of " + models[j].getClass().getSimpleName() + ": "
//                    + eval.truePositiveRate(1)
//                    + "\n----------------");
////            //trueNegative
//            System.out.println("FN of " + models[j].getClass().getSimpleName() + ": "
//                    + eval.falseNegativeRate(1)
//                    + "\n----------------");
////            //trueNegative
//            System.out.println("FP of " + models[j].getClass().getSimpleName() + ": "
//                    + eval.falsePositiveRate(1)
//                    + "\n----------------");
////
////            //trueNegative
//            System.out.println("weightedFalseNegativeRate of " + models[j].getClass().getSimpleName() + ": "
//                    + eval.weightedFalseNegativeRate()
//                    + "\n----------------");
////            //trueNegative
//            System.out.println("weightedFalsePositiveRate of " + models[j].getClass().getSimpleName() + ": "
//                    + eval.weightedFalsePositiveRate()
//                    + "\n----------------");
////            //trueNegative
//            System.out.println("weightedTrueNegativeRate of " + models[j].getClass().getSimpleName() + ": "
//                    + eval.weightedTrueNegativeRate()
//                    + "\n----------------");
////            //trueNegative
//            System.out.println("weightedTruePositiveRate of " + models[j].getClass().getSimpleName() + ": "
//                    + eval.weightedTruePositiveRate()
//                    + "\n----------------");
//
//            System.out.println(s);
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

//        System.out.println();
//        System.out.println("3010.xml");
//        Index index3010 = new Index();
//        Index index30 = new Index();
//        index3010.readFile(new String("goldstd.csv"));
//
//        convertCSVtoArff("outputfile.csv");

        ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource("outputfile.arff");
        Instances instances = dataSource.getDataSet();

        instances.setClassIndex(instances.numAttributes() - 1);

        System.out.println("instances.classIndex(): " + instances.classIndex());
        System.out.println("number of Classes " + instances.numClasses());
        System.out.println("number of Instances(): " + instances.numInstances());
        System.out.println("number of Attributes(): " + instances.numAttributes());

        //Without attributes selection
        System.out.println("\n========================================\n"
                + "With all of the attributes (Without attributes selection :\n"
                + "========================================");
        System.out.println("instances.classIndex(): " + instances.classIndex());
        System.out.println("number of Classes " + instances.numClasses());
        System.out.println("number of Attributes(): " + instances.numAttributes());
        trainModels(instances);

        //IDF atribure selection
        System.out.println("\n========================================\n"
                + "select attribute 2000 using IDF :\n"
                + "========================================");
        Instances instancesIDF = selectAttributeIDF(instances, 2000);
        instancesIDF.setClassIndex(instancesIDF.numAttributes() - 1);
        System.out.println("number of Classes " + instancesIDF.numClasses());
        System.out.println("number of Attributes(): " + instancesIDF.numAttributes());
        trainModels(instancesIDF);

        //IG atribure selection
        System.out.println("\n\n========================================\n"
                + "select attribute 50 using IG :\n"
                + "========================================");
        Instances instancesIG = selectAttributeIG(instances, 50);
        instancesIG.setClassIndex(instancesIG.numAttributes() - 1);
//        instancesIG.setClassIndex(0);
        System.out.println("instances.classIndex(): " + instances.classIndex());
        System.out.println("number of Classes " + instancesIG.numClasses());
        System.out.println("number of Attributes(): " + instancesIG.numAttributes());
        trainModels(instancesIG);

        //IG atribure selection
        System.out.println("\n\n========================================\n"
                + "select attribute 2000 using IDF then select 50 IG :\n"
                + "========================================");
        Instances instancesIDF2000 = selectAttributeIDF(instances, 2000);

        instancesIDF2000.setClassIndex(instancesIDF2000.numAttributes() - 1);
//        instancesIDF2000.setClassIndex(0);
        Instances instancesIDFIG = selectAttributeIG(instancesIDF2000, 50);

        instancesIDFIG.setClassIndex(instancesIDFIG.classIndex());
//        instancesIDFIG.setClassIndex(0);
        System.out.println("instances.classIndex(): " + instances.classIndex());
        System.out.println("number of Classes " + instancesIDF2000.numClasses());
        System.out.println("number of Attributes(): " + instancesIDFIG.numAttributes());
        trainModels(instancesIDFIG);
    }

}
