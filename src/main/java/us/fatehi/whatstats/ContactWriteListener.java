package us.fatehi.whatstats;

import static java.lang.System.lineSeparator;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemWriteListener;

import us.fatehi.whatstats.entity.Contact;
import us.fatehi.whatstats.entity.Message;
import us.fatehi.whatstats.repository.ContactRepository;

public class ContactWriteListener implements ItemWriteListener<Message> {

  private static final Logger log = LoggerFactory.getLogger(ContactWriteListener.class);

  private final ContactRepository contactRepository;

  public ContactWriteListener(final ContactRepository contactRepository) {
    this.contactRepository = requireNonNull(contactRepository);
  }

  @Override
  public void afterWrite(final List<? extends Message> messages) {
    // No-op
  }

  /** Look up or create contacts, so we do not have duplicates. */
  @Override
  public void beforeWrite(final List<? extends Message> messages) {
    if (messages == null || messages.isEmpty()) {
      return;
    }

    for (final Message message : messages) {
      final String from = message.getFrom().getName();
      final List<Contact> contacts = contactRepository.findByName(from);
      final Contact contact;
      if (contacts.isEmpty()) {
        contact = new Contact(from);
        contactRepository.save(contact);
      } else {
        contact = contacts.get(0);
      }
      message.setFrom(contact);
    }
  }

  @Override
  public void onWriteError(final Exception exception, final List<? extends Message> messages) {
    log.warn(
        "Error writing batch"
            + lineSeparator()
            + messages.stream().collect(mapping(Message::toString, joining("\n\n\t"))));
  }
}
