package mip;

public class Bound {
    private int varIndex;
    private boolean isContinuous;
    private double upper;
    private double lower;

    public Bound(int varIndex, boolean isContinuous) {
        this.varIndex = varIndex;
        this.isContinuous = isContinuous;
        this.upper = Double.MAX_VALUE;
        this.lower = -Double.MAX_VALUE;
    }

    public Bound(int varIndex, double lower, double upper) {
        this.varIndex = varIndex;
        this.isContinuous = true;
        this.upper = upper;
        this.lower = lower;
    }

    public Bound(int varIndex, boolean isContinuous, double lower, double upper) {
        this.varIndex = varIndex;
        this.isContinuous = isContinuous;
        this.upper = upper;
        this.lower = lower;
    }

    public int getVarIndex() {
        return varIndex;
    }

    public boolean isContinuous() {
        return isContinuous;
    }

    public double[] getInterval() {
        double[] result = { lower, upper };
        return result;
    }

    public Bound and(Bound that) {
        if (this.varIndex != that.varIndex)
            throw new IllegalArgumentException();
        return new Bound(this.varIndex, this.isContinuous & that.isContinuous, Math.max(this.lower, that.lower),
                Math.min(this.upper, that.upper));
    }
}
