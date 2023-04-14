package rs.raf.app;

import rs.raf.job_desc.ScanType;
import rs.raf.job_desc.ScanningJob;
import rs.raf.scanner.file_scanner.FileScannerThreadPool;
import rs.raf.scanner.web_scanner.WebScannerThreadPool;

import java.util.concurrent.BlockingQueue;

public class JobDispatcher implements Runnable{
    private BlockingQueue<ScanningJob> jobQueue;
    private boolean run = true;
    private FileScannerThreadPool fileScannerThreadPool;
    private WebScannerThreadPool webScannerThreadPool;


    public JobDispatcher(BlockingQueue<ScanningJob> jobQueue, FileScannerThreadPool fileScannerThreadPool,
                         WebScannerThreadPool webScannerThreadPool) {
        this.jobQueue = jobQueue;
        this.fileScannerThreadPool = fileScannerThreadPool;
        this.webScannerThreadPool = webScannerThreadPool;
    }


    @Override
    public void run() {
        while(run) {
            try {
                ScanningJob newJob = jobQueue.take();

                if(newJob.getType().equals(ScanType.FILE)) {
                    fileScannerThreadPool.newJob(newJob);
                }

                if(newJob.getType().equals(ScanType.WEB)) {
                    //webScannerThreadPool.newJob(newJob);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isRun() {
        return run;
    }

    public void setRun(boolean run) {
        this.run = run;
    }
}
