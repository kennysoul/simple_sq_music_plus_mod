package com.sqmusicplus.v3.addon.config;

import com.sqmusicplus.v3.addon.enums.AddonConfigEnum;
import com.sqmusicplus.v3.base.entity.SqConfig;
import com.sqmusicplus.v3.config.SqConfigCache;
import com.sqmusicplus.v3.utils.StringUtils;

public class AddonConfigCache {

    private AddonConfigCache() {
    }

    public static String getValue(AddonConfigEnum configEnum) {
        SqConfig sqConfig = SqConfigCache.getSqConfig(configEnum.getKey());
        if (sqConfig == null || StringUtils.isBlank(sqConfig.getConfigValue())) {
            return configEnum.getDefaultValue();
        }
        return sqConfig.getConfigValue();
    }

    public static boolean getBoolean(AddonConfigEnum configEnum) {
        return Boolean.parseBoolean(getValue(configEnum));
    }
}
