
export const handler = async (event) => {
  const headers = {
    "Content-Type": "application/json",
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Headers": "Content-Type",
    "Access-Control-Allow-Methods": "POST, OPTIONS"
  };

  const reply = (statusCode, data) => ({
    statusCode,
    headers,
    body: JSON.stringify(data)
  });

  if (event.httpMethod === "OPTIONS") {
    return reply(204, {});
  }

  if (event.httpMethod !== "POST") {
    return reply(405, { error: "Use POST." });
  }

  try {
    const apiKey = process.env.GEMINI_API_KEY;

    if (!apiKey) {
      return reply(500, {
        error: "AI service is not configured."
      });
    }

    const body = JSON.parse(event.body || "{}");
    const image = body.imageDataUrl;
    const symbol = String(body.symbol || "XAUUSD");
    const timeframe = String(body.timeframe || "M15");

    if (!image || typeof image !== "string") {
      return reply(400, {
        error: "Please provide a chart image."
      });
    }

    const match = image.match(
      /^data:(image\/(?:jpeg|png|webp));base64,([A-Za-z0-9+/=]+)$/
    );

    if (!match || match[2].length > 9000000) {
      return reply(400, {
        error: "Invalid image or image too large."
      });
    }

    const mimeType = match[1];
    const imageData = match[2];

    const prompt = `
You are ChartPulse AI, a careful technical
trading chart analyst.

Analyze the supplied chart screenshot.

Market: ${symbol}
Timeframe: ${timeframe}

Return ONLY valid JSON with these fields:

{
  "decision": "BUY, SELL, or WAIT",
  "entry": null,
  "stopLoss": null,
  "takeProfits": [null, null, null],
  "confidence": "LOW, MEDIUM, or HIGH",
  "marketStructure": "",
  "supportResistance": "",
  "reasoning": "",
  "invalidation": "",
  "dataQuality": ""
}

RULES:

1. Identify the market direction and structure.
2. Only choose BUY or SELL when the screenshot
   supports a clear technical setup.
3. Otherwise choose WAIT.
4. Never invent market prices.
5. Only provide numeric entry, stop loss and
   take profit values when they are justified
   by readable chart prices and analysis.
6. TP1, TP2 and TP3 must be in the correct
   direction relative to the entry.
7. Stop loss must be on the protective side
   of the entry.
8. If prices cannot be established, use null
   and choose WAIT.
9. Explain the reasons for the signal.
10. State what would invalidate the setup.
11. Confidence is a qualitative assessment,
    NOT a measured probability.
12. This screenshot is not live market data.
13. Do not promise profitable trades.

Return JSON only.
`;

    const response = await fetch(
      "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "x-goog-api-key": apiKey
        },
        body: JSON.stringify({
          contents: [{
            parts: [
              { text: prompt },
              {
                inlineData: {
                  mimeType,
                  data: imageData
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
      throw new Error("Gemini could not analyze the chart.");
    }

    const raw = result.candidates?.[0]?.content
      ?.parts?.map(part => part.text || "")
      .join("")
      .trim();

    if (!raw) {
      throw new Error("No AI analysis was returned.");
    }

    const signal = JSON.parse(raw);

    if (!["BUY", "SELL", "WAIT"].includes(signal.decision)) {
      throw new Error("Invalid AI signal.");
    }

    if (!["LOW", "MEDIUM", "HIGH"].includes(signal.confidence)) {
      signal.confidence = "LOW";
    }

    const targets = Array.isArray(signal.takeProfits)
      ? signal.takeProfits.slice(0, 3)
      : [null, null, null];

    while (targets.length < 3) targets.push(null);

    const prices = [
      signal.entry,
      signal.stopLoss,
      ...targets
    ];

    const validPrices = prices.every(
      price => typeof price === "number" &&
        Number.isFinite(price) && price > 0
    );

    if (!validPrices) {
      signal.decision = "WAIT";
      signal.entry = null;
      signal.stopLoss = null;
      signal.takeProfits = [null, null, null];
      signal.confidence = "LOW";
    } else {
      signal.takeProfits = targets;
    }

    const format = value =>
      value === null ? "Not established" : String(value);

    const analysis = `
CHARTPULSE AI - ${timeframe}

Symbol: ${symbol}
Status: SIGNAL ONLY - Execute manually

SIGNAL: ${signal.decision}

Entry: ${format(signal.entry)}
Stop Loss: ${format(signal.stopLoss)}

TP1: ${format(signal.takeProfits[0])}
TP2: ${format(signal.takeProfits[1])}
TP3: ${format(signal.takeProfits[2])}

Confidence: ${signal.confidence}

MARKET STRUCTURE:
${signal.marketStructure || "Not established"}

SUPPORT AND RESISTANCE:
${signal.supportResistance || "Not established"}

AI REASONING:
${signal.reasoning || "No additional explanation."}

INVALIDATION:
${signal.invalidation || "Not established"}

DATA QUALITY:
${signal.dataQuality || "Screenshot only."}

WARNING:
Screenshot-based AI analysis.
Not live market data or financial advice.
`;

    return reply(200, {
      analysis,
      signal,
      disclaimer:
        "Screenshot-based AI interpretation. Not financial advice."
    });

  } catch (error) {
    return reply(500, {
      error: error.message || "Analysis failed."
    });
  }
};
