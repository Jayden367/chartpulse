
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

1. Identify the overall market trend: bullish, bearish, or ranging.

2. Identify market structure, including higher highs, higher lows, lower highs, lower lows, break of structure, and change of character.

3. Identify important support, resistance, supply, and demand zones using visible price action.

4. Check for liquidity sweeps, breakouts, breakdowns, retests, rejection candles, and momentum shifts.

5. Evaluate both BUY and SELL opportunities on every chart. Do not automatically choose WAIT.

6. For BUY opportunities, look for bullish trends, pullbacks to support, bullish rejection candles, higher lows, and confirmed bullish breakouts or structure shifts.

7. For SELL opportunities, look for bearish trends, pullbacks to resistance, bearish rejection candles, lower highs, and confirmed bearish breakdowns or structure shifts.

8. Evaluate continuation and reversal setups separately. Do not reject a valid continuation setup simply because a full trend reversal has not occurred.

9. A BUY or SELL may be selected when price action provides a defensible entry, stop loss, three logical take-profit targets, and a minimum risk-to-reward ratio of 1:1.5 to TP1.

10. Do not require a perfect chart or unnecessary additional confirmation when the available evidence already supports a valid trade.

11. Choose WAIT only when no sufficiently clear and defensible setup exists, the chart is unreadable, important confirmation is missing, or the risk-to-reward requirement cannot be satisfied.

12. Whenever choosing WAIT, explain the exact missing confirmation, relevant price area, and what price action would make a BUY or SELL valid. Never request confirmation that has already occurred.

13. Never invent prices, force trades, or sacrifice risk management just to produce a BUY or SELL signal.

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
    "whatToWaitFor": "Exact price action or confirmation needed before the next trade decision",
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
- For WAIT, whatToWaitFor must explain a specific confirmation to watch for based on the chart.
- For BUY or SELL, whatToWaitFor should briefly explain what would confirm the setup or state that the entry conditions are already met.
- Never invent a confirmation price that cannot be read from the screenshot.
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

    const validTargets =
      Array.isArray(targets) && targets.length === 3;

    const prices = [
      signal.entry,
      signal.stopLoss,
      ...(validTargets ? targets : [])
    ];

    const validPrices =
      prices.length === 5 &&
      prices.every(price =>
        typeof price === "number" &&
        Number.isFinite(price) &&
        price > 0
      );

    const validDirection =
      validTargets &&
      (
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
          : false
      );

    const risk = validPrices
      ? Math.abs(signal.entry - signal.stopLoss)
      : 0;

    const reward = validPrices
      ? Math.abs(targets[0] - signal.entry)
      : 0;

    const riskReward = risk > 0 ? reward / risk : 0;

    const validRiskReward = riskReward >= 1.5;

    if (signal.decision === "WAIT") {
      signal.entry = null;
      signal.stopLoss = null;
      signal.takeProfits = [null, null, null];
    } else if (
      !["BUY", "SELL"].includes(signal.decision) ||
      !validPrices ||
      !validDirection ||
      !validRiskReward
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
