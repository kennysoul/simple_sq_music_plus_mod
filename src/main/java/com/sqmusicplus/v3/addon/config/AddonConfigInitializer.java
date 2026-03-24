package com.sqmusicplus.v3.addon.config;

import com.sqmusicplus.v3.base.entity.SqConfig;
import com.sqmusicplus.v3.base.enums.DbBooleanConvert;
import com.sqmusicplus.v3.base.service.SqConfigService;
import com.sqmusicplus.v3.config.SqConfigCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Slf4j
@Configuration
@Order(102)
public class AddonConfigInitializer implements ApplicationRunner {

    @Autowired
    private SqConfigService configService;

    @Override
    public void run(ApplicationArguments args) {
        ensureConfig(buildBoolean(
                "【个性化】01 路径非法字符替换开关",
                "addon.path.slash.replace.enable",
                "false",
                "开启后将路径中的 / \\ : * ? \" < > | 替换为自定义字符；关闭时保持原版逻辑"
        ));
        ensureConfig(buildInput(
                "【个性化】02 路径替换字符",
                "addon.path.slash.replace.char",
                "-",
                "例如 + 或 x；仅在“路径非法字符替换开关”开启时生效"
        ));
        ensureConfig(buildBoolean(
                "【个性化】03 专辑按专辑艺术家归档",
                "addon.album.artist.folder.enable",
                "false",
                "开启后同专辑合唱歌曲优先归入专辑艺术家目录；关闭时保持原版逻辑"
        ));
        ensureConfig(buildBoolean(
                "【个性化】04 写入音轨序号到Metadata",
                "addon.track.number.enable",
                "false",
                "仅写入标签 Track 字段，不修改文件名"
        ));

        SqConfigCache.setSqConfigMap(configService.list());
        log.info("addon 个性化设置初始化完成");
    }

    private void ensureConfig(SqConfig target) {
        SqConfig exist = SqConfigCache.getSqConfig(target.getConfigKey());
        if (exist != null) {
            return;
        }
        SqConfigCache.addConfigToDb(target);
    }

    private SqConfig buildBoolean(String name, String key, String value, String remark) {
        return base(name, key, value, "boolean", remark);
    }

    private SqConfig buildInput(String name, String key, String value, String remark) {
        return base(name, key, value, "input", remark);
    }

    private SqConfig base(String name, String key, String value, String type, String remark) {
        SqConfig sqConfig = new SqConfig();
        sqConfig.setConfigName(name);
        sqConfig.setConfigKey(key);
        sqConfig.setConfigValue(value);
        sqConfig.setConfigType(type);
        sqConfig.setConfigShow(DbBooleanConvert.YES.getValue());
        sqConfig.setConfigRemark(remark);
        sqConfig.setConfigNullCheck(DbBooleanConvert.YES.getValue());
        sqConfig.setConfigDisabled(DbBooleanConvert.NO.getValue());
        return sqConfig;
    }
}
