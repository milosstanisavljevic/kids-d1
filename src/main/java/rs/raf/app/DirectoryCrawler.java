package rs.raf.app;

import rs.raf.job_desc.ScanningJob;
import rs.raf.scanner.file_scanner.FileScanner;
import rs.raf.scanner.file_scanner.FileScannerThreadPool;
import rs.raf.util.MyProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class DirectoryCrawler implements Runnable{
    private ConcurrentHashMap<String,Long> lastModified;
    private BlockingQueue<ScanningJob> jobQueue;
    private FileScannerThreadPool fileScannerThreadPool;
    private boolean run = true;
    private List<String> directories =  new ArrayList<>();
    public DirectoryCrawler(ConcurrentHashMap<String,Long> lastModified,BlockingQueue<ScanningJob> jobQueue, FileScannerThreadPool fileScannerThreadPool){
        this.lastModified = lastModified;
        this.jobQueue = jobQueue;
        this.fileScannerThreadPool = fileScannerThreadPool;

    }
    @Override
    public void run() {
        Queue<File> directoryQueue = new LinkedList<>();

        while(true){
            synchronized (this){
                for(String s : directories){
                    directoryQueue.add(new File(s));
                }
            }
            while (!directoryQueue.isEmpty()){
                File curr = directoryQueue.poll();
                if(isCorpus(curr)){
                    startJob(curr);
                }
                File[] files = curr.listFiles();
                for(int i=0; i<files.length; i++) {

                    if(files[i].isDirectory()) {
                        directoryQueue.add(files[i]);
                    }else{
                        if(isCorpus(curr))
                            checkLastModified(files[i]);
                    }
                }
                try {
                    Thread.sleep(MyProperties.getInstance().dir_crawler_sleep_time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
    public void addNewDirectory(String path){
        File dir = new File(path);
        if(dir.isDirectory()){
            synchronized (this){
                directories.add(path);
                System.out.println("dodat" + path);
            }
        }else System.err.println("directory " + path + " is not directory");
    }
    private boolean isCorpus(File f) {
        String name = f.getName();
        if(name.startsWith(MyProperties.getInstance().file_corpus_prefix)) return true;
        else return false;
    }
    public void checkLastModified(File f) {
        lastModified.computeIfPresent(f.getAbsolutePath(), (key, value) -> {

            if (value != f.lastModified()) {

                File parent = f.getParentFile();

                if(parent!=null) {
                    try {
                        jobQueue.put(new FileScanner(fileScannerThreadPool, parent.getAbsolutePath()));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return value;
        });
        lastModified.putIfAbsent(f.getAbsolutePath(), f.lastModified());
    }
    private void startJob(File f) {
        lastModified.computeIfAbsent(f.getAbsolutePath(), (key) -> {
            try {
                System.out.println("uslo");
                jobQueue.put(new FileScanner(fileScannerThreadPool,f.getAbsolutePath()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return f.lastModified();
        });

    }
    public boolean isRun() {
        return run;
    }

    public void setRun(boolean run) {
        this.run = run;
    }
}
