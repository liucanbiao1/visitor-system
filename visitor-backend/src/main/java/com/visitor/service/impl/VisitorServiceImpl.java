package com.visitor.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.visitor.entity.Visitor;
import com.visitor.mapper.VisitorMapper;
import com.visitor.service.VisitorService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VisitorServiceImpl implements VisitorService {

    private final VisitorMapper visitorMapper;
    private final MessageSource messageSource;

    public VisitorServiceImpl(VisitorMapper visitorMapper, MessageSource messageSource) {
        this.visitorMapper = visitorMapper;
        this.messageSource = messageSource;
    }

    @Override
    public List<Visitor> findAll() {
        return visitorMapper.findAll();
    }

    @Override
    public Visitor findById(Long id) {
        return visitorMapper.findById(id);
    }

    @Override
    public void add(Visitor visitor) {
        visitor.setStatus(1);
        visitorMapper.insert(visitor);
    }

    @Override
    public void update(Visitor visitor) {
        visitorMapper.update(visitor);
    }

    @Override
    public void delete(Long id) {
        visitorMapper.deleteById(id);
    }

    @Override
    public Visitor findByPhone(String phone) {
        return visitorMapper.findByPhone(phone);
    }

    @Override
    public Visitor addOrUpdate(Visitor visitor) {
        Visitor existing = visitorMapper.findByPhone(visitor.getPhone());
        if (existing != null) {
            boolean changed = false;
            if (visitor.getName() != null && !visitor.getName().equals(existing.getName())) {
                existing.setName(visitor.getName());
                changed = true;
            }
            if (visitor.getIdCard() != null && !visitor.getIdCard().isEmpty()
                    && !visitor.getIdCard().equals(existing.getIdCard())) {
                existing.setIdCard(visitor.getIdCard());
                changed = true;
            }
            if (changed) {
                visitorMapper.update(existing);
            }
            return existing;
        } else {
            visitor.setStatus(1);
            visitorMapper.insert(visitor);
            return visitor;
        }
    }

    @Override
    public void toggleBlacklist(Long id, Integer status) {
        if (status != 0 && status != 1) {
            throw new IllegalArgumentException(
                    messageSource.getMessage("visitor.status.invalid", null, LocaleContextHolder.getLocale()));
        }
        visitorMapper.updateStatus(id, status);
    }

    @Override
    public Map<String, Object> getPage(int page, int pageSize, String keyword) {
        PageHelper.startPage(page, pageSize);
        List<Visitor> list = visitorMapper.findPage(keyword);
        PageInfo<Visitor> pageInfo = new PageInfo<>(list);
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageInfo.getList());
        result.put("total", pageInfo.getTotal());
        result.put("page", pageInfo.getPageNum());
        result.put("pageSize", pageInfo.getPageSize());
        return result;
    }
}
