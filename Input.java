/*
 * Author: Tyler
 *
 * Helper functions to get user input in REPL loop and from command line
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.lang.reflect.Constructor;

public class Input {

    private static final String prompt =  "  >> ";

    private Input(){}

    private static Statement loadTree(String name){

        try{
            Class optClass = Class.forName(name);
            Constructor optConstructor = optClass.getConstructor();
            Object opt = optConstructor.newInstance();

            if(opt instanceof Statement){
                return (Statement) opt;
            } else{
                System.err.println(name+" must extend Statement");
                System.exit(-1);
            }
        } catch(Exception e){
            System.err.println("ERROR: Can't load parse tree "+name);
            System.exit(-1);
        }

        System.err.println("ERROR: Program should not reach here!");
        return null;
    }

    public static ArrayList<PlugInOptimization> getOptimizations(String[] names){
        ArrayList<PlugInOptimization> optimizations = new ArrayList();

        for (String name : names) {
            System.out.println("Loading Optimizer '"+name+"'");

            // use reflection to load the optimizer
            try {
                Class optClass = Class.forName(name);
                Constructor optConstructor = optClass.getConstructor();
                Object opt = optConstructor.newInstance();

                // check that optimizer responds to the method
                if (opt instanceof PlugInOptimization) {
                    optimizations.add((PlugInOptimization)opt);
                } else {
                    System.err.println("ERROR: OPTIMIZER DOESN'T IMPLEMENT INTERFACE");
                }
            } catch (Exception ex) {
                System.err.println("ERROR: CAN'T LOAD OPTIMIZER "+name+" due to exception: "+ex);
            }
        }
        return optimizations;
    }

    public static int ipAdrToInt(String ipstring){
        // Parse IP parts into an int array
        int[] ip = new int[4];
        String[] parts = ipstring.split("\\.");

        for (int i = 0; i < 4; i++) {
            ip[i] = Integer.parseInt(parts[i]);
        }

        int ipNumbers = 0;
        for (int i = 0; i < 4; i++) {
            ipNumbers += ip[i] << (24 - (8 * i));
        }

        return ipNumbers;
    }

    public static Statement getParseTree(){
        Scanner scanner = new Scanner(System.in);

        Statement[] trees = {
            Stubs.tree,
            Stubs.simpleTree,
            Stubs.simpleSequence,
            Stubs.hashLookup,
            Stubs.switchLookup,
            Stubs.assignTree,
            Stubs.ded,
            Stubs.liv
        };

        String[] descriptions = {
            "Simple tree with several logic ops, a drop action, and a forward action",
            "Drop()",
            "DecrementTTL(), Forward();",
            "Simple hash lookup tree",
            "Switch lookup tree",
            "Tree with lots of assignments",
            "Dead variable test",
            "Live variable test"
        };

        int toLoad = -1;

        while(toLoad < 0 || toLoad > trees.length){
            System.out.println("Select a parse tree to run");

            for(int i = 0; i < trees.length; i++){
                System.out.println("\t"+i+": "+descriptions[i]);
            }

            System.out.println("\t"+trees.length+": Load via reflection");

            System.out.print(prompt);
            toLoad = scanner.nextInt();
        }

        if(toLoad != trees.length)
            return trees[toLoad];
        else{
            System.out.println("Enter the name of a parse tree");
            System.out.print(prompt);
            return loadTree(scanner.next());
        }
    }

    public static int getSwitch(){
        System.out.println("Which switch should the program run on?");
        System.out.print(prompt);
        return (new Scanner(System.in)).nextInt();
    }

    private static boolean validateInt(String s){
        try{
            Integer.parseInt(s);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    private static boolean validateIp(String ipstring){
        // Parse IP parts into an int array
        int[] ip = new int[4];
        String[] parts = ipstring.split("\\.");

        if(parts.length != 4)
            return false;

        for (int i = 0; i < 4; i++) {
            if(!validateInt(parts[i]))
                return false;
            ip[i] = Integer.parseInt(parts[i]);

            if(ip[i] < 0 || ip[i] > 255)
                return false;
        }

        return true;
    }

    public static HashMap<PacketField, Integer> getPacket(){
        System.out.println("Enter packet values using the following format:");
        System.out.println("\tfield=val");
        System.out.println("Where field is: ");
        for(PacketField field : PacketField.values()){
            System.out.println("\t"+field.name());
        }
        System.out.println("To send the packet, enter \".\"on a new line");

        String input = null;
        Scanner scanner = new Scanner(System.in);
        Boolean valCheck = false;

        HashMap<PacketField, Integer> toReturn = new HashMap<PacketField, Integer>();

        while(true){
            System.out.print(prompt);
            input = scanner.next();

            if(input.equals(".")) {
              if (valCheck == true) break;
              else System.out.println("Packet must have 1 or more values.");
            }

            String [] tokens = input.split("=");

            if(tokens.length != 2){
                System.out.println("Invalid format!");
                continue;
            }

            switch(PacketField.valueOf(tokens[0])){
                case IPv4Src:
                case IPv4Dst:
                    if(!validateIp(tokens[1])){
                        System.out.println("Invalid format!");
                        continue;
                    }
                    toReturn.put(PacketField.valueOf(tokens[0]), ipAdrToInt(tokens[1]));
                    break;
                case EthernetSrc:
                case EthernetDst:
                case InPort:
                case OutPort:
                case TTL:
                    if(!validateInt(tokens[1])){
                        System.out.println("Invalid format!");
                        continue;
                    }
                    toReturn.put(PacketField.valueOf(tokens[0]), Integer.parseInt(tokens[1]));
                    valCheck = true;
                    break;
                default:
                    System.out.println("Invalid field val: <"+tokens[0]+">");
                    continue;
            }
        }

        return toReturn;
    }
}
