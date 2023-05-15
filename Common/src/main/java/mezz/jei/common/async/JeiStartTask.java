package mezz.jei.common.async;

public class JeiStartTask extends Thread {
    private boolean isCancelled = false;

    public JeiStartTask(Runnable target) {
        super(target, "JEI Start");
        setDaemon(true);
    }

    public void cancelStart() {
        isCancelled = true;
    }

    /**
     * Check whether the startup should be interrupted.
     * If this is not running on a JEI startup thread, false is returned.
     */
    private static boolean isCanceled() {
        Thread t = Thread.currentThread();
        if (t instanceof JeiStartTask startTask) {
            return startTask.isCancelled;
        } else {
            return false;
        }
    }

    public static void interruptIfCanceled() {
        if (isCanceled()) {
            throw new JeiAsyncStartInterrupt();
        }
    }

    @Override
    public void run() {
        try {
            super.run();
        } catch (JeiAsyncStartInterrupt ignored) {

        }
    }
}
