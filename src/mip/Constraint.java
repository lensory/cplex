package mip;

import java.util.Iterator;

public class Constraint implements Iterable<Term> {
    private double b;
    private int relation; // -1:le, 0:eq, 1:ge
    private Term[] terms;
    private int termNum;

    public Constraint(double b, int relation, double[] coefficient, int[] varIndex) {
        termNum = coefficient.length;
        this.b = b;
        this.relation = relation;
        this.terms = new Term[termNum];
        for (int i = 0; i < termNum; i++)
            this.terms[i] = new Term(varIndex[i], coefficient[i]);
    }

    public double getB() {
        return b;
    }

    public int getRelation() {
        return relation;
    }

    @Override
    public Iterator<Term> iterator() {
        // TODO Auto-generated method stub
        return new CIterator();
    }

    private class CIterator implements Iterator<Term> {
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
