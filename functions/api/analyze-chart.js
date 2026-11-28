
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

1. Analyse the visible chart carefully before making any trading decision.

2. Identify the overall trend, market structure, momentum, support, resistance, supply and demand zones.

3. Evaluate BUY and SELL opportunities independently on every chart. Never focus on only one direction.

4. Evaluate both trend-continuation and reversal opportunities.

5. For BUY setups, look for bullish structure, strong support, liquidity sweeps, bullish rejection, confirmed breakouts or successful retests.

6. For SELL setups, look for bearish structure, strong resistance, liquidity sweeps, bearish rejection, confirmed breakdowns or successful retests.

7. Prioritize entries near defensible price levels. Avoid chasing candles after an extended move.

8. Require meaningful confirmation from price action. Prefer multiple independent pieces of evidence rather than relying on a single indicator or candle.

9. Evaluate the potential BUY and SELL setups separately. Select the direction with the strongest confirmed evidence and best risk-to-reward opportunity.

10. Only issue BUY or SELL when the entry, stop loss and all three take-profit levels are supported by visible market structure and meet the minimum 1:1.5 risk-to-reward requirement to TP1.

11. Use HIGH confidence only when several strong, independent confirmations agree. Use MEDIUM or LOW when evidence is weaker. Never inflate confidence.

12. Choose WAIT when neither direction has a sufficiently confirmed setup, the chart is unreadable, or risk management requirements cannot be satisfied. When a developing setup exists, explain whether it is an early BUY opportunity or an early SELL opportunity, its score, the important price zone, and the exact confirmation still needed. Never invent entry, stop loss or take-profit prices for an unconfirmed setup.

13. Whenever choosing WAIT, explain the exact missing confirmation for BOTH BUY and SELL, the important price levels, and what price action would validate either direction.

14. Never invent prices, confirmations, indicators or market information that cannot be established from the chart.

15. Never force a trade just to avoid returning WAIT. Protecting capital is more important than generating frequent signals.
DEEP MARKET STRUCTURE ANALYSIS:

1. Identify whether the market is trending bullish, trending bearish, ranging, or transitioning.

2. Examine recent swing highs and swing lows. Identify higher highs, higher lows, lower highs and lower lows only when clearly visible.

3. Identify whether price is making a genuine breakout, a false breakout, a pullback, a retest or a liquidity sweep.

4. Evaluate whether bullish or bearish momentum is strengthening, weakening or showing signs of exhaustion.

5. Identify nearby support, resistance, supply and demand zones using visible price action.

6. For BUY setups, examine whether buyers are defending support, forming bullish structure or reclaiming broken resistance.

7. For SELL setups, examine whether sellers are defending resistance, forming bearish structure or breaking and retesting support.

8. Distinguish a confirmed breakout from a temporary candle spike. Prefer a candle close and successful retest when visible.

9. Never assume that a support or resistance level will hold. Look for actual price reaction.

10. Explain which direction has stronger evidence and identify the exact evidence that is still missing.

11. If the market is consolidating, explain the range boundaries and avoid treating movement inside the range as a confirmed breakout.

12. Base every conclusion on visible chart evidence. Never invent volume, indicators or price levels that are not visible.

OPPORTUNITY SCORING SYSTEM:

EARLY OPPORTUNITY WATCH:

1. Calculate the BUY score and SELL score once, each as an integer from 0 to 10.

2. Use those exact same scores in the reasoning field and earlyOpportunity field. Never calculate or report different scores in different sections.

3. If the final decision is WAIT and the higher directional score is at least 6/10, return an earlyOpportunity object for that direction.

4. The earlyOpportunity score must exactly equal the corresponding BUY or SELL score in reasoning.

5. Use this exact structure:
{
  "direction": "BUY or SELL",
  "score": 6,
  "priceZone": "Relevant price zone visible on the chart",
  "confirmationNeeded": "Specific confirmation still required"
}

