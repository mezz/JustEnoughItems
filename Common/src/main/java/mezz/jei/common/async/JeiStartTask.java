package mezz.jei.common.async;

public class JeiStartTask extends Thread {
    private final Runnable startTask;
    private boolean isCancelled = false;

    public JeiStartTask(Runnable startTask) {
        this.startTask = startTask;
        this.setName("JEI Start");
    }

    public void interruptStart() {
        isCancelled = true;
    }

    /**
     * Check whether the startup should be interrupted. If this is not running on a JEI startup thread,
     * false is returned.
     */
    private static boolean isStartInterrupted() {
        Thread t = Thread.currentThread();
        if(t instanceof JeiStartTask) {
            return ((JeiStartTask)t).isCancelled;
        } else
            return false;
    }

    private static final JeiAsyncStartInterrupt INTERRUPT_START = new JeiAsyncStartInterrupt();

    public static void checkStartInterruption() {
        if(isStartInterrupted())
            forceInterrupt();
    }

    public static void forceInterrupt() {
        throw INTERRUPT_START;
    }

    @Override
    public void run() {
        try {
            startTask.run();
        } catch(JeiAsyncStartInterrupt ignored) {
        }
    }
}
