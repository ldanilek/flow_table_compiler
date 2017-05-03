// written by Lee
// interface for an optimization to be determined at runtime

// constructor should take no arguments
public interface PlugInOptimization {
    // optimizes the flow tables.
    // returns boolean indicating whether progress was made.
    public boolean optimize(OptimizableFlowTables ofts);
}
