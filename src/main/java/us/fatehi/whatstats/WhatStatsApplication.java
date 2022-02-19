package us.fatehi.whatstats;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.launch.support.CommandLineJobRunner;

public class WhatStatsApplication {

  private static final Logger log =
      LoggerFactory.getLogger(ParseAndPersistValidationListener.class);

  public static void main(final String[] args) throws Exception {

    if (args == null | args.length == 0) {
      throw new IllegalArgumentException("Chat log not found");
    }
    Path chatLog = Paths.get(args[0]);
    if (!Files.exists(chatLog) || !Files.isReadable(chatLog)) {
      throw new IllegalArgumentException("Chat log not found - " + args[0]);
    }
    chatLog = chatLog.normalize().toAbsolutePath();
    log.info("Reading " + chatLog);

    CommandLineJobRunner.main(
        new String[] {
          BatchConfiguration.class.getName(),
          "AnalyzeMessagesJob",
          "zone_offset=-05:00",
          "chat_log=" + chatLog.toAbsolutePath().toString()
        });
  }
}
