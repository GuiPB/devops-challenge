package ca.erable.devops;

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

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class S3BucketTool {

    public static void main(String[] args) {

        Options options = new Options();
        options.addOption(Option.builder("h").longOpt("help").desc("Prints usage").hasArg(false).build());
        options.addOption(Option.builder("hr").longOpt("human-readable").desc("Shows file size in a human readable format. Ex: kB, MB, GB...").build());
        options.addOption(Option.builder("gr").longOpt("group-by-region").desc("Groups results by regions i.e. summarize by region instead of by bucket").build());
        options.addOption(Option.builder("st").longOpt("stockage-type").desc("Filters shown information by a specified stockage type").hasArg().build());
        options.addOption(Option.builder("regex").longOpt("regular-exp").desc("Filter results by bucket name matching a given 'Java Pattern' regular expression").hasArg().build());

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

                AmazonS3ClientBuilder clientBuilder = AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION);
                AmazonS3Service service = new AmazonS3ServiceImpl(clientBuilder);

                BucketsAnalyser analyser = new BucketsAnalyser(service);

                analyser.analyseBuckets(filterByStorage, pattern);

                List<BucketReport> allReports = analyser.getAllReports();

                if (groupByRegion) {
                    // Bucketed hashmap. Chaque clef comprend une liste.
                    Map<Regions, List<BucketReport>> groupedByRegion = allReports.stream().collect(Collectors.groupingBy(BucketReport::getBucketLocation));
                    for (Regions region : groupedByRegion.keySet()) {
                        System.out.println(region.toString());
                        List<BucketReport> list = groupedByRegion.get(region);
                        for (BucketReport rep : list) {
                            rep.show(humanReadable);
                        }

                    }
                } else {
                    for (BucketReport rep : allReports) {
                        rep.show(humanReadable);
                    }
                }

            }
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("s3buckettool", options);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
