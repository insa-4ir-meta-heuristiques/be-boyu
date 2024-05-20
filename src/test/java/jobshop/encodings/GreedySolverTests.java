package jobshop.encodings;

import jobshop.Instance;
import jobshop.solvers.Solver;
import jobshop.solvers.GreedySolver;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

public class GreedySolverTests {

    @Test
    public void testGreedySolveSPT() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa3"));

        Solver solverSPT = new GreedySolver(GreedySolver.Priority.SPT);
        Optional<Schedule> result = solverSPT.solve(instance, System.currentTimeMillis() + 10);

        assert result.isPresent() : "The solver did not find a solution";
        // extract the schedule associated to the solution
        Schedule schedule = result.get();
        assert  schedule.isValid() : "The solution is not valid";

        System.out.println("SPT:");
        System.out.println("Makespan: " + schedule.makespan());
        System.out.println("Schedule: \n" + schedule);
        System.out.println(schedule.asciiGantt());

        assert schedule.makespan() == 53 : "The basic solver should have produced a makespan of 53 for this instance.";
    }

    @Test
    public void testGreedySolveLRPT() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa3"));

        Solver solverLRPT = new GreedySolver(GreedySolver.Priority.LRPT);
        Optional<Schedule> result = solverLRPT.solve(instance, System.currentTimeMillis() + 10);

        assert result.isPresent() : "The solver did not find a solution";
        // extract the schedule associated to the solution
        Schedule schedule = result.get();
        assert  schedule.isValid() : "The solution is not valid";

        System.out.println("LRPT:");
        System.out.println("Makespan: " + schedule.makespan());
        System.out.println("Schedule: \n" + schedule);
        System.out.println(schedule.asciiGantt());

        assert schedule.makespan() == 54 : "The basic solver should have produced a makespan of 54 for this instance.";
    }

        @Test
        public void testGreedySolveEST_SPT() throws IOException {
            Instance instance = Instance.fromFile(Paths.get("instances/aaa3"));
    
            Solver solverLRPT = new GreedySolver(GreedySolver.Priority.EST_SPT);
            Optional<Schedule> result = solverLRPT.solve(instance, System.currentTimeMillis() + 10);
    
            assert result.isPresent() : "The solver did not find a solution";
            // extract the schedule associated to the solution
            Schedule schedule = result.get();
            assert  schedule.isValid() : "The solution is not valid";
    
            System.out.println("EST_SPT:");
            System.out.println("Makespan: " + schedule.makespan());
            System.out.println("Schedule: \n" + schedule);
            System.out.println(schedule.asciiGantt());
    
            assert schedule.makespan() == 48 : "The basic solver should have produced a makespan of 48 for this instance.";
        }

        @Test
        public void testGreedySolveEST_LRPT() throws IOException {
            Instance instance = Instance.fromFile(Paths.get("instances/aaa3"));
    
            Solver solverLRPT = new GreedySolver(GreedySolver.Priority.EST_LRPT);
            Optional<Schedule> result = solverLRPT.solve(instance, System.currentTimeMillis() + 10);
    
            assert result.isPresent() : "The solver did not find a solution";
            // extract the schedule associated to the solution
            Schedule schedule = result.get();
            assert  schedule.isValid() : "The solution is not valid";
    
            System.out.println("EST_LRPT:");
            System.out.println("Makespan: " + schedule.makespan());
            System.out.println("Schedule: \n" + schedule);
            System.out.println(schedule.asciiGantt());
    
            assert schedule.makespan() == 56 : "The basic solver should have produced a makespan of 56 for this instance.";                    
    }

}
