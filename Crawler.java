package crawler;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler extends SwingWorker<String, String> {
    BlockingQueue<ArrayList<String>> urlsPool;
    ConcurrentMap<String ,String> links;
    WebCrawler webCrawler;

    public Crawler (BlockingQueue<ArrayList<String>> urlsPool, ConcurrentMap<String ,String> links, WebCrawler webCrawler){
        this.urlsPool = urlsPool;
        this.links = links;
        this.webCrawler = webCrawler;
    }

    @Override
    protected String doInBackground() {
        while (!isCancelled()) {
            try {
                ArrayList<String> task = urlsPool.take();

                if (links.containsKey(task.get(0))) {
                    continue;
                }

                URLConnection connection = new URL(task.get(0)).openConnection();
                connection.setRequestProperty("User-Agent", "Google chrome/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");

                if (!connection.getContentType().startsWith("text/html")) {
                    continue;
                }

                InputStream inputStream = new BufferedInputStream(connection.getInputStream());

                String siteHTML = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

                String title = getTitle(siteHTML);

                links.put(task.get(0), title);
                webCrawler.parsedPages();

                int depth = Integer.parseInt(task.get(1));

                if (depth == webCrawler.getDepth()) {
                    continue;
                }

                depth++;

                String[] url = task.get(0).split("/+");
                String protocol = url[0];
                String domain = url[1];

                Matcher matcher = Pattern.compile("href=[\"'].*?[\"']").matcher(siteHTML);

                while (matcher.find()) {
                    String newUrl = matcher.group().substring(6, matcher.end() - matcher.start() - 1);

                    if (newUrl.startsWith("//")) newUrl = protocol + newUrl;
                    else if (newUrl.startsWith("/")) newUrl = protocol + "//" + domain + newUrl;
                    else if (!newUrl.startsWith("http")) newUrl = protocol + "//" + domain + "/" + newUrl;

                    for (int i = depth; i >= 0; i--) {
                        if (urlsPool.contains(new ArrayList<>(Arrays.asList(newUrl, String.valueOf(i)))) || links.containsKey(newUrl)) {
                            break;
                        } else if (i == 0){
                            urlsPool.add(new ArrayList<>(Arrays.asList(newUrl, String.valueOf(depth))));
                        }
                    }
                }
            } catch (Exception ignored) {

            }
        }
        return null;
    }

    public String getTitle(String siteHTML) {
        Matcher matcher = Pattern.compile("(<title>)(.*)(</title>)").matcher(siteHTML);
        if (matcher.find()) return matcher.group(2);
        return null;
    }


}
