package mip;

import java.util.Iterator;

public class Objective implements Iterable<Term> {
    private int termNum;
    private Term[] terms;
    private double constant;

    // default maximize
    public Objective(double[] coefficient, int[] varIndex, double constant) {
        termNum = coefficient.length;
        this.terms = new Term[termNum];
        for (int i = 0; i < termNum; i++)
            this.terms[i] = new Term(varIndex[i], coefficient[i]);
        this.constant = constant;
    }

    public Iterator<Term> iterator() {
        // TODO Auto-generated method stub
        return new OIterator();
    }

    private class OIterator implements Iterator<Term> {
        private int cusor;

        @Override
        public boolean hasNext() {
            // TODO Auto-generated method stub
            if (cusor < termNum)
                return true;
            else
                return false;
        }

        @Override
        public Term next() {
            // TODO Auto-generated method stub

            return terms[cusor++];
        }

    }
}
