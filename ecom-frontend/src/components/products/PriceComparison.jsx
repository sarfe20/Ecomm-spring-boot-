import { useEffect, useMemo, useState } from "react";
import toast from "react-hot-toast";
import {
  Area,
  AreaChart,
  CartesianGrid,
  ReferenceLine,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { FaArrowDown, FaArrowUp, FaChartLine, FaDatabase, FaRupeeSign } from "react-icons/fa";
import api from "../../api/api";
import { formatPrice } from "../../utils/formatPrice";

const formatShortDate = (value) => {
  if (!value) return "";

  return new Intl.DateTimeFormat("en-IN", {
    month: "short",
    day: "numeric",
  }).format(new Date(value));
};

const PriceTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;

  const item = payload[0]?.payload;

  return (
    <div className="rounded-md border border-slate-200 bg-white px-3 py-2 shadow-lg">
      <p className="text-xs font-semibold uppercase text-slate-500">
        {label}
      </p>
      <p className="mt-1 text-base font-bold text-slate-900">
        {formatPrice(Number(payload[0].value))}
      </p>
      {item?.platform && (
        <p className="text-xs text-slate-500">{item.platform}</p>
      )}
    </div>
  );
};

const PriceComparison = ({ productId }) => {
  const [prices, setPrices] = useState([]);
  const [history, setHistory] = useState([]);
  const [analysis, setAnalysis] = useState(null);
  const [productUrl, setProductUrl] = useState("");
  const [platformName, setPlatformName] = useState("Amazon");
  const [manualPrice, setManualPrice] = useState("");
  const [priceDate, setPriceDate] = useState(() => new Date().toISOString().slice(0, 10));
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const chartData = useMemo(
    () =>
      history.map((item) => ({
        date: item.date,
        displayDate: formatShortDate(item.date),
        price: Number(item.price),
        platform: item.platformName,
      })),
    [history]
  );

  const latestPrice = chartData.at(-1)?.price || 0;
  const firstPrice = chartData[0]?.price || 0;
  const priceChange = latestPrice && firstPrice ? latestPrice - firstPrice : 0;
  const priceChangePercentage =
    latestPrice && firstPrice ? ((priceChange / firstPrice) * 100).toFixed(1) : "0.0";
  const isPriceDown = priceChange < 0;

  const loadPriceData = async () => {
    if (!productId) return;

    try {
      setLoading(true);
      const [pricesResponse, historyResponse, analysisResponse] =
        await Promise.all([
          api.get(`/products/${productId}/prices`),
          api.get(`/products/${productId}/prices/history`),
          api.get(`/products/${productId}/prices/analysis`),
        ]);

      setPrices(pricesResponse.data || []);
      setHistory(historyResponse.data || []);
      setAnalysis(analysisResponse.data || null);
    } catch (error) {
      toast.error(error?.response?.data?.message || "Failed to load price data");
    } finally {
      setLoading(false);
    }
  };

  const getSourceLabel = (item) => {
    if (item.sourceType === "STORED" && item.platformName !== "Stored") {
      return "MANUAL";
    }

    return item.sourceType;
  };

  const submitPriceUrl = async (event) => {
    event.preventDefault();

    if (!productUrl.trim() && !manualPrice) {
      toast.error("Enter a marketplace URL or today's price");
      return;
    }

    try {
      setSubmitting(true);
      await api.post(`/products/${productId}/prices`, {
        productUrl: productUrl.trim() || null,
        platformName,
        price: manualPrice ? Number(manualPrice) : null,
        date: priceDate || null,
      });
      toast.success(manualPrice ? "Manual price saved" : "Price checked successfully");
      setProductUrl("");
      setManualPrice("");
      setPriceDate(new Date().toISOString().slice(0, 10));
      await loadPriceData();
    } catch (error) {
      toast.error(error?.response?.data?.message || "Failed to add price");
    } finally {
      setSubmitting(false);
    }
  };

  useEffect(() => {
    loadPriceData();
  }, [productId]);

  return (
    <div className="mt-6 border-t border-slate-200 pt-5">
      <h2 className="text-xl font-semibold text-slate-800">
        Price Comparison
      </h2>

      <form onSubmit={submitPriceUrl} className="mt-4 space-y-3">
        <div className="flex flex-wrap gap-2">
          {["Amazon", "Flipkart"].map((platform) => (
            <button
              key={platform}
              type="button"
              onClick={() => setPlatformName(platform)}
              className={`rounded-md border px-4 py-2 text-sm font-semibold transition ${
                platformName === platform
                  ? "border-blue-600 bg-blue-600 text-white"
                  : "border-slate-300 bg-white text-slate-700 hover:border-blue-500"
              }`}
            >
              {platform}
            </button>
          ))}
        </div>

        <div className="grid gap-3 lg:grid-cols-[1fr_140px_145px_auto]">
        <input
          value={productUrl}
          onChange={(event) => setProductUrl(event.target.value)}
          placeholder={`${platformName} product URL, or leave empty for manual price`}
          className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-hidden focus:border-blue-500"
        />
        <input
          value={manualPrice}
          onChange={(event) => setManualPrice(event.target.value)}
          placeholder="Today's price"
          min="1"
          step="0.01"
          type="number"
          className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-hidden focus:border-blue-500"
        />
        <input
          value={priceDate}
          onChange={(event) => setPriceDate(event.target.value)}
          type="date"
          className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-hidden focus:border-blue-500"
        />
        <button
          disabled={submitting}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 disabled:opacity-60"
          type="submit"
        >
          {submitting ? "Saving..." : "Save Price"}
        </button>
        </div>
      </form>

      {loading ? (
        <p className="mt-4 text-sm text-slate-600">Loading price insights...</p>
      ) : (
        <>
          <div className="mt-5 overflow-hidden rounded-lg border border-slate-200 bg-slate-950 text-white shadow-lg">
            <div className="grid gap-px bg-white/10 md:grid-cols-4">
              <div className="bg-slate-950 p-4">
                <div className="flex items-center gap-2 text-xs font-semibold uppercase text-cyan-200">
                  <FaArrowDown />
                  Lowest
                </div>
                <p className="mt-2 text-2xl font-bold">
                  {formatPrice(analysis?.lowestPrice || 0)}
                </p>
              </div>
              <div className="bg-slate-950 p-4">
                <div className="flex items-center gap-2 text-xs font-semibold uppercase text-rose-200">
                  <FaArrowUp />
                  Highest
                </div>
                <p className="mt-2 text-2xl font-bold">
                  {formatPrice(analysis?.highestPrice || 0)}
                </p>
              </div>
              <div className="bg-slate-950 p-4">
                <div className="flex items-center gap-2 text-xs font-semibold uppercase text-amber-200">
                  <FaRupeeSign />
                  Average
                </div>
                <p className="mt-2 text-2xl font-bold">
                  {formatPrice(analysis?.averagePrice || 0)}
                </p>
              </div>
              <div className="bg-slate-950 p-4">
                <div className="flex items-center gap-2 text-xs font-semibold uppercase text-emerald-200">
                  <FaChartLine />
                  Trend
                </div>
                <p className={`mt-2 text-2xl font-bold ${isPriceDown ? "text-emerald-300" : "text-rose-300"}`}>
                  {priceChange === 0 ? "0.0%" : `${isPriceDown ? "" : "+"}${priceChangePercentage}%`}
                </p>
              </div>
            </div>

            <div className="grid gap-5 p-4 lg:grid-cols-[1fr_220px]">
              <div className="h-72 min-w-0">
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={chartData} margin={{ top: 16, right: 18, left: 0, bottom: 4 }}>
                    <defs>
                      <linearGradient id="priceHistoryFill" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#22d3ee" stopOpacity={0.45} />
                        <stop offset="95%" stopColor="#22d3ee" stopOpacity={0.03} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid stroke="#334155" strokeDasharray="3 3" vertical={false} />
                    <XAxis
                      dataKey="displayDate"
                      axisLine={false}
                      tickLine={false}
                      tick={{ fill: "#cbd5e1", fontSize: 12 }}
                    />
                    <YAxis
                      axisLine={false}
                      tickLine={false}
                      tick={{ fill: "#cbd5e1", fontSize: 12 }}
                      tickFormatter={(value) => formatPrice(Number(value))}
                      width={86}
                    />
                    <Tooltip content={<PriceTooltip />} />
                    {analysis?.averagePrice ? (
                      <ReferenceLine
                        y={analysis.averagePrice}
                        stroke="#f59e0b"
                        strokeDasharray="4 4"
                        ifOverflow="extendDomain"
                      />
                    ) : null}
                    <Area
                      type="monotone"
                      dataKey="price"
                      stroke="#22d3ee"
                      strokeWidth={3}
                      fill="url(#priceHistoryFill)"
                      activeDot={{ r: 6, fill: "#ffffff", stroke: "#22d3ee", strokeWidth: 3 }}
                      dot={{ r: 4, fill: "#0f172a", stroke: "#22d3ee", strokeWidth: 2 }}
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </div>

              <div className="rounded-md border border-white/10 bg-white/5 p-4">
                <div className="flex items-center gap-2 text-sm font-semibold text-slate-100">
                  <FaDatabase />
                  Records
                </div>
                <p className="mt-3 text-3xl font-bold">{prices.length}</p>
                <p className="mt-1 text-sm text-slate-300">
                  tracked price points
                </p>
                <div className="mt-5 space-y-3 text-sm">
                  <div>
                    <p className="text-slate-400">Latest price</p>
                    <p className="font-semibold text-white">
                      {formatPrice(latestPrice)}
                    </p>
                  </div>
                  <div>
                    <p className="text-slate-400">Price movement</p>
                    <p className={isPriceDown ? "font-semibold text-emerald-300" : "font-semibold text-rose-300"}>
                      {priceChange === 0
                        ? "No change"
                        : `${isPriceDown ? "Down" : "Up"} ${formatPrice(Math.abs(priceChange))}`}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {analysis?.suggestion && (
            <p className="mt-3 rounded-md bg-emerald-50 px-3 py-2 text-sm font-medium text-emerald-800">
              {analysis.suggestion}
            </p>
          )}

          <div className="mt-5 overflow-x-auto">
            <table className="w-full overflow-hidden rounded-lg text-left text-sm">
              <thead>
                <tr className="bg-slate-100 text-slate-600">
                  <th className="px-3 py-3">Platform</th>
                  <th className="px-3 py-3">Price</th>
                  <th className="px-3 py-3">Date</th>
                  <th className="px-3 py-3">Source</th>
                </tr>
              </thead>
              <tbody>
                {prices.map((item) => (
                  <tr key={item.id} className="border-b border-slate-100 odd:bg-white even:bg-slate-50">
                    <td className="px-3 py-3">{item.platformName}</td>
                    <td className="px-3 py-3 font-semibold text-slate-900">
                      {formatPrice(Number(item.price))}
                    </td>
                    <td className="px-3 py-3">{item.date}</td>
                    <td className="px-3 py-3">
                      <span className="rounded-md bg-slate-200 px-2 py-1 text-xs font-semibold text-slate-700">
                        {getSourceLabel(item)}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
};

export default PriceComparison;
