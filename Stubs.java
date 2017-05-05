/*
 * Author: Tyler/Lee
 *
 * Some sample parse trees to use
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Stubs{
    public static final Statement tree;
    public static final Statement simpleTree;
    public static final Statement simpleSequence;
    public static final Statement hashLookup;
    public static final Statement switchLookup;
    public static final Statement assignTree;
    public static final Statement ded;
    public static final Statement liv;

    public static final HashMap<PacketField, Integer> packet;
    
    static {
        packet = new HashMap();
        packet.put(PacketField.InPort, 20);
        
        tree =
            new IF(
                new Logic(LogicOperation.AND,
                          new Compare(CompareOperation.GE,
                                      new PacketValue(PacketField.InPort),
                                                      21),
                          new Compare(CompareOperation.GE,
                                      new PacketValue(PacketField.InPort),
                                                      21)),
                new Drop(),
                new Sequence(new DecrementTTL(), new Forward()));
        
        simpleTree = new Drop();
        simpleSequence = new Sequence(new DecrementTTL(), new Forward());
        
        HashMap<Integer, Integer> hashMap = new HashMap();
        hashMap.put(80, 90);
        hashMap.put(90, 100);
        hashMap.put(1024, 0);
        hashMap.put(2, 124);
        hashLookup = new AssignField(PacketField.OutPort, new LookUp(hashMap, new PacketValue(PacketField.InPort)));
        
        switchLookup = new AssignField(PacketField.OutPort, new LookUp(hashMap, new Switch()));
        
        assignTree = 
            new Sequence(
                new IF(
                    new Compare(CompareOperation.EQ,
                        new PacketValue(PacketField.InPort), 80),
                    new Assign("c", new Constant(2)),
                    new Assign("c", new Constant(3))),
                new IF(
                    new Compare(CompareOperation.EQ, new Variable("c"), 3),
                    new Forward(),
                    new Drop()));
        ded = new Sequence(new Assign("x", new Constant(80)), new AssignField(PacketField.InPort, new Variable("x")));
        liv = new Sequence(new Sequence(new Assign("x", new Constant(80)), new AssignField(PacketField.InPort, new Variable("x"))),
                            new IF(new Compare(CompareOperation.EQ, new Variable("x"), 80), new Drop(), new Forward()));

    }
}
