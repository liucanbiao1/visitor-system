package com.visitor.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.visitor.ai.DeepSeekClient;
import com.visitor.annotation.RequirePermission;
import com.visitor.common.Result;
import com.visitor.config.AiProperties;
import com.visitor.dto.AiSettingsDTO;
import com.visitor.dto.AiSettingsVO;
import com.visitor.entity.AiReviewLog;
import com.visitor.mapper.AiReviewLogMapper;
import com.visitor.service.SysConfigService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final String CONFIG_KEY_ENABLED = "ai.review.enabled";

    private final SysConfigService sysConfigService;
    private final DeepSeekClient deepSeekClient;
    private final AiProperties props;
    private final AiReviewLogMapper aiReviewLogMapper;

    public AiController(SysConfigService sysConfigService, DeepSeekClient deepSeekClient,
                        AiProperties props, AiReviewLogMapper aiReviewLogMapper) {
        this.sysConfigService = sysConfigService;
        this.deepSeekClient = deepSeekClient;
        this.props = props;
        this.aiReviewLogMapper = aiReviewLogMapper;
    }

    @GetMapping("/settings")
    @RequirePermission("ai:settings")
    public Result<AiSettingsVO> getSettings() {
        AiSettingsVO vo = new AiSettingsVO();
        vo.setEnabled(sysConfigService.getBoolean(CONFIG_KEY_ENABLED, props.isEnabled()));
        vo.setModel(props.getDeepseek().getModel());
        vo.setBaseUrl(props.getDeepseek().getBaseUrl());
        vo.setApiKeyConfigured(deepSeekClient.isApiKeyConfigured());
        vo.setApiKeyMasked(deepSeekClient.maskApiKey());
        return Result.success(vo);
    }

    @PutMapping("/settings")
    @RequirePermission("ai:settings")
    public Result<Void> updateSettings(@Validated @RequestBody AiSettingsDTO dto,
                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        sysConfigService.setConfig(CONFIG_KEY_ENABLED, String.valueOf(dto.getEnabled()), userId);
        return Result.success();
    }

    @GetMapping("/logs")
    @RequirePermission("ai:logs")
    public Result<Map<String, Object>> logs(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int pageSize) {
        PageHelper.startPage(page, pageSize);
        List<AiReviewLog> list = aiReviewLogMapper.findPage();
        PageInfo<AiReviewLog> pageInfo = new PageInfo<>(list);
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageInfo.getList());
        result.put("total", pageInfo.getTotal());
        result.put("page", pageInfo.getPageNum());
        result.put("pageSize", pageInfo.getPageSize());
        return Result.success(result);
    }
}
