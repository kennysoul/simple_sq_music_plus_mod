package com.sqmusicplus.v3.addon.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sqmusicplus.v3.base.entity.SqConfig;
import com.sqmusicplus.v3.base.enums.DbBooleanConvert;
import com.sqmusicplus.v3.base.service.SqConfigService;
import com.sqmusicplus.v3.config.AjaxResult;
import com.sqmusicplus.v3.config.SqConfigCache;
import com.sqmusicplus.v3.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/addon/config")
public class AddonPersonConfigController {

    @Autowired
    private SqConfigService configService;

    @SaCheckLogin
    @GetMapping("/getPersonalizationConfigList")
    public AjaxResult getPersonalizationConfigList() {
        List<SqConfig> addonList = SqConfigCache.getAllConfig()
                .stream()
                .filter(item -> StringUtils.isNotBlank(item.getConfigKey()))
                .filter(item -> item.getConfigKey().startsWith("addon."))
                .collect(Collectors.toList());
        return AjaxResult.success(addonList);
    }

    @SaCheckLogin
    @PostMapping("/updatePersonalizationConfig")
    public AjaxResult updatePersonalizationConfig(@RequestBody SqConfig data) {
        if (data == null || StringUtils.isBlank(data.getConfigKey())) {
            return AjaxResult.error("参数异常");
        }
        if (!data.getConfigKey().startsWith("addon.")) {
            return AjaxResult.error("仅允许修改 addon 配置");
        }
        SqConfig sqConfig = SqConfigCache.getSqConfig(data.getConfigKey());
        if (sqConfig == null) {
            return AjaxResult.error("配置项不存在");
        }
        if (sqConfig.getConfigDisabled() == DbBooleanConvert.YES.getValue().intValue()) {
            return AjaxResult.error("该设置已禁用不允许修改");
        }
        if (sqConfig.getConfigNullCheck() == DbBooleanConvert.YES.getValue().intValue() && StringUtils.isBlank(data.getConfigValue())) {
            return AjaxResult.error("该设置不允许为空");
        }

        String value = data.getConfigValue();
        if (StringUtils.isBlank(value)) {
            value = "";
        }
        if ("boolean".equals(sqConfig.getConfigType())) {
            if (!"true".equals(value) && !"false".equals(value)) {
                return AjaxResult.error("布尔值仅允许 true/false");
            }
        }

        LambdaUpdateWrapper<SqConfig> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SqConfig::getConfigKey, data.getConfigKey())
                .set(SqConfig::getConfigValue, value);
        configService.update(wrapper);

        List<SqConfig> list = configService.list();
        SqConfigCache.setSqConfigMap(list);
        return AjaxResult.success();
    }
}
