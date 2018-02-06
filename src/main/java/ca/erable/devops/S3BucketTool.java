package ca.erable.devops;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.amazonaws.regions.Regions;

public class S3BucketTool {

    public static void main(String[] args) {

        Options options = new Options();
        options.addOption(Option.builder("h").longOpt("help").desc("Prints usage").hasArg(false).build());
        options.addOption(Option.builder("hr").longOpt("human-readable").desc("Shows file size in a human readable format. Ex: kB, MB, GB...").build());
        options.addOption(Option.builder("gr").longOpt("group-by-region").desc("Groups results by regions i.e. summarize by region instead of by bucket").hasArg().build());
        options.addOption(Option.builder("st").longOpt("stockage-type").desc("Filters shown information by a specified stockage type").hasArg().build());
        options.addOption(Option.builder("rep").longOpt("regular-exp").desc("Filter results by bucket name matching a given 'Java Pattern' regular expression").hasArg().build());

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);

            Regions region = Regions.DEFAULT_REGION;

            boolean humanReadable = false;

            if (line.hasOption("hr")) {
                humanReadable = true;
            }

            boolean groupByRegion = false;

            if (line.hasOption("gr")) {
                groupByRegion = true;
            }

            StorageFilter filterByStorage = StorageFilter.NO_FILTER;

            if (line.hasOption("st")) {
                String storageOpt = line.getOptionValue("st");
                filterByStorage = StorageFilter.valueOf(storageOpt.toUpperCase());
            }

            String pattern = "";

            if (line.hasOption("rep")) {
                pattern = line.getOptionValue("rep");
            }

            AmazonS3Service service = new AmazonS3ServiceImpl();

            BucketsAnalyser analyser = new BucketsAnalyser(service);

            analyser.analyse();

            List<BucketReport> allReports = analyser.getAllReports();
            allReports.stream().forEach(report -> report.show());
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("s3buckettool", options);
            e.printStackTrace();
        }

    }
}
