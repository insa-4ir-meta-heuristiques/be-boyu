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

        //EST
        int[] end_last_task_of_job = new int[instance.numJobs];
        int[] end_last_task_on_machine = new int[instance.numMachines];

        switch(this.priority){
            case SPT :

            for(int taskNumber = 0 ; taskNumber<instance.numTasks ; taskNumber++) {
                for(int jobNumber = 0 ; jobNumber<instance.numJobs ; jobNumber++) {
                    Task tasktmp = new Task(jobNumber, taskNumber);
                    restTasks.add(tasktmp);
                }
            }

            int[][] cmpTasks = new int[instance.numJobs+1][instance.numTasks+1];
            for(int jobNumber = 0 ; jobNumber<instance.numJobs ; jobNumber++) {
                for(int taskNumber = 0; taskNumber<instance.numTasks ; taskNumber++){
                    cmpTasks[jobNumber][taskNumber] = Integer.MAX_VALUE;
                }
            }

            for(int jobNumber = 0 ; jobNumber<instance.numJobs ; jobNumber++) {
                    cmpTasks[jobNumber][0] = instance.duration(jobNumber,0);
            }
            

            while (!(restTasks.isEmpty())) {          
                //compare
                int min = 99999;
                int tmpjobNumber = 0;
                int tmptaskNumber = 0;
                for(int jobNumber = 0 ; jobNumber<instance.numJobs ; jobNumber++) {
                    for(int taskNumber = 0; taskNumber<instance.numTasks ; taskNumber++){
                        if (restTasks.contains(new Task(jobNumber, taskNumber)) && cmpTasks[jobNumber][taskNumber] < min){
                            min = cmpTasks[jobNumber][taskNumber];
                            tmpjobNumber = jobNumber;
                            tmptaskNumber = taskNumber;
                        }
                    } 
                } 
                //add
                Task taskToEnqueue = new Task(tmpjobNumber, tmptaskNumber);
                sol.addTaskToMachine(instance.machine(tmpjobNumber, tmptaskNumber), taskToEnqueue);
                restTasks.remove(taskToEnqueue);  
                if (tmptaskNumber +1 < instance.numTasks) {
                    cmpTasks[tmpjobNumber][tmptaskNumber + 1] = instance.duration(tmpjobNumber,tmptaskNumber + 1);
                }                
            }
            return sol.toSchedule();
            
            case LRPT : 

            for(int taskNumber = 0 ; taskNumber<instance.numTasks ; taskNumber++) {
                for(int jobNumber = 0 ; jobNumber<instance.numJobs ; jobNumber++) {
                    Task tasktmp = new Task(jobNumber, taskNumber);
                    restTasks.add(tasktmp);
                }
            }

            //remaintime table
            int[][] remaintime = new int[instance.numJobs+1][instance.numTasks+1];
            for(int jobNumber = 0 ; jobNumber<instance.numJobs ; jobNumber++) {
                for(int taskNumber = 0; taskNumber<instance.numTasks ; taskNumber++){
                    remaintime[jobNumber][taskNumber] = instance.duration(jobNumber, taskNumber);
                }
            }
            for(int jobNumber = 0 ; jobNumber<instance.numJobs ; jobNumber++) {
                for(int taskNumber = 0 ; taskNumber<instance.numTasks ; taskNumber++) {
                    int i = taskNumber;
                    while(++i<instance.numJobs){
                        remaintime[jobNumber][taskNumber] += remaintime[jobNumber][i] ;
                    }
                }
            }
            
            while(!(restTasks.isEmpty())){
                //max remaintime
                int maxremaintime = 0;
                int tmpjobNumber = 0;
                int tmptaskNumber = 0;
                for(int jobNumber = 0 ; jobNumber<instance.numJobs ; jobNumber++) {
                    for(int taskNumber = 0 ; taskNumber<instance.numTasks ; taskNumber++) {
                        if (restTasks.contains(new Task(jobNumber, taskNumber)) && maxremaintime < remaintime[jobNumber][taskNumber]){
                            maxremaintime = remaintime[jobNumber][taskNumber];
                            tmpjobNumber = jobNumber;
                            tmptaskNumber = taskNumber;
                        }
                    }
                }
                //add
                Task taskToEnqueue = new Task(tmpjobNumber, tmptaskNumber);
                sol.addTaskToMachine(instance.machine(tmpjobNumber, tmptaskNumber), taskToEnqueue);
                restTasks.remove(taskToEnqueue);
            }
            return sol.toSchedule();

            case EST_SPT :
            int[][] STasks = new int[instance.numJobs+1][instance.numTasks+1];
            for(int jobNumber = 0 ; jobNumber<instance.numJobs ; jobNumber++) {
                for(int taskNumber = 0; taskNumber<instance.numTasks ; taskNumber++){
                    STasks[jobNumber][taskNumber] = Integer.MAX_VALUE;
                }
            }

            for(int jobNumber = 0 ; jobNumber<instance.numJobs ; jobNumber++) {
                    STasks[jobNumber][0] = instance.duration(jobNumber,0);
            }
            

            //EST :-
            //1.time at which previous task on jobs
            //2.time at machine free

            for(int jobNumber = 0 ; jobNumber<instance.numJobs ; jobNumber++) {                
                    end_last_task_of_job[jobNumber] = 0;
            //rest task init
                    Task tasktmp = new Task(jobNumber, 0);
                    restTasks.add(tasktmp);
            }
            for(int machine = 0 ; machine<instance.numMachines ; machine++) {                
                end_last_task_on_machine[machine] = 0;
            }


            while(!(restTasks.isEmpty())){
                int minest = Integer.MAX_VALUE;
                int minduration = Integer.MAX_VALUE;
                Task taskToEnqueue = null;
                for (Task task : restTasks) {
                    int est = Integer.max(end_last_task_of_job[task.job], end_last_task_on_machine[instance.machine(task)]);
                    if (est < minest || (est == minest && minduration > instance.duration(task))) {
                        taskToEnqueue = task;
                        minest = est;
                        minduration = instance.duration(task);
                    }
                }
                
                //add
             
                sol.addTaskToMachine(instance.machine(taskToEnqueue), taskToEnqueue);  
                int end_time_of_enqueued_task = minest + instance.duration(taskToEnqueue);
                end_last_task_of_job[taskToEnqueue.job] = end_time_of_enqueued_task;
                end_last_task_on_machine[instance.machine(taskToEnqueue)] = end_time_of_enqueued_task;

                restTasks.remove(taskToEnqueue);
                if (taskToEnqueue.task +1 < instance.numTasks) {
                    restTasks.add(new Task(taskToEnqueue.job,taskToEnqueue.task + 1));
                }  
                
            }
            return sol.toSchedule();

            case EST_LRPT :
            //time remain
            int[][] time = new int[instance.numJobs+1][instance.numTasks+1];
            for(int jobNumber = 0 ; jobNumber<instance.numJobs ; jobNumber++) {
                for(int taskNumber = 0; taskNumber<instance.numTasks ; taskNumber++){
                    time[jobNumber][taskNumber] = instance.duration(jobNumber, taskNumber);
                }
            }
            for(int jobNumber = 0 ; jobNumber<instance.numJobs ; jobNumber++) {
                for(int taskNumber = 0 ; taskNumber<instance.numTasks ; taskNumber++) {
                    int i = taskNumber;
                    while(++i<instance.numJobs){
                        time[jobNumber][taskNumber] += time[jobNumber][i] ;
                    }
                }
            }
            

            //EST :-
            //1.time at which previous task on jobs
            //2.time at machine free

            for(int jobNumber = 0 ; jobNumber<instance.numJobs ; jobNumber++) {                
                    end_last_task_of_job[jobNumber] = 0;
            //rest task init
                    Task tasktmp = new Task(jobNumber, 0);
                    restTasks.add(tasktmp);
            }
            for(int machine = 0 ; machine<instance.numMachines ; machine++) {                
                end_last_task_on_machine[machine] = 0;
            }


            while(!(restTasks.isEmpty())){
                //max remaintime
                int maxremaintime = 0;
                int minest = Integer.MAX_VALUE;
                Task taskToEnqueue = null;
                for (Task task : restTasks) {
                    int est = Integer.max(end_last_task_of_job[task.job], end_last_task_on_machine[instance.machine(task)]);
                    int remainingtime = time[task.job][task.task];
                    if (est < minest || (est == minest && remainingtime > maxremaintime)) {
                        taskToEnqueue = task;
                        minest = est;
                        maxremaintime = remainingtime;
                    }
                }

                
                //add
             
                sol.addTaskToMachine(instance.machine(taskToEnqueue), taskToEnqueue);  
                int end_time_of_enqueued_task = minest + instance.duration(taskToEnqueue);
                end_last_task_of_job[taskToEnqueue.job] = end_time_of_enqueued_task;
                end_last_task_on_machine[instance.machine(taskToEnqueue)] = end_time_of_enqueued_task;

                restTasks.remove(taskToEnqueue);
                if (taskToEnqueue.task +1 < instance.numTasks) {
                    restTasks.add(new Task(taskToEnqueue.job,taskToEnqueue.task + 1));
                }  
                
            }
            return sol.toSchedule();

            default: 
            for(int taskNumber = 0 ; taskNumber<instance.numTasks ; taskNumber++) {
                for(int jobNumber = 0 ; jobNumber<instance.numJobs ; jobNumber++) {
                    Task taskToEnqueue = new Task(jobNumber, taskNumber);
                    sol.addTaskToMachine(instance.machine(jobNumber, taskNumber), taskToEnqueue);
                }
            }
            return sol.toSchedule();
        }
    }

    
}
