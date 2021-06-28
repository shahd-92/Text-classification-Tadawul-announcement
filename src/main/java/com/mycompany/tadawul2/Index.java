/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.tadawul2;

import static com.mycompany.tadawul2.Index.loadStopwords;
import static com.mycompany.tadawul2.Index.stemmer2;
import static com.mycompany.tadawul2.Index.whenRemoveStopwordsUsingRemoveAll_thenSuccess;
import com.opencsv.CSVWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.tartarus.snowball.SnowballStemmer;

/**
 *
 * @author shahdalotaibi
 */
public class Index {

    static Map<Integer, String> sources;
    static HashMap<String, IndexValue> index;
    static HashMap<String, Integer> sortedIndex;
    static HashMap<String, Integer> sortedIndexIDF;
    static List<String> stopwords;
    static List<String> keys;
    private static int docId;
    static private int i;
    private HashMap<String, IndexValue> index30;

    Index() {
        sources = new HashMap<Integer, String>();
        index = new HashMap<String, IndexValue>();
        stopwords = new ArrayList<String>();
        sortedIndex = new HashMap<String, Integer>();
        sortedIndexIDF = new HashMap<String, Integer>();
        index30 = new HashMap<String, IndexValue>();
        keys = new ArrayList<String>();
    }

    public static void loadStopwords() throws IOException {

        stopwords = Files.readAllLines(Paths.get("list.txt"));
    }

    public static String whenRemoveStopwordsUsingRemoveAll_thenSuccess(String original) throws IOException {
        loadStopwords();
        original = original.replaceAll("\\d", " ");
        original = original.replaceAll("[١٢٣٤٥٦٧٨٩٠]+", " ");
        original = original.replaceAll("[!@#$٪^&*)(ـ؛:٬؟/<>،.+ـ=’-]+", " ");
        original = original.replaceAll("[–]", " ");
        original = original.replaceAll("[!@#$%^&*()_+=-}{|:;~`<>/?,./  ]+", " ");

        ArrayList<String> allWords = Stream.of(original.toLowerCase().split(" "))//[, ?.@()<>-]+
                .collect(Collectors.toCollection(ArrayList<String>::new));
        for (String w : allWords) {
            if (w.length() < 3) {
                stopwords.add(w);
            }
        }

        allWords.removeAll(stopwords);
        String result = allWords.stream().collect(Collectors.joining(" "));
        return result;
    }

    public static String stemmer2(String term) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class stemClass = Class.forName("org.tartarus.snowball.ext." + "arabicStemmer");
        SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
        String stemmedTerm = "";
        stemmer.setCurrent(term);
        stemmer.stem();
        return stemmer.getCurrent();
    }

    public void buildIndex(String ln, String tag) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {

        ln = whenRemoveStopwordsUsingRemoveAll_thenSuccess(ln);
        String[] words = ln.split(" "); //[, ?.@()<>-]،:W+

        sources.put(i, tag);
        int wordID = 0;
        for (String word : words) {

            word = word.toLowerCase();
            String Stemedword = stemmer2(word);
            String key = tag + " . " + Stemedword;
            if (!index.containsKey(key)) {
                IndexValue term = new IndexValue();
                term.stemmedTerm.add(word);
                term.df = 1;
                term.ctf = 1;

                term.termPos.add("d" + docId + "." + wordID);
                term.docIds.put(docId, 1);
                index.put(key, term);
                sortedIndex.put(key, term.ctf);

            } else if (index.containsKey(key)) {
                //increment DF , if the term not exist in doc
                if (!index.get(key).docIds.containsKey(docId)) {
                    index.get(key).docIds.put(docId, 1);
                    index.get(key).df++;
                } else if (index.get(key).docIds.containsKey(docId)) {
                    int oldtf = index.get(key).docIds.get(docId);
                    index.get(key).docIds.replace(docId, ++oldtf);
                }
                index.get(key).stemmedTerm.add(word);
                index.get(key).termPos.add("d" + docId + "." + wordID);
                index.get(key).ctf++;
                sortedIndex.replace(key, sortedIndex.get(key) + 1);

            }
            wordID++;
        }
//        index.entrySet().forEach(entry -> {
//            int tempIDF = docId / (int) entry.getValue().df;
//            
//            System.out.println("tempIDF --> " + tempIDF);
//            sortedIndexIDF.put(entry.getKey(), tempIDF);
//        });        //sortedIndexIDF = sortedIndex

        i++;

    }

    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> index) {
        // Create a list from elements of HashMap 

        List<Map.Entry<String, Integer>> list
                = new LinkedList<Map.Entry<String, Integer>>(index.entrySet());

        // Sort the list index.sortedIndex
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                    Map.Entry<String, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // put data from sorted list to hashmap  
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
        int i = 0;
        int percent20 = (int) (0.20 * list.size());

//        System.out.println("list.size() --> " + list.size());
//        System.out.println("docId --> " + docId);

        for (Map.Entry<String, Integer> aa : list) {
            if (i < list.size()) {
//              if (i >=50 && i< percent20 ) {
//            if (i < 30) {
//                System.out.println("i --> " + i);

                temp.put(aa.getKey(), aa.getValue());
//            } else {
//                return temp;
            }
            i++;
        }

        return temp;
    }

    public HashMap<String, IndexValue> mostFreq() {
        HashMap<String, Integer> si = this.sortByValue(this.sortedIndexIDF);
        HashMap<String, IndexValue> index30 = new HashMap<String, IndexValue>();

        si.entrySet().forEach(entry -> {
            if (this.index.containsKey(entry.getKey())) {
                index30.put(entry.getKey(), this.index.get(entry.getKey()));
                keys.add(entry.getKey());
//                System.out.println(entry.getKey() + " --> " + this.index.get(entry.getKey()));
            }
        });
        return index30;
    }

    public void readFile(String file) throws ParserConfigurationException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String line = "";
        String splitBy = ",";
        String[] rows;
        docId = 0;
        try {
//parsing a CSV file into BufferedReader class constructor  
            i = 0;
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) //returns a Boolean value  
            {
                rows = line.split(splitBy);    // use comma as separator  
//                System.out.println("Label=" + rows[0] + ", Date=" + rows[1] + ", Time=" + rows[2] + ", Link=" + rows[3] + "]");
                DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

                org.w3c.dom.Document wDocument = documentBuilder.newDocument();
                wDocument.setStrictErrorChecking(false);
                org.jsoup.nodes.Document doc = SSLHelper.getConnection(rows[3]).userAgent("Chrome").timeout(60 * 5000).get();

                String newsTitle = doc.select("h1").text();
//                System.out.println(newsTitle);
                String article_body = doc.getElementsByClass("article_body").get(0).text();
                buildIndex(newsTitle, "Title");
                buildIndex(article_body, "Text");
                docId++;
            }

            index.entrySet().forEach(entry -> {
                int tempIDF = docId / (int) entry.getValue().df;
                index.get(entry.getKey()).idf = tempIDF;

//                System.out.println("tempIDF --> " + tempIDF);
                sortedIndexIDF.put(entry.getKey(), tempIDF);
            });        //sortedIndexIDF = sortedIndex

            index30 = mostFreq();
