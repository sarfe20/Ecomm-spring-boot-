import { useMemo, useState } from "react";
import { FiExternalLink } from "react-icons/fi";
import { formatPrice } from "../../utils/formatPrice";
import truncateText from "../../utils/truncateText";

const ExternalProductResults = ({ products = [], loading = false }) => {
    const [activeSource, setActiveSource] = useState("All");
    const [sortMode, setSortMode] = useState("priceAsc");

    const sources = useMemo(
        () => ["All", ...new Set(products.map((product) => product.source).filter(Boolean))],
        [products]
    );

    const visibleProducts = useMemo(() => {
        const filteredProducts =
            activeSource === "All"
                ? products
                : products.filter((product) => product.source === activeSource);

        return [...filteredProducts].sort((first, second) => {
            if (sortMode === "platform") {
                return `${first.source}${first.title}`.localeCompare(`${second.source}${second.title}`);
            }

            const firstPrice = first.price ? Number(first.price) : Number.MAX_VALUE;
            const secondPrice = second.price ? Number(second.price) : Number.MAX_VALUE;
            return sortMode === "priceDesc"
                ? secondPrice - firstPrice
                : firstPrice - secondPrice;
        });
    }, [activeSource, products, sortMode]);

    const bestPrice = useMemo(() => {
        const pricedProducts = products
            .map((product) => Number(product.price))
            .filter((price) => Number.isFinite(price) && price > 0);
        return pricedProducts.length ? Math.min(...pricedProducts) : null;
    }, [products]);

    if (loading) {
        return (
            <div className="pt-8">
                <h2 className="text-xl font-bold text-slate-800 mb-4">Marketplace comparison</h2>
                <div className="text-slate-600">Searching marketplaces...</div>
            </div>
        );
    }

    if (!products.length) {
        return null;
    }

    return (
        <section className="pt-10">
            <div className="flex flex-col gap-4 pb-4 lg:flex-row lg:items-center lg:justify-between">
                <div>
                    <h2 className="text-xl font-bold text-slate-800">Marketplace comparison</h2>
                    <p className="text-sm text-slate-500">
                        {products.length} results found across {sources.length - 1} platforms
                    </p>
                </div>

                <div className="flex flex-wrap items-center gap-2">
                    {sources.map((source) => (
                        <button
                            key={source}
                            type="button"
                            onClick={() => setActiveSource(source)}
                            className={`rounded-md border px-3 py-2 text-sm font-semibold transition ${
                                activeSource === source
                                    ? "border-blue-600 bg-blue-600 text-white"
                                    : "border-slate-300 bg-white text-slate-700 hover:border-blue-500"
                            }`}>
                            {source}
                        </button>
                    ))}
                    <select
                        value={sortMode}
                        onChange={(event) => setSortMode(event.target.value)}
                        className="h-10 rounded-md border border-slate-300 bg-white px-3 text-sm font-semibold text-slate-700 outline-hidden focus:border-blue-500">
                        <option value="priceAsc">Lowest price</option>
                        <option value="priceDesc">Highest price</option>
                        <option value="platform">Platform</option>
                    </select>
                </div>
            </div>

            <div className="grid 2xl:grid-cols-4 lg:grid-cols-3 sm:grid-cols-2 gap-y-6 gap-x-6">
                {visibleProducts.map((product, index) => {
                    const productPrice = product.price ? Number(product.price) : null;
                    const isBestPrice = bestPrice && productPrice === bestPrice;
                    const isMarketplaceSearch = product.title?.startsWith('Search "');

                    return (
                    <a
                        key={`${product.source}-${index}-${product.productUrl}`}
                        href={product.productUrl}
                        target="_blank"
                        rel="noreferrer"
                        className="border rounded-lg shadow-md overflow-hidden bg-white hover:shadow-xl transition-shadow duration-300">
                        <div className="w-full overflow-hidden aspect-3/2 bg-slate-100">
                            {product.imageUrl ? (
                                <img
                                    className="w-full h-full object-contain p-3"
                                    src={product.imageUrl}
                                    alt={product.title}
                                />
                            ) : (
                                <div className="h-full flex items-center justify-center text-slate-500">
                                    {isMarketplaceSearch ? product.source : "No image"}
                                </div>
                            )}
                        </div>
                        <div className="p-4">
                            <div className="flex items-center justify-between gap-3 mb-2">
                                <span className="text-xs font-semibold uppercase tracking-wide text-blue-700">
                                    {product.source}
                                </span>
                                <FiExternalLink className="text-slate-600 shrink-0" />
                            </div>
                            <h3 className="text-base font-semibold text-slate-800 min-h-12">
                                {truncateText(product.title, 70)}
                            </h3>
                            <div className="pt-3 text-lg font-bold text-slate-700">
                                {product.price
                                    ? formatPrice(Number(product.price))
                                    : isMarketplaceSearch
                                        ? "Open search"
                                        : "View price"}
                            </div>
                            {isBestPrice && (
                                <div className="mt-2 inline-flex rounded-md bg-emerald-100 px-2 py-1 text-xs font-bold text-emerald-800">
                                    Lowest found
                                </div>
                            )}
                        </div>
                    </a>
                    );
                })}
            </div>
        </section>
    );
};

export default ExternalProductResults;
