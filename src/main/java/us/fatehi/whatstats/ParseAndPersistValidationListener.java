package us.fatehi.whatstats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class ParseAndPersistValidationListener extends StepExecutionListenerSupport {

  private static final Logger log =
      LoggerFactory.getLogger(ParseAndPersistValidationListener.class);

  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public ParseAndPersistValidationListener(final JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public ExitStatus afterStep(final StepExecution stepExecution) {
    final Long numMessages =
        jdbcTemplate.queryForObject(
            """
  		SELECT
  		  COUNT(MESSAGE_ID) AS NUM_MESSAGES
  		FROM
  		  MESSAGES
  		""",
            Long.class);
    if (numMessages == null || numMessages.longValue() <= 0) {
      return ExitStatus.FAILED;
    } else {
      log.info(String.format("Loaded %d messages", numMessages));
      return ExitStatus.COMPLETED;
    }
  }
}
