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

public class Main {

    public static void main(String[] args) {

        Options options = new Options();
        options.addOption(Option.builder("h").desc("Shows file size in a human readable format. Ex: kB, MB, GB..").hasArg(false).build());
        options.addOption(Option.builder("r").longOpt("region").desc("Specifies a region for default AmazonS3Client").build());

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine parse = parser.parse(options, args);

            boolean humanReadable = parse.hasOption("h");

            AmazonS3Service service = new AmazonS3ServiceImpl(Regions.US_EAST_2);
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
