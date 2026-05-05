import { Badge } from "@mui/material";
import { useEffect, useState } from "react";
import { FiSearch } from "react-icons/fi";
import { FaShoppingCart, FaSignInAlt, FaStore } from "react-icons/fa";
import { IoIosMenu } from "react-icons/io";
import { RxCross2 } from "react-icons/rx";
import { useSelector } from "react-redux";
import { Link, useLocation, useNavigate, useSearchParams } from "react-router-dom";
import UserMenu from "../UserMenu";

const Navbar = () => {
    const location = useLocation();
    const path = location.pathname;
    const [navbarOpen, setNavbarOpen] = useState(false);
    const [searchTerm, setSearchTerm] = useState("");
    const { cart } = useSelector((state) => state.carts);
    const { user } = useSelector((state) => state.auth);
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    useEffect(() => {
        const keyword = path === "/products" ? searchParams.get("keyword") || "" : "";
        setSearchTerm(keyword);
    }, [path, searchParams]);

    const submitSearch = (event) => {
        event.preventDefault();
        const params = new URLSearchParams();
        const keyword = searchTerm.trim();

        if (keyword) {
            params.set("keyword", keyword);
        }

        navigate(`/products${params.toString() ? `?${params.toString()}` : ""}`);
        setNavbarOpen(false);
    };
    
    return (
        <div className="min-h-[70px] bg-custom-gradient text-white z-50 flex items-center sticky top-0">
            <div className="lg:px-14 sm:px-8 px-4 py-3 w-full flex flex-wrap items-center justify-between gap-4">
                <Link to="/" className="flex items-center text-2xl font-bold">
                    <FaStore className="mr-2 text-3xl" />
                    <span className="font-[Poppins]">E-Shop</span>
                </Link>

                <form
                    onSubmit={submitSearch}
                    className="hidden lg:flex items-center flex-1 max-w-xl mx-4"
                >
                    <div className="relative w-full">
                        <FiSearch className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500 text-lg" />
                        <input
                            type="search"
                            value={searchTerm}
                            onChange={(event) => setSearchTerm(event.target.value)}
                            placeholder="Search products across store, Amazon, Flipkart..."
                            className="w-full rounded-full border border-white/20 bg-white px-12 py-3 text-slate-800 outline-none focus:ring-2 focus:ring-white/60"
                        />
                    </div>
                </form>

            <ul className={`flex sm:gap-10 gap-4 sm:items-center  text-slate-800 sm:static absolute left-0 top-[70px] sm:shadow-none shadow-md ${
            navbarOpen ? "h-fit sm:pb-0 pb-5" : "h-0 overflow-hidden"
          }  transition-all duration-100 sm:h-fit sm:bg-none bg-custom-gradient text-white sm:w-fit w-full sm:flex-row flex-col px-4 sm:px-0`}>
                <li className="lg:hidden pt-2">
                    <form onSubmit={submitSearch} className="relative">
                        <FiSearch className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500 text-lg" />
                        <input
                            type="search"
                            value={searchTerm}
                            onChange={(event) => setSearchTerm(event.target.value)}
                            placeholder="Search products..."
                            className="w-full rounded-full border border-white/20 bg-white px-12 py-3 text-slate-800 outline-none focus:ring-2 focus:ring-white/60"
                        />
                    </form>
                </li>
                <li className="font-medium transition-all duration-150">
                   <Link className={`${
                    path === "/" ? "text-white font-semibold" : "text-gray-200"
                   }`}
                    to="/">
                        Home
                   </Link> 
                </li>

                <li className="font-medium transition-all duration-150">
                   <Link className={`${
                    path === "/products" ? "text-white font-semibold" : "text-gray-200"
                   }`}
                    to="/products">
                        Products
                   </Link> 
                </li>


                <li className="font-medium transition-all duration-150">
                   <Link className={`${
                    path === "/about" ? "text-white font-semibold" : "text-gray-200"
                   }`}
                    to="/about">
                        About
                   </Link> 
                </li>

                <li className="font-medium transition-all duration-150">
                   <Link className={`${
                    path === "/contact" ? "text-white font-semibold" : "text-gray-200"
                   }`}
                    to="/contact">
                        Contact
                   </Link> 
                </li>

                <li className="font-medium transition-all duration-150">
                   <Link className={`${
                    path === "/cart" ? "text-white font-semibold" : "text-gray-200"
                   }`}
                    to="/cart">
                        <Badge
                            showZero
                            badgeContent={cart?.length || 0}
                            color="primary"
                            overlap="circular"
                            anchorOrigin={{ vertical: 'top', horizontal: 'right', }}>
                                <FaShoppingCart size={25} />
                        </Badge>
                   </Link> 
                </li>

                {(user && user.id) ? (
                    <li className="font-medium transition-all duration-150">
                        <UserMenu />
                    </li>
                ) : (
                <li className="font-medium transition-all duration-150">
                   <Link className="flex items-center space-x-2 px-4 py-[6px] 
                            bg-linear-to-r from-purple-600 to-red-500 
                            text-white font-semibold rounded-md shadow-lg 
                            hover:from-purple-500 hover:to-red-400 transition 
                            duration-300 ease-in-out transform "
                    to="/login">
                        <FaSignInAlt />
                        <span>Login</span>
                   </Link> 
                </li>
                )}
            </ul>

            <button
                onClick={() => setNavbarOpen(!navbarOpen)}
                className="sm:hidden flex items-center sm:mt-0 mt-2">
                    {navbarOpen ? (
                        <RxCross2 className="text-white text-3xl" />
                    ) : (
                        <IoIosMenu className="text-white text-3xl" />
                    )}
            </button>
            </div>
        </div>
    )
}

export default Navbar;
