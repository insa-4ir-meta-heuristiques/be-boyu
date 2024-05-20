package jobshop.encodings;

import jobshop.Instance;
import jobshop.solvers.Solver;
import jobshop.solvers.neighborhood.Neighborhood;
import jobshop.solvers.neighborhood.Nowicki;
import jobshop.solvers.neighborhood.Nowicki.Swap;
import jobshop.solvers.BasicSolver;
import jobshop.solvers.DescentSolver;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
public class DescentSolverTests {

    @Test
    public void testDescentSolver() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/ft06"));

        Nowicki neighbor = new Nowicki();
        /*Solver solver = new DescentSolver(neighbor, Solver.getSolver("basic"));
        Optional<Schedule> result = solver.solve(instance, System.currentTimeMillis() + 10);

        assert result.isPresent() : "The solver did not find a solution";
        // extract the schedule associated to the solution
        Schedule schedule = result.get();
        assert  schedule.isValid() : "The solution is not valid";

        System.out.println("DescentSolver:");
        System.out.println("Makespan: " + schedule.makespan());
        System.out.println("Schedule: \n" + schedule);

        System.out.println(schedule.asciiGantt());

        //assert schedule.makespan() == 12 : "The basic solver should have produced a makespan of 12 for this instance.";*/
        System.out.println("DescentSolver:");
        /*List<Swap> tmp = neighbor.allSwaps(new ResourceOrder(instance));
        for (Swap schedule : tmp){
            System.out.println("Schedule: \n" + schedule);
        }      */  
        Solver solver = new BasicSolver();
        Optional<Schedule> result = solver.solve(instance, System.currentTimeMillis() + 10);

        assert result.isPresent() : "The solver did not find a solution";
        // extract the schedule associated to the solution
        Schedule schedule = result.get();
        assert  schedule.isValid() : "The solution is not valid";
        List<Task> criticalpath = new ResourceOrder(schedule).toSchedule().get().criticalPath();
        System.out.println("criticalPath:" + criticalpath);
        for (Task task : criticalpath){
            System.out.println("machine:" + instance.machine(task));
        }   
        System.out.println(neighbor.blocksOfCriticalPath(new ResourceOrder(schedule)));
}
}
