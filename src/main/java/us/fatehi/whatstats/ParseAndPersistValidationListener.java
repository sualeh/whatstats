package us.fatehi.whatstats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.jdbc.core.JdbcTemplate;

public class ParseAndPersistValidationListener implements StepExecutionListener {

  private static final Logger log =
      LoggerFactory.getLogger(ParseAndPersistValidationListener.class);

  private final JdbcTemplate jdbcTemplate;

  public ParseAndPersistValidationListener(final JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public ExitStatus afterStep(final StepExecution stepExecution) {
    final long numMessages =
        jdbcTemplate.queryForObject(
            """
  		SELECT
  		  COUNT(MESSAGE_ID) AS NUM_MESSAGES
  		FROM
  		  MESSAGES
  		""",
            long.class);
    if (numMessages <= 0) {
      return ExitStatus.FAILED;
    } else {
      log.info(String.format("Loaded %d messages", numMessages));
      return ExitStatus.COMPLETED;
    }
  }

  @Override
  public void beforeStep(final StepExecution stepExecution) {
    // No-op
  }
}
