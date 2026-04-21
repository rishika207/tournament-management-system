"""
Multi-Sport Tournament Management System
Streamlit Frontend — calls the Java Spark REST API at http://localhost:4567
"""

import streamlit as st
import requests
import pandas as pd
from datetime import date, datetime

# ─────────────────────────────────────────────────────────
# CONFIG
# ─────────────────────────────────────────────────────────
API_BASE = "http://localhost:4567"

st.set_page_config(
    page_title="TournamentHub",
    page_icon="🏆",
    layout="wide",
    initial_sidebar_state="expanded",
)

# ─────────────────────────────────────────────────────────
# CUSTOM CSS — Dark editorial theme
# ─────────────────────────────────────────────────────────
st.markdown("""
<style>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=DM+Sans:wght@300;400;500;700&display=swap');

html, body, [class*="css"] {
    font-family: 'DM Sans', sans-serif;
}

/* Page background */
.stApp {
    background: #0a0e1a;
    color: #e8eaf0;
}

/* Sidebar */
section[data-testid="stSidebar"] {
    background: linear-gradient(180deg, #111827 0%, #0d1117 100%);
    border-right: 1px solid #1e2d3d;
}
section[data-testid="stSidebar"] .stRadio label {
    color: #94a3b8 !important;
    font-weight: 500;
    font-size: 0.95rem;
    padding: 6px 0;
    transition: color 0.2s;
}
section[data-testid="stSidebar"] .stRadio label:hover {
    color: #f59e0b !important;
}

/* Hero heading font */
h1 { font-family: 'Bebas Neue', sans-serif; letter-spacing: 3px; color: #f59e0b !important; }
h2 { font-family: 'Bebas Neue', sans-serif; letter-spacing: 2px; color: #e2e8f0 !important; }
h3 { color: #cbd5e1 !important; }

/* Cards */
.card {
    background: #111827;
    border: 1px solid #1e2d3d;
    border-radius: 12px;
    padding: 20px 24px;
    margin-bottom: 16px;
    transition: border-color 0.2s;
}
.card:hover { border-color: #f59e0b; }
.card-title {
    font-family: 'Bebas Neue', sans-serif;
    font-size: 1.3rem;
    letter-spacing: 1.5px;
    color: #f59e0b;
    margin-bottom: 8px;
}
.badge {
    display: inline-block;
    padding: 3px 10px;
    border-radius: 20px;
    font-size: 0.75rem;
    font-weight: 600;
    letter-spacing: 0.5px;
}
.badge-upcoming  { background: #1e3a5f; color: #60a5fa; }
.badge-ongoing   { background: #14532d; color: #4ade80; }
.badge-completed { background: #3b1f1f; color: #f87171; }
.badge-scheduled { background: #1e2d3d; color: #94a3b8; }
.badge-cricket   { background: #3b2f07; color: #fbbf24; }
.badge-football  { background: #0f2a1a; color: #34d399; }
.badge-badminton { background: #1e1b4b; color: #a78bfa; }

/* Score board */
.scoreboard {
    background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
    border: 1px solid #334155;
    border-radius: 16px;
    padding: 24px;
    text-align: center;
}
.score-num {
    font-family: 'Bebas Neue', sans-serif;
    font-size: 3rem;
    color: #f59e0b;
}
.score-team {
    font-size: 1rem;
    color: #94a3b8;
    text-transform: uppercase;
    letter-spacing: 1px;
}
.vs-divider { color: #475569; font-size: 1.5rem; font-weight: 700; }

/* Metric boxes */
.metric-box {
    background: #111827;
    border: 1px solid #1e2d3d;
    border-radius: 10px;
    padding: 16px;
    text-align: center;
}
.metric-value {
    font-family: 'Bebas Neue', sans-serif;
    font-size: 2.5rem;
    color: #f59e0b;
    line-height: 1;
}
.metric-label { color: #64748b; font-size: 0.8rem; text-transform: uppercase; letter-spacing: 1px; }

/* Streamlit widgets overrides */
.stButton > button {
    background: linear-gradient(135deg, #f59e0b, #d97706) !important;
    color: #0a0e1a !important;
    border: none !important;
    font-weight: 700 !important;
    border-radius: 8px !important;
    padding: 10px 28px !important;
    font-size: 0.9rem !important;
    letter-spacing: 0.5px !important;
    transition: opacity 0.2s !important;
}
.stButton > button:hover { opacity: 0.88 !important; }

.stTextInput > div > div > input,
.stSelectbox > div > div,
.stDateInput > div > div > input,
.stNumberInput > div > div > input {
    background: #111827 !important;
    border: 1px solid #1e2d3d !important;
    color: #e8eaf0 !important;
    border-radius: 8px !important;
}

div[data-testid="stDataFrame"] {
    background: #111827 !important;
    border-radius: 10px;
}

/* Dividers */
hr { border-color: #1e2d3d !important; }

/* Success/Error banners */
.stSuccess { background: #14532d !important; color: #4ade80 !important; }
.stError   { background: #3b1f1f !important; color: #f87171 !important; }

/* Spinner */
.stSpinner > div { border-top-color: #f59e0b !important; }
</style>
""", unsafe_allow_html=True)

