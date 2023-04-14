package rs.raf;

import rs.raf.app.DirectoryCrawler;
import rs.raf.app.JobDispatcher;
import rs.raf.job_desc.ScanningJob;
import rs.raf.retriever.ResultRetrieverThreadPool;
import rs.raf.scanner.file_scanner.FileScannerThreadPool;
import rs.raf.scanner.web_scanner.WebScannerThreadPool;
import rs.raf.util.MyProperties;

import java.util.Scanner;
import java.util.concurrent.*;

public class Main {

    public static void main(String[] args) {
        boolean isStopped = false;
        ExecutorService pool = Executors.newCachedThreadPool();
        BlockingQueue<ScanningJob> jobQueue = new LinkedBlockingQueue<>();
        ConcurrentHashMap<String,Long> lastModified = new ConcurrentHashMap<>();
        ResultRetrieverThreadPool resultRetrieverThreadPool = new ResultRetrieverThreadPool();
        FileScannerThreadPool fileScannerThreadPool = new FileScannerThreadPool(resultRetrieverThreadPool);
        WebScannerThreadPool webScannerThreadPool = new WebScannerThreadPool();
        JobDispatcher jobDispatcher = new JobDispatcher(jobQueue,fileScannerThreadPool,webScannerThreadPool);
        DirectoryCrawler directoryCrawler = new DirectoryCrawler(lastModified,jobQueue,fileScannerThreadPool);
        pool.submit(directoryCrawler);
        pool.submit(jobDispatcher);
        Scanner sc = new Scanner(System.in);
        String command = sc.nextLine();

        while(isStopped == false) {

            if(command.startsWith("ad")) {
                //ad src/main/java/rs/raf/kids_d1_data_primer/example/data
                String[] arr = command.split(" ");
                if(arr.length == 2) {
                    System.out.println(arr[1]);
                    directoryCrawler.addNewDirectory(arr[1]);
                }else System.out.println("Komanda nije ispravna");

            }else if(command.startsWith("aw")) {
                System.out.println("nisam uradio :(");
            }else if(command.startsWith("get file|summary")) {
                System.out.println(resultRetrieverThreadPool.getFileSummary());
            }else if(command.startsWith("get web|summary")) {
                System.out.println("nisam uradio :(");
            }else if(command.startsWith("query file|summary")) {
                System.out.println(resultRetrieverThreadPool.queryFileSummary());
            }else if(command.startsWith("query web|summary")) {
                System.out.println("nisam uradio :(");
            }else if(command.startsWith("cfs")) {
                System.out.println(resultRetrieverThreadPool.clearFileSummary());
            }else if(command.startsWith("cws")) {
                System.out.println("nisam uradio :(");
            }else if(command.startsWith("get file|")) {
                String[] arr = command.split("\\|");
                if(arr.length == 2){
                    System.out.println(resultRetrieverThreadPool.getResultForCorpus(arr[1]));
                }
                else System.out.println("komanda nije ispravna");

            }else if(command.startsWith("query file|")) {
                String[] arr = command.split("\\|");
                if(arr.length == 2){
                    System.out.println(resultRetrieverThreadPool.getQueryForCorpus(arr[1]));
                }
                else System.out.println("komanda nije ispravna");

            }else if(command.startsWith("get web|")) {
                System.out.println("nisam uradio :(");

            }else if(command.startsWith("query web|")) {
                System.out.println("nisam uradio :(");
            }else if(command.startsWith("stop")){
                isStopped = true;
            }
            else {
                System.out.println("Komanda ne postoji.");
            }

            command = sc.nextLine();

        }
        fileScannerThreadPool.shutdown();
        resultRetrieverThreadPool.shutdown();
        jobDispatcher.setRun(false);
        directoryCrawler.setRun(false);
        pool.shutdown();
        System.out.println("Stopping...");
        System.exit(0);
    }
}