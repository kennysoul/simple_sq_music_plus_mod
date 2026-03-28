package com.sqmusicplus.v3.addon.hook;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class VersionUpdateHook {

    private static final long CACHE_TTL_MS = 6 * 60 * 60 * 1000L;
    private static final String LATEST_RELEASE_API = "https://api.github.com/repos/kennysoul/simple_sq_music_plus_mod/releases/latest";

    private volatile long lastCheckAt = 0L;
    private volatile Map<String, Object> lastResult = null;

    public synchronized Map<String, Object> check(String currentVersion) {
        long now = System.currentTimeMillis();
        if (lastResult != null && (now - lastCheckAt) < CACHE_TTL_MS) {
            return attachCurrentVersion(lastResult, currentVersion);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("checkedAt", now);
        result.put("checkMode", "non-intrusive");

        try {
            String body = cn.hutool.http.HttpRequest.get(LATEST_RELEASE_API)
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "sqmusic-mod-update-check")
                    .timeout(5000)
                    .execute()
                    .body();

            JSONObject json = JSONObject.parseObject(body);
            String latestVersion = trimVersion(json.getString("tag_name"));
            if (latestVersion == null || latestVersion.isEmpty()) {
                latestVersion = trimVersion(json.getString("name"));
            }

            String releaseUrl = json.getString("html_url");
            String publishedAt = json.getString("published_at");
            String normalizedCurrent = trimVersion(currentVersion);
            boolean hasUpdate = compareVersion(normalizedCurrent, latestVersion) < 0;

            result.put("hasUpdate", hasUpdate);
            result.put("currentVersion", normalizedCurrent);
            result.put("latestVersion", latestVersion);
            result.put("releaseUrl", releaseUrl);
            result.put("publishedAt", publishedAt);
            result.put("source", "github");
        } catch (Exception ex) {
            log.warn("版本更新检查失败: {}", ex.getMessage());
            result.put("hasUpdate", false);
            result.put("currentVersion", trimVersion(currentVersion));
            result.put("latestVersion", "");
            result.put("releaseUrl", "");
            result.put("publishedAt", "");
            result.put("source", "github");
            result.put("checkError", "update-check-failed");
        }

        lastCheckAt = now;
        lastResult = new HashMap<>(result);
        return result;
    }

    private Map<String, Object> attachCurrentVersion(Map<String, Object> cached, String currentVersion) {
        Map<String, Object> result = new HashMap<>(cached);
        String normalizedCurrent = trimVersion(currentVersion);
        result.put("currentVersion", normalizedCurrent);

        Object latestObj = result.get("latestVersion");
        String latestVersion = latestObj == null ? "" : String.valueOf(latestObj);
        result.put("hasUpdate", compareVersion(normalizedCurrent, latestVersion) < 0);
        return result;
    }

    private String trimVersion(String value) {
        if (value == null) {
            return "";
        }
        String v = value.trim();
        if (v.startsWith("v") || v.startsWith("V")) {
            v = v.substring(1);
        }
        return v;
    }

    private int compareVersion(String current, String latest) {
        int[] a = parseVersion(current);
        int[] b = parseVersion(latest);
        int len = Math.max(a.length, b.length);
        for (int i = 0; i < len; i++) {
            int av = i < a.length ? a[i] : 0;
            int bv = i < b.length ? b[i] : 0;
            if (av != bv) {
                return Integer.compare(av, bv);
            }
        }
        return 0;
    }

    private int[] parseVersion(String version) {
        if (version == null || version.isEmpty()) {
            return new int[]{0};
        }
        String[] parts = version.split("\\.");
        int[] values = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].replaceAll("[^0-9]", "");
            if (part.isEmpty()) {
                values[i] = 0;
            } else {
                try {
                    values[i] = Integer.parseInt(part);
                } catch (NumberFormatException ex) {
                    values[i] = 0;
                }
            }
        }
        return values;
    }
}