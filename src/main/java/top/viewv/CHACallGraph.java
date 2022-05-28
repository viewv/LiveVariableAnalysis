package top.viewv;

import soot.*;
import soot.jimple.*;

import java.util.*;

public class CHACallGraph {
    private SootMethod entryMethod;

    private Boolean enableFilter;

    private HashMap<Unit, HashSet<SootMethod>> callGraph = new HashMap<>();
    private List<SootMethod> reachableMethods = new ArrayList<>();

    private HashMap<Unit, SootMethod> unitSootMethodHashMap = new HashMap<>();

    public CHACallGraph(SootMethod entryMethod, Boolean enableFilter) {
        this.entryMethod = entryMethod;
        this.enableFilter = enableFilter;
        this.analyze();
    }

    public CHACallGraph(SootMethod entryMethod) {
        this.entryMethod = entryMethod;
        this.enableFilter = true;
        this.analyze();
    }

    public HashMap<Unit, HashSet<SootMethod>> getCallGraph() {
        return callGraph;
    }

    public List<SootMethod> getReachableMethods() {
        return reachableMethods;
    }

    public HashMap<Unit, SootMethod> getUnitSootMethodHashMap() {
        return unitSootMethodHashMap;
    }

    private void analyze() {
        Queue<SootMethod> workList = new LinkedList<>();
        workList.add(entryMethod);

        while (!workList.isEmpty()) {
            SootMethod method = workList.poll();
            if (!reachableMethods.contains(method)) {
                reachableMethods.add(method);
                try {
                    Body body = method.retrieveActiveBody();
                    for (Unit unit : body.getUnits()) {
                        if (unit instanceof InvokeStmt) {
                            InvokeStmt invokeStmt = (InvokeStmt) unit;
                            SootMethod targetMethod = invokeStmt.getInvokeExpr().getMethod();
                            if (!unitSootMethodHashMap.containsKey(unit)){
                                unitSootMethodHashMap.put(unit, method);
                            }
                            if (enableFilter && targetMethod.getDeclaringClass().isJavaLibraryClass()) {
                                if (!callGraph.containsKey(unit)) {
                                    callGraph.put(unit, new HashSet<>());
                                }
                                callGraph.get(unit).add(targetMethod);
                            }else {
                                HashSet<SootMethod> callees = resolve(invokeStmt);
                                for (SootMethod callee : callees) {
                                    if (!callGraph.containsKey(unit)) {
                                        callGraph.put(unit, new HashSet<>());
                                    }
                                    callGraph.get(unit).add(callee);
                                    workList.add(callee);
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private HashSet<SootMethod> resolve(InvokeStmt invokeStmt) {
        HashSet<SootMethod> result = new HashSet<>();
        InvokeExpr invokeExpr = invokeStmt.getInvokeExpr();
        SootMethod targetMethod = invokeExpr.getMethod();
        SootClass targetClass = targetMethod.getDeclaringClass();
        if (invokeExpr instanceof StaticInvokeExpr){
            result.add(targetMethod);
        }else if (invokeExpr instanceof SpecialInvokeExpr){
            SootMethod dispatchMethod = dispatch(targetClass, targetMethod);
            result.add(dispatchMethod);
        }else if (invokeExpr instanceof VirtualInvokeExpr){
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
            HashSet<SootClass> subClasses = new HashSet<>();
            subClasses.addAll(Scene.v().getFastHierarchy().getAllImplementersOfInterface(targetClass));
            for (SootClass subClass : subClasses) {
                SootMethod dispatchMethod = dispatch(subClass, targetMethod);
                result.add(dispatchMethod);
            }
        }else {
            result.add(targetMethod);
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
