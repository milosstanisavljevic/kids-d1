package rs.raf.scanner.file_scanner;

import rs.raf.job_desc.ScanningJob;
import rs.raf.retriever.ResultRetrieverThreadPool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public class FileScannerThreadPool {

    private ForkJoinPool pool;
    public ConcurrentHashMap<String, Boolean> corpusJobs;
    private ResultRetrieverThreadPool resultRetrieverThreadPool;

    public FileScannerThreadPool(ResultRetrieverThreadPool resultRetrieverThreadPool) {
        pool = ForkJoinPool.commonPool();
        this.resultRetrieverThreadPool = resultRetrieverThreadPool;
        corpusJobs = new ConcurrentHashMap<>();
    }

    public void newJob(ScanningJob job) {
        pool = ForkJoinPool.commonPool();
        RecursiveTask<Map<String, Integer>> jobRunnable = (FileScanner) job;
        System.out.println("Starting file scan for file | " + job.getQuery());
        Future<Map<String, Integer>> future = pool.submit(jobRunnable);
        resultRetrieverThreadPool.addNewFileResult(job.getQuery(), future);
    }

    public void jobStarted(String url) {
        corpusJobs.put(url, false);
    }

    public void jobEnded(String url) {
        corpusJobs.put(url, true);
    }

    public void setResultRetrieverThreadPool(ResultRetrieverThreadPool resultRetrieverThreadPool) {
        this.resultRetrieverThreadPool = resultRetrieverThreadPool;
    }

    public void shutdown() {
        pool.shutdown();
    }

}
