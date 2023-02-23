package org.rpl.backend.cost;
import java.io.*;
import java.util.*;
import org.javatuples.Quartet;

import org.rpl.RPLc;
import org.rpl.frontend.ast.Model;
import org.rpl.frontend.parser.Main;

public class CostAnalysis extends Main{
    public static int doMain(RPLc args) {
        CostAnalysis backend = new CostAnalysis();
        backend.arguments = args;
        int result = 0;
        try {
            result = backend.compute(args);
        } catch (Exception e) {
            System.err.println("An error occurred during cost computation:\n" + e.getMessage());
            if (backend.arguments.debug) {
                e.printStackTrace();
            }
            result = 1;
        }
        return result;
    }
    public int compute(RPLc args) throws Exception {
        PrintWriter writer = null;
        try {
            File file = new File("Cost-Analysis/Synch_Schema.txt");
            writer = new PrintWriter(file);
            System.setProperty("line.separator", System.lineSeparator());
            //System.out.println("Computation of Cost is started:");
            this.arguments = args;
            final Model model = parse(arguments.files);
            //System.out.println("Parsedd:");
            model.generate_sync_schema(null, writer);
            //System.out.println("Generated Sync Schema:");
            Set<Set<String>> sync_schema = new HashSet<Set<String>>();

            Map<String, Set<Set<String>>> sync_schema_map = new HashMap<String, Set<Set<String>>>();
            sync_schema_map = scan_merge_schema();
            //System.out.println("Merged Schema:");
            Map<String,Set<String>> I = new HashMap<String,Set<String>>();
            Map<Set<String>,String> Psi = new HashMap<Set<String>,String>();
            String o = null;
            String ta = null;
            String t = null;
            Quartet<Map<String,Set<String>>, Map<Set<String>,String>, String, String> quartet =
                new Quartet<Map<String,Set<String>>, Map<Set<String>,String>, String, String>(I, Psi, ta, t);
            Map<String,Quartet<Map<String,Set<String>>, Map<Set<String>,String>, String, String>> trans_result =
                new HashMap<String,Quartet<Map<String,Set<String>>, Map<Set<String>,String>, String, String>>();
            trans_result = model.translate(trans_result,sync_schema_map,sync_schema,null);
            store_cost(trans_result);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        return 0;
    }
    public void print_cost(Map<String,Quartet<Map<String,Set<String>>, Map<Set<String>,String>, String, String>> cost)
    {
        Iterator<String> method_name_itr = cost.keySet().iterator();
        while (method_name_itr.hasNext()) {
            String name = method_name_itr.next();
            System.out.println("*************************************************************");
            System.out.println(name+": ");
            Quartet<Map<String,Set<String>>, Map<Set<String>,String>, String, String> quartet = cost.get(name);
            // I

            System.out.print("I : { ");
            Map<String,Set<String>> map_I = quartet.getValue0();
            Iterator<String> itr_I = map_I.keySet().iterator();
            while (itr_I.hasNext()) {
                String f = itr_I.next();
                System.out.print(f + " -> ");
                Set<String> syn_set = map_I.get(f);
                Iterator<String> itr_syn_set = syn_set.iterator();
                System.out.print("{");
                if (itr_syn_set.hasNext())
                    System.out.print(itr_syn_set.next());
                while (itr_syn_set.hasNext())
                {
                    System.out.print(","+itr_syn_set.next());
                }
                System.out.print("} ");
            }
            System.out.print("}");
            System.out.println();

            // Psi

            System.out.print("Psi : { ");
            Map<Set<String>,String> map_Psi = quartet.getValue1();
            Iterator<Set<String>> itr_Psi = map_Psi.keySet().iterator();
            while (itr_Psi.hasNext())
            {
                Set<String> set_Psi = itr_Psi.next();
                String cur_cost = map_Psi.get(set_Psi);
                System.out.print("{");
                Iterator<String> syn_set_Psi_itr = set_Psi.iterator();
                if (syn_set_Psi_itr.hasNext())
                    System.out.print(syn_set_Psi_itr.next());
                while (syn_set_Psi_itr.hasNext())
                {
                    System.out.print(","+syn_set_Psi_itr.next());
                }
                System.out.print("}");
                System.out.print(" -> "+cur_cost+" ");
            }
            System.out.print("}");
            System.out.println();

            // ta

            System.out.print("ta :");
            String ta = quartet.getValue2();
            System.out.print(ta);
            System.out.println();

            // t

            System.out.print("t :");
            String t = quartet.getValue3();
            System.out.print(t);
            System.out.println();
            //System.out.println("*************************************************************");
        }
    }

public void store_cost(Map<String,Quartet<Map<String,Set<String>>, Map<Set<String>,String>, String, String>> cost) throws FileNotFoundException {
    File file = new File("Cost-Analysis/CostEquations.txt");
    PrintWriter writer = new PrintWriter(file);
    Iterator<String> method_name_itr = cost.keySet().iterator();
    while (method_name_itr.hasNext()) {
        String name = method_name_itr.next();
        writer.println("*************************************************************");
        writer.print(name+": ");
        Quartet<Map<String,Set<String>>, Map<Set<String>,String>, String, String> quartet = cost.get(name);
        // I

        writer.print("I : { ");
        Map<String,Set<String>> map_I = quartet.getValue0();
        Iterator<String> itr_I = map_I.keySet().iterator();
        while (itr_I.hasNext()) {
            String f = itr_I.next();
            writer.print(f + " -> ");
            Set<String> syn_set = map_I.get(f);
            Iterator<String> itr_syn_set = syn_set.iterator();
            writer.print("{");
            if (itr_syn_set.hasNext())
                writer.print(itr_syn_set.next());
            while (itr_syn_set.hasNext())
            {
                writer.print(","+itr_syn_set.next());
            }
            writer.print("} ");
        }
        writer.print("}");
        writer.println();

        // Psi

        writer.print("Psi : { ");
        Map<Set<String>,String> map_Psi = quartet.getValue1();
        Iterator<Set<String>> itr_Psi = map_Psi.keySet().iterator();
        while (itr_Psi.hasNext())
        {
            Set<String> set_Psi = itr_Psi.next();
            String cur_cost = map_Psi.get(set_Psi);
            writer.print("{");
            Iterator<String> syn_set_Psi_itr = set_Psi.iterator();
            if (syn_set_Psi_itr.hasNext())
                writer.print(syn_set_Psi_itr.next());
            while (syn_set_Psi_itr.hasNext())
            {
                writer.print(","+syn_set_Psi_itr.next());
            }
            writer.print("}");
            writer.print(" -> "+cur_cost+" ");
        }
        writer.print("}");
        writer.println();

        // ta

        writer.print("ta :");
        String ta = quartet.getValue2();
        writer.print(ta);
        writer.println();

        // t

        writer.print("t :");
        String t = quartet.getValue3();
        writer.print(t);
        writer.println();
        //System.out.println("*************************************************************");
    }
    writer.close();
}

    public Map<String, Set<Set<String>>> scan_merge_schema() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File("Cost-Analysis/Synch_Schema.txt"));
        String method_name = null;
        String objs = null;
        Map<String, Set<Set<String>>> sync_schema_map = new HashMap<String, Set<Set<String>>>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            //System.out.println(line);
            Scanner scanner1 = new Scanner(line);
            scanner1.useDelimiter("/");
            method_name = scanner1.next();
            Set<Set<String>> sync_schema = new HashSet<Set<String>>();
            //System.out.println(method_name);
            while (scanner1.hasNext()) {
                objs = scanner1.next();
                //System.out.println(objs);
                Scanner scanner2 = new Scanner(objs);
                scanner2.useDelimiter(",");
                Set<String> sync_set = new HashSet<String>();
                while (scanner2.hasNext()) {
                    //System.out.println(scanner2.next());
                    sync_set.add(scanner2.next());
                }
                /*Iterator<String> set_itr = sync_set.iterator();
                System.out.print("{");
                if(set_itr.hasNext())
                    System.out.print(set_itr.next());
                while (set_itr.hasNext()) {
                    System.out.print(",");
                    System.out.print(set_itr.next());
                }
                System.out.print("}");
                System.out.println();

                 */
                sync_schema = merge_schema(sync_schema, sync_set);
                scanner2.close();
            }
            sync_schema_map.put(method_name, sync_schema);
            method_name = null;
            scanner1.close();
        }
        scanner.close();
        //print_schema(sync_schema_map);
        return sync_schema_map;
    }
    public Set<Set<String>> merge_schema(Set<Set<String>> sync_schema, Set<String> sync_set){
        Set<Set<String>> temp_schema = new HashSet<Set<String>>(sync_schema);
        Set<String> union = new HashSet<String>(sync_set);
        boolean flag = true;
        if (!sync_schema.isEmpty()){
            Iterator<Set<String>> i = sync_schema.iterator();
            while (i.hasNext()) {
                Set<String> temp = new HashSet<String>(i.next());
                Set<String> intersection = new HashSet<String>(temp);
                intersection.retainAll(union);
                System.out.println();
                if (!intersection.isEmpty()) {
                    //System.out.println("intersaction is not empty");
                    flag = false;
                    temp_schema.remove(new HashSet<String>(temp));
                    union.addAll(temp);
                }
            }
            temp_schema.add(union);
        }
        if(flag == true)
        {
            //System.out.println("intersaction is empty");
            temp_schema.add(sync_set);
        }
        return temp_schema;
    }
    public void print_schema(Map<String,Set<Set<String>>> map) {
        Iterator<String> method_name_itr = map.keySet().iterator();
        while (method_name_itr.hasNext()) {
            String name = method_name_itr.next();
            System.out.print(name+": ");
            Set<Set<String>> val = map.get(name);
            Iterator<Set<String>> sync_set_itr = val.iterator();
            while (sync_set_itr.hasNext()) {
                Iterator<String> objs_itr = sync_set_itr.next().iterator();
                System.out.print("{");
                if(objs_itr.hasNext())
                    System.out.print(objs_itr.next());
                while (objs_itr.hasNext()) {
                    System.out.print(",");
                    System.out.print(objs_itr.next());
                }
                System.out.print("}");
            }
            System.out.println();
        }
    }
}
