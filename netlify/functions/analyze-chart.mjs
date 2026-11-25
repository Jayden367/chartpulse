
export const handler = async (event) => {
  const headers = {
    "Content-Type": "application/json",
    "Access-Control-Allow-Origin": "*"
  };

  if (event.httpMethod === "OPTIONS") {
    return { statusCode: 204, headers };
  }

  if (event.httpMethod !== "POST") {
    return {
      statusCode: 405,
      headers,
      body: JSON.stringify({ error: "Use POST." })
    };
  }

  try {
    const apiKey = process.env.GEMINI_API_KEY;

    if (!apiKey) {
      throw new Error("AI service is not configured.");
    }

    const body = JSON.parse(event.body || "{}");
    const image = body.imageDataUrl;
    const symbol = String(body.symbol || "XAUUSD");
    const timeframe = String(body.timeframe || "M15");

    if (!image || typeof image !== "string") {
      return {
        statusCode: 400,
        headers,
        body: JSON.stringify({ error: "Please provide a chart image." })
      };
    }

    const match = image.match(
      /^data:(image\/(?:jpeg|png|webp));base64,([A-Za-z0-9+/=]+)$/
    );

    if (!match || match[2].length > 9000000) {
      return {
        statusCode: 400,
        headers,
        body: JSON.stringify({ error: "Invalid image or image too large." })
      };
    }

    const mimeType = match[1];
    const imageData = match[2];

    const prompt = `
Analyze this trading chart screenshot.

Market: ${symbol}
Timeframe: ${timeframe}

Explain:
1. Overall trend and market structure.
2. Important support and resistance areas.
3. Possible bullish scenario.
4. Possible bearish scenario.
5. What would invalidate each scenario.
6. Whether the screenshot is clear enough to analyze.

Do not invent prices, indicators, or levels that cannot be read.
If an entry, stop loss, or target cannot be justified, say so.
Explain uncertainty. This is educational analysis, not financial advice.
Do not claim these are live prices.

Return a clear, useful analysis in plain text.
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
          }]
        })
      }
    );

    const result = await response.json();

    if (!response.ok) {
      return {
        statusCode: 502,
        headers,
        body: JSON.stringify({
          error: "The AI analysis service could not complete the request."
        })
      };
    }

    const analysis =
      result.candidates?.[0]?.content?.parts
        ?.map(part => part.text || "")
        .join("\n")
        .trim();

    if (!analysis) {
      throw new Error("No analysis was returned.");
    }

    return {
      statusCode: 200,
      headers,
      body: JSON.stringify({
        analysis,
        disclaimer: "AI chart interpretation only. Not financial advice."
      })
    };

  } catch (error) {
    return {
      statusCode: 500,
      headers,
      body: JSON.stringify({
        error: error.message || "Something went wrong."
      })
    };
  }
};
