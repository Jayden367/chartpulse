(() => {
  'use strict';

  const STORAGE_KEY = 'chartpulse_journal_v1';

  const CONTRACT_SPECS = {
    GOLD_XAUUSD: { id: 'GOLD_XAUUSD', displayName: 'Gold · XAUUSD ($100/pt)', dollarPerPointPerLot: 100, priceDecimals: 2 },
    FX_STANDARD: { id: 'FX_STANDARD', displayName: 'FX Major ($100k/lot)', dollarPerPointPerLot: 100000, priceDecimals: 4 },
    FX_JPY: { id: 'FX_JPY', displayName: 'FX JPY ($1k/lot)', dollarPerPointPerLot: 1000, priceDecimals: 2 },
    CRYPTO_INDEX: { id: 'CRYPTO_INDEX', displayName: 'Crypto / Index ($1/pt)', dollarPerPointPerLot: 1, priceDecimals: 2 }
  };

  function inferContractSpec(pair) {
    const u = String(pair || '').toUpperCase();
    if (u.includes('XAU') || u.includes('GOLD')) return 'GOLD_XAUUSD';
    if (u.includes('JPY')) return 'FX_JPY';
    if (u.includes('BTC') || u.includes('ETH') || u.includes('US30') || u.includes('NAS') || u.includes('SPX')) return 'CRYPTO_INDEX';
    return 'FX_STANDARD';
  }

  function createPresets() {
    return [
      {
        id: 'preset_xauusd_m15',
        pair: 'XAUUSD',
        timeframe: 'M15',
        currentPrice: 3634.69,
        marketStatus: 'CLOSED_ROLLOVER',
        marketStatusLabel: 'Market Closed · Rollover',
        marketStatusNote: 'Market closed during daily rollover window — spreads can widen sharply at reopen. Wait for spreads to normalize.',
        trendBias: 'BULLISH',
        signalSummary: 'BUY WATCH on 3631–3635 flip defense · SELL contingency on M15 close below 3630.',
        priceDecimals: 2,
        chartPatternPoints: [3618, 3605, 3612, 3633, 3601, 3624, 3636, 3613, 3642, 3674, 3689, 3671, 3659, 3647, 3639, 3632, 3634.69],
        keyLevels: [
          { id: 'xau_peak', title: 'Session Impulse High', low: 3686, high: 3689, role: 'Major Peak', type: 'SESSION_EXTREME', note: 'Full breakout peak and major liquidity target if bullish momentum resumes.' },
          { id: 'xau_res_mid', title: 'Mid-Range Supply', low: 3658, high: 3662, role: 'Upper Target', type: 'RESISTANCE', note: 'Secondary upside magnet once 3647 clears cleanly.' },
          { id: 'xau_res_near', title: 'Overhead Resistance', low: 3640, high: 3647, role: 'Structure Shift', type: 'RESISTANCE', note: 'Buyers must reclaim 3640–3647 to break the M15 lower-high sequence.' },
          { id: 'xau_sup_flip', title: 'Resistance-Turned-Support', low: 3631, high: 3635, role: 'Active Flip Zone', type: 'SUPPORT', note: 'Price is resting here at 3634.69 with early lower wicks defending 3631–3633.' },
          { id: 'xau_imb', title: 'Breakout Imbalance Fill', low: 3613, high: 3620, role: 'Lower Support', type: 'IMBALANCE', note: 'Origin of the big green breakout candle; thin structure above this pocket.' },
          { id: 'xau_low', title: 'Session Range Low', low: 3598, high: 3602, role: 'Range Floor', type: 'SESSION_EXTREME', note: 'Earlier consolidation floor if full retracement completes.' }
        ],
        priceActionPhases: [
          { title: 'Earlier Consolidation', rangeLabel: '~3600 – 3637', bias: 'NEUTRAL', summary: 'Sideways range with swing low near 3600 and resistance capping rallies around 3635–3637.' },
          { title: 'Sharp Bullish Impulse', rangeLabel: '~3613 → 3689', bias: 'BULLISH', summary: 'Mid-session vertical breakout cleared range highs and drove Gold up to a 3689 peak.' },
          { title: 'Steady Retracement', rangeLabel: '3689 → 3631', bias: 'BEARISH', summary: 'Lower highs and lower lows retraced ~70–75% of the breakout, resting right on old range highs.' }
        ],
        bullishScenario: {
          id: 'xau_bull',
          title: 'Bullish Bounce Signal',
          bias: 'BULLISH',
          triggerCondition: 'Hold 3631–3635 after reopen, wait for normal spreads, and confirm with a strong M15 bullish engulfing candle or higher low.',
          entryPrice: 3635.00,
          stopLossPrice: 3627.50,
          takeProfitPrice: 3658.00,
          invalidationNote: 'Invalidated on a clean M15 candle close below 3630.'
        },
        bearishScenario: {
          id: 'xau_bear',
          title: 'Bearish Continuation Signal',
          bias: 'BEARISH',
          triggerCondition: 'Clean M15 candle close below 3630 confirms sellers still control momentum from the 3689 sell-off.',
          entryPrice: 3629.50,
          stopLossPrice: 3637.50,
          takeProfitPrice: 3614.00,
          invalidationNote: 'Invalidated if price reclaims 3640–3647 overhead resistance.'
        }
      },
      {
        id: 'preset_eurusd_h1',
        pair: 'EURUSD',
        timeframe: 'H1',
        currentPrice: 1.0842,
        marketStatus: 'OPEN',
        marketStatusLabel: 'Session Active · Normal Spreads',
        marketStatusNote: 'London/NY overlap active — tight spreads and normal execution liquidity.',
        trendBias: 'BULLISH',
        signalSummary: 'BUY bias above 1.0820 London flip · SELL signal only if 1.0820 support breaks.',
        priceDecimals: 4,
        chartPatternPoints: [1.0812, 1.0802, 1.0818, 1.0796, 1.0788, 1.0824, 1.0848, 1.0856, 1.0838, 1.0842],
        keyLevels: [
          { id: 'eu_res', title: 'Weekly Supply Cap', low: 1.0875, high: 1.0890, role: 'Major Target', type: 'RESISTANCE', note: 'Prior swing high supply.' },
          { id: 'eu_sup', title: 'London Breakout Flip', low: 1.0820, high: 1.0832, role: 'Key Support', type: 'SUPPORT', note: 'Retest zone for continuation longs.' },
          { id: 'eu_low', title: 'Sweep Low', low: 1.0785, high: 1.0795, role: 'Invalidation', type: 'SESSION_EXTREME', note: 'Session liquidity sweep bottom.' }
        ],
        priceActionPhases: [
          { title: 'Asian Range Accumulation', rangeLabel: '1.0795 – 1.0820', bias: 'NEUTRAL', summary: 'Tight overnight compression below 1.0820 before London open sweep.' },
          { title: 'Liquidity Sweep & Reclaim', rangeLabel: '1.0788 → 1.0856', bias: 'BULLISH', summary: 'Quick stop-hunt below 1.0795 snapped back above 1.0825 on volume.' }
        ],
        bullishScenario: {
          id: 'eu_bull',
          title: 'Bullish Continuation Signal',
          bias: 'BULLISH',
          triggerCondition: 'Higher low on H1 pullback into 1.0825–1.0832 flip zone.',
          entryPrice: 1.0830,
          stopLossPrice: 1.0798,
          takeProfitPrice: 1.0885,
          invalidationNote: 'H1 close back inside Asian range below 1.0810.'
        },
        bearishScenario: {
          id: 'eu_bear',
          title: 'Failed Breakout Short Signal',
          bias: 'BEARISH',
          triggerCondition: 'Rejection wick at 1.0860 followed by close under 1.0820.',
          entryPrice: 1.0818,
          stopLossPrice: 1.0848,
          takeProfitPrice: 1.0788,
          invalidationNote: 'Clean H1 acceptance above 1.0855.'
        }
      },
      {
        id: 'preset_gbpusd_h4',
        pair: 'GBPUSD',
        timeframe: 'H4',
        currentPrice: 1.2685,
        marketStatus: 'OPEN',
        marketStatusLabel: 'Session Active · Normal Spreads',
        marketStatusNote: 'London session active — watch UK/US rate differential headlines around NY open.',
        trendBias: 'BEARISH',
        signalSummary: 'SELL WATCH under 1.2715 supply rejection · BUY recovery only on H4 reclaim above 1.2730.',
        priceDecimals: 4,
        chartPatternPoints: [1.2710, 1.2738, 1.2762, 1.2742, 1.2755, 1.2724, 1.2695, 1.2674, 1.2692, 1.2685],
        keyLevels: [
          { id: 'gu_res_high', title: 'Weekly Supply High', low: 1.2745, high: 1.2765, role: 'Invalidation', type: 'SESSION_EXTREME', note: 'Swing high cap protecting bearish structure.' },
          { id: 'gu_res_flip', title: 'Broken Trendline Flip', low: 1.2705, high: 1.2720, role: 'Active Supply', type: 'RESISTANCE', note: 'Prior support turned resistance for short entries.' },
          { id: 'gu_sup_target', title: 'H4 Demand Pocket', low: 1.2615, high: 1.2635, role: 'Downside Target', type: 'SUPPORT', note: 'Untapped demand cluster from prior impulse.' }
        ],
        priceActionPhases: [
          { title: 'Distribution Ceiling', rangeLabel: '1.2720 – 1.2765', bias: 'BEARISH', summary: 'Repeated upper wicks at 1.2750–1.2765 signaled institutional supply absorption.' },
          { title: 'Channel Breakdown', rangeLabel: '1.2740 → 1.2672', bias: 'BEARISH', summary: 'H4 bearish engulfing broke minor ascending trendline, retesting 1.2700 from below.' }
        ],
        bullishScenario: {
          id: 'gu_bull',
          title: 'Support Reclaim Long Signal',
          bias: 'BULLISH',
          triggerCondition: 'H4 close back above 1.2725 clearing the broken trendline zone.',
          entryPrice: 1.2728,
          stopLossPrice: 1.2688,
          takeProfitPrice: 1.2795,
          invalidationNote: 'Failure to hold 1.2700 on retest.'
        },
        bearishScenario: {
          id: 'gu_bear',
          title: 'Supply Retest Short Signal',
          bias: 'BEARISH',
          triggerCondition: 'Bearish pin bar or engulfing rejection inside 1.2705–1.2720.',
          entryPrice: 1.2708,
          stopLossPrice: 1.2748,
          takeProfitPrice: 1.2625,
          invalidationNote: 'Invalidated on H4 candle close above 1.2745.'
        }
      },
      {
        id: 'preset_usdjpy_h1',
        pair: 'USDJPY',
        timeframe: 'H1',
        currentPrice: 152.45,
        marketStatus: 'OPEN',
        marketStatusLabel: 'Session Active · Normal Spreads',
        marketStatusNote: 'Tokyo/London crossover liquidity — monitor US10Y yield correlation.',
        trendBias: 'BULLISH',
        signalSummary: 'BUY continuation above 152.00 round-number support · SELL reversal below 151.75.',
        priceDecimals: 2,
        chartPatternPoints: [151.35, 151.70, 151.55, 151.95, 151.82, 152.25, 152.68, 152.52, 152.38, 152.45],
        keyLevels: [
          { id: 'uj_res', title: 'Prior Swing High', low: 153.20, high: 153.45, role: 'Target Zone', type: 'RESISTANCE', note: 'Major upside liquidity pool.' },
          { id: 'uj_sup', title: '152.00 Flip Zone', low: 151.95, high: 152.15, role: 'Key Support', type: 'SUPPORT', note: 'Psychological round number acting as demand.' },
          { id: 'uj_imb', title: 'Tokyo Open Imbalance', low: 151.40, high: 151.65, role: 'Lower Support', type: 'IMBALANCE', note: 'Origin of breakout leg.' }
        ],
        priceActionPhases: [
          { title: 'Higher-Low Base', rangeLabel: '151.30 – 152.10', bias: 'NEUTRAL', summary: 'Clean ascending base forming higher lows into 152.00 psychological resistance.' },
          { title: '152.00 Breakout Hold', rangeLabel: '152.00 → 152.68', bias: 'BULLISH', summary: 'Impulse break above 152.00 followed by controlled flag pullback to 152.45.' }
        ],
        bullishScenario: {
          id: 'uj_bull',
          title: 'Flag Continuation Long Signal',
          bias: 'BULLISH',
          triggerCondition: 'Hold 152.10–152.25 on H1 pullback with bullish momentum candle.',
          entryPrice: 152.20,
          stopLossPrice: 151.75,
          takeProfitPrice: 153.30,
          invalidationNote: 'H1 close back under 151.90.'
        },
        bearishScenario: {
          id: 'uj_bear',
          title: 'False Break Reversal Short Signal',
          bias: 'BEARISH',
          triggerCondition: 'Decisive H1 breakdown below 151.90 invalidating the 152.00 breakout.',
          entryPrice: 151.85,
          stopLossPrice: 152.30,
          takeProfitPrice: 151.10,
          invalidationNote: 'Reclaim above 152.35.'
        }
      },
      {
        id: 'preset_btcusd_m30',
        pair: 'BTCUSD',
        timeframe: 'M30',
        currentPrice: 94280,
        marketStatus: 'OPEN',
        marketStatusLabel: 'Session Active · Normal Spreads',
        marketStatusNote: '24/7 crypto spot & perp market active — monitor funding and NY session volatility.',
        trendBias: 'BULLISH',
        signalSummary: 'BUY retest at 93,900–94,150 · SELL deviation sweep below 93,900.',
        priceDecimals: 0,
        chartPatternPoints: [92700, 93400, 92450, 93600, 94050, 93250, 94120, 94850, 94360, 94280],
        keyLevels: [
          { id: 'btc_res', title: 'Psychological Supply', low: 95400, high: 95800, role: 'Target Zone', type: 'RESISTANCE', note: 'Round-number resistance cluster.' },
          { id: 'btc_sup', title: 'Breakout Retest', low: 93900, high: 94150, role: 'Support Flip', type: 'SUPPORT', note: 'Prior range ceiling now acting as support.' },
          { id: 'btc_imb', title: 'Mid-Range Pocket', low: 92800, high: 93200, role: 'Imbalance', type: 'IMBALANCE', note: 'Unfilled M30 impulse pocket.' }
        ],
        priceActionPhases: [
          { title: 'Range Compression', rangeLabel: '92,400 – 94,100', bias: 'NEUTRAL', summary: 'Multi-hour coiled structure building liquidity above 94,100.' },
          { title: 'Range High Breakout', rangeLabel: '94,100 → 94,850', bias: 'BULLISH', summary: 'Impulse push testing upper resistance with shallow pullback to 94,280.' }
        ],
        bullishScenario: {
          id: 'btc_bull',
          title: 'Breakout Retest Long Signal',
          bias: 'BULLISH',
          triggerCondition: 'Hold 93,900–94,150 on M30 close with rising volume.',
          entryPrice: 94150,
          stopLossPrice: 93550,
          takeProfitPrice: 95550,
          invalidationNote: 'M30 close back below 93,800.'
        },
        bearishScenario: {
          id: 'btc_bear',
          title: 'Deviation Sweep Short Signal',
          bias: 'BEARISH',
          triggerCondition: 'Failure back below 93,900 sweeps toward 93,000 imbalance.',
          entryPrice: 93850,
          stopLossPrice: 94450,
          takeProfitPrice: 92850,
          invalidationNote: 'Reclaim above 94,500.'
        }
      }
    ];
  }

  function loadInitialJournal() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (raw) {
        const parsed = JSON.parse(raw);
        if (Array.isArray(parsed)) return parsed;
      }
    } catch (_) {
      // Fallback to default seeded trades if storage is unavailable
    }
    const now = Date.now();
    return [
      {
        id: now,
        pair: 'XAUUSD',
        timeframe: 'M15',
        direction: 'BUY',
        entryPrice: 3634.50,
        stopLoss: 3627.50,
        takeProfit: 3655.00,
        accountBalance: 10000,
        riskPercent: 1.0,
        lotSize: 0.14,
        riskRewardRatio: 2.93,
        profitLossUsd: null,
        status: 'PLANNED',
        notes: 'Wait for market reopen spread normalization + M15 bullish engulfing hold above 3631–3635 flip zone.',
        createdAtMillis: now
      },
      {
        id: now - 3600000,
        pair: 'XAUUSD',
        timeframe: 'M15',
        direction: 'SELL',
        entryPrice: 3629.50,
        stopLoss: 3637.50,
        takeProfit: 3614.00,
        accountBalance: 10000,
        riskPercent: 1.0,
        lotSize: 0.12,
        riskRewardRatio: 1.94,
        profitLossUsd: null,
        status: 'PLANNED',
        notes: 'Bearish continuation trigger on decisive M15 close below 3630 targeting lower breakout imbalance 3613–3620.',
        createdAtMillis: now - 3600000
      }
    ];
  }

  function persistJournal() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(state.journal));
    } catch (_) {
      // Ignore storage quota errors in private browsing
    }
  }

  const presets = createPresets();

  const state = {
    activeTab: 'analysis',
    currentAnalysis: structuredClone(presets[0]),
    messages: [
      {
        id: 'init_1',
        isUser: false,
        text: '[Sample Preview] Loaded XAUUSD M15 (3634.69 · Market Closed). Price is resting on the 3631–3635 resistance-turned-support flip after a 70–75% retracement from 3689. Tap a scenario below or ask a follow-up question.'
      }
    ],
    planner: {
      pair: 'XAUUSD',
      timeframe: 'M15',
      direction: 'LONG',
      balanceInput: '10000',
      riskPercentInput: '1.0',
      lotSizeInput: '0.13',
      isCustomLotMode: false,
      entryInput: '3635.00',
      stopLossInput: '3627.50',
      takeProfitInput: '3658.00',
      contractSpec: 'GOLD_XAUUSD',
      spreadNormalized: false,
      candleConfirmed: false,
      notesInput: 'Wait for market reopen spreads to normalize, then watch for M15 bullish hold above 3631–3635.',
      calculationStatusMessage: null
    },
    journal: loadInitialJournal(),
    journalFilter: 'ALL'
  };

  function fmt(num, decimals) {
    const n = Number(num);
    if (!Number.isFinite(n)) return '--';
    return n.toFixed(decimals);
  }

  function escapeHtml(str) {
    return String(str ?? '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function filterNumeric(raw, allowSign = false) {
    const normalized = String(raw || '').replace(/,/g, '.');
    let out = '';
    let hasDot = false;
    for (let i = 0; i < normalized.length; i++) {
      const ch = normalized[i];
      if (ch >= '0' && ch <= '9') {
        out += ch;
      } else if (ch === '.' && !hasDot) {
        hasDot = true;
        out += ch;
      } else if (allowSign && (ch === '-' || ch === '+') && i === 0) {
        out += ch;
      }
    }
    return out;
  }

  function calculateMetrics(p) {
    const spec = CONTRACT_SPECS[p.contractSpec] || CONTRACT_SPECS.GOLD_XAUUSD;
    const balance = parseFloat(p.balanceInput) || 0;
    const riskPct = parseFloat(p.riskPercentInput) || 0;
    const customLots = parseFloat(p.lotSizeInput) || 0;
    const entry = parseFloat(p.entryInput) || 0;
    const sl = parseFloat(p.stopLossInput) || 0;
    const tp = parseFloat(p.takeProfitInput) || 0;

    if (p.isCustomLotMode) {
      if (customLots <= 0) {
        return { isValid: false, hint: 'Enter a positive Lot Size or Risk %.', stopDist: 0, targetDist: 0, rr: 0, riskUsd: 0, rewardUsd: 0, lots: 0, impliedRiskPct: 0 };
      }
    } else if (balance <= 0 || riskPct <= 0) {
      return { isValid: false, hint: 'Enter positive values for Account Balance and Risk %.', stopDist: 0, targetDist: 0, rr: 0, riskUsd: 0, rewardUsd: 0, lots: 0, impliedRiskPct: 0 };
    }

    if (entry <= 0 || sl <= 0 || tp <= 0) {
      return { isValid: false, hint: 'Enter positive values for Entry Price, Stop Loss, and Take Profit.', stopDist: 0, targetDist: 0, rr: 0, riskUsd: 0, rewardUsd: 0, lots: 0, impliedRiskPct: 0 };
    }

    const isLong = p.direction === 'LONG';
    if (isLong ? sl >= entry : sl <= entry) {
      return {
        isValid: false,
        hint: isLong ? 'For a BUY (LONG) setup, Stop Loss must be below Entry Price.' : 'For a SELL (SHORT) setup, Stop Loss must be above Entry Price.',
        stopDist: 0, targetDist: 0, rr: 0, riskUsd: 0, rewardUsd: 0, lots: 0, impliedRiskPct: 0
      };
    }
    if (isLong ? tp <= entry : tp >= entry) {
      return {
        isValid: false,
        hint: isLong ? 'For a BUY (LONG) setup, Take Profit must be above Entry Price.' : 'For a SELL (SHORT) setup, Take Profit must be below Entry Price.',
        stopDist: 0, targetDist: 0, rr: 0, riskUsd: 0, rewardUsd: 0, lots: 0, impliedRiskPct: 0
      };
    }

    const stopDist = Math.abs(entry - sl);
    const targetDist = Math.abs(tp - entry);
    const rr = stopDist > 0 ? targetDist / stopDist : 0;

    if (p.isCustomLotMode) {
      const riskUsd = customLots * stopDist * spec.dollarPerPointPerLot;
      const rewardUsd = customLots * targetDist * spec.dollarPerPointPerLot;
      const impliedRiskPct = balance > 0 ? (riskUsd / balance) * 100 : 0;
      return { isValid: true, hint: null, stopDist, targetDist, rr, riskUsd, rewardUsd, lots: customLots, impliedRiskPct };
    } else {
      const riskUsd = balance * (riskPct / 100);
      const riskPerLot = stopDist * spec.dollarPerPointPerLot;
      const lots = riskPerLot > 0 ? riskUsd / riskPerLot : 0;
      const rewardUsd = riskUsd * rr;
      return { isValid: true, hint: null, stopDist, targetDist, rr, riskUsd, rewardUsd, lots, impliedRiskPct: riskPct };
    }
  }

  function syncPlannerComputed() {
    const m = calculateMetrics(state.planner);
    if (!m.isValid) return m;
    if (state.planner.isCustomLotMode) {
      if ((parseFloat(state.planner.balanceInput) || 0) > 0) {
        state.planner.riskPercentInput = fmt(m.impliedRiskPct, 2);
      }
    } else {
      state.planner.lotSizeInput = fmt(m.lots, 2);
    }
    return m;
  }

  /* ======================== NAVIGATION ======================== */
  function switchTab(tab) {
    state.activeTab = tab;
    document.querySelectorAll('[data-tab]').forEach((btn) => {
      const isActive = btn.getAttribute('data-tab') === tab;
      btn.classList.toggle('active', isActive);
      btn.setAttribute('aria-pressed', String(isActive));
    });
    document.getElementById('tab-analysis').hidden = tab !== 'analysis';
    document.getElementById('tab-planner').hidden = tab !== 'planner';
    document.getElementById('tab-journal').hidden = tab !== 'journal';

    if (tab === 'analysis') {
      drawPriceActionCanvas();
    } else if (tab === 'planner') {
      renderPlanner();
    } else if (tab === 'journal') {
      renderJournal();
    }
    window.scrollTo({ top: 0, behavior: 'instant' });
  }

  document.querySelectorAll('[data-tab]').forEach((btn) => {
    btn.addEventListener('click', () => switchTab(btn.getAttribute('data-tab')));
  });

  /* ======================== MODALS ======================== */
  const modalBackdrop = document.getElementById('modal-backdrop');
  const modalTitle = document.getElementById('modal-title');
  const modalBody = document.getElementById('modal-body');
  document.getElementById('modal-close-btn').addEventListener('click', closeModal);
  modalBackdrop.addEventListener('click', (e) => {
    if (e.target === modalBackdrop) closeModal();
  });

  function openModal(title, html, setupFn) {
    modalTitle.textContent = title;
    modalBody.innerHTML = html;
    modalBackdrop.hidden = false;
    if (setupFn) setupFn(modalBody);
  }

  function closeModal() {
    modalBackdrop.hidden = true;
    modalBody.innerHTML = '';
  }

  /* ======================== ANALYSIS TAB ======================== */
  function renderAnalysis() {
    const a = state.currentAnalysis;
    const dec = a.priceDecimals;
    const isLive = a.marketStatus === 'OPEN';
    const biasBadgeClass = a.trendBias === 'BULLISH' ? 'badge-bull' : a.trendBias === 'BEARISH' ? 'badge-bear' : 'badge-gold';
    const biasLabel = a.trendBias === 'BULLISH' ? 'BUY WATCH' : a.trendBias === 'BEARISH' ? 'SELL WATCH' : 'RANGE NEUTRAL';
    const nearestSup = a.keyLevels.find((z) => z.type === 'SUPPORT');
    const nearestRes = a.keyLevels.find((z) => z.type === 'RESISTANCE');

    const root = document.getElementById('analysis-root');
    root.innerHTML = `
      <div class="hero-card">
        ${a.imageUri ? `<img class="hero-screenshot-bg" src="${escapeHtml(a.imageUri)}" alt="Uploaded chart screenshot" />` : ''}
        <div class="hero-inner stack-md">
          <div class="row-between">
            <div>
              <div class="row-wrap" style="gap:8px; align-items:center;">
                <span style="font-size:20px;">📈</span>
                <span style="font-family:var(--font-display);font-size:24px;font-weight:700;">${escapeHtml(a.pair)}</span>
                <span class="badge badge-gold">${escapeHtml(a.timeframe)}</span>
              </div>
              <div class="mono-lg" style="margin-top:4px;">${fmt(a.currentPrice, dec)}</div>
            </div>
            <span class="badge ${isLive ? 'badge-bull' : 'badge-amber'}">${isLive ? 'LIVE' : 'CLOSED'}</span>
          </div>
          <div class="field-grid-2">
            <button type="button" id="btn-analyze-pair" class="btn btn-outline">Analyze Pair</button>
            <button type="button" id="btn-pick-screenshot" class="btn btn-gold">Screenshot</button>
          </div>
        </div>
      </div>

      <div class="hscroll-row">
        ${presets.map((p) => `
          <button type="button" class="chip ${p.id === a.id ? 'selected' : ''}" data-preset-id="${escapeHtml(p.id)}">
            ${escapeHtml(p.pair)} · ${escapeHtml(p.timeframe)}
          </button>
        `).join('')}
      </div>

      <div class="notice-banner notice-gold">
        <span>✨</span>
        <div>
          <strong style="color:var(--gold);display:block;margin-bottom:2px;">Sample Preview Mode</strong>
          <span style="color:var(--text-secondary);">Displaying built-in sample technical analysis for instant offline/web use.</span>
        </div>
      </div>

      <div class="notice-banner ${isLive ? 'notice-bull' : 'notice-amber'}">
        <span>${isLive ? '🕒' : '⚠️'}</span>
        <div>
          <strong style="color:${isLive ? 'var(--bull)' : 'var(--amber)'};display:block;margin-bottom:2px;">${escapeHtml(a.marketStatusLabel)}</strong>
          <span style="color:var(--text-primary);opacity:0.9;">${escapeHtml(a.marketStatusNote)}</span>
        </div>
      </div>

      <div class="card">
        <div class="row-between">
          <span class="label-caps">Structure &amp; Key Zones</span>
          <div class="row-wrap" style="font-size:11px;color:var(--text-secondary);">
            <span><span style="color:var(--bull);">●</span> Support</span>
            <span><span style="color:var(--bear);">●</span> Resist</span>
            <span><span style="color:var(--gold);">●</span> Price</span>
          </div>
        </div>
        <div class="chart-canvas-wrap">
          <canvas id="price-action-canvas" class="chart-canvas"></canvas>
        </div>
      </div>

      <div>
        <h2 class="section-title">Buy &amp; Sell Signal Analysis</h2>
        <p class="section-sub">Signal bias &amp; trigger plans · Tap Plan to calculate position size</p>
      </div>

      <div class="card stack-sm" style="border-color:${a.trendBias === 'BULLISH' ? 'rgba(16,185,129,0.4)' : a.trendBias === 'BEARISH' ? 'rgba(239,68,68,0.4)' : 'var(--border)'};">
        <div class="row-between">
          <span class="label-caps">SIGNAL BIAS (${escapeHtml(a.pair)} · ${escapeHtml(a.timeframe)})</span>
          <span class="badge ${biasBadgeClass}">${biasLabel}</span>
        </div>
        <div style="font-size:13px;">${escapeHtml(a.signalSummary)}</div>
        <div class="row-wrap" style="font-size:12px;">
          ${nearestSup ? `<span class="mono-sm c-bull">SUP: ${fmt(nearestSup.low, dec)}–${fmt(nearestSup.high, dec)}</span>` : ''}
          ${nearestRes ? `<span class="mono-sm c-bear">RES: ${fmt(nearestRes.low, dec)}–${fmt(nearestRes.high, dec)}</span>` : ''}
        </div>
      </div>

      ${renderScenarioCardHtml(a.bullishScenario, dec, 'bull')}
      ${renderScenarioCardHtml(a.bearishScenario, dec, 'bear')}

      <div>
        <h2 class="section-title">Key Support &amp; Resistance Zones</h2>
        <p class="section-sub">Snap any zone midpoint directly to your trade planner</p>
      </div>

      ${a.keyLevels.map((z) => {
        const zColor = z.type === 'SUPPORT' ? 'var(--bull)' : z.type === 'RESISTANCE' ? 'var(--bear)' : z.type === 'IMBALANCE' ? 'var(--gold)' : 'var(--text-secondary)';
        const mid = (z.low + z.high) / 2;
        return `
          <div class="card stack-sm">
            <div class="row-between">
              <div>
                <div style="font-weight:700;">${escapeHtml(z.title)}</div>
                <div style="font-size:12px;color:${zColor};">${escapeHtml(z.role)}</div>
              </div>
              <span class="badge badge-muted" style="color:${zColor};">${fmt(z.low, dec)} – ${fmt(z.high, dec)}</span>
            </div>
            <div style="font-size:12px;color:var(--text-secondary);">${escapeHtml(z.note)}</div>
            <div class="snap-grid">
              <button type="button" class="chip" data-snap-price="${mid}" data-snap-target="ENTRY">Set Entry</button>
              <button type="button" class="chip" data-snap-price="${mid}" data-snap-target="STOP_LOSS">Set SL</button>
              <button type="button" class="chip" data-snap-price="${mid}" data-snap-target="TAKE_PROFIT">Set TP</button>
            </div>
          </div>
        `;
      }).join('')}

      <div>
        <h2 class="section-title">Price Action Breakdown</h2>
        <p class="section-sub">Sequential structure leading into current price</p>
      </div>

      ${a.priceActionPhases.map((ph) => {
        const col = ph.bias === 'BULLISH' ? 'var(--bull)' : ph.bias === 'BEARISH' ? 'var(--bear)' : 'var(--gold)';
        return `
          <div class="card stack-sm">
            <div class="row-between">
              <strong style="font-size:14px;">${escapeHtml(ph.title)}</strong>
              <span class="mono-sm" style="color:${col};">${escapeHtml(ph.rangeLabel)}</span>
            </div>
            <div style="font-size:12px;color:var(--text-secondary);">${escapeHtml(ph.summary)}</div>
          </div>
        `;
      }).join('')}

      <div>
        <h2 class="section-title">Trade Desk Q&amp;A</h2>
        <p class="section-sub">Ask about entries, stop placement, or spread conditions</p>
      </div>

      <div class="card stack-sm">
        <div class="hscroll-row">
          ${['Plan Buy Signal', 'Plan Sell Signal', 'Support & Resistance Levels', 'Where should Stop Loss go?', 'Check Rollover Spread Risk'].map((q) => `
            <button type="button" class="chip" data-quick-q="${escapeHtml(q)}">${escapeHtml(q)}</button>
          `).join('')}
        </div>
        <div class="stack-sm">
          ${state.messages.slice(-6).map((m) => `
            <div class="chat-bubble ${m.isUser ? 'chat-user' : 'chat-analyst'}">${escapeHtml(m.text)}</div>
          `).join('')}
        </div>
        <div style="display:flex;gap:8px;align-items:center;">
          <input id="analyst-q-input" class="input" type="text" placeholder="Ask about entry, SL, S/R, or rollover risk..." />
          <button type="button" id="btn-ask-analyst" class="btn btn-gold" style="min-width:56px;">Send</button>
        </div>
      </div>
    `;

    root.querySelector('#btn-analyze-pair').addEventListener('click', openAnalyzePairModal);
    root.querySelector('#btn-pick-screenshot').addEventListener('click', () => {
      document.getElementById('screenshot-file-input').click();
    });

    root.querySelectorAll('[data-preset-id]').forEach((btn) => {
      btn.addEventListener('click', () => selectPreset(btn.getAttribute('data-preset-id')));
    });

    root.querySelectorAll('[data-scenario-key]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const key = btn.getAttribute('data-scenario-key');
        const scen = key === 'bull' ? state.currentAnalysis.bullishScenario : state.currentAnalysis.bearishScenario;
        applyScenarioToPlanner(scen, state.currentAnalysis);
        switchTab('planner');
      });
    });

    root.querySelectorAll('[data-snap-target]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const price = parseFloat(btn.getAttribute('data-snap-price'));
        const target = btn.getAttribute('data-snap-target');
        snapPriceToPlanner(price, target);
      });
    });

    root.querySelectorAll('[data-quick-q]').forEach((btn) => {
      btn.addEventListener('click', () => askFollowUp(btn.getAttribute('data-quick-q')));
    });

    const qInput = root.querySelector('#analyst-q-input');
    root.querySelector('#btn-ask-analyst').addEventListener('click', () => {
      const val = qInput.value.trim();
      if (val) askFollowUp(val);
    });
    qInput.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') {
        const val = qInput.value.trim();
        if (val) askFollowUp(val);
      }
    });

    drawPriceActionCanvas();
  }

  function renderScenarioCardHtml(scen, dec, key) {
    const isBull = scen.bias === 'BULLISH';
    const stopDist = Math.max(Math.abs(scen.entryPrice - scen.stopLossPrice), 0.00001);
    const rr = Math.abs(scen.takeProfitPrice - scen.entryPrice) / stopDist;
    return `
      <div class="card stack-sm" style="border-color:${isBull ? 'rgba(16,185,129,0.45)' : 'rgba(239,68,68,0.45)'};">
        <div class="row-between">
          <strong style="font-size:15px;">${isBull ? '📈' : '📉'} ${escapeHtml(scen.title)}</strong>
          <span class="badge ${isBull ? 'badge-bull' : 'badge-bear'}">${isBull ? 'BUY' : 'SELL'} · 1:${fmt(rr, 1)}</span>
        </div>
        <div style="font-size:13px;color:var(--text-secondary);">${escapeHtml(scen.triggerCondition)}</div>
        <div class="metrics-strip">
          <div><div class="metric-label">Entry</div><div class="metric-val">${fmt(scen.entryPrice, dec)}</div></div>
          <div><div class="metric-label">Stop Loss</div><div class="metric-val c-bear">${fmt(scen.stopLossPrice, dec)}</div></div>
          <div><div class="metric-label">Target</div><div class="metric-val c-bull">${fmt(scen.takeProfitPrice, dec)}</div></div>
        </div>
        <div class="row-between">
          <span style="font-size:12px;color:var(--text-muted);flex:1;">${escapeHtml(scen.invalidationNote)}</span>
          <button type="button" class="btn btn-outline btn-sm" data-scenario-key="${key}">Plan</button>
        </div>
      </div>
    `;
  }

  function drawPriceActionCanvas() {
    const canvas = document.getElementById('price-action-canvas');
    if (!canvas) return;
    const dpr = window.devicePixelRatio || 1;
    const rect = canvas.getBoundingClientRect();
    const cssW = Math.max(rect.width || 320, 260);
    const cssH = 210;
    canvas.width = Math.round(cssW * dpr);
    canvas.height = Math.round(cssH * dpr);

    const ctx = canvas.getContext('2d');
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    ctx.clearRect(0, 0, cssW, cssH);

    const a = state.currentAnalysis;
    const points = a.chartPatternPoints;
    const allVals = [...points, a.currentPrice];
    a.keyLevels.forEach((z) => { allVals.push(z.low, z.high); });
    if (state.planner.pair === a.pair) {
      const e = parseFloat(state.planner.entryInput);
      const s = parseFloat(state.planner.stopLossInput);
      const t = parseFloat(state.planner.takeProfitInput);
      if (e > 0) allVals.push(e);
      if (s > 0) allVals.push(s);
      if (t > 0) allVals.push(t);
    }

    const rawMin = Math.min(...allVals);
    const rawMax = Math.max(...allVals);
    const pad = Math.max((rawMax - rawMin) * 0.12, 0.0001);
    const minP = rawMin - pad;
    const maxP = rawMax + pad;
    const span = Math.max(maxP - minP, 0.0001);

    const chartW = cssW * 0.78;
    const padY = 10;
    const innerH = cssH - padY * 2;
    const yFor = (p) => padY + innerH - Math.min(Math.max((p - minP) / span, 0), 1) * innerH;

    // Grid
    ctx.strokeStyle = 'rgba(38, 52, 73, 0.55)';
    ctx.lineWidth = 1;
    for (let i = 0; i <= 4; i++) {
      const y = padY + innerH * (i / 4);
      ctx.beginPath();
      ctx.moveTo(0, y);
      ctx.lineTo(cssW, y);
      ctx.stroke();
    }

    // Zones
    a.keyLevels.forEach((z) => {
      const yTop = yFor(z.high);
      const yBot = yFor(z.low);
      const h = Math.max(yBot - yTop, 5);
      const color = z.type === 'SUPPORT' ? '16, 185, 129' : z.type === 'RESISTANCE' ? '239, 68, 68' : z.type === 'IMBALANCE' ? '245, 183, 49' : '148, 163, 184';
      ctx.fillStyle = `rgba(${color}, 0.16)`;
      ctx.fillRect(0, yTop, chartW, h);
      ctx.strokeStyle = `rgba(${color}, 0.65)`;
      ctx.lineWidth = 1;
      ctx.beginPath();
      ctx.moveTo(0, (yTop + yBot) / 2);
      ctx.lineTo(chartW, (yTop + yBot) / 2);
      ctx.stroke();
    });

    // Planner guides
    if (state.planner.pair === a.pair) {
      const drawGuide = (val, color) => {
        if (!(val > 0)) return;
        const y = yFor(val);
        ctx.save();
        ctx.setLineDash([6, 5]);
        ctx.strokeStyle = color;
        ctx.lineWidth = 1.4;
        ctx.beginPath();
        ctx.moveTo(0, y);
        ctx.lineTo(chartW, y);
        ctx.stroke();
        ctx.restore();
      };
      drawGuide(parseFloat(state.planner.takeProfitInput), '#10B981');
      drawGuide(parseFloat(state.planner.stopLossInput), '#EF4444');
      drawGuide(parseFloat(state.planner.entryInput), 'rgba(248, 250, 252, 0.85)');
    }

    // Price Action Curve
    if (points.length >= 2) {
      const stepX = chartW / (points.length - 1);
      ctx.beginPath();
      points.forEach((v, idx) => {
        const x = idx * stepX;
        const y = yFor(v);
        if (idx === 0) ctx.moveTo(x, y);
        else ctx.lineTo(x, y);
      });
      ctx.strokeStyle = '#F5B731';
      ctx.lineWidth = 2.5;
      ctx.lineCap = 'round';
      ctx.lineJoin = 'round';
      ctx.stroke();

      ctx.lineTo(chartW, padY + innerH);
      ctx.lineTo(0, padY + innerH);
      ctx.closePath();
      const grad = ctx.createLinearGradient(0, padY, 0, padY + innerH);
      grad.addColorStop(0, 'rgba(245, 183, 49, 0.25)');
      grad.addColorStop(1, 'rgba(245, 183, 49, 0)');
      ctx.fillStyle = grad;
      ctx.fill();

      const lastY = yFor(a.currentPrice);
      ctx.save();
      ctx.setLineDash([4, 4]);
      ctx.strokeStyle = 'rgba(245, 183, 49, 0.55)';
      ctx.lineWidth = 1;
      ctx.beginPath();
      ctx.moveTo(0, lastY);
      ctx.lineTo(cssW, lastY);
      ctx.stroke();
      ctx.restore();

      ctx.fillStyle = 'rgba(245, 183, 49, 0.28)';
      ctx.beginPath();
      ctx.arc(chartW, lastY, 8, 0, Math.PI * 2);
      ctx.fill();
      ctx.fillStyle = '#F5B731';
      ctx.beginPath();
      ctx.arc(chartW, lastY, 4, 0, Math.PI * 2);
      ctx.fill();
    }

    // Right price labels
    ctx.font = '600 11px "JetBrains Mono", monospace';
    ctx.textAlign = 'right';
    ctx.fillStyle = '#94A3B8';
    ctx.fillText(fmt(maxP, a.priceDecimals), cssW - 4, 16);
    ctx.fillText(fmt(minP, a.priceDecimals), cssW - 4, cssH - 6);
    ctx.fillStyle = '#F5B731';
    ctx.fillText(fmt(a.currentPrice, a.priceDecimals), cssW - 4, Math.min(Math.max(yFor(a.currentPrice) - 4, 28), cssH - 18));
  }

  function selectPreset(presetId) {
    const found = presets.find((p) => p.id === presetId);
    if (!found) return;
    state.currentAnalysis = structuredClone(found);
    applyScenarioToPlanner(found.bullishScenario, found);
    state.messages = [
      {
        id: 'preset_' + found.id,
        isUser: false,
        text: `[Sample Preview] Switched workspace to ${found.pair} ${found.timeframe} at ${fmt(found.currentPrice, found.priceDecimals)}. ${found.marketStatusNote}`
      }
    ];
    renderAnalysis();
  }

  function openAnalyzePairModal() {
    const a = state.currentAnalysis;
    openModal('Analyze Pair or Chart', `
      <p class="section-sub">Enter pair &amp; timeframe to run structured technical analysis.</p>
      <div class="field">
        <label for="scan-pair">Instrument / Pair</label>
        <input id="scan-pair" class="input" type="text" value="${escapeHtml(a.pair)}" />
      </div>
      <div class="field">
        <label for="scan-tf">Timeframe (e.g., M15, H1, H4)</label>
        <input id="scan-tf" class="input" type="text" value="${escapeHtml(a.timeframe)}" />
      </div>
      <div class="field">
        <label for="scan-note">Focus / Question (optional)</label>
        <textarea id="scan-note" class="textarea" placeholder="e.g., Check London open breakout levels"></textarea>
      </div>
      <div class="field-grid-2" style="margin-top:4px;">
        <button type="button" id="scan-cancel" class="btn btn-outline">Cancel</button>
        <button type="button" id="scan-run" class="btn btn-gold">Run Analysis</button>
      </div>
    `, (body) => {
      body.querySelector('#scan-cancel').addEventListener('click', closeModal);
      body.querySelector('#scan-run').addEventListener('click', () => {
        const pair = body.querySelector('#scan-pair').value.trim().toUpperCase() || 'XAUUSD';
        const tf = body.querySelector('#scan-tf').value.trim().toUpperCase() || 'M15';
        const note = body.querySelector('#scan-note').value.trim();
        runCustomAnalysis(null, pair, tf, note);
        closeModal();
      });
    });
  }

  function runCustomAnalysis(imageUri, pairHint, tfHint, note) {
    let base = presets[0];
    if (pairHint.includes('GBP')) base = presets[2];
    else if (pairHint.includes('JPY')) base = presets[3];
    else if (/EUR|AUD|NZD|CAD|CHF/.test(pairHint)) base = presets[1];
    else if (/BTC|ETH/.test(pairHint)) base = presets[4];

    const copy = structuredClone(base);
    copy.id = 'custom_' + Date.now();
    copy.pair = pairHint;
    copy.timeframe = tfHint;
    copy.imageUri = imageUri || null;
    if (note) copy.marketStatusNote = `${copy.marketStatusNote} · Focus: ${note}`;

    state.currentAnalysis = copy;
    applyScenarioToPlanner(copy.bullishScenario, copy);
    state.messages.push({
      id: 'scan_' + Date.now(),
      isUser: false,
      text: `[Sample Preview] Analyzed ${copy.pair} (${copy.timeframe}) chart preview. Watch ${copy.keyLevels[0]?.title || 'key zones'}.`
    });
    renderAnalysis();
  }

  document.getElementById('screenshot-file-input').addEventListener('change', (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      const a = state.currentAnalysis;
      runCustomAnalysis(String(reader.result || ''), a.pair, a.timeframe, 'Uploaded chart screenshot');
    };
    reader.readAsDataURL(file);
    e.target.value = '';
  });

  function askFollowUp(question) {
    state.messages.push({ id: 'u_' + Date.now(), isUser: true, text: question });
    const a = state.currentAnalysis;
    const dec = a.priceDecimals;
    const q = question.toLowerCase();
    const sup = a.keyLevels.find((z) => z.type === 'SUPPORT');
    const res = a.keyLevels.find((z) => z.type === 'RESISTANCE');

    let reply;
    if (/long|buy|entry/.test(q)) {
      const b = a.bullishScenario;
      reply = `[Sample Preview] For a ${a.pair} (${a.timeframe}) BUY signal: ${b.triggerCondition} Entry: ${fmt(b.entryPrice, dec)}, Stop Loss: ${fmt(b.stopLossPrice, dec)}, Take Profit: ${fmt(b.takeProfitPrice, dec)}. Invalidation: ${b.invalidationNote}`;
    } else if (/short|sell|bear/.test(q)) {
      const s = a.bearishScenario;
      reply = `[Sample Preview] For a ${a.pair} (${a.timeframe}) SELL signal: ${s.triggerCondition} Entry: ${fmt(s.entryPrice, dec)}, Stop Loss: ${fmt(s.stopLossPrice, dec)}, Take Profit: ${fmt(s.takeProfitPrice, dec)}. Invalidation: ${s.invalidationNote}`;
    } else if (/support|resistance|level|zone/.test(q)) {
      const supTxt = sup ? `${sup.title} (${fmt(sup.low, dec)}–${fmt(sup.high, dec)})` : 'key support';
      const resTxt = res ? `${res.title} (${fmt(res.low, dec)}–${fmt(res.high, dec)})` : 'overhead resistance';
      reply = `[Sample Preview] ${a.pair} (${a.timeframe}) key S/R map: Support sits at ${supTxt} while Resistance caps near ${resTxt}. Tap any Set Entry / SL / TP button above to snap levels directly to the Planner.`;
    } else if (/stop|sl|risk/.test(q)) {
      reply = `[Sample Preview] Keep stops outside support reaction wicks for longs or above resistance for shorts so spread noise does not tag you out. Aim for >= 1:1.5 R:R in the Planner tab.`;
    } else if (/rollover|spread|close|open/.test(q)) {
      reply = `[Sample Preview] ${a.marketStatusNote} Avoid market orders during the first 10–15 minutes of market reopen.`;
    } else {
      reply = `[Sample Preview] ${a.pair} (${a.timeframe}) bias is ${a.trendBias} at ${fmt(a.currentPrice, dec)}. Watch whether buyers defend ${sup?.title || 'support'} or sellers break below it.`;
    }

    state.messages.push({ id: 'a_' + Date.now(), isUser: false, text: reply });
    renderAnalysis();
  }

  function applyScenarioToPlanner(scen, analysis) {
    const dec = analysis.priceDecimals;
    state.planner.pair = analysis.pair;
    state.planner.timeframe = analysis.timeframe;
    state.planner.direction = scen.bias === 'BEARISH' ? 'SHORT' : 'LONG';
    state.planner.isCustomLotMode = false;
    state.planner.entryInput = fmt(scen.entryPrice, dec);
    state.planner.stopLossInput = fmt(scen.stopLossPrice, dec);
    state.planner.takeProfitInput = fmt(scen.takeProfitPrice, dec);
    state.planner.contractSpec = inferContractSpec(analysis.pair);
    state.planner.notesInput = `${scen.title}: ${scen.triggerCondition}`;
    state.planner.calculationStatusMessage = null;
    syncPlannerComputed();
  }

  function snapPriceToPlanner(price, target) {
    const a = state.currentAnalysis;
    const dec = a.priceDecimals;
    if (state.planner.pair !== a.pair) {
      state.planner.pair = a.pair;
      state.planner.timeframe = a.timeframe;
      state.planner.contractSpec = inferContractSpec(a.pair);
    }
    const formatted = fmt(price, dec);
    if (target === 'ENTRY') state.planner.entryInput = formatted;
    else if (target === 'STOP_LOSS') state.planner.stopLossInput = formatted;
    else if (target === 'TAKE_PROFIT') state.planner.takeProfitInput = formatted;
    state.planner.calculationStatusMessage = null;
    syncPlannerComputed();
    switchTab('planner');
  }

  /* ======================== PLANNER TAB ======================== */
  function renderPlannerTelemetryOnly() {
    const card = document.getElementById('planner-telemetry-card');
    if (!card) return;
    const p = state.planner;
    const m = calculateMetrics(p);
    const spec = CONTRACT_SPECS[p.contractSpec] || CONTRACT_SPECS.GOLD_XAUUSD;
    const dec = spec.priceDecimals;
    const isLong = p.direction === 'LONG';

    let distSummary = '--';
    if (m.isValid) {
      if (spec.id === 'FX_STANDARD') distSummary = `${fmt(m.stopDist * 10000, 0)}/${fmt(m.targetDist * 10000, 0)}p`;
      else if (spec.id === 'FX_JPY') distSummary = `${fmt(m.stopDist * 100, 0)}/${fmt(m.targetDist * 100, 0)}p`;
      else distSummary = `${fmt(m.stopDist, dec)}/${fmt(m.targetDist, dec)}`;
    }

    card.innerHTML = `
      <div class="row-between">
        <div>
          <div style="font-weight:700;font-size:16px;">${escapeHtml(p.pair || 'CUSTOM')} · ${escapeHtml(p.timeframe || 'M15')}</div>
          <div style="font-size:12px;color:${m.isValid ? 'var(--text-secondary)' : 'var(--bear)'};">
            ${escapeHtml(m.isValid ? 'Position Size & Risk Live' : (m.hint || 'Check Levels'))}
          </div>
        </div>
        <span class="badge ${isLong ? 'badge-bull' : 'badge-bear'}">${isLong ? 'BUY' : 'SELL'}</span>
      </div>

      <div class="row-between" style="margin-top:10px;">
        <div>
          <div class="metric-label">Risk : Reward</div>
          <div class="mono-lg c-gold">${m.isValid ? `1 : ${fmt(m.rr, 2)}` : '--'}</div>
        </div>
        <div style="text-align:right;">
          <div class="metric-label">Lot Size</div>
          <div class="mono-lg">${m.isValid ? `${fmt(m.lots, 2)} lots` : '--'}</div>
        </div>
      </div>

      <div class="metrics-strip" style="margin-top:10px;">
        <div>
          <div class="metric-label">Risk ($)</div>
          <div class="metric-val c-bear">${m.isValid ? `-$${fmt(m.riskUsd, 2)}` : '--'}</div>
        </div>
        <div>
          <div class="metric-label">Profit ($)</div>
          <div class="metric-val c-bull">${m.isValid ? `+$${fmt(m.rewardUsd, 2)}` : '--'}</div>
        </div>
        <div>
          <div class="metric-label">${spec.id.startsWith('FX_') ? 'SL/TP Pips' : 'SL/TP Dist'}</div>
          <div class="metric-val">${distSummary}</div>
        </div>
      </div>

      ${p.calculationStatusMessage ? `
        <div class="notice-banner ${m.isValid ? 'notice-gold' : 'notice-bear'}" style="margin-top:10px;">
          <span style="color:${m.isValid ? 'var(--gold)' : 'var(--bear)'};font-weight:600;">${escapeHtml(p.calculationStatusMessage)}</span>
        </div>
      ` : ''}
    `;
  }

  function renderPlanner() {
    syncPlannerComputed();
    const p = state.planner;
    const m = calculateMetrics(p);
    const rrHealthy = m.isValid && m.rr >= 1.5;

    const root = document.getElementById('planner-root');
    root.innerHTML = `
      <div id="planner-telemetry-card" class="card"></div>

      <div class="card stack-sm">
        <span class="label-caps">Position &amp; Risk Calculator</span>

        <div class="dir-grid">
          <button type="button" id="dir-long-btn" class="dir-btn ${p.direction === 'LONG' ? 'buy-active' : ''}">BUY (LONG)</button>
          <button type="button" id="dir-short-btn" class="dir-btn ${p.direction === 'SHORT' ? 'sell-active' : ''}">SELL (SHORT)</button>
        </div>

        <div class="field">
          <label for="pl-entry">Entry Price</label>
          <input id="pl-entry" class="input mono" type="text" inputmode="decimal" autocomplete="off" value="${escapeHtml(p.entryInput)}" />
        </div>

        <div class="field-grid-2">
          <div class="field">
            <label for="pl-sl">Stop Loss</label>
            <input id="pl-sl" class="input mono" type="text" inputmode="decimal" autocomplete="off" value="${escapeHtml(p.stopLossInput)}" />
          </div>
          <div class="field">
            <label for="pl-tp">Take Profit</label>
            <input id="pl-tp" class="input mono" type="text" inputmode="decimal" autocomplete="off" value="${escapeHtml(p.takeProfitInput)}" />
          </div>
        </div>

        <div class="field-grid-2">
          <div class="field">
            <label for="pl-balance">Account Balance ($)</label>
            <input id="pl-balance" class="input mono" type="text" inputmode="decimal" autocomplete="off" value="${escapeHtml(p.balanceInput)}" />
          </div>
          <div class="field">
            <label for="pl-risk">Risk Percentage (%)</label>
            <input id="pl-risk" class="input mono" type="text" inputmode="decimal" autocomplete="off" value="${escapeHtml(p.riskPercentInput)}" />
          </div>
        </div>

        <div class="field-grid-3">
          <div class="field">
            <label for="pl-lots">Lot Size</label>
            <input id="pl-lots" class="input mono" type="text" inputmode="decimal" autocomplete="off" value="${escapeHtml(p.lotSizeInput)}" />
          </div>
          <div class="field">
            <label for="pl-pair">Pair / Symbol</label>
            <input id="pl-pair" class="input" type="text" autocomplete="off" value="${escapeHtml(p.pair)}" />
          </div>
          <div class="field">
            <label for="pl-tf">TF</label>
            <input id="pl-tf" class="input" type="text" autocomplete="off" value="${escapeHtml(p.timeframe)}" />
          </div>
        </div>

        <button type="button" id="btn-calculate-pos" class="btn btn-outline-gold btn-full">🧮 Calculate</button>

        <div class="hscroll-row">
          <span class="mono-sm c-muted">Risk:</span>
          ${['0.5', '1.0', '2.0', '3.0'].map((r) => `
            <button type="button" class="chip ${!p.isCustomLotMode && p.riskPercentInput === r ? 'selected' : ''}" data-quick-risk="${r}">${r}%</button>
          `).join('')}
          <span class="mono-sm c-muted">TP:</span>
          ${[1.5, 2.0, 3.0].map((rr) => `
            <button type="button" class="chip" data-auto-rr="${rr}">1:${rr.toFixed(1)}</button>
          `).join('')}
        </div>
      </div>

      <div class="card stack-sm">
        <div class="hscroll-row">
          <span class="mono-sm c-muted">Signals:</span>
          <button type="button" class="chip" data-load-sig="bull">${escapeHtml(state.currentAnalysis.pair)} Buy Signal</button>
          <button type="button" class="chip" data-load-sig="bear">${escapeHtml(state.currentAnalysis.pair)} Sell Signal</button>
        </div>

        <div class="hscroll-row">
          <span class="mono-sm c-muted">Pairs:</span>
          ${presets.map((pr) => `
            <button type="button" class="chip ${p.pair === pr.pair ? 'selected' : ''}" data-pl-preset="${escapeHtml(pr.id)}">${escapeHtml(pr.pair)}</button>
          `).join('')}
        </div>

        <div class="hscroll-row">
          ${Object.values(CONTRACT_SPECS).map((sp) => `
            <button type="button" class="chip ${p.contractSpec === sp.id ? 'selected' : ''}" data-spec="${sp.id}">${escapeHtml(sp.displayName)}</button>
          `).join('')}
        </div>

        <div class="field">
          <label for="pl-notes">Execution Trigger &amp; Notes</label>
          <textarea id="pl-notes" class="textarea">${escapeHtml(p.notesInput)}</textarea>
        </div>
      </div>

      <div class="card stack-sm">
        <span class="label-caps">Pre-Entry Discipline Checklist</span>
        <label class="check-row">
          <input id="chk-spread" type="checkbox" ${p.spreadNormalized ? 'checked' : ''} />
          <span>Spreads normalized after rollover / market open</span>
        </label>
        <label class="check-row">
          <input id="chk-candle" type="checkbox" ${p.candleConfirmed ? 'checked' : ''} />
          <span>${escapeHtml(p.timeframe || 'M15')} confirmation candle closed at key level</span>
        </label>
        <div id="rr-health-row" style="font-size:12px;color:${rrHealthy ? 'var(--bull)' : 'var(--text-secondary)'};">
          ${rrHealthy ? `✓ Reward-to-Risk is >= 1.50R (${fmt(m.rr, 2)}R)` : 'ⓘ Aim for >= 1.50R reward-to-risk before committing capital'}
        </div>
      </div>

      <button type="button" id="btn-save-planner" class="btn btn-gold btn-full">🔖 Save Setup to Journal</button>
    `;

    renderPlannerTelemetryOnly();

    const entryEl = root.querySelector('#pl-entry');
    const slEl = root.querySelector('#pl-sl');
    const tpEl = root.querySelector('#pl-tp');
    const balEl = root.querySelector('#pl-balance');
    const riskEl = root.querySelector('#pl-risk');
    const lotsEl = root.querySelector('#pl-lots');
    const pairEl = root.querySelector('#pl-pair');
    const tfEl = root.querySelector('#pl-tf');
    const notesEl = root.querySelector('#pl-notes');

    function refreshAfterFieldEdit(editedMode) {
      state.planner.calculationStatusMessage = null;
      const mNow = syncPlannerComputed();
      if (editedMode === 'lots') {
        if (mNow.isValid && (parseFloat(state.planner.balanceInput) || 0) > 0) {
          riskEl.value = state.planner.riskPercentInput;
        }
      } else {
        if (mNow.isValid) {
          lotsEl.value = state.planner.lotSizeInput;
        }
      }
      renderPlannerTelemetryOnly();
      const rrRow = document.getElementById('rr-health-row');
      if (rrRow) {
        const ok = mNow.isValid && mNow.rr >= 1.5;
        rrRow.style.color = ok ? 'var(--bull)' : 'var(--text-secondary)';
        rrRow.textContent = ok ? `✓ Reward-to-Risk is >= 1.50R (${fmt(mNow.rr, 2)}R)` : 'ⓘ Aim for >= 1.50R reward-to-risk before committing capital';
      }
    }

    entryEl.addEventListener('input', () => {
      const f = filterNumeric(entryEl.value);
      if (entryEl.value !== f) entryEl.value = f;
      state.planner.entryInput = f;
      refreshAfterFieldEdit('risk');
    });
    slEl.addEventListener('input', () => {
      const f = filterNumeric(slEl.value);
      if (slEl.value !== f) slEl.value = f;
      state.planner.stopLossInput = f;
      refreshAfterFieldEdit('risk');
    });
    tpEl.addEventListener('input', () => {
      const f = filterNumeric(tpEl.value);
      if (tpEl.value !== f) tpEl.value = f;
      state.planner.takeProfitInput = f;
      refreshAfterFieldEdit('risk');
    });
    balEl.addEventListener('input', () => {
      const f = filterNumeric(balEl.value);
      if (balEl.value !== f) balEl.value = f;
      state.planner.balanceInput = f;
      refreshAfterFieldEdit(state.planner.isCustomLotMode ? 'lots' : 'risk');
    });
    riskEl.addEventListener('input', () => {
      const f = filterNumeric(riskEl.value);
      if (riskEl.value !== f) riskEl.value = f;
      state.planner.isCustomLotMode = false;
      state.planner.riskPercentInput = f;
      refreshAfterFieldEdit('risk');
    });
    lotsEl.addEventListener('input', () => {
      const f = filterNumeric(lotsEl.value);
      if (lotsEl.value !== f) lotsEl.value = f;
      state.planner.isCustomLotMode = true;
      state.planner.lotSizeInput = f;
      refreshAfterFieldEdit('lots');
    });
    pairEl.addEventListener('input', () => {
      state.planner.pair = pairEl.value.toUpperCase().slice(0, 12);
      state.planner.contractSpec = inferContractSpec(state.planner.pair);
      refreshAfterFieldEdit('risk');
    });
    tfEl.addEventListener('input', () => {
      state.planner.timeframe = tfEl.value.toUpperCase().slice(0, 6);
      renderPlannerTelemetryOnly();
    });
    notesEl.addEventListener('input', () => {
      state.planner.notesInput = notesEl.value;
    });

    root.querySelector('#dir-long-btn').addEventListener('click', () => {
      state.planner.direction = 'LONG';
      renderPlanner();
    });
    root.querySelector('#dir-short-btn').addEventListener('click', () => {
      state.planner.direction = 'SHORT';
      renderPlanner();
    });

    root.querySelector('#btn-calculate-pos').addEventListener('click', () => {
      const mNow = syncPlannerComputed();
      if (mNow.isValid) {
        state.planner.calculationStatusMessage = `${fmt(mNow.lots, 2)} lots · Risk $${fmt(mNow.riskUsd, 2)} (${fmt(mNow.impliedRiskPct, 2)}%) · Profit +$${fmt(mNow.rewardUsd, 2)} · 1:${fmt(mNow.rr, 2)} R:R`;
      } else {
        state.planner.calculationStatusMessage = mNow.hint;
      }
      renderPlanner();
    });

    root.querySelectorAll('[data-quick-risk]').forEach((btn) => {
      btn.addEventListener('click', () => {
        state.planner.isCustomLotMode = false;
        state.planner.riskPercentInput = btn.getAttribute('data-quick-risk');
        state.planner.calculationStatusMessage = null;
        renderPlanner();
      });
    });

    root.querySelectorAll('[data-auto-rr]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const targetRR = parseFloat(btn.getAttribute('data-auto-rr'));
        const entry = parseFloat(state.planner.entryInput) || 0;
        const sl = parseFloat(state.planner.stopLossInput) || 0;
        const isLong = state.planner.direction === 'LONG';
        if (entry <= 0 || sl <= 0 || (isLong ? sl >= entry : sl <= entry)) {
          state.planner.calculationStatusMessage = 'Set valid Entry Price and Stop Loss first to compute Take Profit.';
          renderPlannerTelemetryOnly();
          return;
        }
        const stopDist = Math.abs(entry - sl);
        const tp = isLong ? entry + stopDist * targetRR : Math.max(entry - stopDist * targetRR, 0.0001);
        const dec = CONTRACT_SPECS[state.planner.contractSpec].priceDecimals;
        state.planner.takeProfitInput = fmt(tp, dec);
        state.planner.calculationStatusMessage = null;
        renderPlanner();
      });
    });

    root.querySelectorAll('[data-load-sig]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const key = btn.getAttribute('data-load-sig');
        const scen = key === 'bull' ? state.currentAnalysis.bullishScenario : state.currentAnalysis.bearishScenario;
        applyScenarioToPlanner(scen, state.currentAnalysis);
        renderPlanner();
      });
    });

    root.querySelectorAll('[data-pl-preset]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const chosen = presets.find((pr) => pr.id === btn.getAttribute('data-pl-preset'));
        if (!chosen) return;
        applyScenarioToPlanner(chosen.bullishScenario, chosen);
        renderPlanner();
      });
    });

    root.querySelectorAll('[data-spec]').forEach((btn) => {
      btn.addEventListener('click', () => {
        state.planner.contractSpec = btn.getAttribute('data-spec');
        state.planner.calculationStatusMessage = null;
        renderPlanner();
      });
    });

    root.querySelector('#chk-spread').addEventListener('change', (e) => {
      state.planner.spreadNormalized = e.target.checked;
    });
    root.querySelector('#chk-candle').addEventListener('change', (e) => {
      state.planner.candleConfirmed = e.target.checked;
    });

    root.querySelector('#btn-save-planner').addEventListener('click', () => {
      const mNow = syncPlannerComputed();
      if (!mNow.isValid) {
        state.planner.calculationStatusMessage = mNow.hint;
        renderPlannerTelemetryOnly();
        return;
      }
      const now = Date.now();
      state.journal.unshift({
        id: now,
        pair: state.planner.pair.trim().toUpperCase() || 'XAUUSD',
        timeframe: state.planner.timeframe.trim().toUpperCase() || 'M15',
        direction: state.planner.direction === 'SHORT' ? 'SELL' : 'BUY',
        entryPrice: parseFloat(state.planner.entryInput),
        stopLoss: parseFloat(state.planner.stopLossInput),
        takeProfit: parseFloat(state.planner.takeProfitInput),
        accountBalance: parseFloat(state.planner.balanceInput) || 10000,
        riskPercent: mNow.impliedRiskPct,
        lotSize: state.planner.isCustomLotMode ? (parseFloat(state.planner.lotSizeInput) || mNow.lots) : mNow.lots,
        riskRewardRatio: mNow.rr,
        profitLossUsd: null,
        status: 'PLANNED',
        notes: state.planner.notesInput.trim(),
        createdAtMillis: now
      });
      persistJournal();
      state.journalFilter = 'ALL';
      switchTab('journal');
    });
  }

  /* ======================== JOURNAL TAB ======================== */
  function renderJournal() {
    const all = state.journal;
    const filtered = all.filter((item) => {
      if (state.journalFilter === 'ALL') return true;
      if (state.journalFilter === 'PLANNED') return item.status === 'PLANNED';
      if (state.journalFilter === 'ACTIVE') return item.status === 'ACTIVE';
      return item.status === 'WON' || item.status === 'LOST';
    });

    const activeCount = all.filter((t) => t.status === 'ACTIVE').length;
    const wonCount = all.filter((t) => t.status === 'WON').length;
    const closedCount = all.filter((t) => t.status === 'WON' || t.status === 'LOST').length;
    const winPct = closedCount > 0 ? `${Math.round((wonCount * 100) / closedCount)}%` : '--';
    const pnlEntries = all.map((t) => t.profitLossUsd).filter((v) => typeof v === 'number' && Number.isFinite(v));
    const pnlSum = pnlEntries.reduce((acc, v) => acc + v, 0);
    const netPnlText = pnlEntries.length > 0 ? `${pnlSum >= 0 ? '+' : '-'}$${fmt(Math.abs(pnlSum), 2)}` : '--';
    const netPnlColor = pnlEntries.length === 0 ? 'var(--text-primary)' : pnlSum >= 0 ? 'var(--bull)' : 'var(--bear)';
    const avgRr = all.length > 0 ? `1:${fmt(all.reduce((acc, t) => acc + (t.riskRewardRatio || 0), 0) / all.length, 1)}` : '--';

    const root = document.getElementById('journal-root');
    root.innerHTML = `
      <div class="card journal-stats">
        <div><div class="metric-label">Trades</div><div class="metric-val">${all.length}</div></div>
        <div><div class="metric-label">Active</div><div class="metric-val c-gold">${activeCount}</div></div>
        <div><div class="metric-label">Net P&amp;L</div><div class="metric-val" style="color:${netPnlColor};">${netPnlText}</div></div>
        <div><div class="metric-label">Avg R:R</div><div class="metric-val c-gold">${avgRr}</div></div>
        <div><div class="metric-label">Win %</div><div class="metric-val">${winPct}</div></div>
      </div>

      <button type="button" id="btn-open-add-trade" class="btn btn-gold btn-full">+ Add Trade</button>

      <div class="stack-sm">
        <span class="label-caps">Journal History</span>
        <div class="hscroll-row">
          ${[
            { id: 'ALL', label: 'All' },
            { id: 'PLANNED', label: 'Planned' },
            { id: 'ACTIVE', label: 'Active' },
            { id: 'CLOSED', label: 'Closed' }
          ].map((f) => `
            <button type="button" class="chip ${state.journalFilter === f.id ? 'selected' : ''}" data-jfilter="${f.id}">${f.label}</button>
          `).join('')}
        </div>
      </div>

      ${filtered.length === 0 ? `
        <div class="card" style="text-align:center;padding:28px 18px;">
          <div style="font-size:28px;">📒</div>
          <div style="font-weight:700;margin-top:6px;">No trades in this view</div>
          <div class="section-sub">Tap Add Trade above to log a trade directly, or save a plan from the Planner tab.</div>
        </div>
      ` : filtered.map((t) => {
        const isBuy = t.direction === 'BUY' || t.direction === 'LONG';
        const dec = CONTRACT_SPECS[inferContractSpec(t.pair)].priceDecimals;
        const hasPnl = typeof t.profitLossUsd === 'number' && Number.isFinite(t.profitLossUsd);
        const pnlStr = hasPnl ? `${t.profitLossUsd >= 0 ? '+' : '-'}$${fmt(Math.abs(t.profitLossUsd), 2)}` : 'Open / Planned';
        const pnlColor = !hasPnl ? 'var(--text-secondary)' : t.profitLossUsd >= 0 ? 'var(--bull)' : 'var(--bear)';

        return `
          <div class="card stack-sm">
            <div class="row-between">
              <div class="row-wrap" style="gap:8px;">
                <strong style="font-size:16px;">${escapeHtml(t.pair)} · ${escapeHtml(t.timeframe)}</strong>
                <span class="badge ${isBuy ? 'badge-bull' : 'badge-bear'}">${isBuy ? 'BUY' : 'SELL'}</span>
              </div>
              <span class="mono-sm c-gold">1:${fmt(t.riskRewardRatio || 0, 2)} · ${fmt(t.lotSize, 2)} lots</span>
            </div>

            <div class="metrics-strip">
              <div><div class="metric-label">Entry</div><div class="metric-val">${fmt(t.entryPrice, dec)}</div></div>
              <div><div class="metric-label">Stop Loss</div><div class="metric-val c-bear">${fmt(t.stopLoss, dec)}</div></div>
              <div><div class="metric-label">Take Profit</div><div class="metric-val c-bull">${fmt(t.takeProfit, dec)}</div></div>
            </div>

            <div class="row-between" style="background:rgba(22,32,51,0.7);padding:8px 12px;border-radius:10px;">
              <span class="metric-label">Profit / Loss</span>
              <span class="metric-val" style="color:${pnlColor};">${pnlStr}</span>
            </div>

            ${t.notes ? `<div style="font-size:13px;color:var(--text-secondary);">${escapeHtml(t.notes)}</div>` : ''}

            <div class="hscroll-row">
              ${['PLANNED', 'ACTIVE', 'WON', 'LOST'].map((st) => {
                const active = t.status === st;
                const cls = active ? (st === 'WON' ? 'bull-selected' : st === 'LOST' ? 'bear-selected' : 'selected') : '';
                return `<button type="button" class="chip ${cls}" data-set-status="${st}" data-tid="${t.id}">${st}</button>`;
              }).join('')}
            </div>

            <div class="row-between">
              <button type="button" class="btn btn-outline btn-sm" data-load-planner="${t.id}">Load in Planner</button>
              <div class="row-wrap" style="gap:6px;">
                <button type="button" class="icon-btn" data-edit-notes="${t.id}" aria-label="Edit notes">✏️</button>
                <button type="button" class="icon-btn" data-delete-trade="${t.id}" aria-label="Delete trade">🗑️</button>
              </div>
            </div>
          </div>
        `;
      }).join('')}
    `;

    root.querySelector('#btn-open-add-trade').addEventListener('click', openAddTradeModal);

    root.querySelectorAll('[data-jfilter]').forEach((btn) => {
      btn.addEventListener('click', () => {
        state.journalFilter = btn.getAttribute('data-jfilter');
        renderJournal();
      });
    });

    root.querySelectorAll('[data-set-status]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const id = Number(btn.getAttribute('data-tid'));
        const st = btn.getAttribute('data-set-status');
        state.journal = state.journal.map((t) => (t.id === id ? { ...t, status: st } : t));
        persistJournal();
        renderJournal();
      });
    });

    root.querySelectorAll('[data-load-planner]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const id = Number(btn.getAttribute('data-load-planner'));
        const t = state.journal.find((item) => item.id === id);
        if (!t) return;
        const specId = inferContractSpec(t.pair);
        const dec = CONTRACT_SPECS[specId].priceDecimals;
        state.planner.pair = t.pair;
        state.planner.timeframe = t.timeframe;
        state.planner.direction = (t.direction === 'SELL' || t.direction === 'SHORT') ? 'SHORT' : 'LONG';
        state.planner.balanceInput = fmt(t.accountBalance || 10000, 0);
        state.planner.riskPercentInput = fmt(t.riskPercent || 1.0, 1);
        state.planner.lotSizeInput = fmt(t.lotSize || 0.1, 2);
        state.planner.isCustomLotMode = false;
        state.planner.entryInput = fmt(t.entryPrice, dec);
        state.planner.stopLossInput = fmt(t.stopLoss, dec);
        state.planner.takeProfitInput = fmt(t.takeProfit, dec);
        state.planner.contractSpec = specId;
        state.planner.notesInput = t.notes || '';
        state.planner.calculationStatusMessage = null;
        switchTab('planner');
      });
    });

    root.querySelectorAll('[data-edit-notes]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const id = Number(btn.getAttribute('data-edit-notes'));
        const t = state.journal.find((item) => item.id === id);
        if (!t) return;
        openModal(`Edit Notes · ${t.pair}`, `
          <div class="field">
            <label for="edit-notes-box">Trade Execution &amp; Review Notes</label>
            <textarea id="edit-notes-box" class="textarea" style="min-height:110px;">${escapeHtml(t.notes || '')}</textarea>
          </div>
          <div class="field-grid-2">
            <button type="button" id="notes-cancel" class="btn btn-outline">Cancel</button>
            <button type="button" id="notes-save" class="btn btn-gold">Save</button>
          </div>
        `, (body) => {
          body.querySelector('#notes-cancel').addEventListener('click', closeModal);
          body.querySelector('#notes-save').addEventListener('click', () => {
            const nextNotes = body.querySelector('#edit-notes-box').value.trim();
            state.journal = state.journal.map((item) => (item.id === id ? { ...item, notes: nextNotes } : item));
            persistJournal();
            closeModal();
            renderJournal();
          });
        });
      });
    });

    root.querySelectorAll('[data-delete-trade]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const id = Number(btn.getAttribute('data-delete-trade'));
        state.journal = state.journal.filter((t) => t.id !== id);
        persistJournal();
        renderJournal();
      });
    });
  }

  function openAddTradeModal() {
    let dir = 'BUY';
    let status = 'ACTIVE';

    openModal('Add Trade to Journal', `
      <div class="hscroll-row">
        ${['XAUUSD', 'EURUSD', 'GBPUSD', 'USDJPY'].map((sym) => `
          <button type="button" class="chip" data-quick-sym="${sym}">${sym}</button>
        `).join('')}
      </div>

      <div class="field-grid-2">
        <div class="field">
          <label for="jt-symbol">Symbol</label>
          <input id="jt-symbol" class="input" type="text" value="XAUUSD" />
        </div>
        <div class="field">
          <label for="jt-tf">TF</label>
          <input id="jt-tf" class="input" type="text" value="M15" />
        </div>
      </div>

      <div class="dir-grid">
        <button type="button" id="jt-buy" class="dir-btn buy-active">BUY</button>
        <button type="button" id="jt-sell" class="dir-btn">SELL</button>
      </div>

      <div class="field-grid-2">
        <div class="field">
          <label for="jt-entry">Entry Price</label>
          <input id="jt-entry" class="input mono" type="text" inputmode="decimal" value="3635.00" />
        </div>
        <div class="field">
          <label for="jt-lots">Lot Size</label>
          <input id="jt-lots" class="input mono" type="text" inputmode="decimal" value="0.10" />
        </div>
      </div>

      <div class="field-grid-2">
        <div class="field">
          <label for="jt-sl">Stop Loss</label>
          <input id="jt-sl" class="input mono" type="text" inputmode="decimal" value="3627.50" />
        </div>
        <div class="field">
          <label for="jt-tp">Take Profit</label>
          <input id="jt-tp" class="input mono" type="text" inputmode="decimal" value="3658.00" />
        </div>
      </div>

      <div class="field">
        <label for="jt-pnl">Profit/Loss ($)</label>
        <input id="jt-pnl" class="input mono" type="text" placeholder="e.g. 230.00 or -75.00" />
      </div>

      <div class="hscroll-row">
        ${['PLANNED', 'ACTIVE', 'WON', 'LOST'].map((st) => `
          <button type="button" class="chip ${st === status ? 'selected' : ''}" data-jt-status="${st}">${st}</button>
        `).join('')}
      </div>

      <div class="field">
        <label for="jt-notes">Notes</label>
        <textarea id="jt-notes" class="textarea" placeholder="Entry trigger, confirmation candle, or trade review..."></textarea>
      </div>

      <div id="jt-err" class="notice-banner notice-bear" hidden></div>

      <div class="field-grid-2" style="margin-top:4px;">
        <button type="button" id="jt-cancel" class="btn btn-outline">Cancel</button>
        <button type="button" id="jt-save" class="btn btn-gold">Save Trade</button>
      </div>
    `, (body) => {
      const symEl = body.querySelector('#jt-symbol');
      const tfEl = body.querySelector('#jt-tf');
      const buyBtn = body.querySelector('#jt-buy');
      const sellBtn = body.querySelector('#jt-sell');
      const entryEl = body.querySelector('#jt-entry');
      const lotsEl = body.querySelector('#jt-lots');
      const slEl = body.querySelector('#jt-sl');
      const tpEl = body.querySelector('#jt-tp');
      const pnlEl = body.querySelector('#jt-pnl');
      const notesEl = body.querySelector('#jt-notes');
      const errEl = body.querySelector('#jt-err');

      body.querySelectorAll('[data-quick-sym]').forEach((btn) => {
        btn.addEventListener('click', () => { symEl.value = btn.getAttribute('data-quick-sym'); });
      });

      buyBtn.addEventListener('click', () => {
        dir = 'BUY';
        buyBtn.classList.add('buy-active');
        sellBtn.classList.remove('sell-active');
      });
      sellBtn.addEventListener('click', () => {
        dir = 'SELL';
        sellBtn.classList.add('sell-active');
        buyBtn.classList.remove('buy-active');
      });

      [entryEl, lotsEl, slEl, tpEl].forEach((el) => {
        el.addEventListener('input', () => {
          const f = filterNumeric(el.value, false);
          if (el.value !== f) el.value = f;
          errEl.hidden = true;
        });
      });

      pnlEl.addEventListener('input', () => {
        const f = filterNumeric(pnlEl.value, true);
        if (pnlEl.value !== f) pnlEl.value = f;
      });

      body.querySelectorAll('[data-jt-status]').forEach((btn) => {
        btn.addEventListener('click', () => {
          status = btn.getAttribute('data-jt-status');
          body.querySelectorAll('[data-jt-status]').forEach((b) => b.classList.toggle('selected', b === btn));
        });
      });

      body.querySelector('#jt-cancel').addEventListener('click', closeModal);
      body.querySelector('#jt-save').addEventListener('click', () => {
        const symbol = symEl.value.trim().toUpperCase();
        const tf = tfEl.value.trim().toUpperCase() || 'M15';
        const entry = parseFloat(entryEl.value) || 0;
        const lots = parseFloat(lotsEl.value) || 0;
        const sl = parseFloat(slEl.value) || 0;
        const tp = parseFloat(tpEl.value) || 0;
        const pnlRaw = pnlEl.value.trim();
        const pnl = pnlRaw && pnlRaw !== '-' && pnlRaw !== '+' ? parseFloat(pnlRaw) : null;

        if (!symbol || entry <= 0 || sl <= 0 || tp <= 0 || lots <= 0) {
          errEl.textContent = 'Enter a Symbol and positive values for Entry Price, Stop Loss, Take Profit, and Lot Size.';
          errEl.hidden = false;
          return;
        }

        const stopDist = Math.max(Math.abs(entry - sl), 0.00001);
        const rr = Math.abs(tp - entry) / stopDist;
        const now = Date.now();

        state.journal.unshift({
          id: now,
          pair: symbol,
          timeframe: tf,
          direction: dir,
          entryPrice: entry,
          stopLoss: sl,
          takeProfit: tp,
          accountBalance: parseFloat(state.planner.balanceInput) || 10000,
          riskPercent: parseFloat(state.planner.riskPercentInput) || 1.0,
          lotSize: lots,
          riskRewardRatio: rr,
          profitLossUsd: Number.isFinite(pnl) ? pnl : null,
          status,
          notes: notesEl.value.trim(),
          createdAtMillis: now
        });

        persistJournal();
        state.journalFilter = 'ALL';
        closeModal();
        renderJournal();
      });
    });
  }

  window.addEventListener('resize', () => {
    if (state.activeTab === 'analysis') drawPriceActionCanvas();
  });

  // Initial render
  syncPlannerComputed();
  renderAnalysis();
})();
