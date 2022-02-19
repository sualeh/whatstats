package us.fatehi.whatstats.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import us.fatehi.whatstats.entity.Contact;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

  List<Contact> findByName(String name);
}
