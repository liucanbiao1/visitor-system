package com.visitor.service;

import com.visitor.entity.Visitor;

import java.util.List;
import java.util.Map;

public interface VisitorService {
    List<Visitor> findAll();
    Visitor findById(Long id);
    void add(Visitor visitor);
    void update(Visitor visitor);
    void delete(Long id);

    Visitor findByPhone(String phone);
    Visitor addOrUpdate(Visitor visitor);
    void toggleBlacklist(Long id, Integer status);
    Map<String, Object> getPage(int page, int pageSize, String keyword);
}
