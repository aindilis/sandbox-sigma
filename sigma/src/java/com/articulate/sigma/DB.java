/* This code is copyrighted by Articulate Software (c) 2007.
It is released under the GNU Public License <http://www.gnu.org/copyleft/gpl.html>.
Users of this code also consent, by use of this code, to credit Articulate Software in any
writings, briefings, publications, presentations, or other representations of any
software which incorporates, builds on, or uses this code.
Please cite the following article in any publication with references:

Pease, A., (2003). The Sigma Ontology Development Environment, 
in Working Notes of the IJCAI-2003 Workshop on Ontology and Distributed Systems,
August 9, Acapulco, Mexico.  See also http://sigmakee.sourceforge.net
*/
package com.articulate.sigma;
import java.util.*;
import java.io.*;

/* A class to interface with databases and database-like formats,
such as spreadsheets. */

public class DB {

    /** ***************************************************************
     */
    private static String wordWrap(String input, int length) {

        StringBuffer s = new StringBuffer();
        String[] r = input.split("\n");
        if (length > 0) {        
            for (int i = 0; i < r.length; i++) {
                s = new StringBuffer();
                while (r[i].length() > length) {
                    int cursor = length;
                    while (cursor > 0 && r[i].charAt(cursor) != ' ') 
                        cursor--;
                    if (cursor > 0)  {
                        s.append(r[i].substring(0,cursor) +  System.getProperty("line.separator"));
                        r[i] = r[i].substring(cursor+1);
                    }
                }
                r[i] = s.toString() + r[i];
            }
        }

        s = new StringBuffer();
        for (int i = 0; i < r.length; i++) {
            s.append(r[i] +  System.getProperty("line.separator"));
        }
        return s.toString();
    };


    /** ***************************************************************
     */
    private static String StringToKIFid(String s) {

        //System.out.println("INFO in StringToKIFid() : " + s);
        if (s == null) 
            return s;
        s = s.trim();
        if (s.length() < 1)
            return s;
        if (!Character.isJavaIdentifierStart(s.charAt(0))) 
            s = s.substring(1);
        int i = 0;
        while (i < s.length()) {
            if (!Character.isJavaIdentifierPart(s.charAt(i))) 
                s = s.substring(0,i) + "-" + s.substring(i+1);
            i++;
        }
        return s;
    }

    /** ***************************************************************
     * Parse a CSV file into an ArrayList of ArrayLists of class
     * Concatenate lines not starting with one of two "flag" characters,
     * indicating a bug that Excel has split a cell during output.
     */
    private static ArrayList readSpreadsheet(String fname) {

        ArrayList rows = new ArrayList();

        // System.out.println("INFO in DB.readSpreadsheet()");
        try {
            FileReader r = new FileReader(fname);
            LineNumberReader lr = new LineNumberReader(r);
            String line = null;
            ArrayList textrows = new ArrayList();
            while ((line = lr.readLine()) != null) {               // concatenate lines not starting with "u" or "$"
                if (!line.startsWith("\"u\"") && !line.startsWith("\"$\"")) {
                    // System.out.println("Line with bad start: " + line);
                    String previousLine = (String) textrows.get(textrows.size()-1);
                    // System.out.println("Previous line " + previousLine);
                    line = previousLine + line;
                    textrows.remove(textrows.size()-1);
                    textrows.add(line);
                    // System.out.println("New line " + line);
                }
                else
                    textrows.add(line);
            }
            for (int i = 0; i < textrows.size(); i++) {         // parse comma delimited cells into an ArrayList
                line = (String) textrows.get(i);
                // System.out.println(line);
                StringBuffer cell = new StringBuffer();
                ArrayList row = new ArrayList();
                boolean inString = false;
                for (int j = 0; j < line.length(); j++) {
                    //System.out.print(line.charAt(i));
                    if (line.charAt(j) == ',' && !inString) {
                        row.add(cell.toString());
                        cell = new StringBuffer();
                    } else if (line.charAt(j) == '"') {
                        inString = !inString;
                        //System.out.println();
                        //System.out.println(inString);
                    } else  
                        cell.append(line.charAt(j));                    
                }
                rows.add(row);
            }
        }
        catch (IOException ioe) {
            System.out.println("Error in DB.readSpreadsheet(): IOException: " + ioe.getMessage());
        } 
        return rows;
    }      

    /** ***************************************************************
     */
    private static boolean emptyString(String s) {

        if (s == null) 
            return true;
        else if (s == "") 
            return true;
        else if (s.length() < 1) 
            return true;
        else
            return false;
    }

