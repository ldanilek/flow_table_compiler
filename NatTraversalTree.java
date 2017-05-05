import java.util.*;

public class NatTraversalTree extends Statement{

    private static int[] internal = {Input.ipAdrToInt("192.168.1.8")};
    private static int[] external = {Input.ipAdrToInt("130.132.173.57")};

    private static Map<Integer, Integer> internalToExternal = new HashMap();
    private static Map<Integer, Integer> externalToInternal = new HashMap();

    private static Statement prog;

    static{
        internalToExternal.put(internal[0], external[0]);
        externalToInternal.put(external[0], internal[0]);

        prog =
            new IF(
                new Contains(internal, new PacketValue(PacketField.IPv4Src)),
                new Sequence(
                    new AssignField(
                        PacketField.IPv4Src,
                        new LookUp(
                            internalToExternal,
                            new PacketValue(PacketField.IPv4Src))),
                    new Sequence(
                        new RecomputeChecksum(),
                        new Forward())
                    ),
                new IF(
                    new Contains(external, new PacketValue(PacketField.IPv4Dst)),
                    new Sequence(
                        new AssignField(
                            PacketField.IPv4Dst,
                            new LookUp(
                                externalToInternal,
                                new PacketValue(PacketField.IPv4Dst))),
                        new Sequence(
                            new RecomputeChecksum(),
                            new Forward())
                        ),
                    new Drop()
                )
            );
    }
    
    @Override
    public ArrayList<FlowTable> asFlowTables(Integer jumpIndex) {
        return prog.asFlowTables(jumpIndex);
    }
}
