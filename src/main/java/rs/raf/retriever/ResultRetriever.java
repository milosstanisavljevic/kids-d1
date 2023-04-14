package rs.raf.retriever;

import rs.raf.util.MyProperties;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ResultRetriever implements Callable<Map<String, Map<String, Integer>>> {
    private final JobType type;
    private final String[] keywords;
    private final String domain;
    private final ConcurrentHashMap<String, Future<Map<String, Integer>>> fileResults;
    private final ConcurrentHashMap<String, Future<Map<String, Integer>>> webResults;

    public ResultRetriever(
            ConcurrentHashMap<String, Future<Map<String, Integer>>> webResults,
            ConcurrentHashMap<String, Future<Map<String, Integer>>> fileResults,
            String domain,
            JobType type
    ) {
        this.webResults = webResults;
        this.fileResults = fileResults;
        this.domain = domain;
        this.type = type;
        this.keywords = MyProperties.getInstance().keywords;
    }

//    public Map<String, Map<String, Integer>> summaryWeb() throws ExecutionException, InterruptedException {
//        Map<String, Map<String, Integer>> result = new HashMap<>();
//        for (Map.Entry<String, Future<Map<String, Integer>>> entry : webResults.entrySet()) {
//            String url = entry.getKey();
//            URI uri;
//            try {
//                uri = new URI(url);
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//                continue;
//            }
//            String host = uri.getHost();
//            Map<String, Integer> map = entry.getValue().get();
//            result.computeIfAbsent(host, k -> new HashMap<>());
//            for (Map.Entry<String, Integer> e : map.entrySet()) {
//                String keyword = e.getKey();
//                Integer count = e.getValue();
//                result.get(host).merge(keyword, count, Integer::sum);
//            }
//        }
//        return result;
//    }

    public Map<String, Map<String, Integer>> summaryFile() throws ExecutionException, InterruptedException {
        Map<String, Map<String, Integer>> result = new HashMap<>();
        for (Map.Entry<String, Future<Map<String, Integer>>> entry : fileResults.entrySet()) {
            result.put(entry.getKey(), entry.getValue().get());
        }
        return result;
    }


    @Override
    public Map<String, Map<String, Integer>> call() throws Exception {
        return summaryFile();
//        switch (type) {
////            case WEBSUMMARY:
////                return summaryWeb();
//            case FILESUMMARY:
//                return summaryFile();
//            default:
//                throw new IllegalArgumentException("Unknown job type: " + type);
//        }
    }
}
