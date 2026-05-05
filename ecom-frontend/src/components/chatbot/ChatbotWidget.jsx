import { useMemo, useRef, useState } from "react";
import { FaComments, FaTimes } from "react-icons/fa";
import api from "../../api/api";

const starterMessage = {
  role: "assistant",
  content:
    "Hi, I can help with product questions from this store catalog.",
};

const ChatbotWidget = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([starterMessage]);
  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const endRef = useRef(null);

  const recentHistory = useMemo(
    () =>
      messages
        .filter((message) => message.role === "user" || message.role === "assistant")
        .slice(-10),
    [messages]
  );

  const scrollToLatest = () => {
    window.requestAnimationFrame(() => {
      endRef.current?.scrollIntoView({ behavior: "smooth" });
    });
  };

  const sendMessage = async (event) => {
    event.preventDefault();

    const cleanInput = input.trim();
    if (!cleanInput || isLoading) return;

    const userMessage = { role: "user", content: cleanInput };
    setMessages((currentMessages) => [...currentMessages, userMessage]);
    setInput("");
    setIsLoading(true);
    scrollToLatest();

    try {
      const { data } = await api.post("/public/chatbot", {
        message: cleanInput,
        history: recentHistory,
      });

      setMessages((currentMessages) => [
        ...currentMessages,
        {
          role: "assistant",
          content: data?.answer || "I could not find a helpful answer for that.",
        },
      ]);
    } catch (error) {
      setMessages((currentMessages) => [
        ...currentMessages,
        {
          role: "assistant",
          content:
            error?.response?.data?.message ||
            "The local AI chatbot is not ready yet. Please start the desktop ai_chatbot server and try again.",
        },
      ]);
    } finally {
      setIsLoading(false);
      scrollToLatest();
    }
  };

  return (
    <div className="fixed bottom-5 right-5 z-50 font-montserrat">
      {isOpen && (
        <section className="mb-3 flex h-[520px] w-[min(92vw,380px)] flex-col overflow-hidden rounded-lg border border-slate-200 bg-white shadow-custom">
          <header className="flex items-center justify-between bg-emerald-700 px-4 py-3 text-white">
            <div>
              <p className="text-sm font-semibold">Project Chat</p>
              <p className="text-xs text-emerald-50">Ask about products</p>
            </div>
            <button
              className="rounded-md p-2 transition hover:bg-emerald-800"
              type="button"
              aria-label="Close chatbot"
              onClick={() => setIsOpen(false)}
            >
              <FaTimes />
            </button>
          </header>

          <div className="flex-1 space-y-3 overflow-y-auto bg-slate-50 px-4 py-4">
            {messages.map((message, index) => (
              <div
                key={`${message.role}-${index}`}
                className={`flex ${
                  message.role === "user" ? "justify-end" : "justify-start"
                }`}
              >
                <p
                  className={`max-w-[82%] whitespace-pre-wrap rounded-lg px-3 py-2 text-sm leading-6 ${
                    message.role === "user"
                      ? "bg-emerald-700 text-white"
                      : "border border-slate-200 bg-white text-slate-800"
                  }`}
                >
                  {message.content}
                </p>
              </div>
            ))}

            {isLoading && (
              <div className="flex justify-start">
                <p className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-600">
                  Thinking...
                </p>
              </div>
            )}
            <div ref={endRef} />
          </div>

          <form className="border-t border-slate-200 bg-white p-3" onSubmit={sendMessage}>
            <label className="sr-only" htmlFor="chatbot-message">
              Ask the project chatbot
            </label>
            <div className="flex gap-2">
              <input
                id="chatbot-message"
                className="min-w-0 flex-1 rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-emerald-700"
                type="text"
                value={input}
                maxLength={1000}
                placeholder="Ask about products, prices, stock..."
                onChange={(event) => setInput(event.target.value)}
              />
              <button
                className="rounded-lg bg-rose-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-rose-700 disabled:cursor-not-allowed disabled:bg-slate-300"
                type="submit"
                disabled={isLoading || !input.trim()}
              >
                Send
              </button>
            </div>
          </form>
        </section>
      )}

      <button
        className="ml-auto flex h-14 w-14 items-center justify-center rounded-lg bg-emerald-700 text-xl text-white shadow-custom transition hover:bg-emerald-800"
        type="button"
        aria-label="Open project chatbot"
        onClick={() => setIsOpen((currentValue) => !currentValue)}
      >
        {isOpen ? <FaTimes /> : <FaComments />}
      </button>
    </div>
  );
};

export default ChatbotWidget;
