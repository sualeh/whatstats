package us.fatehi.whatstats;

import static java.util.Objects.requireNonNull;
import static java.util.regex.Pattern.DOTALL;
import static org.springframework.util.StringUtils.hasText;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.batch.item.file.LineMapper;

import us.fatehi.whatstats.entity.Contact;
import us.fatehi.whatstats.entity.Message;
import us.fatehi.whatstats.entity.MessageType;

public class MessageLineMapper implements LineMapper<Message> {

  private static final String SYSTEM_USER = "System";

  private static final Pattern linePattern =
      Pattern.compile(
          "\\[(\\d{1,2}\\/\\d{1,2}\\/\\d{1,2}, \\d{1,2}:\\d{1,2}:\\d{1,2})\\] (.*?): (.*)", DOTALL);
  private static final Pattern adminActionPattern =
      Pattern.compile(
          "\\[(\\d{1,2}\\/\\d{1,2}\\/\\d{1,2}, \\d{1,2}:\\d{1,2}:\\d{1,2})\\]( )(.*)", DOTALL);
  private static final Pattern urlPattern =
      Pattern.compile(
          "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)");

  private static final DateTimeFormatter dateTimeFormatter =
      DateTimeFormatter.ofPattern("M/d/yy, HH:mm:ss");

  private final ZoneOffset fileTimeZoneOffset;

  public MessageLineMapper(final ZoneOffset fileTimeZoneOffset) {
    this.fileTimeZoneOffset = requireNonNull(fileTimeZoneOffset);
  }

  @Override
  public Message mapLine(final String line, final int lineNumber) throws Exception {
    if (line == null) {
      return null;
    }

    Matcher matcher = linePattern.matcher(line);
    if (!matcher.matches()) {
      matcher = adminActionPattern.matcher(line);
      if (!matcher.matches()) {
        throw new RuntimeException(String.format("Could not parse line:%n%s", line));
      }
    }

    final Instant sent = sent(matcher.group(1));
    final String from = matcher.group(2);
    final String message = matcher.group(3);

    final MessageType messageType = messageType(from, message);
    final String messageText =
        switch (messageType) {
          case image:
          case video:
            {
              yield "";
            }
          default:
            yield message;
        };
    final String fromContact = hasText(from) ? from : SYSTEM_USER;

    final Message messageData =
        new Message(sent, new Contact(fromContact), messageText, messageType);
    return messageData;
  }

  private MessageType messageType(final String from, final String message) {
    if (!hasText(message)) {
      return MessageType.message;
    }
    if (from.equalsIgnoreCase(" ")) {
      return MessageType.information;
    }
    if (message.equalsIgnoreCase("image omitted")) {
      return MessageType.image;
    }
    if (message.equalsIgnoreCase("video omitted")) {
      return MessageType.video;
    }
    if (urlPattern.matcher(message).matches()) {
      return MessageType.link;
    }

    return MessageType.message;
  }

  private Instant sent(final String dateTimeString) {
    if (!hasText(dateTimeString)) {
      return null;
    }
    final LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, dateTimeFormatter);
    return dateTime.toInstant(fileTimeZoneOffset);
  }
}
