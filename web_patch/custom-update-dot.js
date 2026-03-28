(function () {
  var API_NOTICE = "/api/config/version/update-notice";
  var API_VERSION = "/api/config/version";
  var UPDATE_STATE = "unknown";
  var CURRENT_VERSION = "";
  var LATEST_VERSION = "";
  var UPSTREAM_API = "https://api.github.com/repos/59799517/simple_sq_music_plus/releases/latest";
  var UPSTREAM_STATE = "unknown"; // "unknown" | "behind" | "synced"
  var UPSTREAM_VERSION = "";

  function createDot() {
    var dot = document.createElement("span");
    dot.className = "sq-update-dot";
    dot.style.display = "inline-block";
    dot.style.width = "8px";
    dot.style.height = "8px";
    dot.style.marginLeft = "8px";
    dot.style.borderRadius = "50%";
    dot.style.verticalAlign = "middle";
    return dot;
  }

  function getDotStyle() {
    if (UPDATE_STATE === "update") {
      return {
        color: "#ff4d4f",
        ring: "rgba(255,77,79,0.25)",
        title: "检测到新版本: " + LATEST_VERSION + " (当前 " + CURRENT_VERSION + ")"
      };
    }
    if (UPSTREAM_STATE === "behind") {
      return {
        color: "#fa8c16",
        ring: "rgba(250,140,22,0.25)",
        title: "原版已更新至 " + UPSTREAM_VERSION + "，当前 mod 尚未同步，建议升级"
      };
    }
    if (UPDATE_STATE === "ok" || UPSTREAM_STATE === "synced") {
      return {
        color: "#52c41a",
        ring: "rgba(82,196,26,0.25)",
        title: UPSTREAM_STATE === "synced"
          ? "已同步至原版最新: " + UPSTREAM_VERSION + " (当前 " + CURRENT_VERSION + ")"
          : "当前已是最新版本: " + CURRENT_VERSION
      };
    }
    return {
      color: "#8c8c8c",
      ring: "rgba(140,140,140,0.25)",
      title: "正在检查版本更新"
    };
  }

  function applyDotState(dot) {
    var style = getDotStyle();
    dot.style.background = style.color;
    dot.style.boxShadow = "0 0 0 2px " + style.ring;
    dot.title = style.title;
  }

  function upsertDot(node) {
    if (!node) {
      return;
    }
    var dot = null;
    for (var i = 0; i < node.childNodes.length; i++) {
      var child = node.childNodes[i];
      if (child && child.classList && child.classList.contains("sq-update-dot")) {
        dot = child;
        break;
      }
    }
    if (!dot) {
      dot = createDot();
      node.appendChild(dot);
    }
    applyDotState(dot);
  }

  function findVersionTargets() {
    var all = document.querySelectorAll("div,span,p,strong");
    var targets = [];
    for (var i = 0; i < all.length; i++) {
      var el = all[i];
      if (!el || !el.textContent) {
        continue;
      }
      var text = (el.textContent || "").replace(/\s+/g, "");
      if (text.indexOf("后端版本") !== -1) {
        targets.push(el);
      }
    }
    return targets;
  }

  function normalizeVersion(version) {
    if (version == null) {
      return "";
    }
    var v = String(version).trim();
    if (!v) {
      return "";
    }
    if (v.charAt(0).toLowerCase() === "v") {
      return v;
    }
    return "v" + v;
  }

  function ensureBackendVersionText(target) {
    if (!target) {
      return;
    }
    var text = (target.textContent || "").trim();
    if (text.indexOf("后端版本") === -1) {
      return;
    }

    var displayVersion = normalizeVersion(CURRENT_VERSION);
    if (!displayVersion) {
      return;
    }

    var normalized = text.replace(/\s+/g, "");
    var hasVersion = /后端版本[：:](v|V)?\d/.test(normalized);
    if (hasVersion) {
      return;
    }

    for (var i = target.childNodes.length - 1; i >= 0; i--) {
      var node = target.childNodes[i];
      if (node && node.classList && node.classList.contains("sq-update-dot")) {
        continue;
      }
      target.removeChild(node);
    }

    target.appendChild(document.createTextNode("后端版本: " + displayVersion));
  }

  function pickBestVersionTarget() {
    var targets = findVersionTargets();
    if (!targets.length) {
      return null;
    }
    var best = targets[0];
    for (var i = 1; i < targets.length; i++) {
      var a = (best.innerText || best.textContent || "").trim().length;
      var b = (targets[i].innerText || targets[i].textContent || "").trim().length;
      if (b > 0 && b < a) {
        best = targets[i];
      }
    }
    return best;
  }

  function clearAllDotsExcept(target) {
    var dots = document.querySelectorAll(".sq-update-dot");
    for (var i = 0; i < dots.length; i++) {
      var dot = dots[i];
      if (!target || dot.parentNode !== target) {
        dot.remove();
      }
    }
  }

  function renderDot() {
    var target = pickBestVersionTarget();
    ensureBackendVersionText(target);
    clearAllDotsExcept(target);
    upsertDot(target);
  }

  function startObserver() {
    var observer = new MutationObserver(function () {
      renderDot();
    });
    observer.observe(document.body, { childList: true, subtree: true });
  }

  function checkUpdate() {
    fetch(API_NOTICE, { method: "GET", cache: "no-store" })
      .then(function (resp) { return resp.json(); })
      .then(function (data) {
        if (!data || data.code !== 200 || !data.data) {
          UPDATE_STATE = "unknown";
          return;
        }
        CURRENT_VERSION = String(data.data.currentVersion || "");
        LATEST_VERSION = String(data.data.latestVersion || "");
        UPDATE_STATE = data.data.hasUpdate ? "update" : "ok";
        renderDot();
      })
      .catch(function () {
        UPDATE_STATE = "unknown";
        renderDot();
      });
  }

  function checkVersion() {
    fetch(API_VERSION, { method: "GET", cache: "no-store" })
      .then(function (resp) { return resp.json(); })
      .then(function (data) {
        if (!data || data.code !== 200) {
          return;
        }
        var apiVersion = normalizeVersion(data.data || "");
        if (apiVersion) {
          CURRENT_VERSION = apiVersion;
          renderDot();
          checkUpstreamUpdate();
        }
      })
      .catch(function () {
        // 静默失败，不影响主功能
      });
  }

  function compareVersionNums(a, b) {
    var clean = function (v) { return String(v).replace(/^v/i, "").replace(/-.*$/, ""); };
    var partsA = clean(a).split(".").map(Number);
    var partsB = clean(b).split(".").map(Number);
    for (var i = 0; i < Math.max(partsA.length, partsB.length); i++) {
      var na = partsA[i] || 0;
      var nb = partsB[i] || 0;
      if (na !== nb) { return na - nb; }
    }
    return 0;
  }

  function checkUpstreamUpdate() {
    if (!CURRENT_VERSION) { return; }
    fetch(UPSTREAM_API, { method: "GET", cache: "no-store" })
      .then(function (resp) { return resp.json(); })
      .then(function (data) {
        if (!data || !data.tag_name) { return; }
        var tag = String(data.tag_name).trim();
        UPSTREAM_VERSION = tag.charAt(0).toLowerCase() === "v" ? tag : "v" + tag;
        if (compareVersionNums(tag, CURRENT_VERSION) > 0) {
          UPSTREAM_STATE = "behind";
        } else {
          UPSTREAM_STATE = "synced";
        }
        renderDot();
      })
      .catch(function () {
        UPSTREAM_STATE = "unknown";
      });
  }

  function boot() {
    startObserver();
    checkUpdate();
    checkVersion();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", boot);
  } else {
    boot();
  }
})();
