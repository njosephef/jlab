package org.jlab.service;

import org.jlab.domain.Message;
import org.jlab.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SomeClient {

  @Autowired
  private MessageRepository repository;

  public void doSomething() {
    List<Message> messages = repository.findByUrl("Matthews");
  }
}