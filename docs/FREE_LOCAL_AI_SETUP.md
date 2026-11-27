# Free local AI brain for YUKI

This mode uses Ollama on a computer you control. No paid API key is required. The computer must stay on and run the backend while the Android phone uses YUKI on the same trusted Wi-Fi network.

## 1. Install Ollama
Install Ollama from https://ollama.com/download for your computer.

## 2. Download a small model
Open Terminal / PowerShell and run:
```bash
ollama pull qwen2.5:3b
```
The model download uses internet and disk space. Actual chat inference runs locally; speed depends on your computer.

## 3. Start YUKI backend
In the repository's backend folder:
```bash
python -m venv .venv
```
Activate the environment, then:
```bash
pip install -r requirements.txt
```
Copy `.env.example` to `.env`. Default settings use Ollama:
```env
YUKI_PROVIDER=ollama
YUKI_MODEL=qwen2.5:3b
OLLAMA_BASE_URL=http://127.0.0.1:11434/v1
```
Start the backend:
```bash
uvicorn main:app --host 0.0.0.0 --port 8000
```
Check `http://127.0.0.1:8000/health` on the computer. Keep Ollama and the backend running.

## 4. Connect Android
For a phone and computer on the same trusted Wi-Fi, replace `YOUR-BACKEND-URL` in the Android asset HTML with `http://COMPUTER-LAN-IP:8000`. Do not expose this development server to the public internet. For remote access, set up authenticated HTTPS hosting first.

## Notes
- Local mode does not include built-in web search; current facts need a separate search integration.
- Local model quality and speed are limited by the computer's hardware.
- Conversation history and correction notes are stored in the backend SQLite file. Back it up and protect access.
- Do not add API keys, passwords, or private data to GitHub.
- This is a development setup, not a finished APK. Build and test the Android app separately.
