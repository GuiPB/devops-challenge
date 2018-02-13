package ca.erable.devops;

public class DirectoryResult {

    private Integer fileCount = 0;
    private Long fileSize = 0L;

    public DirectoryResult(Integer fileCount, Long fileSize) {
        this.fileCount = fileCount;
        this.fileSize = fileSize;
    }

    public Integer fileCount() {
        return fileCount;
    }

    public Long fileSize() {
        return fileSize;
    }

}