# ─────────────────────────────────────────────────────────
# API HELPERS
# ─────────────────────────────────────────────────────────
def api_get(path: str, params: dict = None):
    try:
        r = requests.get(f"{API_BASE}{path}", params=params, timeout=5)
        data = r.json()
        if data.get("status") == "success":
            return data.get("data", [])
        st.error(f"API Error: {data.get('message', 'Unknown error')}")
        return []
    except requests.exceptions.ConnectionError:
        st.error("⚠️ Cannot connect to Java API at localhost:4567. Please start the backend first.")
        return []
    except Exception as e:
        st.error(f"Request failed: {e}")
        return []

def api_post(path: str, payload: dict):
    try:
        r = requests.post(f"{API_BASE}{path}", json=payload, timeout=5)
        return r.json()
    except requests.exceptions.ConnectionError:
        return {"status": "error", "message": "Cannot connect to Java API at localhost:4567"}
    except Exception as e:
        return {"status": "error", "message": str(e)}

def badge(text: str, css_class: str):
    return f'<span class="badge {css_class}">{text}</span>'

def sport_badge(sport: str):
    cls = f"badge-{sport.lower()}" if sport else "badge-scheduled"
    icon = {"Cricket": "🏏", "Football": "⚽", "Badminton": "🏸"}.get(sport, "🎮")
    return badge(f"{icon} {sport}", cls)

def status_badge(status: str):
    cls = {
        "Upcoming": "badge-upcoming", "Ongoing": "badge-ongoing",
        "Completed": "badge-completed", "Scheduled": "badge-scheduled",
        "InProgress": "badge-ongoing", "Cancelled": "badge-completed",
        "Confirmed": "badge-ongoing", "Pending": "badge-upcoming",
    }.get(status, "badge-scheduled")
    return badge(status, cls)

# ─────────────────────────────────────────────────────────
# SIDEBAR NAVIGATION
# ─────────────────────────────────────────────────────────
with st.sidebar:
    st.markdown("""
    <div style="text-align:center; padding: 20px 0 30px">
        <div style="font-family:'Bebas Neue',sans-serif; font-size:2.2rem;
                    color:#f59e0b; letter-spacing:3px; line-height:1">
            TOURNAMENT<br>HUB
        </div>
        <div style="color:#475569; font-size:0.75rem; letter-spacing:2px; margin-top:4px">
            MULTI-SPORT PLATFORM
        </div>
    </div>
    """, unsafe_allow_html=True)

    page = st.radio(
        "Navigate",
        options=[
            "🏠  Home",
            "🏆  Create Tournament",
            "👥  Register Team",
            "📅  Schedule Match",
            "📊  Update Score",
            "🥇  View Results",
        ],
        label_visibility="collapsed"
    )

    st.markdown("---")
    st.markdown("""
    <div style="color:#374151; font-size:0.72rem; padding:0 8px">
        <b style="color:#475569">ACTORS</b><br>
        👑 Admin • Organizer<br>
        🧑 TeamManager • Referee<br>
        👁 Viewer • Payment GW
    </div>
    """, unsafe_allow_html=True)

