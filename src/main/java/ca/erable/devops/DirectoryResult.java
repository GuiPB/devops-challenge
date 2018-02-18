package ca.erable.devops;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * This class is a placeholder for a directory analysis result
 * 
 * @author guillaume
 *
 */
public class DirectoryResult {

    private Integer fileCount = 0;
    private Long fileSize = 0L;
    private List<String> commonPrefixes;
    private Date lastModified;

    public DirectoryResult(Integer fileCount, Long fileSize, List<String> comPrefixes, Date lastModified) {
        this.fileCount = fileCount;
        this.fileSize = fileSize;
        this.commonPrefixes = comPrefixes;
        this.lastModified = lastModified;
    }

    public Integer getFileCount() {
        return fileCount;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public List<String> getCommonPrefixes() {
        return Collections.unmodifiableList(commonPrefixes);
    }

    public Date getLastModified() {
        return lastModified;
    }
}
