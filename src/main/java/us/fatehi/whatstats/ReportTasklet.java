package us.fatehi.whatstats;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class ReportTasklet implements Tasklet {

  private static final Logger log = LoggerFactory.getLogger(ReportTasklet.class);

  private final JdbcTemplate jdbcTemplate;

  private final List<SqlReportDefinition> reportDefinitions =
      List.of(
          new SqlReportDefinition(
              """
  				SELECT
  				  CONTACTS.CONTACT_NAME,
  				  MESSAGES.MESSAGE_TYPE,
  				  COUNT(MESSAGES.MESSAGE_ID) AS NUM_MESSAGES,
  				  AVG(LENGTH(MESSAGES.MESSAGE_TEXT)) AS AVG_MESSAGE_LENGTH
  				FROM
  				  MESSAGES
  				  INNER JOIN CONTACTS
  				    ON MESSAGES.CONTACT_ID = CONTACTS.CONTACT_ID
  				GROUP BY
  				  CONTACTS.CONTACT_NAME,
  				  MESSAGES.MESSAGE_TYPE
  				ORDER BY
  				  NUM_MESSAGES DESC
          		""",
              Paths.get("message_summary.csv")),
          new SqlReportDefinition(
              """
				SELECT
				  CONTACTS.CONTACT_NAME,
				  SUM(CASE WHEN MESSAGES.MESSAGE_TYPE = 'message' THEN 1 ELSE 0 END) * 3 +
				  SUM(CASE WHEN MESSAGES.MESSAGE_TYPE = 'image' THEN 1 ELSE 0 END) * -1 +
				  SUM(CASE WHEN MESSAGES.MESSAGE_TYPE = 'video' THEN 1 ELSE 0 END) * -1 +
				  SUM(CASE WHEN MESSAGES.MESSAGE_TYPE = 'image' THEN 1 ELSE 0 END) * -1
				    AS VALUE_ADDED,
  				  AVG(LENGTH(MESSAGES.MESSAGE_TEXT)) AS AVG_MESSAGE_LENGTH
				FROM
				  MESSAGES
				  INNER JOIN CONTACTS
				    ON MESSAGES.CONTACT_ID = CONTACTS.CONTACT_ID
				GROUP BY
				  CONTACTS.CONTACT_NAME
				ORDER BY
				  VALUE_ADDED DESC
              		""",
              Paths.get("value_added.csv")));

  @Autowired
  public ReportTasklet(final JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext)
      throws Exception {

    for (final SqlReportDefinition reportDefinition : reportDefinitions) {

      final List<Map<String, Object>> data = jdbcTemplate.queryForList(reportDefinition.getQuery());
      writeCsvReport(
          data, new OutputStreamWriter(reportDefinition.getWritableResource().getOutputStream()));
    }

    return RepeatStatus.FINISHED;
  }

  public void writeCsvReport(final List<Map<String, Object>> data, final Writer writer) {

    if (data == null || data.isEmpty()) {
      return;
    }
    requireNonNull(writer);

    try (final CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
      // Print header
      final Map<String, Object> firstRow = data.get(0);
      csvPrinter.printRecord(firstRow.keySet());
      // Print data
      for (final Map<String, Object> row : data) {
        csvPrinter.printRecord(row.values());
      }
    } catch (final IOException e) {
      log.error("Could not write report", e);
    }
  }
}
