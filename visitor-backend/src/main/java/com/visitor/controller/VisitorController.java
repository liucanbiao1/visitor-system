package com.visitor.controller;

import com.visitor.annotation.RequirePermission;
import com.visitor.common.Result;
import com.visitor.dto.VisitorDTO;
import com.visitor.entity.Visitor;
import com.visitor.service.VisitorService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/visitor")
public class VisitorController {

    private final VisitorService visitorService;

    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    @GetMapping
    public Result<List<Visitor>> list() {
        return Result.success(visitorService.findAll());
    }

    @GetMapping("/page")
    @RequirePermission("visitor:list")
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(visitorService.getPage(page, pageSize, keyword));
    }

    @GetMapping("/{id}")
    public Result<Visitor> detail(@PathVariable Long id) {
        return Result.success(visitorService.findById(id));
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody VisitorDTO dto) {
        Visitor visitor = new Visitor();
        BeanUtils.copyProperties(dto, visitor);
        visitorService.add(visitor);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody VisitorDTO dto) {
        Visitor visitor = new Visitor();
        BeanUtils.copyProperties(dto, visitor);
        visitor.setId(id);
        visitorService.update(visitor);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        visitorService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @RequirePermission("visitor:edit")
    public Result<Void> toggleBlacklist(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        visitorService.toggleBlacklist(id, body.get("status"));
        return Result.success();
    }
}
