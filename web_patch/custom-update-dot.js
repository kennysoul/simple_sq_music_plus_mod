(function () {
  var API = "/api/config/version/update-notice";
  var UPDATE_STATE = "unknown";
  var CURRENT_VERSION = "";
  var LATEST_VERSION = "";

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
    if (UPDATE_STATE === "ok") {
      return {
        color: "#52c41a",
        ring: "rgba(82,196,26,0.25)",
        title: "当前已是最新版本: " + CURRENT_VERSION
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
      if (el.textContent.indexOf("后端版本:") !== -1) {
        targets.push(el);
      }
    }
    return targets;
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
    fetch(API, { method: "GET", cache: "no-store" })
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

  function boot() {
    startObserver();
    checkUpdate();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", boot);
  } else {
    boot();
  }
})();
