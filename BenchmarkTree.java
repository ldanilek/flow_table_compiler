/*
 * Author: Tyler
 *
 * Test runtime loading of parse tree
 */

import java.util.ArrayList;

public class BenchmarkTree extends Statement{

    private final Statement toEval;

    public long total = 0;

    public BenchmarkTree(int size){
        toEval = generateStmTree(size);
    }

    private Statement copyTree(Sequence s){
        Statement l = null;
        Statement r = null;
        
        if(s.stm1 instanceof Sequence)
            l = copyTree((Sequence) s.stm1);
        else
            l = new DecrementTTL();
        
        if(s.stm2 instanceof Sequence)
            r = copyTree((Sequence) s.stm2);
        else
            r = new DecrementTTL();

        return new Sequence(l, r);
    }
    
    private Statement generateStmTree(int size){
        Sequence toReturn = new Sequence(new DecrementTTL(), new DecrementTTL());

        long total = 3;
        
        while(total < size){
            toReturn = new Sequence(toReturn, copyTree(toReturn));
            total = total * 2 + 1;
        }

        this.total = total;
        return toReturn;
    }

    @Override
    public ArrayList<FlowTable> asFlowTables(Integer jumpIndex) {
        return toEval.asFlowTables(jumpIndex);
    }
}

