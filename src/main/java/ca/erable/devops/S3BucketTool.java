package ca.erable.devops;

import java.awt.IllegalComponentStateException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class S3BucketTool {

    public static void main(String[] args) {

        Options options = new Options();
        options.addOption(Option.builder("h").longOpt("help").desc("Prints usage").hasArg(false).build());
        options.addOption(Option.builder("hr").longOpt("human-readable").desc("Shows file size in a human readable format. Ex: kB, MB, GB...").build());
        options.addOption(Option.builder("gr").longOpt("group-by-region").desc("Groups results by regions i.e. summarize by region instead of by bucket").build());
        options.addOption(Option.builder("st").longOpt("stockage-type").desc("Filters shown information by a specified stockage type").hasArg().build());
        options.addOption(Option.builder("regex").longOpt("regular-exp").desc("Filter results by bucket name matching a given 'Java Pattern' regular expression").hasArg().build());
        options.addOption(Option.builder("json").longOpt("json-output").desc("Outputs is json formatted").build());

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("s3buckettool", options);
            } else {

                // Config par defaut de l'app
                boolean humanReadable = false;
                boolean groupByRegion = false;
                boolean jsonFormated = false;
                StorageFilter filterByStorage = StorageFilter.NO_FILTER;
                String pattern = ".*";

                if (line.hasOption("hr")) {
                    humanReadable = true;
                }

                if (line.hasOption("gr")) {
                    groupByRegion = true;
                }

                if (line.hasOption("st")) {
                    String storageOpt = line.getOptionValue("st");
                    filterByStorage = StorageFilter.valueOf(storageOpt.toUpperCase());
                }

                if (line.hasOption("regex")) {
                    pattern = line.getOptionValue("regex");
                }

                if (line.hasOption("json")) {
                    jsonFormated = true;
                }

                S3BucketTool s3BucketTool = new S3BucketTool();
                s3BucketTool.runApp(humanReadable, groupByRegion, jsonFormated, filterByStorage, pattern);

            }
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("s3buckettool", options);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void runApp(boolean humanReadable, boolean groupByRegion, boolean jsonFormated, StorageFilter filterByStorage, String pattern) throws InterruptedException, JsonProcessingException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        AmazonS3ClientBuilder clientBuilder = ctx.getBean(AmazonS3ClientBuilder.class);

        if (clientBuilder == null) {
            ctx.close();
            throw new IllegalComponentStateException("Cannot create an AmazonS3ClientBuilder");
        }

        AmazonS3Service service = new AmazonS3ServiceImpl(clientBuilder);

        BucketsAnalyser analyser = new BucketsAnalyser(service);

        analyser.analyseBuckets(filterByStorage, pattern);

        List<BucketReport> allReports = analyser.getAllReports();

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        if (groupByRegion) {
            // Bucketed hashmap. Chaque clef comprend une liste.
            Map<String, List<BucketReport>> groupedByRegion = allReports.stream().collect(Collectors.groupingBy(BucketReport::getBucketLocation));
            for (String region : groupedByRegion.keySet()) {
                System.out.println(region.toString());
                List<BucketReport> list = groupedByRegion.get(region);
                for (BucketReport rep : list) {
                    showResults(humanReadable, jsonFormated, ow, rep);
                }

            }
        } else {
            for (BucketReport rep : allReports) {
                showResults(humanReadable, jsonFormated, ow, rep);
            }
        }

        ctx.close();
    }

    private void showResults(boolean humanReadable, boolean jsonFormated, ObjectWriter ow, BucketReport rep) throws JsonProcessingException {
        PrintStream out = System.out;
        if (jsonFormated) {
            String json = ow.writeValueAsString(rep);
            out.print(json + System.lineSeparator());
        } else {
            rep.show(humanReadable, out);
        }
    }
}