# ─────────────────────────────────────────────────────────
# PAGE: HOME
# ─────────────────────────────────────────────────────────
if "Home" in page:
    st.markdown("# TOURNAMENT HUB")
    st.markdown("### Real-time multi-sport tournament management")
    st.markdown("---")

    tournaments = api_get("/tournaments")
    matches     = api_get("/matches")
    teams       = api_get("/teams")

    total = len(tournaments)
    ongoing   = sum(1 for t in tournaments if t.get("status") == "Ongoing")
    upcoming  = sum(1 for t in tournaments if t.get("status") == "Upcoming")
    completed = sum(1 for t in tournaments if t.get("status") == "Completed")

    c1, c2, c3, c4 = st.columns(4)
    metrics = [
        ("🏆", total,   "Tournaments"),
        ("⚡", ongoing,  "Live Now"),
        ("📅", upcoming, "Upcoming"),
        ("✅", completed,"Completed"),
    ]
    for col, (icon, val, label) in zip([c1,c2,c3,c4], metrics):
        with col:
            st.markdown(f"""
            <div class="metric-box">
                <div style="font-size:1.6rem">{icon}</div>
                <div class="metric-value">{val}</div>
                <div class="metric-label">{label}</div>
            </div>
            """, unsafe_allow_html=True)

    st.markdown("<br>", unsafe_allow_html=True)
    col_t, col_m = st.columns([3, 2])

    with col_t:
        st.markdown("## TOURNAMENTS")
        if not tournaments:
            st.info("No tournaments yet. Create one from the sidebar!")
        for t in tournaments[:6]:
            st.markdown(f"""
            <div class="card">
                <div class="card-title">{t['name']}</div>
                {sport_badge(t.get('sport_type',''))} &nbsp;
                {status_badge(t.get('status',''))}
                <div style="margin-top:10px; color:#64748b; font-size:0.85rem">
                    📍 {t.get('venue_name','—')} · {t.get('city','—')}<br>
                    🗓 {t.get('start_date','—')} → {t.get('end_date','—')}
                    <span style="float:right; color:#94a3b8">by {t.get('creator_name','—')}</span>
                </div>
            </div>
            """, unsafe_allow_html=True)

    with col_m:
        st.markdown("## RECENT MATCHES")
        if not matches:
            st.info("No matches scheduled yet.")
        for m in matches[-5:]:
            winner = m.get("winner_name") or "TBD"
            t1s = m.get("team1_score") or "–"
            t2s = m.get("team2_score") or "–"
            st.markdown(f"""
            <div class="card">
                <div style="display:flex; justify-content:space-between; align-items:center">
                    <span style="color:#e2e8f0; font-weight:600; font-size:0.9rem">
                        {m['team1_name']}
                    </span>
                    <span style="color:#f59e0b; font-family:'Bebas Neue',sans-serif; font-size:1.3rem">
                        {t1s} – {t2s}
                    </span>
                    <span style="color:#e2e8f0; font-weight:600; font-size:0.9rem">
                        {m['team2_name']}
                    </span>
                </div>
                <div style="margin-top:6px; font-size:0.78rem; color:#64748b">
                    {status_badge(m.get('status',''))}
                    &nbsp; {m.get('match_date','')[:16]}
                    {f"&nbsp; 🏅 {winner}" if m.get('status')=='Completed' else ''}
                </div>
            </div>
            """, unsafe_allow_html=True)

    st.markdown("---")
    st.markdown(f"""
    <div style="color:#374151; font-size:0.8rem; text-align:center">
        Powered by Java Spark REST API + MySQL + Streamlit &nbsp;|&nbsp;
        Factory Pattern · Command Pattern · OOP Inheritance
    </div>
    """, unsafe_allow_html=True)


