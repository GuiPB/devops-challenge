package ca.erable.coveo;

import java.util.Date;
import java.util.List;

import com.amazonaws.services.s3.model.S3ObjectSummary;

public class BucketReport {

	private Integer fileCount;
	private Date creationDate;
	private String name;
	private Long totalFileSize = 0L;
	private Date lastModifiedDate = null;

	public BucketReport(String name, Date creationDate, List<S3ObjectSummary> objects) {
		this.name = name;
		this.fileCount = objects.size();
		this.creationDate = creationDate;
		if (!objects.isEmpty()) {
			this.totalFileSize = objects.stream().mapToLong(S3ObjectSummary::getSize).sum();
			this.lastModifiedDate = objects.stream()
					.sorted((f, g) -> g.getLastModified().compareTo(f.getLastModified())).findFirst().get()
					.getLastModified();
		}
	}

	public Integer fileCount() {
		return fileCount;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public String getName() {
		return name;
	}

	public Long totalFileSize() {
		return totalFileSize;
	}

	public String totalReadableFileSize() {
		return new String();
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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

}
