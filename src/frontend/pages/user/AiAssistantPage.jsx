import { useMemo, useRef, useState } from 'react';
import Button from '../../components/Button';
import { aiApi } from '../../api/aiApi';
import { useAuth } from '../../context/AuthContext';
import { getErrorMessage } from '../../utils/errors';

const SUGGESTIONS = [
  'Show details of my last booking.',
  'Suggest hotels in Patna, Bihar with rating above 4.',
  'Show my booking details and status.',
  'Suggest the best hotel names by state, district, and rating.',
];

function formatTime(date) {
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function MessageBubble({ message }) {
  return (
    <article className={`ai-message ${message.role}`}>
      <div className="ai-message-meta">
        <span>{message.role === 'user' ? 'You' : 'GrandStay AI'}</span>
        <time>{message.time}</time>
      </div>
      <p>{message.content}</p>
    </article>
  );
}

export default function AiAssistantPage() {
  const { user } = useAuth();
  const inputRef = useRef(null);
  const [prompt, setPrompt] = useState('');
  const [loading, setLoading] = useState(false);
  const [messages, setMessages] = useState(() => [
    {
      id: 'welcome',
      role: 'assistant',
      content: 'Hi, I can help with hotel search, booking flow, payments, receipts, profile, and review questions.',
      time: formatTime(new Date()),
    },
  ]);

  const displayName = useMemo(() => {
    const name = `${user?.firstName || ''} ${user?.lastName || ''}`.trim();
    return name || user?.email || 'Guest';
  }, [user]);

  const sendMessage = async (event, quickPrompt) => {
    event?.preventDefault();
    const text = (quickPrompt || prompt).trim();
    if (!text || loading) return;

    const now = new Date();
    const userMessage = {
      id: `user-${now.getTime()}`,
      role: 'user',
      content: text,
      time: formatTime(now),
    };

    setMessages((current) => [...current, userMessage]);
    setPrompt('');
    setLoading(true);

    try {
      const { data } = await aiApi.chat(text);
      setMessages((current) => [
        ...current,
        {
          id: `assistant-${Date.now()}`,
          role: 'assistant',
          content: data?.response || 'I could not generate a response right now.',
          time: formatTime(new Date()),
        },
      ]);
    } catch (error) {
      setMessages((current) => [
        ...current,
        {
          id: `error-${Date.now()}`,
          role: 'assistant',
          content: getErrorMessage(error),
          time: formatTime(new Date()),
        },
      ]);
    } finally {
      setLoading(false);
      inputRef.current?.focus();
    }
  };

  return (
    <section className="section page-section ai-page">
      <div className="ai-hero">
        <div>
          <span className="eyebrow">AI concierge</span>
          <h1>Ask GrandStay AI</h1>
          <p>Ask for hotel suggestions, your booking details, last booked hotel, room guidance, payment, receipts, and reviews.</p>
        </div>
        <div className="ai-user-card glass">
          <span>Assisting</span>
          <strong>{displayName}</strong>
        </div>
      </div>

      <div className="ai-layout">
        <aside className="ai-sidebar glass">
          <h2>Try asking</h2>
          <div className="ai-suggestions">
            {SUGGESTIONS.map((item) => (
              <button type="button" key={item} onClick={(event) => sendMessage(event, item)} disabled={loading}>
                {item}
              </button>
            ))}
          </div>
        </aside>

        <div className="ai-chat glass">
          <div className="ai-chat-window" aria-live="polite">
            {messages.map((message) => (
              <MessageBubble key={message.id} message={message} />
            ))}
            {loading && (
              <article className="ai-message assistant">
                <div className="ai-message-meta">
                  <span>GrandStay AI</span>
                  <time>Thinking</time>
                </div>
                <p className="ai-typing">Preparing a helpful answer...</p>
              </article>
            )}
          </div>

          <form className="ai-composer" onSubmit={sendMessage}>
            <textarea
              ref={inputRef}
              value={prompt}
              onChange={(event) => setPrompt(event.target.value)}
              placeholder="Ask about hotels, bookings, payment, receipts, or rooms..."
              rows="2"
            />
            <Button disabled={loading || !prompt.trim()}>{loading ? 'Sending...' : 'Send'}</Button>
          </form>
        </div>
      </div>
    </section>
  );
}
