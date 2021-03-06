package ca.erable.devops;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import com.amazonaws.regions.Regions;

/**
 * This class serves as a report from a bucket analysis
 * 
 * @author guillaume
 *
 */
public class BucketReport {

    private Integer fileCount = 0;
    private Date creationDate;
    private String name;
    private Long totalFileSize = 0L;
    private Date lastModifiedDate = null;
    private String bucketLocation = Regions.DEFAULT_REGION.toString();

    /**
     * 
     * @param name
     * @param creationDate
     * @param bucketLocation
     * @param fileCountParam
     * @param totalFileSizeParam
     * @param lastModifiedParam
     */
    public BucketReport(String name, Date creationDate, String bucketLocation, Integer fileCountParam, Long totalFileSizeParam, Date lastModifiedParam) {
        this.name = name;
        this.creationDate = creationDate;
        this.fileCount = fileCountParam;
        this.totalFileSize = totalFileSizeParam;
        this.lastModifiedDate = lastModifiedParam;
        this.bucketLocation = bucketLocation;
    }

    public Integer getFileCount() {
        return fileCount;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getName() {
        return name;
    }

    public Long getTotalFileSize() {
        return totalFileSize;
    }

    public String getBucketLocation() {
        return bucketLocation;
    }

    public String toReadableFileSize() {
        return toReadableFileSize(totalFileSize);
    }

    /**
     * This method will convert a number of byte to a nearest adequate unit. B kB MB
     * TB PB EB. It will convert on a internationnal standard.
     * 
     * Ref:
     * https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
     * 
     * @param bytes
     * @return
     */
    public String toReadableFileSize(Long bytes) {
        // Ce code provient d'une solution propose par un developpeur sur stackoverflow.
        // Il est rare que je copie des lignes de code, mais j'ai confiance en celles-ci
        // :)
        // Reference:
        // https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
        int unit = 1000;

        // Retour premature si on est en bas du 1000 B
        if (bytes < unit)
            return bytes + " B";

        // Determiner l'exposant de l'unitee de mesure la plus adequate
        int exp = (int) (Math.log(bytes) / Math.log(unit));

        // Sert a determiner l'unitee de mesure qu'on va employer. Base sur la position
        // de l'exposant
        char pre = "kMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bucketLocation == null) ? 0 : bucketLocation.hashCode());
        result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
        result = prime * result + ((fileCount == null) ? 0 : fileCount.hashCode());
        result = prime * result + ((lastModifiedDate == null) ? 0 : lastModifiedDate.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((totalFileSize == null) ? 0 : totalFileSize.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BucketReport other = (BucketReport) obj;
        if (bucketLocation == null) {
            if (other.bucketLocation != null)
                return false;
        } else if (!bucketLocation.equals(other.bucketLocation))
            return false;
        if (creationDate == null) {
            if (other.creationDate != null)
                return false;
        } else if (!creationDate.equals(other.creationDate))
            return false;
        if (fileCount == null) {
            if (other.fileCount != null)
                return false;
        } else if (!fileCount.equals(other.fileCount))
            return false;
        if (lastModifiedDate == null) {
            if (other.lastModifiedDate != null)
                return false;
        } else if (!lastModifiedDate.equals(other.lastModifiedDate))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (totalFileSize == null) {
            if (other.totalFileSize != null)
                return false;
        } else if (!totalFileSize.equals(other.totalFileSize))
            return false;
        return true;
    }

    public void show(boolean humanReadable, OutputStream out) {
        try {
            out.write("*********************************************".getBytes());
            out.write(System.lineSeparator().getBytes());
            out.write(("name: " + getName()).getBytes());
            out.write(System.lineSeparator().getBytes());
            out.write(("location: " + getBucketLocation().toString()).getBytes());
            out.write(System.lineSeparator().getBytes());
            out.write(("creation date: " + getCreationDate()).getBytes());
            out.write(System.lineSeparator().getBytes());
            out.write(("last modified: " + getLastModifiedDate()).getBytes());
            out.write(System.lineSeparator().getBytes());
            out.write(("file count: " + getFileCount()).getBytes());
            out.write(System.lineSeparator().getBytes());
            if (humanReadable) {
                out.write(("size: " + toReadableFileSize()).getBytes());
            } else {
                out.write(("size: " + getTotalFileSize()).getBytes());
            }
            out.write(System.lineSeparator().getBytes());
            out.write(("*********************************************").getBytes());
            out.write(System.lineSeparator().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
