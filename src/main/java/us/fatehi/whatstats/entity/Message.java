package us.fatehi.whatstats.entity;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Entity
@Table(name = "MESSAGES")
public final class Message implements Serializable {

  private static final long serialVersionUID = -4888655107701591263L;

  @Id
  @GeneratedValue
  @Positive
  @Column(name = "MESSAGE_ID")
  private Long id;

  @NotNull
  @Past
  @Column(name = "MESSAGE_SENT")
  private Instant sent;

  @NotNull
  @ManyToOne(cascade = {CascadeType.ALL})
  @JoinColumn(name = "CONTACT_ID", nullable = false)
  private Contact from;

  @NotNull
  @Size(max = 65535)
  @Column(name = "MESSAGE_TEXT")
  private String message;

  @NotNull
  @Column(name = "MESSAGE_TYPE")
  @Enumerated(EnumType.STRING)
  private MessageType type;

  public Message() {}

  public Message(
      final Instant sent, final Contact from, final String message, final MessageType type) {
    this.sent = requireNonNull(sent);
    this.from = requireNonNull(from);
    this.message = requireNonNull(message);
    this.type = requireNonNull(type);
  }

  public Contact getFrom() {
    return from;
  }

  public Long getId() {
    return id;
  }

  public String getMessage() {
    return message;
  }

  public Instant getSent() {
    return sent;
  }

  public MessageType getType() {
    return type;
  }

  public void setFrom(final Contact from) {
    this.from = requireNonNull(from);
  }

  @Override
  public String toString() {
    return "Message [id="
        + id
        + ", sent="
        + sent
        + ", from="
        + from
        + ", message="
        + message
        + ", type="
        + type
        + "]";
  }
}
