package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** An empty shell to implement a greedy solver. */
public class GreedySolver implements Solver {

    /** All possible priorities for the greedy solver. */
    public enum Priority {
        SPT, LPT, SRPT, LRPT, EST_SPT, EST_LPT, EST_SRPT, EST_LRPT
    }

    /** Priority that the solver should use. */
    final Priority priority;

    /** Creates a new greedy solver that will use the given priority. */
    public GreedySolver(Priority p) {
        this.priority = p;
    }

    @Override
    public Optional<Schedule> solve(Instance instance, long deadline) {
        ResourceOrder sol = new ResourceOrder(instance);

        List<Task> restTasks = new ArrayList<>();
        for(int taskNumber = 0 ; taskNumber<instance.numTasks ; taskNumber++) {
            for(int jobNumber = 0 ; jobNumber<instance.numJobs ; jobNumber++) {
                Task tasktmp = new Task(jobNumber, taskNumber);
                restTasks.add(tasktmp);
            }
        }

        List<Task> cmpTasks = new ArrayList<>();
        Task tasktmp = restTasks.get(0);
        while (!(restTasks.isEmpty())) {
            for(int taskNumber = tasktmp.task ; taskNumber<instance.numTasks ; taskNumber++) {
                if (taskNumber + 1 < instance.numTasks){
                    Task taskadd1 = new Task(tasktmp.job, taskNumber +1);
                    if (restTasks.contains(taskadd1)){
                        cmpTasks.add(tasktmp);
                    }
                }
                for(int jobNumber = 0 ; jobNumber<instance.numJobs ; jobNumber++) {
                    Task taskadd2 = new Task(jobNumber, taskNumber);
                    if (restTasks.contains(taskadd2)){
                        cmpTasks.add(tasktmp);
                    }
                }
            }
            //compare
            int min = 99999;
            for (int i = 0 ; i < cmpTasks.size(); i++){
                int tmp = instance.duration(cmpTasks.get(i));
                if (tmp < min){
                    tasktmp = cmpTasks.get(i);
                    min = tmp;
                }                 
            }
            //add            
            sol.addTaskToMachine(instance.machine(tasktmp.job, tasktmp.task), tasktmp);
            restTasks.remove(tasktmp);
        }



        return sol.toSchedule();
    }

    
}