    /** ***************************************************************
     */
    private static void processForRDFExport(ArrayList rows) {

        //System.out.println("<!-- Begin Export -->");
        //System.out.println(rows.size());
        System.out.println("<rdf:RDF");
        System.out.println("  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"");
        System.out.println("  xmlns:op=\"http://ontologyportal.org/SUMO.owl.txt\">");

        String domain = "";
        String subject = "";
        String relator = "";
        String range = "";
        for (int rowNum = 0; rowNum < rows.size(); rowNum++) {
            ArrayList row = (ArrayList) rows.get(rowNum);
            if (row.size() > 7) {            
                domain = (String) row.get(4);
                relator = (String) row.get(5);
                range = (String) row.get(7);
                if (!emptyString(domain) && !emptyString(relator) && !emptyString(range)) {                                              
                    System.out.println("  <rdf:Description rdf:about=\"" + domain + "\">");
                    System.out.println("    <op:" + relator + ">" + range + "</op:" + relator + ">");
                    System.out.println("  </rdf:Description>");
                }
            }
        }
        System.out.println("</rdf:RDF>");
    }

    /** ***************************************************************
     */
    private static void processSpreadsheet(ArrayList rows) {

        String domain = "";
        String subject = "";            // from column M
        String relator = "";
        String range = "";
        String cell = "";
        String documentation = "";
        String namespace = "";
        for (int rowNum = 47; rowNum < rows.size(); rowNum++) {
            ArrayList row = (ArrayList) rows.get(rowNum);
            boolean comment = ((String) row.get(0)).equals("$");                // comment is denoted by $ in first cell
            if (row.size() > 14 && !comment) {                                              
                cell = (String) row.get(12);                                    // column M
                if (cell.indexOf("^") > -1) {
                    subject = cell.substring(cell.indexOf("^") + 1, cell.length());
                    namespace = cell.substring(0,cell.indexOf("^"));
                }
                else 
                    subject = cell;
                subject = StringToKIFid(subject);
                documentation = (String) row.get(14);                           // column O
                documentation = documentation.trim();
                if (!emptyString(documentation) && !emptyString(subject)) {
                    String doc = "(localDocumentation " + namespace + " EnglishLanguage " + subject + " \"" + documentation + "\")";
                    System.out.print(wordWrap(doc,70));
                }
                if (row.size() > 24) {                                          
                    cell = (String) row.get(21);                                // column V
                    if (cell.indexOf("^") > -1) 
                        domain = cell.substring(cell.indexOf("^") + 1, cell.length());
                    else 
                        domain = cell;
                    domain = StringToKIFid(domain);
                    cell = (String) row.get(23);                                // column X
                    if (cell.indexOf("^") > -1) 
                        relator = cell.substring(cell.indexOf("^") + 1, cell.length());
                    else 
                        relator = cell;
                    relator = StringToKIFid(relator);
                    if (relator.equals("IsA")) relator = "instance";
                    if (relator.equals("IsSubClassOf")) relator = "subclass";
                    if (relator.equals("IsSubRelatorOf")) relator = "subrelation";
                    if (relator.equals("IsReciprocalOf")) relator = "inverse";            
                    cell = (String) row.get(24);                                // column Y
                    if (cell.indexOf("^") > -1) 
                        range = cell.substring(cell.indexOf("^") + 1, cell.length());
                    else 
                        range = cell;
                    range = StringToKIFid(range);
                    if (domain != null && domain != "" && domain.length() > 0)
                        System.out.println("(" + relator + " " + domain + " " + range + ")");            
                    if (row.size() > 29) {
                        String cardinality = (String) row.get(29);              // column AD
                        if (!emptyString(cardinality) && !emptyString(domain)) 
                            System.out.println("(cardinality " + domain + " " + cardinality);                                                
                        if ((cardinality.equals("0-1") || cardinality.equals("1")) && !emptyString(domain))
                            System.out.println("(instance " + domain + " SingleValuedRelation)");                        
                        if (row.size() > 38) {
                            cell = (String) row.get(38);                        // column AM
                            if (cell.indexOf("^") > -1) 
                                relator = cell.substring(cell.indexOf("^") + 1, cell.length());
                            else 
                                relator = cell;
                            relator = StringToKIFid(relator);
                            cell = (String) row.get(39);                        // column AN
                            if (cell.indexOf("^") > -1) 
                                range = cell.substring(cell.indexOf("^") + 1, cell.length());
                            else 
                                range = cell.trim();
                            range = StringToKIFid(range);
                            //System.out.println("INFO: " + range + " " + domain + " " + relator); 
                            if (!emptyString(subject) && !emptyString(range) && 
                                !emptyString(relator) && relator.equals("IsSameAs"))
                                System.out.println("(synonymousExternalConcept \"" + range + "\" " + subject + " " + namespace + ")"); 
                            if (row.size() > 40) {
                                documentation = (String) row.get(40);           // column AO
                                documentation = documentation.trim();
                                if (!emptyString(subject) && !emptyString(documentation))
                                    System.out.println(wordWrap("(documentation " + subject + " \"" + documentation + "\")",70)); 
                                if (row.size() > 41) {
                                    documentation = (String) row.get(41);       // column AP
                                    documentation = documentation.trim();
                                    if (!emptyString(subject) && !emptyString(documentation))
                                        System.out.println(wordWrap("(documentation " + subject + " \"" + documentation + "\")",70)); 
                                    if (row.size() > 42) {
                                        documentation = (String) row.get(42);   // column AQ
                                        documentation = documentation.trim();
                                        if (!emptyString(subject) && !emptyString(documentation))
                                            System.out.println(wordWrap("(documentation " + subject + " \"" + documentation + "\")",70)); 
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** ***************************************************************
     *  Collect relations in the knowledge base 
     *
     *  @return The set of relations in the knowledge base.
     */
    private ArrayList getRelations(KB kb) {

        ArrayList relations = new ArrayList();
        Iterator it = kb.terms.iterator();
        while (it.hasNext()) {
            String term = (String) it.next();
            if (kb.childOf(term,"BinaryPredicate"))
                relations.add(term.intern());            
        }
        return relations;
    }      

    /** ***************************************************************
     * Print a comma-delimited matrix.  The values of the rows
     * are TreeMaps, whose values in turn are Strings.  The ArrayList of
     * relations forms the column headers, which are Strings.
     * 
     * @param rows - the matrix
     * @param relations - the relations that form the column header
     */
    private void printSpreadsheet(TreeMap rows, ArrayList relations) {

        StringBuffer line = new StringBuffer();
        line.append("Domain/Range,");
        for (int i = 0; i < relations.size(); i++) {
            String relation = (String) relations.get(i);
            line.append(relation);
            if (i < relations.size()-1)
                line.append(",");            
        }
        System.out.println(line);

        Iterator it = rows.keySet().iterator();
        while (it.hasNext()) {
            String term = (String) it.next();
            TreeMap row = (TreeMap) rows.get(term);
            System.out.print(term + ",");

            for (int i = 0; i < relations.size(); i++) {
                String relation = (String) relations.get(i);
                if (row.get(relation) == null) 
                    System.out.print(",");
                else {
                    System.out.print((String) row.get(relation));
                    if (i < relations.size()-1)
                        System.out.print(",");            
                }
                if (i == relations.size()-1)
                    System.out.println();
            }
        }
    }      

    /** ***************************************************************
     * Export a comma-delimited table of all the ground binary
     * statements in the knowledge base.  Only the relations that are
     * actually used are included in the header.
     *
     *  @param The knowledge base.
     */
    public void exportTable(KB kb) {

        ArrayList relations = getRelations(kb);
        ArrayList usedRelations = new ArrayList();
        TreeMap rows = new TreeMap();
        for (int i = 0; i < relations.size(); i++) {
            String term = (String) relations.get(i);
            ArrayList statements = kb.ask("arg",0,term);
            if (statements != null) {
                TreeMap row = new TreeMap();
                for (int j = 0; j < statements.size(); j++) {
                    Formula f = (Formula) statements.get(j);
                    String arg1 = f.getArgument(1);
                    if (Character.isUpperCase(arg1.charAt(0)) && !arg1.endsWith("Fn")) {
                        if (!usedRelations.contains(term)) 
                            usedRelations.add(term);
                        String arg2 = f.getArgument(2);
                        if (rows.get(f.getArgument(1)) == null) {
                            row = new TreeMap();                    
                            rows.put(arg1,row);
                        }
                        else 
                            row = (TreeMap) rows.get(arg1); 
                        if (row.get(term) == null) 
                            row.put(term,f.getArgument(2));
                        else {
                            String element = (String) row.get(term);
                            element = element + "/" + f.getArgument(2);
                            row.put(term,element);
                        }
                    }
                }
            }
        }
        printSpreadsheet(rows,usedRelations);
    }      

    /** *************************************************************
     * A test method.
     */
    public static void main (String args[]) {

    /*
        DB db = new DB();
        try {
            KBmanager.getMgr().initializeOnce();
        } catch (IOException ioe ) {
            System.out.println(ioe.getMessage());
        }
        KB kb = KBmanager.getMgr().getKB("SUMO");
        db.exportTable(kb);
        */

        //ArrayList al = readSpreadsheet("scowCCLI.csv");
        ArrayList al = readSpreadsheet("scowCCLI.csv");
        processSpreadsheet(al);

        //ArrayList al = readSpreadsheet("CCLI-SPISampleData.csv");
        //processForRDFExport(al);
    }
}
