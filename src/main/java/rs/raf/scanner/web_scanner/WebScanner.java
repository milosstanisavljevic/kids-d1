package rs.raf.scanner.web_scanner;

import rs.raf.job_desc.ScanType;
import rs.raf.job_desc.ScanningJob;

public class WebScanner implements ScanningJob {
    @Override
    public ScanType getType() {
        return ScanType.WEB;
    }

    @Override
    public String getQuery() {
        return null;
    }
}
