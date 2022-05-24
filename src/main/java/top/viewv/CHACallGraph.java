package top.viewv;

import soot.*;
import soot.jimple.*;

import java.util.*;

public class CHACallGraph {
    private SootMethod entryMethod;

    public CHACallGraph(SootMethod entryMethod) {
        this.entryMethod = entryMethod;
        this.analyze();
    }

    private void analyze() {
        Queue<SootMethod> workList = new LinkedList<>();
        workList.add(entryMethod);

        List<SootMethod> reachableMethods = new ArrayList<>();
        HashMap<Unit, HashSet<SootMethod>> callGraph = new HashMap<>();

        while (!workList.isEmpty()) {
            SootMethod method = workList.poll();
            if (!reachableMethods.contains(method)) {
                reachableMethods.add(method);
                if (method.hasActiveBody()){
                    Body body = method.retrieveActiveBody();
                    for (Unit unit : body.getUnits()) {
                        if (unit instanceof InvokeStmt) {
                            InvokeStmt invokeStmt = (InvokeStmt) unit;
                            HashSet<SootMethod> callees = resolve(invokeStmt);
                            for (SootMethod callee : callees) {
                                if (!callGraph.containsKey(unit)) {
                                    callGraph.put(unit, new HashSet<>());
                                }
                                callGraph.get(unit).add(callee);
                                if (!reachableMethods.contains(callee)) {
                                    workList.add(callee);
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Reachable Methods: " + reachableMethods);
        System.out.println("Call Graph: " + callGraph);
    }

    private HashSet<SootMethod> resolve(InvokeStmt invokeStmt) {
        HashSet<SootMethod> result = new HashSet<>();
        InvokeExpr invokeExpr = invokeStmt.getInvokeExpr();
        SootMethod targetMethod = invokeExpr.getMethod();
        if (invokeExpr instanceof StaticInvokeExpr){
            result.add(targetMethod);
        }else if (invokeExpr instanceof SpecialInvokeExpr){
            SootClass targetClass = targetMethod.getDeclaringClass();
            SootMethod dispatchMethod = dispatch(targetClass, targetMethod);
            result.add(dispatchMethod);
        }else if (invokeExpr instanceof VirtualInvokeExpr){
            SootClass targetClass = targetMethod.getDeclaringClass();
            while (targetClass.hasSuperclass()){
                targetClass = targetClass.getSuperclass();
            }
            HashSet<SootClass> subClasses = new HashSet<>();
            subClasses.add(targetClass);
            subClasses.addAll(Scene.v().getActiveHierarchy().getSubclassesOfIncluding(targetClass));
            for (SootClass subClass : subClasses) {
                SootMethod dispatchMethod = dispatch(subClass, targetMethod);
                result.add(dispatchMethod);
            }
        }else if (invokeExpr instanceof InterfaceInvokeExpr){
            SootClass targetClass = targetMethod.getDeclaringClass();
            HashSet<SootClass> subClasses = new HashSet<>();
            subClasses.addAll(Scene.v().getFastHierarchy().getAllImplementersOfInterface(targetClass));
            for (SootClass subClass : subClasses) {
                SootMethod dispatchMethod = dispatch(subClass, targetMethod);
                result.add(dispatchMethod);
            }
        }
        return result;
    }

    private SootMethod dispatch(SootClass initClass, SootMethod initMethod) {
        SootMethod dispatchMethod = initClass.getMethodUnsafe(initMethod.getSubSignature());
        if (dispatchMethod == null || !dispatchMethod.getDeclaringClass().isApplicationClass()) {
            return initMethod;
        }else if (dispatchMethod.isAbstract() || !testEqualMethod(dispatchMethod, initMethod)) {
            return dispatch(initClass.getSuperclass(), initMethod);
        }else {
            return dispatchMethod;
        }
    }

    private Boolean testEqualMethod(SootMethod method1, SootMethod method2) {
        if (Objects.equals(method1.getName(), method2.getName()) && method1.getParameterCount() == method2.getParameterCount()) {
            for (int i = 0; i < method1.getParameterCount(); i++) {
                if (!Objects.equals(method1.getParameterType(i), method2.getParameterType(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
