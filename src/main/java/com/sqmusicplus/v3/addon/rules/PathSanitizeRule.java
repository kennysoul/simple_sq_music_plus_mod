package com.sqmusicplus.v3.addon.rules;

import com.sqmusicplus.v3.addon.config.AddonConfigCache;
import com.sqmusicplus.v3.addon.enums.AddonConfigEnum;
import com.sqmusicplus.v3.utils.StringUtils;

public class PathSanitizeRule {

    private PathSanitizeRule() {
    }

    public static String apply(String input) {
        if (StringUtils.isBlank(input)) {
            return input;
        }
        if (!AddonConfigCache.getBoolean(AddonConfigEnum.PATH_SLASH_REPLACE_ENABLE)) {
            return input;
        }

        String replaceChar = AddonConfigCache.getValue(AddonConfigEnum.PATH_SLASH_REPLACE_CHAR);
        if (StringUtils.isBlank(replaceChar)) {
            replaceChar = "-";
        }

        String safe = input
                .replace("/", replaceChar)
                .replace("\\", replaceChar)
                .replace(":", replaceChar)
                .replace("*", replaceChar)
                .replace("?", replaceChar)
                .replace("\"", replaceChar)
                .replace("<", replaceChar)
                .replace(">", replaceChar)
                .replace("|", replaceChar);

        return safe.trim();
    }
}
