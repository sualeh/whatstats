package us.fatehi.whatstats.entity;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import org.springframework.util.StringUtils;

@Entity
@Table(
    name = "CONTACTS",
    uniqueConstraints =
        @UniqueConstraint(
            name = "UNIQUE_CONTACT_NAMES",
            columnNames = {"CONTACT_NAME"}))
public class Contact implements Serializable {

  private static final long serialVersionUID = -6344248976540440146L;

  @Id
  @GeneratedValue
  @Positive
  @Column(name = "CONTACT_ID")
  private Long id;

  @NotNull
  @NotBlank(message = "Contact name cannot be blank")
  @Size(max = 100, message = "Contact name must not be longer that 100 characters")
  @Column(name = "CONTACT_NAME", length = 400)
  private String name;

  public Contact() {}

  public Contact(final String name) {
    this.name = requireNonNull(name);
    if (!StringUtils.hasText(name)) {
      throw new IllegalArgumentException("No contact name provided");
    }
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "Contact [id=" + id + ", name=" + name + "]";
  }
}
