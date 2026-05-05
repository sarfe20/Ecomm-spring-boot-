import axios from "axios";

const api = axios.create({
    baseURL: `${import.meta.env.VITE_BACK_END_URL}/api`,
    withCredentials: true,
});

export const extractJwtToken = (value) => {
    if (!value) return null;

    if (typeof value === "string") {
        if (value.includes("=")) {
            return value.split(";")[0].split("=")[1] || null;
        }

        return value;
    }

    return null;
};

const getStoredJwtToken = () => {
    const rawToken = extractJwtToken(localStorage.getItem("jwtToken"));
    if (rawToken) return rawToken;

    const storedAuth = localStorage.getItem("auth");
    if (!storedAuth) return null;

    try {
        const parsedAuth = JSON.parse(storedAuth);
        return extractJwtToken(parsedAuth?.jwtToken);
    } catch {
        return null;
    }
};

api.interceptors.request.use((config) => {
    const token = getStoredJwtToken();
    config.headers = config.headers || {};

    if (token && !config.headers.Authorization) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

export default api;
