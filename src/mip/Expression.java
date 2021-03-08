package mip;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Expression {
    private HashMap<String, Integer> varMap;
    private int varNum;
    private TreeMap<Integer, Double> coef;
    private double constant;
    private int relation;
    private double[] coefficient;
    private int[] varIndex;

    public Expression(String line) {
        this.constant = 0;
        this.coef = new TreeMap<>();
        this.relation = -2;

        this.varMap = new HashMap<>();
        this.varNum = 0;

        readLine(line);
    }

    public Expression(String line, HashMap<String, Integer> varMap) {
        this.constant = 0;
        this.coef = new TreeMap<>();
        this.relation = -2;

        this.varMap = varMap;
        this.varNum = varMap.size();

        readLine(line);
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, Integer> getVarMap() {
        return (HashMap<String, Integer>) varMap.clone();
    }

    public double[] getCoefficient() {
        return Arrays.copyOf(coefficient, coefficient.length);
    }

    public int[] getVarIndex() {
        return Arrays.copyOf(varIndex, varIndex.length);
    }

    public double getConstant() {
        return constant;
    }

    public int getRelation() {
        if (relation == 2)
            throw new IllegalArgumentException("No relation for Objective");
        return relation;
    }

    private void readLine(String line) {
        boolean isCoef = true;
        int leftSide = 1;
        int coefSignal = 1;
        StringBuilder coefDigit = new StringBuilder();
        boolean end = false;
        StringBuilder s = new StringBuilder();
        for (int cusor = 0; cusor < line.length(); cusor++) {
            char chr = line.charAt(cusor);
            if (chr == ' ')
                continue;
            if (chr == '*' || chr == '/')
                throw new IllegalArgumentException("Not support * or /");

            if (isSignal(chr)) {
                if (!isCoef) {
                    isCoef = true;
                    end = true;
                }
            } else if (isAlpha(chr))
                isCoef = false;
            else if (isDigit(chr))
                ;
            else
                throw new IllegalArgumentException("Unknown character in the expression!");

            if (end) {
                updateCoef(s, coefSignal, coefDigit.toString(), leftSide);
                coefDigit = new StringBuilder();
                coefSignal = 1;
                s = new StringBuilder();
                end = false;
            }
            if (isCoef) {
                if (chr == '-')
                    coefSignal *= -1;
                else if (isDigit(chr))
                    coefDigit.append(chr);
                else if (relation == -2 && (chr == '=' || chr == '<' || chr == '>')) {
                    leftSide = -1;
                    if (chr == '<')
                        relation = -1;
                    else if (chr == '>')
                        relation = 1;
                    else
                        relation = 0;
                }
            } else {
                s.append(chr);
            }
        }
        updateCoef(s, coefSignal, coefDigit.toString(), leftSide);
        this.coefficient = new double[coef.size()];
        this.varIndex = new int[coef.size()];
        int i = 0;
        for (Map.Entry<Integer, Double> entry : coef.entrySet()) {
            coefficient[i] = entry.getValue();
            varIndex[i] = entry.getKey();
            i++;
        }
    }

    private void updateCoef(StringBuilder s, int coefSignal, String coefDigit, int leftSide) {
        if (s.length() == 0 && coefDigit.length() == 0)
            throw new IllegalArgumentException();
        double coefVal = coefSignal;
        if (coefDigit.length() != 0)
            coefVal *= Double.parseDouble(coefDigit.toString());

        if (s.length() == 0) {
            constant += (coefVal * leftSide);
            return;
        }

        String varName = s.toString();
        if (!varMap.containsKey(varName)) {
            varMap.put(varName, varNum);
            varNum++;
        }
        int index = varMap.get(varName);
        if (!coef.containsKey(index)) {
            coef.put(index, 0.0);
        }
        coef.put(index, coef.get(index) + leftSide * coefVal);
    }

    private boolean isAlpha(char chr) {
        if ((chr >= 'A' && chr <= 'Z') || (chr >= 'a' && chr <= 'z'))
            return true;
        return false;
    }

    private boolean isDigit(char chr) {
        if ((chr >= '0' && chr <= '9') || chr == '.')
            return true;
        return false;
    }

    private boolean isSignal(char chr) {
        if (chr == '-' || chr == '+' || chr == '=' || chr == '<' || chr == '>')
            return true;
        return false;
    }

}
