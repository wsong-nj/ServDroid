package com.event.dataAnalysis;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.solver.cfg.BackwardsInfoflowCFG;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;

public class EventManager {

	public Set<SootMethod> getMethodCalls(Unit targetLocation, BackwardsInfoflowCFG backwardsCFG) {
		Set<SootMethod> headMethods = getInitalMethodCalls(targetLocation, backwardsCFG);
		return headMethods;

	}

	private Set<SootMethod> getInitalMethodCalls(Unit targetLocation, BackwardsInfoflowCFG backwardsCFG) {
		Set<SootMethod> headUnits = new HashSet<SootMethod>();
		Set<Unit> reachedUnits = new HashSet<Unit>();
		LinkedList<Unit> worklist = new LinkedList<Unit>();
		Unit previousUnit = null;
		worklist.add(targetLocation);

		while (!worklist.isEmpty()) {
			// get front element
			Unit currentUnit = worklist.removeFirst();

			if (reachedUnits.contains(currentUnit)) {
				previousUnit = currentUnit;
				continue;
			} else
				reachedUnits.add(currentUnit);

			SootMethod currentMethod = backwardsCFG.getMethodOf(currentUnit);
			// we reached the head unit
			if (currentMethod.getDeclaringClass().toString().equals("dummyMainClass")) {
				if (previousUnit == null)
					throw new RuntimeException("there should be a previous unit");
				headUnits.add(backwardsCFG.getMethodOf(previousUnit));
				continue;
			}

			// in case we reached the start of the method (vice verse in backward analysis)
			if (backwardsCFG.isExitStmt(currentUnit)) {
				SootMethod sm = backwardsCFG.getMethodOf(currentUnit);
				// first: get all callers
				Collection<Unit> callers = backwardsCFG.getCallersOf(sm);
				for (Unit caller : callers) {
					// get the predecessors (aka succs of cfg) of the callers and add them to the
					// worklist
					List<Unit> succOfCaller = backwardsCFG.getSuccsOf(caller);
					for (Unit unit : succOfCaller)
						worklist.addFirst(unit);
				}
				previousUnit = currentUnit;
				// there is no need for further progress
				continue;
			}

			List<Unit> nextUnits = backwardsCFG.getSuccsOf(currentUnit);
			for (Unit unit : nextUnits)
				worklist.addFirst(unit);
			previousUnit = currentUnit;
		}

		return headUnits;
	}
}
