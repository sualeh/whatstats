package us.fatehi.whatstats;

import java.io.Serializable;

import org.springframework.core.io.PathResource;
import org.springframework.core.io.WritableResource;

public class SqlReportDefinition implements Serializable {

  private static final long serialVersionUID = 9101443136783137368L;

  private final String reportFile;
  private final String query;

  public SqlReportDefinition(final String query, final String reportFile) {
    this.query = query;
    this.reportFile = reportFile;
  }

  public String getQuery() {
    return query;
  }

  public WritableResource getWritableResource() {
    return new PathResource(reportFile);
  }
}
