package com.qunite.api.service;

import com.qunite.api.data.EntryRepository;
import com.qunite.api.data.QueueRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.Entry;
import com.qunite.api.domain.EntryId;
import com.qunite.api.domain.Queue;

import java.util.*;

import com.qunite.api.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor(onConstructor_ = {@Lazy})
@Service
public class QueueServiceImpl implements QueueService {
    private final QueueRepository queueRepository;
    private final UserRepository userRepository;
    private final EntryRepository entryRepository;
    private final QueueServiceImpl self;

    @Override
    @Transactional
    public Queue create(Queue queue) {
        return queueRepository.save(queue);
    }

    @Override
    @Transactional
    public void enrollMemberToQueue(Long memberId, Long queueId) {
        userRepository.findById(memberId)
                .flatMap(user ->
                        queueRepository.findById(queueId)
                                .map(queue -> new Entry(user, queue)))
                .ifPresent(entry -> entry.getQueue().addEntry(entry));
    }

    @Override
    @Transactional
    public Optional<Integer> getMembersAmountInQueue(Long queueId) {
        return self.findById(queueId).map(queue -> queue.getEntries().size());
    }

    @Override
    @Transactional
    public Optional<Integer> getMemberPositionInQueue(Long memberId, Long queueId) {
        return entryRepository.findById(new EntryId(memberId, queueId))
                .map(entry -> entry.getEntryIndex() + 1);
    }


    @Transactional
    public Optional<User> getCreator(Long queueId) {
        return self.findById(queueId).map(Queue::getCreator);
    }

    @Transactional
    public Optional<List<User>> getManagers(Long queueId) {
        return self.findById(queueId).map(Queue::getManagers).map(List::copyOf);
    }

    @Override
    @Transactional
    public void deleteById(Long queueId) {
        queueRepository.deleteById(queueId);
    }

    @Override
    public List<Queue> findAll() {
        return queueRepository.findAll();
    }

    @Override
    public Optional<Queue> findById(Long queueId) {
        return queueRepository.findById(queueId);
    }
}