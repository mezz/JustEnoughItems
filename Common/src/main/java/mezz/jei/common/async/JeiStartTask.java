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

    public static void interruptIfCanceled() {
        Thread t = Thread.currentThread();
        if (t instanceof JeiStartTask startTask) {
            if (startTask.isCancelled) {
                throw new JeiAsyncStartInterrupt();
            }
        }
    }

    @Override
    public void run() {
        try {
            super.run();
        } catch (JeiAsyncStartInterrupt ignored) {

        }
    }

    private static final class JeiAsyncStartInterrupt extends Error {
        public JeiAsyncStartInterrupt() {
        }
    }
}
