
export async function onRequestPost({ request, env }) {
  const reply = (data, status = 200) =>
    new Response(JSON.stringify(data), {
      status,
      headers: { "Content-Type": "application/json" }
    });

  try {
    if (!env.GEMINI_API_KEY) {
      return reply({ error: "Gemini API key is missing." }, 500);
    }

    const body = await request.json();
    const { imageDataUrl, symbol, timeframe } = body;

    if (
      typeof imageDataUrl !== "string" ||
      !imageDataUrl.startsWith("data:image/") ||
      !imageDataUrl.includes(",")
    ) {
      return reply({ error: "Please upload a valid chart image." }, 400);
    }

    const [header, imageBase64] = imageDataUrl.split(",");

    if (!imageBase64 || imageBase64.length > 12000000) {
      return reply({ error: "Image is missing or too large." }, 400);
    }

    const mimeType = header.match(/data:(image\/[a-zA-Z0-9.+-]+);base64/);

    if (!mimeType) {
      return reply({ error: "Unsupported image format." }, 400);
    }

    const prompt = `
You are ChartPulse, a careful technical chart analyst.

Analyze this trading chart screenshot.
Symbol: ${symbol || "Unknown"}
Timeframe: ${timeframe || "Unknown"}

Return ONLY valid JSON with these fields:
{
  "analysis": "Detailed chart analysis",
  "signal": {
    "decision": "BUY, SELL, or WAIT",
    "entry": null,
    "stopLoss": null,
    "takeProfits": [null, null, null],
    "confidence": "LOW, MEDIUM, or HIGH",
    "marketStructure": "Market structure",
    "supportResistance": "Support and resistance",
    "reasoning": "Reason for the decision",
    "invalidation": "What invalidates the setup",
    "dataQuality": "Image and chart quality"
  }
}

Use numeric prices for entry, stop loss and all three targets.
For WAIT, use null for entry, stop loss and all targets.
Never invent unreadable prices. If uncertain, choose WAIT.
For BUY, stop loss must be below entry and targets above entry.
For SELL, stop loss must be above entry and targets below entry.
Be cautious. Do not promise profits.
`;

    const response = await fetch(
      "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=" +
        encodeURIComponent(env.GEMINI_API_KEY),
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          contents: [{
            parts: [
              { text: prompt },
              {
                inline_data: {
                  mime_type: mimeType[1],
                  data: imageBase64
                }
              }
            ]
          }],
          generationConfig: {
            responseMimeType: "application/json",
            temperature: 0.2
          }
        })
      }
    );

    const result = await response.json();

    if (!response.ok) {
      return reply({
        error: result.error?.message || "Gemini analysis failed."
      }, 502);
    }

    const text = result.candidates?.[0]?.content?.parts?.[0]?.text;

    if (!text) {
      return reply({ error: "Gemini returned no analysis." }, 502);
    }

    const parsed = JSON.parse(text);
    const signal = parsed.signal;

    if (!signal) {
      return reply({ error: "Invalid analysis response." }, 502);
    }

    const targets = signal.takeProfits;
    const prices = [
      signal.entry,
      signal.stopLoss,
      ...(Array.isArray(targets) ? targets : [])
    ];

    const validPrices =
      prices.length === 5 &&
      prices.every(price =>
        typeof price === "number" &&
        Number.isFinite(price) &&
        price > 0
      );

    const validDirection =
      signal.decision === "BUY"
        ? signal.stopLoss < signal.entry &&
          targets[0] > signal.entry &&
          targets[1] > targets[0] &&
          targets[2] > targets[1]
        : signal.decision === "SELL"
        ? signal.stopLoss > signal.entry &&
          targets[0] < signal.entry &&
          targets[1] < targets[0] &&
          targets[2] < targets[1]
        : false;

    if (signal.decision === "WAIT") {
      signal.entry = null;
      signal.stopLoss = null;
      signal.takeProfits = [null, null, null];
    } else if (
      !["BUY", "SELL"].includes(signal.decision) ||
      !validPrices ||
      !validDirection
    ) {
      signal.decision = "WAIT";
      signal.entry = null;
      signal.stopLoss = null;
      signal.takeProfits = [null, null, null];
      signal.confidence = "LOW";
    }

    return reply({
      analysis: parsed.analysis || "Chart analysis completed.",
      signal,
      disclaimer: "Screenshot-based AI interpretation. Not financial advice."
    });

  } catch (error) {
    return reply({
      error: error.message || "Analysis failed."
    }, 500);
  }
}
