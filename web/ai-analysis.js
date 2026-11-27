
(() => {
  const panel = document.createElement("section");
  panel.id = "chartpulse-ai-panel";

  panel.innerHTML = `
    <style>
      #chartpulse-ai-panel {
        margin:20px 0;padding:20px;border:1px solid #303b50;
        border-radius:18px;background:#111827;color:#f8fafc;
        font-family:Arial,sans-serif;
      }
      #chartpulse-ai-panel h2 {margin-top:0}
      #chartpulse-ai-panel label {display:block;margin:12px 0 6px}
      #chartpulse-ai-panel select,
      #chartpulse-ai-panel button {
        padding:12px;border-radius:9px;border:1px solid #475569;
        background:#1e293b;color:white;width:100%;
      }
      #chartpulse-ai-panel button {cursor:pointer;margin-top:14px}
      #chartpulse-ai-panel button:disabled {opacity:.5}
      .cp-card {background:#1e293b;border:1px solid #334155;
        border-radius:12px;padding:14px;margin-top:12px}
      .cp-grid {display:grid;grid-template-columns:1fr 1fr;gap:10px}
      .cp-value {font-size:20px;font-weight:bold;margin-top:7px}
      .cp-muted {color:#94a3b8;font-size:12px}
      .cp-signal {font-size:25px;font-weight:bold}
      .cp-buy {color:#4ade80}
      .cp-sell {color:#fb7185}
      .cp-wait {color:#fbbf24}
      .cp-history-item {padding:10px;border-bottom:1px solid #334155}
      #cp-result {margin-top:15px}
      #cp-preview {max-width:100%;border-radius:10px;margin-top:12px}
    </style>

    <h2>ChartPulse AI Dashboard</h2>
    <p>Upload a trading chart for AI analysis.</p>

    <label>Market</label>
    <select id="cp-symbol">
      <option>XAUUSD</option><option>EURUSD</option>
      <option>GBPUSD</option><option>USDJPY</option>
      <option>BTCUSD</option>
    </select>

    <label>Timeframe</label>
    <select id="cp-timeframe">
      <option>M5</option><option selected>M15</option>
      <option>M30</option><option>H1</option>
      <option>H4</option><option>D1</option>
    </select>

    <label>Chart screenshot</label>
    <input id="cp-image" type="file"
      accept="image/png,image/jpeg,image/webp">

    <img id="cp-preview" alt="Chart preview" hidden>

    <button id="cp-analyze" type="button">
      Analyze chart with AI
    </button>

    <p id="cp-status" role="status">Ready for analysis.</p>
    <div id="cp-result"></div>

    <div class="cp-card">
      <h3>Analysis History</h3>
      <div id="cp-history"></div>
      <button id="cp-clear" type="button">Clear history</button>
    </div>

    <p class="cp-muted">
      Screenshot-based AI interpretation only.
      Not financial advice or live market data.
    </p>
  `;

  const mount = () => {
    if (document.getElementById(panel.id)) return;
    const target = document.querySelector("main") ||
      document.querySelector(".app") || document.body;
    target.appendChild(panel);
  };

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", mount);
  } else {
    mount();
  }

  const $ = id => panel.querySelector("#" + id);
  const status = $("cp-status");
  const result = $("cp-result");
  const button = $("cp-analyze");
  const preview = $("cp-preview");
  const HISTORY_KEY = "chartpulse_ai_history_v1";

  let imageDataUrl = "";
let analysisTimer = null;
let analysisSeconds = 0;
  function getHistory() {
    try {
      return JSON.parse(localStorage.getItem(HISTORY_KEY) || "[]");
    } catch {
      return [];
    }
  }

  function saveHistory(item) {
    try {
      const history = getHistory();
      history.unshift(item);
      localStorage.setItem(
        HISTORY_KEY,
        JSON.stringify(history.slice(0, 20))
      );
      renderHistory();
    } catch {
      status.textContent += " History could not be saved.";
    }
  }

  function renderHistory() {
    const history = getHistory();
    const area = $("cp-history");
    area.replaceChildren();

    if (!history.length) {
      area.textContent = "No saved analyses yet.";
      return;
    }

    history.forEach(item => {
      const entry = document.createElement("div");
      entry.className = "cp-history-item";

      const heading = document.createElement("strong");
      heading.textContent =
        `${item.symbol} · ${item.timeframe} · ${item.decision}`;

      const date = document.createElement("div");
      date.className = "cp-muted";
      date.textContent = item.date;

      entry.append(heading, date);
      area.appendChild(entry);
    });
  }

  function addCard(title, value) {
    const card = document.createElement("div");
    card.className = "cp-card";

    const heading = document.createElement("div");
    heading.className = "cp-muted";
    heading.textContent = title;

    const content = document.createElement("div");
    content.className = "cp-value";
    content.textContent = value ?? "N/A";

    card.append(heading, content);
    return card;
  }

  function showAnalysis(data, symbol, timeframe) {
    result.replaceChildren();

    const signal = data.signal;
    if (!signal) {
      const message = document.createElement("p");
      message.textContent = data.analysis || "No analysis received.";
      result.appendChild(message);
      return;
    }

    const decision = ["BUY", "SELL", "WAIT"].includes(signal.decision)
      ? signal.decision : "WAIT";

    const banner = document.createElement("div");
    banner.className = "cp-card";

    const title = document.createElement("div");
    title.className = "cp-muted";
    title.textContent = `${symbol} · ${timeframe} · TRADE SIGNAL`;

    const value = document.createElement("div");
    value.className = "cp-signal " +
      (decision === "BUY" ? "cp-buy" :
       decision === "SELL" ? "cp-sell" : "cp-wait");
    value.textContent = decision;

    banner.append(title, value);
    result.appendChild(banner);

    const prices = document.createElement("div");
    prices.className = "cp-grid";

    prices.append(
      addCard("ENTRY", signal.entry),
      addCard("STOP LOSS", signal.stopLoss),
      addCard("TAKE PROFIT 1", signal.takeProfits?.[0]),
      addCard("TAKE PROFIT 2", signal.takeProfits?.[1]),
      addCard("TAKE PROFIT 3", signal.takeProfits?.[2]),
      addCard("CONFIDENCE", signal.confidence)
    );

    result.appendChild(prices);
    const confidenceText = String(signal.confidence || "LOW").toUpperCase();
    const confidenceScore =
      confidenceText.includes("HIGH") ? 90 :
      confidenceText.includes("MEDIUM") ? 60 : 30;

    const meter = document.createElement("div");
    meter.className = "cp-card";

    const meterTitle = document.createElement("strong");
    meterTitle.textContent = "AI CONFIDENCE METER";

    const meterTrack = document.createElement("div");
    meterTrack.style.cssText =
      "height:12px;background:#334155;border-radius:20px;margin-top:12px;overflow:hidden;";

    const meterFill = document.createElement("div");
    meterFill.style.cssText =
      `height:100%;width:${confidenceScore}%;background:#fbbf24;border-radius:20px;`;

    const meterLabel = document.createElement("p");
    meterLabel.textContent = `${confidenceText} · ${confidenceScore}%`;

    meterTrack.appendChild(meterFill);
    meter.append(meterTitle, meterTrack, meterLabel);
    result.appendChild(meter);
    [
      ["MARKET STRUCTURE", signal.marketStructure],
      ["SUPPORT & RESISTANCE", signal.supportResistance],
      ["REASONING", signal.reasoning],
      ["INVALIDATION", signal.invalidation],
      ["DATA QUALITY", signal.dataQuality]
    ].forEach(([heading, content]) => {
      const card = document.createElement("div");
      card.className = "cp-card";

      const h = document.createElement("strong");
      h.textContent = heading;

      const p = document.createElement("p");
      p.textContent = content || "Not provided.";

      card.append(h, p);
      result.appendChild(card);
    });

    const disclaimer = document.createElement("p");
    disclaimer.className = "cp-muted";
    disclaimer.textContent = data.disclaimer ||
      "Screenshot-based AI interpretation. Not financial advice.";
    result.appendChild(disclaimer);

    saveHistory({
      symbol,
      timeframe,
      decision,
      date: new Date().toLocaleString()
    });
  }

  $("cp-image").addEventListener("change", () => {
    const file = $("cp-image").files[0];
    imageDataUrl = "";
    preview.hidden = true;
    result.replaceChildren();

    if (!file) return;

    if (!["image/jpeg", "image/png", "image/webp"].includes(file.type)) {
      status.textContent = "Choose a JPG, PNG, or WebP image.";
      $("cp-image").value = "";
      return;
    }

    if (file.size > 6 * 1024 * 1024) {
      status.textContent = "Image must be under 6 MB.";
      $("cp-image").value = "";
      return;
    }

    const reader = new FileReader();

    reader.onload = () => {
      imageDataUrl = reader.result;
      preview.src = imageDataUrl;
      preview.hidden = false;
      status.textContent = "Chart ready for analysis.";
    };

    reader.onerror = () => {
      status.textContent = "Could not read the image.";
    };

    reader.readAsDataURL(file);
  });

  button.addEventListener("click", async () => {
    if (!imageDataUrl) {
      status.textContent = "Please upload a chart screenshot first.";
      return;
    }

    button.disabled = true;
    status.textContent = "Analyzing your chart. Please wait...";
    result.replaceChildren();
analysisSeconds = 0;

clearInterval(analysisTimer);

analysisTimer = setInterval(() => {
  analysisSeconds++;

  status.textContent = analysisSeconds >= 15
  ? `Still waiting... The AI service may be busy. Please keep this page open. (${analysisSeconds}s)`
  : `Analyzing your chart... ${analysisSeconds}s elapsed. Please wait.`;
}, 1000);
    const symbol = $("cp-symbol").value;
    const timeframe = $("cp-timeframe").value;

    try {
      const response = await fetch("/api/analyze-chart", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
          imageDataUrl,
          symbol,
          timeframe
        })
      });

      const responseText = await response.text();
let data;

try {
  data = JSON.parse(responseText);
} catch {
  if (response.status === 524) {
    throw new Error(
      "Analysis timed out. Please try a smaller chart screenshot."
    );
  }

  throw new Error(
    `Server error (HTTP ${response.status}). Please try again.`
  );
}

      if (!response.ok) {
        throw new Error(data.error || "Analysis failed.");
      }

      showAnalysis(data, symbol, timeframe);
      status.textContent = "Analysis complete.";
   } catch (error) {
  status.textContent = error.message ||
    "Analysis failed. Please try again in a moment.";

  result.replaceChildren();

  const errorCard = document.createElement("div");
  errorCard.className = "cp-card";
  errorCard.textContent =
    "If the AI service is busy, wait a little and try again with your chart screenshot.";

  result.appendChild(errorCard);
}
    } finally {
      button.disabled = false;
    }
  });

  $("cp-clear").addEventListener("click", () => {
    if (!confirm("Clear all saved AI analysis history?")) return;
    localStorage.removeItem(HISTORY_KEY);
    renderHistory();
  });

  renderHistory();
})();