6. If BUY and SELL scores are equal, return earlyOpportunity as null.

7. If both scores are below 6/10, return earlyOpportunity as null.

8. If the final decision is BUY or SELL, return earlyOpportunity as null.

9. Never invent prices or present an early opportunity as a confirmed trade.

SCORING CRITERIA:

1. Evaluate BUY and SELL setups independently. Never give either direction an automatic advantage.

2. Score each direction separately out of 10 points using these criteria:

- Trend alignment: 0–2 points.
- Market structure: 0–2 points.
- Support, resistance and entry location: 0–2 points.
- Momentum and price-action confirmation: 0–2 points.
- Risk-to-reward and stop-loss placement: 0–2 points.

DIRECTIONAL SCORING REQUIREMENTS:

1. Assess BUY and SELL independently. Never assume both directions deserve similar scores.

2. Score each of the five criteria separately from 0 to 2 for BOTH directions.

3. Use these strict scoring definitions:

0/2 — NO EVIDENCE:
No clear supporting evidence is visible, or the evidence contradicts the direction.

1/2 — PARTIAL EVIDENCE:
Some supporting evidence exists, but it is mixed, weak, incomplete or not sufficiently confirmed.

2/2 — STRONG EVIDENCE:
Clear, visible and convincing evidence strongly supports the direction. The evidence must be specific to that criterion.

4. Do not automatically award 1/2. Award zero when evidence is absent or contradictory. Award two only when evidence is strong and clearly visible.

5. TREND ALIGNMENT:
BUY: Award points for a clearly bullish trend or convincing bullish recovery.
SELL: Award points for a clearly bearish trend or convincing bearish recovery.
A sideways market must not automatically receive full trend points.

6. MARKET STRUCTURE:
Evaluate swing highs, swing lows, breakouts, breakdowns, reversals and structural shifts.
Award points only when the relevant structure is visible.

7. SUPPORT, RESISTANCE AND ENTRY LOCATION:
Evaluate whether price is near a meaningful level and whether the location offers a defensible entry.
Do not award full points simply because support or resistance exists somewhere on the chart.

8. MOMENTUM AND PRICE ACTION:
Evaluate candle strength, rejection, follow-through, directional momentum and confirmation.
Do not confuse a single large candle with sustained momentum.

9. RISK-TO-REWARD AND STOP-LOSS:
Evaluate whether a defensible entry, logical stop-loss and sufficient target distance can be established.
If these cannot be established, award zero or partial points as justified. Never invent trade levels.

10. For every criterion, explain the exact chart evidence supporting the score and why the remaining points were withheld.

11. Compare BUY and SELL evidence directly. Clearly identify which direction is stronger, or explain why neither direction has an advantage.

12. A developing opportunity may receive partial evidence-based credit before full confirmation, but it must never be presented as a confirmed trade.

13. Add the five scores to calculate each directional total. The totals must match the individual scores exactly.

14. Never balance BUY and SELL scores artificially. Their scores may be very different.

15. Never manipulate scores to force a BUY or SELL signal. Preserve every existing trade confirmation, risk-to-reward and safety requirement.

16. DIRECTIONAL PRESSURE COMPARISON:
Do not treat a previous bearish or bullish impulse as proof that the same direction is still dominant. Separate the previous move from the current price action.

17. For BUY analysis, examine whether buyers are defending support, creating higher lows, producing strong bullish candles, or breaking resistance.

18. For SELL analysis, examine whether sellers are rejecting resistance, creating lower highs, producing strong bearish candles, or breaking support.

19. When price is recovering after a bearish impulse, assess whether the recovery is a genuine bullish reversal, a weak corrective retracement, or simply consolidation. Apply the same principle to bearish recoveries after bullish impulses.

20. Compare the latest visible candles and swing structure for both directions. Award different scores whenever the chart provides different evidence. Equal scores are acceptable only when the evidence genuinely supports equal scores.

