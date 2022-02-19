package us.fatehi.whatstats;

import static java.lang.System.lineSeparator;

import java.time.ZoneOffset;
import java.util.regex.Pattern;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

import us.fatehi.whatstats.entity.Message;

/**
 * Buffer lines for a given chat message at a time, since we do not know how many lines a chat
 * message is going to be. Use a modified version of the technique in
 * https://docs.spring.io/spring-batch/docs/current/reference/html/common-patterns.html#multiLineRecords
 */
public class MessageItemReader implements ResourceAwareItemReaderItemStream<Message> {

  private static final Pattern linePrefix =
      Pattern.compile("\\[\\d{1,2}\\/\\d{1,2}\\/\\d{1,2}, \\d{1,2}:\\d{1,2}:\\d{1,2}\\] .*");

  private final FlatFileItemReader<String> delegate;
  private StringBuilder buffer;
  private final MessageLineMapper lineMapper;

  public MessageItemReader(final ZoneOffset fileTimeZoneOffset) {

    lineMapper = new MessageLineMapper(fileTimeZoneOffset);

    delegate = new FlatFileItemReader<String>();
    delegate.setName("messageItemReader");
    delegate.setLineMapper((line, lineNumber) -> line);

    buffer = new StringBuilder();
  }

  @Override
  public void close() throws ItemStreamException {
    this.delegate.close();
  }

  @Override
  public void open(final ExecutionContext executionContext) throws ItemStreamException {
    this.delegate.open(executionContext);
  }

  /** @see org.springframework.batch.item.ItemReader#read() */
  @Nullable
  @Override
  public Message read() throws Exception {
    for (String readLine; (readLine = this.delegate.read()) != null; ) {
      final String line = readLine.replaceAll("\u200E", "");
      if (buffer.isEmpty()) {
        buffer.append(line);
      } else if (linePrefix.matcher(line).matches()) {
        final Message message = lineMapper.mapLine(buffer.toString(), 0);
        buffer = new StringBuilder();
        buffer.append(line);
        return message;
      } else {
        buffer.append(lineSeparator()).append(line);
      }
    }

    if (buffer.isEmpty()) {
      return null;
    } else {
      final Message message = lineMapper.mapLine(buffer.toString(), 0);
      buffer = new StringBuilder();
      return message;
    }
  }

  @Override
  public void setResource(final Resource resource) {
    delegate.setResource(resource);
  }

  @Override
  public void update(final ExecutionContext executionContext) throws ItemStreamException {
    this.delegate.update(executionContext);
  }
}
