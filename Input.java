/*
 * Author: Tyler
 *
 * Helper functions to get user input in REPL loop and from command line
 */

import java.util.ArrayList;
import java.util.Scanner;
import java.lang.reflect.Constructor;

public class Input {

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

            toLoad = scanner.nextInt();
        }

        if(toLoad != trees.length)
            return trees[toLoad];
        else{
            System.out.println("Enter the name of a parse tree");
            return loadTree(scanner.next());
        }
    }

    public static int getSwitch(){
        System.out.println("Which switch should the program run on?");
        return (new Scanner(System.in)).nextInt();
    }
}