21. Never invent candle patterns, swing points, breakouts, or momentum. If the screenshot does not provide enough evidence, state that the evidence is insufficient and score conservatively.

22. In the reasoning, explain the strongest evidence favouring BUY, the strongest evidence favouring SELL, and why one direction does or does not have an advantage. Do not merely repeat that the market is consolidating.

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
    "reasoning": "BUY score: X/10. BUY breakdown: Trend X/2, Structure X/2, Support/Resistance X/2, Momentum X/2, Risk-to-reward X/2. SELL score: X/10. SELL breakdown: Trend X/2, Structure X/2, Support/Resistance X/2, Momentum X/2, Risk-to-reward X/2. Each X must be a whole number from 0 to 2. Add the five scores to get the total out of 10. The total must match exactly. Never use /1, /5 or any denominator other than /2 for individual criteria. Explain which setup is stronger and why.",
    "whatToWaitFor": "Exact confirmation needed",
    "invalidation": "What invalidates the setup",
    "dataQuality": "Image and chart quality"
  },
  "earlyOpportunity": null
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
    let earlyOpportunity = null;


const early = parsed.earlyOpportunity;
const reasoning = String(signal.reasoning || "");

const buyMatch = reasoning.match(/BUY score:\s*(\d+)\s*\/\s*10/i);
const sellMatch = reasoning.match(/SELL score:\s*(\d+)\s*\/\s*10/i);

const buyScore = buyMatch ? Number(buyMatch[1]) : null;
const sellScore = sellMatch ? Number(sellMatch[1]) : null;

// Validate all five scoring criteria and their totals.
function validateBreakdown(direction, total) {
  const match = reasoning.match(
    new RegExp(
      direction +
      "\\s+breakdown:\\s*Trend\\s*(\\d+)\\s*/\\s*2\\s*,\\s*" +
      "Structure\\s*(\\d+)\\s*/\\s*2\\s*,\\s*" +
      "Support/Resistance\\s*(\\d+)\\s*/\\s*2\\s*,\\s*" +
      "Momentum\\s*(\\d+)\\s*/\\s*2\\s*,\\s*" +
      "Risk-to-reward\\s*(\\d+)\\s*/\\s*2",
      "i"
    )
  );

  if (!match || total === null || total < 0 || total > 10) {
    return false;
  }

  const scores = match.slice(1).map(Number);

  return (
    scores.every(score =>
      Number.isInteger(score) && score >= 0 && score <= 2
    ) &&
    scores.reduce((sum, score) => sum + score, 0) === total
  );
}

const validScoring =
  validateBreakdown("BUY", buyScore) &&
  validateBreakdown("SELL", sellScore);

if (!validScoring) {
  return reply({
    error: "Invalid scoring breakdown. Please run the chart analysis again."
  }, 502);
}


if (
  signal.decision === "WAIT" &&
  early &&
  typeof early === "object" &&
  !Array.isArray(early) &&
  buyScore !== null &&
  sellScore !== null
) {
  const matchingScore =
    early.direction === "BUY" ? buyScore :
    early.direction === "SELL" ? sellScore :
    null;

  const validEarly =
    matchingScore !== null &&
    typeof early.score === "number" &&
    Number.isInteger(early.score) &&
    early.score >= 6 &&
    early.score <= 10 &&
    early.score === matchingScore &&
    buyScore !== sellScore &&
    typeof early.priceZone === "string" &&
    early.priceZone.trim().length > 0 &&
    typeof early.confirmationNeeded === "string" &&
    early.confirmationNeeded.trim().length > 0;

  if (validEarly) {
    earlyOpportunity = {
      direction: early.direction,
      score: early.score,
      priceZone: early.priceZone.trim(),
      confirmationNeeded: early.confirmationNeeded.trim()
    };
  }
}

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
  earlyOpportunity,
  disclaimer: "Screenshot-based AI interpretation. Not financial advice."
});

  } catch (error) {
    return reply({
      error: error.message || "Analysis failed."
    }, 500);
  }
}
