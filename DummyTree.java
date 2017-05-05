/*
 * Author: Tyler
 *
 * Test runtime loading of parse tree
 */

import java.util.ArrayList;

public class DummyTree extends Statement{

    @Override
    public ArrayList<FlowTable> asFlowTables(Integer jumpIndex) {
        return Stubs.tree.asFlowTables(jumpIndex);
    }
}
