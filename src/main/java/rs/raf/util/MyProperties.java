package rs.raf.util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

public class MyProperties {
    private static MyProperties instance = null;
    public String file_corpus_prefix;
    public long dir_crawler_sleep_time;
    public String[] keywords;
    public long file_scanning_size_limit;
    public int hop_count;
    public long url_refresh_time;


    public MyProperties() {
        InputStream inputStream;
        try {
            Properties prop = new Properties();
            String propFileName = "app.properties";

            inputStream  = getClass().getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            file_corpus_prefix = prop.getProperty("file_corpus_prefix");
            dir_crawler_sleep_time = Long.parseLong(prop.getProperty("dir_crawler_sleep_time"));
            String words = prop.getProperty("keywords");
            keywords = words.split(",");
            file_scanning_size_limit = Long.parseLong(prop.getProperty("file_scanning_size_limit"));
            hop_count = Integer.parseInt(prop.getProperty("hop_count"));
            url_refresh_time = Long.parseLong(prop.getProperty("url_refresh_time"));

            inputStream.close();

        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }

    }

    public static MyProperties getInstance() {
        if (instance == null)
            instance = new MyProperties();
        return instance;
    }

}