# ─────────────────────────────────────────────────────────
# PAGE: CREATE TOURNAMENT (Admin)
# ─────────────────────────────────────────────────────────
elif "Create Tournament" in page:
    st.markdown("# CREATE TOURNAMENT")
    st.markdown("*Actor: **Admin** — uses SportFactory to validate sport type*")
    st.markdown("---")

    locations = api_get("/locations")
    admins    = api_get("/users", {"role": "Admin"})

    loc_map   = {f"{l['venue_name']} — {l['city']}": l['location_id'] for l in locations}
    admin_map = {a['name']: a['user_id'] for a in admins}

    with st.form("create_tournament"):
        col1, col2 = st.columns(2)
        with col1:
            t_name  = st.text_input("Tournament Name", placeholder="e.g. IPL 2025")
            sport   = st.selectbox("Sport", ["Cricket", "Football", "Badminton"])
            creator = st.selectbox("Created By (Admin)", list(admin_map.keys()) if admin_map else ["—"])
        with col2:
            start_d  = st.date_input("Start Date", value=date.today())
            end_d    = st.date_input("End Date",   value=date.today())
            location = st.selectbox("Venue", list(loc_map.keys()) if loc_map else ["—"])

        submitted = st.form_submit_button("🏆 Create Tournament")

    if submitted:
        if not t_name.strip():
            st.error("Tournament name is required.")
        elif end_d < start_d:
            st.error("End date must be after start date.")
        else:
            payload = {
                "name":        t_name.strip(),
                "sport_type":  sport,
                "start_date":  str(start_d),
                "end_date":    str(end_d),
                "location_id": loc_map.get(location, 1),
                "created_by":  admin_map.get(creator, 1),
            }
            resp = api_post("/tournaments", payload)
            if resp.get("status") == "success":
                st.success(f"✅ Tournament '{t_name}' created! ID: {resp['data'].get('tournament_id')}")
                st.balloons()
            else:
                st.error(f"❌ {resp.get('message','Failed')}")

    st.markdown("---")
    st.markdown("### Existing Tournaments")
    tournaments = api_get("/tournaments")
    if tournaments:
        df = pd.DataFrame(tournaments)[["tournament_id","name","sport_type","start_date","end_date","status","venue_name","city"]]
        df.columns = ["ID","Name","Sport","Start","End","Status","Venue","City"]
        st.dataframe(df, use_container_width=True, hide_index=True)


# ─────────────────────────────────────────────────────────
# PAGE: REGISTER TEAM (TeamManager → payment triggered)
# ─────────────────────────────────────────────────────────
elif "Register Team" in page:
    st.markdown("# REGISTER TEAM")
    st.markdown("*Actor: **TeamManager** — registration triggers **Payment Gateway** (Command Pattern)*")
    st.markdown("---")

    tab_reg, tab_team, tab_player = st.tabs(
        ["📋 Register for Tournament", "➕ Create New Team", "👤 Add Player"]
    )

    # ── Tab 1: Register team in a tournament ──────────────
    with tab_reg:
        tournaments = api_get("/tournaments")
        teams       = api_get("/teams")
        managers    = api_get("/users", {"role": "TeamManager"})

        tourn_map = {f"[{t['tournament_id']}] {t['name']} ({t['sport_type']})": t['tournament_id']
                     for t in tournaments}
        team_map  = {f"[{t['team_id']}] {t['team_name']}": t['team_id'] for t in teams}

        with st.form("register_team_form"):
            sel_t = st.selectbox("Select Tournament", list(tourn_map.keys()) if tourn_map else ["—"])
            sel_tm = st.selectbox("Select Team",      list(team_map.keys())  if team_map  else ["—"])
            amount = st.number_input("Registration Fee (₹)", min_value=0.0, value=500.0, step=50.0)
            sub = st.form_submit_button("🎯 Register & Pay")

        if sub:
            payload = {
                "tournament_id": tourn_map.get(sel_t, 1),
                "team_id":       team_map.get(sel_tm, 1),
                "amount":        amount,
            }
            resp = api_post("/registrations", payload)
            if resp.get("status") == "success":
                d = resp['data']
                st.success(f"✅ Team registered! Payment Ref: **{d.get('payment_ref')}**")
                st.info(f"Registration ID: {d.get('registration_id')}")
            else:
                st.error(f"❌ {resp.get('message','Registration failed')}")

    # ── Tab 2: Create a new team ───────────────────────────
    with tab_team:
        managers = api_get("/users", {"role": "TeamManager"})
        mgr_map  = {m['name']: m['user_id'] for m in managers}

        with st.form("create_team_form"):
            team_name = st.text_input("Team Name", placeholder="e.g. Thunder Hawks")
            manager   = st.selectbox("Manager", list(mgr_map.keys()) if mgr_map else ["—"])
            sub2 = st.form_submit_button("👥 Create Team")

        if sub2:
            resp = api_post("/teams", {
                "team_name": team_name.strip(),
                "manager_id": mgr_map.get(manager, 1)
            })
            if resp.get("status") == "success":
                st.success(f"✅ Team '{team_name}' created! ID: {resp['data'].get('team_id')}")
            else:
                st.error(f"❌ {resp.get('message','Failed')}")

    # ── Tab 3: Add player ──────────────────────────────────
    with tab_player:
        teams = api_get("/teams")
        team_map = {f"[{t['team_id']}] {t['team_name']}": t['team_id'] for t in teams}

        with st.form("add_player_form"):
            p_name  = st.text_input("Player Name")
            p_age   = st.number_input("Age", min_value=10, max_value=50, value=22)
            p_skill = st.selectbox("Skill Level", ["Beginner", "Intermediate", "Advanced", "Pro"])
            p_team  = st.selectbox("Team", list(team_map.keys()) if team_map else ["—"])
            sub3 = st.form_submit_button("➕ Add Player")

        if sub3:
            resp = api_post("/players", {
                "name":    p_name.strip(),
                "age":     p_age,
                "skill":   p_skill,
                "team_id": team_map.get(p_team, 1)
            })
            if resp.get("status") == "success":
                st.success(f"✅ Player '{p_name}' added!")
            else:
                st.error(f"❌ {resp.get('message','Failed')}")

        st.markdown("---")
        if teams:
            sel_view = st.selectbox("View players for team:", list(team_map.keys()))
            if sel_view:
                players = api_get(f"/teams/{team_map[sel_view]}/players")
                if players:
                    df = pd.DataFrame(players)[["player_id","name","age","skill"]]
                    df.columns = ["ID","Name","Age","Skill"]
                    st.dataframe(df, use_container_width=True, hide_index=True)
                else:
                    st.info("No players in this team yet.")


