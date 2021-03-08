package mip;

import java.util.ArrayList;
import java.util.Collections;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

public class MIP {
    private double Zmax;
    private double[] para;
    private boolean[] isContinuous; // 1 for continuous, 0 for integer
    private IloNumVar[] var;
    private ArrayList<Integer> randomSeq;

    public MIP(int n, Objective obj, Iterable<Constraint> constraints, Iterable<Bound> bounds) {
        this.para = new double[n];
        this.isContinuous = new boolean[n];
        this.randomSeq = new ArrayList<>();
        for (int i = 0; i < n; i++)
            randomSeq.add(i);
        Collections.shuffle(randomSeq);
        try {
            IloCplex model = new IloCplex();
            model.setOut(null);
            this.var = new IloNumVar[n];
            for (Bound a : bounds) {
                var[a.getVarIndex()] = model.numVar(a.getInterval()[0], a.getInterval()[1], IloNumVarType.Float);
                isContinuous[a.getVarIndex()] = a.isContinuous();
            }
            IloLinearNumExpr expr = model.linearNumExpr();
            for (Term term : obj)
                expr.addTerm(term.getCoefficient(), var[term.getVarIndex()]);
            model.addMaximize(expr);
            for (Constraint constraint : constraints) {
                expr = model.linearNumExpr();
                for (Term term : constraint)
                    expr.addTerm(term.getCoefficient(), var[term.getVarIndex()]);
                if (constraint.getRelation() == -1)
                    model.addLe(expr, constraint.getB());
                else if (constraint.getRelation() == 1)
                    model.addGe(expr, constraint.getB());
                else
                    model.addEq(expr, constraint.getB());
            }
            this.Zmax = -Double.MAX_VALUE;
            solveModel(model, 0);
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private String showPara() {
        StringBuilder s = new StringBuilder();
        s.append('[');
        for (double d : para) {
            s.append(d);
            s.append(',');
        }
        s.append(']');
        return s.toString();
    }

    public MIP(Data data) {
        this(data.getVarMap().size(), data.getObjective(), data.getConstraints(), data.getBounds());
    }

    public void solveModel(IloCplex model, int layer) {
        try {
            System.out.println("层数：" + layer);
            model.exportModel("lastModel.lp");
            if (!model.solve()) {
                // test 2 无可行解
                System.out.println("无解");
                return;
            }

//            System.out.println("Z:" + model.getObjValue() + ";Para:" + showPara());
//            System.out.println("Zmax:" + Zmax);
            if (model.getObjValue() <= Zmax) {
                System.out.println("最好都很差");
                // test1 最好情况还要差
                return;
            }

            double[] val = model.getValues(var);
            Collections.shuffle(randomSeq);
            Integer[] temp = new Integer[1];
            temp = randomSeq.toArray(temp);
            for (int j = 0; j < val.length; j++) {
                int i = temp[j];
                if ((!isContinuous[i]) && (int) val[i] != val[i]) {
//                    System.out.println("X" + i + ">=" + Math.ceil(val[i]));
                    updateModel(model, model.addGe(model.prod(1, var[i]), Math.ceil(val[i])), layer);
//                    System.out.println("X" + i + "<=" + Math.floor(val[i]));
                    updateModel(model, model.addLe(model.prod(1, var[i]), Math.floor(val[i])), layer);
                    return;
                }
            }

            // test3 为整数解
            para = val;
            Zmax = model.getObjValue();
            System.out.println("找到整数解了");
            return;
        } catch (IloException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void updateModel(IloCplex model, IloRange newConstraint, int layer) {
        try {
            solveModel(model, layer + 1);
            model.remove(newConstraint);
        } catch (IloException e) {
            e.printStackTrace();
        }
        return;
    }

    public double[] getPara() {
        return para;
    }

    public double getObjValue() {
        return Zmax;
    }

    public static void main(String[] args) {
        MIP mip = new MIP(new Data("SpringGarden.lp"));
        System.out.println(mip.Zmax);
        System.out.println(mip.showPara());
    }
}
