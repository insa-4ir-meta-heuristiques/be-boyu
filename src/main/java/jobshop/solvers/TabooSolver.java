package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.solvers.neighborhood.Neighborhood;

import java.util.*;

public class TabooSolver implements Solver {
    final Neighborhood neighborhood;
    final Solver baseSolver;
    private int historyForbidenSize;

    /**
     * Creates a new taboo solver with a given neighborhood and a solver for the initial solution.
     *
     * @param neighborhood Neighborhood object that should be used to generate neighbor solutions to the current candidate.
     * @param baseSolver   A solver to provide the initial solution.
     */
    public TabooSolver(Neighborhood neighborhood, Solver baseSolver) {
        this.neighborhood = neighborhood;
        this.baseSolver = baseSolver;
        this.historyForbidenSize = 5;
    }

    @Override
    public Optional<Schedule> solve(Instance instance, long deadline) {
        Schedule schedule = baseSolver.solve(instance, deadline).get();
        ResourceOrder order = new ResourceOrder(schedule);
        ResourceOrder result = order;

        // Create an array to store the cycle
        ArrayList<Integer> historic = new ArrayList<>();

        // Init forbidden states: Array of forbidden swaps
        int[][][] forbiddenNeighbours = new int[instance.numMachines][instance.numJobs][instance.numJobs];
        int[] zeroArray = new int[instance.numJobs];
        Arrays.fill(zeroArray, 0);

        for (int i = 0; i < instance.numMachines; i++) {
            Arrays.fill(forbiddenNeighbours[i], zeroArray);
        }

        int iterationCount = 0;
        Integer best = Integer.MAX_VALUE;
        ResourceOrder bestRO = order;
        ResourceOrder previousRO;

        while (System.currentTimeMillis() < deadline) {
            List<ResourceOrder> neighbours = neighborhood.generateNeighbors(order);
            previousRO = order;

            try {
                Integer finalBest = best;
                try { // Is there an improving best?
                    order = Collections.min(neighbours.stream()
                            .filter(e -> e.toSchedule().get().isValid()) // Removes invalid solutions
                            .filter(e -> e.toSchedule().get().makespan() < finalBest) // Removes solutions that do not improve
                            .map(e -> new AbstractMap.SimpleEntry<>(e, e.toSchedule().get().makespan())) // Map to makespan
                            .toList(), Comparator.comparingInt(Map.Entry::getValue)) // Finds the best makespan
                            .getKey();
                    result = order;
                } catch (NoSuchElementException e1) {
                    ResourceOrder finalPreviousRO = previousRO;
                    int[][][] finalForbiddenNeighbours = forbiddenNeighbours;
                    int finalIterationCount = iterationCount;

                    order = Collections.min(neighbours.stream()
                            .filter(e -> e.toSchedule().get().isValid()) // Removes invalid solutions
                            .filter(e -> !isForbidden(e, finalPreviousRO, finalForbiddenNeighbours, finalIterationCount))
                            .map(e -> new AbstractMap.SimpleEntry<>(e, e.toSchedule().get().makespan())) // Map to makespan
                            .toList(), Comparator.comparingInt(Map.Entry::getValue)) // Finds the best makespan
                            .getKey();
                    result = order;
                }
            } catch (NoSuchElementException e2) { // No solution found, wait
            }

            // Check if we have a cycle
            if (isCycle(historic, result.toSchedule().get())) {
                System.out.println("CYCLE");
                break; // Remove to allow cycles
            }

            System.out.println("Current makespan: " + result.toSchedule().get().makespan() +
                    ", Current best makespan: " + best +
                    ", Number of neighbours: " + getNumberOfNeighbours(neighbours) +
                    ", Number of non-forbidden neighbours: " + getNumberOfNonForbiddenNeighbours(neighbours, previousRO, forbiddenNeighbours, iterationCount));

            // Add to graph data
            best = Math.min(result.toSchedule().get().makespan(), best);
            if (bestRO.toSchedule().get().makespan() > result.toSchedule().get().makespan()) {
                bestRO = result;
            }
            forbiddenNeighbours = computeNewForbidden(result, previousRO, forbiddenNeighbours, iterationCount);
            iterationCount++;
        }

        return bestRO.toSchedule();
    }

    // Used to compute the number of neighbours seen
    private int getNumberOfNeighbours(List<ResourceOrder> neighbours) {
        int count = 0;
        for (ResourceOrder neighbor : neighbours) {
            if (neighbor.toSchedule().get().isValid()) { // Removes invalid solutions
                count++;
            }
        }
        return count;
    }

    private int getNumberOfNonForbiddenNeighbours(List<ResourceOrder> neighbours, ResourceOrder previous, int[][][] forbiddenNeighbours, int iterationCount) {
        int count = 0;
        for (ResourceOrder neighbor : neighbours) {
            if (neighbor.toSchedule().get().isValid() && !isForbidden(neighbor, previous, forbiddenNeighbours, iterationCount)) {
                count++;
            }
        }
        return count;
    }

    private boolean isForbidden(ResourceOrder ro, ResourceOrder previous, int[][][] forbiddenNeighbours, int iterationCount) {
        int[] changes = ro.getSwap(previous);

        if (changes[0] == -1) {
            return true;
        }

        return forbiddenNeighbours[changes[0]][changes[1]][changes[2]] != 0 && forbiddenNeighbours[changes[0]][changes[1]][changes[2]] >= iterationCount - historyForbidenSize;
    }

    private int[][][] computeNewForbidden(ResourceOrder ro, ResourceOrder previous, int[][][] forbiddenNeighbours, int iterationCount) {
        int[] changes = ro.getSwap(previous);
        if (changes[0] != -1) {
            forbiddenNeighbours[changes[0]][changes[1]][changes[2]] = iterationCount;
        }
        return forbiddenNeighbours;
    }

    private boolean isCycle(ArrayList historic, Schedule schedule) {
        historic.add(schedule.hashCode());
        Boolean found = false;
        Boolean result = true;
        int position1 = 1;
        int position2 = 2;
        int lenCycle = 0;
        if (historic.size() > 1000) { // To only store the last thousand
            historic.remove(0);
        }

        // Find the first occurrence of duplicate state
        if (historic.size() > 100) { // In order not to cut too short
            for (position1 = 1; position1 < (historic.size() / 2); position1++) {
                position2 = 2 * position1;
                if (historic.get(position1).equals(historic.get(position2))) {
                    lenCycle = position2 - position1;
                    found = true;
                    break;
                }
            }
        }

        // If there is one, then checks that the sequence leading one to the other is the same n times
        if (found) {
            try {
                result = true;
                for (int k = 1; k < 5 /* Tune this for more or less tolerance */; k++) {
                    result = result && historic.subList(position1, position2).equals(historic.subList(position1 + lenCycle * k, position2 + lenCycle * k));
                }
            } catch (IndexOutOfBoundsException e) { // If one occurs then the list is too short to include a cycle
            }
        }
        return found;
    }
}
