package dfs.process;

/**
 * Created by Dávid on 1.10.2016.
 */
public abstract class AbstractProcess implements dfs.process.Process {

    private boolean _isRunning = false;

    @Override
    public void start() {
        _isRunning = true;

        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void stop() {
        _isRunning = false;
    }

    @Override
    public abstract void run();

    @Override
    public boolean isRunning() {
        return _isRunning;
    }
}
