package mip;

public class Term {
    private double coefficient;
    private int varIndex;

    public Term(int varIndex, double coefficient) {
        this.varIndex = varIndex;
        this.coefficient = coefficient;
    }

    public int getVarIndex() {
        return varIndex;
    }

    public double getCoefficient() {
        return coefficient;
    }
}
