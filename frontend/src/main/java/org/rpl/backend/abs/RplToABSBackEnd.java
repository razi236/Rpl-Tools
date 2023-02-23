package org.rpl.backend.abs;

import org.rpl.RPLc;
import org.rpl.frontend.ast.Model;
import org.rpl.frontend.parser.Main;

import java.io.*;
import java.sql.*;

public class RplToABSBackEnd extends Main {

    public static int doMain(RPLc args) {
        RplToABSBackEnd backend = new RplToABSBackEnd();
        //backend.importCSV();
        backend.arguments = args;
        int result = 0;
        try {
            result = backend.compile(args);

        } catch (Exception e) {
            System.err.println("An error occurred during compilation:\n" + e.getMessage());
            result = 1;
        }
        return result;
    }

    public Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:identifier.sqlite";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public void selectAll(){
        String sql = "SELECT * FROM Resources";

        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            // loop through the result set
            while (rs.next()) {
                System.out.println(rs.getInt("ResourceID") +  "\t" +
                    rs.getString("ResourceCategory") + "\t" +
                    rs.getInt("ResourceEfficiency"));
            }
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void importCSV(){
        String sql =
            "CREATE TABLE cities(\n" +
            "  \"name\" TEXT,\n" +
            "  \"population\" TEXT,\n" +
            "  \"id\" TEXT\n" +
            ");";
        //String sql2 = ".import Quality.csv ResourceQuality1";
        Connection conn = this.connect();
        try (
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
             conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public int compile(RPLc args) throws Exception {
        this.arguments = args;
        final Model model = parse(arguments.files);

        /*
        if (arguments.prettyprint_keepsugar) {
            model.doAACrewrite = false;
            model.doForEachRewrite = false;
        }
        if (arguments.prettyprint_keepstdlib) {
            model.doPrettyPrintStdLib = true;
        }
        if (!arguments.prettyprint_force && (model.hasParserErrors() || model.hasErrors() || model.hasTypeErrors())) {
            printErrorMessage();
            return 1;
        }
        */

        // Set the line separator to LF so the file output prints UNIX line endings on println() calls.
        System.setProperty("line.separator", "\n");
        final PrintStream stream;
        final String loc;
        if (arguments.outputfile != null) {
            stream = new PrintStream(new FileOutputStream(arguments.outputfile), false, "utf-8");
            loc = arguments.outputfile.getAbsolutePath();
        } else {
            stream = System.out;
            loc = "Standard Output Stream";
        }

        if (arguments.verbose) {
            System.out.println("Output ABS model source code to " + loc + "...");
        }

        /*PrintWriter writer = new PrintWriter(new OutputStreamWriter(stream), true);
        // Set line separator back to default value
        System.setProperty("line.separator", System.lineSeparator());
        RplToABSFormatter formatter = new DefaultABSFormatter(writer);
        model.doPrettyPrint(writer, formatter);*/

        File file = new File("RplToStaticABS/RplToABS.abs");
        PrintWriter writer = new PrintWriter(file);
        writer.print("module ABS.ResourceManager;");
        writer.println();
        writer.print("import * from ABS.StdLib;");
        writer.println();
        writer.print("export *;");
        writer.println();
        writer.print("data Quality = ");

        String sql = "SELECT * FROM ResourceQuality";
        Connection conn = this.connect();
        try (
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
             if(rs.next()){
                 writer.print(rs.getString("Quality"));
             }

            // loop through the result set
            while (rs.next()) {
                writer.print(" | ");
                writer.print(rs.getString("Quality"));
            }
            writer.print(";");
            writer.println();
            writer.println();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            writer.close();
            conn.close();
        }
        FileInputStream inputStream = new FileInputStream("frontend/src/main/resources/abs/lang/ResourceManagerForStaticABS.abs");
        FileOutputStream outputStream = new FileOutputStream(file,true);
        //PrintWriter writer = null;
        try {
            // declare variable for indexing
            int i;
            // use while loop with read() method of FileInputStream class to read bytes data from file1
            while ((i = inputStream.read()) != -1) {

                // use write() method of FileOutputStream class to write the byte data into file2
                outputStream.write(i);
            }
        }
        // catch block to handle exceptions
        catch (Exception e) {
            System.out.println("Error Found: " + e.getMessage());
        }
        // use finally to close instance of the FileInputStream and FileOutputStream classes
        finally {
            if (inputStream != null) {
                // use close() method of FileInputStream class to close the stream
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }

        BufferedWriter br = null;
        FileWriter fr = null;
        try {
            fr = new FileWriter(file,true);
            br = new BufferedWriter(fr);
            writer = new PrintWriter(br);
            System.setProperty("line.separator", System.lineSeparator());
            RplToABSFormatter formatter = new DefaultABSFormatter(writer);
            model.doABSTranslationStdLib = false;
            model.doABSTranslationResourceManager = false;
            model.doABSTranslation(writer, formatter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (br != null) {
                br.close();
            }
            if (fr != null) {
                fr.close();
            }
        }
        return 0;
    }
}
