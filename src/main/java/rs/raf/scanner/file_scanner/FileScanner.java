package rs.raf.scanner.file_scanner;

import rs.raf.job_desc.ScanType;
import rs.raf.job_desc.ScanningJob;
import rs.raf.util.MyProperties;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

public class FileScanner extends RecursiveTask<Map<String, Integer>> implements ScanningJob {
    private FileScannerThreadPool fileScannerThreadPool;
    private String path;
    private String corpusName;
    private String[] keywords;
    private List<File> files = new ArrayList<>();
    public FileScanner(FileScannerThreadPool fileScannerThreadPool, String path){
        this.fileScannerThreadPool = fileScannerThreadPool;
        this.path = path;
        this.corpusName = new File(path).getName();
        getFilesFromCorpus(path);
    }
    public FileScanner(FileScannerThreadPool fileScannerThreadPool,String corpusName, String path){
        this.fileScannerThreadPool = fileScannerThreadPool;
        this.path = path;
        this.corpusName = corpusName;
        getFilesFromCorpus(path);
    }
    public void getFilesFromCorpus(String corpus){
        File[] filesTemp = new File(corpus).listFiles();
        for(int i=0; i<filesTemp.length; i++) {
            if(!filesTemp[i].isDirectory()) files.add(filesTemp[i]);
        }
    }

    @Override
    public ScanType getType() {
        return ScanType.FILE;
    }

    @Override
    public String getQuery() {
        return this.corpusName;
    }
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf);
    }

    @Override
    protected Map<String, Integer> compute() {
        fileScannerThreadPool.jobStarted(corpusName);

        Map<String, Integer> result = new HashMap<>();
        List<File> listOfFiles = splitTheJob(this.files);

        if (listOfFiles.isEmpty()) {
            fileScannerThreadPool.jobEnded(corpusName);
            return result;
        }

        for (String keyword : keywords) {
            result.put(keyword, 0);
        }

        List<ForkJoinTask<Map<String, Integer>>> subtasks = new ArrayList<>();
        for (File file : listOfFiles) {
            if (!getFileExtension(file).equals(".txt")) {
                System.out.println("File " + file.getName() + " isn't txt.");
                continue;
            }

            FileScanner subtask = new FileScanner(fileScannerThreadPool, this.corpusName, file.getAbsolutePath());
            subtasks.add(subtask);
            subtask.fork();
        }

        Map<String, Integer> joinMap = new HashMap<>();
        for (ForkJoinTask<Map<String, Integer>> subtask : subtasks) {
            Map<String, Integer> subResult = subtask.join();
            for (Map.Entry<String, Integer> entry : subResult.entrySet()) {
                joinMap.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }

        result.putAll(joinMap);

        fileScannerThreadPool.jobEnded(corpusName);
        return result;
    }

    public List<File> splitTheJob(List<File> files) {
        List<File> result = new ArrayList<>();
        long totalSize = 0;
        for (File file : files) {
            if (totalSize + file.length() > MyProperties.getInstance().file_scanning_size_limit) {
                break;
            }
            result.add(file);
            totalSize += file.length();
        }
        files.removeAll(result);
        return result;
    }
}
