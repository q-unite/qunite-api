package com.qunite.api.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Queue {
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private User creator;
    private LinkedList<User> members;
    private List<User> managers;

    public void addMember(User member) {
        members.add(member);
    }

    public void removeMember() {
        members.remove();
    }

    public List<User> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public void addManager(User manager) {
        managers.add(manager);
    }

    public void removeManager(User manager) {
        managers.remove(manager);
    }

    public List<User> getManagers() {
        return Collections.unmodifiableList(managers);
    }
}
