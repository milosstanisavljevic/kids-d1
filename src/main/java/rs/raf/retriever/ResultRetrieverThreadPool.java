package rs.raf.retriever;

import rs.raf.scanner.web_scanner.WebScannerThreadPool;

import java.util.Map;
import java.util.concurrent.*;

public class ResultRetrieverThreadPool {

    private ExecutorService pool;
    public ConcurrentHashMap<String, Future<Map<String, Integer>>> fileResults,webResults;
    public Future<Map<String, Map<String, Integer>>>  futureFileSummary;
    public ConcurrentHashMap<String, Map<String, Integer>>fileSummary;

    //private WebScannerThreadPool webScannerThreadPool;
    private ScheduledExecutorService executor;

    public ResultRetrieverThreadPool() {
        this.fileResults = new ConcurrentHashMap<>();
        this.webResults = new ConcurrentHashMap<>();
        this.fileSummary = new ConcurrentHashMap<>();
        this.pool = Executors.newCachedThreadPool();
        executor = Executors.newScheduledThreadPool(5);
        this.futureFileSummary = null;
    }

    public void addNewFileResult(String corpus,Future<Map<String, Integer>> result) {
        fileResults.putIfAbsent(corpus, result);

    }

    public String getFileSummary() {
            if(fileSummary.isEmpty()) {
                Callable<Map<String, Map<String, Integer>>> retrieverJob = new ResultRetriever(webResults, fileResults, "", JobType.FILESUMMARY);
                Future<Map<String, Map<String, Integer>>> future = pool.submit(retrieverJob);
                futureFileSummary = future;
                try {
                    Map<String, Map<String, Integer>> map = future.get();

                    for (Map.Entry<String, Map<String, Integer>> entry : map.entrySet()) {
                        fileSummary.put(entry.getKey(), entry.getValue());
                    }

                    return fileSummary.toString();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                } catch (ExecutionException e1) {
                    e1.printStackTrace();
                }
            }else {
                return fileSummary.toString();
            }
        return null;
    }


    public String queryFileSummary() {
        if(futureFileSummary != null) {
            if(futureFileSummary.isDone()) {
                return fileSummary.toString();
            }else {
                return "Summary is not ready yet.";
            }
        }
        return "There are no result for summary.";
    }


    public String clearFileSummary() {
        if(fileSummary.isEmpty()) {
            return "File summary is empty";
        }
        fileSummary.clear();
        futureFileSummary = null;
        return "Deleting summary file";
    }


    public String getResultForCorpus(String corpus) {
        System.out.println(corpus);
        System.out.println(fileResults + "aa");
        if(this.fileResults.containsKey(corpus)) {
            System.out.println("uslo");
            try {
                System.out.println("uslo2");
                System.out.println("??????????????????????");
                return fileResults.get(corpus).get().toString();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return "There is not job for this corpus";
    }

    public String getQueryForCorpus(String corpus) {
        try {
            if(fileResults.containsKey(corpus)) {
                Future<Map<String,Integer>> result = fileResults.get(corpus);
                if(result.isDone()) {
                    return fileResults.get(corpus).get().toString();
                }else {
                    return "Job for this corpus exists but isn't done";
                }
            } else {
                return "There is not job for this corpus";
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void shutdown() {
        this.executor.shutdown();
        this.pool.shutdown();
    }


}