package mip;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Data {
    private Objective obj;
    private Stack<Constraint> constraints;
    private HashMap<Integer, Bound> bounds;
    private HashMap<String, Integer> varMap;

    public Data(String fileName) {
        File file = new File(fileName);
        constraints = new Stack<>();
        bounds = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            int keyword = 0;
            int part = 0; // 1:Maximize, 2:Minimize, 3:Constraint, 4:Bound, 5:General, 6:Binary, -1:end
            while ((line = reader.readLine()) != null) {
                if (line.length() == 0)
                    continue;
                keyword = keywordDetect(line);
                if (keyword != 0) {
                    part = keyword;
                    continue;
                }
                switch (part) {
                case 1:
                    obj = buildMaximize(line);
                    break;
                case 2:
                    obj = buildMinimize(line);
                    break;
                case 3:
                    constraints.add(buildConstraint(line));
                    break;
                case 4:
                    addBound(line);
                    break;
                case 5:
                    addGeneral(line);
                    break;
                case 6:
                    addBinary(line);
                    break;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Objective getObjective() {
        return obj;
    }

    public Iterable<Constraint> getConstraints() {
        return constraints;
    }

    public Iterable<Bound> getBounds() {
        return bounds.values();
    }

    public Map<String, Integer> getVarMap() {
        return varMap;
    }

    private Objective buildMaximize(String line) {
        Expression expr = new Expression(line);
        this.varMap = expr.getVarMap();

        return new Objective(expr.getCoefficient(), expr.getVarIndex(), expr.getConstant());
    }

    private Objective buildMinimize(String line) {
        // 不管Object最后的value对不对。
        Expression expr = new Expression(line);
        this.varMap = expr.getVarMap();
        double[] coef = expr.getCoefficient();
        for (int i = 0; i < coef.length; i++) {
            coef[i] = -coef[i];
        }
        return new Objective(coef, expr.getVarIndex(), expr.getConstant());
    }

    private Constraint buildConstraint(String line) {
        Expression expr = new Expression(line, varMap);
        this.varMap = expr.getVarMap();
        return new Constraint(-expr.getConstant(), expr.getRelation(), expr.getCoefficient(), expr.getVarIndex());
    }

    private void addBound(String line) {
        Bound b = buildBound(line);
        int varIndex = b.getVarIndex();
        if (bounds.containsKey(varIndex))
            bounds.put(varIndex, bounds.get(varIndex).and(b));
        else
            bounds.put(varIndex, b);
    }

    private Bound buildBound(String line) {
        ArrayList<String> lst = new ArrayList<>();
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char chr = line.charAt(i);
            if (chr == ' ' || chr == '=')
                continue;
            if (chr == '<' || chr == '>') {
                lst.add(s.toString());
                s = new StringBuilder();
                char[] temp = new char[1];
                temp[0] = chr;
                lst.add(new String(temp));
            } else
                s.append(chr);
        }
        lst.add(s.toString());
        Double lower = -Double.MAX_VALUE;
        Double upper = Double.MAX_VALUE;
        if (lst.size() == 3) {
            String part1 = lst.get(0);
            String part2 = lst.get(2);
            if (isAlpha(part1.charAt(0))) {
                if (lst.get(1).equals("<"))
                    return new Bound(varMap.get(part1), true, lower, Double.parseDouble(part2));
                else
                    return new Bound(varMap.get(part1), true, Double.parseDouble(part2), upper);
            } else {
                if (lst.get(1).equals("<"))
                    return new Bound(varMap.get(part2), true, Double.parseDouble(part1), upper);
                else
                    return new Bound(varMap.get(part2), true, lower, Double.parseDouble(part1));
            }
        } else if (lst.size() == 5) {
            if (!lst.get(1).equals(lst.get(3)))
                throw new IllegalArgumentException("error:连续不等关系符号不同");
            if (lst.get(1).equals("<"))
                return new Bound(varMap.get(lst.get(2)), true, Double.parseDouble(lst.get(0)),
                        Double.parseDouble(lst.get(4)));
            else
                return new Bound(varMap.get(lst.get(2)), true, Double.parseDouble(lst.get(4)),
                        Double.parseDouble(lst.get(0)));
        } else
            throw new IllegalArgumentException("error:区间关系项数过多");
    }

    private void addGeneral(String line) {
        String[] vars = line.split(" ");
        for (String v : vars) {
            int varIndex = varMap.get(v);
            Bound b = new Bound(varIndex, false);
            if (bounds.containsKey(varIndex))
                bounds.put(varIndex, bounds.get(varIndex).and(b));
            else
                bounds.put(varIndex, b);
        }
    }

    private void addBinary(String line) {
        String[] vars = line.split(" ");
        for (String v : vars) {
            int varIndex = varMap.get(v);
            Bound b = new Bound(varIndex, false, 0, 1);
            if (bounds.containsKey(varIndex))
                bounds.put(varIndex, bounds.get(varIndex).and(b));
            else
                bounds.put(varIndex, b);
        }
    }

    private boolean isAlpha(char chr) {
        if ((chr >= 'A' && chr <= 'Z') || (chr >= 'a' && chr <= 'z'))
            return true;
        return false;
    }

    private int keywordDetect(String keyword) {
        if (keyword.length() > 10)
            return 0;
        String[] max = { "maximize", "max" };
        String[] min = { "minimize", "min" };
        String[] constraint = { "constraint", "subject to" };
        String[] bound = { "bounds", "bound" };
        String[] general = { "general" };
        String[] binary = { "binary", "bin" };
        for (String a : max)
            if (keyword.equalsIgnoreCase(a))
                return 1;
        for (String a : min)
            if (keyword.equalsIgnoreCase(a))
                return 2;
        for (String a : constraint)
            if (keyword.equalsIgnoreCase(a))
                return 3;
        for (String a : bound)
            if (keyword.equalsIgnoreCase(a))
                return 4;
        for (String a : general)
            if (keyword.equalsIgnoreCase(a))
                return 5;
        for (String a : binary)
            if (keyword.equalsIgnoreCase(a))
                return 6;
        if (keyword.equalsIgnoreCase("end"))
            return -1;

        return 0;
    }

    public static void main(String[] args) {
        Data data = new Data("test.lp");
        ;
    }
}
