package com.sqmusicplus.v3.addon.enums;

/**
 * addon 功能配置项，默认关闭以保持与原版行为一致。
 */
public enum AddonConfigEnum {
    PATH_SLASH_REPLACE_ENABLE("addon.path.slash.replace.enable", "false"),
    PATH_SLASH_REPLACE_CHAR("addon.path.slash.replace.char", "-"),
    ALBUM_ARTIST_FOLDER_ENABLE("addon.album.artist.folder.enable", "false"),
    TRACK_NUMBER_ENABLE("addon.track.number.enable", "false");

    private final String key;
    private final String defaultValue;

    AddonConfigEnum(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}