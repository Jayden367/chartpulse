
(() => {
  const panel = document.createElement("section");
  panel.id = "chartpulse-ai-panel";
  panel.style.cssText = `
    margin:20px 0;
    padding:18px;
    border:1px solid #444;
    border-radius:14px;
    background:#151923;
    color:white;
  `;

  panel.innerHTML = `
    <h2>AI Chart Analysis</h2>
    <p>Upload a chart screenshot for AI analysis.</p>
    <label for="cp-symbol">Market</label>
    <select id="cp-symbol">
      <option>XAUUSD</option>
      <option>EURUSD</option>
      <option>GBPUSD</option>
      <option>USDJPY</option>
      <option>BTCUSD</option>
    </select>
    <br><br>
    <label for="cp-timeframe">Timeframe</label>
    <select id="cp-timeframe">
      <option>M5</option>
      <option selected>M15</option>
      <option>M30</option>
      <option>H1</option>
      <option>H4</option>
      <option>D1</option>
    </select>
    <br><br>
    <input id="cp-image" type="file" accept="image/png,image/jpeg,image/webp">
    <br><br>
    <img id="cp-preview" alt="Chart preview" style="display:none;max-width:100%;border-radius:10px">
    <br><br>
    <button id="cp-analyze" type="button">Analyze chart with AI</button>
    <p id="cp-status" role="status"></p>
    <pre id="cp-result" style="white-space:pre-wrap;overflow-wrap:anywhere"></pre>
    <small>AI interpretation of your screenshot only. Not financial advice or live market data.</small>
  `;

  const mount = () => {
    if (document.getElementById(panel.id)) return;
    const target =
      document.querySelector("main") ||
      document.querySelector(".app") ||
      document.body;
    target.appendChild(panel);
  };

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", mount);
  } else {
    mount();
  }

  const imageInput = panel.querySelector("#cp-image");
  const preview = panel.querySelector("#cp-preview");
  const status = panel.querySelector("#cp-status");
  const result = panel.querySelector("#cp-result");
  const button = panel.querySelector("#cp-analyze");

  let imageDataUrl = "";

  imageInput.addEventListener("change", () => {
    const file = imageInput.files[0];
    imageDataUrl = "";
    preview.style.display = "none";
    result.textContent = "";

    if (!file) return;

    if (!["image/jpeg", "image/png", "image/webp"].includes(file.type)) {
      status.textContent = "Please choose a JPG, PNG, or WebP image.";
      imageInput.value = "";
      return;
    }

    if (file.size > 6 * 1024 * 1024) {
      status.textContent = "Image is too large. Please choose one under 6 MB.";
      imageInput.value = "";
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      imageDataUrl = reader.result;
      preview.src = imageDataUrl;
      preview.style.display = "block";
      status.textContent = "Chart ready for analysis.";
    };
    reader.onerror = () => {
      status.textContent = "Could not read that image. Try another one.";
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
    result.textContent = "";

    try {
      const response = await fetch("/.netlify/functions/analyze-chart", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          imageDataUrl,
          symbol: panel.querySelector("#cp-symbol").value,
          timeframe: panel.querySelector("#cp-timeframe").value
        })
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.error || "Analysis failed.");
      }

      const signal = data.signal;

if (signal) {
  result.textContent = `
TRADE SIGNAL: ${signal.decision}

ENTRY: ${signal.entry ?? "N/A"}
STOP LOSS: ${signal.stopLoss ?? "N/A"}

TAKE PROFIT 1: ${signal.takeProfits?.[0] ?? "N/A"}
TAKE PROFIT 2: ${signal.takeProfits?.[1] ?? "N/A"}
TAKE PROFIT 3: ${signal.takeProfits?.[2] ?? "N/A"}

CONFIDENCE: ${signal.confidence}

MARKET STRUCTURE:
${signal.marketStructure}

SUPPORT & RESISTANCE:
${signal.supportResistance}

REASONING:
${signal.reasoning}

INVALIDATION:
${signal.invalidation}

DATA QUALITY:
${signal.dataQuality}

${data.disclaimer || ""}
`;
} else {
  result.textContent = data.analysis || "No analysis received.";
}
      status.textContent = "Analysis complete.";
    } catch (error) {
      status.textContent = error.message || "Could not analyze the chart.";
    } finally {
      button.disabled = false;
    }
  });
})();
