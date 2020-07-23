package crawler;

import javax.swing.*;

public class Timer extends SwingWorker<String, String> {
    private WebCrawler webCrawler;

    public Timer(WebCrawler webCrawler) {
        this.webCrawler = webCrawler;
    }

    @Override
    protected String doInBackground() throws Exception {
        while (!isCancelled()) {
            Thread.sleep(1000);
            webCrawler.addTime();
        }
        return null;
    }
}
