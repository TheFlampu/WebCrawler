package crawler;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawler extends JFrame {
    private final JTextField urlField;
    private final JTextField workersField;
    private final JTextField depthField;
    private final JTextField timeField;
    private final JLabel elapsedField;
    private final JLabel pagesField;
    private final JTextField exportField;
    private final JToggleButton runBtn = new JToggleButton("Run");
    private final JCheckBox depthBtn = new JCheckBox("Enable");
    private final JCheckBox timeBtn = new JCheckBox("Enable");

    private Timer timer;
    private int seconds;
    private ArrayList<Crawler> workers = new ArrayList<>();
    private BlockingQueue<ArrayList<String>> urlsPool = new LinkedBlockingDeque<>();
    private ConcurrentMap<String ,String> links = new ConcurrentHashMap<>();

    public WebCrawler() {
        setTitle("Web Crawler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(600, 220));
        setLocationRelativeTo(null);
        setLayout(new FlowLayout(){{setAlignment(FlowLayout.LEFT);}});

        JLabel urlLabel = new JLabel("Start URL");
        JLabel workersLabel = new JLabel("Workers:");
        JLabel depthLabel = new JLabel("Maximum depth:");
        JLabel timeLabel = new JLabel("Time limit:");
        JLabel elapsedLabel = new JLabel("Elapsed time:");
        JLabel secondsLabel = new JLabel("seconds");
        JLabel pagesLabel = new JLabel("Parsed pages:");
        JLabel exportLabel = new JLabel("Export:");

        urlLabel.setPreferredSize(new Dimension(110, 20));
        workersLabel.setPreferredSize(new Dimension(110, 20));
        depthLabel.setPreferredSize(new Dimension(110, 20));
        timeLabel.setPreferredSize(new Dimension(110, 20));
        elapsedLabel.setPreferredSize(new Dimension(110, 20));
        pagesLabel.setPreferredSize(new Dimension(110, 20));
        exportLabel.setPreferredSize(new Dimension(110, 20));

        urlField = new JTextField("");
        workersField = new JTextField("");
        depthField = new JTextField("");
        timeField = new JTextField("");
        elapsedField = new JLabel("0:00");
        pagesField = new JLabel("0");
        exportField = new JTextField("");

        urlField.setPreferredSize(new Dimension(380,20));
        workersField.setPreferredSize(new Dimension(455,20));
        depthField.setPreferredSize(new Dimension(380,20));
        timeField.setPreferredSize(new Dimension(326,20));
        elapsedField.setPreferredSize(new Dimension(400,20));
        pagesField.setPreferredSize(new Dimension(400,20));
        exportField.setPreferredSize(new Dimension(380,20));

        JButton exportBtn = new JButton("Save");

        runBtn.setPreferredSize(new Dimension(75,20));
        depthBtn.setPreferredSize(new Dimension(75,20));
        timeBtn.setPreferredSize(new Dimension(75,20));
        exportBtn.setPreferredSize(new Dimension(75,20));

        add(urlLabel);
        add(urlField);
        add(runBtn);
        add(workersLabel);
        add(workersField);
        add(depthLabel);
        add(depthField);
        add(depthBtn);
        add(timeLabel);
        add(timeField);
        add(secondsLabel);
        add(timeBtn);
        add(elapsedLabel);
        add(elapsedField);
        add(pagesLabel);
        add(pagesField);
        add(exportLabel);
        add(exportField);
        add(exportBtn);

        urlField.setName("UrlTextField");
        runBtn.setName("RunButton");
        exportField.setName("ExportUrlTextField");
        exportBtn.setName("ExportButton");
        pagesField.setName("ParsedLabel");
        depthBtn.setName("DepthCheckBox");
        depthField.setName("DepthTextField");


        revalidate();
        setVisible(true);
        depthBtn.setSelected(true);

        runBtn.addActionListener(actionEvent -> {
            if(runBtn.isSelected()) {
                timer = new Timer(this);
                timer.execute();
                workers.clear();
                int countWorkers = workersField.getText().equals("") ? 1 : Integer.parseInt(workersField.getText());
                for (int i = 0; i < countWorkers; i++) {
                    workers.add(new Crawler(urlsPool, links, this));
                    workers.get(i).execute();
                }
                urlsPool.add(new ArrayList<>(Arrays.asList(urlField.getText(), "0")));
            } else {
                for (Crawler worker : workers) {
                    worker.cancel(true);
                }
                timer.cancel(true);
            }
        });

        exportBtn.addActionListener(actionEvent -> {
            try (FileWriter writer = new FileWriter(new File(exportField.getText()))) {
                boolean isFirst = true;
                for (Map.Entry<String, String> el : links.entrySet()) {
                    if (isFirst) {
                        writer.write(el.getKey() + "\n" + el.getValue());
                        isFirst = false;
                        continue;
                    }
                    writer.write("\n" + el.getKey() + "\n" + el.getValue());
                }
                seconds = 0;
                links.clear();
            } catch (Exception ignored) {
            }
        });

        timeBtn.addActionListener(actionEvent -> {
            for (var link : links.entrySet()) {
                System.out.println(link.getKey() + " " + link.getValue());
            }
        });

        depthBtn.addActionListener(actionEvent -> {
        });
    }

    public void parsedPages() {
        pagesField.setText(String.valueOf(links.size()));
    }

    public void addTime() {
        if (urlsPool.isEmpty() && seconds >= 1){
            seconds = 0;
            runBtn.doClick();
        } else if (seconds < (timeBtn.isSelected() ? Integer.parseInt(timeField.getText()) : Integer.MAX_VALUE)) {
            seconds++;
            elapsedField.setText(seconds / 60 + ":" + (seconds % 60 <= 9 ? "0" + seconds % 60 : seconds % 60));
        } else {
            runBtn.doClick();
        }
    }

    public int getDepth() {
        return depthBtn.isSelected() ? Integer.parseInt(depthField.getText()) : Integer.MAX_VALUE;
    }
}