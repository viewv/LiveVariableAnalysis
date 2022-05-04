package top.viewv;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.stat.TableStat;
import soot.*;
import soot.jimple.Stmt;

import java.util.HashSet;
import java.util.List;

public class SQLAnalysis {
    public String sourceDirectory;
    public String clsName;

    public SQLAnalysis(String sourceDirectory, String clsName) {
        this.sourceDirectory = sourceDirectory;
        this.clsName = clsName;
        SootSetup.initSoot(sourceDirectory, clsName);
    }

    public void analysis() {
        SootClass sc = Scene.v().getSootClass(clsName);
        HashSet<String> sqlSet = new HashSet<>();
        for (SootMethod method : sc.getMethods()) {
            if (!"<init>".equals(method.getName())) {
                System.out.println(method.getName());
                Body body = method.retrieveActiveBody();
                for (Unit unit : body.getUnits()) {
                    Stmt stmt = (Stmt) unit;
                    if (stmt.containsInvokeExpr()) {
                        SootMethod invokedMethod = stmt.getInvokeExpr().getMethod();
                        sqlSet.add(invokedMethod.getSignature());
//                        System.out.println(invokedMethod.getSignature());
//                        List<Value> values = stmt.getInvokeExpr().getArgs();
//                        System.out.println(values);
                    }
                }
            }
        }
        for (String sql : sqlSet) {
            System.out.println(sql);
        }

        String sql = "SELECT * FROM TABLE1 WHERE ID < 100";
        SQLStatementParser parser = new SQLStatementParser(sql);
        SQLStatement statement = parser.parseStatement();
        MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
        statement.accept(visitor);
        List<TableStat.Condition> conditions = visitor.getConditions();
        visitor.getColumns();
        visitor.getTables();
        System.out.println(visitor.getTables().keySet());
        for (TableStat.Condition condition : conditions) {
            System.out.println(condition.getColumn());
            System.out.println(condition.getOperator());
            System.out.println(condition.getValues());
        }
    }
}
