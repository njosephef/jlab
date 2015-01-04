package org.jlab.repository;

import org.jlab.domain.Message;
import org.springframework.data.repository.Repository;

import java.util.List;

/**
 * Created by scorpiovn on 1/3/15.
 */
public interface MessageRepository extends Repository<Message, Long> {
    List<Message> findByUrl(String url);
}