//            System.out.print("index30.size() :" + index30.size());
            writeCSV(index30);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeCSV(HashMap<String, IndexValue> index) {
        String filePath = "goldstd.csv";
        String line = "";
        String splitBy = ",";
        String[] rows;
        File file = new File(filePath);
        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter("outputfile.csv");
            int rowIndex = 0;
            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputfile);
            BufferedReader br = new BufferedReader(new FileReader(file));
            // adding header to csv

            String[] header = new String[index.size() + 1];
//            List<String> header = new ArrayList<String>();
//            header.add("Label");
//            header.add("Date");
//            header.add("Time");
//            header[1] = "Date";
//            header[2] = "Time";
            header[index.size()] = "Label";
            for (int i = 0; i < index.size(); i++) {
//                header.add(keys.get(i));
                header[i] = keys.get(i);
            }

//            String[] h = (String[]) header.toArray();
//            writer.writeNext(header);
            while ((line = br.readLine()) != null) //returns a Boolean value  
            {
//                System.out.println("rowIndex --> " + rowIndex);
                rows = line.split(splitBy);
//                ArrayList<String> data1 = new ArrayList<String>();
                String[] data1 = new String[index.size() + 1];
                
//                data1.add(rows[0]);
//                data1.add(rows[1]);
//                data1.add(rows[2]);

//                data1[1] = rows[1];
//                data1[2] = rows[2];
//                index.entrySet().forEach(entry -> {
// add data to csv
//                    String[] data1 = {"Aman", "10", "620"};
//                    String[] data1 = new String[33];
                for (int i = 0; i < index.size(); i++) {
//                        data1.add(keys.get(i));
//                    System.out.println(" --> " + index30.get(keys.get(i)).docIds.keySet().toString());
                    data1[index.size()] = rows[0].toString();
                    if (rows[0].toString()=="Ôªø0" || rowIndex ==0)
                        data1[index.size()]="0";
                    if (index30.get(keys.get(i)).docIds.containsKey(rowIndex)) {
                        data1[i] = index30.get(keys.get(i)).docIds.get(rowIndex).toString();
//                        System.out.println(" true ");
                    } else {
                        data1[i] = "0";
//                        System.out.println(" false ");
                    }

                }
                writer.writeNext(data1);

//                System.out.println(entry.getKey() + " --> " + this.index.get(entry.getKey()));
//                });
                rowIndex++;
            }
            // closing writer connection
            writer.close();
            System.out.println("CSV file is written");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

class IndexValue {

    int df, ctf, idf;
    HashSet<String> termPos;
    HashSet<String> stemmedTerm;
    HashMap<Integer, Integer> docIds;

    IndexValue() {
        df = 0;
        idf = 0;
        ctf = 0;
        termPos = new HashSet<String>();
        stemmedTerm = new HashSet<String>();
        docIds = new HashMap<Integer, Integer>();

    }

    @Override
    public String toString() {
        String docIdsString = "";
        docIds.entrySet().forEach(entry -> {
            docIdsString.concat("(d" + entry.getKey() + " , " + entry.getValue() + ")");
        });
        return "{" + "df=" + df + ", idf=" + idf + ", ctf=" + ctf //+ ", termPos=" + termPos.toString() 
                + ", documents Ids=" + docIds.toString().replace("=", ": tf=").replace("{", "{d").replace(", ", ", d") + ", stemmed terms=" + stemmedTerm.toString() + "}";

    }

}
