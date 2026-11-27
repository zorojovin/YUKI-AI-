import os, sqlite3
from pathlib import Path
from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException
from fastapi.staticfiles import StaticFiles
from fastapi.middleware.cors import CORSMiddleware
from openai import OpenAI
from pydantic import BaseModel, Field

load_dotenv()
BASE = Path(__file__).resolve().parent
DB = BASE / 'yuki_memory.db'
MODEL = os.getenv('OPENAI_MODEL', 'gpt-4.1-mini')
client = OpenAI(api_key=os.getenv('OPENAI_API_KEY')) if os.getenv('OPENAI_API_KEY') else None
app = FastAPI(title='YUKI Personal Assistant', version='0.1.0')
app.add_middleware(CORSMiddleware, allow_origins=['*'], allow_credentials=False, allow_methods=['*'], allow_headers=['*'])
SYSTEM = '''You are YUKI, a helpful personal AI assistant. Speak naturally in Tamil Nadu Tamil-English mix (Tanglish) when the user does. Be clear, warm, honest and practical. Do not pretend to be human or have a body. Use web search for current facts when available and explain uncertainty. Consider explicit user correction notes, but do not claim to retrain your model or know everything. Ask before sensitive or irreversible actions. Never send messages, spend money, or share private data without clear confirmation. Keep financial content educational for teen users; do not encourage real-money trading.'''

def connect():
    c=sqlite3.connect(DB)
    c.execute('CREATE TABLE IF NOT EXISTS messages(id INTEGER PRIMARY KEY, role TEXT, content TEXT, created_at DATETIME DEFAULT CURRENT_TIMESTAMP)')
    c.execute('CREATE TABLE IF NOT EXISTS feedback(id INTEGER PRIMARY KEY, note TEXT, created_at DATETIME DEFAULT CURRENT_TIMESTAMP)')
    return c

class Chat(BaseModel): message: str = Field(min_length=1, max_length=8000)
class Feedback(BaseModel): note: str = Field(min_length=1, max_length=2000)

@app.get('/health')
def health(): return {'ok':True,'service':'YUKI','api_key_configured':client is not None}

@app.post('/chat')
def chat(req: Chat):
    if client is None: raise HTTPException(503,'Add OPENAI_API_KEY to backend .env on the server.')
    c=connect()
    try:
        history=c.execute('SELECT role,content FROM messages ORDER BY id DESC LIMIT 12').fetchall()[::-1]
        notes=c.execute('SELECT note FROM feedback ORDER BY id DESC LIMIT 12').fetchall()
        prompt=SYSTEM + ('\nUser-approved correction notes:\n'+'\n'.join('- '+n[0] for n in notes) if notes else '')
        inputs=[{'role':'system','content':prompt}]+[{'role':r,'content':t} for r,t in history]+[{'role':'user','content':req.message}]
        response=client.responses.create(model=MODEL,input=inputs,tools=[{'type':'web_search_preview'}])
        answer=response.output_text or 'Sorry, I could not answer that.'
        c.execute('INSERT INTO messages(role,content) VALUES(?,?)',('user',req.message))
        c.execute('INSERT INTO messages(role,content) VALUES(?,?)',('assistant',answer)); c.commit()
        return {'reply':answer}
    except Exception as e: raise HTTPException(502,'AI request failed: '+type(e).__name__)
    finally: c.close()

@app.post('/feedback')
def feedback(req: Feedback):
    c=connect(); c.execute('INSERT INTO feedback(note) VALUES(?)',(req.note,)); c.commit(); c.close(); return {'saved':True}

@app.get('/memory')
def memory():
    c=connect(); rows=c.execute('SELECT id,note,created_at FROM feedback ORDER BY id DESC').fetchall(); c.close()
    return {'items':[{'id':r[0],'note':r[1],'created_at':r[2]} for r in rows]}

@app.delete('/memory/{note_id}')
def delete_memory(note_id:int):
    c=connect(); cur=c.execute('DELETE FROM feedback WHERE id=?',(note_id,)); c.commit(); c.close()
    if not cur.rowcount: raise HTTPException(404,'Memory note not found')
    return {'deleted':True}

app.mount('/app',StaticFiles(directory=BASE/'web',html=True),name='app')
