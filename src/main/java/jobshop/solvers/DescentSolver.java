package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.solvers.neighborhood.Neighborhood;
import jobshop.solvers.neighborhood.Nowicki;
import jobshop.solvers.neighborhood.Nowicki.Swap;

import java.util.List;
import java.util.Optional;

/** An empty shell to implement a descent solver. */
public class DescentSolver implements Solver {

    final Neighborhood neighborhood;
    final Solver baseSolver;

    /** Creates a new descent solver with a given neighborhood and a solver for the initial solution.
     *
     * @param neighborhood Neighborhood object that should be used to generates neighbor solutions to the current candidate.
     * @param baseSolver A solver to provide the initial solution.
     */
    public DescentSolver(Neighborhood neighborhood, Solver baseSolver) {
        this.neighborhood = neighborhood;
        this.baseSolver = baseSolver;
    }

    @Override
    public Optional<Schedule> solve(Instance instance, long deadline) {        
        ResourceOrder sol = new ResourceOrder(instance);        
        Optional<Schedule> result = baseSolver.solve(instance, System.currentTimeMillis() + 10);
        List<ResourceOrder> neighbor = neighborhood.generateNeighbors(sol);
        Schedule schedule = result.get();

        //
        int i = 0;
        boolean meillleur = true;
        int meilleurmakespan = Integer.MAX_VALUE;
        ResourceOrder s = sol;
        while (++i < deadline || meillleur) {
            meillleur = false;
            // meilleur
            for (ResourceOrder swap : neighbor) {
                // check validity of swap (toSchedule is non empty)
                Optional<Schedule> optSchedule = swap.toSchedule();
                boolean valid = optSchedule.isPresent();
                if (valid && optSchedule.get().makespan() < meilleurmakespan){
                    s = swap;                    
                    meillleur = true;
                    meilleurmakespan = optSchedule.get().makespan();
                }
            }
            if (s.toSchedule().get().makespan() < schedule.makespan()){
                sol = s;
            }        
        }

        return sol.toSchedule();
    }

}