# ─────────────────────────────────────────────────────────
# PAGE: SCHEDULE MATCH (Organizer)
# ─────────────────────────────────────────────────────────
elif "Schedule Match" in page:
    st.markdown("# SCHEDULE MATCH")
    st.markdown("*Actor: **Organizer** — assigns referee, uses ScheduleMatchCommand*")
    st.markdown("---")

    tournaments = api_get("/tournaments")
    teams       = api_get("/teams")
    referees    = api_get("/users", {"role": "Referee"})
    locations   = api_get("/locations")

    tourn_map = {f"[{t['tournament_id']}] {t['name']}": t['tournament_id'] for t in tournaments}
    team_map  = {f"[{t['team_id']}] {t['team_name']}": t['team_id'] for t in teams}
    ref_map   = {r['name']: r['user_id'] for r in referees}
    loc_map   = {f"{l['venue_name']} — {l['city']}": l['location_id'] for l in locations}

    with st.form("schedule_match"):
        col1, col2 = st.columns(2)
        with col1:
            sel_tourn = st.selectbox("Tournament",     list(tourn_map.keys()) if tourn_map else ["—"])
            sel_team1 = st.selectbox("Team 1",         list(team_map.keys())  if team_map  else ["—"])
            sel_team2 = st.selectbox("Team 2",         list(team_map.keys())  if team_map  else ["—"])
        with col2:
            sel_ref  = st.selectbox("Referee",         list(ref_map.keys())   if ref_map   else ["—"])
            sel_loc  = st.selectbox("Venue",           list(loc_map.keys())   if loc_map   else ["—"])
            match_dt = st.date_input("Match Date", value=date.today())
            match_tm = st.time_input("Match Time", value=datetime.now().time())

        sub = st.form_submit_button("📅 Schedule Match")

    if sub:
        if sel_team1 == sel_team2:
            st.error("A team cannot play against itself!")
        else:
            dt_str = f"{match_dt} {match_tm}"
            payload = {
                "tournament_id": tourn_map.get(sel_tourn, 1),
                "team1_id":      team_map.get(sel_team1, 1),
                "team2_id":      team_map.get(sel_team2, 2),
                "referee_id":    ref_map.get(sel_ref, 1),
                "location_id":   loc_map.get(sel_loc, 1),
                "match_date":    dt_str,
            }
            resp = api_post("/matches", payload)
            if resp.get("status") == "success":
                st.success(f"✅ Match scheduled! ID: {resp['data'].get('match_id')}")
            else:
                st.error(f"❌ {resp.get('message','Failed')}")

    st.markdown("---")
    st.markdown("### Scheduled Matches")
    matches = api_get("/matches")
    if matches:
        rows = []
        for m in matches:
            rows.append({
                "ID": m["match_id"],
                "Tournament": m.get("tournament_id"),
                "Team 1": m["team1_name"],
                "Team 2": m["team2_name"],
                "Referee": m.get("referee_name","—"),
                "Venue": f"{m.get('venue_name','—')}, {m.get('city','—')}",
                "Date": m.get("match_date","")[:16],
                "Status": m.get("status","—"),
            })
        df = pd.DataFrame(rows)
        st.dataframe(df, use_container_width=True, hide_index=True)


