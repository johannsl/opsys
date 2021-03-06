/**
 * Created by iver on 08.04.15.
 */
public class CPU {
	private EventQueue eventQueue;
    private Queue cpuQueue;
    private Gui gui;
    private long clock;
    private Statistics statistics;
    private long maxCPUTime;
    private Process activeProcess;

	public CPU(EventQueue eventQueue, Queue cpuQueue, Gui gui, long clock, Statistics statistics, long maxCPUTime) {
		this.eventQueue = eventQueue;
        this.cpuQueue = cpuQueue;
        this.gui = gui;
        this.clock = clock;
        this.statistics = statistics;
        this.maxCPUTime = maxCPUTime;
    }
	
    public void insertProcess(Process process) {
    	if (process != null) {
    		cpuQueue.insert(process);
    		process.enterCPUQueue(clock);
    	}
    	
    	// TEST PRINT
//    	else System.out.print("CPU insert process = null! \n");
    	
    }
    
    public Process extractProcess() {
    	if (activeProcess != null) {	
    		Process process = activeProcess;
    		activeProcess = null;
    		process.leaveCPU(clock);
    		gui.setCpuActive(null);
    		return process;
    	}
    	
    	// TEST PRINT
//    	System.out.print("There was no CPU process to extract! \n");
    	
    	return null;
    }
    
    public boolean isIdle() {
    	return (activeProcess == null);
    }

    /*	This is the part where the 'Round Robin algorithm' is implemented.
     * 
     * 	Basically, when the CPU.run() method is called the CPU 'runs', which means it will reduce the work needed
     * 	to complete the current process (activeProcess). The Round Robin implementation here limits the CPU to work
     *	on any process for longer than the work limit (standard 500ms); If the process needs more work, the process
     * 	will be moved back in the line (cpuQueue) so that other processes can be worked on.
     * 
     * 	The two other situations the CPU might face are: the active process has an I/O request, in which case the
     * 	process will be moved to the I/O queue, or the active process does not need more processing, in which case
     * 	the process is terminated.
     *  
     * 	- johannsl & iverasp
     */
    public void run() {
    	if (!cpuQueue.isEmpty()) {
    		if (isIdle()) {
    			activeProcess = (Process) cpuQueue.removeNext();
    			activeProcess.enterCPU(clock);
    			gui.setCpuActive(activeProcess);
    			long nextIo = activeProcess.calcTimeToNextIoOperation();
    			if (activeProcess.getCpuTimeNeeded() > maxCPUTime && nextIo > maxCPUTime) {
    				eventQueue.insertEvent(new Event(Constants.SWITCH_PROCESS, clock + maxCPUTime));
    				
    				// TEST PRINT
//    				System.out.print("CPU switching process \n");
    			
    			}
    			else if (nextIo <= maxCPUTime && nextIo < activeProcess.getCpuTimeNeeded()) {
    				eventQueue.insertEvent(new Event(Constants.IO_REQUEST, clock + nextIo));
    			
    				// TEST PRINT
//    				System.out.print("CPU requesting IO \n");
    			
    			}
    			else if (activeProcess.getCpuTimeNeeded() <= maxCPUTime && activeProcess.getCpuTimeNeeded() <= nextIo) {
    				eventQueue.insertEvent(new Event(Constants.END_PROCESS, clock + activeProcess.getCpuTimeNeeded()));
    			
    				// TEST PRINT
//    				System.out.print("CPU ending process \n");
    			
    			}
    			
        		// TEST PRINT
        		else {
        			System.out.print("PLEASE CHECK CPU FOR BUGS! \n");
        			System.out.print("Time needed: " + activeProcess.getCpuTimeNeeded() + ", maxCPUTime: " + maxCPUTime + ", NextIO: " + nextIo + "\n");
        		}	
    		}    		
    	}
    	
    	// TEST PRINT
//    	else System.out.print("The cpuQueue was empty! \n");
    	
    }
    
	public void updateClock(long clock) {
		
		//TEST PRINT
//		System.out.print("The CPU clock is now: " + clock + "\n");
		
		this.clock = clock;
	}
	
	public void timePassed(long timePassed) {
		statistics.cpuQueueLengthTime += cpuQueue.getQueueLength()*timePassed;
		if (cpuQueue.getQueueLength() > statistics.cpuQueueLargestLength) {
			statistics.cpuQueueLargestLength = cpuQueue.getQueueLength();
		}
	}
	
    public long getMaxCPUTime() {
		return maxCPUTime;
	}

	public void setMaxCPUTime(long maxCPUTime) {
		this.maxCPUTime = maxCPUTime;
	}    
}
