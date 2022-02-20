package us.fatehi.whatstats;

import java.nio.file.Paths;
import java.time.ZoneOffset;

import javax.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import us.fatehi.whatstats.entity.Message;
import us.fatehi.whatstats.repository.ContactRepository;

@Configuration
@EnableAutoConfiguration
@EnableBatchProcessing
public class BatchConfiguration {

  @Autowired public JobBuilderFactory jobBuilderFactory;

  @Autowired public StepBuilderFactory stepBuilderFactory;

  @Autowired public ContactRepository contactRepository;

  @Bean
  public ContactWriteListener contactWriteListener() {
    return new ContactWriteListener(contactRepository);
  }

  @Bean(name = "AnalyzeMessagesJob")
  public Job job(final Step parseAndPersistStep, final Step reportStep) {
    return jobBuilderFactory
        .get("AnalyzeMessagesJob")
        .incrementer(new RunIdIncrementer())
        .flow(parseAndPersistStep)
        .next(reportStep)
        .end()
        .build();
  }

  @Bean
  public Step parseAndPersistStep(
      final ItemReader<Message> reader,
      final ItemWriter<Message> writer,
      final ContactWriteListener contactWriteListener,
      final JdbcTemplate jdbcTemplate) {
    final ParseAndPersistValidationListener parseAndPersistValidationListener =
        new ParseAndPersistValidationListener(jdbcTemplate);
    return stepBuilderFactory
        .get("ParseAndPersistMessagesStep")
        .<Message, Message>chunk(10)
        .reader(reader)
        .writer(writer)
        .listener(contactWriteListener)
        .listener(parseAndPersistValidationListener)
        .build();
  }

  @Bean
  @StepScope
  public MessageItemReader reader(
      @Value("#{jobParameters['zone_offset']}") final String zoneOffsetString,
      @Value("#{jobParameters['chat_log']}") final String chatLog) {
    final MessageItemReader messageItemReader =
        new MessageItemReader(ZoneOffset.of(zoneOffsetString));
    messageItemReader.setResource(new PathResource(Paths.get(chatLog)));
    return messageItemReader;
  }

  @Bean
  public Step reportStep(final JdbcTemplate jdbcTemplate) {
    final ReportTasklet reportTasklet = new ReportTasklet(jdbcTemplate);
    return stepBuilderFactory.get("ReportStep").tasklet(reportTasklet).build();
  }

  @Bean
  public ItemWriter<Message> writer(final EntityManagerFactory entityManagerFactory) {
    return new JpaItemWriterBuilder<Message>().entityManagerFactory(entityManagerFactory).build();
  }
}
