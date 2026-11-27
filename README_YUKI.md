# YUKI — Personal AI Voice Assistant

Android-first personal assistant starter with Tanglish chat, voice input/output, a YUKI avatar placeholder, web research, and explicit correction memory.

## Status
This is a starter build, not a finished unrestricted assistant or production APK. It needs a server-side OpenAI API key and real-device testing.

## Included
- Web chat interface with YUKI portrait placeholder.
- Browser speech recognition where supported; device Tamil voice output.
- FastAPI backend using OpenAI Responses API and web search.
- SQLite chat history and user-submitted correction notes.
- Memory review and delete API endpoints.

## Run
Install Python 3.11+. In backend, install requirements, copy .env.example to .env, add your API key, then run: uvicorn main:app --host 0.0.0.0 --port 8000. Open /app in your browser.

## Honest limits
No AI can learn everything or operate without platform, safety, permission, cost, and rate limits. YUKI can search the web for current information and use corrections saved by the user; it does not retrain its underlying model. Sensitive actions must require confirmation. Never commit API keys or private data.

## Roadmap
Native Android shell and permissions; custom YUKI portrait/animated avatar; wake phrase with Android foreground-service restrictions; reminders; WhatsApp compose/share with confirmation; privacy controls; signed APK and device testing.
