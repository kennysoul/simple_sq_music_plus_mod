package com.sqmusicplus.v3.addon.rules;

import com.sqmusicplus.v3.addon.config.AddonConfigCache;
import com.sqmusicplus.v3.addon.enums.AddonConfigEnum;
import com.sqmusicplus.v3.plug.base.hander.SearchHander;
import com.sqmusicplus.v3.plug.entity.Album;
import com.sqmusicplus.v3.plug.entity.Music;
import com.sqmusicplus.v3.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AlbumArtistRule {

    private AlbumArtistRule() {
    }

    public static String apply(String fallbackMainArtist, Music music, SearchHander searchHander) {
        if (!AddonConfigCache.getBoolean(AddonConfigEnum.ALBUM_ARTIST_FOLDER_ENABLE)) {
            return fallbackMainArtist;
        }
        try {
            if (music == null || StringUtils.isBlank(music.getAlbumId()) || searchHander == null) {
                return fallbackMainArtist;
            }
            Album album = searchHander.queryAlbumById(music.getAlbumId());
            if (album == null || StringUtils.isBlank(album.getAlbumArtist())) {
                return fallbackMainArtist;
            }
            return album.getAlbumArtist().trim();
        } catch (Exception e) {
            log.warn("addon 专辑艺术家规则执行失败，回退原逻辑: {}", e.getMessage());
            return fallbackMainArtist;
        }
    }
}
