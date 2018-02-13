package ca.erable.devops;

import java.util.List;

public class DirectoryResult {

    private Integer fileCount = 0;
    private Long fileSize = 0L;
    private List<String> commonPrefixes;

    public DirectoryResult(Integer fileCount, Long fileSize, List<String> comPrefixes) {
        this.fileCount = fileCount;
        this.fileSize = fileSize;
        this.commonPrefixes = comPrefixes;
    }

    public Integer getFileCount() {
        return fileCount;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public List<String> getCommonPrefixes() {
        return commonPrefixes;
    }
}
