package com.sqmusicplus.v3.addon.rules;

import com.alibaba.fastjson2.JSONObject;
import com.sqmusicplus.v3.addon.config.AddonConfigCache;
import com.sqmusicplus.v3.addon.enums.AddonConfigEnum;
import com.sqmusicplus.v3.base.entity.DownloadInfo;
import com.sqmusicplus.v3.plug.entity.Music;

import java.util.Arrays;
import java.util.List;

public class TrackNumberRule {

    private static final List<String> CANDIDATE_KEYS = Arrays.asList(
            "trackNumber",
            "trackNo",
            "track",
            "no",
            "index_album",
            "songNo",
            "songNoInAlbum",
            "index",
            "order"
    );

    private TrackNumberRule() {
    }

    public static Integer resolve(Music music, DownloadInfo downloadInfo) {
        if (!AddonConfigCache.getBoolean(AddonConfigEnum.TRACK_NUMBER_ENABLE)) {
            return null;
        }

        if (music != null) {
            if (music.getTrackNumber() != null && music.getTrackNumber() > 0) {
                return music.getTrackNumber();
            }
            Integer fromDataInfo = parseFromJson(music.getDataInfo());
            if (fromDataInfo != null) {
                return fromDataInfo;
            }
        }

        if (downloadInfo != null) {
            try {
                JSONObject info = JSONObject.parseObject(downloadInfo.getDownloadMusicInfo());
                Integer fromDownloadInfo = parseFromJson(info);
                if (fromDownloadInfo != null) {
                    return fromDownloadInfo;
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private static Integer parseFromJson(JSONObject data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        for (String key : CANDIDATE_KEYS) {
            Integer value = parseInteger(data.get(key));
            if (value != null && value > 0) {
                return value;
            }
        }
        return null;
    }

    private static Integer parseInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception ignored) {
            return null;
        }
    }
}