# ─────────────────────────────────────────────────────────
# PAGE: UPDATE SCORE (Referee)
# ─────────────────────────────────────────────────────────
elif "Update Score" in page:
    st.markdown("# UPDATE SCORE")
    st.markdown("*Actor: **Referee** — uses UpdateScoreCommand, declares match winner*")
    st.markdown("---")

    matches  = api_get("/matches")
    referees = api_get("/users", {"role": "Referee"})
    teams    = api_get("/teams")

    active_matches = [m for m in matches if m.get("status") in ("Scheduled","InProgress")]
    match_map  = {f"[{m['match_id']}] {m['team1_name']} vs {m['team2_name']} ({m.get('match_date','')[:10]})":
                  m for m in active_matches}
    ref_map    = {r['name']: r['user_id'] for r in referees}
    team_map_id = {t['team_name']: t['team_id'] for t in teams}

    if not active_matches:
        st.info("No active matches. Schedule matches first.")
    else:
        with st.form("update_score_form"):
            sel_match = st.selectbox("Select Match", list(match_map.keys()))
            sel_ref   = st.selectbox("Referee", list(ref_map.keys()) if ref_map else ["—"])

            match_obj = match_map.get(sel_match, {})
            t1_name = match_obj.get("team1_name","Team 1")
            t2_name = match_obj.get("team2_name","Team 2")

            col1, _, col2 = st.columns([5,1,5])
            with col1:
                st.markdown(f"**{t1_name}**")
                score1 = st.text_input(f"Score", key="s1", placeholder="e.g. 145 or 3")
            with _:
                st.markdown("<br><br>", unsafe_allow_html=True)
                st.markdown("<div style='text-align:center;color:#f59e0b;font-weight:700'>VS</div>",
                            unsafe_allow_html=True)
            with col2:
                st.markdown(f"**{t2_name}**")
                score2 = st.text_input(f"Score", key="s2", placeholder="e.g. 130 or 1")

            winner = st.selectbox("Declare Winner", [t1_name, t2_name, "Draw"])
            sub = st.form_submit_button("📊 Update & Finalize")

        if sub:
            match_id   = match_obj.get("match_id")
            t1_id = match_obj.get("team1_id") or team_map_id.get(t1_name, 1)
            t2_id = match_obj.get("team2_id") or team_map_id.get(t2_name, 2)
            w_id  = t1_id if winner == t1_name else (t2_id if winner == t2_name else 0)

            payload = {
                "match_id":      match_id,
                "team1_score":   score1 or "0",
                "team2_score":   score2 or "0",
                "winner_team_id": w_id,
                "updated_by":    ref_map.get(sel_ref, 1),
            }
            resp = api_post("/scores", payload)
            if resp.get("status") == "success":
                st.success("✅ Score updated! Match marked as Completed.")
                if winner != "Draw":
                    st.balloons()
            else:
                st.error(f"❌ {resp.get('message','Failed')}")

    st.markdown("---")
    st.markdown("### Completed Matches")
    done = [m for m in matches if m.get("status") == "Completed"]
    if done:
        for m in done:
            t1s = m.get("team1_score","—")
            t2s = m.get("team2_score","—")
            w   = m.get("winner_name") or "Draw"
            st.markdown(f"""
            <div class="scoreboard" style="margin-bottom:12px">
                <div style="display:flex; justify-content:space-around; align-items:center">
                    <div>
                        <div class="score-team">{m['team1_name']}</div>
                        <div class="score-num">{t1s}</div>
                    </div>
                    <div class="vs-divider">VS</div>
                    <div>
                        <div class="score-team">{m['team2_name']}</div>
                        <div class="score-num">{t2s}</div>
                    </div>
                </div>
                <div style="margin-top:12px; color:#94a3b8; font-size:0.85rem">
                    🏅 Winner: <b style="color:#f59e0b">{w}</b>
                    &nbsp;|&nbsp; {m.get('match_date','')[:16]}
                </div>
            </div>
            """, unsafe_allow_html=True)
    else:
        st.info("No completed matches yet.")


