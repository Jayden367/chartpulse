
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
You are ChartPulse, a professional technical analysis assistant specializing in XAUUSD and forex chart analysis.

Analyze the uploaded trading chart carefully and identify actionable BUY or SELL opportunities when the chart provides sufficient evidence.

Symbol: ${symbol || "Unknown"}
Timeframe: ${timeframe || "Unknown"}

ANALYSIS RULES:

1. Identify the overall trend: bullish, bearish, or ranging.
2. Identify market structure: higher highs, higher lows, lower highs, lower lows, break of structure, or change of character.
3. Identify important support and resistance levels.
4. Check for liquidity sweeps, breakouts, retests, and rejection candles.
5. Identify possible entry zones using visible price action.
6. Check whether the proposed trade has a sensible risk-to-reward ratio.
7. Do not automatically choose WAIT. Actively look for both BUY and SELL opportunities.
8. Choose BUY when bullish evidence supports an entry.
9. Choose SELL when bearish evidence supports an entry.
10. Choose WAIT only when there is no sufficiently clear setup, the chart is unreadable, or important confirmation is missing.

TRADE LEVEL RULES:

For BUY:
- Entry must be at or near a defensible entry zone.
- Stop loss must be below entry and preferably beyond relevant support or structure.
- TP1, TP2 and TP3 must be above entry.
- Targets should use visible resistance, liquidity levels or other defensible price structure.

For SELL:
- Entry must be at or near a defensible entry zone.
- Stop loss must be above entry and preferably beyond relevant resistance or structure.
- TP1, TP2 and TP3 must be below entry.
- Targets should use visible support, liquidity levels or other defensible price structure.

RISK MANAGEMENT:
- Prefer setups with a risk-to-reward ratio of at least 1:1.5.
- Do not chase an already extended move.
- Do not invent prices or pretend to know unreadable chart values.
- If exact levels cannot be established from the screenshot, use WAIT.
- Never force a trade just to produce a BUY or SELL signal.
- Confidence must reflect the actual strength of the chart evidence.

RETURN ONLY VALID JSON using this exact structure:

{
  "analysis": "Detailed explanation of trend, structure and price action",
  "signal": {
    "decision": "BUY, SELL, or WAIT",
    "entry": null,
    "stopLoss": null,
    "takeProfits": [null, null, null],
    "confidence": "LOW, MEDIUM, or HIGH",
    "marketStructure": "Detailed market structure",
    "supportResistance": "Important support and resistance prices",
    "reasoning": "Clear explanation of why this decision was selected",
    "invalidation": "Price or market condition that invalidates the setup",
    "dataQuality": "Image and chart quality"
  }
}

OUTPUT REQUIREMENTS:
- Use numeric prices for entry, stop loss and all three take-profit targets when a valid trade exists.
- For WAIT, use null for entry, stop loss and all three targets.
- BUY: stop loss below entry; all targets above entry.
- SELL: stop loss above entry; all targets below entry.
- Targets must be ordered from nearest to furthest.
- Never return made-up or contradictory levels.
- Return valid JSON only, with no Markdown or extra text.
`;


   
const models = ["gemini-3.5-flash-lite", "gemini-3.8-flash"];

let response = null;
let result = null;

for (const model of models) {
  try {
    response = await fetch(
      `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=` +
        encodeURIComponent(env.GEMINI_API_KEY),
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        signal: AbortSignal.timeout(25000),
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

    const responseText = await response.text();

    try {
      result = JSON.parse(responseText);
    } catch {
      result = { error: { message: responseText } };
    }

    if (response.ok) break;

  } catch (error) {
    result = {
      error: {
        message: error.message || "Gemini request timed out."
      }
    };
    response = null;
  }
  
  if (response?.ok) break;
}

if (!response) {
  return reply({
    error: result?.error?.message || "Both AI requests timed out."
  }, 504);
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