# ─────────────────────────────────────────────────────────
# PAGE: VIEW RESULTS (Viewer)
# ─────────────────────────────────────────────────────────
elif "View Results" in page:
    st.markdown("# VIEW RESULTS & STANDINGS")
    st.markdown("*Actor: **Viewer** — read-only access to schedules, scores & standings*")
    st.markdown("---")

    tournaments = api_get("/tournaments")
    if not tournaments:
        st.info("No tournaments available yet.")
        st.stop()

    tourn_map = {f"[{t['tournament_id']}] {t['name']}": t for t in tournaments}
    sel = st.selectbox("Select Tournament", list(tourn_map.keys()))
    t_obj = tourn_map[sel]
    t_id  = t_obj["tournament_id"]

    col1, col2, col3 = st.columns(3)
    with col1:
        st.markdown(f"""
        <div class="metric-box">
            <div style="font-size:1.2rem">{sport_badge(t_obj.get('sport_type',''))}</div>
            <div class="metric-label" style="margin-top:6px">Sport</div>
        </div>
        """, unsafe_allow_html=True)
    with col2:
        st.markdown(f"""
        <div class="metric-box">
            <div style="font-size:1.1rem">{status_badge(t_obj.get('status',''))}</div>
            <div class="metric-label" style="margin-top:6px">Status</div>
        </div>
        """, unsafe_allow_html=True)
    with col3:
        st.markdown(f"""
        <div class="metric-box">
            <div class="metric-value" style="font-size:1.2rem">
                {t_obj.get('start_date','—')} → {t_obj.get('end_date','—')}
            </div>
            <div class="metric-label" style="margin-top:6px">Duration</div>
        </div>
        """, unsafe_allow_html=True)

    st.markdown("<br>", unsafe_allow_html=True)

    tab_sched, tab_stand, tab_reg = st.tabs(["📅 Schedule & Results", "🥇 Standings", "📋 Registrations"])

    with tab_sched:
        matches = api_get("/matches", {"tournament_id": t_id})
        if not matches:
            st.info("No matches scheduled for this tournament.")
        else:
            for m in matches:
                t1s = m.get("team1_score","–") or "–"
                t2s = m.get("team2_score","–") or "–"
                w   = m.get("winner_name") or "TBD"
                st.markdown(f"""
                <div class="card">
                    <div style="display:flex; justify-content:space-between; align-items:center">
                        <div style="flex:1; text-align:right">
                            <span style="font-weight:700; color:#e2e8f0; font-size:1rem">
                                {m['team1_name']}
                            </span>
                        </div>
                        <div style="flex:0; padding:0 20px; text-align:center">
                            <div style="font-family:'Bebas Neue',sans-serif; font-size:1.8rem;
                                        color:#f59e0b; line-height:1">
                                {t1s} – {t2s}
                            </div>
                            {status_badge(m.get('status',''))}
                        </div>
                        <div style="flex:1; text-align:left">
                            <span style="font-weight:700; color:#e2e8f0; font-size:1rem">
                                {m['team2_name']}
                            </span>
                        </div>
                    </div>
                    <div style="margin-top:8px; font-size:0.78rem; color:#64748b; text-align:center">
                        📍 {m.get('venue_name','—')}, {m.get('city','—')}
                        &nbsp;|&nbsp; 🗓 {m.get('match_date','')[:16]}
                        {f"&nbsp;|&nbsp; 🏅 <b style='color:#f59e0b'>{w}</b>" if m.get('status')=='Completed' else ''}
                    </div>
                </div>
                """, unsafe_allow_html=True)

    with tab_stand:
        standings = api_get(f"/standings/{t_id}")
        if not standings:
            st.info("Standings not available yet — matches need to be completed.")
        else:
            rows = []
            for i, s in enumerate(standings):
                medal = ["🥇","🥈","🥉"][i] if i < 3 else f"{i+1}."
                rows.append({
                    "Pos":    medal,
                    "Team":   s["team_name"],
                    "P":      s["played"],
                    "W":      s["wins"],
                    "L":      s["losses"],
                    "D":      s["draws"],
                    "Pts":    s["wins"] * 3 + s["draws"],
                })
            df = pd.DataFrame(rows)
            st.dataframe(df, use_container_width=True, hide_index=True)

    with tab_reg:
        regs = api_get(f"/registrations/{t_id}")
        if not regs:
            st.info("No teams registered yet.")
        else:
            for r in regs:
                st.markdown(f"""
                <div class="card" style="padding:12px 20px">
                    <span style="color:#e2e8f0; font-weight:600">
                        {r['team_name']}
                    </span>
                    &nbsp;&nbsp;
                    {status_badge(r.get('status',''))}
                    <span style="float:right; color:#475569; font-size:0.8rem">
                        Reg #{r['registration_id']}
                    </span>
                </div>
                """, unsafe_allow_html=True)
